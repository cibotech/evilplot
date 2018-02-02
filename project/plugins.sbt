addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.22")
dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.22"

// Tooling around scala-js: see https://github.com/lihaoyi/workbench
addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.0")

// Used to show project dependencies, e.g. `sbt dependencyTree`.
// See https://github.com/jrudolph/sbt-dependency-graph .
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

// see http://www.scalatest.org/install
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
