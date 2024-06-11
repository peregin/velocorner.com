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

    "mark disabled missing withings enabling" in {
      val conf = new SecretConfig(ConfigFactory.parseString(""))
      conf.isServiceEnabled(ServiceProvider.Withings) mustBe false
    }

    "mark enabled explicit withings setup" in {
      val testConfig =
        """
          |withings.enabled=true
        """.stripMargin
      val conf = new SecretConfig(ConfigFactory.parseString(testConfig))
      conf.isServiceEnabled(ServiceProvider.Withings) mustBe true
    }

    "check crawler when explicit setup is not provided" in {
      val conf = new SecretConfig(ConfigFactory.parseString(""))
      conf.isServiceEnabled(ServiceProvider.Crawler) mustBe false
    }
  }
}
