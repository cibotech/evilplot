enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "EvilPlot"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.3",
  "com.lihaoyi" %%% "scalatags" % "0.6.1",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
)

// see http://www.scalatest.org/install
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"


// See https://www.scala-js.org/tutorial/basic/index.html
// This is an application with a main method
scalaJSUseMainModuleInitializer := true
jsDependencies += RuntimeDOM
