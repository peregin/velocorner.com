package controllers.auth

import controllers.auth.JwtUser.issuer
import org.joda.time.{DateTime, DateTimeZone}
import pdi.jwt.{JwtAlgorithm, JwtJson, JwtOptions}
import play.api.libs.json._
import velocorner.api.Account
import velocorner.model.strava.Athlete

import java.time.Clock
import scala.util.{Failure, Success}

object JwtUser {

  implicit val format: Format[JwtUser] = Format[JwtUser](Json.reads[JwtUser], Json.writes[JwtUser])

  val issuer = "velocorner"

  def toJwtUser(account: Account): JwtUser = JwtUser(id = account.athleteId)
  def toJwtUser(athlete: Athlete): JwtUser = JwtUser(id = athlete.id)

  def fromToken(token: String)(implicit secret: String): JwtUser =
    JwtJson.decode(token, secret, Seq(JwtAlgorithm.HS256), JwtOptions(expiration = true)) match {
      case Success(claim) =>
        if (!claim.isValid(issuer)(Clock.systemUTC())) throw new SecurityException("token expired")

        val json = Json.parse(claim.content)
        (json \ "user").get.validate[JwtUser] match {
          case JsSuccess(that, _) => that
          case JsError(errors)    => throw new SecurityException(s"unable to parse token because $errors from $json")
        }
      case Failure(exception) => throw exception
    }
}

case class JwtUser(id: Long) {

  def toToken(implicit secret: String): String = {
    val now = DateTime.now(DateTimeZone.UTC)
    val exp = now.plusDays(30)

    val json = Json.toJson(this)
    val obj = JsObject(
      Seq(
        "user" -> json,
        "exp" -> JsNumber(exp.getMillis),
        "iat" -> JsNumber(now.getMillis),
        "iss" -> JsString(issuer)
      )
    )
    val header = Json.obj(("typ", "JWT"), ("alg", "HS256"))
    JwtJson.encode(header, obj, secret)
  }
}
