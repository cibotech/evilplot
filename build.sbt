enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "EvilPlot"

lazy val buildVersion = sys.env.getOrElse("TRAVIS_BUILD_NUMBER", (System.currentTimeMillis() / 1000).toString)

version      := s"0.1.$buildVersion"

scalaVersion := "2.12.3"

organization := "com.cibo"

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

publishTo in ThisBuild := {
  val repo = ""
  if (isSnapshot.value) {
    Some("snapshots" at repo + "libs-snapshot-local")
  } else {
    Some("releases" at repo + "libs-release-local")
  }
}