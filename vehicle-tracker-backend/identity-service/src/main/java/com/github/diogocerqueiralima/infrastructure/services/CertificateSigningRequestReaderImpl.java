package com.github.diogocerqueiralima.infrastructure.services;

import com.github.diogocerqueiralima.domain.model.CertificateSigningRequest;
import com.github.diogocerqueiralima.domain.model.CertificateSubject;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateSigningRequestReader;
import com.github.diogocerqueiralima.infrastructure.exceptions.CouldNotReadCertificateSigningRequestException;
import com.github.diogocerqueiralima.infrastructure.exceptions.InvalidCertificateSigningRequestException;
import com.github.diogocerqueiralima.infrastructure.exceptions.InvalidSignatureException;
import com.github.diogocerqueiralima.infrastructure.exceptions.SignatureVerificationException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

@Service
public class CertificateSigningRequestReaderImpl implements CertificateSigningRequestReader {

    private static final Logger log = LoggerFactory.getLogger(CertificateSigningRequestReaderImpl.class);

    @Override
    public CertificateSigningRequest read(byte[] data) {

        // 1. Parse the PKCS#10 request (PEM format)
        try(
                Reader reader = new InputStreamReader(new ByteArrayInputStream(data));
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

            // 3. Extract subject information and other relevant data from the PKCS#10 request
            X500Name subject = pkcs10Request.getSubject();
            CertificateSubject certificateSubject = new CertificateSubject(
                    getRdnValue(subject, BCStyle.CN),
                    getRdnValue(subject, BCStyle.O),
                    getRdnValue(subject, BCStyle.C)
            );

            // 4. Extract the public key from the PKCS#10 request
            SubjectPublicKeyInfo subjectPublicKeyInfo = pkcs10Request.getSubjectPublicKeyInfo();
            PublicKey publicKey = BouncyCastleProvider.getPublicKey(subjectPublicKeyInfo);

            return new CertificateSigningRequest(certificateSubject, publicKey);

        } catch (IOException e) {
            throw new CouldNotReadCertificateSigningRequestException();
        }

    }

    private String getRdnValue(X500Name subject, ASN1ObjectIdentifier type) {

        RDN[] rdns = subject.getRDNs(type);
        if (rdns.length == 0) return null;

        return rdns[0].getFirst().getValue().toString();
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
