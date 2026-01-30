package com.github.diogocerqueiralima.infrastructure.signer;

import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigner;
import com.github.diogocerqueiralima.infrastructure.config.CertificateAuthorityConfig;
import com.github.diogocerqueiralima.infrastructure.exceptions.*;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Component
public class HardwareCertificateSigner implements CertificateSigner {

    private static final Logger log = LoggerFactory.getLogger(HardwareCertificateSigner.class);
    private final KeyPair keyPair;
    private final CertificateAuthorityConfig caConfig;

    public HardwareCertificateSigner(KeyPair keyPair, CertificateAuthorityConfig caConfig) {
        this.keyPair = keyPair;
        this.caConfig = caConfig;
    }

    /**
     *
     * {@inheritDoc}
     * <p>
     * This method uses the CA's private key stored in a trusted module platform (TPM) to sign the certificate.
     */
    @Override
    public Certificate sign(CertificateSigningRequest request) {

        // 1. Parse the PKCS#10 request (PEM format)

        try(
                Reader reader = new InputStreamReader(new ByteArrayInputStream(request.getData()));
                PEMParser pemParser = new PEMParser(reader)
        ) {

            Object obj = pemParser.readObject();

            if (!(obj instanceof PKCS10CertificationRequest pkcs10Request)) {
                log.error("Invalid PKCS#10 certification request");
                throw new InvalidCertificateSigningRequestException();
            }

            // 2. Verify the signature of the PKCS#10 request

            if (!verifySignature(pkcs10Request)) {
                log.error("Invalid signature in the certificate signing request");
                throw new InvalidSignatureException();
            }

            // 3. Extract information from the PKCS#10 request

            Optional<String> commonNameOpt = extractCommonName(pkcs10Request.getSubject());

            if (commonNameOpt.isEmpty()) {
                log.error("Common Name (CN) not found in the certificate signing request");
                throw new InvalidCommonNameException();
            }

            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            String commonName = commonNameOpt.get();
            BigInteger serialNumber = generateSerialNumber();
            Instant notBefore = Instant.now();
            Instant notAfter = notBefore.plus(caConfig.getValidityDays(), ChronoUnit.DAYS);

            // 4. Build the X.509 certificate

            X509v3CertificateBuilder certBuilder =
                    new JcaX509v3CertificateBuilder(
                            new X500Name(caConfig.getIssuer()),
                            serialNumber,
                            Date.from(notBefore),
                            Date.from(notAfter),
                            pkcs10Request.getSubject(),
                            pkcs10Request.getSubjectPublicKeyInfo()
                    );

            // 5. Add extensions to the certificate

            certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
            certBuilder.addExtension(
                    Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment)
            );

            GeneralName[] names = { new GeneralName(GeneralName.rfc822Name, commonName) };
            certBuilder.addExtension(Extension.subjectAlternativeName, false, new GeneralNames(names));

            certBuilder.addExtension(
                    Extension.subjectKeyIdentifier,
                    false,
                    new SubjectKeyIdentifier(pkcs10Request.getSubjectPublicKeyInfo().getEncoded())
            );

            certBuilder.addExtension(
                    Extension.authorityKeyIdentifier, false, new AuthorityKeyIdentifier(publicKey.getEncoded())
            );

            // 6. Sign the certificate using the CA's private key

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA")
                    .build(privateKey);

            X509CertificateHolder holder = certBuilder.build(signer);

            // 7. Convert the certificate to PEM format

            byte[] pemBytes = convertToPem(holder);

            // 8. Return the signed certificate

            log.info("Certificate signed successfully for CN={}", commonName);
            return new Certificate(pemBytes);

        }  catch (OperatorCreationException | IOException e) {
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

    /**
     *
     * Extracts the Common Name (CN) from the subject's distinguished name.
     *
     * @param subject the X500Name representing the subject's distinguished name
     * @return an Optional containing the Common Name if present, otherwise an empty Optional
     */
    private Optional<String> extractCommonName(X500Name subject) {

        RDN[] rdns = subject.getRDNs(BCStyle.CN);

        return Arrays.stream(rdns)
                .findFirst()
                .map(RDN::getFirst)
                .flatMap(attribute -> Optional.ofNullable(attribute.getValue().toString()));
    }

    /**
     *
     * Generates a random serial number for the certificate.
     *
     * @return the generated serial number
     */
    private BigInteger generateSerialNumber() {
        return new BigInteger(256, new SecureRandom());
    }

    /**
     *
     * Verifies the signature of the given PKCS#10 certification request.
     *
     * @param pkcs10Request the PKCS#10 certification request to verify
     * @return true if the signature is valid, false otherwise
     */
    private boolean verifySignature(PKCS10CertificationRequest pkcs10Request)  {

        try {

            JcaPKCS10CertificationRequest jcaCR = new JcaPKCS10CertificationRequest(pkcs10Request);

            return jcaCR.isSignatureValid(
                    new JcaContentVerifierProviderBuilder()
                            .setProvider("BC")
                            .build(jcaCR.getPublicKey())
            );

        } catch (NoSuchAlgorithmException | InvalidKeyException | OperatorCreationException | PKCSException e) {
            log.error("Error verifying PKCS#10 signature", e);
            throw new SignatureVerificationException();
        }

    }

}
