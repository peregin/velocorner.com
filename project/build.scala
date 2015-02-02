import sbt._
import Keys._


object dependency {
  object Version {
    val Spark        = "1.2.0"
    val ScalaTest    = "2.1.4"
    val ScalaCheck   = "1.11.3"
  }

  val sparkCore      = "org.apache.spark"  %% "spark-core"      % Version.Spark
  val sparkStreaming = "org.apache.spark"  %% "spark-streaming" % Version.Spark
  val sparkSQL       = "org.apache.spark"  %% "spark-sql"       % Version.Spark

  val scalaTest      = "org.scalatest"     %% "scalatest"       % Version.ScalaTest  % "test"
  val scalaCheck     = "org.scalacheck"    %% "scalacheck"      % Version.ScalaCheck % "test"
}

object dependencies {
  import dependency._

  def spark = Seq(sparkCore, sparkStreaming, sparkSQL,
    scalaTest, scalaCheck)
}

object build extends Build {


  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    name          := "velocorner",
    version       := "1.0.0-SNAPSHOT",
    scalaVersion  := "2.10.4",
    organization  := "com.github.peregin",
    description   := "The Cycling Platform",
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8")
  )

  lazy val common = Project(
    id = "common",
    base = file("common"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.spark
    )
  )
}
