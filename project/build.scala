import sbt._
import Keys._
import play.sbt.routes.RoutesCompiler.autoImport._
import sbtbuildinfo.{BuildInfoKeys, _}
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import play.sbt.PlayImport._


object dependencies {

  val sparkVersion = "2.2.1"
  val logbackVersion = "1.2.3"
  val elasticVersion = "6.1.0"
  val specsVersion = "3.7"
  val orientDbVersion = "2.2.31"
  val log4jVersion = "2.10.0"
  val slf4sVersion = "1.7.25"
  val playWsVersion = "1.1.3" // standalone version

  val couchbaseClient = "com.couchbase.client" % "couchbase-client" % "1.4.13"
  val rethinkClient = "com.rethinkdb" % "rethinkdb-driver" % "2.3.3"
  val mongoClient = "org.mongodb" %% "casbah" % "3.1.1"

  def orientDb = Seq(
    "com.orientechnologies" % "orientdb-core" % orientDbVersion,
    "com.orientechnologies" % "orientdb-client" % orientDbVersion,
    "com.orientechnologies" % "orientdb-server" % orientDbVersion
  )

  val playJson = "com.typesafe.play" %% "play-json" % play.core.PlayVersion.current
  val playJsonJoda = "com.typesafe.play" %% "play-json-joda" % play.core.PlayVersion.current
  val playWsJsonStandalone = "com.typesafe.play" %% "play-ws-standalone-json" % playWsVersion
  val playWsAhcStandalone = "com.typesafe.play" %% "play-ahc-ws-standalone" % playWsVersion
  val playTest = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test"

  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val slf4s = "org.slf4s" %% "slf4s-api" % slf4sVersion

  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % sparkVersion
  val sparkMlLib = "org.apache.spark" %% "spark-mllib" % sparkVersion

  val ficus = "net.ceedubs" %% "ficus" % "1.1.2"

  val rx = "io.reactivex" %% "rxscala" % "0.26.5"

  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
  val scalaSpec = "org.specs2" %% "specs2" % specsVersion % "test"
  val apacheCommons = "commons-io" % "commons-io" % "2.6" % "test"

  def logging = Seq(logback, slf4s,
    "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
    "org.apache.logging.log4j" % "log4j-to-slf4j" % log4jVersion
  )
  def spark = Seq(sparkCore, sparkStreaming, sparkSQL, sparkMlLib)
  def elastic4s = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-tcp" % elasticVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-http" % elasticVersion
  )
  def storage = Seq(couchbaseClient, rethinkClient, mongoClient) ++ orientDb
}

object sbuild extends Build {

  lazy val runDist : ReleaseStep = ReleaseStep(
    action = { st: State =>
      val extracted = Project.extract(st)
      extracted.runAggregated(com.typesafe.sbt.packager.Keys.dist in Global in webApp, st)
    }
  )

  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    version <<= version in ThisBuild,
    scalaVersion := "2.11.12",
    organization := "com.github.peregin",
    description := "The Cycling Platform",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions := Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Amazon Repository" at "http://dynamodb-local.s3-website-us-west-2.amazonaws.com/release"
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
    ),
    dependencyOverrides ++= Set(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5" // because of spark
    ),
    dependencyOverrides += "org.apache.logging.log4j" % "log4j" % "2.6.2", // because of ES 5
    dependencyOverrides += "com.google.guava" % "guava" % "16.0" // because of Hadoop MR Client
  )

  lazy val dataProvider = Project(
    id = "data-provider",
    base = file("data-provider"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        dependencies.playJson, dependencies.playJsonJoda, dependencies.playWsAhcStandalone,
        dependencies.ficus, dependencies.rx,
        dependencies.scalaSpec, dependencies.apacheCommons
      ) ++ dependencies.logging
        ++ dependencies.storage
    )
  )

  lazy val dataCruncher = Project(
    id = "data-cruncher",
    base = file("data-cruncher"),
    dependencies = Seq(dataProvider % "test->test;compile->compile"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.spark
    )
  )

  lazy val dataSearch = Project(
    id = "data-search",
    base = file("data-search"),
    dependencies = Seq(dataProvider % "test->test;compile->compile"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.elastic4s
    )
  )

  lazy val webApp = Project(
    id = "web-app",
    base = file("web-app"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(guice, ehcache, dependencies.playWsJsonStandalone, dependencies.playTest),
      routesGenerator := InjectedRoutesGenerator,
      BuildInfoKeys.buildInfoKeys := Seq[BuildInfoKey](
        name, version, scalaVersion, sbtVersion,
        BuildInfoKey.action("buildTime") {
          java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now())
        }
      ),
      maintainer := "velocorner.com@gmail.com",
      packageName in Docker := "velocorner.com",
      dockerExposedPorts in Docker := Seq(9000),
      dockerBaseImage in Docker := "java:8"
    ),
    dependencies = Seq(dataProvider)
  ).enablePlugins(play.sbt.PlayScala, BuildInfoPlugin)


  // top level aggregate
  lazy val root = Project(
    id = "velocorner",
    base = file("."),
    settings = buildSettings,
    aggregate = Seq(dataProvider, dataCruncher, dataSearch, webApp)
  )
}
