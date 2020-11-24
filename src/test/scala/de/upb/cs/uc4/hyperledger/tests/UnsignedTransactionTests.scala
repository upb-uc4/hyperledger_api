package de.upb.cs.uc4.hyperledger.tests

import java.io.File
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import java.util.Base64

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionCertificateTrait, ConnectionMatriculationTrait}
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.exceptions.traits.{HyperledgerExceptionTrait, TransactionExceptionTrait}
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.TestDataMatriculation
import de.upb.cs.uc4.hyperledger.utilities.{EnrollmentManager, RegistrationManager, WalletManager}
import de.upb.cs.uc4.hyperledger.utilities.helper.{Logger, ReflectionHelper, TransactionHelper}
import org.hyperledger.fabric.gateway.impl.TransactionImpl
import org.hyperledger.fabric.gateway.impl.identity.{GatewayUser, X509IdentityImpl}
import org.hyperledger.fabric.gateway.{Identities, Identity, Wallet, X509Identity}
import org.hyperledger.fabric.protos.peer.Chaincode
import org.hyperledger.fabric.protos.peer.ProposalPackage.{Proposal, SignedProposal}
import org.hyperledger.fabric.sdk.identity.X509Enrollment
import org.hyperledger.fabric.sdk.{ChaincodeID, Channel, HFClient, NetworkConfig, TransactionProposalRequest, User}
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.hyperledger.fabric.sdk.transaction.{ProposalBuilder, TransactionContext}

import scala.io.Source

