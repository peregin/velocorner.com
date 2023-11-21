object Dependencies {

  val projectScalaVersion = "2.13.12"

  val flywayVersion = "9.22.3"
  val catsVersion = "2.10.0"
  val catsEffectVersion = "3.5.2"
  val mouseVersion = "1.2.2"
  val fs2Version = "3.9.3"
  val http4s = "0.23.24"
  val zioVersion = "2.0.19"
  val finatraVersion = "21.2.0" // in the test service
  val argonautVersion = "6.3.9" // in the test service
  val playWsVersion = "3.0.0" // standalone version
  val sangriaVersion = "4.0.2"
  val playJsonVersion = "3.0.1"
  val shapelessVersion = "2.3.10"
  val logbackVersion = "1.4.11" // updating will cause conflict
  val doobieVersion = "1.0.0-RC5"
  val orientDbVersion = "3.2.24"
  val mongoDbVersion = "4.11.1"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.11.1"
  val jwtVersion = "9.4.4"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.5.0"
  val circeVersion = "0.14.6"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.16.2"
  val scalaTestVersion = "3.2.17"
  val mockitoVersion = "5.7.0"
  val catsEffectTestVersion = "1.5.0"

  val springVersion = "3.1.5" // java world
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
