import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._


object dependencies {


  val couchbaseClient = "com.couchbase.client" % "couchbase-client" % "1.4.7"

  val json = "com.typesafe.play" %% "play-json" % play.core.PlayVersion.current

  val SparkVersion = "1.2.1"
  val sparkCore = "org.apache.spark" %% "spark-core" % SparkVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % SparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % SparkVersion

  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
  val scalaSpec = "org.specs2" %% "specs2" % "2.4.2" % "test"


  def spark = Seq(sparkCore, sparkStreaming, sparkSQL)
}

object build extends Build {


  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.5",
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
        dependencies.couchbaseClient, dependencies.json, dependencies.scalaSpec
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
