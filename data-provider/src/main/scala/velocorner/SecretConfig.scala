package velocorner

import com.typesafe.config.{Config, ConfigFactory}
import velocorner.SecretConfig.PimpMyConfig

/**
 * Created by levi on 29/03/15.
 * Read configs from environment variables and merge it with the default application.conf
 */
object SecretConfig {

  def load(config: Config): SecretConfig = new SecretConfig(config)

  def load(): SecretConfig = load(ConfigFactory.load())

  implicit class PimpMyConfig(config: Config) {

    def getOptAs[T](path: String): Option[T] = {
      val maybePath = Some(path).filter(config.hasPath)
      maybePath.map(config.getAnyRef).map(_.asInstanceOf[T])
    }

    def getOptBoolean(path: String, default: Boolean): Boolean = getOptAs[String](path).map(_.toBoolean).getOrElse(default)
  }

}

case class SecretConfig(configProps: Config) {

  private val config = ConfigFactory.systemEnvironment().withFallback(configProps).resolve()

  def isServiceEnabled(application: ServiceProvider.Value): Boolean = config.getOptBoolean(s"${application.toString.toUpperCase}_ENABLED", default = false)

  def getAuthId(application: ServiceProvider.Value): String = config.getString(s"${application.toString.toUpperCase}_API_ID")

  def getAuthToken(application: ServiceProvider.Value): String = config.getString(s"${application.toString.toUpperCase}_API_TOKEN")

  def getAuthSecret(application: ServiceProvider.Value): String = config.getString(s"${application.toString.toUpperCase}_API_SECRET")

  def getAuthCallbackUrl(application: ServiceProvider.Value): String = config.getString(s"${application.toString.toUpperCase}_API_CALLBACK")

  def getStorageType: Option[String] = config.getOptAs[String]("storage")

  def getPsqlUrl: String = config.getString("DB_URL")

  def getPsqlUser: String = config.getString("DB_USER")

  def getPsqlPassword: String = config.getString("DB_PASSWORD")

  def getProxyHost: Option[String] = config.getOptAs[String]("proxy.host")

  def getProxyPort: Option[Int] = config.getOptAs[Int]("proxy.port")

  def getProxyUser: Option[String] = config.getOptAs[String]("proxy.user")

  def getProxyPassword: Option[String] = config.getOptAs[String]("proxy.password")

  def getJwtSecret: String = config.getString("JWT_SECRET")

  def getElasticSearchUrl: String = config.getString("elasticsearch.url")
  def getZincUrl: String = config.getString("zinc.url")
  def getZincUser: String = config.getString("zinc.user")
  def getZincPassword: String = config.getString("zinc.password")

  def getCrawlerUrl: String = config.getString("CRAWLER_URL")
  def getRatesUrl: String = config.getString("RATES_URL")

  def getAiChatProvider: String =
    config
      .getOptAs[String]("AI_CHAT_PROVIDER")
      .orElse(config.getOptAs[String]("AI_PROVIDER"))
      .map(_.trim.toLowerCase)
      .filter(_.nonEmpty)
      .getOrElse("openrouter")

  def getAiChatUrl: String = config.getOptAs[String]("AI_CHAT_URL").getOrElse("https://ai.peregin.com/chat")
  def getAiChatToken: Option[String] = config.getOptAs[String]("AI_CHAT_API_TOKEN").orElse(config.getOptAs[String]("AI_API_TOKEN"))
  def getAiGeminiModel: String = config.getOptAs[String]("AI_GEMINI_MODEL").getOrElse("gemini-2.0-flash-lite")
  def getAiGeminiBaseUrl: String =
    config.getOptAs[String]("AI_GEMINI_BASE_URL").getOrElse("https://generativelanguage.googleapis.com/v1beta/models")
  def getAiGeminiApiKey: Option[String] =
    config
      .getOptAs[String]("AI_GEMINI_API_KEY")
      .orElse(config.getOptAs[String]("GEMINI_API_KEY"))
      .orElse(config.getOptAs[String]("AI_API_TOKEN"))
  def getAiOpenRouterUrl: String =
    config.getOptAs[String]("AI_OPENROUTER_URL").getOrElse("https://openrouter.ai/api/v1/chat/completions")
  def getAiOpenRouterModel: String =
    config.getOptAs[String]("AI_OPENROUTER_MODEL").getOrElse("google/gemini-2.0-flash-001")
  def getAiOpenRouterApiKey: Option[String] =
    config
      .getOptAs[String]("AI_OPENROUTER_API_KEY")
      .orElse(config.getOptAs[String]("OPENROUTER_API_KEY"))
      .orElse(config.getOptAs[String]("AI_API_TOKEN"))
  def getAiOpenRouterReferer: Option[String] =
    config.getOptAs[String]("AI_OPENROUTER_REFERER").orElse(config.getOptAs[String]("OPENROUTER_REFERER"))
  def getAiOpenRouterTitle: Option[String] =
    config.getOptAs[String]("AI_OPENROUTER_TITLE").orElse(config.getOptAs[String]("OPENROUTER_TITLE"))
}
