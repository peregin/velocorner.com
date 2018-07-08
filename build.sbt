import play.sbt.routes.RoutesCompiler.autoImport._
import sbtbuildinfo.BuildInfoKeys
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import play.sbt.PlayImport._


val logbackVersion = "1.2.3"
val elasticVersion = "6.2.9"
val orientDbVersion = "3.0.3"
val log4jVersion = "2.11.0"
val slf4sVersion = "1.7.25"
val playWsVersion = "1.1.9" // standalone version
val playJsonVersion = "2.6.9"
val specsVersion = "4.3.1"
val mockitoVersion = "2.19.0"

val couchbaseClient = "com.couchbase.client" % "couchbase-client" % "1.4.13"
val rethinkClient = "com.rethinkdb" % "rethinkdb-driver" % "2.3.3"
val mongoClient = "org.mongodb" %% "casbah" % "3.1.1"

def orientDb = Seq(
  "com.orientechnologies" % "orientdb-core" % orientDbVersion,
  "com.orientechnologies" % "orientdb-client" % orientDbVersion,
  "com.orientechnologies" % "orientdb-server" % orientDbVersion
)

val playJson = "com.typesafe.play" %% "play-json" % playJsonVersion
val playJsonJoda = "com.typesafe.play" %% "play-json-joda" % playJsonVersion
val playWsJsonStandalone = "com.typesafe.play" %% "play-ws-standalone-json" % playWsVersion
val playWsAhcStandalone = "com.typesafe.play" %% "play-ahc-ws-standalone" % playWsVersion
val playTest = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test"
val playSwagger = "io.swagger" %% "swagger-play2" % "1.6.0"

val rx = "io.reactivex" %% "rxscala" % "0.26.5"

val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
val scalaSpec = "org.specs2" %% "specs2-core" % specsVersion % "test"
val apacheCommons = "commons-io" % "commons-io" % "2.6"
val mockito = "org.mockito" % "mockito-core" % mockitoVersion % "test"

def logging = Seq(
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.slf4s" %% "slf4s-api" % slf4sVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion
)
def elastic4s = Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-core" % elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elasticVersion % "test"
)
def storage = Seq(couchbaseClient, rethinkClient, mongoClient) ++ orientDb


lazy val runDist: ReleaseStep = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    extracted.runAggregated(com.typesafe.sbt.packager.Keys.dist in Global in webApp, st)
  }
)

lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  version := (version in ThisBuild).value,
  scalaVersion := "2.12.6",
  organization := "com.github.peregin",
  description := "The Cycling Platform",
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions := Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
  scalacOptions in Test ++= Seq("-Yrangepos"),
  resolvers ++= Seq(
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  ),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    runDist,
    setNextVersion,
    commitNextVersion
  )
)

lazy val dataProvider = (project in file("data-provider") withId("data-provider"))
  .settings(
    buildSettings,
    name := "data-provider",
    libraryDependencies ++= Seq(
      playJson, playJsonJoda, playWsAhcStandalone,
      rx, apacheCommons,
      scalaSpec
    ) ++ logging
      ++ storage
  )

lazy val dataSearch = (project in file("data-search") withId("data-search"))
  .settings(
    buildSettings,
    name := "data-search",
    libraryDependencies ++= elastic4s
  )
  .dependsOn(dataProvider % "test->test;compile->compile")

lazy val webApp = (project in file("web-app") withId("web-app"))
  .settings(
    buildSettings,
    name := "web-app",
    libraryDependencies ++= Seq(guice, ehcache, playWsJsonStandalone, playTest, mockito, playSwagger),
    routesGenerator := InjectedRoutesGenerator,
    BuildInfoKeys.buildInfoKeys := Seq[BuildInfoKey](
      name, version, scalaVersion, sbtVersion,
      BuildInfoKey.action("buildTime") {
        java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now())
      },
      "elasticVersion" -> elasticVersion,
      "playVersion" -> play.core.PlayVersion.current
    ),
    maintainer := "velocorner.com@gmail.com",
    packageName in Docker := "velocorner.com",
    dockerExposedPorts in Docker := Seq(9000),
    dockerBaseImage in Docker := "java:8"
  )
  .enablePlugins(play.sbt.PlayScala, BuildInfoPlugin)
  .dependsOn(dataProvider)


// top level aggregate
lazy val root = (project in file(".") withId("velocorner"))
  .aggregate(dataProvider, dataSearch, webApp)
  .settings(
    name := "velocorner",
    buildSettings
  )

