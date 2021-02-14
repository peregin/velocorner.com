package controllers.auth

import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.libs.json._
import velocorner.model.Account
import velocorner.util.JsonIo

import scala.util.{Failure, Success}

object JwtUser {

  implicit val format = Format[JwtUser](Json.reads[JwtUser], Json.writes[JwtUser])

  val secret = "key"

  def toJwtUser(account: Account) =
    JwtUser(
      name = account.displayName,
      location = account.displayLocation,
      avatarUrl = account.avatarUrl
    )

  def fromToken(token: String): JwtUser = {
    JwtJson.decode(token, secret, Seq(JwtAlgorithm.HS256)) match {
      case Success(claim) =>
        val json = Json.parse(claim.content)
        (json \ "user").get.validate[JwtUser] match {
          case JsSuccess(that, _) => that
          case JsError(errors)    => throw new SecurityException(s"unable to parse token because $errors from $json")
        }
      case Failure(exception) => throw exception
    }
  }
}

case class JwtUser(name: String, location: String, avatarUrl: String) {

  def toToken() = {
    val json = Json.toJson(this)
    val obj = JsObject(Seq("user" -> json))
    val header = Json.obj(("typ", "JWT"), ("alg", "HS256"))
    JwtJson.encode(header, obj, JwtUser.secret)
  }
}
