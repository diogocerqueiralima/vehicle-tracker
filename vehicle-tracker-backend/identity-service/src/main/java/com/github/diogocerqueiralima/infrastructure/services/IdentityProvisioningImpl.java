package com.github.diogocerqueiralima.infrastructure.services;

import com.github.diogocerqueiralima.domain.ports.outbound.IdentityProvisioning;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IdentityProvisioningImpl implements IdentityProvisioning {

    private final RestTemplate restTemplate;

    public IdentityProvisioningImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void provisionIdentity(String name) {


    }

}
