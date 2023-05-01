# mill-cs

A Mill plugin to fetch a [coursier](https://github.com/coursier/coursier) from Mill

This plugin fetches native coursier launcher, depending on your OS (Linux / macOS /
Windows) and architecture (x86-64 / ARM64), and falls back to any launcher
available in the `PATH` in unsupported OSes or architectures.

Use like
```scala
import $ivy.`io.get-coursier.mill::mill-cs::0.1.0`
import coursier.mill.MillCs

object cs extends MillCs {
  // override these to hard-code the coursier version you'd like to use
  // def csVersion = MillCs.defaultVersion
  // def csArmVersion = MillCs.defaultArmVersion
}
```

You can then get the command to run coursier with `cs.cs()` (typed as a `String`).
