object Dependencies {

  val projectScalaVersion = "2.13.12"

  val flywayVersion = "9.22.0"
  val catsVersion = "2.10.0"
  val catsEffectVersion = "3.5.1"
  val mouseVersion = "1.2.1"
  val fs2Version = "3.9.1"
  val http4s = "0.23.23"
  val zioVersion = "2.0.16"
  val finatraVersion = "21.2.0" // in the test service
  val argonautVersion = "6.3.8" // in the test service
  val playWsVersion = "2.1.11" // standalone version
  val sangriaVersion = "4.0.1"
  val playJsonVersion = "2.9.4"
  val shapelessVersion = "2.3.10"
  val logbackVersion = "1.4.8" // updating will cause conflict
  val doobieVersion = "1.0.0-RC4"
  val orientDbVersion = "3.2.22"
  val mongoDbVersion = "4.10.2"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.9.2"
  val jwtVersion = "9.4.4"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.5.0"
  val circeVersion = "0.14.6"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.16.1"
  val scalaTestVersion = "3.2.17"
  val mockitoVersion = "5.5.0"
  val catsEffectTestVersion = "1.5.0"

  val springVersion = "3.1.3" // java world
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
