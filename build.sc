// originally based on https://github.com/alexarchambault/mill-native-image/blob/dca83cef17f1a90360b62610e1dcce57271ef37d/build.sc

import $ivy.`io.chris-kipp::mill-ci-release::0.1.6`
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`

import io.kipp.mill.ci.release.{CiReleaseModule, SonatypeHost}
import mill._, scalalib._, publish._
import mill.scalalib.api.Util.scalaNativeBinaryVersion

// No 0.9.x support, as the coursier version it depends on has no ArchiveCache support
val millVersions       = Seq("0.10.12", "0.11.0-M8")
val millBinaryVersions = millVersions.map(millBinaryVersion)

def millBinaryVersion(millVersion: String) = scalaNativeBinaryVersion(millVersion)
def millVersion(binaryVersion: String) =
  millVersions.find(v => millBinaryVersion(v) == binaryVersion).get

trait MillCsPublishModule extends CiReleaseModule {
  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "io.get-coursier.mill",
    url = s"https://github.com/coursier/mill-cs",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("coursier", "mill-cs"),
    developers = Seq(
      Developer("alexarchambault", "Alex Archambault", "https://github.com/alexarchambault")
    )
  )
  override def sonatypeHost = Some(SonatypeHost.s01)
}

object Scala {
  def version = "2.13.10"
}

object plugin extends Cross[PluginModule](millBinaryVersions: _*)
class PluginModule(millBinaryVersion: String)
    extends ScalaModule
    with MillCsPublishModule {
  def artifactName   = s"mill-cs_mill$millBinaryVersion"
  def millSourcePath = super.millSourcePath / os.up
  def scalaVersion   = Scala.version
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.lihaoyi::mill-scalalib:${millVersion(millBinaryVersion)}"
  )

  object test extends Tests {
    def ivyDeps = super.ivyDeps() ++ Seq(
      ivy"org.scalameta::munit:0.7.29"
    )
    def testFramework = "munit.Framework"
  }
}
