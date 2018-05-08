scalaVersion := "2.12.4"
enablePlugins(MicrositesPlugin)

name := "EvilPlot"
description := "Combinators for graphics"
organizationName := "CiBO Technologies"
organizationHomepage := Some(new java.net.URL("http://www.cibotechnologies.com"))
micrositeGithubOwner := "cibotech"
micrositeGithubRepo := "evilplot"
micrositeFooterText := None
micrositeDocumentationUrl := "/evilplot/scaladoc/jvm/index.html"
micrositeBaseUrl := "/evilplot"
micrositeShareOnSocial := false
micrositeGitterChannel := false
micrositePalette := Map(
  "brand-primary" -> "#008080",
  "brand-secondary" -> "#484848",
  "brand-tertiary" -> "#323232",
  "gray-dark" -> "#453E46",
  "gray" -> "#837F84",
  "gray-light" -> "#E3E2E3",
  "gray-lighter" -> "#F4F3F4",
  "white-color" -> "#FFFFFF"
)
