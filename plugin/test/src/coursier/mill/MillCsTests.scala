package coursier.mill

object MillCsTests {

  case class Entry(
    arch: String,
    isWin: Boolean,
    isMac: Boolean,
    isLinux: Boolean
  ) {
    def version =
      if (arch == "aarch64") MillCs.defaultArmVersion
      else MillCs.defaultVersion
    def os =
      if (isWin) "Windows"
      else if (isMac) "macOS"
      else if (isLinux) "Linux"
      else "other"
    def name = s"$arch $os"
    def url  = MillCs.url(arch, version, isWin, isMac, isLinux)
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

class MillCsTests extends munit.FunSuite {

  import MillCsTests._

  for (entry <- entries)
    test(s"${entry.name} check") {
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
