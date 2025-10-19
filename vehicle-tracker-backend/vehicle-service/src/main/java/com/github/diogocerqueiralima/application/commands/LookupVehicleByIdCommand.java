package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LookupVehicleByIdCommand(@NotNull UUID id) {}
