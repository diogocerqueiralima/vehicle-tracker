package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.assets.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleApplicationMapperTest {

    @Test
    void shouldMapCreateCommandToDomain() {
        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        Vehicle vehicle = VehicleApplicationMapper.toDomain(command, now);

        assertThat(vehicle.getId()).isNotNull();
        assertThat(vehicle.getCreatedAt()).isEqualTo(now);
        assertThat(vehicle.getUpdatedAt()).isEqualTo(now);
        assertThat(vehicle.getVin()).isEqualTo(command.vin());
    }

    @Test
    void shouldMapUpdateCommandToDomainUsingExistingVehicleIdentity() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-10T12:00:00Z");
        Vehicle existingVehicle = new Vehicle(
                id,
                createdAt,
                createdAt,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        UpdateVehicleCommand command = new UpdateVehicleCommand(
                id,
                "1HGCM82633A123456",
                "BB-11-BB",
                "Model Y",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        Instant updatedAt = Instant.parse("2026-03-20T10:00:00Z");
        Vehicle mapped = VehicleApplicationMapper.toDomain(command, existingVehicle, updatedAt);

        assertThat(mapped.getId()).isEqualTo(id);
        assertThat(mapped.getCreatedAt()).isEqualTo(createdAt);
        assertThat(mapped.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(mapped.getPlate()).isEqualTo("BB-11-BB");
    }

    @Test
    void shouldMapDomainToResult() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        Vehicle vehicle = new Vehicle(
                id,
                now,
                now,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        VehicleResult result = VehicleApplicationMapper.toResult(vehicle);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.vin()).isEqualTo(vehicle.getVin());
        assertThat(result.plate()).isEqualTo(vehicle.getPlate());
    }

}

