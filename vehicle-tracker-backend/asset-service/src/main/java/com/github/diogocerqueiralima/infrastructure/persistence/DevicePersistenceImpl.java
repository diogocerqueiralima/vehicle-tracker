package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.assets.Device;
import com.github.diogocerqueiralima.application.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.DeviceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return deviceRepository.existsBySerialNumber(serialNumber);
    }

    @Override
    public boolean existsByImei(String imei) {
        return deviceRepository.existsByImei(imei);
    }

    @Override
    public Page<Device> getPage(int pageNumber, int pageSize) {

        // 1. Converts one-based inbound page number to Spring Data zero-based index.
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);

        // 2. Loads entity page and maps each entry to the domain model.
        return deviceRepository.findAll(pageRequest)
                .map(DeviceMapper::toDomain);
    }

}

