object Dependencies {

  val projectScalaVersion = "2.13.17"

  val flywayVersion = "11.14.0"
  val catsVersion = "2.13.0"
  val catsEffectVersion = "3.6.3"
  val mouseVersion = "1.3.2"
  val fs2Version = "3.12.2"
  val http4s = "0.23.32"
  val zioVersion = "2.1.21"
  val playWsVersion = "3.0.8" // standalone version
  val pekkoVersion = "1.2.1"
  val playJsonVersion = "3.0.6"
  val shapelessVersion = "2.3.12"
  val logbackVersion = "1.5.19" // updating will cause conflict
  val doobieVersion = "1.0.0-RC10"
  val orientDbVersion = "3.2.44"
  val mongoDbVersion = "5.6.1"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.19.0"
  val jwtVersion = "11.0.3"
  val squantsVersion = "1.8.3"
  val sparkVersion = "4.0.1"
  val circeVersion = "0.14.15"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.21.2"
  val scalaTestVersion = "3.2.19"
  val mockitoVersion = "5.20.0"
  val catsEffectTestVersion = "1.7.0"
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
