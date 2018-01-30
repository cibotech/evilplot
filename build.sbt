enablePlugins(ScalaJSPlugin)

// adapted from https://github.com/ochrons/scalajs-spa-tutorial

lazy val root: Project = project.in(file("."))
  .aggregate(evilplotJS, evilplotJVM)
  .settings(
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    crossScalaVersions := Settings.versions.crossScalaVersions
  )

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name := s"${Settings.name}",
  organization := Settings.organization,
  version := Settings.version,
  crossScalaVersions := Settings.versions.crossScalaVersions,
  scalaVersion := crossScalaVersions.value.head,
  scalacOptions ++= Settings.scalacOptions
)

lazy val evilplot = crossProject.in(file("."))
  .settings(
    commonSettings,
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
  .jsSettings(
    libraryDependencies ++= Settings.scalajsDependencies.value,
    libraryDependencies ++= Settings.sharedDependencies.value,
    jsDependencies ++= Settings.jsDependencies.value,
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    jsEnv in Test := new PhantomJS2Env(scalaJSPhantomJSClassLoader.value),
    skip in packageJSDependencies := false,
    scalaJSUseMainModuleInitializer := false,
    scalaJSUseMainModuleInitializer in Test := false
  )
  .enablePlugins(WorkbenchPlugin)
  .jvmSettings(
    libraryDependencies ++= Settings.jvmDependencies.value
  )

lazy val evilplotJS = evilplot.js
lazy val evilplotJVM = evilplot.jvm
