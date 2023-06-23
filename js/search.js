// When the user clicks on the search box, we want to toggle the search dropdown
function displayToggleSearch(e) {
  e.preventDefault();
  e.stopPropagation();

  closeDropdownSearch(e);
  
  if (idx === null) {
    console.log("Building search index...");
    prepareIdxAndDocMap();
    console.log("Search index built.");
  }
  const dropdown = document.querySelector("#search-dropdown-content");
  if (dropdown) {
    if (!dropdown.classList.contains("show")) {
      dropdown.classList.add("show");
    }
    document.addEventListener("click", closeDropdownSearch);
    document.addEventListener("keydown", searchOnKeyDown);
    document.addEventListener("keyup", searchOnKeyUp);
  }
}

//We want to prepare the index only after clicking the search bar
var idx = null
const docMap = new Map()

function prepareIdxAndDocMap() {
  const docs = [  
    {
      "title": "Colors",
      "url": "/evilplot/colors.html",
      "content": "Colors Colors in EvilPlot are described by their HSL representation. import com.cibo.evilplot.colors._ val transparentColor = HSLA(210, 100, 56, 0.8) val color = HSL(210, 100, 56) // fully opaque You can also use the RGB representation to describe a color. It will be converted to HSL automatically. import com.cibo.evilplot.colors._ val transparentColor = RGBA(230, 126, 34, 0.8) val color = RGB(230, 126, 34) val hexColor = HEX(\"E67E22\") Predefined colors All of the HTML named colors are available to use: import com.cibo.evilplot.colors._ val htmlRed = HTMLNamedColors.red val htmlDodgerBlue = HTMLNamedColors.dodgerBlue val htmlTomato = HTMLNamedColors.tomato Modifying colors You can create colors by modifying the HSLA properties of another color. import com.cibo.evilplot.colors._ val htmlRed = HTMLNamedColors.red val transparentRed = htmlRed.copy(opacity = 0.5) val difficultWhite = htmlRed.copy(lightness = 100) EvilPlot has several helpful methods for modifying colors: import com.cibo.evilplot.colors._ val htmlRed = HTMLNamedColors.red val lighterRed = htmlRed.lighten(20) // Increase lightness by 20 val darkerRed = htmlRed.darken(20) // Two new colors evenly spaced around the HSL color wheel from the first val (triad1, triad2) = htmlRed.triadic // Two new colors adjacent to the first color on the color wheel // (+/- 45 degrees away in this example) val (analogue1, analogue2) = htmlRed.analogous(45) Color Sequences and Gradients A variety of streams and sequences of colors can be generated. import com.cibo.evilplot.colors._ val colors = Color.stream.take(40) import com.cibo.evilplot.colors._ // A gradient across all hues val colors = Color.getGradientSeq(10) import com.cibo.evilplot.colors._ // A gradient between specific hues val colors = Color.getGradientSeq(10, startHue = 0, endHue = 120) import com.cibo.evilplot.colors._ val colors = Color.getDefaultPaletteSeq(8) Data can be colored using a continuous gradient. import com.cibo.evilplot.colors._ import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.renderers.PointRenderer import com.cibo.evilplot.geometry.Extent import com.cibo.evilplot.numeric.Point val gradient = ContinuousColoring.gradient(HTMLNamedColors.blue, HTMLNamedColors.orange) val points = Seq.tabulate(100) { i =&gt; Point(i.toDouble, (math.sin(i.toDouble / 10) * 50) + 50) } val renderer = PointRenderer.depthColor(points.map(_.y), Some(gradient)) ScatterPlot(points, pointRenderer = Some(renderer)) .frame().xAxis().yAxis().xGrid().yGrid() .xbounds(0, 100).ybounds(0, 100) .render(Extent(500, 300)) import com.cibo.evilplot.colors._ // Multiple stop points can be given for continuous gradients val gradient = ContinuousColoring.gradient(Seq( HTMLNamedColors.green, HTMLNamedColors.red, HTMLNamedColors.blue, HTMLNamedColors.red ), Some(0d), Some(40d), GradientMode.Linear) val gradientFunc = gradient(Seq(0, 40)) val colors = Seq.tabulate(40) { gradientFunc(_) } Categorical data can be colored using a categorical gradient. import com.cibo.evilplot.colors._ import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.renderers.PointRenderer import com.cibo.evilplot.geometry.Extent import com.cibo.evilplot.numeric.Point val gradient: Coloring[Double] = CategoricalColoring.gradient( HTMLNamedColors.blue, HTMLNamedColors.orange ) val points = Seq.tabulate(100) { i =&gt; Point(i.toDouble, i.toDouble) } val renderer = PointRenderer.colorByCategory(points.map(p =&gt; p.x % 3), Some(gradient)) ScatterPlot(points, pointRenderer = Some(renderer)) .frame().xAxis().yAxis().xGrid().yGrid() .xbounds(0, 100).ybounds(0, 100) .rightLegend() .render(Extent(500, 300))"
    } ,    
    {
      "title": "Custom Components",
      "url": "/evilplot/custom-components.html",
      "content": "Custom Components Many of the plots in the documentation apply fluent methods to a plot before rendering, for example: import com.cibo.evilplot.plot._ import com.cibo.evilplot.numeric.Point LinePlot(Seq(Point(1, 1), Point(3, 3))) .xAxis().yAxis() .xGrid().yGrid() .hline(2d) .title(\"An example plot\") .render() In this plot, xAxis, yAxis, xGrid, yGrid, hline, and title, all add plot components to the plot. You can see the javadoc for all the built-in components here by filtering for the “Implicits” traits. Building a Custom Component You can build your own components by extending plot.compoments.PlotComponent. They are added to a plot using the plot’s component method. Every PlotComponent instance has a Position indicating how it should be rendered relative to the plot. Position.Overlay The component occupies the same space as the plot and is rendered in front of it. Position.Background Background is similar to Overlay but is rendered behind the plot. Position.Left, Position.Top, Position.Right, Position.Bottom Components with these positions are rendered outside of the plot space and placed next to the left, top, right, or bottom edge of the plot. Each component added at a position will be rendered outside previous components at that position (further from the plot). You can see this in the plot below where three Label components are added, all at Position.Top. LinePlot(Seq(Point(1, 1), Point(3, 3))) .xAxis().yAxis() .xGrid().yGrid() .hline(2d) .topLabel(\"First Label\") .topLabel(\"Second Label\") .topLabel(\"Third Label\") .render() Here’s a sample custom component that adds a marker to the right side of our example plot. import com.cibo.evilplot.geometry.{Align, Drawable, Extent, Polygon, Text} import com.cibo.evilplot.plot.aesthetics.Theme import com.cibo.evilplot.plot.components.{PlotComponent, Position} case class RightSideMarker( msg: String, value: Double ) extends PlotComponent { val position = Position.Right private val triangle: Drawable = Polygon(Seq(Point(0, 0), Point(20, 0), Point(10, 20))) private val marker: Drawable = Align.middle( triangle.rotated(90), Text(msg).padAll(5) ).reduce(beside) override def size(plot: Plot): Extent = marker.extent def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = { val yoffset = plot.ytransform(plot, extent)(value) - (marker.extent.height / 2) marker.translate(y = yoffset) } } LinePlot(Seq(Point(1, 1), Point(3, 3))) .xAxis().yAxis() .xGrid().yGrid() .hline(2d) .title(\"An example plot\") .component(RightSideMarker(\"marker at 2\", 2)) .render()"
    } ,    
    {
      "title": "Custom Renderers",
      "url": "/evilplot/custom-renderers.html",
      "content": "Custom Renderers In the introduction to plotting with EvilPlot, we gave ScatterPlot PointRenderer.colorByCategory as an argument. We also mentioned that there are several PointRenderers available by default. If you took a look at the plot catalog, you might have also seen that there were some other types of renderers, like BoxRenderer, PathRenderer, and BarRenderer that we can use to customize the base plots. All of these are PlotElementRenderers, and you’re not limited to the ones that EvilPlot provides. With a little bit of understanding of the Drawing API, implementing a renderer can be the easiest way to get a highly custom plot. Let’s look at the interface: trait PlotElementRenderer[C] { def render(plot: Plot, extent: Extent, context: C): Drawable } All you have to do is define how a piece of data gets displayed as a Drawable, and you’re done. When you call render() on the Plot later on, it will call render on your PlotElementRenderer and pass through the appropriate context. The question is: what are those three arguments, and what can you do with them? The first one is straightforward, you’ve seen Plot all over the place already! When you define your render method, you get a hook back into the plot, so you can get certain information out of it. For example, you might be interested in applying the plot’s xtransform or ytransform to some auxiliary data, and you can just pull that out: val xTransform: Double =&gt; Double = plot.xtransformer(plot, ...) The data to screen transformation will be different depending on how big we want to make the plot when we render it. That’s why render also gets an extent argument. This is the real size of the plot once it’s rendered. Keep in mind that we’re able to access that quantity here because renderers are only called when we turn the Plot into a Drawable, not when we construct the Plot. val xTransform: Double =&gt; Double = plot.ytransformer(plot, extent) Finally, the context. The context is a value of type C. The best way to explain this parameter is probably to look at the renderers you’ve already seen, and what their respective context type is: PointRenderer is a PlotElementRenderer[Int] PathRenderer is a PlotElementRenderer[Seq[Point]] BoxRenderer is a PlotElementRenderer[BoxPlotSummaryStatistics] PointRenderer uses its context as a way to index into another sequence of data (like a sequence of categories in the colorByCategory renderer). PathRenderer gets as context all the points that are in a particular path. Let’s implement a few custom renderers as examples: Implementing “jitter” You might be familiar with adding random “jitter” to points in a scatter plot from other plotting libraries. We can implement that in EvilPlot as a custom point renderer. This adds a bit of jitter in the y dimension. import com.cibo.evilplot.geometry.{Disc, Extent} import com.cibo.evilplot.plot.Plot import com.cibo.evilplot.plot.aesthetics.Theme import com.cibo.evilplot.plot.renderers.PointRenderer import scala.util.Random def jitter(range: Double)(implicit theme: Theme): PointRenderer = (plot: Plot, extent: Extent, context: Int) =&gt; { val scaleY = (y: Double) =&gt; plot.ytransform(plot, extent)(y + plot.ybounds.min) - extent.height Disc .centered(theme.elements.pointSize) .transY(scaleY(range * (Random.nextDouble() - .5))) } A plot using the default PointRenderer at left and the jitter renderer at right: Coloring data using a function Our jitter renderer didn’t access the context during plot creation at all, which makes it somewhat uninteresting and not illustrative of the power of what you can do with plot element renderers. Let’s write one that does. Imagine you need a renderer for a bar chart that colors the bar based on the value, say red for negative and blue for positive. This will work for non-stacked bar charts: import com.cibo.evilplot.colors.Color import com.cibo.evilplot.geometry.{Drawable, Extent, Rect} import com.cibo.evilplot.plot.{Bar, Plot} import com.cibo.evilplot.plot.renderers.BarRenderer def colorBy(fn: Double =&gt; Color): BarRenderer = new BarRenderer { def render(plot: Plot, extent: Extent, category: Bar): Drawable = Rect(extent) filled fn(category.values.head) } For a stacked bar chart, things might get a bit more complicated. But, we can use this and observe it in action. import com.cibo.evilplot.colors.Color import com.cibo.evilplot.plot._ val coloring: Double =&gt; Color = (d: Double) =&gt; if (d &lt;= 0) crimson else dodgerBlue BarChart.custom(Seq(-15, 22, -5).map(Bar(_)), Some(colorBy(coloring))) .xAxis(Seq(\"one\", \"two\", \"three\")) .yAxis() .hline(0) .frame() .render()"
    } ,    
    {
      "title": "Drawing API",
      "url": "/evilplot/drawing-api.html",
      "content": "Low-Level Drawing API Drawing Primitives In EvilPlot, we represent everything that can be drawn to the screen as a Drawable. A Drawable is simply a description of the scene, and constructing one does not actually render anything. There are only a handful of drawing primitives that exist within EvilPlot, and they can be divided into three categories: drawing, positioning, and style. The drawing primitives are the “leaves” of a scene. They represent concrete things like shapes and text. They are: Line Path Rect BorderRect Disc Wedge Polygon Text Positioning Primitives Positioning primitives alter where other Drawables are placed. Notably, Drawable scenes do not expose a notion of absolute coordinates. Translate Rotate Scale Affine Styling Primitives Styling primitives allow us to modify the appearance of scenes. Style colors the inside of a Drawable StrokeStyle colors the outline of a Drawable StrokeWeight changes the thickness of the outline of a Drawable LineDash applies a line style to the outline of a Drawable Composition Creating scenes is simply a matter of composing these primitives. For example, to place a blue rectangle next to a red circle, you might write: import com.cibo.evilplot.geometry._ import com.cibo.evilplot.colors._ val rect = Style(Rect(400, 400), HTMLNamedColors.red) Group( Seq( Style( Translate(Disc(200), x = rect.extent.width), HTMLNamedColors.blue ), rect ) ) The geometry package also provides convenient syntax for dealing with positioning and styling primitives. So, instead of nesting constructors like we did up there, we can do the following with the same result. import com.cibo.evilplot.geometry._ import com.cibo.evilplot.colors._ val rect = Rect(40, 40) filled HTMLNamedColors.red Disc(20) transX rect.extent.width filled HTMLNamedColors.blue behind rect The drawing API gives us the power to describe all of the scenes involved in the plots that EvilPlot can create; at no point do plot implementations reach below it and onto the rendering target. Positional Combinators EvilPlot gives a higher-level vocabulary to refer to common geometric manipulations. In fact, above we just wrote a positional combinator called beside. The geometry package is full of similar combinators, so most of the time you’ll never have to manually think about shifting objects by their widths like we did above. beside behind above Alignment Additionally, you can align an entire sequence of Drawables by calling one of the Align methods, which produce a new sequence with all elements aligned. Combining alignment with reducing using a binary position operator is especially helpful: import com.cibo.evilplot.geometry._ import com.cibo.evilplot.colors.HTMLNamedColors._ import com.cibo.evilplot.numeric.Point val aligned: Seq[Drawable] = Align.right( Polygon(Seq(Point(0, 30), Point(15, 0), Point(30, 30))) filled red, Rect(50, 50) filled blue, Disc(15) filled red, ) aligned.reduce(_ below _) The available alignment functions are:1. Align.bottom Align.right Align.middle Align.center An example With EvilPlot’s drawing combinators on our side, we’re well equipped to recreate the box example from above that we made using only primitives–except it will be far easier to reason about. Recall, first we created our elements: two lines and two boxes, then centered them vertically and placed them on top of each other. import com.cibo.evilplot.geometry._ import com.cibo.evilplot.colors.HTMLNamedColors.{blue, white} // Some values from the outside: val width = 100 val topWhisker = 60 val bottomWhisker = 40 val upperToMiddle = 50 val middleToLower = 45 val strokeWidth = 3 Align.center( Line(topWhisker, strokeWidth).rotated(90), BorderRect.filled(width, upperToMiddle), BorderRect.filled(width, middleToLower), Line(bottomWhisker, strokeWidth).rotated(90) ).reduce(_ above _) .colored(blue) .filled(white) And in fact, if you were to look at the source code for EvilPlot’s default box plot, it would be almost identical. Drawing to the screen Of course, at some point we want to draw our scenes to the screen. To do that, we call draw() trait Drawable { // ... def draw(ctx: RenderContext): Unit } The RenderContext is the interface between our drawing API and platform-specific rendering targets. For more information on how to obtain a RenderContext, see the docs page. A .reduce(_ above _) or .reduce(_ beside _) was applied to each of these so the examples didn’t stomp all over each other. &#8617;"
    } ,    
    {
      "title": "Getting Started",
      "url": "/evilplot/getting-started.html",
      "content": "Getting Started To get going with EvilPlot, you’ll need to add it to your build. EvilPlot is published for Scala 2.12 and 2.13. libraryDependencies += \"io.github.cibotech\" %% \"evilplot\" % \"0.9.0\" // Use %%% instead of %% if you're using ScalaJS Throughout the getting started guide, we’ll assume you’re working either in a Scala REPL or the Ammonite. We publish an additional utility to make using EvilPlot from the REPL easier. To import it, add: libraryDependencies += \"io.github.cibotech\" %% \"evilplot-repl\" % \"0.9.0\" // Use %%% instead of %% if you're using ScalaJS to your build. Our first plot EvilPlot is all about building larger graphical capabilities out of smaller ones. What this means for you is that making simple plots is easy, and we don’t have to spend a lot of time going over all the features just to get started. So let’s make our first plot, a simple scatter plot with sequential x-values and random y-values. Then we’ll open that plot up in a new window and take a look at it. import com.cibo.evilplot._ import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.aesthetics.DefaultTheme._ import com.cibo.evilplot.numeric.Point val data = Seq.tabulate(100) { i =&gt; Point(i.toDouble, scala.util.Random.nextDouble()) } displayPlot(ScatterPlot(data).render()) There’s a version of this example which renders to an HTML canvas using ScalaJS here To break it down: EvilPlot uses an implicit theming system, which lets you control the appearance of plots wholesale. In this case, we’re just using the built-in one, so we import it. ScatterPlot returns a Plot object, which is a description of how data should be plotted (plot these points as little circles on the screen), with what components (like axes, a background etc.). In this case, we’ve used no components. All we get is points on the screen! render() on Plot returns a Drawable object. Think of a Drawable as a fully specified description of a scene. render itself does not perform any side-effects, it simply constructs a scene given a plot and a size. Finally, displayPlot is an additional utility the opens the plot in a new window. You might not use this inside an application, but it’s useful for exploring in the REPL! This plot is not very interesting, of course. We should probably add some axes, so we know the range of the data, and some labels, so our audience knows what we’re talking about. That’s easy as well: import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.aesthetics.DefaultTheme._ import com.cibo.evilplot.numeric.Point val data = Seq.tabulate(100) { i =&gt; Point(i.toDouble, scala.util.Random.nextDouble()) } ScatterPlot(data) .xAxis() .yAxis() .frame() .xLabel(\"x\") .yLabel(\"y\") .render() Adding these things is simply a matter of calling an additional method on your plot that specifies exactly what type of component you want to add. Plot Construction Later on we’ll see that EvilPlot actually does not have different types for different kinds of plot – everything is “just a Plot.” ScatterPlot is just a convenience function that wraps the constructor for Plot. These convenience functions are likely what you’ll start with most of the time that you use EvilPlot, and there are a bunch of them in the plot package: ScatterPlot LinePlot ContourPlot BoxPlot BarChart Histogram Heatmap Facets Overlay Through this tutorial, we’ll see examples of using all of these, but the pattern is mostly the same as what we’ve seen above: call it with your data, add your components, convert to a scene. The rest of this tutorial is split up as follows: Using built-in plots A tour of the drawing API. How to use plot element renderers, which allow you to fully customize your plots. Colors and color utilities"
    } ,    
    {
      "title": "Home",
      "url": "/evilplot/",
      "content": "EvilPlot is a combinator-based data visualization library written in Scala. Its main objective is to enable composed, principled, and infinitely customizable data visualization without the need to cross from Scala into languages like R or Python. EvilPlot offers a handful of ways to create simple plots, along with several combinators for making more complicated plots out of those simpler ones. Everything is backed by a high-level two-dimensional drawing API, which makes it easy to invent new components for your plots from scratch. Check out our documentation, where you can learn just how easy and awesome it is to get going with EvilPlot. Alternatively, skip straight to the Plot Catalog for some examples of what EvilPlot can do, along with code. You can also take a look at the complete API documentation or the source on GitHub. Have a question, or want to get involved? Feel free to file an issue or open a pull request."
    } ,      
    {
      "title": "Plot Catalog",
      "url": "/evilplot/plot-catalog.html",
      "content": "Plot Catalog EvilPlot examples both simple and built-in as well as complex and custom are here. If you have one you’d like to share, please contribute it! Plot Catalog Bar Chart Clustered Bar Chart Stacked Bar Chart Clustered Stacked Bar Chart Function Plot Box Plot Scatter Plot Scatter Plot with Marginal Histograms Pie Chart Pairs Plot Density Plot Overlapping Histograms Contour Plot Bar Chart import com.cibo.evilplot.colors.RGB import com.cibo.evilplot.geometry.{Align, Drawable, Extent, Rect, Text} import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultFonts, DefaultTheme} import com.cibo.evilplot.plot.renderers.BarRenderer implicit val theme: DefaultTheme = DefaultTheme().copy( fonts = DefaultFonts() .copy(tickLabelSize = 14, legendLabelSize = 14, fontFace = \"'Lato', sans-serif\") ) val percentChange = Seq[Double](-10, 5, 12, 68, -22) val labels = Seq(\"one\", \"two\", \"three\", \"four\", \"five\") val labeledByColor = new BarRenderer { val positive = RGB(241, 121, 6) val negative = RGB(226, 56, 140) def render(plot: Plot, extent: Extent, category: Bar): Drawable = { val rect = Rect(extent) val value = category.values.head val color = if (value &gt;= 0) positive else negative Align.center(rect filled color, Text(s\"$value%\", size = 20) .filled(theme.colors.label) ).group } } BarChart .custom(percentChange.map(Bar.apply), spacing = Some(20), barRenderer = Some(labeledByColor) ) .standard(xLabels = labels) .hline(0) .render() Clustered Bar Chart import com.cibo.evilplot.plot._ val data = Seq[Seq[Double]]( Seq(1, 2, 3), Seq(4, 5, 6), Seq(3, 4, 1), Seq(2, 3, 4) ) BarChart .clustered( data, labels = Seq(\"one\", \"two\", \"three\") ) .title(\"Clustered Bar Chart Demo\") .xAxis(Seq(\"a\", \"b\", \"c\", \"d\")) .yAxis() .frame() .bottomLegend() .render() Stacked Bar Chart import com.cibo.evilplot.plot._ val data = Seq[Seq[Double]]( Seq(1, 2, 3), Seq(4, 5, 6), Seq(3, 4, 1), Seq(2, 3, 4) ) BarChart .stacked( data, labels = Seq(\"one\", \"two\", \"three\") ) .title(\"Stacked Bar Chart Demo\") .xAxis(Seq(\"a\", \"b\", \"c\", \"d\")) .yAxis() .frame() .bottomLegend() .render() Clustered Stacked Bar Chart import com.cibo.evilplot.plot._ val data = Seq[Seq[Seq[Double]]]( Seq(Seq(1, 2, 3), Seq(4, 5, 6)), Seq(Seq(3, 4, 1), Seq(2, 3, 4)) ) BarChart .clusteredStacked( data, labels = Seq(\"one\", \"two\", \"three\") ).title(\"Clustered Stacked Bar Chart Demo\") .standard(Seq(\"Category 1\", \"Category 2\")) .xLabel(\"Category\") .yLabel(\"Level\") .rightLegend() .render() Function Plot import com.cibo.evilplot.colors.HTMLNamedColors._ import com.cibo.evilplot.numeric.Bounds import com.cibo.evilplot.plot._ Overlay( FunctionPlot.series(x =&gt; x * x, \"y = x^2\", HTMLNamedColors.dodgerBlue, xbounds = Some(Bounds(-1, 1))), FunctionPlot.series(x =&gt; math.pow(x, 3), \"y = x^3\", HTMLNamedColors.crimson, xbounds = Some(Bounds(-1, 1))), FunctionPlot.series(x =&gt; math.pow(x, 4), \"y = x^4\", HTMLNamedColors.green, xbounds = Some(Bounds(-1, 1))) ).title(\"A bunch of polynomials.\") .overlayLegend() .standard() .render() Box Plot import com.cibo.evilplot.plot._ import scala.util.Random val data = Seq.fill(10)(Seq.fill(Random.nextInt(30))(Random.nextDouble())) BoxPlot(data) .standard(xLabels = (1 to 10).map(_.toString)) .render() Scatter Plot import com.cibo.evilplot.numeric._ import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.renderers.PointRenderer val points = Seq.fill(150) { Point(Random.nextDouble(), Random.nextDouble()) } :+ Point(0.0, 0.0) val years = Seq.fill(150)(Random.nextDouble()) :+ 1.0 ScatterPlot( points, pointRenderer = Some(PointRenderer.depthColor(years, None, None)) ) .standard() .xLabel(\"x\") .yLabel(\"y\") .trend(1, 0) .rightLegend() .render() Scatter Plot with Marginal Histograms import com.cibo.evilplot.colors.RGB import com.cibo.evilplot.geometry.Extent import com.cibo.evilplot.geometry.LineStyle.DashDot import com.cibo.evilplot.numeric.Point import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.renderers.PointRenderer val allYears = (2007 to 2013).map(_.toDouble).toVector val data = Seq.fill(150)(Point(Random.nextDouble(), Random.nextDouble())) val years = Seq.fill(150)(allYears(Random.nextInt(allYears.length))) val xhist = Histogram(data.map(_.x), bins = 50) val yhist = Histogram(data.map(_.y), bins = 40) ScatterPlot( data = data, pointRenderer = Some(PointRenderer.colorByCategory(years)) ).topPlot(xhist) .rightPlot(yhist) .standard() .title(\"Measured vs Actual\") .xLabel(\"measured\") .yLabel(\"actual\") .trend(1, 0, color = RGB(45, 45, 45), lineStyle = LineStyle.DashDot) .overlayLegend(x = 0.95, y = 0.8) .render(Extent(600, 400)) Pie Chart import com.cibo.evilplot.plot._ val data = Seq(\"one\" -&gt; 1.5, \"two\" -&gt; 3.5, \"three\" -&gt; 2.0) PieChart(data).rightLegend().render() Pairs Plot A pairs plot can be built by combining ScatterPlot and Histogram plots with Facet. import com.cibo.evilplot.numeric.Point import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.aesthetics.DefaultTheme._ import scala.util.Random val labels = Vector(\"a\", \"b\", \"c\", \"d\") val data = for (i &lt;- 1 to 4) yield { (labels(i - 1), Seq.fill(10) { Random.nextDouble() * 10 }) } val plots = for ((xlabel, xdata) &lt;- data) yield { for ((ylabel, ydata) &lt;- data) yield { val points = (xdata, ydata).zipped.map { (a, b) =&gt; Point(a, b) } if (ylabel == xlabel) { Histogram(xdata, bins = 4) } else { ScatterPlot(points) } } } Facets(plots) .standard() .title(\"Pairs Plot with Histograms\") .topLabels(data.map { _._1 }) .rightLabels(data.map { _._1 }) .render() Density Plot A FunctionPlot can be used to build density plots. import com.cibo.evilplot.colors.Color import com.cibo.evilplot.numeric.Bounds import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.aesthetics.DefaultTheme._ import com.cibo.evilplot.plot.renderers.PathRenderer import scala.util.Random def gaussianKernel(u: Double): Double = { 1 / math.sqrt(2 * math.Pi) * math.exp(-0.5d * u * u) } def densityEstimate(data: Seq[Double], bandwidth: Double)( x: Double ): Double = { val totalProbDensity = data.map { x_i =&gt; gaussianKernel((x - x_i) / bandwidth) }.sum totalProbDensity / (data.length * bandwidth) } val data = Seq.fill(150)(Random.nextDouble() * 30) val colors = Color.getGradientSeq(3) val bandwidths = Seq(5d, 2d, 0.5d) Overlay( colors.zip(bandwidths).map { case (c, b) =&gt; FunctionPlot( densityEstimate(data, b), Some(Bounds(0, 30)), Some(500), Some(PathRenderer.default(color = Some(c))) ) }:_* ) .standard() .xbounds(0, 30) .render() Overlapping Histograms import com.cibo.evilplot.colors.HTMLNamedColors.{green, red} import com.cibo.evilplot.geometry.Extent import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.aesthetics.DefaultTheme._ import com.cibo.evilplot.plot.renderers.BarRenderer import scala.util.Random val plotAreaSize: Extent = Extent(1000, 600) val data = Seq.fill(150)(Random.nextDouble() * 22) val data2 = Seq.fill(150)((Random.nextDouble() * 28) + 12) Overlay( Histogram(data, barRenderer = Some(BarRenderer.default(Some(red.copy(opacity = 0.5))))), Histogram(data2, barRenderer = Some(BarRenderer.default(Some(green.copy(opacity = 0.5))))) ) .standard() .render(plotAreaSize) Contour Plot import com.cibo.evilplot.numeric.Point import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.aesthetics.DefaultTheme._ import scala.util.Random val data = Seq.fill(100) { Point(Random.nextDouble() * 20, Random.nextDouble() * 20 } ContourPlot(data) .standard() .xbounds(0, 20) .ybounds(0, 20) .render()"
    } ,    
    {
      "title": "Plotting",
      "url": "/evilplot/plots.html",
      "content": "Plots In the last section, we made a simple scatter plot using a built-in plot method in EvilPlot. In this section, we’re going to explore some more built-in plots. ScatterPlot: The Whole Story If we take a look at the method signature for ScatterPlot.apply, we see it takes more than just data: object ScatterPlot { def apply[X &lt;: Datum2d[X]]( // Point extends Datum2d[Point] data: Seq[X], pointRenderer: Option[PointRenderer[X]] = None, xBoundBuffer: Option[Double] = None, yBoundBuffer: Option[Double] = None )(implicit theme: Theme): Plot = ??? } Before, we passed the defaults for all parameters in ScatterPlot and let our theme determine how things looked. But what if we want to color each point differently depending on the value of another variable? That’s where the pointRenderer argument comes in. A PointRenderer tells your plot how to draw the data. When we don’t pass one into ScatterPlot, we use PointRenderer.default(), which plots each item in the data sequence as a disc filled with the color in theme.colors.fill. But, there are a bunch more PointRenderers to choose from. import com.cibo.evilplot.numeric.Point import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.renderers.PointRenderer import com.cibo.evilplot.plot.aesthetics.DefaultTheme._ import scala.util.Random val qualities = Seq(\"good\", \"bad\") final case class MyFancyData(x: Double, y: Double, quality: String) val data: Seq[MyFancyData] = Seq.fill(100) { MyFancyData(Random.nextDouble(), Random.nextDouble(), qualities(Random.nextInt(2))) } val points = data.map(d =&gt; Point(d.x, d.y)) ScatterPlot( points, pointRenderer = Some(PointRenderer.colorByCategory(data.map(_.quality))) ).xAxis() .yAxis() .frame() .rightLegend() .render() The colorByCategory point renderer handled our categories for us. You’ll notice we included a call to .rightLegend() as well. The renderer is able to add our categories and their associated aesthetics to a context for a legend automatically, so we get a legend that makes sense. Overlaying EvilPlot takes an unusual approach to overlaying, in the spirit of building more complex visualizations out of simpler ones. When you want to make a multilayered plot, just make a plot for each layer. We then give you a combinator, Overlay, to compose all the plots into a meaningful composite. Let’s take a look at some real world data. In its natural state, real world data can be hard to interpret and even harder to put to use. We will use Wicked Free WiFi, a public data set containing the latitude and longitude (columns X and Y respectively) coordinates of several free WiFi hotspots in Boston. After downloading the CSV file, let’s parse the data into a Seq[Points]: import com.cibo.evilplot.numeric.Point import scala.collection.mutable.ArrayBuffer import scala.io.Source val path: String = //Insert your path to saved CSV file val data: Seq[Point] = { val bufferedSource = Source.fromFile(path + \"wifi.csv\") val points = bufferedSource.getLines.drop(1).map { line =&gt; val columns = line.split(\",\").map{_.trim()} //The coordinates are found in the first and second columns Point(columns.head.toDouble, columns(1).toDouble) }.toSeq points } Now that we have our data in a usable form, let’s make a contour plot to show the general density of free WiFi locations throughout Boston: import com.cibo.evilplot.colors._ import com.cibo.evilplot.plot._ def contourPlot(seq: Seq[Point]): Plot = { ContourPlot( seq, surfaceRenderer = Some(SurfaceRenderer.contours( color = Some(HTMLNamedColors.dodgerBlue)) )) } contourPlot(data) .xLabel(\"Lon\") .yLabel(\"Lat\") .xbounds(-71.2, -71) .ybounds(42.20, 42.4) .xAxis() .yAxis() .frame() .render() How can we make use of this plot? To start, we can add points for reference using EvilPlot’s Overlay. Let’s add a reference point at (-71.081754, 42.3670272), the approximate location of CiBO’s Cambridge Office. Overlay( contourPlot(data), ScatterPlot( Seq(Point(-71.081754,42.3670272)), pointRenderer = Some(PointRenderer.default(color = Some(HTMLNamedColors.crimson)))) ).xLabel(\"Lon\") .yLabel(\"Lat\") .xbounds(-71.2, -71) .ybounds(42.20, 42.4) .xAxis() .yAxis() .frame() .render() The Cambridge office location is just north of the central city of Boston, so this reference point serves as an easy check to make sure that we rendered the plot rendered correctly. With Overlay, we don’t have to worry that the bounds of our plots might not align–EvilPlot will take care of all of the necessary transformations for you. Adding side plots The contour plot gives us a good glimpse at the bivariate distribution. But, let’s say we were secondarily interested in looking at the univariate distribution of each geographic coordinate. Of course, we could just make histograms. val lonHistogram = Histogram(data.map(_.x)) val latHistogram = Histogram(data.map(_.y)) But we don’t have to stop at plotting these separately. EvilPlot will let us place plots in the margins of another plot using a whole family of border plot combinators. If we take what we’d been working on from before: contourPlot(data) .frame() .xLabel(\"Lon\") .yLabel(\"Lat\") .xbounds(-71.2, -71) .ybounds(42.20, 42.4) .xAxis() .yAxis() .topPlot(Histogram(data.map(_.x))) .rightPlot(Histogram(data.map(_.y))) .render() Faceting Plotting WiFi locations is helpful, but what if we have even more data that we want to compare and plot using the same axis? EvilPlot’s faceting lets us compare several plots at the same time. Let’s grab data from Snow Parking, Water Playgrounds, and Polling Locations and compare their respective contour plots. For the below example, load each of the CSVs into their own Seq[Point] and add each of them into a Seq[Seq[Point]] called allData in order, so that WiFi is first, then snow parking, then water playgrounds, then polling locations. Facets( Seq(allData.take(2).map(ps =&gt; contourPlot(ps) .topPlot(Histogram(ps.map(_.x))) .rightPlot(Histogram(ps.map(_.y))) .frame()), allData.takeRight(2).map(ps =&gt; contourPlot(ps) .topPlot(Histogram(ps.map(_.x))) .rightPlot(Histogram(ps.map(_.y))) .frame())) ) .topLabels(Seq(\"Family Outing\", \"Snowy Election\")) .xbounds(-71.2, -71) .ybounds(42.1, 42.4) .xAxis(tickCount = Some(4)) .yAxis(tickCount = Some(4)) .render() Hopefully at this point we can start to draw some useful conclusions from our data. Interested in a low-key family outing? Your best bet is to search in an area with a high density of both free WiFi and water playgrounds! Did the Boston winter hit a little sooner than expected? No problem. Just look for a polling center near a highly dense emergency snow parking area. These examples are a little contrived, but now you know how to start with a simple visual and compose more and more complexity on top of it using plot combinators. We first saw that the base plot can be customized using PlotRenderers. After that we saw how simple Plot =&gt; Plot combinators can add additional features piece-by-piece. Next, we saw that “multilayer plots” are expressed in EvilPlot simply by applying Overlay, a function from Seq[Plot] =&gt; Plot. Finally, we saw how we can build a multivariate, faceted display out of other plots easily, regardless of how complicated the individual plots are. But, we’re just getting started. Check out the Plot Catalog for awesome examples of what you can do with EvilPlot. Or, read about the drawing API for some background before venturing into writing your own renderers, which is an easy way to make EvilPlot even more customizable."
    } ,    
    {
      "title": "Render Contexts",
      "url": "/evilplot/render-context.html",
      "content": "RenderContext A RenderContext is the final target for all drawing operations in EvilPlot–it’s where a constructed Drawable goes to ultimately be put on a screen. EvilPlot provides a RenderContext for each supported platform. CanvasRenderContext CanvasRenderContext is for rendering to an HTML5 Canvas element and is available from within ScalaJS. To use it, you must obtain a CanvasRenderingContext2D from a canvas element in your page. Here’s the example plot from the Getting Started page using CanvasRenderContext. import com.cibo.evilplot.geometry.{CanvasRenderContext, Extent} import com.cibo.evilplot.plot._ import com.cibo.evilplot.plot.aesthetics.DefaultTheme._ import com.cibo.evilplot.numeric.Point import org.scalajs.dom val canvas = dom.document.createElement(\"canvas\").asInstanceOf[dom.html.Canvas] canvas.width = 400 canvas.height = 400 dom.document.body.appendChild(canvas) val context = CanvasRenderContext(canvas.getContext(\"2d\").asInstanceOf[dom.CanvasRenderingContext2D]) val data = Seq.tabulate(100) { i =&gt; Point(i.toDouble, scala.util.Random.nextDouble()) } ScatterPlot(data).render(Extent(400, 400)).draw(context) Canvas only: The text metrics buffer EvilPlot’s canvas rendering backend requires a buffer for text measurements, which have to be made to construct Drawable objects if they contain Text. EvilPlot searches for a canvas element called measureBuffer, so you must have one in your page for it to work. There is no such requirement on the JVM. Graphics2DRenderContext EvilPlot also supports rendering to a Graphics2D in Java’s AWT. You can obtain a Graphics2DRenderContext by passing a Graphics2D to it. When you use the JVM version of EvilPlot, you can use the asBufferedImage method, which will handle the creation of a RenderContext and the rendering process: import com.cibo.evilplot._ import com.cibo.evilplot.geometry._ Rect(40, 40).asBufferedImage"
    } ,      
  ];

  idx = lunr(function () {
    this.ref("title");
    this.field("content");

    docs.forEach(function (doc) {
      this.add(doc);
    }, this);
  });

  docs.forEach(function (doc) {
    docMap.set(doc.title, doc.url);
  });
}

