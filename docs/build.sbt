scalaVersion := "2.12.4"
enablePlugins(MicrositesPlugin)

lazy val scalaFiddleUrl = {
	if (sys.env.get("TRAVIS_BRANCH").contains("master")) "ignoreForNow"
	else "http://localhost:8880/"
}

micrositeConfigYaml := microsites.ConfigYml(yamlInline =
s"""
  |scalafiddle:
  |  dependency: com.cibo %%% evilplot % 0.1.883
  |  scalaFiddleUrl: $scalaFiddleUrl
""".stripMargin)

name := "EvilPlot"
description := "Combinators for graphics"
organizationName := "CiBO Technologies"
organizationHomepage := Some(new java.net.URL("http://www.cibotechnologies.com"))
micrositeGithubOwner := "cibotech"
micrositeGithubRepo := "evilplot"
micrositeGitterChannel := false
micrositeFooterText := None

micrositePalette := Map(
  "brand-primary" -> "#008080",
  "brand-secondary" -> "#485155",
  "brand-tertiary"    -> "#2B7699",
  "gray-dark"         -> "#453E46",
  "gray"              -> "#837F84",
  "gray-light"        -> "#E3E2E3",
  "gray-lighter"      -> "#F4F3F4",
  "white-color"       -> "#FFFFFF"
)

