scalaVersion := "2.12.4"
enablePlugins(MicrositesPlugin)

lazy val scalaFiddleUrl = {
  if (sys.env.get("TRAVIS_BRANCH").contains("master")) "ignoreForNow"
  else "http://localhost:8880/"
}

name := "EvilPlot"
description := "Combinators for graphics"
organizationName := "CiBO Technologies"
organizationHomepage := Some(new java.net.URL("http://www.cibotechnologies.com"))
micrositeGithubOwner := "cibotech"
micrositeGithubRepo := "evilplot"
micrositeFooterText := None
micrositeDocumentationUrl := "/cibotech/evilplot/docs/"
micrositeBaseUrl := "/cibotech/evilplot/"
micrositeShareOnSocial := false
// Can turn these off depending on what we want to do...
// micrositeGithubLinks := false
micrositeGitterChannel := false

micrositePalette := Map(
  //"brand-primary" -> "#2B7699",
  "brand-primary" -> "#008080",
  "brand-secondary" -> "#606C71",
  "brand-tertiary" -> "#485155",
  "gray-dark" -> "#453E46",
  "gray" -> "#837F84",
  "gray-light" -> "#E3E2E3",
  "gray-lighter" -> "#F4F3F4",
  "white-color" -> "#FFFFFF"
)
