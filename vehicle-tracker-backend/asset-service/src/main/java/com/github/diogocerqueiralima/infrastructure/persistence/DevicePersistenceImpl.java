package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.assets.Device;
import com.github.diogocerqueiralima.application.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper.toDomain;
import static com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper.toEntity;

@Service
public class DevicePersistenceImpl implements DevicePersistence {

    private final DeviceRepository deviceRepository;

    public DevicePersistenceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Device save(Device device) {

        DeviceEntity entity = toEntity(device);
        DeviceEntity savedEntity = deviceRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<Device> findById(UUID id) {
        return deviceRepository
                .findById(id)
                .map(DeviceMapper::toDomain);
    }

}

