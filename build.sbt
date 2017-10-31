import sbt.Keys.ivyScala
import sbt.Project.projectToRef

// adapted from https://github.com/ochrons/scalajs-spa-tutorial

lazy val root = (project in file("."))
  .aggregate(sharedJS, sharedJVM, js, jvm)
  .settings(
    publishArtifact := false,
    crossScalaVersions := Settings.versions.crossScalaVersions)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name := s"${Settings.name}",
  organization := Settings.organization,
  version := Settings.version,
  crossScalaVersions := Settings.versions.crossScalaVersions,
  scalaVersion := crossScalaVersions.value.head,
  scalacOptions ++= Settings.scalacOptions,
  ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
)

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(commonSettings)
  .settings(
    name := s"${Settings.name}_shared",
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

lazy val sharedJVM: Project = shared.jvm
lazy val sharedJS: Project = shared.js

// instantiate the JS project for SBT with some additional settings
lazy val js: Project = (project in file("js"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Settings.scalajsDependencies.value,
    libraryDependencies ++= Settings.sharedDependencies.value,
    jsDependencies ++= Settings.jsDependencies.value,
    jsDependencies += RuntimeDOM,
    jsEnv in Test := new PhantomJS2Env(scalaJSPhantomJSClassLoader.value),
    skip in packageJSDependencies := false,
    scalaJSUseMainModuleInitializer := false,
    scalaJSUseMainModuleInitializer in Test := false
  ).enablePlugins(WorkbenchPlugin)
  .dependsOn(sharedJS)


// js projects (just one in this case)
lazy val jss = Seq(js)

// instantiate the JVM project for SBT with some additional settings
lazy val jvm: Project = (project in file("jvm"))
  .settings(commonSettings: _*)
  .settings(
  libraryDependencies ++= Settings.jvmDependencies.value,
  resources in Compile += fullOptJS.in(js).in(Compile).value.data
  )
.aggregate(jss.map(projectToRef): _*)
.dependsOn(sharedJVM)
