import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._


object dependencies {

  val sparkVersion = "1.6.1"
  val playAuthVersion = "0.14.2"
  val logbackVersion = "1.1.3"
  val elasticVersion = "2.3.0"

  val couchbaseClient = "com.couchbase.client" % "couchbase-client" % "1.4.12"
  val rethinkClient = "com.rethinkdb" % "rethinkdb-driver" % "2.3.3"

  val playJson = "com.typesafe.play" %% "play-json" % play.core.PlayVersion.current
  val playWs = "com.typesafe.play" %% "play-ws" % play.core.PlayVersion.current
  val playCache = "com.typesafe.play" %% "play-cache" % play.core.PlayVersion.current
  val playAuth = "jp.t2v" %% "play2-auth" % playAuthVersion
  val playAuthSocial = "jp.t2v" %% "play2-auth-social" % playAuthVersion

  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val slf4s = "org.slf4s" %% "slf4s-api" % "1.7.13"

  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % sparkVersion
  val sparkMlLib = "org.apache.spark" %% "spark-mllib" % sparkVersion
  val couchbaseSpark = "com.couchbase.client" %% "spark-connector" % "1.2.0"

  val ficus = "net.ceedubs" %% "ficus" % "1.1.2"

  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
  val scalaSpec = "org.specs2" %% "specs2" % "3.7" % "test"

  def logging = Seq(logback, slf4s)
  def spark = Seq(sparkCore, sparkStreaming, sparkSQL, sparkMlLib, couchbaseSpark)
  def auth = Seq(playAuth, playAuthSocial)
  def elastic4s = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-core" % elasticVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-streams" % elasticVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-json4s" % elasticVersion
  )
}

object build extends Build {


  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    organization := "com.github.peregin",
    description := "The Cycling Platform",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions := Seq("-target:jvm-1.7", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    dependencyOverrides ++= Set(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4" // because of spark and couchbase connector
    )
  )

  lazy val dataStorage = Project(
    id = "data-storage",
    base = file("data-storage"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        dependencies.couchbaseClient, dependencies.rethinkClient,
        dependencies.playJson, dependencies.playWs, dependencies.ficus,
        dependencies.scalaSpec
      ) ++ dependencies.logging ++ dependencies.elastic4s
    )
  )

  lazy val dataCruncher = Project(
    id = "data-cruncher",
    base = file("data-cruncher"),
    dependencies = Seq(dataStorage % "test->test;compile->compile"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.spark

    )
  )

  lazy val webApp = Project(
    id = "web-app",
    base = file("web-app"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.auth ++ Seq(dependencies.playCache)
    ),
    dependencies = Seq(dataStorage)
  ).enablePlugins(play.sbt.PlayScala)


  // top level aggregate
  lazy val root = Project(
    id = "velocorner",
    base = file("."),
    settings = buildSettings,
    aggregate = Seq(dataStorage, dataCruncher, webApp)
  )
}
