package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.Device;
import com.github.diogocerqueiralima.domain.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.DeviceRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class DevicePersistenceImpl implements DevicePersistence {

    private final DeviceMapper deviceMapper;
    private final DeviceRepository deviceRepository;

    public DevicePersistenceImpl(
            @Qualifier("dm-infrastructure") DeviceMapper deviceMapper, DeviceRepository deviceRepository
    ) {
        this.deviceMapper = deviceMapper;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Device save(Device device) {

        DeviceEntity entity = deviceMapper.toEntity(device);
        DeviceEntity savedEntity = deviceRepository.save(entity);

        return deviceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Device> findById(UUID id) {
        return deviceRepository.findById(id)
                .map(deviceMapper::toDomain);
    }

    @Override
    public void delete(Device device) {
        DeviceEntity entity = deviceMapper.toEntity(device);
        deviceRepository.delete(entity);
    }

}
