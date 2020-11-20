package de.upb.cs.uc4.hyperledger.tests.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionExaminationRegulationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import org.scalatest.matchers.should.Matchers._

object TestHelper {

  /// EXAMINATION REGULATIONS
  def testAddExaminationRegulationAccess(connection: ConnectionExaminationRegulationTrait, name: String, modules: Array[String], state: Boolean): Unit = {
    val testObject = TestDataExaminationRegulation.validExaminationRegulation(name, modules, state);
    val testResult = connection.addExaminationRegulation(testObject)

    compareExaminationRegulations(testObject, testResult)
  }
  def compareExaminationRegulations(testObject: String, testResult: String): Unit = {
    compareJson(testObject, testResult)
  }

  /// CERTIFICATES
  def testAddCertificateAccess(testId: String, certificateConnection: ConnectionCertificateTrait): Unit = {
    val testCert = "whatever"
    certificateConnection.addCertificate(testId, testCert)
    val result: String = certificateConnection.getCertificate(testId)
    result should be(testCert)
  }
  def compareCertificates(expected: String, actual: String): Unit = {
    val cleanExpected = expected
    val cleanActual = actual
    cleanActual should be(cleanExpected)
  }

  /// GENERAL
  def compareJson(expected: String, actual: String): Unit = {
    val cleanExpected = cleanJson(expected)
    val cleanActual = cleanJson(actual)
    cleanActual should be(cleanExpected)
  }
  def cleanJson(input: String): String = {
    input
      .replace("\n", "")
      .replace(" ", "")
  }
  def getJsonList(items: Seq[String]): String = {
    "[" + items.tail.fold(items.head)((A, B) => A + "," + B) + "]"
  }

  // Exception
  def testTransactionException(transactionName: String, f: () => Any) = {
    val result = intercept[TransactionExceptionTrait](f.apply())
    result.transactionName should be(transactionName)
  }

  def nullableSeqToString(input: Seq[String]): String = {
    if(input == null) "null"
    else input.mkString(", ")
  }
}
