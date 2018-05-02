import sbt.Keys.resolvers

enablePlugins(ScalaJSPlugin)

crossScalaVersions in ThisBuild := Settings.versions.crossScalaVersions
scalaVersion in ThisBuild := crossScalaVersions.value.head
scalacOptions in ThisBuild ++= Settings.scalacOptions

lazy val root = project
  .in(file("."))
  .aggregate(evilplotJVM, evilplotJS, assetJVM, evilplotRunner)
  .settings(
    publishArtifact := false
  )
  .disablePlugins(HeaderPlugin)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  organization := Settings.organization,
  crossScalaVersions := Settings.versions.crossScalaVersions,
  scalaVersion := crossScalaVersions.value.head,
  scalacOptions ++= Settings.scalacOptions
)

lazy val licenseSettings = Seq(
  homepage := Some(url("https://www.github.com/cibotech/evilplot")),
  startYear := Some(2018),
  description := "A Scala combinator-based visualization library.",
  headerLicense := Some(HeaderLicense.BSD3Clause("2018", "CiBO Technologies, Inc."))
)

lazy val evilplotAsset = crossProject
  .in(file("asset"))
  .dependsOn(evilplot)
  .settings(commonSettings)
  .settings(licenseSettings)
  .settings(
    name := "evilplot-asset",
    resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
  )
  .jvmSettings(
    resourceGenerators.in(Compile) += Def.task {
      val fullOptAsset = fullOptJS.in(evilplotJS).in(Compile).value.data
      val fastOptAsset = fastOptJS.in(evilplotJS).in(Compile).value.data
      val fullOptDest = resourceDirectory.in(Compile).value / fullOptAsset.getName
      val fastOptDest = resourceDirectory.in(Compile).value / fastOptAsset.getName
      IO.copy(Seq(fullOptAsset -> fullOptDest, fastOptAsset -> fastOptDest)).toSeq
    }
  )

lazy val assetJS = evilplotAsset.js
lazy val assetJVM = evilplotAsset.jvm

lazy val evilplot = crossProject
  .in(file("."))
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(
    name := "evilplot",
    libraryDependencies ++= Settings.sharedDependencies.value,
    Defaults.itSettings
  )
  .settings(licenseSettings)
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
lazy val evilplotRunner = project
  .in(file("runner"))
  .aggregate(evilplotJS)
  .dependsOn(evilplotJS)
  .settings(
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
  .settings(licenseSettings)
  .enablePlugins(WorkbenchPlugin)
