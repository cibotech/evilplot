enablePlugins(ScalaJSPlugin)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  organization := Settings.organization,
  version := Settings.version,
  crossScalaVersions := Settings.versions.crossScalaVersions,
  scalaVersion := crossScalaVersions.value.head,
  scalacOptions ++= Settings.scalacOptions
)

lazy val root = project.in(file("."))
  .aggregate(evilplotJS, evilplotJVM, assetJS, assetJVM)
  .settings(
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    crossScalaVersions := Settings.versions.crossScalaVersions
  )

lazy val evilplotAsset = crossProject.in(file("asset"))
  .dependsOn(evilplot)
  .aggregate(evilplot)
  .settings(
    name := "evilplot-asset",
    commonSettings,
    publishTo in ThisBuild := {
      val repo = ""
      if (isSnapshot.value) {
        Some("snapshots" at repo + "libs-snapshot-local")
      } else {
        Some("releases" at repo + "libs-release-local")
      }
    }
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

// "Core" project so we can package the JS with the jar in the "evilplot" project.
lazy val evilplot = crossProject.in(file("."))
  .settings(
    commonSettings,
    name := "evilplot",
    publishTo in ThisBuild := {
      val repo = ""
      if (isSnapshot.value) {
        Some("snapshots" at repo + "libs-snapshot-local")
      } else {
        Some("releases" at repo + "libs-release-local")
      }
    },
    libraryDependencies ++= Settings.sharedDependencies.value,
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    crossScalaVersions := Settings.versions.crossScalaVersions
  )
  .jsSettings(
    libraryDependencies ++= Settings.scalajsDependencies.value,
    libraryDependencies ++= Settings.sharedDependencies.value,
    jsDependencies ++= Settings.jsDependencies.value,
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    jsEnv in Test := new PhantomJS2Env(scalaJSPhantomJSClassLoader.value),
    skip in packageJSDependencies := false,
    scalaJSUseMainModuleInitializer := false,
    scalaJSUseMainModuleInitializer in Test := false,
    artifactPath := baseDirectory.value
  )
  .jvmSettings(
    libraryDependencies ++= Settings.jvmDependencies.value
  )
  .enablePlugins(WorkbenchPlugin)

lazy val evilplotJS: Project = evilplot.js
lazy val evilplotJVM: Project = evilplot.jvm

