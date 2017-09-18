/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot
// turn off the warning about too many types defined in a file
// scalastyle:off
package object colors {
  sealed trait Color {
    val repr: String
  }
  case class HSL(hue: Int, saturation: Int, lightness: Int) extends Color {
    require(hue        >= 0 && hue        <  360, s"hue must be within [0, 360) {was $hue}")
    require(saturation >= 0 && saturation <= 100, s"saturation must be within [0, 100] {was $saturation}")
    require(lightness  >= 0 && lightness  <= 100, s"lightness must be within [0, 100] {was $lightness}")

    private def boundHue(hue: Int) = if (hue < 0) hue + 360 else if (hue > 360) hue - 360 else hue
    def triadic: (HSL, HSL) = (this.copy(hue = boundHue(this.hue - 120)), this.copy(hue = boundHue(this.hue + 120)))
    def analogous: (HSL, HSL) = (this.copy(hue = boundHue(this.hue - 14)), this.copy(hue = boundHue(this.hue + 14)))
    def incremental(increment: Int): (HSL, HSL) =
      (this.copy(hue = boundHue(this.hue - increment)), this.copy(hue = boundHue(this.hue + increment)))

    val repr = s"hsl($hue, $saturation%, $lightness%)"
  }

  case object Clear extends Color {
    val repr: String = "rgba(0,0,0,0)"
  }

  trait NamedColor extends Color
  case object AliceBlue             extends NamedColor { val repr = "aliceblue" }
  case object AntiqueWhite          extends NamedColor { val repr = "antiquewhite" }
  case object Aqua                  extends NamedColor { val repr = "aqua" }
  case object Aquamarine            extends NamedColor { val repr = "aquamarine" }
  case object Azure                 extends NamedColor { val repr = "azure" }
  case object Beige                 extends NamedColor { val repr = "beige" }
  case object Bisque                extends NamedColor { val repr = "bisque" }
  case object Black                 extends NamedColor { val repr = "black" }
  case object BlanchedAlmond        extends NamedColor { val repr = "blanchedalmond" }
  case object Blue                  extends NamedColor { val repr = "blue" }
  case object BlueViolet            extends NamedColor { val repr = "blueviolet" }
  case object Brown                 extends NamedColor { val repr = "brown" }
  case object Burlywood             extends NamedColor { val repr = "burlywood" }
  case object CadetBlue             extends NamedColor { val repr = "cadetblue" }
  case object Chartreuse            extends NamedColor { val repr = "chartreuse" }
  case object Chocolate             extends NamedColor { val repr = "chocolate" }
  case object Coral                 extends NamedColor { val repr = "coral" }
  case object CornflowerBlue        extends NamedColor { val repr = "cornflowerblue" }
  case object Cornsilk              extends NamedColor { val repr = "cornsilk" }
  case object Crimson               extends NamedColor { val repr = "crimson" }
  case object Cyan                  extends NamedColor { val repr = "cyan" }
  case object DarkBlue              extends NamedColor { val repr = "darkblue" }
  case object DarkCyan              extends NamedColor { val repr = "darkcyan" }
  case object DarkGoldenrod         extends NamedColor { val repr = "darkgoldenrod" }
  case object DarkGray              extends NamedColor { val repr = "darkgray" }
  case object DarkGreen             extends NamedColor { val repr = "darkgreen" }
  case object DarkGrey              extends NamedColor { val repr = "darkgrey" }
  case object DarkKhaki             extends NamedColor { val repr = "darkkhaki" }
  case object DarkMagenta           extends NamedColor { val repr = "darkmagenta" }
  case object DarkOliveGreen        extends NamedColor { val repr = "darkolivegreen" }
  case object DarkOrange            extends NamedColor { val repr = "darkorange" }
  case object DarkOrchid            extends NamedColor { val repr = "darkorchid" }
  case object DarkRed               extends NamedColor { val repr = "darkred" }
  case object DarkSalmon            extends NamedColor { val repr = "darksalmon" }
  case object DarkSeagreen          extends NamedColor { val repr = "darkseagreen" }
  case object DarkSlateBlue         extends NamedColor { val repr = "darkslateblue" }
  case object DarkSlateGray         extends NamedColor { val repr = "darkslategray" }
  case object DarkSlateGrey         extends NamedColor { val repr = "darkslategrey" }
  case object DarkTurquoise         extends NamedColor { val repr = "darkturquoise" }
  case object DarkViolet            extends NamedColor { val repr = "darkviolet" }
  case object DeepPink              extends NamedColor { val repr = "deeppink" }
  case object DeepskyBlue           extends NamedColor { val repr = "deepskyblue" }
  case object DimGray               extends NamedColor { val repr = "dimgray" }
  case object DimGrey               extends NamedColor { val repr = "dimgrey" }
  case object DodgerBlue            extends NamedColor { val repr = "dodgerblue" }
  case object Firebrick             extends NamedColor { val repr = "firebrick" }
  case object FloralWhite           extends NamedColor { val repr = "floralwhite" }
  case object ForestGreen           extends NamedColor { val repr = "forestgreen" }
  case object Fuchsia               extends NamedColor { val repr = "fuchsia" }
  case object Gainsboro             extends NamedColor { val repr = "gainsboro" }
  case object Ghostwhite            extends NamedColor { val repr = "ghostwhite" }
  case object Gold                  extends NamedColor { val repr = "gold" }
  case object Goldenrod             extends NamedColor { val repr = "goldenrod" }
  case object Gray                  extends NamedColor { val repr = "gray" }
  case object Green                 extends NamedColor { val repr = "green" }
  case object GreenYellow           extends NamedColor { val repr = "greenyellow" }
  case object Grey                  extends NamedColor { val repr = "grey" }
  case object Honeydew              extends NamedColor { val repr = "honeydew" }
  case object HotPink               extends NamedColor { val repr = "hotpink" }
  case object IndianRed             extends NamedColor { val repr = "indianred" }
  case object Indigo                extends NamedColor { val repr = "indigo" }
  case object Ivory                 extends NamedColor { val repr = "ivory" }
  case object Khaki                 extends NamedColor { val repr = "khaki" }
  case object Lavender              extends NamedColor { val repr = "lavender" }
  case object LavenderBlush         extends NamedColor { val repr = "lavenderblush" }
  case object LawnGreen             extends NamedColor { val repr = "lawngreen" }
  case object LemonChiffon          extends NamedColor { val repr = "lemonchiffon" }
  case object LightBlue             extends NamedColor { val repr = "lightblue" }
  case object LightCoral            extends NamedColor { val repr = "lightcoral" }
  case object Lightyan              extends NamedColor { val repr = "lightcyan" }
  case object LightGoldenrodYellow  extends NamedColor { val repr = "lightgoldenrodyellow" }
  case object LightGray             extends NamedColor { val repr = "lightgray" }
  case object LightGreen            extends NamedColor { val repr = "lightgreen" }
  case object LightGrey             extends NamedColor { val repr = "lightgrey" }
  case object LightPink             extends NamedColor { val repr = "lightpink" }
  case object LightSalmon           extends NamedColor { val repr = "lightsalmon" }
  case object LightSeaGreen         extends NamedColor { val repr = "lightseagreen" }
  case object LightSkyBlue          extends NamedColor { val repr = "lightskyblue" }
  case object LightSlateGray        extends NamedColor { val repr = "lightslategray" }
  case object LightSlateGrey        extends NamedColor { val repr = "lightslategrey" }
  case object LightSteelblue        extends NamedColor { val repr = "lightsteelblue" }
  case object LightYellow           extends NamedColor { val repr = "lightyellow" }
  case object Lime                  extends NamedColor { val repr = "lime" }
  case object LimeGreen             extends NamedColor { val repr = "limegreen" }
  case object Linen                 extends NamedColor { val repr = "linen" }
  case object Magenta               extends NamedColor { val repr = "magenta" }
  case object Maroon                extends NamedColor { val repr = "maroon" }
  case object MediumAquamarine      extends NamedColor { val repr = "mediumaquamarine" }
  case object MediumBlue            extends NamedColor { val repr = "mediumblue" }
  case object MediumOrchid          extends NamedColor { val repr = "mediumorchid" }
  case object MediumPurple          extends NamedColor { val repr = "mediumpurple" }
  case object MediumSeagreen        extends NamedColor { val repr = "mediumseagreen" }
  case object MediumSlateBlue       extends NamedColor { val repr = "mediumslateblue" }
  case object MediumSpringGreen     extends NamedColor { val repr = "mediumspringgreen" }
  case object MediumTurquoise       extends NamedColor { val repr = "mediumturquoise" }
  case object MediumVioletRed       extends NamedColor { val repr = "mediumvioletred" }
  case object MidnightBlue          extends NamedColor { val repr = "midnightblue" }
  case object MintCream             extends NamedColor { val repr = "mintcream" }
  case object MistyRose             extends NamedColor { val repr = "mistyrose" }
  case object Moccasin              extends NamedColor { val repr = "moccasin" }
  case object NavajoWhite           extends NamedColor { val repr = "navajowhite" }
  case object Navy                  extends NamedColor { val repr = "navy" }
  case object Oldlace               extends NamedColor { val repr = "oldlace" }
  case object Olive                 extends NamedColor { val repr = "olive" }
  case object Olivedrab             extends NamedColor { val repr = "olivedrab" }
  case object Orange                extends NamedColor { val repr = "orange" }
  case object OrangeRed             extends NamedColor { val repr = "orangered" }
  case object Orchid                extends NamedColor { val repr = "orchid" }
  case object PaleGoldenrod         extends NamedColor { val repr = "palegoldenrod" }
  case object PaleGreen             extends NamedColor { val repr = "palegreen" }
  case object PaleTurquoise         extends NamedColor { val repr = "paleturquoise" }
  case object PaleVioletRed         extends NamedColor { val repr = "palevioletred" }
  case object Papayawhip            extends NamedColor { val repr = "papayawhip" }
  case object PeachPuff             extends NamedColor { val repr = "peachpuff" }
  case object Peru                  extends NamedColor { val repr = "peru" }
  case object Pink                  extends NamedColor { val repr = "pink" }
  case object Plum                  extends NamedColor { val repr = "plum" }
  case object PowderBlue            extends NamedColor { val repr = "powderblue" }
  case object Purple                extends NamedColor { val repr = "purple" }
  case object Red                   extends NamedColor { val repr = "red" }
  case object RosyBrown             extends NamedColor { val repr = "rosybrown" }
  case object RoyalBlue             extends NamedColor { val repr = "royalblue" }
  case object SaddleBrown           extends NamedColor { val repr = "saddlebrown" }
  case object Salmon                extends NamedColor { val repr = "salmon" }
  case object SandyBrown            extends NamedColor { val repr = "sandybrown" }
  case object Seagreen              extends NamedColor { val repr = "seagreen" }
  case object Seashell              extends NamedColor { val repr = "seashell" }
  case object Sienna                extends NamedColor { val repr = "sienna" }
  case object Silver                extends NamedColor { val repr = "silver" }
  case object SkyBlue               extends NamedColor { val repr = "skyblue" }
  case object SlateBlue             extends NamedColor { val repr = "slateblue" }
  case object SlateGray             extends NamedColor { val repr = "slategray" }
  case object SlateGrey             extends NamedColor { val repr = "slategrey" }
  case object Snow                  extends NamedColor { val repr = "snow" }
  case object SpringGreen           extends NamedColor { val repr = "springgreen" }
  case object SteelBlue             extends NamedColor { val repr = "steelblue" }
  case object Tan                   extends NamedColor { val repr = "tan" }
  case object Teal                  extends NamedColor { val repr = "teal" }
  case object Thistle               extends NamedColor { val repr = "thistle" }
  case object Tomato                extends NamedColor { val repr = "tomato" }
  case object Turquoise             extends NamedColor { val repr = "turquoise" }
  case object Violet                extends NamedColor { val repr = "violet" }
  case object Wheat                 extends NamedColor { val repr = "wheat" }
  case object White                 extends NamedColor { val repr = "white" }
  case object WhiteSmoke            extends NamedColor { val repr = "whitesmoke" }
  case object Yellow                extends NamedColor { val repr = "yellow" }
  case object YellowGreen           extends NamedColor { val repr = "yellowgreen" }
  // scalastyle:on
}
