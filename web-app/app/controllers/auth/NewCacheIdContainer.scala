package controllers.auth

import java.security.SecureRandom
import javax.inject.Inject

import AuthController.AuthenticityToken
import play.api.cache.SyncCacheApi

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.Random

class NewCacheIdContainer[Id: ClassTag] @Inject()(cache: SyncCacheApi) {

  private[auth] val tokenSuffix = ":token"
  private[auth] val userIdSuffix = ":userId"
  private[auth] val random = new Random(new SecureRandom())

  def startNewSession(userId: Id, timeoutInSeconds: Int): AuthenticityToken = {
    removeByUserId(userId)
    val token = generate
    store(token, userId, timeoutInSeconds)
    token
  }

  @tailrec
  private[auth] final def generate: AuthenticityToken = {
    val table = "abcdefghijklmnopqrstuvwxyz1234567890_.~*'()"
    val token = Iterator.continually(random.nextInt(table.size)).map(table).take(64).mkString
    if (get(token).isDefined) generate else token
  }

  private[auth] def removeByUserId(userId: Id) {
    cache.get[String](userId.toString + userIdSuffix) foreach unsetToken
    unsetUserId(userId)
  }

  def remove(token: AuthenticityToken) {
    get(token) foreach unsetUserId
    unsetToken(token)
  }

  private[auth] def unsetToken(token: AuthenticityToken) {
    cache.remove(token + tokenSuffix)
  }
  private[auth] def unsetUserId(userId: Id) {
    cache.remove(userId.toString + userIdSuffix)
  }

  def get(token: AuthenticityToken) = cache.get[Id](token + tokenSuffix)

  private[auth] def store(token: AuthenticityToken, userId: Id, timeoutInSeconds: Int) {
    cache.set(token + tokenSuffix, userId, timeoutInSeconds seconds)
    cache.set(userId.toString + userIdSuffix, token, timeoutInSeconds seconds)
  }

  def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int) {
    get(token).foreach(store(token, _, timeoutInSeconds))
  }

}

