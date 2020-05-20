addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.4.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.32")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.1")
dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.32"

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")
//addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.1")
addSbtPlugin("com.47deg" % "sbt-microsites" % "0.7.18")

dependencyOverrides ++= Seq(
  "org.scala-js" % "sbt-scalajs" % "0.6.32",
  "com.typesafe.akka" %% "akka-actor" % "2.6.1",
  "com.typesafe.akka" %% "akka-stream" % "2.6.1"
)
