addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.0.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.22")
dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.22"

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")
addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.1")
addSbtPlugin("com.47deg" % "sbt-microsites" % "0.7.18")

dependencyOverrides ++= Seq(
  "org.scala-js" % "sbt-scalajs" % "0.6.22",
  "com.typesafe.akka" %% "akka-actor" % "2.4.19",
  "com.typesafe.akka" %% "akka-stream" % "2.4.19"
)

