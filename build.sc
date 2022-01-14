// build.sc
import mill._, scalalib._
import mill.scalalib.publish._
import coursier.maven.MavenRepository
import $ivy.`com.lihaoyi::mill-contrib-playlib:$MILL_VERSION`, mill.playlib._
import $ivy.`com.lihaoyi::mill-contrib-docker:$MILL_VERSION`
import $ivy.`com.lihaoyi::mill-contrib-buildinfo:$MILL_VERSION`
import contrib.docker.DockerModule
import mill.contrib.buildinfo.BuildInfo

// import from shared location
val projectScalaVersion = "2.13.4"

val catsVersion = "2.4.1"
val zioVersion = "1.0.4"
val shapelessVersion = "2.3.3"
val logbackVersion = "1.2.3"
val doobieVersion = "0.10.0"
val orientDbVersion = "3.1.8"
val mongoDbVersion = "4.1.0"
val rethinkDbVersion = "2.4.4"
val flywayVersion = "7.5.3"
val elasticVersion = "7.10.3"
val finatraVersion = "20.8.1"
val playFwVersion = "2.8.7"
val playWsVersion = "2.1.2" // standalone version
val playJsonVersion = "2.9.2"
val squantsVersion = "1.7.0"
val specsVersion = "4.10.3"
val mockitoVersion = "3.7.7"

val logging = Agg(
  ivy"ch.qos.logback:logback-classic::$logbackVersion",
  ivy"com.typesafe.scala-logging::scala-logging::3.9.2",
  ivy"org.codehaus.janino:janino::3.1.6", // conditional logback processing
  ivy"com.papertrailapp:logback-syslog4j:1.0.0"
)
val apacheCommons = Agg(
  ivy"commons-io:commons-io:2.6",
  ivy"commons-codec:commons-codec:1.14"
)
val cats = Agg(
  ivy"org.typelevel::cats-core::$catsVersion",
  ivy"org.typelevel::mouse::0.24"
)
val zio = Agg(
  ivy"dev.zio::zio::$zioVersion",
  ivy"dev.zio::zio-logging::0.5.6"
)
val storage = Agg(
  ivy"com.rethinkdb:rethinkdb-driver::$rethinkDbVersion",
  ivy"com.googlecode.json-simple:json-simple::1.1.1",
  ivy"org.mongodb.scala::mongo-scala-driver::$mongoDbVersion",
  ivy"com.orientechnologies:orientdb-client::$orientDbVersion",
  ivy"org.tpolecat::doobie-core::$doobieVersion",
  ivy"org.tpolecat::doobie-postgres::$doobieVersion",
  ivy"org.tpolecat::doobie-hikari::$doobieVersion",
  ivy"org.flywaydb:flyway-core::$flywayVersion"
)
val elastic4s = Agg(
  ivy"com.sksamuel.elastic4s::elastic4s-core::$elasticVersion",
  ivy"com.sksamuel.elastic4s::elastic4s-client-esjava::$elasticVersion",
  ivy"com.sksamuel.elastic4s::elastic4s-http-streams::$elasticVersion"
)
val specs2 = Agg(
  ivy"org.specs2::specs2-core::$specsVersion",
  ivy"org.specs2::specs2-junit::$specsVersion"
)

trait CommonModule extends SbtModule {
  def scalaVersion = T { projectScalaVersion }
  def scalacOptions = T { Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8") }
  def pomSettings = PomSettings(
    description = "The Cycling Platform",
    organization = "com.github.peregin",
    url = "https://github.com/peregin/velocorner.com",
    licenses = Seq(License.MIT),
    scm = SCM(
      "git://github.com/peregin/velocorner.com.git",
      "scm:git://github.com/peregin/velocorner.com.git"
    ),
    developers = Seq(
      Developer("peregin", "Levi", "https://github.com/peregin")
    )
  )
}

object dataSearch extends CommonModule {
  def millSourcePath = velocorner.millSourcePath / "data-search"
  def ivyDeps = T { elastic4s }
  def moduleDeps = Seq(dataProvider)
  object test extends Tests {
    def testFrameworks = Seq("org.specs2.runner.Specs2Framework")
    def ivyDeps = T {
      Agg(
        ivy"com.sksamuel.elastic4s::elastic4s-testkit::$elasticVersion"
      )
    }
    def moduleDeps = super.moduleDeps ++ Seq(dataProvider.test)
  }
}

object dataProvider extends CommonModule {
  def millSourcePath = velocorner.millSourcePath / "data-provider"
  def ivyDeps = T {
    Agg(
      ivy"com.typesafe.play::play-json::$playJsonVersion",
      ivy"com.typesafe.play::play-json-joda::$playJsonVersion",
      ivy"com.typesafe.play::play-ahc-ws-standalone::$playWsVersion",
      ivy"com.typesafe.play::play-ws-standalone-json::$playWsVersion",
      ivy"org.typelevel::squants::$squantsVersion",
      ivy"ai.x::play-json-extensions::0.42.0"
    ) ++ logging ++ storage ++ apacheCommons ++ cats ++ zio
  }
  override def repositories() = super.repositories ++ Seq(
    MavenRepository("https://repo.typesafe.com/typesafe/releases/")
  )
  object test extends Tests {
    def testFrameworks = Seq("org.specs2.runner.Specs2Framework")
    def ivyDeps = T {
      Agg(
        ivy"com.opentable.components:otj-pg-embedded::0.13.3"
      ) ++ specs2
    }
  }
}

object webApp extends CommonModule with BuildInfo with PlayModule with DockerModule {
  def millSourcePath = velocorner.millSourcePath / "web-app"
  override def ivyDeps = T {
    super.ivyDeps() ++ Agg(filters()) ++ Agg(
      ivy"com.typesafe.play::play-ehcache::${playVersion()}",
      ivy"com.github.jwt-scala::jwt-play-json::9.0.2",
      ivy"org.scala-lang.modules::scala-xml::1.2.0"
    )
  }
  override def playVersion: T[String] = T { playFwVersion }
  override def twirlVersion = T { "1.5.0" }
  override def moduleDeps = super.moduleDeps ++ Seq(dataProvider)
  override def buildInfoPackageName = Some("velocorner.build")
  override def buildInfoMembers = T {
    Map(
      "buildTime" -> java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now()),
      "version" -> "n/a",
      "scalaVersion" -> projectScalaVersion,
      "sbtVersion" -> "n/a",
      "catsVersion" -> catsVersion,
      "elasticVersion" -> elasticVersion,
      "playVersion" -> playFwVersion,
      "gitHash" -> "n/a"
    )
  }
  object test extends PlayTests
  object docker extends DockerConfig {
    def baseImage = T { "openjdk:8-jre-alpine" }
    def tags = T { List("peregin/web-app") }
  }
}

object velocorner extends CommonModule {
  def millSourcePath = os.pwd
  def moduleDeps = Seq(dataSearch, dataProvider, webApp)
}
