package velocorner

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SecretConfigSpec extends AnyWordSpec with Matchers {

  "config" should {

    "read proxy settings" in {

      val testConfig =
        """
          |proxy.host=localhost
          |proxy.port=9999
        """.stripMargin
      val conf = new SecretConfig(ConfigFactory.parseString(testConfig))
      conf.getProxyHost mustBe Some("localhost")
      conf.getProxyPort mustBe Some(9999)
    }

    "ignore proxy settings" in {
      val testConfig =
        """
          |key=value
        """.stripMargin
      val conf = new SecretConfig(ConfigFactory.parseString(testConfig))
      conf.getProxyHost mustBe empty
      conf.getProxyPort mustBe empty
    }

    "mark disabled missing withings enabling" ignore {
      val conf = new SecretConfig(ConfigFactory.parseString(""))
      conf.isServiceEnabled(ServiceProvider.Withings) mustBe false
    }

    "mark enabled explicit withings setup" ignore {
      val testConfig =
        """
          |WITHINGS_ENABLED="true"
        """.stripMargin
      val conf = new SecretConfig(ConfigFactory.parseString(testConfig))
      conf.isServiceEnabled(ServiceProvider.Withings) mustBe true
    }

    "check crawler when explicit setup is not provided" in {
      val conf = new SecretConfig(ConfigFactory.parseString(""))
      conf.isServiceEnabled(ServiceProvider.Crawler) mustBe false
    }

    "default AI chat provider to peregin" in {
      val conf = new SecretConfig(ConfigFactory.parseString(""))
      conf.getAiChatProvider mustBe "openrouter"
    }

    "read Gemini AI chat settings" in {
      val testConfig =
        """
          |AI_CHAT_PROVIDER=gemini
          |AI_GEMINI_MODEL=gemini-2.5-flash
          |AI_GEMINI_API_KEY=test-key
        """.stripMargin
      val conf = new SecretConfig(ConfigFactory.parseString(testConfig))
      conf.getAiChatProvider mustBe "gemini"
      conf.getAiGeminiModel mustBe "gemini-2.5-flash"
      conf.getAiGeminiApiKey mustBe Some("test-key")
    }

    "read OpenRouter AI chat settings" in {
      val testConfig =
        """
          |AI_CHAT_PROVIDER=openrouter
          |AI_OPENROUTER_MODEL=google/gemini-2.5-flash
          |AI_OPENROUTER_API_KEY=test-key
          |AI_OPENROUTER_REFERER="https://velocorner.com"
          |AI_OPENROUTER_TITLE="Velocorner"
        """.stripMargin
      val conf = new SecretConfig(ConfigFactory.parseString(testConfig))
      conf.getAiChatProvider mustBe "openrouter"
      conf.getAiOpenRouterModel mustBe "google/gemini-2.5-flash"
      conf.getAiOpenRouterApiKey mustBe Some("test-key")
      conf.getAiOpenRouterReferer mustBe Some("https://velocorner.com")
      conf.getAiOpenRouterTitle mustBe Some("Velocorner")
    }
  }
}
