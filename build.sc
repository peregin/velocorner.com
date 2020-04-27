// build.sc
import mill._, scalalib._
import mill.scalalib.publish._
import coursier.maven.MavenRepository
import $ivy.`com.lihaoyi::mill-contrib-playlib:$MILL_VERSION`, mill.playlib._
import $ivy.`com.lihaoyi::mill-contrib-docker:$MILL_VERSION`
import contrib.docker.DockerModule

val projectScalaVersion = "2.13.1"

val catsVersion = "2.1.1"
val zioVersion = "1.0.0-RC18-2"
val logbackVersion = "1.2.3"
val doobieVersion = "0.9.0"
val orientDbVersion = "3.0.30"
val mongoDbVersion = "2.8.0"
val rethinkDbVersion = "2.4.0"
val elasticVersion = "7.6.0"
val playWsVersion = "2.1.2" // standalone version
val playJsonVersion = "2.8.1"
val specsVersion = "4.9.3"
val mockitoVersion = "3.3.3"

val logging = Agg(
  ivy"ch.qos.logback:logback-classic::$logbackVersion",
  ivy"com.typesafe.scala-logging::scala-logging::3.9.2",
  ivy"org.codehaus.janino:janino::3.1.2", // conditional logback processing
  ivy"com.papertrailapp:logback-syslog4j:1.0.0"
)
val apacheCommons = Agg(
  ivy"commons-io:commons-io:2.6",
  ivy"commons-codec:commons-codec:1.14"
)
val cats = Agg(
  ivy"org.typelevel::cats-core::$catsVersion",
  ivy"org.typelevel::mouse::0.24"
)
val zio = Agg(
  ivy"dev.zio::zio::$zioVersion"
)
val storage = Agg(
  ivy"com.rethinkdb:rethinkdb-driver::$rethinkDbVersion",
  ivy"org.mongodb.scala::mongo-scala-driver::$mongoDbVersion",
  ivy"com.orientechnologies:orientdb-client::$orientDbVersion",
  ivy"org.tpolecat::doobie-core::$doobieVersion",
  ivy"org.tpolecat::doobie-postgres::$doobieVersion",
  ivy"org.flywaydb:flyway-core::6.3.3"
)
val elastic4s = Agg(
  ivy"com.sksamuel.elastic4s::elastic4s-core::$elasticVersion",
  ivy"com.sksamuel.elastic4s::elastic4s-client-esjava::$elasticVersion",
  ivy"com.sksamuel.elastic4s::elastic4s-http-streams::$elasticVersion"
)
val specs2 = Agg(
  ivy"org.specs2::specs2-core::$specsVersion",
  ivy"org.specs2::specs2-junit::$specsVersion"
)

trait CommonModule extends SbtModule {
  def scalaVersion = T{projectScalaVersion}
  def scalacOptions = T{Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8")}
  def pomSettings = PomSettings(
    description = "The Cycling Platform",
    organization = "com.github.peregin",
    url = "https://github.com/peregin/velocorner.com",
    licenses = Seq(License.MIT),
    scm = SCM(
      "git://github.com/peregin/velocorner.com.git",
      "scm:git://github.com/peregin/velocorner.com.git"
    ),
    developers = Seq(
      Developer("peregin", "Levi", "https://github.com/peregin")
    )
  )
}

object dataSearch extends CommonModule {
  def millSourcePath = velocorner.millSourcePath / "data-search"
  def ivyDeps = T {elastic4s}
  def moduleDeps = Seq(dataProvider)
  object test extends Tests {
    def testFrameworks = Seq("org.specs2.runner.Specs2Framework")
    def ivyDeps = T {Agg(
      ivy"com.sksamuel.elastic4s::elastic4s-testkit::$elasticVersion"
    )}
    def moduleDeps = super.moduleDeps ++ Seq(dataProvider.test)
  }
}

object dataProvider extends CommonModule {
  def millSourcePath = velocorner.millSourcePath / "data-provider"
  def ivyDeps = T {Agg(
    ivy"com.typesafe.play::play-json::$playJsonVersion",
    ivy"com.typesafe.play::play-json-joda::$playJsonVersion",
    ivy"com.typesafe.play::play-ahc-ws-standalone::$playWsVersion",
    ivy"com.typesafe.play::play-ws-standalone-json::$playWsVersion",
    ivy"ai.x::play-json-extensions::0.40.2"
  ) ++ logging ++ storage ++ apacheCommons ++ cats ++ zio}
  override def repositories() = super.repositories ++ Seq(
    MavenRepository("https://repo.typesafe.com/typesafe/releases/")
  )
  object test extends Tests {
    def testFrameworks = Seq("org.specs2.runner.Specs2Framework")
    def ivyDeps = T {Agg(
      ivy"com.opentable.components:otj-pg-embedded::0.13.3"
    ) ++ specs2}
  }
}

object webApp extends CommonModule with PlayModule {
  def millSourcePath = velocorner.millSourcePath / "web-app"
  override def playVersion= T{"2.8.0"}
  override def twirlVersion= T{"1.4.0"}
  object test extends PlayTests
}

object velocorner extends CommonModule {
  def millSourcePath = os.pwd
  def moduleDeps = Seq(dataSearch, dataProvider, webApp)
}