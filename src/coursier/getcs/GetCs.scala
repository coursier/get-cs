//> using scala "2.12.17", "2.13.10"
//> using lib "io.get-coursier::coursier:2.1.2"

package coursier.getcs

import coursier.core.Version

import java.io.File
import java.util.Locale

import scala.util.Properties

object GetCs {

  /** Provides the command to run "cs". Can be either a path, or a command name.
    */
  def cs(csVersion: String = defaultVersion, csArmVersion: String = null): String = {

    val arch = sys.props.getOrElse("os.arch", "").toLowerCase(Locale.ROOT)
    val ext  = if (Properties.isWin) ".exe" else ""
    val csVersion0 =
      if (arch == "aarch64" && Properties.isMac && csArmVersion != null) csArmVersion else csVersion

    val downloadOpt: Option[String] =
      url(arch, csVersion0, Properties.isWin, Properties.isMac, Properties.isLinux).map {
        url0 =>
          val cache        = coursier.cache.FileCache()
          val archiveCache = coursier.cache.ArchiveCache().withCache(cache)
          val task         = cache.logger.using(archiveCache.get(coursier.util.Artifact(url0)))
          val maybeFile =
            try task.unsafeRun()(cache.ec)
            catch {
              case t: Throwable =>
                throw new Exception(s"Error getting and extracting $url0", t)
            }
          val f = maybeFile.fold(ex => throw new Exception(ex), identity)
          val exec =
            if (Properties.isWin && f.isDirectory() && f.getName.endsWith(".zip"))
              f.listFiles.find(_.getName.endsWith(".exe")).getOrElse(
                sys.error(s"No .exe found under $f")
              )
            else
              f

          if (!Properties.isWin)
            exec.setExecutable(true)

          exec.toString
      }

    downloadOpt.getOrElse(fromPath("cs"))
  }

  def fromPath(name: String): String =
    if (Properties.isWin) {
      val pathExt = Option(System.getenv("PATHEXT"))
        .toSeq
        .flatMap(_.split(File.pathSeparator).toSeq)
      val path = Seq(new File("").getAbsoluteFile) ++
        Option(System.getenv("PATH"))
          .toSeq
          .flatMap(_.split(File.pathSeparator))
          .map(new File(_))

      def candidates =
        for {
          dir <- path.iterator
          ext <- pathExt.iterator
        } yield new File(dir, name + ext)

      candidates
        .filter(_.canExecute)
        .toStream
        .headOption
        .map(_.getAbsolutePath)
        .getOrElse {
          System.err.println(s"Warning: could not find $name in PATH.")
          name
        }
    }
    else
      name

  def defaultVersion: String    = "2.1.2"
  def defaultArmVersion: String = defaultVersion

  def url(
    arch: String,
    version: String,
    isWin: Boolean,
    isMac: Boolean,
    isLinux: Boolean
  ): Option[String] =
    arch match {
      case "x86_64" | "amd64" =>
        if (isWin)
          Some(
            s"https://github.com/coursier/coursier/releases/download/v$version/cs-x86_64-pc-win32.zip"
          )
        else if (isMac)
          Some(
            s"https://github.com/coursier/coursier/releases/download/v$version/cs-x86_64-apple-darwin.gz"
          )
        else if (isLinux)
          Some(
            s"https://github.com/coursier/coursier/releases/download/v$version/cs-x86_64-pc-linux.gz"
          )
        else None
      case "aarch64" =>
        if (isLinux) {
          val useVirtusLabRepo = Version(version).compare(Version("2.1.0-RC5")) >= 0
          val url0 =
            if (useVirtusLabRepo)
              s"https://github.com/VirtusLab/coursier-m1/releases/download/v$version/cs-aarch64-pc-linux.gz"
            else
              s"https://github.com/coursier/coursier/releases/download/v$version/cs-aarch64-pc-linux.gz"
          Some(url0)
        }
        else if (isMac)
          Some(
            s"https://github.com/VirtusLab/coursier-m1/releases/download/v$version/cs-aarch64-apple-darwin.gz"
          )
        else None
      case _ =>
        None
    }
}
