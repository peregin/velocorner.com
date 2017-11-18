
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

// gen-idea plugin for IntelliJ
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// eclipse support
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.8.2")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.3")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.7")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

// Gerolf's release plugin
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.5")

