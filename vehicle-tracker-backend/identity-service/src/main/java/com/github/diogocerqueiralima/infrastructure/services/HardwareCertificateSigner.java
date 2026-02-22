package com.github.diogocerqueiralima.infrastructure.services;

import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.CertificateSubject;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigner;
import com.github.diogocerqueiralima.infrastructure.exceptions.*;
import com.github.diogocerqueiralima.presentation.config.ApplicationURIs;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.Date;

@Service
public class HardwareCertificateSigner implements CertificateSigner {

    private static final Logger log = LoggerFactory.getLogger(HardwareCertificateSigner.class);
    private final KeyPair keyPair;

    public HardwareCertificateSigner(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    /**
     *
     * {@inheritDoc}
     * <p>
     * This method uses the CA's private key stored in a trusted module platform (TPM) to sign the certificate.
     */
    @Override
    public byte[] sign(
            CertificateSigningRequest certificateSigningRequest, String issuer, BigInteger serialNumber,
            Instant notBefore, Instant notAfter
    ) {

        try {

            // 1. Extract the public key and subject information from the certificate signing request
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            CertificateSubject subject = certificateSigningRequest.getSubject();

            // 2. Build the X.509 certificate
            X509v3CertificateBuilder certBuilder =
                    new JcaX509v3CertificateBuilder(
                            new X500Name(issuer),
                            serialNumber,
                            Date.from(notBefore),
                            Date.from(notAfter),
                            new X500Name(subject.toString()),
                            certificateSigningRequest.getPublicKey()
                    );

            // 3. Add extensions to the certificate

            // 3.1 Basic Constraints: Not a CA

            certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
            certBuilder.addExtension(
                    Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment)
            );

            // 3.2 Subject Alternative Name: Add the Common Name as an alternative name

            GeneralName[] names = {new GeneralName(GeneralName.rfc822Name, subject.getCommonName())};
            certBuilder.addExtension(Extension.subjectAlternativeName, false, new GeneralNames(names));

            // 3.3 Subject Key Identifier and Authority Key Identifier

            certBuilder.addExtension(
                    Extension.subjectKeyIdentifier,
                    false,
                    new SubjectKeyIdentifier(certificateSigningRequest.getPublicKey().getEncoded())
            );

            certBuilder.addExtension(
                    Extension.authorityKeyIdentifier, false, new AuthorityKeyIdentifier(publicKey.getEncoded())
            );

            // 3.4 CRL Distribution Points: Add a placeholder CRL distribution point

            DistributionPoint[] distributionPoints = {
                    new DistributionPoint(
                            new DistributionPointName(
                                    new GeneralNames(
                                            new GeneralName(
                                                    GeneralName.uniformResourceIdentifier,
                                                    "https://api.mytracker.pt"
                                                            + ApplicationURIs.CRL_DISTRIBUTION_POINT_URI
                                            )
                                    )
                            ),
                            null,
                            null
                    )
            };

            certBuilder.addExtension(
                    Extension.cRLDistributionPoints, false, new CRLDistPoint(distributionPoints)
            );

            // 4. Sign the certificate using the CA's private key

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA")
                    .build(privateKey);

            X509CertificateHolder holder = certBuilder.build(signer);

            // 5. Convert the certificate to PEM format

            byte[] pemBytes = convertToPem(holder);

            // 6. Return the signed certificate

            log.info("Certificate signed successfully for Subject={}", subject.toString());
            return pemBytes;

        } catch (CertIOException | OperatorCreationException e) {
            log.error("Error signing certificate", e);
            throw new CouldNotSignCertificateException();
        }

    }

    /**
     *
     * Converts an X509CertificateHolder to PEM format.
     *
     * @param holder the X509CertificateHolder to convert
     * @return the PEM-encoded byte array of the certificate
     */
    private byte[] convertToPem(X509CertificateHolder holder) {

        try {

            StringWriter sw = new StringWriter();

            try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
                pemWriter.writeObject(holder);
            }

            return sw.toString().getBytes(StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("Error converting certificate to PEM format", e);
            throw new CouldNotConvertCertificateToPEMException();
        }

    }

}
