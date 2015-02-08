import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._


object dependencies {


  val couchbaseClient = "com.couchbase.client" % "couchbase-client" % "1.4.7"

  val json = "com.typesafe.play" %% "play-json" % play.core.PlayVersion.current

  val SparkVersion = "1.2.0"
  val sparkCore = "org.apache.spark" %% "spark-core" % SparkVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % SparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % SparkVersion

  val scalaTest = "org.scalatest" %% "scalatest" % "2.1.4" % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"


  def spark = Seq(sparkCore, sparkStreaming, sparkSQL, scalaTest, scalaCheck)
}

object build extends Build {


  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.10.4",
    organization := "com.github.peregin",
    description := "The Cycling Platform",
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8")
  )

  lazy val dataStorage = Project(
    id = "data-storage",
    base = file("data-storage"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        dependencies.couchbaseClient, dependencies.json
      )
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
