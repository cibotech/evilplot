enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "EvilPlot"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.3",
  "com.lihaoyi" %%% "scalatags" % "0.6.5",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test",
  // allows us to run tests in IntellJ
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

// see http://www.scalatest.org/install
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

// see https://www.scala-js.org/doc/project/building.html
scalaJSUseMainModuleInitializer := true

jsDependencies += RuntimeDOM

jsEnv in Test := new PhantomJS2Env(scalaJSPhantomJSClassLoader.value)
