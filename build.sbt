import play.sbt.routes.RoutesCompiler.autoImport.*
import sbtbuildinfo.BuildInfoKeys
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.*
import com.typesafe.sbt.SbtNativePackager.autoImport.*
import play.sbt.PlayImport.*

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

val psqlDbClient = Seq(
  "org.tpolecat" %% "doobie-core" % Dependencies.doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % Dependencies.doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % Dependencies.doobieVersion,
  "org.flywaydb" % "flyway-core" % Dependencies.flywayVersion,
  "org.flywaydb" % "flyway-database-postgresql" % Dependencies.flywayVersion,
  "com.opentable.components" % "otj-pg-embedded" % "1.1.1" % "test"
)

val playJson = "org.playframework" %% "play-json" % Dependencies.playJsonVersion
// for more than 22 parameter case classes
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
  "org.apache.commons" % "commons-csv" % "1.14.1"
)

def scala3UsesScala213(module: ModuleID): ModuleID = module.cross(CrossVersion.for3Use2_13)

val playTestPlus = "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % "test"
val mockito = "org.mockito" % "mockito-core" % Dependencies.mockitoVersion % "test"
val scalaTest = "org.scalatest" %% "scalatest" % Dependencies.scalaTestVersion % "test"

def logging = Seq(
  "ch.qos.logback" % "logback-classic" % Dependencies.logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
  "org.codehaus.janino" % "janino" % "3.1.12", // conditional logback processing
  "com.github.loki4j" % "loki-logback-appender" % Dependencies.lokiLogbackVersion
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

def squants = Seq(
  "org.typelevel" %% "squants" % Dependencies.squantsVersion
)

def spark = Seq(
  scala3UsesScala213(
    ("org.apache.spark" %% "spark-mllib" % Dependencies.sparkVersion)
      .excludeAll("com.google.inject")
      .exclude("org.scala-lang.modules", "scala-xml_2.13")
      .exclude("org.scala-lang.modules", "scala-parser-combinators_2.13")
      .exclude("org.typelevel", "cats-kernel_2.13")
  )
)

def smile: Seq[ModuleID] = Seq(
  "com.github.haifengl" % "smile-core" % "5.2.3"
)

def http4s: Seq[ModuleID] = Seq(
  "org.http4s" %% "http4s-ember-client",
  "org.http4s" %% "http4s-ember-server",
  "org.http4s" %% "http4s-circe",
  "org.http4s" %% "http4s-dsl"
).map(_ % Dependencies.http4s)

def tapir: Seq[ModuleID] = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"
).map(_ % Dependencies.tapirVersion)

def circe: Seq[ModuleID] = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic"
).map(_ % Dependencies.circeVersion)

def scalacache = Seq(
  "com.github.cb372" %% "scalacache-core",
  "com.github.cb372" %% "scalacache-guava"
).map(module => scala3UsesScala213(module % Dependencies.scalacacheVersion))

lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  version := (ThisBuild / version).value,
  scalaVersion := Dependencies.projectScalaVersion,
  organization := "velocorner",
  description := "The Cycling Platform",
  javacOptions ++= Seq("-source", "17", "-target", "17"),
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
  scalacOptions ++= {
    if (scalaBinaryVersion.value == "3") Seq("-source:3.8-migration") else Seq.empty
  },
  versionScheme := Some("early-semver"),
  Test / scalacOptions ++= {
    if (scalaBinaryVersion.value == "2.13") Seq("-Yrangepos") else Seq.empty
  },
  ThisBuild / resolvers ++= Seq(
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
  ),
  packageDoc / publishArtifact := false,
  packageSrc / publishArtifact := false,
  libraryDependencySchemes ++= Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always
  ),
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2",
    "org.scala-lang.modules" %% "scala-xml" % "2.4.0"
  ),
  dependencyOverrides ++= Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"
  ),
  // because of: missing method in the newer ones, this is needed just for the Java 11 support
  dependencyUpdatesFilter -= moduleFilter(organization = "com.google.inject", name = "guice")
)

Global / excludeLintKeys += webApp / bomFileName
ThisBuild / useSuperShell := false

