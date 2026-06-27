package com.github.diogocerqueiralima.identity.service.infrastructure.repositories;

import com.github.diogocerqueiralima.identity.service.infrastructure.entities.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

/**
 * Repository interface for managing {@link CertificateEntity} instances in the database.
 */
@Repository
public interface CertificateRepository extends JpaRepository<CertificateEntity, BigInteger> {}
