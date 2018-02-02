Evil Plot is about combinators for graphics.

One-time setup:
Install PhantomJS: `brew install phantomjs`

To run Evil Plot:

1. open sbt console
2. compile, then run fastOptJS listening for changes
```bash
$ sbt
> compile
> ~ fastOptJS
```
3. go to `localhost:12345/index.html` in your browser

Running unit tests:
Requires PhantomJS, and unit tests must be run from `sbt`, not from within IntelliJ.

