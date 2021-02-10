import sbt.Keys.resolvers

enablePlugins(ScalaJSPlugin)

crossScalaVersions in ThisBuild := Settings.versions.crossScalaVersions
scalaVersion in ThisBuild := crossScalaVersions.value.head
scalacOptions in ThisBuild ++= Settings.scalacOptions

lazy val `evilplot-root` = project
  .in(file("."))
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
  .settings(
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
  .disablePlugins(HeaderPlugin)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  organization := Settings.organization,
  crossScalaVersions := Settings.versions.crossScalaVersions,
  scalaVersion := crossScalaVersions.value.head,
  scalacOptions ++= Settings.scalacOptions,
  bintrayOrganization := Some("cibotech"),
  bintrayRepository := "public",
  bintrayPackageLabels := Seq("scala", "plot", "visualization", "visualisation"),
  licenses += ("BSD 3-Clause", url("https://opensource.org/licenses/BSD-3-Clause"))
)

// Macroparadise is included in scala 2.13. Do contortion here for 2.12/2.13 crossbuild
Compile / scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 => "-Ymacro-annotations" :: Nil
    case _                       => Nil
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
  .settings(
    name := "evilplot-asset",
    resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"
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

lazy val evilplotMath = crossProject(JSPlatform, JVMPlatform)
  .in(file("math"))
  .settings(commonSettings)
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
    jsEnv in Test := PhantomJSEnv().value,
    scalaJSLinkerConfig ~= { _.withESFeatures(_.withUseECMAScript2015(false)) },
    skip in packageJSDependencies := false,
    scalaJSUseMainModuleInitializer := false,
    scalaJSUseMainModuleInitializer in Test := false
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
  .settings(
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
  .settings(licenseSettings)

lazy val evilplotRepl = project
  .in(file("repl-plots"))
  .dependsOn(evilplotJVM)
  .settings(commonSettings)
  .settings(licenseSettings)
  .settings(
    name := "evilplot-repl"
  )

lazy val evilplotJupyterScala = project
  .in(file("jupyter-scala"))
  .dependsOn(evilplotJVM)
  .settings(commonSettings)
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
      mappings in (Compile, packageDoc) in project,
      s"scaladoc/${project.id.stripPrefix("evilplot").toLowerCase}"
    )
}

lazy val docs = project
  .in(file("docs"))
  .settings(
    name := "evilplot-docs",
    micrositeName := "EvilPlot",
    description := "Combinators for graphics",
    organizationName := "CiBO Technologies",
    organizationHomepage := Some(new java.net.URL("http://www.cibotechnologies.com")),
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
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )
  .settings(apiDocumentation)
  .enablePlugins(MicrositesPlugin)
