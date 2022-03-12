import play.sbt.routes.RoutesCompiler.autoImport._
import sbtbuildinfo.BuildInfoKeys
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import play.sbt.PlayImport._
import sbtrelease.ReleasePlugin.runtimeVersion

// setup common keys for every service, some of them might have extra build information
def buildInfoKeys(extraKeys: Seq[BuildInfoKey] = Seq.empty) = Def.setting(
  Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion,
    BuildInfoKey.action("buildTime") {
      // is parsed and used in sitemap as well
      java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now())
    },
    "gitHash" -> git.gitHeadCommit.value.getOrElse("n/a")
  ) ++ extraKeys
)

val rethinkClient = Seq(
  "com.rethinkdb" % "rethinkdb-driver" % Dependencies.rethinkDbVersion,
  "com.googlecode.json-simple" % "json-simple" % "1.1.1"
)
val mongoClient = "org.mongodb.scala" %% "mongo-scala-driver" % Dependencies.mongoDbVersion
val orientDbClient = "com.orientechnologies" % "orientdb-client" % Dependencies.orientDbVersion
val psqlDbClient = Seq(
  "org.tpolecat" %% "doobie-core" % Dependencies.doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % Dependencies.doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % Dependencies.doobieVersion,
  "org.flywaydb" % "flyway-core" % Dependencies.flywayVersion,
  "com.opentable.components" % "otj-pg-embedded" % "1.0.0" % "test"
)

val playJson = "com.typesafe.play" %% "play-json" % Dependencies.playJsonVersion
// for more than 22 parameter case classes
val playJsonExtensions = "ai.x" %% "play-json-extensions" % "0.42.0"
val playJsonJoda = "com.typesafe.play" %% "play-json-joda" % Dependencies.playJsonVersion
val playWsAhcStandalone = "com.typesafe.play" %% "play-ahc-ws-standalone" % Dependencies.playWsVersion
val playWsJsonStandalone = "com.typesafe.play" %% "play-ws-standalone-json" % Dependencies.playWsVersion

val apacheCommons = Seq(
  "commons-io" % "commons-io" % "2.11.0",
  "commons-codec" % "commons-codec" % "1.15",
  "org.apache.commons" % "commons-csv" % "1.9.0"
)

val playTest = "org.scalatestplus" %% "mockito-3-2" % "3.1.2.0" % "test"
val playTestPlus = "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
val mockito = "org.mockito" % "mockito-core" % Dependencies.mockitoVersion % "test"
val scalaTest = "org.scalatest" %% "scalatest" % Dependencies.scalaTestVersion % "test"

def logging = Seq(
  "ch.qos.logback" % "logback-classic" % Dependencies.logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "org.codehaus.janino" % "janino" % "3.1.6", // conditional logback processing
  "com.papertrailapp" % "logback-syslog4j" % "1.0.0"
)
def elastic4s = Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-core" % Dependencies.elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % Dependencies.elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % Dependencies.elasticVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Dependencies.elasticVersion % "test"
)

def cats = Seq(
  "org.typelevel" %% "cats-core" % Dependencies.catsVersion,
  "org.typelevel" %% "mouse" % Dependencies.mouseVersion
)

def catsEffect = Seq(
  "org.typelevel" %% "cats-effect" % Dependencies.catsEffectVersion
)

def zio = Seq(
  "dev.zio" %% "zio" % Dependencies.zioVersion,
  "dev.zio" %% "zio-logging" % Dependencies.zioLoggingVersion
)

def fs2 = Seq(
  "co.fs2" %% "fs2-core" % Dependencies.fs2Version,
  "co.fs2" %% "fs2-io" % Dependencies.fs2Version
)

def squants = Seq(
  "org.typelevel" %% "squants" % Dependencies.squantsVersion
)

def spark = Seq(
  "org.apache.spark" %% "spark-mllib" % Dependencies.sparkVersion exclude ("com.google.inject", "guice")
)

def smile = Seq(
  "com.github.haifengl" % "smile-core" % "2.6.0"
)

lazy val runWebAppDist: ReleaseStep = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    extracted.runAggregated(webApp / dist, st)
  }
)

lazy val runWebAppDockerPush: ReleaseStep = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    extracted.runAggregated(webApp / Docker / publish, st)
  }
)

lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  version := (ThisBuild / version).value,
  scalaVersion := Dependencies.projectScalaVersion,
  organization := "velocorner",
  description := "The Cycling Platform",
  javacOptions ++= Seq("-source", "11", "-target", "11"),
  scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
  versionScheme := Some("early-semver"),
  Test / scalacOptions ++= Seq("-Yrangepos"),
  ThisBuild / resolvers ++= Seq(
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
  ),
  packageDoc / publishArtifact := false,
  packageSrc / publishArtifact := false,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    // runClean, // it is disabled at release time, avoid reloading sbt
    // runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    // runWebAppDist, // it is a dependency of publish
    runWebAppDockerPush, // will push automatically the image to the docker hub
    setNextVersion,
    commitNextVersion
    // pushChanges  // travis/circleci release script will push the changes
  ),
  releaseCommitMessage := s"Setting version to ${runtimeVersion.value} [skip ci]", // it is invoked from ci, skip a new trigger
  releaseNextCommitMessage := s"Setting version to ${runtimeVersion.value} [skip ci]",
  libraryDependencySchemes += "org.scala-lang.modules" %% "scala-java8-compat" % "always",
  libraryDependencies ++= Seq("org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2")
)

lazy val dataProvider = (project in file("data-provider") withId "data-provider")
  .settings(
    buildSettings,
    name := "data-provider",
    libraryDependencies ++= Seq(
      playJson,
      playJsonExtensions,
      playJsonJoda,
      playWsAhcStandalone,
      scalaTest
    ) ++ logging
      ++ psqlDbClient
      ++ apacheCommons
      ++ cats
      ++ zio.map(_ % "test")
      ++ squants
  )

lazy val dataProviderExtension = (project in file("data-provider-ext") withId "data-provider-ext")
  .settings(
    buildSettings,
    name := "data-provider-ext",
    description := "Extensions for data-provider with various storage providers (OrientDb, RethinkDb, MongoDb, etc.)",
    libraryDependencies ++= Seq(
      playJson,
      playJsonExtensions,
      playJsonJoda,
      scalaTest
    ) ++ logging
      ++ Seq(mongoClient, orientDbClient) ++ rethinkClient
      ++ cats
      ++ zio.map(_ % "test")
  )
  .dependsOn(dataProvider % "test->test;compile->compile")

lazy val dataSearch = (project in file("data-search") withId "data-search")
  .settings(
    buildSettings,
    name := "data-search",
    libraryDependencies ++=
      elastic4s
        ++ catsEffect.map(_ % "test")
        ++ fs2.map(_ % "test"),
    dependencyOverrides ++= catsEffect
  )
  .dependsOn(dataProvider % "test->test;compile->compile")

// module for various analytics supporting the generic stack
lazy val dataAnalytics = (project in file("data-analytics") withId "data-analytics")
  .settings(
    buildSettings,
    name := "data-analytics",
    libraryDependencies ++= smile ++ logging
  )
  .dependsOn(dataProvider % "compile->compile; test->test")

// module dedicated for analytics with Spark, with a special Scala version
lazy val dataAnalyticsSpark = (project in file("data-analytics-spark") withId "data-analytics-spark")
  .settings(
    buildSettings,
    name := "data-analytics-spark",
    libraryDependencies ++= spark ++ logging ++ Seq(playJsonJoda)
  )
  .dependsOn(dataProvider % "compile->compile; test->test")

lazy val testServiceJava = (project in file("test/test-service-java") withId "test-service-java")
  .settings(
    buildSettings,
    name := "test-service-java",
    libraryDependencies ++= Seq(
      "com.twitter" %% "finatra-http" % Dependencies.finatraVersion,
      "com.chuusai" %% "shapeless" % Dependencies.shapelessVersion,
      "ch.qos.logback" % "logback-classic" % Dependencies.logbackVersion,
      "io.argonaut" %% "argonaut" % Dependencies.argonautVersion,
      "org.springframework.boot" % "spring-boot-starter-web" % Dependencies.springVersion,
      "javax.servlet" % "javax.servlet-api" % "4.0.1",
      scalaTest
    ) ++ cats,
    resolvers += "MavenRepository" at "https://mvnrepository.com/"
  )

