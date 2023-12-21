object Dependencies {

  val projectScalaVersion = "2.13.12"

  val flywayVersion = "10.4.0"
  val catsVersion = "2.10.0"
  val catsEffectVersion = "3.5.2"
  val mouseVersion = "1.2.2"
  val fs2Version = "3.9.3"
  val http4s = "0.23.24"
  val zioVersion = "2.0.20"
  val finatraVersion = "21.2.0" // in the test service
  val argonautVersion = "6.3.9" // in the test service
  val playWsVersion = "3.0.1" // standalone version
  val sangriaVersion = "4.0.2"
  val playJsonVersion = "3.0.1"
  val shapelessVersion = "2.3.10"
  val logbackVersion = "1.4.14" // updating will cause conflict
  val doobieVersion = "1.0.0-RC5"
  val orientDbVersion = "3.2.25"
  val mongoDbVersion = "4.11.1"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.11.5"
  val jwtVersion = "9.4.5"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.5.0"
  val circeVersion = "0.14.6"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.17.1"
  val scalaTestVersion = "3.2.17"
  val mockitoVersion = "5.8.0"
  val catsEffectTestVersion = "1.5.0"

  val springVersion = "3.2.1" // java world
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
