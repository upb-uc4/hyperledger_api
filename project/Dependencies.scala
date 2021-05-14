import sbt._

object Dependencies {
  // libraries
  private val hyperledgerSDK = "org.hyperledger.fabric-sdk-java" % "fabric-sdk-java" % "2.2.5"
  private val hyperledgerGateway = "org.hyperledger.fabric" % "fabric-gateway-java" % "2.2.1"
  private val scalaTest = "org.scalatest" %% "scalatest" % "3.2.9" % Test
  private val flexmark = "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test
  private val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.30"
  private val slf4jLog = "ch.qos.logback" % "logback-core" % "1.2.3"
  private val slf4jClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  // dependency groups
  val hyperledgerDependencies = Seq(hyperledgerSDK, hyperledgerGateway)
  val scalaTestDependencies = Seq(scalaTest, flexmark)
  val slf4j = Seq(slf4jApi, slf4jLog, slf4jClassic)
}
