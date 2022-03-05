package velocorner.util

import velocorner.SecretConfig
import velocorner.storage.PsqlDbStorage

import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}
import scala.jdk.CollectionConverters.EnumerationHasAsScala

trait FlywaySupport {

  lazy val config = SecretConfig.load()

  def testPsqlDb(port: Int): PsqlDbStorage = FlywaySupport.copyInTemp("psql/migration") { tmpDir =>
    new PsqlDbStorage(
      dbUrl = s"jdbc:postgresql://localhost:$port/postgres",
      dbUser = "postgres",
      dbPassword = "test",
      flywayLocation = "filesystem:" + tmpDir.getAbsolutePath
    )
  }

  def localPsqlDb: PsqlDbStorage = FlywaySupport.copyInTemp(locations = "psql/migration", testMigrationHasPriority = false) { tmpDir =>
    new PsqlDbStorage(
      config.getPsqlUrl,
      config.getPsqlUser,
      config.getPsqlPassword,
      flywayLocation = "filesystem:" + tmpDir.getAbsolutePath
    )
  }
}

object FlywaySupport {

  // test resources are containing some overrides from the regular folder (V4__ip2nation.sql)
  // want to have test classes at the end, it means that the test resources have priority over the regular files
  // this helps to operate with smaller files & migrations in testing
  def copyInTemp[T](locations: String, testMigrationHasPriority: Boolean = true)(block: File => T): T = {
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
      .sortBy { path =>
        val isTest = path.getPath contains "test-classes"
        isTest && testMigrationHasPriority
      }

    val tmpDir = Files.createTempDirectory("migration").toFile

    // copy as side effect
    files.foreach { file =>
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
