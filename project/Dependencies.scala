object Dependencies {

  val projectScalaVersion = "2.13.10"

  val flywayVersion = "9.11.0"
  val catsVersion = "2.9.0"
  val catsEffectVersion = "3.4.4"
  val mouseVersion = "1.2.1"
  val fs2Version = "3.4.0"
  val zioVersion = "2.0.5"
  val http4s = "0.23.17"
  val finatraVersion = "21.2.0"
  val argonautVersion = "6.3.8"
  val playWsVersion = "2.1.10" // standalone version
  val playJsonVersion = "2.9.3"
  val shapelessVersion = "2.3.10"
  val logbackVersion = "1.4.5"
  val doobieVersion = "1.0.0-RC2"
  val orientDbVersion = "3.2.14"
  val mongoDbVersion = "4.8.1"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.5.2"
  val jwtVersion = "9.1.2"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.3.1"
  val circeVersion = "0.14.3"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.15.3"
  val scalaTestVersion = "3.2.15"
  val mockitoVersion = "4.11.0"
  val catsEffectTestVersion = "1.5.0"

  val springVersion = "3.0.1" // java world
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
