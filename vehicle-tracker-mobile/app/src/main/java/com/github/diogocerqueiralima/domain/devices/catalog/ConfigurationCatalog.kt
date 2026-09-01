package com.github.diogocerqueiralima.domain.devices.catalog

import java.util.UUID

/**
 * Wire format of a characteristic's value, as encoded by the device firmware.
 * See docs/device/ble for the source of truth this catalog mirrors.
 */
enum class CharacteristicFormat {
    STRING,
    UINT8,
    UINT16,
    UINT32,
    BOOLEAN
}

/**
 * Static description of a single GATT characteristic exposed by the device.
 *
 * @property key Unique identifier for this characteristic, suitable for indexing UI state.
 */
data class CharacteristicSpec(
    val name: String,
    val serviceId: UUID,
    val characteristicId: UUID,
    val format: CharacteristicFormat,
    val readable: Boolean,
    val writable: Boolean,
    val description: String
) {
    val key: String = "$serviceId:$characteristicId"
}

data class ServiceSpec(
    val name: String,
    val uuid: UUID,
    val characteristics: List<CharacteristicSpec>
)

/**
 * Static catalog of every GATT service/characteristic the device exposes, mirroring
 * docs/device/ble/{connection,gps,authentication}/overview.md.
 */
object Catalog {

    private val connectionServiceId = UUID.fromString("1304eaaa-c937-511a-6605-c9858c877865")
    private val gpsServiceId = UUID.fromString("6ed54c3d-ca79-1398-7148-34a9bf29d12d")
    private val authenticationServiceId = UUID.fromString("347df573-cf50-f1b5-e749-1efe2d71272e")

    val services: List<ServiceSpec> = listOf(
        ServiceSpec(
            name = "Connection",
            uuid = connectionServiceId,
            characteristics = listOf(
                CharacteristicSpec(
                    name = "broker_url",
                    serviceId = connectionServiceId,
                    characteristicId = UUID.fromString("755e0efa-870c-6f29-f505-577140f42a00"),
                    format = CharacteristicFormat.STRING,
                    readable = true,
                    writable = true,
                    description = "The URL of the MQTT broker to which the service will connect."
                ),
                CharacteristicSpec(
                    name = "keep_alive",
                    serviceId = connectionServiceId,
                    characteristicId = UUID.fromString("8f97f131-bce8-6f20-d10b-363ff3db3b19"),
                    format = CharacteristicFormat.UINT16,
                    readable = true,
                    writable = true,
                    description = "The keep-alive interval in seconds for the MQTT connection."
                ),
                CharacteristicSpec(
                    name = "qos",
                    serviceId = connectionServiceId,
                    characteristicId = UUID.fromString("8611d086-a805-7f20-840d-5202ba787349"),
                    format = CharacteristicFormat.UINT8,
                    readable = true,
                    writable = true,
                    description = "The Quality of Service level for MQTT messages (0, 1, or 2)."
                ),
                CharacteristicSpec(
                    name = "recon_interval",
                    serviceId = connectionServiceId,
                    characteristicId = UUID.fromString("7caaecb0-677f-9131-4b0c-0ba295f27214"),
                    format = CharacteristicFormat.UINT32,
                    readable = true,
                    writable = true,
                    description = "The interval in seconds for attempting to reconnect to the MQTT broker if the connection is lost."
                )
            )
        ),
        ServiceSpec(
            name = "GPS",
            uuid = gpsServiceId,
            characteristics = listOf(
                CharacteristicSpec(
                    name = "gps_update",
                    serviceId = gpsServiceId,
                    characteristicId = UUID.fromString("a7586118-a223-8f13-930b-215917f29126"),
                    format = CharacteristicFormat.UINT32,
                    readable = true,
                    writable = true,
                    description = "The frequency in seconds at which the device updates its GPS location."
                ),
                CharacteristicSpec(
                    name = "gps_timeout",
                    serviceId = gpsServiceId,
                    characteristicId = UUID.fromString("ca561647-1ef5-4213-190f-daf6f42c83d1"),
                    format = CharacteristicFormat.UINT32,
                    readable = true,
                    writable = true,
                    description = "The maximum time in seconds the device will wait for a GPS fix before giving up."
                ),
                CharacteristicSpec(
                    name = "gps_mode",
                    serviceId = gpsServiceId,
                    characteristicId = UUID.fromString("d67ed5fc-a8db-c68c-2544-e5ed59f356c6"),
                    format = CharacteristicFormat.STRING,
                    readable = true,
                    writable = true,
                    description = "Fix strategy for the GPS: standalone, ue-based, or ue-assisted."
                )
            )
        ),
        ServiceSpec(
            name = "Authentication",
            uuid = authenticationServiceId,
            characteristics = listOf(
                CharacteristicSpec(
                    name = "csr",
                    serviceId = authenticationServiceId,
                    characteristicId = UUID.fromString("90785634-12ef-cd2b-9008-f6e5d4c3b2a1"),
                    format = CharacteristicFormat.STRING,
                    readable = true,
                    writable = false,
                    description = "The Certificate Signing Request (CSR) generated by the device."
                ),
                CharacteristicSpec(
                    name = "certificate",
                    serviceId = authenticationServiceId,
                    characteristicId = UUID.fromString("df5c268d-107b-d836-e808-a442ac515561"),
                    format = CharacteristicFormat.STRING,
                    readable = true,
                    writable = true,
                    description = "The certificate generated from the CSR."
                ),
                CharacteristicSpec(
                    name = "ca",
                    serviceId = authenticationServiceId,
                    characteristicId = UUID.fromString("39897685-53c9-7092-d542-7dbfc95f7dce"),
                    format = CharacteristicFormat.STRING,
                    readable = true,
                    writable = true,
                    description = "The CA certificate trusted by the device."
                ),
                CharacteristicSpec(
                    name = "revoke",
                    serviceId = authenticationServiceId,
                    characteristicId = UUID.fromString("6e5b0cbc-87f6-9c9a-4d43-e29bb72441aa"),
                    format = CharacteristicFormat.BOOLEAN,
                    readable = false,
                    writable = true,
                    description = "A flag to revoke the current certificate."
                ),
                CharacteristicSpec(
                    name = "expiration",
                    serviceId = authenticationServiceId,
                    characteristicId = UUID.fromString("7c85ca03-73b1-d990-6d4f-8267197e2e0e"),
                    format = CharacteristicFormat.STRING,
                    readable = true,
                    writable = true,
                    description = "The expiration time used to generate the certificate, in seconds."
                ),
                CharacteristicSpec(
                    name = "status",
                    serviceId = authenticationServiceId,
                    characteristicId = UUID.fromString("aeb2f94c-b465-568d-264f-8a4ca22abc62"),
                    format = CharacteristicFormat.STRING,
                    readable = true,
                    writable = false,
                    description = "The status of the certificate (valid, expired, pending, revoked)."
                )
            )
        )
    )

}
