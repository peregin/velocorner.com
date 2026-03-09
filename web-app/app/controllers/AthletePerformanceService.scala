package controllers

import play.Logger
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}
import velocorner.api.strava.Activity
import velocorner.api.{Account, AthletePerformance, AthletePerformanceAnalysis}

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._
import scala.util.control.NonFatal

@Singleton
class AthletePerformanceService @Inject() (connectivity: ConnectivitySettings)(implicit ec: ExecutionContext) {

  private val logger = Logger.of(this.getClass)
  private val runningEvaluations = ConcurrentHashMap.newKeySet[String]().asScala
  private val httpClient = HttpClient.newBuilder().build()

  private val maxRecentActivities = 40

  def getPerformance(account: Account): Future[AthletePerformance] = {
    val storage = connectivity.getStorage
    val analysisStorage = storage.getAthletePerformanceAnalysisStorage
    val who = s"${account.displayName}#${account.athleteId}"
    logger.info(s"performance request received from $who")

    for {
      recent <- storage.listRecentActivities(account.athleteId, maxRecentActivities)
      sorted = recent.toList.sortBy(_.getStartDateLocal().getMillis).reverse
      context = buildContext(sorted)
      _ = logger.info(s"performance context for $who: recentActivities=${sorted.size}, hasContext=${context.nonEmpty}")
      result <- context match {
        case None =>
          logger.info(s"no activity context for $who, trying latest cached analysis")
          analysisStorage.latest(account.athleteId).map {
            case Some(analysis) =>
              logger.info(s"returning cached analysis for $who createdAt=${analysis.createdAt}")
              AthletePerformance(
                summary = Some(analysis.summary),
                evaluating = false,
                basedOn = analysis.basedOn,
                createdAt = Some(analysis.createdAt)
              )
            case None =>
              logger.info(s"no cached analysis found for $who")
              AthletePerformance(
                summary = Some("No recent activities available for evaluation yet."),
                evaluating = false,
                basedOn = "No recent activities",
                createdAt = None
              )
          }

        case Some(ctx) =>
          logger.info(s"activity context for $who fingerprint=${fingerprintLabel(ctx.fingerprint)} basedOn='${ctx.basedOn}'")
          analysisStorage.byFingerprint(account.athleteId, ctx.fingerprint).flatMap {
            case Some(current) =>
              logger.info(
                s"cache hit for $who fingerprint=${fingerprintLabel(ctx.fingerprint)} createdAt=${current.createdAt}, returning existing result"
              )
              Future.successful(
                AthletePerformance(
                  summary = Some(current.summary),
                  evaluating = false,
                  basedOn = current.basedOn,
                  createdAt = Some(current.createdAt)
                )
              )

            case None =>
              logger.info(s"cache miss for $who fingerprint=${fingerprintLabel(ctx.fingerprint)}, triggering async generation")
              analysisStorage.latest(account.athleteId).map { previous =>
                runIfNeeded(account.athleteId, ctx)
                logger.info(
                  s"returning previous result while evaluation runs for $who hasPrevious=${previous.nonEmpty}"
                )
                AthletePerformance(
                  summary = previous.map(_.summary),
                  evaluating = true,
                  basedOn = previous.map(_.basedOn).getOrElse(ctx.basedOn),
                  createdAt = previous.map(_.createdAt)
                )
              }
          }
      }
    } yield result
  }

  private case class EvaluationContext(fingerprint: String, basedOn: String, prompt: String)

  private def buildContext(activities: List[Activity]): Option[EvaluationContext] = {
    val now = org.joda.time.DateTime.now()
    val twoWeeksAgo = now.minusWeeks(2)
    val fromLastTwoWeeks = activities.filter(_.getStartDateLocal().isAfter(twoWeeksAgo)).take(5)
    val selected = if (fromLastTwoWeeks.nonEmpty) fromLastTwoWeeks else activities.take(5)
    Option.when(selected.nonEmpty) {
      val basedOn =
        if (fromLastTwoWeeks.nonEmpty) s"${selected.size} activities from the last 2 weeks"
        else s"${selected.size} latest activities"
      val fingerprint = sha256(
        selected
          .map(a => s"${a.id}:${a.getStartDateLocal().getMillis}:${a.distance}:${a.moving_time}:${a.total_elevation_gain}")
          .mkString("|")
      )
      val prompt = buildPrompt(selected)
      EvaluationContext(fingerprint = fingerprint, basedOn = basedOn, prompt = prompt)
    }
  }

