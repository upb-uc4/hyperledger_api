package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.TestHelper
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager, WalletManager }
import org.hyperledger.fabric_ca.sdk.HFCAClient

import scala.io.Source

class UserManagementTests extends TestBase {

  "The enrollmentManager" when {
    "enrolling a User without csr" should {
      "allow for the new User to access the chain [300]" in {
        val enrollmentID = "300"
        Logger.info("Enroll Admin as user.")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        val connection = super.initializeCertificate(username)
        TestHelper.testAddCertificateAccess(enrollmentID, connection)
      }
    }
    "enrolling a User with csr" should {
      "not directly allow for the new User to access the chain [testid]" in {
        Logger.info("EnrollAdmin")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Register TestUser")
        val testUserName = "testid"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)

        Logger.debug("get csr_pem")
        val resource = getClass.getResource("/testid.csr")
        Logger.debug(s"file: ${resource.getFile}")
        val source = Source.fromURL(resource)
        var content: String = null
        try {
          content = source.mkString
        }
        finally {
          source.close()
        }
        Logger.debug(s"content: $content")

        val signedCert: String = EnrollmentManager.enrollSecure(caURL, tlsCert, testUserName, testUserPw, content, adminName = username, adminWalletPath = walletPath, channel, chaincode, networkDescriptionPath)
        Logger.info("Finished enrolling new user")

        signedCert should not be null
      }
    }
  }

  "The registrationManager" when {
    "performing a registration [302]" should {
      "not throw exceptions" in {
        val enrollmentID = "302"
        Logger.info("Enroll as admin and store cert to wallet")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Register TestUser")
        val testUserName = s"Tester$enrollmentID"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)

        Logger.info("Enroll TestUser")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, testUserName, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Access Chain as TestUser")
        val connection = super.initializeCertificate(testUserName)
        TestHelper.testAddCertificateAccess(enrollmentID, connection)
      }
    }
  }
}
