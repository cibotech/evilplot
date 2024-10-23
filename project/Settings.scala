import sbt._
//import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Settings {
  val organization = "io.github.cibotech"

  val scalacOptions = Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
  )

  object versions { //scalastyle:ignore
    val crossScalaVersions = Seq("2.12.18", "2.13.11")
    val scalaDom = "0.9.8"
    val scalaTest = "3.2.15"
    val scalactic = "3.2.15"
    val scopt = "3.5.0"
    val circe = "0.14.9"
    val circeGenericExtras = "0.14.4"
    val jupyterScala = "0.4.1"
  }

  val sharedDependencies = Def.setting(
    Seq(
      "io.circe" %%% "circe-core" % versions.circe,
      "io.circe" %%% "circe-generic" % versions.circe,
      "io.circe" %%% "circe-parser" % versions.circe,
      "io.circe" %%% "circe-generic-extras" % versions.circeGenericExtras,
      "org.scalatest" %%% "scalatest" % versions.scalaTest % "it,test",
    )
  )

  val jupyterScalaDependencies = Def.setting(
    Seq(
      // "org.jupyter-scala" %% "kernel-api" % versions.jupyterScala % Provided
//      ("sh.almond" % "jupyter-api" % "0.9.1" % "provided").cross(CrossVersion.full)
      "sh.almond" %% "jupyter-api" % "0.9.1" % "provided"
    )
  )

  val sharedMathDependencies = Def.setting(
    Seq(
      "org.scalactic" %%% "scalactic" % versions.scalactic % "test",
      "org.scalatest" %%% "scalatest" % versions.scalaTest % "test"
    )
  )

  val jvmMathDependencies = Def.setting(
    Seq(
      "org.scalanlp" %% "breeze" % "1.0",
      "org.scalanlp" %% "breeze-natives" % "1.0" // removing will still work but will be slower
    )
  )

  val jvmDependencies = Def.setting(
    Seq(
      "org.scalactic" %%% "scalactic" % versions.scalactic % "test",
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.15.0" % "test",
      )
  )

  val scalajsDependencies = Def.setting(
    Seq(
      "org.scala-js" %%% "scalajs-dom" % versions.scalaDom
    )
  )

  val jsDependencies = Def.setting(
    Seq(
      )
  )
}
