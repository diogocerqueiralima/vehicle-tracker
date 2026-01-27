package com.github.diogocerqueiralima.infrastructure.signer;

import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigner;
import com.github.diogocerqueiralima.infrastructure.config.CertificateAuthorityConfig;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Component
public class HardwareCertificateSigner implements CertificateSigner {

    private final KeyPair keyPair;
    private final CertificateAuthorityConfig caConfig;

    public HardwareCertificateSigner(KeyPair keyPair, CertificateAuthorityConfig caConfig) {
        this.keyPair = keyPair;
        this.caConfig = caConfig;
    }

    /**
     *
     * {@inheritDoc}
     *
     * This method uses the CA's private key stored in a hardware security module (HSM) to sign the certificate.
     */
    @Override
    public Optional<Certificate> sign(CertificateSigningRequest request) {

        try {

            PKCS10CertificationRequest pkcs10Request = new PKCS10CertificationRequest(request.getData());

            if (!verifySignature(pkcs10Request)) {
                return Optional.empty();
            }

            Optional<String> commonNameOpt = extractCommonName(pkcs10Request.getSubject());

            if (commonNameOpt.isEmpty()) {
                return Optional.empty();
            }

            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            String commonName = commonNameOpt.get();
            BigInteger serialNumber = generateSerialNumber();
            Instant notBefore = Instant.now();
            Instant notAfter = notBefore.plus(caConfig.getValidityDays(), ChronoUnit.DAYS);

            X509v3CertificateBuilder certBuilder =
                    new JcaX509v3CertificateBuilder(
                            new X500Name(caConfig.getIssuer()),
                            serialNumber,
                            Date.from(notBefore),
                            Date.from(notAfter),
                            pkcs10Request.getSubject(),
                            pkcs10Request.getSubjectPublicKeyInfo()
                    );

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

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider("BC")
                    .build(privateKey);

            X509CertificateHolder holder = certBuilder.build(signer);

            return Optional.of(new Certificate(holder.getEncoded()));

        } catch (Exception e) {
            return Optional.empty();
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
     * @throws NoSuchAlgorithmException if the algorithm used for signature verification is not available
     * @throws InvalidKeyException if the public key in the request is invalid
     * @throws OperatorCreationException if there is an error creating the content verifier
     * @throws PKCSException if there is an error processing the PKCS#10 request
     */
    private boolean verifySignature(PKCS10CertificationRequest pkcs10Request)
            throws NoSuchAlgorithmException, InvalidKeyException, OperatorCreationException, PKCSException {

        JcaPKCS10CertificationRequest jcaCR = new JcaPKCS10CertificationRequest(pkcs10Request);

        return jcaCR.isSignatureValid(
                new JcaContentVerifierProviderBuilder()
                        .setProvider("BC")
                        .build(jcaCR.getPublicKey())
        );
    }

}
