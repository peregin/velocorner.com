import ScalafmtExtensionPlugin.autoImport.scalafmtGenerated
import play.sbt.routes.RoutesCompiler.autoImport._
import sbtbuildinfo.BuildInfoKeys
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import play.sbt.PlayImport._

val rethinkClient = Seq(
  "com.rethinkdb" % "rethinkdb-driver" % Dependencies.rethinkDbVersion,
  "com.googlecode.json-simple" % "json-simple" % "1.1.1"
)
val mongoClient = "org.mongodb.scala" %% "mongo-scala-driver" % Dependencies.mongoDbVersion
val orientDbClient = "com.orientechnologies" % "orientdb-client" % Dependencies.orientDbVersion
val psqlDbClient = Seq(
  "org.tpolecat" %% "doobie-core" % Dependencies.doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % Dependencies.doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % Dependencies.doobieVersion,
  "org.flywaydb" % "flyway-core" % Dependencies.flywayVersion,
  "com.opentable.components" % "otj-pg-embedded" % "0.13.3" % "test"
)

val playJson = "com.typesafe.play" %% "play-json" % Dependencies.playJsonVersion
// for more than 22 parameter case classes
val playJsonExtensions = "ai.x" %% "play-json-extensions" % "0.42.0"
val playJsonJoda = "com.typesafe.play" %% "play-json-joda" % Dependencies.playJsonVersion
val playWsAhcStandalone = "com.typesafe.play" %% "play-ahc-ws-standalone" % Dependencies.playWsVersion
val playWsJsonStandalone = "com.typesafe.play" %% "play-ws-standalone-json" % Dependencies.playWsVersion

val apacheCommons = Seq(
  "commons-io" % "commons-io" % "2.8.0",
  "commons-codec" % "commons-codec" % "1.15"
)

val playTest = "org.scalatestplus" %% "mockito-3-2" % "3.1.2.0" % "test"
val playTestPlus = "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
val mockito = "org.mockito" % "mockito-core" % Dependencies.mockitoVersion % "test"
val scalaTest = "org.scalatest" %% "scalatest" % Dependencies.scalaTestVersion % "test"

def logging = Seq(
  "ch.qos.logback" % "logback-classic" % Dependencies.logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3",
  "org.codehaus.janino" % "janino" % "3.1.3", // conditional logback processing
  "com.papertrailapp" % "logback-syslog4j" % "1.0.0"
)
def elastic4s = Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-core" % Dependencies.elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % Dependencies.elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % Dependencies.elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Dependencies.elasticVersion % "test"
)
def storage = Seq(mongoClient, orientDbClient) ++ psqlDbClient ++ rethinkClient

def cats = Seq(
  "org.typelevel" %% "cats-core" % Dependencies.catsVersion,
  "org.typelevel" %% "mouse" % Dependencies.mouseVersion
)

def scalaz = Seq(
  "org.scalaz" %% "scalaz-core" % "7.3.2"
)

def zio = Seq(
  "dev.zio" %% "zio" % Dependencies.zioVersion,
  "dev.zio" %% "zio-logging" % Dependencies.zioLoggingVersion
)

def squants = Seq(
  "org.typelevel" %% "squants" % Dependencies.squantsVersion
)

def spark = Seq(
  "org.apache.spark" %% "spark-mllib" % Dependencies.sparkVersion exclude("com.google.inject", "guice")
)

def smile = Seq(
  "com.github.haifengl" % "smile-core" % "2.6.0"
)

lazy val runWebAppDist: ReleaseStep = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    extracted.runAggregated(com.typesafe.sbt.packager.Keys.dist in Global in webApp, st)
  }
)

lazy val runWebAppDockerPush: ReleaseStep = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    extracted.runAggregated(publish in Docker in webApp, st)
  }
)

lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  version := (version in ThisBuild).value,
  scalaVersion := Dependencies.projectScalaVersion,
  organization := "com.github.peregin",
  description := "The Cycling Platform",
  javacOptions ++= Seq("-source", "11", "-target", "11"),
  scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
  scalacOptions in Test ++= Seq("-Yrangepos"),
  resolvers in ThisBuild ++= Seq(
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
  ),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    runWebAppDist,
    runWebAppDockerPush, // will push automatically the image to the docker hub
    setNextVersion,
    commitNextVersion
    // pushChanges  // travis release script will push the changes
  )
)

lazy val dataProvider = (project in file("data-provider") withId "data-provider")
  .settings(
    buildSettings,
    name := "data-provider",
    libraryDependencies ++= Seq(
      playJson,
      playJsonExtensions,
      playJsonJoda,
      playWsAhcStandalone,
      scalaTest
    ) ++ logging
      ++ storage
      ++ apacheCommons
      ++ cats
      ++ zio
      ++ squants
  )

lazy val dataSearch = (project in file("data-search") withId "data-search")
  .settings(
    buildSettings,
    name := "data-search",
    libraryDependencies ++= elastic4s
  )
  .dependsOn(dataProvider % "test->test;compile->compile")

