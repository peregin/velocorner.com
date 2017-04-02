import sbt._
import Keys._
import play.sbt._
import Play.autoImport._
import akka.util.Helpers.Requiring
import play.sbt.routes.RoutesCompiler.autoImport._


object dependencies {

  val sparkVersion = "2.1.0"
  val playAuthVersion = "0.14.2"
  val logbackVersion = "1.2.2"
  val elasticVersion = "5.2.11"
  val specsVersion = "3.7"
  val orientDbVersion = "2.2.17"
  val log4jVersion = "2.8.1"

  val couchbaseClient = "com.couchbase.client" % "couchbase-client" % "1.4.12"
  val rethinkClient = "com.rethinkdb" % "rethinkdb-driver" % "2.3.3"
  val mongoClient = "org.mongodb" %% "casbah" % "3.1.1"

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
  val couchbaseSpark = "com.couchbase.client" %% "spark-connector" % "2.1.0"

  val ficus = "net.ceedubs" %% "ficus" % "1.1.2"

  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
  val scalaSpec = "org.specs2" %% "specs2" % specsVersion % "test"

  def logging = Seq(logback, slf4s,
    "org.slf4j" % "slf4j-simple" % "1.7.21", // needed because of the ES
    "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
    "org.apache.logging.log4j" % "log4j-to-slf4j" % log4jVersion,
    "org.apache.logging.log4j" % "jcl-over-slf4j" % log4jVersion
  )
  def spark = Seq(sparkCore, sparkStreaming, sparkSQL, sparkMlLib, couchbaseSpark)
  def auth = Seq(playAuth, playAuthSocial)
  def elastic4s = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-core" % elasticVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-streams" % elasticVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-json4s" % elasticVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elasticVersion
  )
  def orientDb = Seq(
    "com.orientechnologies" % "orientdb-core" % orientDbVersion,
    "com.orientechnologies" % "orientdb-client" % orientDbVersion,
    "com.orientechnologies" % "orientdb-server" % orientDbVersion
  )
  def storage = Seq(couchbaseClient, rethinkClient, mongoClient) ++ orientDb
}

object sbuild extends Build {

  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    organization := "com.github.peregin",
    description := "The Cycling Platform",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions := Seq("-target:jvm-1.7", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Amazon Repository" at "http://dynamodb-local.s3-website-us-west-2.amazonaws.com/release"
    ),
    dependencyOverrides ++= Set(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5" // because of spark and couchbase connector
    ),
    dependencyOverrides += "org.apache.logging.log4j" % "log4j" % "2.6.2", // because of ES 5
    dependencyOverrides += "io.netty" % "netty-codec-http" % "4.0.41.Final", // because of ES 5
    dependencyOverrides += "io.netty" % "netty-handler" % "4.0.41.Final",
    dependencyOverrides += "io.netty" % "netty-codec" % "4.0.41.Final",
    dependencyOverrides += "io.netty" % "netty-transport" % "4.0.41.Final",
    dependencyOverrides += "io.netty" % "netty-buffer" % "4.0.41.Final",
    dependencyOverrides += "io.netty" % "netty-common" % "4.0.41.Final",
    dependencyOverrides += "io.netty" % "netty-transport-native-epoll" % "4.0.41.Final"
  )

  lazy val dataStorage = Project(
    id = "data-storage",
    base = file("data-storage"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        dependencies.playJson, dependencies.playWs, dependencies.ficus, dependencies.scalaSpec
      ) ++ dependencies.logging
        ++ dependencies.storage
        ++ dependencies.elastic4s
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
      libraryDependencies ++= dependencies.auth ++ Seq(dependencies.playCache),
      routesGenerator := StaticRoutesGenerator
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
