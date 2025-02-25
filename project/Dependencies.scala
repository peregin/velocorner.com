object Dependencies {

  val projectScalaVersion = "2.13.16"

  val flywayVersion = "11.3.3"
  val catsVersion = "2.13.0"
  val catsEffectVersion = "3.5.7"
  val mouseVersion = "1.3.2"
  val fs2Version = "3.11.0"
  val http4s = "0.23.30"
  val zioVersion = "2.1.15"
  val playWsVersion = "3.0.7" // standalone version
  val pekkoVersion = "1.1.3"
  val playJsonVersion = "3.0.4"
  val shapelessVersion = "2.3.12"
  val logbackVersion = "1.5.17" // updating will cause conflict
  val doobieVersion = "1.0.0-RC7"
  val orientDbVersion = "3.2.37"
  val mongoDbVersion = "5.3.1"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.16.0"
  val jwtVersion = "10.0.4"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.5.4"
  val circeVersion = "0.14.10"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.18.3"
  val scalaTestVersion = "3.2.19"
  val mockitoVersion = "5.15.2"
  val catsEffectTestVersion = "1.6.0"
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
