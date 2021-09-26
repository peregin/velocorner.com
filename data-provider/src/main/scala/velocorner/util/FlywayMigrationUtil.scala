package velocorner.util

import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}
import scala.jdk.CollectionConverters.EnumerationHasAsScala

object FlywayMigrationUtil {

  // test resources are containing some overrides from the regular folder (V4__ip2nation.sql)
  // want to have test classes at the end
  // block gets the tmp dir name
  def copyInTemp[T](locations: String)(block: File => T): Unit = {
    // test resources are containing some overrides from the regular folder (V4__ip2nation.sql)
    // want to have test classes at the end
    val urls = getClass.getClassLoader.getResources(locations).asScala
    val files = urls
      .flatMap { u =>
        val f = new File(u.toURI)
        if (f.exists() && f.isDirectory) {
          f.listFiles()
        } else Nil
      }
      .toList
      .sortBy(_.getPath contains "test-classes")

    val tmpDir = Files.createTempDirectory("migration").toFile

    // copy as side effect
    files.foreach{ file =>
      val from = Paths.get(file.toURI)
      val to = Paths.get(new File(tmpDir, file.getName).toURI)
      Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)
    }

    try {
      block(tmpDir)
    } finally {
      tmpDir.delete()
    }
  }
}