class UnsignedTransactionTests extends TestBase {

  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    certificateConnection = initializeCertificate()
    matriculationConnection = initializeMatriculation()
  }

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    super.afterAll()
  }

  "The ConnectionCertificate" when {
    "querying for an unsigned transaction" should {
      "return an unsigned transaction" in {
        val enrollmentId = "100"
        val certificate = "Whatever"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
      }
    }

    "passing a signed transaction" should {
      "submit the proposal transaction to the proposal contract, even if the signature was not created using the private key belonging to the connection" in {
        val argEnrollmentId = "113"
        val argCertificate = "Whatever"
        val testAffiliation = "org1MSP"

        val wallet: Wallet = WalletManager.getWallet(this.walletPath)

        super.tryEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)
        // try register and enroll test user 102
        try {
          val testUserPw = RegistrationManager.register(caURL, tlsCert, argEnrollmentId, username, walletPath, testAffiliation)
          EnrollmentManager.enroll(caURL, tlsCert, walletPath, argEnrollmentId, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)
        } catch {
          case _: Throwable =>
        }

        // initialize crypto primitives
        val crypto: CryptoPrimitives = new CryptoPrimitives()
        val securityLevel: Integer = 256
        ReflectionHelper.safeCallPrivateMethod(crypto)("setSecurityLevel")(securityLevel)
        // TODO use get- and set properties
        //val mspId: String = ""
        //val certificatePem: String = ""

        // get testUser certificate and private key
        val testUserIdentity: X509IdentityImpl = wallet.get(argEnrollmentId).asInstanceOf[X509IdentityImpl]
        // val privateKeyPem: String = ""
        //val certificatePem: String = ""
        // val privateKey: PrivateKey = Identities.readPrivateKey(privateKeyPem)
        // val certificate: X509Certificate = Identities.readX509Certificate(certificatePem)
        //val identity: X509Identity = Identities.newX509Identity(mspId, certificate, privateKey)

        val privateKey: PrivateKey = testUserIdentity.getPrivateKey()
        val certificate: X509Certificate = testUserIdentity.getCertificate()
        // TODO proper values here?

        // mock certificate (replace admin mspId by testUser mspId)
        val adminIdentity: X509IdentityImpl = wallet.get(this.username).asInstanceOf[X509IdentityImpl]
        // val originalCertificate: X509Certificate = adminIdentity.getCertificate()
        // ReflectionHelper.setPrivateField(adminIdentity)("certificate")(certificate)
        // val originalMspId: String = adminIdentity.getMspId()
        // ReflectionHelper.setPrivateField(adminIdentity)("mspId")(testUserIdentity.getMspId())
        // wallet.remove(this.username)
        // wallet.put(this.username, adminIdentity)

        // get proposal
        //val proposalBytes = certificateConnection.getProposalAddCertificate(argEnrollmentId, argCertificate)
        val enrollment: X509Enrollment = new X509Enrollment(new PrivateKey {
          override def getAlgorithm: String = null
          override def getFormat: String = null
          override def getEncoded: Array[Byte] = null
        }, Identities.toPemString(certificate))
        val user: User = new GatewayUser(argEnrollmentId, testAffiliation, enrollment);
        // val user: User = new GatewayUser(argEnrollmentId, testAffiliation, new X509Enrollment(adminIdentity.getPrivateKey, Identities.toPemString(adminIdentity.getCertificate)))
        val request = TransactionProposalRequest.newInstance(user)
        request.setChaincodeName(this.chaincode)
        request.setFcn("UC4.Approval:approveTransaction")
        request.setArgs("UC4.Certificate" ,"addCertificate", "[\"" + argEnrollmentId + "\",\"" + argCertificate + "\"]")
        val networkConfigFile: File = networkDescriptionPath.toFile()
        val networkConfig: NetworkConfig = NetworkConfig.fromYamlFile(networkConfigFile)
        val hfClient: HFClient = HFClient.createNewInstance()
        hfClient.setCryptoSuite(crypto)
        hfClient.setUserContext(user)
        val channelObj: Channel = hfClient.loadChannelFromConfig(channel, networkConfig)
        val ctx: TransactionContext = new TransactionContext(channelObj, user, crypto)
        val chaincodeId: Chaincode.ChaincodeID = Chaincode.ChaincodeID.newBuilder().setName(this.chaincode).build()
        val proposal = ProposalBuilder.newBuilder().context(ctx).request(request).chaincodeID(chaincodeId).build()
        //val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nPROPOSALBYTES:\n##########################\n\n" + proposal.toByteString.toStringUtf8)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)

        val proposalBytes: Array[Byte] = proposal.toByteArray
        // sign proposal with testUser privateKey
        val signatureBytes = crypto.sign(privateKey, proposalBytes)
        // val signatureBytes = crypto.sign(adminIdentity.getPrivateKey, proposalBytes)

        val b64Sig = ByteString.copyFrom(Base64.getEncoder.encode(signatureBytes)).toStringUtf8
        println("\n\n\n##########################\nSignature:\n##########################\n\n" + b64Sig)

        // submit only signed transaction to approval contract
        val signature: ByteString = ByteString.copyFrom(signatureBytes)
        // create signedProposal Object and get Info Objects
        val (transaction: TransactionImpl, context: TransactionContext, signedProposal: SignedProposal) =
          TransactionHelper.createSignedProposal(certificateConnection.approvalConnection.get, proposal, signature)
        // submit approval

        val approvalResult = certificateConnection.internalSubmitApprovalProposal(transaction, context, signedProposal)
        // reset certificate of admin user
        // ReflectionHelper.setPrivateField(adminIdentity)("certificate")(originalCertificate)
        // ReflectionHelper.setPrivateField(adminIdentity)("mspId")(originalMspId)
        // wallet.remove(this.username)
        // wallet.put(this.username, adminIdentity)

        println("\n\n\n##########################\nResult:\n##########################\n\n" + approvalResult)
      }
      "submit the proposal transaction to the proposal contract" in {
        val enrollmentId = "102"
        val certificate = "Whatever"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nPROPOSALBYTES:\n##########################\n\n" + proposal.toByteString.toStringUtf8)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val transactionContext: TransactionContext = certificateConnection.contract.getNetwork.getChannel.newTransactionContext()
        val signature = transactionContext.signByteString(proposalBytes)
        val b64Sig = ByteString.copyFrom(Base64.getEncoder.encode(signature.toByteArray)).toStringUtf8
        println("\n\n\n##########################\nSignature:\n##########################\n\n" + b64Sig)
        val result = certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray)
        println("\n\n\n##########################\nResult:\n##########################\n\n" + result)
      }
      "submit the real transaction to the real contract" in {
        // store info
        val enrollmentId = "103"
        val certificate = "Whatever"
        println("\n\n\n##########################\nGET PROPOSAL:\n##########################\n\n")
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val transactionContext: TransactionContext = certificateConnection.contract.getNetwork.getChannel.newTransactionContext()
        val signature = transactionContext.signByteString(proposalBytes)
        println("\n\n\n##########################\nSUBMIT PROPOSAL:\n##########################\n\n")
        val result = certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray)
        println("\n\n\n##########################\nResult103:\n##########################\n\n" + result)

        // test info stored
        val storedCert = certificateConnection.getCertificate(enrollmentId)
        storedCert should be(certificate)
      }
    }

    "passing a wrongly-signed transaction" should {
      "deny the transaction on the ledger" in {
        val approvalTransactionName = "approveTransaction"
        val enrollmentId = "101"
        val wrongCertificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, wrongCertificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val signature = ByteString.copyFrom(Base64.getDecoder.decode("MEUCIQD92OsJsVVFqFfifMV14ROiL5Ni/RaOBkR0DqzetvPfkQIgcrgu9vxr5TuZY6lft5adCETaC3CSE8QA+bs9MheeLcI="))
        val result = intercept[HyperledgerExceptionTrait](certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray))
        result.actionName should be(approvalTransactionName)
      }
    }

    "testing info" should {
      "not fail 1" in {
        val inputMatJSon = TestDataMatriculation.validMatriculationData3("500")
        val proposalBytes = matriculationConnection.getProposalAddMatriculationData(inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataProposal:: $info")
      }
      "not fail 2" in {
        val inputMatJSon = TestDataMatriculation.validMatriculationData4("500")
        val proposalBytes = matriculationConnection.getProposalUpdateMatriculationData(inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataProposal:: $info")
      }
      "not fail 3" in {
        val proposalBytes = matriculationConnection.getProposalAddEntriesToMatriculationData(
          "500",
          TestDataMatriculation.validMatriculationEntry
        )
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataProposal:: $info")
      }
    }
  }
}
