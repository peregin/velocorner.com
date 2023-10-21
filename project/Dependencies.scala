object Dependencies {

  val projectScalaVersion = "2.13.12"

  val flywayVersion = "9.22.3"
  val catsVersion = "2.10.0"
  val catsEffectVersion = "3.5.2"
  val mouseVersion = "1.2.1"
  val fs2Version = "3.9.2"
  val http4s = "0.23.23"
  val zioVersion = "2.0.18"
  val finatraVersion = "21.2.0" // in the test service
  val argonautVersion = "6.3.9" // in the test service
  val playWsVersion = "2.2.3" // standalone version
  val sangriaVersion = "4.0.2"
  val playJsonVersion = "2.10.1"
  val shapelessVersion = "2.3.10"
  val logbackVersion = "1.4.8" // updating will cause conflict
  val doobieVersion = "1.0.0-RC4"
  val orientDbVersion = "3.2.23"
  val mongoDbVersion = "4.11.0"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.9.4"
  val jwtVersion = "9.4.4"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.5.0"
  val circeVersion = "0.14.6"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.16.2"
  val scalaTestVersion = "3.2.17"
  val mockitoVersion = "5.6.0"
  val catsEffectTestVersion = "1.5.0"

  val springVersion = "3.1.4" // java world
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
