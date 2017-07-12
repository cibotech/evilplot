Evil Plot is about combinators for graphics.

One-time setup:

Run `npm install` in the top-level project directory

To run Evil Plot:

1. open sbt console
2. run fastOptJS listening for changes
```bash
$ sbt
> ~ fastOptJS
```
3. go to `localhost:12345/index.html` in your browser

sbt note:
Per the [ScalaTest installation instructions](http://www.scalatest.org/install), add this line to ~/.sbt/0.13/global.sbt:
```resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"```
