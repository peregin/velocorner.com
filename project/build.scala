import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._


object dependencies {

  val SparkVersion = "1.2.0"

  val couchbaseClient = "com.couchbase.client" % "couchbase-client" % "1.4.7"

  val sparkCore = "org.apache.spark" %% "spark-core" % SparkVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % SparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % SparkVersion

  val scalaTest = "org.scalatest" %% "scalatest" % "2.1.4" % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"


  def spark = Seq(sparkCore, sparkStreaming, sparkSQL, scalaTest, scalaCheck)
}

object build extends Build {


  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    name := "velocorner",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.10.4",
    organization := "com.github.peregin",
    description := "The Cycling Platform",
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8")
  )

  lazy val dataCruncher = Project(
    id = "data-cruncher",
    base = file("data-cruncher"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.spark ++ Seq(dependencies.couchbaseClient)
    )
  )

  lazy val webApp = Project(
    id = "web-app",
    base = file("web-app")
  ).enablePlugins(play.PlayScala)
    .dependsOn(dataCruncher)
    .settings(
      libraryDependencies ++= Seq()
    )

  // top level aggregate
  lazy val root = Project(
    id = "velocorner",
    base = file("."),
    settings = buildSettings,
    aggregate = Seq(dataCruncher, webApp)
  )
}
