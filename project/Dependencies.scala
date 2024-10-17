object Dependencies {

  val projectScalaVersion = "2.13.15"

  val flywayVersion = "10.20.0"
  val catsVersion = "2.12.0"
  val catsEffectVersion = "3.5.4"
  val mouseVersion = "1.3.2"
  val fs2Version = "3.11.0"
  val http4s = "0.23.28"
  val zioVersion = "2.1.11"
  val playWsVersion = "3.0.5" // standalone version
  val pekkoVersion = "1.1.2"
  val playJsonVersion = "3.0.4"
  val shapelessVersion = "2.3.12"
  val logbackVersion = "1.5.11" // updating will cause conflict
  val doobieVersion = "1.0.0-RC6"
  val orientDbVersion = "3.2.33"
  val mongoDbVersion = "5.2.0"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.15.2"
  val jwtVersion = "10.0.1"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.5.3"
  val circeVersion = "0.14.10"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.18.1"
  val scalaTestVersion = "3.2.19"
  val mockitoVersion = "5.14.2"
  val catsEffectTestVersion = "1.5.0"
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
