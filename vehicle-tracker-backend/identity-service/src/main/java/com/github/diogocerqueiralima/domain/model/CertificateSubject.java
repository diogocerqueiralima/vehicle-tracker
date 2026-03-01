package com.github.diogocerqueiralima.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the subject of a certificate, containing information such as Common Name (CN), Organization (O), and Country (C).
 */
public class CertificateSubject {

    private final String commonName;
    private final String organization;
    private final String country;

    /**
     *
     * Creates a new instance of CertificateSubject with the specified common name, organization, and country.
     *
     * @param commonName the common name (CN) of the certificate subject
     * @param organization the organization (O) associated with the certificate subject
     * @param country the country (C) associated with the certificate subject
     */
    public CertificateSubject(String commonName, String organization, String country) {
        this.commonName = commonName;
        this.organization = organization;
        this.country = country;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return "CN=" + commonName + ", O=" + organization + ", C=" + country;
    }

    /**
     *
     * Parser for a Distinguished Name (DN) string in the format "CN=..., O=..., C=...".
     *
     * @param dn the Distinguished Name string to parse
     * @return a CertificateSubject instance created from the parsed DN string
     */
    public static CertificateSubject fromString(String dn) {

        if (dn == null || dn.isEmpty()) {
            throw new IllegalArgumentException("Distinguished Name cannot be null or empty");
        }

        Map<String, String> attributes = new HashMap<>();
        String[] parts = dn.split(",");

        for (String part : parts) {

            String[] keyValue = part.trim().split("=", 2);

            if (keyValue.length == 2) {
                attributes.put(keyValue[0].trim().toUpperCase(), keyValue[1].trim());
            }

        }

        String cn = attributes.get("CN");
        String o = attributes.get("O");
        String c = attributes.get("C");

        if (cn == null || o == null || c == null) {
            throw new IllegalArgumentException("Distinguished Name must contain CN, O and C");
        }

        return new CertificateSubject(cn, o, c);
    }

}
