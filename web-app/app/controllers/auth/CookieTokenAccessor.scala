package controllers.auth

import StravaController.AuthenticityToken
import play.api.mvc.{Cookie, DiscardingCookie, RequestHeader, Result}

class CookieTokenAccessor(
    protected val cookieName: String,
    protected val cookieSecureOption: Boolean = false,
    protected val cookieHttpOnlyOption: Boolean = true,
    protected val cookieDomainOption: Option[String] = None,
    protected val cookiePathOption: String = "/",
    protected val cookieMaxAge: Option[Int] = None
) {

  def put(token: AuthenticityToken)(result: Result): Result = {
    val c = Cookie(cookieName, token, cookieMaxAge, cookiePathOption, cookieDomainOption, cookieSecureOption, cookieHttpOnlyOption)
    result.withCookies(c)
  }

  def extract(request: RequestHeader): Option[AuthenticityToken] =
    request.cookies.get(cookieName).map(_.value)

  def delete(result: Result)(implicit request: RequestHeader): Result =
    result.discardingCookies(DiscardingCookie(cookieName))
}
