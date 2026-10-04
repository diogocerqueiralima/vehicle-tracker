package com.github.diogocerqueiralima.asset.service.infrastructure.persistence

import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.DeviceMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.repositories.DeviceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DevicePersistenceImpl(private val deviceRepository: DeviceRepository) : DevicePersistence {

    override fun save(device: Device): Device {

        val entity = DeviceMapper.toEntity(device)
        val savedEntity = deviceRepository.save(entity)

        return DeviceMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Device? =
        deviceRepository.findByIdOrNull(id)?.let(DeviceMapper::toDomain)

    override fun findByIdAndOwnerId(id: UUID, ownerId: UUID): Device? =
        deviceRepository.findByIdAndOwnerId(id, ownerId)?.let(DeviceMapper::toDomain)

    override fun isOwner(id: UUID, ownerId: UUID): Boolean =
        deviceRepository.existsByIdAndOwnerId(id, ownerId)

    override fun getPage(pageNumber: Int, pageSize: Int): Page<Device> {

        // 1. Converts one-based inbound page number to Spring Data zero-based index.
        val pageRequest = PageRequest.of(pageNumber - 1, pageSize)

        // 2. Loads entity page and maps each entry to the domain model.
        return deviceRepository.findAll(pageRequest).map { DeviceMapper.toDomain(it) }
    }

    override fun getPageByOwnerId(pageNumber: Int, pageSize: Int, ownerId: UUID): Page<Device> {

        // 1. Converts inbound page number to Spring Data zero-based index.
        val pageRequest = PageRequest.of(pageNumber, pageSize)

        // 2. Loads owner-scoped entity page and maps each entry to the domain model.
        return deviceRepository.findAllByOwnerId(ownerId, pageRequest).map { DeviceMapper.toDomain(it) }
    }

    override fun isSerialNumberOrImeiTakenByAnotherDevice(serialNumber: String, imei: String, excludingId: UUID): Boolean =
        deviceRepository.existsBySerialNumberOrImeiAndIdNot(serialNumber, imei, excludingId)

}
