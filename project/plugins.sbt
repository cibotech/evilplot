addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.0.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.22")
dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.22"

addSbtPlugin("com.lihaoyi" % "workbench" % "0.3.0")