  private def runIfNeeded(athleteId: Long, context: EvaluationContext): Unit = {
    val key = s"$athleteId:${context.fingerprint}"
    if (runningEvaluations.add(key)) {
      logger.info(s"starting AI performance evaluation athlete=$athleteId fingerprint=${fingerprintLabel(context.fingerprint)}")
      val analysisStorage = connectivity.getStorage.getAthletePerformanceAnalysisStorage
      generateSummary(context.prompt)
        .flatMap {
          case Some(summary) =>
            logger.info(s"AI response ready athlete=$athleteId fingerprint=${fingerprintLabel(context.fingerprint)}, storing analysis")
            analysisStorage.store(
              AthletePerformanceAnalysis(
                athleteId = athleteId,
                fingerprint = context.fingerprint,
                basedOn = context.basedOn,
                summary = summary.trim,
                createdAt = org.joda.time.DateTime.now()
              )
            )
          case None =>
            logger.warn(s"AI response empty athlete=$athleteId fingerprint=${fingerprintLabel(context.fingerprint)}")
            Future.unit
        }
        .recover { case NonFatal(ex) => logger.warn(s"AI performance evaluation failed for athlete=$athleteId", ex) }
        .foreach { _ =>
          runningEvaluations.remove(key)
          logger.info(s"AI performance evaluation finished athlete=$athleteId fingerprint=${fingerprintLabel(context.fingerprint)}")
        }
    } else {
      logger.info(
        s"AI performance evaluation already running athlete=$athleteId fingerprint=${fingerprintLabel(context.fingerprint)}, skipping duplicate trigger"
      )
    }
  }

  private def fingerprintLabel(fingerprint: String): String = fingerprint.take(12)

  private def buildPrompt(activities: List[Activity]): String = {
    val lines = activities.zipWithIndex.map { case (a, ix) =>
      val localDate = a.getStartDateLocal().toString("yyyy-MM-dd")
      val avgSpeedKmh = a.average_speed.map(v => f"${v * 3.6}%.1f km/h").getOrElse("n/a")
      val avgHr = a.average_heartrate.map(v => f"${v}%.0f bpm").getOrElse("n/a")
      val avgPower = a.average_watts.map(v => f"${v}%.0f W").getOrElse("n/a")
      val distanceKm = f"${a.distance / 1000}%.1f"
      val elevationM = f"${a.total_elevation_gain}%.0f"
      s"${ix + 1}. $localDate | ${a.`type`} | ${a.name} | $distanceKm km | $elevationM m | ${a.moving_time / 60} min | speed $avgSpeedKmh | hr $avgHr | power $avgPower"
    }

    s"""You are a cycling performance coach.
       |Evaluate this athlete's recent performance based on the activities below.
       |Write a concise summary with:
       |- trend assessment (improving / stable / declining) with evidence
       |- strengths
       |- one or two concrete recommendations for the next week
       |Keep it practical, neutral, and under 120 words.
       |
       |Activities:
       |${lines.mkString("\n")}
       |""".stripMargin
  }

  private def generateSummary(prompt: String): Future[Option[String]] = {
    val url = connectivity.secretConfig.getAiChatUrl
    val token = connectivity.secretConfig.getAiChatToken
    val reqBody = Json.obj("message" -> prompt).toString()

    val requestBuilder = HttpRequest
      .newBuilder()
      .uri(URI.create(url))
      .header("Content-Type", "application/json")
      .header("Accept", "application/json")

    token.foreach(t => requestBuilder.header("Authorization", s"Bearer $t"))

    val request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(reqBody, StandardCharsets.UTF_8)).build()

    httpClient
      .sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      .asScala
      .map { response =>
        if (response.statusCode() / 100 == 2) {
          val text = extractText(response.body())
          text.filter(_.nonEmpty)
        } else {
          logger.warn(s"AI chat endpoint returned ${response.statusCode()}")
          None
        }
      }
      .recover {
        case NonFatal(ex) =>
          logger.warn("AI chat request failed", ex)
          None
      }
  }

  private def extractText(rawBody: String): Option[String] = {
    def fromJson(js: JsValue): Option[String] =
      (js \ "response").asOpt[String]
        .orElse((js \ "text").asOpt[String])
        .orElse((js \ "content").asOpt[String])
        .orElse((js \ "message").asOpt[String])
        .orElse(((js \ "choices")(0) \ "message" \ "content").asOpt[String])
        .orElse(((js \ "choices")(0) \ "text").asOpt[String])
        .orElse(js match {
          case JsObject(fields) =>
            fields.values.toList.flatMap(fromJson).headOption
          case JsArray(values) =>
            values.toList.flatMap(fromJson).headOption
          case JsString(value) =>
            Some(value)
          case _ =>
            None
        })

    scala.util.Try(Json.parse(rawBody)).toOption.flatMap(fromJson).orElse(Option(rawBody).map(_.trim).filter(_.nonEmpty))
  }

  private def sha256(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.digest(value.getBytes(StandardCharsets.UTF_8)).map("%02x".format(_)).mkString
  }
}