lazy val dataProvider = (project in file("data-provider") withId "data-provider")
  .settings(
    buildSettings,
    name := "data-provider",
    description := "from storage and API feeds",
    libraryDependencies ++= Seq(
      playJson,
      playJsonJoda,
      playWsAhcStandalone,
      "com.beachape" %% "enumeratum" % "1.9.7",
      scalaTest
    ) ++ logging ++ psqlDbClient ++ apacheCommons ++ cats ++ squants ++ catsEffect.map(_ % Test)
  )

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
    libraryDependencies ++= catsEffect ++ http4s ++ tapir ++ circe ++ scalacache ++ Seq(
      "org.typelevel" %% "log4cats-slf4j" % "2.8.0",
      "org.jsoup" % "jsoup" % Dependencies.jsoupVersion
    ) ++ catsEffectTest.map(_ % Test),
    BuildInfoKeys.buildInfoKeys := buildInfoKeys().value,
    buildInfoPackage := "velocorner.crawler.build",
    assembly / test := {},
    assembly / assemblyJarName := "crawler-service-all.jar",
    assembly / mainClass := Some("velocorner.crawler.Main"),
    assembly / assemblyMergeStrategy := {
      case PathList("reference.conf") =>
        MergeStrategy.concat
      case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
        MergeStrategy.first
      case PathList("META-INF", "FastDoubleParser-LICENSE") =>
        MergeStrategy.discard
      case manifest if manifest.contains("MANIFEST.MF") =>
        MergeStrategy.discard
      case PathList("META-INF", "versions", "9", "module-info.class") =>
        MergeStrategy.discard
      case "module-info.class" =>
        MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    libraryDependencies ++= {
      if (scalaBinaryVersion.value == "2.13")
        Seq(compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"))
      else Seq.empty
    }
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

lazy val webApp = (project in file("web-app") withId "web-app")
  .settings(
    buildSettings,
    name := "web-app",
    swaggerV3 := true,
    swaggerDomainNameSpaces := Seq("velocorner.api"),
    libraryDependencies ++= Seq(
      guice,
      ehcache,
      playWsJsonStandalone,
      "com.github.jwt-scala" %% "jwt-play-json" % Dependencies.jwtVersion,
      "com.google.inject" % "guice" % "5.1.0", // for Java 11 support, do not bump!!!
      playTestPlus,
      mockito,
      scalaTest
    ) ++ pekko ++ logging,
    routesGenerator := InjectedRoutesGenerator,
    BuildInfoKeys.buildInfoKeys := buildInfoKeys(extraKeys =
      Seq(
        "playVersion" -> play.core.PlayVersion.current,
        "catsVersion" -> Dependencies.catsVersion
      )
    ).value,
    buildInfoPackage := "velocorner.build",
    Universal / javaOptions ++= Seq("-Dplay.server.pidfile.path=/dev/null", "-Duser.timezone=UTC"),
    assembly / test := {},
    assembly / assemblyJarName := "web-app-all.jar",
    assembly / mainClass := Some("play.core.server.ProdServerStart"),
    assembly / fullClasspath += Attributed.blank(PlayKeys.playPackageAssets.value),
    assembly / assemblyMergeStrategy := {
      case PathList("reference.conf") =>
        MergeStrategy.concat
      case PathList("application.conf") =>
        MergeStrategy.last
      case manifest if manifest.contains("MANIFEST.MF") =>
        // We don't need manifest files since sbt-assembly will create
        // one with the given settings
        MergeStrategy.discard
      case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
        // Keep the content for all reference-overrides.conf files
        MergeStrategy.concat
      case PathList("META-INF", "versions", "9", "module-info.class") =>
        MergeStrategy.discard
      case "module-info.class" =>
        MergeStrategy.discard
      case x =>
        // For all the other files, use the default sbt-assembly merge strategy
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    // Enable SBOM generation for web-app module only
    makeBom / skip := false,
    bomFileName := "web-app.bom.json",
    bomFormat := "json"
  )
  .enablePlugins(
    play.sbt.PlayScala,
    SwaggerPlugin,
    BuildInfoPlugin,
    ScalafmtExtensionPlugin
  )
  .enablePlugins(PlayLogback)
  .dependsOn(dataProvider % "compile->compile; test->test")

// top level aggregate
lazy val root = (project in file(".") withId "velocorner")
  .aggregate(
    crawlerService,
    dataProvider,
    dataSearch,
    dataAnalytics,
    webApp
  )
  .settings(
    name := "velocorner",
    description := "Cycling platform with for data analytics with experiments on various technologies",
    buildSettings,
    onLoadMessage := WelcomeBanner.text().value
  )

addCommandAlias("fmt", "; scalafmtAll ; scalafmtSbt ; scalafmtGenerated")
