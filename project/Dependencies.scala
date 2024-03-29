object Dependencies {

  val projectScalaVersion = "2.13.13"

  val flywayVersion = "10.10.0"
  val catsVersion = "2.10.0"
  val catsEffectVersion = "3.5.4"
  val mouseVersion = "1.2.3"
  val fs2Version = "3.10.1"
  val http4s = "0.23.26"
  val zioVersion = "2.0.21"
  val finatraVersion = "21.2.0" // in the test service
  val argonautVersion = "6.3.9" // in the test service
  val playWsVersion = "3.0.2" // standalone version
  val sangriaVersion = "4.1.0"
  val playJsonVersion = "3.0.2"
  val shapelessVersion = "2.3.10"
  val logbackVersion = "1.5.3" // updating will cause conflict
  val doobieVersion = "1.0.0-RC5"
  val orientDbVersion = "3.2.28"
  val mongoDbVersion = "5.0.0"
  val rethinkDbVersion = "2.4.4"
  val elasticVersion = "8.11.5"
  val jwtVersion = "10.0.0"
  val squantsVersion = "1.8.3"
  val sparkVersion = "3.5.1"
  val circeVersion = "0.14.6"
  val scalacacheVersion = "0.28.0"
  val jsoupVersion = "1.17.2"
  val scalaTestVersion = "3.2.18"
  val mockitoVersion = "5.11.0"
  val catsEffectTestVersion = "1.5.0"

  val springVersion = "3.2.4" // java world
}

object DockerBuild {

  val baseImage = "openjdk:17-slim-buster"
  val maintainer = "velocorner.com@gmail.com"
}
