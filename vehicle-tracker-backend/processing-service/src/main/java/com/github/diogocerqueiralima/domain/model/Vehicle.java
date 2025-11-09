package com.github.diogocerqueiralima.domain.model;

import java.util.UUID;

public class Vehicle {

    private final UUID id;

    public Vehicle(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

}
