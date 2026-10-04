package com.github.diogocerqueiralima.identity.service.infrastructure.adapters

import com.github.diogocerqueiralima.error.common.exceptions.BadRequestException
import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSigningRequest
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSubject
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.CertificateSigningRequestReader
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.operator.OperatorCreationException
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.PKCSException
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.StringReader
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException

private val log = LoggerFactory.getLogger(CertificateSigningRequestReaderImpl::class.java)

@Component
class CertificateSigningRequestReaderImpl : CertificateSigningRequestReader {

    override fun read(data: String): CertificateSigningRequest {

        // 1. Parse the PKCS#10 request (PEM format)
        try {

            PEMParser(StringReader(data)).use { pemParser ->

                val pkcs10Request = pemParser.readObject() as? PKCS10CertificationRequest
                    ?: run {
                        log.error("Invalid PKCS#10 certification request")
                        throw BadRequestException("The certificate signing request is invalid.")
                    }

                // 2. Verify the signature of the PKCS#10 request
                if (!verifySignature(pkcs10Request)) {
                    log.error("Invalid signature in the certificate signing request")
                    throw BadRequestException("The signature is invalid.")
                }

                // 3. Extract subject information and other relevant items from the PKCS#10 request
                val subject = pkcs10Request.subject
                val certificateSubject = CertificateSubject(
                    getRdnValue(subject, BCStyle.CN),
                    getRdnValue(subject, BCStyle.O),
                    getRdnValue(subject, BCStyle.C)
                )

                // 4. Extract the public key from the PKCS#10 request
                val publicKey = BouncyCastleProvider.getPublicKey(pkcs10Request.subjectPublicKeyInfo)

                return CertificateSigningRequest(certificateSubject, publicKey)
            }

        } catch (e: IOException) {
            throw OperationFailedException("Could not read the Certificate Signing Request (CSR).")
        }

    }

    /**
     * Reads the first value of the given attribute from the subject.
     *
     * @param subject the subject to read from
     * @param type the attribute to look up
     * @return the attribute value
     * @throws BadRequestException if the subject does not carry the attribute
     */
    private fun getRdnValue(subject: X500Name, type: ASN1ObjectIdentifier): String {

        val rdn = subject.getRDNs(type).firstOrNull()
            ?: throw BadRequestException("The certificate signing request subject must contain CN, O and C.")

        return rdn.first.value.toString()
    }

    /**
     * Verifies the signature of the given PKCS#10 certification request.
     *
     * @param pkcs10Request the PKCS#10 certification request to verify
     * @return true if the signature is valid, false otherwise
     */
    private fun verifySignature(pkcs10Request: PKCS10CertificationRequest): Boolean {

        try {

            val jcaCR = JcaPKCS10CertificationRequest(pkcs10Request)

            return jcaCR.isSignatureValid(
                JcaContentVerifierProviderBuilder()
                    .setProvider("BC")
                    .build(jcaCR.publicKey)
            )

        } catch (e: Exception) {

            when (e) {
                is NoSuchAlgorithmException, is InvalidKeyException, is OperatorCreationException, is PKCSException -> {
                    log.error("Error verifying PKCS#10 signature", e)
                    throw OperationFailedException("The signature could not be verified.")
                }
                else -> throw e
            }

        }

    }

}
