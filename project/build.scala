import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._


object dependencies {

  val couchbaseClient = "com.couchbase.client" % "couchbase-client" % "1.4.10"

  val playJson = "com.typesafe.play" %% "play-json" % play.core.PlayVersion.current
  val playWs = "com.typesafe.play" %% "play-ws" % play.core.PlayVersion.current

  val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
  val slf4s = "org.slf4s" %% "slf4s-api" % "1.7.12"

  val sparkVersion = "1.5.1"
  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % sparkVersion

  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
  val scalaSpec = "org.specs2" %% "specs2" % "2.4.2" % "test"


  def logging = Seq(logback, slf4s)
  def spark = Seq(sparkCore, sparkStreaming, sparkSQL)
}

object build extends Build {


  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    organization := "com.github.peregin",
    description := "The Cycling Platform",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions := Seq("-target:jvm-1.7", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  lazy val dataStorage = Project(
    id = "data-storage",
    base = file("data-storage"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        dependencies.couchbaseClient, dependencies.playJson, dependencies.playWs, dependencies.scalaSpec
      ) ++ dependencies.logging
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
    settings = buildSettings,
    dependencies = Seq(dataStorage)
  ).enablePlugins(play.PlayScala)


  // top level aggregate
  lazy val root = Project(
    id = "velocorner",
    base = file("."),
    settings = buildSettings,
    aggregate = Seq(dataStorage, dataCruncher, webApp)
  )
}
