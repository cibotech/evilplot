This is the documentation site for EvilPlot.

To work on the docs:

1. Install Jekyll (e.g. `gem install jekyll -v 4`; version 4.3.1 does not work at this time)
2. Run `sbt docs/makeMicrosite` from the root of this repo.
3. `cd` to `docs/target/site` and run `jekyll serve --baseurl /evilplot`.
4. Visit the docs site at `localhost:4000/evilplot/` in your browser.
