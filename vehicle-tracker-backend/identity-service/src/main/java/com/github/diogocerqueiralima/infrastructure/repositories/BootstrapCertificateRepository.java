package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.BootstrapCertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BootstrapCertificateRepository extends JpaRepository<BootstrapCertificateEntity, String> {}
