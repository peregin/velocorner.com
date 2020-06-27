import play.sbt.routes.RoutesCompiler.autoImport._
import sbtbuildinfo.BuildInfoKeys
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import play.sbt.PlayImport._

val projectScalaVersion = "2.13.3"

val catsVersion = "2.1.1"
val zioVersion = "1.0.0-RC21-1"
val logbackVersion = "1.2.3"
val doobieVersion = "0.9.0"
val orientDbVersion = "3.1.0"
val mongoDbVersion = "4.0.4"
val rethinkDbVersion = "2.4.0"
val flywayVersion = "6.5.0"
val elasticVersion = "7.8.0"
val finatraVersion = "20.6.0"
val playWsVersion = "2.1.2" // standalone version
val playJsonVersion = "2.9.0"
val specsVersion = "4.10.0"
val mockitoVersion = "3.3.3"

val rethinkClient = "com.rethinkdb" % "rethinkdb-driver" % rethinkDbVersion
val mongoClient = "org.mongodb.scala" %% "mongo-scala-driver" % mongoDbVersion
val orientDbClient = "com.orientechnologies" % "orientdb-client" % orientDbVersion
val psqlDbClient = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.flywaydb" % "flyway-core" % flywayVersion,
  "com.opentable.components" % "otj-pg-embedded" % "0.13.3" % "test"
)

val playJson = "com.typesafe.play" %% "play-json" % playJsonVersion
// for more than 22 parameter case classes
val playJsonExtensions = "ai.x" %% "play-json-extensions" % "0.40.2"
val playJsonJoda = "com.typesafe.play" %% "play-json-joda" % playJsonVersion
val playWsAhcStandalone = "com.typesafe.play" %% "play-ahc-ws-standalone" % playWsVersion
val playWsJsonStandalone = "com.typesafe.play" %% "play-ws-standalone-json" % playWsVersion

val apacheCommons = Seq(
  "commons-io" % "commons-io" % "2.7",
  "commons-codec" % "commons-codec" % "1.14"
)

val playTest = "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % "test"
val scalaSpec = "org.specs2" %% "specs2-core" % specsVersion % "test"
val scalaSpecJunit = "org.specs2" %% "specs2-junit" % specsVersion % "test"
val mockito = "org.mockito" % "mockito-core" % mockitoVersion % "test"

def logging = Seq(
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.codehaus.janino" % "janino" % "3.1.2", // conditional logback processing
  "com.papertrailapp" % "logback-syslog4j" % "1.0.0"
)
def elastic4s = Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-core" % elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elasticVersion % "test"
)
def storage = Seq(rethinkClient, mongoClient, orientDbClient) ++ psqlDbClient

def cats = Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "mouse" % "0.25"
)

def zio = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-logging" % "0.3.2"
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
  scalaVersion := projectScalaVersion,
  organization := "com.github.peregin",
  description := "The Cycling Platform",
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions := Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
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
      playJson, playJsonExtensions, playJsonJoda, playWsAhcStandalone,
      scalaSpec, scalaSpecJunit
    ) ++ logging
      ++ storage
      ++ apacheCommons
      ++ cats
      ++ zio
  )

lazy val dataSearch = (project in file("data-search") withId "data-search")
  .settings(
    buildSettings,
    name := "data-search",
    libraryDependencies ++= elastic4s
  )
  .dependsOn(dataProvider % "test->test;compile->compile")

lazy val webApp = (project in file("web-app") withId "web-app")
  .settings(
    buildSettings,
    name := "web-app",
    libraryDependencies ++= Seq(
      guice, ehcache,
      playWsJsonStandalone,
      playTest, mockito, scalaSpec
    ),
    routesGenerator := InjectedRoutesGenerator,
    BuildInfoKeys.buildInfoKeys := Seq[BuildInfoKey](
      name, version, scalaVersion, sbtVersion,
      BuildInfoKey.action("buildTime") {
        java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now())
      },
      "elasticVersion" -> elasticVersion,
      "playVersion" -> play.core.PlayVersion.current,
      "catsVersion" -> catsVersion,
      "gitHash" -> git.gitHeadCommit.value.getOrElse("n/a")
    ),
    buildInfoPackage := "velocorner.build",
    maintainer := "velocorner.com@gmail.com",
    packageName in Docker := "velocorner.com",
    dockerExposedPorts in Docker := Seq(9000),
    dockerBaseImage in Docker := "openjdk:8-jre-alpine",
    dockerUsername := Some("peregin"),
    version in Docker := "latest",
    javaOptions in Universal ++= Seq("-Dplay.server.pidfile.path=/dev/null"),
    swaggerDomainNameSpaces := Seq("velocorner.api"),
    swaggerPrettyJson := true,
    swaggerV3 := true
  )
  .enablePlugins(play.sbt.PlayScala, BuildInfoPlugin, com.iheart.sbtPlaySwagger.SwaggerPlugin)
  .dependsOn(dataProvider % "compile->compile; test->test")

lazy val gatewayService = (project in file("gateway-service") withId "gateway-service")
  .settings(
    buildSettings,
    name := "gateway-service",
    scalaVersion := "2.12.11", // because finagle is not fully supported in 2.13
    libraryDependencies += "com.twitter" %% "finatra-http" % finatraVersion,
    resolvers += "MavenRepository" at "https://mvnrepository.com/"
  )

// top level aggregate
lazy val root = (project in file(".") withId "velocorner")
  .aggregate(gatewayService, dataProvider, dataSearch, webApp)
  .settings(
    name := "velocorner",
    buildSettings
  )

