
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

// gen-idea plugin for IntelliJ
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// eclipse support
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.1")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.9")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.5.0")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.9")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.4.0")

