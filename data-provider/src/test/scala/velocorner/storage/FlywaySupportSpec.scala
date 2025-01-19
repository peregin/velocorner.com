package velocorner.storage

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.util.FlywaySupport

class FlywaySupportSpec extends AnyWordSpec with Matchers {

  "scanner" should {

    "read resource folder" in
      // test resources are containing some overrides from the regular folder (V4__ip2nation.sql)
      // want to have test classes at the end
      FlywaySupport.copyInTemp("psql/migration") { tmpDir =>
        val files = tmpDir.listFiles().filterNot(_.isDirectory).filter(_.exists())
        files.map(_.getName) must contain atLeastOneElementOf List("V4__ip2nation.sql")

        val overriddenFile = files.find(_.getName == "V4__ip2nation.sql").get
        overriddenFile.length() must be < 600L
      }
  }
}
