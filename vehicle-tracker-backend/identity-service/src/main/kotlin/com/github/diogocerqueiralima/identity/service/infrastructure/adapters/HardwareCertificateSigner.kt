package com.github.diogocerqueiralima.identity.service.infrastructure.adapters

import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException
import com.github.diogocerqueiralima.identity.service.domain.model.certificate.CertificateSigningRequest
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.CertificateSigner
import com.github.diogocerqueiralima.identity.service.presentation.config.ApplicationURIs
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.CRLDistPoint
import org.bouncycastle.asn1.x509.DistributionPoint
import org.bouncycastle.asn1.x509.DistributionPointName
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.cert.CertIOException
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.OperatorCreationException
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.StringWriter
import java.math.BigInteger
import java.security.KeyPair
import java.time.Instant
import java.util.Date

private val log = LoggerFactory.getLogger(HardwareCertificateSigner::class.java)

@Component
class HardwareCertificateSigner(private val keyPair: KeyPair) : CertificateSigner {

    /**
     * {@inheritDoc}
     *
     * This method uses the CA's private key stored in a trusted module platform (TPM) to sign the certificate.
     */
    override fun sign(
        csr: CertificateSigningRequest, issuer: String, serialNumber: BigInteger,
        notBefore: Instant, notAfter: Instant
    ): String {

        try {

            // 1. Extract the public key and subject information from the certificate signing request
            val privateKey = keyPair.private
            val publicKey = keyPair.public
            val subject = csr.subject

            // 2. Build the X.509 certificate
            val certBuilder = JcaX509v3CertificateBuilder(
                X500Name(issuer),
                serialNumber,
                Date.from(notBefore),
                Date.from(notAfter),
                X500Name(subject.toString()),
                csr.publicKey
            )

            // 3. Add extensions to the certificate

            // 3.1 Basic Constraints: Not a CA

            certBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(false))
            certBuilder.addExtension(
                Extension.keyUsage, true, KeyUsage(KeyUsage.digitalSignature or KeyUsage.keyEncipherment)
            )

            // 3.2 Subject Alternative Name: Add the Common Name as an alternative name

            val names = arrayOf(GeneralName(GeneralName.rfc822Name, subject.commonName))
            certBuilder.addExtension(Extension.subjectAlternativeName, false, GeneralNames(names))

            // 3.3 Subject Key Identifier and Authority Key Identifier

            certBuilder.addExtension(
                Extension.subjectKeyIdentifier,
                false,
                SubjectKeyIdentifier(csr.publicKey.encoded)
            )

            certBuilder.addExtension(
                Extension.authorityKeyIdentifier, false, AuthorityKeyIdentifier(publicKey.encoded)
            )

            // 3.4 CRL Distribution Points: Add a placeholder CRL distribution point

            val distributionPoints = arrayOf(
                DistributionPoint(
                    DistributionPointName(
                        GeneralNames(
                            GeneralName(
                                GeneralName.uniformResourceIdentifier,
                                "https://api.mytracker.pt" + ApplicationURIs.CRL_DISTRIBUTION_POINT_URI
                            )
                        )
                    ),
                    null,
                    null
                )
            )

            certBuilder.addExtension(
                Extension.cRLDistributionPoints, false, CRLDistPoint(distributionPoints)
            )

            // 4. Sign the certificate using the CA's private key

            val signer = JcaContentSignerBuilder("SHA256withECDSA").build(privateKey)

            val holder = certBuilder.build(signer)

            // 5. Convert the certificate to PEM format and return the signed certificate

            val pem = convertToPem(holder)

            log.info("Certificate signed successfully for Subject={}", subject)
            return pem

        } catch (e: Exception) {

            when (e) {
                is CertIOException, is OperatorCreationException -> {
                    log.error("Error signing certificate", e)
                    throw OperationFailedException("Could not sign the certificate.")
                }
                else -> throw e
            }

        }

    }

    /**
     * Converts an [X509CertificateHolder] to PEM format.
     *
     * @param holder the [X509CertificateHolder] to convert
     * @return the PEM-encoded certificate
     */
    private fun convertToPem(holder: X509CertificateHolder): String {

        try {

            val sw = StringWriter()

            JcaPEMWriter(sw).use { it.writeObject(holder) }

            return sw.toString()

        } catch (e: IOException) {
            log.error("Error converting certificate to PEM format", e)
            throw OperationFailedException("It was not possible to convert the certificate to PEM format.")
        }

    }

}
