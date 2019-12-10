addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.12")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.3.1")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.31")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")
dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.31"

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")
//addSbtPlugin("com.lihaoyi" % "workbench" % "0.4.1")
addSbtPlugin("com.47deg" % "sbt-microsites" % "0.7.18")

dependencyOverrides ++= Seq(
  "org.scala-js" % "sbt-scalajs" % "0.6.31",
  "com.typesafe.akka" %% "akka-actor" % "2.6.1",
  "com.typesafe.akka" %% "akka-stream" % "2.6.1"
)

