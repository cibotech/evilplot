import sbt.Keys._
import sbt.Project.projectToRef

// adapted from ochrons/scalajs-spa-tutorial

lazy val root =(project in file("."))
  .aggregate(sharedJS, sharedJVM, js, jvm)
  .settings(publishArtifact := false)

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
.settings(
  name := s"${Settings.name}_shared",
  organization := Settings.organization,
  version := Settings.version,
  scalaVersion := Settings.versions.scala,
  libraryDependencies ++= Settings.sharedDependencies.value,
  publishTo in ThisBuild := {
  val repo = ""
  if (isSnapshot.value) {
    Some("snapshots" at repo + "libs-snapshot-local")
    } else {
      Some("releases" at repo + "libs-release-local")
    }
  }
  )


/*lazy val evilplot =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .settings(sharedSettings)
*/

lazy val sharedJVM: Project = shared.jvm
lazy val sharedJS: Project = shared.js

// instantiate the JS project for SBT with some additional settings
lazy val js: Project = (project in file("js"))
.settings(
  name := "evilplot",
  organization := Settings.organization,
  version := Settings.version,
  scalaVersion := Settings.versions.scala,
  scalacOptions ++= Settings.scalacOptions,
  libraryDependencies ++= Settings.scalajsDependencies.value,
  libraryDependencies ++= Settings.sharedDependencies.value,
  jsDependencies ++= Settings.jsDependencies.value,
  jsDependencies += RuntimeDOM,
  jsEnv in Test := new PhantomJS2Env(scalaJSPhantomJSClassLoader.value),
  skip in packageJSDependencies := false,
  scalaJSUseMainModuleInitializer in Test := false
).enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
.dependsOn(sharedJS)

// js projects (just one in this case)
lazy val jss = Seq(js)

// instantiate the JVM project for SBT with some additional settings
lazy val jvm: Project = (project in file("jvm"))
.settings(
  name := "evilplot",
  organization := Settings.organization,
  version := Settings.version,
  scalaVersion := Settings.versions.scala,
  scalacOptions ++= Settings.scalacOptions,
  libraryDependencies ++= Settings.jvmDependencies.value
  )
.aggregate(jss.map(projectToRef): _*)
.dependsOn(sharedJVM)



