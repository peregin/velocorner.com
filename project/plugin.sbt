ThisBuild / resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
ThisBuild / resolvers += "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/"

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.1")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")

// Gerolf's release plugin
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

// Generate swagger doc from the routes
addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.10.5-PLAY2.8")

// Report the licenses used in the project
addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

// Show the hash of the current version
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.13")

// format scala classes and generated file, see ScalafmtExtensionPlugin
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// checkDuplicates for resources and classes
addSbtPlugin("com.github.sbt" % "sbt-duplicates-finder" % "1.1.0")
