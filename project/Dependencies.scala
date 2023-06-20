object Dependencies {

  val projectScalaVersion = "2.13.11"

  val flywayVersion = "9.19.4"
  val catsVersion = "2.9.0"
  val catsEffectVersion = "3.5.0"
  val mouseVersion = "1.2.1"
  val fs2Version = "3.7.0"
  val http4s = "0.23.21"
  val zioVersion = "2.0.15"
  val finatraVersion = "21.2.0" // in the test service
  val argonautVersion = "6.3.8" // in the test service
  val playWsVersion = "2.1.10" // standalone version
  val playJsonVersion = "2.9.4"
  val shapelessVersion = "2.3.10"
  val logbackVersion = "1.4.8"
  val doobieVersion = "1.0.0-RC4"
  val orientDbVersion = "3.2.19"
  val mongoDbVersion = "4.9.1"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.7.1"
  val jwtVersion = "9.3.0"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.4.0"
  val circeVersion = "0.14.5"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.16.1"
  val scalaTestVersion = "3.2.16"
  val mockitoVersion = "5.4.0"
  val catsEffectTestVersion = "1.5.0"

  val springVersion = "3.1.0" // java world
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