lazy val testServiceScala = (project in file("test/test-service-scala") withId "test-service-scala")
  .settings(
    buildSettings,
    name := "test-service-scala",
    libraryDependencies ++= Seq(
      "com.twitter" %% "finatra-http" % Dependencies.finatraVersion,
      "com.chuusai" %% "shapeless" % Dependencies.shapelessVersion,
      "ch.qos.logback" % "logback-classic" % Dependencies.logbackVersion,
      "io.argonaut" %% "argonaut" % Dependencies.argonautVersion,
      scalaTest
    ) ++ cats,
    BuildInfoKeys.buildInfoKeys := buildInfoKeys().value,
    buildInfoPackage := "test.service.scala.build"
  )
  .enablePlugins(
    BuildInfoPlugin
  )

lazy val infoService = (project in file("info-service") withId "info-service")
  .settings(
    buildSettings,
    name := "info-service",
    libraryDependencies ++= cats ++ catsEffect,
    BuildInfoKeys.buildInfoKeys := buildInfoKeys().value,
    buildInfoPackage := "velocorner.info.build",
    maintainer := "velocorner.com@gmail.com",
    Docker / packageName := "velocorner.info",
    Docker / dockerExposedPorts := Seq(9100),
    dockerBaseImage := Dependencies.dockerBaseImage,
    dockerUsername := Some("peregin"),
    Docker / version := "latest"
  )
  .enablePlugins(
    BuildInfoPlugin,
    JavaAppPackaging,
    DockerPlugin
  )

lazy val webApp = (project in file("web-app") withId "web-app")
  .settings(
    buildSettings,
    name := "web-app",
    libraryDependencies ++= Seq(
      guice,
      ehcache,
      playWsJsonStandalone,
      "com.github.jwt-scala" %% "jwt-play-json" % Dependencies.jwtVersion,
      "com.google.inject" % "guice" % "5.1.0", // for Java 11 support,
      playTest,
      playTestPlus,
      mockito,
      scalaTest
    ),
    routesGenerator := InjectedRoutesGenerator,
    BuildInfoKeys.buildInfoKeys := buildInfoKeys(extraKeys =
      Seq(
        "elasticVersion" -> Dependencies.elasticVersion,
        "playVersion" -> play.core.PlayVersion.current,
        "catsVersion" -> Dependencies.catsVersion,
        "dockerBaseImage" -> Dependencies.dockerBaseImage
      )
    ).value,
    buildInfoPackage := "velocorner.build",
    maintainer := "velocorner.com@gmail.com",
    Docker / packageName := "velocorner.com",
    Docker / dockerExposedPorts := Seq(9000),
    dockerBaseImage := Dependencies.dockerBaseImage,
    dockerUsername := Some("peregin"),
    Docker / version := "latest",
    Universal / javaOptions ++= Seq("-Dplay.server.pidfile.path=/dev/null"),
    swaggerDomainNameSpaces := Seq("velocorner.api"),
    swaggerPrettyJson := true,
    swaggerV3 := true
    // whenever we generate build information, run the formatter on the generated files
    /*
    Compile / buildInfo := Def.taskDyn {
      val files = (Compile / buildInfo).value
      Def.task {
        (Compile / scalafmtGenerated).value
        files
      }
    }.value
     */
  )
  .enablePlugins(
    play.sbt.PlayScala,
    play.sbt.PlayAkkaHttp2Support,
    BuildInfoPlugin,
    com.iheart.sbtPlaySwagger.SwaggerPlugin,
    ScalafmtExtensionPlugin
  )
  .dependsOn(dataProvider % "compile->compile; test->test", dataSearch)

// top level aggregate
lazy val root = (project in file(".") withId "velocorner")
  .aggregate(
    dataProvider,
    dataProviderExtension,
    dataSearch,
    dataAnalytics,
    dataAnalyticsSpark,
    webApp,
    infoService,
    testServiceJava,
    testServiceScala
  )
  .settings(
    name := "velocorner",
    description := "Cycling platform with for data analytics with experiments on various technologies",
    buildSettings,
    onLoadMessage := WelcomeBanner.text().value
  )

addCommandAlias("fmt", "; scalafmtAll ; scalafmtSbt ; scalafmtGenerated")
