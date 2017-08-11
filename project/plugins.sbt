// see http://www.scalatest.org/install
// doesn't support Scala 2.12 yet, alas, commenting out for now (it's optional)
//addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.2")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.4.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.19")
dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.19"

// Tooling around scala-js: see https://github.com/lihaoyi/workbench
addSbtPlugin("com.lihaoyi" % "workbench" % "0.3.0")

// see http://www.scalatest.org/install
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
