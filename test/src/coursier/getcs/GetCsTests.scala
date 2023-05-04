//> using lib "org.scalameta::munit:0.7.29"

package coursier.getcs

object GetCsTests {

  case class Entry(
    arch: String,
    isWin: Boolean,
    isMac: Boolean,
    isLinux: Boolean
  ) {
    def version =
      if (arch == "aarch64") GetCs.defaultArmVersion
      else GetCs.defaultVersion
    def os =
      if (isWin) "Windows"
      else if (isMac) "macOS"
      else if (isLinux) "Linux"
      else "other"
    def name = s"$arch $os"
    def url  = GetCs.url(arch, version, isWin, isMac, isLinux)
  }

  def entries: Seq[Entry] =
    for {
      arch <- Seq("aarch64", "x86_64")
      (isWin, isMac, isLinux) <- Seq(
        (true, false, false),
        (false, true, false),
        (false, false, true)
      )
      if arch != "aarch64" || !isWin
    } yield Entry(arch, isWin, isMac, isLinux)

}

class GetCsTests extends munit.FunSuite {

  import GetCsTests._

  for (entry <- entries)
    test(s"${entry.name} cs check") {
      val url = entry.url.getOrElse {
        sys.error("no URL")
      }
      val cache = coursier.cache.FileCache()
      cache.file(coursier.util.Artifact(url)).run.unsafeRun()(cache.ec) match {
        case Left(err) => throw new Exception(err)
        case Right(_)  =>
      }
    }

  for (entry <- entries)
    test(s"${entry.name} scala-cli check") {
      val url = entry.url.getOrElse {
        sys.error("no URL")
      }
      val cache = coursier.cache.FileCache()
      cache.file(coursier.util.Artifact(url)).run.unsafeRun()(cache.ec) match {
        case Left(err) => throw new Exception(err)
        case Right(_)  =>
      }
    }

}
