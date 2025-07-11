ThisBuild / resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
ThisBuild / resolvers += "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/"

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

// it is sourced by sbt from 1.4.x
addDependencyTreePlugin
// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
// Gerolf's release plugin
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
// Generate swagger doc from the routes
addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.11.0")
// Report the licenses used in the project
addSbtPlugin("com.github.sbt" % "sbt-license-report" % "1.7.0")
// Show the hash of the current version
addSbtPlugin("com.github.sbt" % "sbt-git" % "2.1.0")
// Use the Play sbt plugin for Play projects
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.7")
// format scala classes and generated file, see ScalafmtExtensionPlugin
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.5")
// checkDuplicates for resources and classes
addSbtPlugin("com.github.sbt" % "sbt-duplicates-finder" % "1.1.0")
// refactoring and linting
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.3")
// generate bill of materials
addSbtPlugin("com.github.sbt" %% "sbt-sbom" % "0.4.0")
