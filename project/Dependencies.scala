object Dependencies {

  val projectScalaVersion = "2.13.16"

  val flywayVersion = "11.10.3"
  val catsVersion = "2.13.0"
  val catsEffectVersion = "3.6.2"
  val mouseVersion = "1.3.2"
  val fs2Version = "3.12.0"
  val http4s = "0.23.30"
  val zioVersion = "2.1.19"
  val playWsVersion = "3.0.7" // standalone version
  val pekkoVersion = "1.1.4"
  val playJsonVersion = "3.0.5"
  val shapelessVersion = "2.3.12"
  val logbackVersion = "1.5.18" // updating will cause conflict
  val doobieVersion = "1.0.0-RC10"
  val orientDbVersion = "3.2.39"
  val mongoDbVersion = "5.5.0"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.18.2"
  val jwtVersion = "11.0.0"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.5.6"
  val circeVersion = "0.14.14"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.21.1"
  val scalaTestVersion = "3.2.19"
  val mockitoVersion = "5.18.0"
  val catsEffectTestVersion = "1.6.0"
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
