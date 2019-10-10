
resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/"

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.9.0")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.0")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")

// Gerolf's release plugin
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.3")

// Generate swagger doc from the routes
addSbtPlugin("com.iheart" % "sbt-play-swagger" % "0.7.8-PLAY2.7")

// Report the licenses used in the project
addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

