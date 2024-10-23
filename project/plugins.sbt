addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.2.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs-env-phantomjs" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.21")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.4.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.17.0")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")
addSbtPlugin("com.47deg" % "sbt-microsites" % "1.4.3")

dependencyOverrides ++= Seq(
  "org.scala-js" % "sbt-scalajs" % "1.17.0",
  "com.typesafe.akka" %% "akka-actor" % "2.6.1",
  "com.typesafe.akka" %% "akka-stream" % "2.6.1"
)


