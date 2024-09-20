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
    "gitHash" -> git.gitHeadCommit.value.getOrElse("n/a"),
    "builtFromBranch" -> git.gitCurrentBranch.value
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
  "org.flywaydb" % "flyway-database-postgresql" % Dependencies.flywayVersion,
  "com.opentable.components" % "otj-pg-embedded" % "1.1.0" % "test"
)

val playJson = "org.playframework" %% "play-json" % Dependencies.playJsonVersion
// for more than 22 parameter case classes
val playJsonExtensions = "ai.x" %% "play-json-extensions" % "0.42.0"
val playJsonJoda = "org.playframework" %% "play-json-joda" % Dependencies.playJsonVersion
val playWsAhcStandalone = "org.playframework" %% "play-ahc-ws-standalone" % Dependencies.playWsVersion
val playWsJsonStandalone = "org.playframework" %% "play-ws-standalone-json" % Dependencies.playWsVersion

val pekko = Seq(
  "org.apache.pekko" %% "pekko-slf4j" % Dependencies.pekkoVersion,
  "org.apache.pekko" %% "pekko-serialization-jackson" % Dependencies.pekkoVersion,
  "org.apache.pekko" %% "pekko-actor-typed" % Dependencies.pekkoVersion,
  "org.apache.pekko" %% "pekko-protobuf-v3" % Dependencies.pekkoVersion,
  "org.apache.pekko" %% "pekko-stream" % Dependencies.pekkoVersion
)

val apacheCommons = Seq(
  "org.apache.commons" % "commons-csv" % "1.11.0"
)

val playTest = "org.scalatestplus" %% "mockito-3-2" % "3.1.2.0" % "test"
val playTestPlus = "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % "test"
val mockito = "org.mockito" % "mockito-core" % Dependencies.mockitoVersion % "test"
val scalaTest = "org.scalatest" %% "scalatest" % Dependencies.scalaTestVersion % "test"

def logging = Seq(
  "ch.qos.logback" % "logback-classic" % Dependencies.logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.codehaus.janino" % "janino" % "3.1.12", // conditional logback processing
  "com.papertrailapp" % "logback-syslog4j" % "1.0.0"
)
def elastic4s = Seq(
  "nl.gn0s1s" %% "elastic4s-core" % Dependencies.elasticVersion,
  "nl.gn0s1s" %% "elastic4s-client-esjava" % Dependencies.elasticVersion,
  "nl.gn0s1s" %% "elastic4s-http-streams" % Dependencies.elasticVersion,
  "nl.gn0s1s" %% "elastic4s-testkit" % Dependencies.elasticVersion % Test
)

def cats = Seq(
  "org.typelevel" %% "cats-core" % Dependencies.catsVersion,
  "org.typelevel" %% "mouse" % Dependencies.mouseVersion
)

def catsEffect = Seq(
  "org.typelevel" %% "cats-effect" % Dependencies.catsEffectVersion withSources () withJavadoc ()
)

def catsEffectTest = Seq(
  "org.typelevel" %% "cats-effect-testing-scalatest" % Dependencies.catsEffectTestVersion
)

def zio = Seq(
  "dev.zio" %% "zio" % Dependencies.zioVersion
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

def smile: Seq[ModuleID] = Seq(
  "com.github.haifengl" % "smile-core" % "3.1.1"
)

def http4s: Seq[ModuleID] = Seq(
  "org.http4s" %% "http4s-ember-client",
  "org.http4s" %% "http4s-ember-server",
  "org.http4s" %% "http4s-circe",
  "org.http4s" %% "http4s-dsl"
).map(_ % Dependencies.http4s)

def circe: Seq[ModuleID] = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic"
).map(_ % Dependencies.circeVersion) ++ Seq(
  "io.circe" %% "circe-generic-extras" % "0.14.4"
)

def scalacache = Seq(
  "com.github.cb372" %% "scalacache-core",
  "com.github.cb372" %% "scalacache-guava"
).map(_ % Dependencies.scalacacheVersion)

def sangria = Seq(
  "org.sangria-graphql" %% "sangria" % Dependencies.sangriaVersion,
  "org.sangria-graphql" %% "sangria-slowlog" % "3.0.0",
  "org.sangria-graphql" %% "sangria-play-json" % "2.0.2"
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
  javacOptions ++= Seq("-source", "17", "-target", "17"),
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
  libraryDependencySchemes ++= Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always
  ),
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2"
  ),
  dependencyOverrides ++= Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
  ),
  // because of: missing method in the newer ones, this is needed just for the Java 11 support
  dependencyUpdatesFilter -= moduleFilter(organization = "com.google.inject", name = "guice")
)

