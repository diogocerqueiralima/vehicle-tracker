package com.github.diogocerqueiralima.infrastructure.parser;

import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateParser;
import com.github.diogocerqueiralima.infrastructure.exceptions.CertificateCouldNotBeParsedException;
import com.github.diogocerqueiralima.infrastructure.exceptions.InvalidCertificateException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@Component
public class CertificateParserImpl implements CertificateParser {

    private static final Logger log = LoggerFactory.getLogger(CertificateParserImpl.class);

    @Override
    public CertificateInfo parse(Certificate certificate) {

        try(
                Reader reader = new InputStreamReader(new ByteArrayInputStream(certificate.getData()));
                PEMParser pemParser = new PEMParser(reader)
        ) {

            Object obj = pemParser.readObject();

            if (!(obj instanceof X509CertificateHolder holder)) {
                log.error("Invalid X.509 certificate: {}", obj.getClass().getName());
                throw new InvalidCertificateException();
            }

            return new CertificateInfo(
                    holder.getSerialNumber().toString(),
                    holder.getSubject().toString(),
                    holder.getNotBefore().toInstant(),
                    holder.getNotAfter().toInstant(),
                    false
            );

        } catch (IOException e) {
            log.error("Error parsing certificate", e);
            throw new CertificateCouldNotBeParsedException();
        }

    }

}
