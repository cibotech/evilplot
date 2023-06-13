import sbt.Keys.resolvers
import xerial.sbt.Sonatype._
import org.scalajs.linker.interface.ESVersion

enablePlugins(ScalaJSPlugin)

ThisBuild / crossScalaVersions := Settings.versions.crossScalaVersions
ThisBuild / scalaVersion := crossScalaVersions.value.head
ThisBuild / scalacOptions ++= Settings.scalacOptions


lazy val noPublish: Seq[Setting[_]] = Seq(
  publishArtifact := false,
  publish / skip := true,
  publishLocal := {}
)

lazy val publishSettings: Seq[Setting[_]] = Seq(
  organization := Settings.organization,
  organizationName := "CiBO Technologies",
  organizationHomepage := Some(new java.net.URL("http://www.cibotechnologies.com")),
  licenses += ("BSD 3-Clause", url("https://opensource.org/licenses/BSD-3-Clause")),
  sonatypeProjectHosting := Some(GitHubHosting("cibotech", "evilplot", "devops@cibotechnologies.com")),
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeProfileName := "io.github.cibotech",
  pomIncludeRepository := { _ => false },
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true)

lazy val `evilplot-root` = project
  .in(file("."))
  .settings(publishSettings)
  .aggregate(
    evilplotJVM,
    evilplotJS,
    evilplotRepl,
    evilplotJupyterScala,
    assetJVM,
    evilplotRunner,
    mathJS,
    mathJVM
  )
  .disablePlugins(HeaderPlugin)


lazy val commonSettings: Seq[Setting[_]] = Seq(
  crossScalaVersions := Settings.versions.crossScalaVersions,
  scalaVersion := crossScalaVersions.value.head,
  scalacOptions ++= Settings.scalacOptions
)

// Macroparadise is included in scala 2.13. Do contortion here for 2.12/2.13 crossbuild
Compile / scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 => "-Ymacro-annotations" :: "-release:8" :: Nil
    case _                       => "-Xfatal-warnings" :: "-Xsource:2.12" :: "-target:jvm-1.8" :: Nil
  }
}

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 => Nil
    case _ =>
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) :: Nil
  }
}

lazy val licenseSettings = Seq(
  homepage := Some(url("https://www.github.com/cibotech/evilplot")),
  startYear := Some(2018),
  description := "A Scala combinator-based visualization library.",
  headerLicense := Some(HeaderLicense.BSD3Clause("2018", "CiBO Technologies, Inc."))
)

lazy val evilplotAsset = crossProject(JSPlatform, JVMPlatform)
  .in(file("asset"))
  .dependsOn(evilplot)
  .settings(commonSettings)
  .settings(licenseSettings)
  .settings(publishSettings)
  .settings(
    name := "evilplot-asset",
    resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"
  )
  .jvmSettings(
    Compile / resourceGenerators += Def.task {
      val fullOptAsset = (Compile / (evilplotJS / fullOptJS)).value.data
      val fastOptAsset = (Compile / (evilplotJS / fastOptJS)).value.data
      val fullOptDest = (Compile / resourceDirectory).value / fullOptAsset.getName
      val fastOptDest = (Compile / resourceDirectory).value / fastOptAsset.getName
      IO.copy(Seq(fullOptAsset -> fullOptDest, fastOptAsset -> fastOptDest)).toSeq
    }
  )

lazy val assetJS = evilplotAsset.js
lazy val assetJVM = evilplotAsset.jvm

lazy val evilplotMath = crossProject(JSPlatform, JVMPlatform)
  .in(file("math"))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(licenseSettings)
  .settings(
    name := "evilplot-math",
    resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases",
    libraryDependencies ++= Settings.sharedMathDependencies.value
  )
  .jvmSettings(
    libraryDependencies ++= Settings.jvmMathDependencies.value
  )

lazy val mathJS = evilplotMath.js
lazy val mathJVM = evilplotMath.jvm

lazy val evilplot = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(publishSettings)
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
    Test / jsEnv := PhantomJSEnv().value,
    scalaJSLinkerConfig ~= { _.withESFeatures(_.withESVersion(ESVersion.ES5_1)) },
    packageJSDependencies / skip := false,
    scalaJSUseMainModuleInitializer := false,
    Test / scalaJSUseMainModuleInitializer := false
  )
  .jvmSettings(
    libraryDependencies ++= Settings.jvmDependencies.value
  )
  .dependsOn(evilplotMath)

lazy val evilplotJVM = evilplot.jvm
lazy val evilplotJS = evilplot.js.enablePlugins(JSDependenciesPlugin)

lazy val evilplotRunner = project
  .in(file("runner"))
  .aggregate(evilplotJS)
  .dependsOn(evilplotJS)
  .settings(noPublish)
  .settings(licenseSettings)

lazy val evilplotRepl = project
  .in(file("repl-plots"))
  .dependsOn(evilplotJVM)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(licenseSettings)
  .settings(
    name := "evilplot-repl"
  )

lazy val evilplotJupyterScala = project
  .in(file("jupyter-scala"))
  .dependsOn(evilplotJVM)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(licenseSettings)
  .settings(
    name := "evilplot-jupyter-scala",
    libraryDependencies ++= Settings.jupyterScalaDependencies.value,
    resolvers += "jitpack" at "https://jitpack.io"
  )

val EvilPlotJVM = config("jvm")
val EvilPlotJS = config("js")
lazy val apiDocProjects = Seq(evilplotJVM -> EvilPlotJVM, evilplotJS -> EvilPlotJS)
lazy val apiDocumentation = apiDocProjects.flatMap {
  case (project, conf) =>
    SiteScaladocPlugin.scaladocSettings(
      conf,
      project / (mappings in Scope(This, Select(Compile), Select(packageDoc.key), This)),
      s"scaladoc/${project.id.stripPrefix("evilplot").toLowerCase}"
    )
}

lazy val docs = project
  .in(file("docs"))
  .settings(noPublish)
  .settings(
    name := "evilplot-docs",
    micrositeName := "EvilPlot",
    description := "Combinators for graphics",
    organization := Settings.organization,
    organizationName := "CiBO Technologies",
    organizationHomepage := Some(new java.net.URL("http://www.cibotechnologies.com")),
    mdocIn := file("docs/src/main/tut"),
    micrositeGithubOwner := "cibotech",
    micrositeGithubRepo := "evilplot",
    micrositeFooterText := None,
    micrositeDocumentationUrl := "/evilplot/scaladoc/jvm/com/cibo/evilplot/index.html",
    micrositeBaseUrl := "/evilplot",
    micrositeShareOnSocial := false,
    micrositeGitterChannel := false,
    micrositePalette := Map(
      "brand-primary" -> "#008080",
      "brand-secondary" -> "#484848",
      "brand-tertiary" -> "#323232",
      "gray-dark" -> "#453E46",
      "gray" -> "#837F84",
      "gray-light" -> "#E3E2E3",
      "gray-lighter" -> "#F4F3F4",
      "white-color" -> "#FFFFFF"
    ),
    micrositeTheme := "pattern",
  )
  .settings(apiDocumentation)
  .enablePlugins(MicrositesPlugin)