// The onkeypress handler for search functionality
function searchOnKeyDown(e) {
  const keyCode = e.keyCode;
  const parent = e.target.parentElement;
  const isSearchBar = e.target.id === "search-bar";
  const isSearchResult = parent ? parent.id.startsWith("result-") : false;
  const isSearchBarOrResult = isSearchBar || isSearchResult;

  if (keyCode === 40 && isSearchBarOrResult) {
    // On 'down', try to navigate down the search results
    e.preventDefault();
    e.stopPropagation();
    selectDown(e);
  } else if (keyCode === 38 && isSearchBarOrResult) {
    // On 'up', try to navigate up the search results
    e.preventDefault();
    e.stopPropagation();
    selectUp(e);
  } else if (keyCode === 27 && isSearchBarOrResult) {
    // On 'ESC', close the search dropdown
    e.preventDefault();
    e.stopPropagation();
    closeDropdownSearch(e);
  }
}

// Search is only done on key-up so that the search terms are properly propagated
function searchOnKeyUp(e) {
  // Filter out up, down, esc keys
  const keyCode = e.keyCode;
  const cannotBe = [40, 38, 27];
  const isSearchBar = e.target.id === "search-bar";
  const keyIsNotWrong = !cannotBe.includes(keyCode);
  if (isSearchBar && keyIsNotWrong) {
    // Try to run a search
    runSearch(e);
  }
}

