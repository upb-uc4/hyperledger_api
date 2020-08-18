coverageEnabled in ThisBuild := true

lazy val hyperledger_api = (project in file("."))
  .settings(
    Commons.commonSettings("hyperledger_api"),
    name := "hyperledger_api",
    libraryDependencies ++= Dependencies.scalaTestDependencies,
    libraryDependencies ++= Dependencies.hyperledgerDependencies,
  )