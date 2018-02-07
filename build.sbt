enablePlugins(ScalaJSPlugin)

crossScalaVersions in ThisBuild := Settings.versions.crossScalaVersions
scalaVersion in ThisBuild := crossScalaVersions.value.head
scalacOptions in ThisBuild ++= Settings.scalacOptions

lazy val root = project.in(file("."))
  .aggregate(evilplotJVM, evilplotJS, assetJVM, evilplotRunner)
  .settings(
    publishArtifact := false
  )

lazy val commonSettings: Seq[Setting[_]] = Seq(
  version := Settings.version,
  organization := Settings.organization,
  crossScalaVersions := Settings.versions.crossScalaVersions,
  scalaVersion := crossScalaVersions.value.head,
  scalacOptions ++= Settings.scalacOptions,
  publishTo in ThisBuild := {
    val repo = ""
    if (isSnapshot.value) {
      Some("snapshots" at repo + "libs-snapshot-local")
    } else {
      Some("releases" at repo + "libs-release-local")
    }
  }
)

lazy val evilplotAsset = crossProject.in(file("asset"))
  .dependsOn(evilplot)
  .settings(commonSettings)
  .settings(
    name := "evilplot-asset"
  )
  .jvmSettings(
    resourceGenerators.in(Compile) += Def.task {
      val asset = fullOptJS.in(evilplotJS).in(Compile).value.data
      val dest = resourceDirectory.in(Compile).value / asset.getName
      IO.copy(Seq(asset -> dest)).toSeq
    }
  )

lazy val assetJS = evilplotAsset.js
lazy val assetJVM = evilplotAsset.jvm

lazy val evilplot = crossProject.in(file("."))
  .settings(commonSettings)
  .settings(
    name := "evilplot",
    libraryDependencies ++= Settings.sharedDependencies.value
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
  .jvmSettings(
    libraryDependencies ++= Settings.jvmDependencies.value
  )

lazy val evilplotJVM = evilplot.jvm
lazy val evilplotJS = evilplot.js

// For the workbench plugin
lazy val evilplotRunner = project.in(file("runner"))
  .aggregate(evilplotJS)
  .dependsOn(evilplotJS)
  .settings(
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
  .enablePlugins(WorkbenchPlugin)

