package com.github.diogocerqueiralima.domain.model.options;

/**
 * Interface representing options for certificate operations, such as revocation or usage.
 * Implementations of this interface should provide validation logic to ensure that the options are valid for the intended operation.
 */
public interface CertificateOptions {

    /**
     *
     * Validates the options to ensure they are suitable for the intended certificate operation.
     * This method should be implemented by all classes that implement this interface to provide specific validation logic based on the options they represent.
     *
     * @return true if the options are valid for the intended operation, false otherwise.
     */
    boolean validate();

}