// module for various analytics supporting the generic stack
lazy val dataAnalytics = (project in file("data-analytics") withId "data-analytics")
  .settings(
    buildSettings,
    name := "data-analytics",
    libraryDependencies ++= smile ++ logging
  ).dependsOn(dataProvider % "compile->compile; test->test")

// module dedicated for analytics with Spark, with a special Scala version
lazy val dataAnalyticsSpark = (project in file("data-analytics-spark") withId "data-analytics-spark")
  .settings(
    buildSettings,
    name := "data-analytics-spark",
    scalaVersion := Dependencies.scala12Version, // spark is supported on 2.12 only
    libraryDependencies ++= spark ++ logging ++ Seq(playJsonJoda)
  ) //.dependsOn(dataProvider % "compile->compile; test->test") // data provider must be compiled on 2.12 as well

lazy val testService = (project in file("test/test-service-java") withId "test-service-java")
  .settings(
    buildSettings,
    name := "test-service",
    libraryDependencies ++= Seq(
      "com.twitter" %% "finatra-http" % Dependencies.finatraVersion,
      "com.chuusai" %% "shapeless" % Dependencies.shapelessVersion,
      "ch.qos.logback" % "logback-classic" % Dependencies.logbackVersion,
      "io.argonaut" %% "argonaut" % "6.3.3",
      "org.springframework.boot" % "spring-boot-starter-web" % Dependencies.springVersion,
      "javax.servlet" % "javax.servlet-api" % "4.0.1",
      scalaTest
    ) ++ cats,
    resolvers += "MavenRepository" at "https://mvnrepository.com/"
  )

lazy val webApp = (project in file("web-app") withId "web-app")
  .settings(
    buildSettings,
    name := "web-app",
    libraryDependencies ++= Seq(
      guice,
      ehcache,
      playWsJsonStandalone,
      "com.pauldijou" %% "jwt-play-json" % "5.0.0",
      "com.google.inject" % "guice" % "5.0.1", // for Java 11 support
      playTest,
      playTestPlus,
      mockito,
      scalaTest
    ),
    routesGenerator := InjectedRoutesGenerator,
    BuildInfoKeys.buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      BuildInfoKey.action("buildTime") {
        // is parsed and used in sitemap as well
        java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now())
      },
      "elasticVersion" -> Dependencies.elasticVersion,
      "playVersion" -> play.core.PlayVersion.current,
      "catsVersion" -> Dependencies.catsVersion,
      "gitHash" -> git.gitHeadCommit.value.getOrElse("n/a")
    ),
    buildInfoPackage := "velocorner.build",
    maintainer := "velocorner.com@gmail.com",
    packageName in Docker := "velocorner.com",
    dockerExposedPorts in Docker := Seq(9000),
    dockerBaseImage := "openjdk:11-jre-slim",
    dockerUsername := Some("peregin"),
    version in Docker := "latest",
    javaOptions in Universal ++= Seq("-Dplay.server.pidfile.path=/dev/null"),
    swaggerDomainNameSpaces := Seq("velocorner.api"),
    swaggerPrettyJson := true,
    swaggerV3 := true,
    // whenever we generate build information, run the formatter on the generated files
    Compile / buildInfo := Def.taskDyn {
      val files = (Compile / buildInfo).value
      Def.task {
        (Compile / scalafmtGenerated).value
        files
      }
    }.value
  )
  .enablePlugins(play.sbt.PlayScala, play.sbt.PlayAkkaHttp2Support, BuildInfoPlugin, com.iheart.sbtPlaySwagger.SwaggerPlugin, ScalafmtExtensionPlugin)
  .dependsOn(dataProvider % "compile->compile; test->test")

// top level aggregate
lazy val root = (project in file(".") withId "velocorner")
  .aggregate(testService, dataProvider, dataSearch, dataAnalytics, dataAnalyticsSpark, webApp)
  .settings(
    name := "velocorner",
    buildSettings,
    onLoadMessage := welcomeMessage.value
  )

def welcomeMessage = Def.setting {
  import scala.Console._
  def red(text: String): String = s"$RED$text$RESET"
  def item(text: String): String = s"$GREEN▶ $CYAN$text$RESET"

  s"""|${red("""                                         """)}
      |${red("""          _                              """)}
      |${red(""" __ _____| |___  __ ___ _ _ _ _  ___ _ _ """)}
      |${red(""" \ V / -_) / _ \/ _/ _ \ '_| ' \/ -_) '_|""")}
      |${red("""  \_/\___|_\___/\__\___/_| |_||_\___|_|  """)}
      |${red("""                                         """ + version.value)}
      |
      |Useful sbt tasks:
      |${item("\"project web-app\" run")} - run web application
      |${item("scalafmtGenerated")} - formats generated scala sources
      """.stripMargin
}
