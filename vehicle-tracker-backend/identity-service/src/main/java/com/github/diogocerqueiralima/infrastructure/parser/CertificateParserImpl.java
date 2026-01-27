package com.github.diogocerqueiralima.infrastructure.parser;

import com.github.diogocerqueiralima.domain.model.Certificate;
import com.github.diogocerqueiralima.domain.model.CertificateInfo;
import com.github.diogocerqueiralima.domain.ports.outbound.CertificateParser;
import org.springframework.stereotype.Component;

@Component
public class CertificateParserImpl implements CertificateParser {

    @Override
    public CertificateInfo parse(Certificate certificate) {
        return null;
    }

}
