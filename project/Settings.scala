import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Settings {
  val organization = "com.cibo"

  val scalacOptions = Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
//    "-Xfatal-warnings", uncomment when internal deprecations are fixed
    "-Xsource:2.12",
    "-target:jvm-1.8"
  )

  object versions { //scalastyle:ignore
    val crossScalaVersions = Seq("2.12.4")
    val scalaDom = "0.9.3"
    val scalaTest = "3.0.7"
    val scalactic = "3.0.7"
    val scopt = "3.5.0"
    val circe = "0.9.0"
    val jupyterScala = "0.4.1"
    val scalacheck = "1.14.0"
  }

  val sharedDependencies = Def.setting(
    Seq(
      "io.circe" %%% "circe-core" % versions.circe,
      "io.circe" %%% "circe-generic" % versions.circe,
      "io.circe" %%% "circe-parser" % versions.circe,
      "io.circe" %%% "circe-generic-extras" % versions.circe,
      "org.scalactic" %%% "scalactic" % versions.scalactic,
      "org.scalacheck" %%% "scalacheck" % versions.scalacheck % "test",
      "org.scalatest" %%% "scalatest" % versions.scalaTest % "it,test",
      compilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)
    ))

  val jupyterScalaDependencies = Def.setting(
    Seq(
      "org.jupyter-scala" %% "kernel-api" % versions.jupyterScala % Provided
    ))

  val sharedMathDependencies = Def.setting(
    Seq(
      "org.scalactic" %%% "scalactic" % versions.scalactic,
      "org.scalatest" %%% "scalatest" % versions.scalaTest % "test",
      compilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)
    )
  )

  val jvmMathDependencies = Def.setting(
    Seq(
      "org.scalanlp"               %% "breeze"          % "0.13.2",
      "org.scalanlp"               %% "breeze-natives"  % "0.13.2", // removing will still work but will be slower
    ))

  val jvmDependencies = Def.setting(
    Seq(
      ))

  val scalajsDependencies = Def.setting(
    Seq(
      "org.scala-js" %%% "scalajs-dom" % versions.scalaDom
    ))

  val jsDependencies = Def.setting(
    Seq(
      ))
}