// Move the cursor up the search list
function selectUp(e) {
  if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index) && (index > 0)) {
      const nextIndexStr = "result-" + (index - 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Move the cursor down the search list
function selectDown(e) {
  if (e.target.id === "search-bar") {
    const firstResult = document.querySelector("li[id$='result-0']");
    if (firstResult) {
      firstResult.firstChild.focus();
    }
  } else if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index)) {
      const nextIndexStr = "result-" + (index + 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Search for whatever the user has typed so far
function runSearch(e) {
  if (e.target.value === "") {
    // On empty string, remove all search results
    // Otherwise this may show all results as everything is a "match"
    applySearchResults([]);
  } else {
    const tokens = e.target.value.split(" ");
    const moddedTokens = tokens.map(function (token) {
      // "*" + token + "*"
      return token;
    })
    const searchTerm = moddedTokens.join(" ");
    const searchResults = idx.search(searchTerm);
    const mapResults = searchResults.map(function (result) {
      const resultUrl = docMap.get(result.ref);
      return { name: result.ref, url: resultUrl };
    })

    applySearchResults(mapResults);
  }

}

// After a search, modify the search dropdown to contain the search results
function applySearchResults(results) {
  const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
  if (dropdown) {
    //Remove each child
    while (dropdown.firstChild) {
      dropdown.removeChild(dropdown.firstChild);
    }

    //Add each result as an element in the list
    results.forEach(function (result, i) {
      const elem = document.createElement("li");
      elem.setAttribute("class", "dropdown-item");
      elem.setAttribute("id", "result-" + i);

      const elemLink = document.createElement("a");
      elemLink.setAttribute("title", result.name);
      elemLink.setAttribute("href", result.url);
      elemLink.setAttribute("class", "dropdown-item-link");

      const elemLinkText = document.createElement("span");
      elemLinkText.setAttribute("class", "dropdown-item-link-text");
      elemLinkText.innerHTML = result.name;

      elemLink.appendChild(elemLinkText);
      elem.appendChild(elemLink);
      dropdown.appendChild(elem);
    });
  }
}

// Close the dropdown if the user clicks (only) outside of it
function closeDropdownSearch(e) {
  // Check if where we're clicking is the search dropdown
  if (e.target.id !== "search-bar") {
    const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
    if (dropdown) {
      dropdown.classList.remove("show");
      document.documentElement.removeEventListener("click", closeDropdownSearch);
    }
  }
}
