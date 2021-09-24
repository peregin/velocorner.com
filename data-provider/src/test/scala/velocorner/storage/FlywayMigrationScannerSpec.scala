package velocorner.storage

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import scala.jdk.CollectionConverters.EnumerationHasAsScala

class FlywayMigrationScannerSpec extends AnyWordSpec with Matchers {

  "scanner" should {

    // test resources are containing some overrides from the regular folder (V4__ip2nation.sql)
    "read resource folder" in {

      // want to have test classes at the end
      val urls = getClass.getClassLoader.getResources("psql/migration").asScala
      val files = urls
        .flatMap { u =>
          val f = new File(u.toURI)
          if (f.exists() && f.isDirectory) {
            f.listFiles()
          } else Nil
        }
        .toList
        .sortBy(_.getPath contains "test-classes")

      files.foreach(println)
    }
  }
}