lazy val dataProvider = (project in file("data-provider") withId "data-provider")
  .settings(
    buildSettings,
    name := "data-provider",
    description := "from storage and API feeds",
    libraryDependencies ++= Seq(
      playJson,
      playJsonExtensions,
      playJsonJoda,
      playWsAhcStandalone,
      "com.beachape" %% "enumeratum" % "1.7.4",
      scalaTest
    ) ++ logging
      ++ psqlDbClient
      ++ apacheCommons
      ++ cats
      ++ squants
      ++ catsEffect.map(_ % Test)
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
      ++ zio.map(_ % Test)
  )
  .dependsOn(dataProvider % "test->test;compile->compile")

lazy val dataSearchElastic = (project in file("data-search-elastic") withId "data-search-elastic")
  .settings(
    buildSettings,
    description := "search with classic ElasticSearch",
    name := "data-search-elastic",
    libraryDependencies ++=
      elastic4s
        ++ catsEffect.map(_ % Test)
        ++ fs2.map(_ % Test)
  )
  .dependsOn(dataProvider % "test->test;compile->compile")

lazy val dataSearch = (project in file("data-search") withId "data-search")
  .settings(
    buildSettings,
    name := "data-search",
    description := "search with zinc lightweight engine",
    libraryDependencies ++= catsEffect.map(_ % Test)
  )
  .dependsOn(dataProvider % "test->test;compile->compile")

lazy val crawlerService = (project in file("crawler-service") withId "crawler-service")
  .settings(
    buildSettings,
    name := "crawler-service",
    description := "product crawler with an up-to-date data feed",
    libraryDependencies ++= catsEffect
      ++ http4s
      ++ circe
      ++ scalacache
      ++ Seq(
        "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
        "org.jsoup" % "jsoup" % Dependencies.jsoupVersion
      )
      ++ catsEffectTest.map(_ % Test),
    BuildInfoKeys.buildInfoKeys := buildInfoKeys().value,
    buildInfoPackage := "velocorner.crawler.build",
    maintainer := DockerBuild.maintainer,
    Docker / packageName := "velocorner.crawler",
    Docker / dockerExposedPorts := Seq(9011),
    dockerBaseImage := DockerBuild.baseImage,
    dockerUsername := Some("peregin"),
    Docker / version := "latest",
    // implicit0, withFilter, final map
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
  .dependsOn(dataProvider % "test->test;compile->compile")
  .enablePlugins(
    BuildInfoPlugin,
    JavaAppPackaging,
    DockerPlugin
  )

// module for various analytics supporting the generic stack with different ML libraries
lazy val dataAnalytics = (project in file("data-analytics") withId "data-analytics")
  .settings(
    buildSettings,
    name := "data-analytics",
    libraryDependencies ++= spark ++ logging ++ Seq(playJsonJoda) ++ smile
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
      "org.springframework.boot" % "spring-boot-starter-actuator" % Dependencies.springVersion,
      "javax.servlet" % "javax.servlet-api" % "4.0.1",
      scalaTest
    ) ++ cats,
    resolvers += "MavenRepository" at "https://mvnrepository.com/"
  )

lazy val testServiceScala = (project in file("test/test-service-scala") withId "test-service-scala")
  .settings(
    buildSettings,
    name := "test-service-scala",
    description := "test service with finatra",
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

lazy val webApp = (project in file("web-app") withId "web-app")
  .settings(
    buildSettings,
    name := "web-app",
    libraryDependencies ++= Seq(
      guice,
      ehcache,
      playWsJsonStandalone,
      "com.github.jwt-scala" %% "jwt-play-json" % Dependencies.jwtVersion,
      "com.google.inject" % "guice" % "5.1.0", // for Java 11 support, do not bump!!!
      playTest,
      playTestPlus,
      mockito,
      scalaTest
    ) ++ pekko ++ logging ++ sangria,
    routesGenerator := InjectedRoutesGenerator,
    BuildInfoKeys.buildInfoKeys := buildInfoKeys(extraKeys =
      Seq(
        "playVersion" -> play.core.PlayVersion.current,
        "catsVersion" -> Dependencies.catsVersion,
        "dockerBaseImage" -> DockerBuild.baseImage
      )
    ).value,
    buildInfoPackage := "velocorner.build",
    maintainer := DockerBuild.maintainer,
    Docker / packageName := "velocorner.com",
    Docker / dockerExposedPorts := Seq(9000),
    dockerBaseImage := DockerBuild.baseImage,
    dockerUsername := Some("peregin"),
    dockerEntrypoint := Seq(s"bin/${executableScriptName.value}", "-Duser.timezone=UTC", "$JAVA_OPTS"),
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
    // play.sbt.PlayAkkaHttp2Support,
    BuildInfoPlugin,
    com.iheart.sbtPlaySwagger.SwaggerPlugin,
    ScalafmtExtensionPlugin
  )
  .enablePlugins(PlayLogback)
  .dependsOn(dataProvider % "compile->compile; test->test", dataSearch)

// top level aggregate
lazy val root = (project in file(".") withId "velocorner")
  .aggregate(
    crawlerService,
    dataProvider,
    dataProviderExtension,
    dataSearchElastic,
    dataSearch,
    dataAnalytics,
    webApp,
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
