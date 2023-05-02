# get-cs

A library to fetch a [coursier](https://github.com/coursier/coursier) from Scala code

This library fetches a native coursier launcher, depending on your OS (Linux / macOS /
Windows) and architecture (x86-64 / ARM64), and falls back to any launcher
available in the `PATH` in unsupported OSes or architectures.

Use like
```scala
//> using lib "io.get-coursier.util:get-cs::0.1.0"
import coursier.getcs.GetCs

val csCommand = GetCs.cs() // optionally pass a coursier version to hard-code the version you're using

new ProcessBuilder(csCommand, "fetch", "org:name:ver")
  .start()
```
