resolvers in ThisBuild += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers in ThisBuild += "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/"

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.9.0")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.1")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")

// Gerolf's release plugin
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")

// Generate swagger doc from the routes
addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.10.2-PLAY2.8")

// Report the licenses used in the project
addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

// Show the hash of the current version
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.7")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

