
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.9.0")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.3")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

// Gerolf's release plugin
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.7")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.15")

// Library to fetch dependencies from Maven / Ivy repositories super fast - 1.1.0-M4
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.3")

