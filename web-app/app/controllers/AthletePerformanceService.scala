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
      logger.debug(s"prompt context $context")
      logger.debug("---------------------------------------------------------")
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
      val avgSpeedKmh = a.average_speed.map(v => f"${v * 3.6}%.1f").getOrElse("n/a")
      val avgHr = a.average_heartrate.map(v => f"$v%.0f").getOrElse("n/a")
      val avgPower = a.average_watts.map(v => f"$v%.0f").getOrElse("n/a")
      val distanceKm = f"${a.distance / 1000}%.1f"
      val elevationM = f"${a.total_elevation_gain}%.0f"
      s"${ix + 1}. date: $localDate | sport: ${a.`type`} | name: ${a.name} | distance: $distanceKm km | elevation: $elevationM m | duration: ${a.moving_time / 60} min | speed: $avgSpeedKmh km/h | hr $avgHr bpm | power $avgPower W"
    }

    s"""You are a cycling performance coach writing a short direct message to the athlete.
       |
       |Task:
       |Review the recent activities provided below and assess the athlete’s current performance trend.
       |
       |Instructions:
       |- Base the assessment only on the supplied activities.
       |- Compare recency, duration, elevation, speed, heart rate, and power when available.
       |- If data is insufficient or mixed across sports, say so explicitly and avoid overclaiming.
       |- Treat non-cycling activities as supporting fitness, but give priority to cycling-specific evidence.
       |- Keep the tone practical, and personalized using “you”.
       |- Keep the message body under 120 words.
       |
       |Return your answer in this exact JSON format:
       |{
       |  "trend": {
       |    "label": "improving | stable | declining | inconclusive",
       |    "evidence": "1-2 sentence explanation"
       |  },
       |  "strengths": [
       |    "strength 1",
       |    "strength 2"
       |  ],
       |  "recommendations": [
       |    "recommendation 1",
       |    "recommendation 2"
       |  ],
       |  "message": "A short direct message to the athlete in under 120 words."
       |}
       |
       |Rules:
       |- Always return valid JSON only.
       |- Use short, concrete strings in strengths and recommendations.
       |- Include 1 to 2 recommendations only with encouragement.
       |- Do not invent missing values.
       |- If only one cycling activity is available, set trend.label to "inconclusive" unless the evidence is unusually strong.
       |
       |Activities:
       |${lines.mkString("\n")}
       |""".stripMargin
  }

  private def generateSummary(prompt: String): Future[Option[String]] = {
    connectivity.secretConfig.getAiChatProvider match {
      case "gemini" | "google" =>
        generateGeminiSummary(prompt)
      case "openrouter" =>
        generateOpenRouterSummary(prompt)
      case "peregin" =>
        generatePereginSummary(prompt)
      case provider =>
        logger.warn(s"Unknown AI chat provider '$provider', falling back to peregin")
        generatePereginSummary(prompt)
    }
  }

  private def generatePereginSummary(prompt: String): Future[Option[String]] = {
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
    executeSummaryRequest(request, "AI chat endpoint")
  }

  private def generateGeminiSummary(prompt: String): Future[Option[String]] = {
    val baseUrl = connectivity.secretConfig.getAiGeminiBaseUrl.stripSuffix("/")
    val model = connectivity.secretConfig.getAiGeminiModel
    val apiKey = connectivity.secretConfig.getAiGeminiApiKey
    val url = s"$baseUrl/$model:generateContent"
    val reqBody = Json
      .obj(
        "contents" -> Json.arr(
          Json.obj(
            "parts" -> Json.arr(
              Json.obj("text" -> prompt)
            )
          )
        )
      )
      .toString()

    val requestBuilder = HttpRequest
      .newBuilder()
      .uri(URI.create(url))
      .header("Content-Type", "application/json")
      .header("Accept", "application/json")

    apiKey.foreach(key => requestBuilder.header("x-goog-api-key", key))

    val request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(reqBody, StandardCharsets.UTF_8)).build()
    executeSummaryRequest(request, "Gemini API")
  }

  private def generateOpenRouterSummary(prompt: String): Future[Option[String]] = {
    val url = connectivity.secretConfig.getAiOpenRouterUrl
    val apiKey = connectivity.secretConfig.getAiOpenRouterApiKey
    val model = connectivity.secretConfig.getAiOpenRouterModel
    val reqBody = Json
      .obj(
        "model" -> model,
        "messages" -> Json.arr(
          Json.obj(
            "role" -> "user",
            "content" -> prompt
          )
        )
      )
      .toString()

    val requestBuilder = HttpRequest
      .newBuilder()
      .uri(URI.create(url))
      .header("Content-Type", "application/json")
      .header("Accept", "application/json")

    apiKey.foreach(key => requestBuilder.header("Authorization", s"Bearer $key"))
    connectivity.secretConfig.getAiOpenRouterReferer.foreach(value => requestBuilder.header("HTTP-Referer", value))
    connectivity.secretConfig.getAiOpenRouterTitle.foreach(value => requestBuilder.header("X-Title", value))

    val request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(reqBody, StandardCharsets.UTF_8)).build()
    executeSummaryRequest(request, "OpenRouter API")
  }

  private def executeSummaryRequest(request: HttpRequest, endpointLabel: String): Future[Option[String]] = {
    httpClient
      .sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      .asScala
      .map { response =>
        if (response.statusCode() / 100 == 2) {
          val text = extractText(response.body())
          text.filter(_.nonEmpty)
        } else {
          val headers = response.headers().map().asScala.toSeq
            .sortBy(_._1)
            .map { case (name, values) => s"$name=${values.asScala.mkString("[", ", ", "]")}" }
            .mkString(", ")
          logger.warn(
            s"$endpointLabel returned ${response.statusCode()} headers={$headers} body=${response.body()}"
          )
          None
        }
      }
      .recover {
        case NonFatal(ex) =>
          logger.warn(s"$endpointLabel request failed", ex)
          None
      }
  }

  private def extractText(rawBody: String): Option[String] = {
    def fromJson(js: JsValue): Option[String] =
      (js \ "candidates").asOpt[JsArray]
        .flatMap(_.value.headOption)
        .flatMap(candidate => (candidate \ "content" \ "parts").asOpt[JsArray])
        .flatMap(_.value.headOption)
        .flatMap(part => (part \ "text").asOpt[String])
        .orElse((js \ "response").asOpt[String])
        .orElse((js \ "text").asOpt[String])
        .orElse((js \ "content").asOpt[String])
        .orElse((js \ "message").asOpt[String])
        .orElse(
          (js \ "choices").asOpt[JsArray]
            .flatMap(_.value.headOption)
            .flatMap(choice => (choice \ "message" \ "content").asOpt[String])
        )
        .orElse(
          (js \ "choices").asOpt[JsArray]
            .flatMap(_.value.headOption)
            .flatMap(choice => (choice \ "text").asOpt[String])
        )
        .orElse(js match {
          case JsObject(fields) =>
            fields.values.toList.flatMap(fromJson).headOption
          case JsArray(values) =>
            values.toList.flatMap(fromJson).headOption
          case JsString(value) => Option(value)
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
