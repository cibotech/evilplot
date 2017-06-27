enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "EvilPlot"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.lihaoyi" %%% "scalatags" % "0.6.1",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

// see http://www.scalatest.org/install
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

persistLauncher in Compile := true
