object Dependencies {

  val projectScalaVersion = "2.13.18"

  val flywayVersion = "12.1.0"
  val catsVersion = "2.13.0"
  val catsEffectVersion = "3.7.0"
  val mouseVersion = "1.4.0"
  val http4s = "0.23.33"
  val zioVersion = "2.1.24"
  val playWsVersion = "3.0.10" // standalone version
  val pekkoVersion = "1.4.0"
  val playJsonVersion = "3.0.6"
  val shapelessVersion = "2.3.12"
  val logbackVersion = "1.5.32"
  val doobieVersion = "1.0.0-RC12"
  val orientDbVersion = "3.2.50"
  val mongoDbVersion = "5.6.4"
  val rethinkDbVersion = "2.4.4"
  val jwtVersion = "11.0.3"
  val squantsVersion = "1.8.3"
  val sparkVersion = "4.1.1"
  val circeVersion = "0.14.15"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.22.1"
  val scalaTestVersion = "3.2.19"
  val mockitoVersion = "5.22.0"
  val catsEffectTestVersion = "1.7.0"
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
