import sbt.Keys.{ testOptions, _ }
import sbt.{ Developer, TestFrameworks, Tests, url }
import sbtbuildinfo.BuildInfoPlugin.autoImport.{ BuildInfoKey, buildInfoKeys, buildInfoPackage }
import scoverage.ScoverageKeys.{ coverageFailOnMinimum, coverageMinimum }

// format: OFF    <-- this directive disables formatting from this point
object Commons {

  def projectSettings(project: String) = Seq(
    // scala version
    scalaVersion := "2.13.6",
    // append -deprecation to the options passed to the Scala compiler
    scalacOptions += "-deprecation",
    // append -feature to the options passed to the Scala compiler
    scalacOptions += "-feature",
    // testOption for test-reports
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test_reports/" + project),
    //coverage threshold
    coverageMinimum := sys.env.getOrElse("COVERAGE_MINIMUM", "100").toDouble,
    coverageFailOnMinimum := true,
    // build info
    buildInfoKeys := Seq[BuildInfoKey](organization, name, version, scalaVersion, sbtVersion),
    buildInfoPackage := s"${organization.value}.hyperledger"
  )

  def projectInfo() = Seq(
    // ----------------------------------
    // Info for Maven Publishing - set by plugins
    // version := "v0.10.0",
    // isSnapshot := version.value endsWith "SNAPSHOT",
    // scmInfo := Some(ScmInfo(url("https://github.com/upb-uc4/hlf-api"), "scm:git@github.com:upb-uc4/hlf-api.git")),
    // publishMavenStyle := true,
    // publishArtifact in Test := false,
    // ----------------------------------
    // Sonatype - set by plugin from env-vars
    // credentials += Credentials(
    // "Sonatype Nexus Repository Manager",
    // "oss.sonatype.org",
    // sys.env.getOrElse("SONATYPE_USER", ""),
    // sys.env.getOrElse("SONATYPE_PASS", "")
    // )
    // ----------------------------------
    organization := "de.upb.cs.uc4",
    organizationName := "uc4",
    homepage := Some(url("https://uc4.cs.upb.de/")),
    developers := List(Developer("UC4", "UC4", "UC4_official@web.de", url("https://github.com/upb-uc4"))),
    licenses := List("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    crossPaths := false,
    pomIncludeRepository := { _ => false }
  )

  /* ----------------------------------
  // GPG Settings - set by plugin from env-vars
  def gpgSettings() = Seq(
    useGpg := false,
    useGpgPinentry := true,
    pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray),
    usePgpKeyHex("02C7EAB5DE1AD596FE5CCB68DBBB1A432C70E654")
  )
  */// ----------------------------------
}
// format: ON    <-- this directive enables formatting from this point
