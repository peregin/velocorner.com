object Dependencies {

  val projectScalaVersion = "2.13.10"

  val flywayVersion = "9.17.0"
  val catsVersion = "2.9.0"
  val catsEffectVersion = "3.4.10"
  val mouseVersion = "1.2.1"
  val fs2Version = "3.6.1"
  val http4s = "0.23.18"
  val zioVersion = "2.0.13"
  val finatraVersion = "21.2.0" // in the test service
  val argonautVersion = "6.3.8" // in the test service
  val playWsVersion = "2.1.10" // standalone version
  val playJsonVersion = "2.9.4"
  val shapelessVersion = "2.3.10"
  val logbackVersion = "1.4.7"
  val doobieVersion = "1.0.0-RC2"
  val orientDbVersion = "3.2.18"
  val mongoDbVersion = "4.9.1"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.7.0"
  val jwtVersion = "9.2.0"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.4.0"
  val circeVersion = "0.14.5"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.16.1"
  val scalaTestVersion = "3.2.15"
  val mockitoVersion = "5.3.1"
  val catsEffectTestVersion = "1.5.0"

  val springVersion = "3.0.6" // java world
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
