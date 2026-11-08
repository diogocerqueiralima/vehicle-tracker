package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.DeleteSimCardByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetSimCardByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardPersistence
import com.github.diogocerqueiralima.error.common.exceptions.ConflictException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SimCardUseCaseImplTest {

    @Mock
    private lateinit var simCardPersistence: SimCardPersistence

    @InjectMocks
    private lateinit var simCardUseCase: SimCardUseCaseImpl

    @Test
    @DisplayName("Should create sim card when unique fields are available")
    fun should_create_sim_card_when_unique_fields_are_available() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val command = CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001", ownerId)
        val saved = SimCard(id, ownerId, createdAt, updatedAt, command.iccid, command.msisdn, command.imsi)

        whenever(simCardPersistence.save(any())).thenReturn(saved)

        val result = simCardUseCase.create(command)

        assertThat(result.iccid).isEqualTo(command.iccid)
        assertThat(result.msisdn).isEqualTo(command.msisdn)
        assertThat(result.imsi).isEqualTo(command.imsi)
        verify(simCardPersistence).save(any())
    }

    @Test
    @DisplayName("Should fail creating when ICCID, MSISDN or IMSI already exists")
    fun should_fail_creating_when_unique_field_already_exists() {

        val command = CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001", UUID.randomUUID())
        whenever(simCardPersistence.save(any())).thenThrow(ConflictException("A SIM card with the provided ICCID, MSISDN or IMSI already exists."))

        assertThatThrownBy { simCardUseCase.create(command) }
            .isInstanceOf(ConflictException::class.java)
    }

    @Test
    @DisplayName("Should update sim card when it exists and unique fields are valid")
    fun should_update_sim_card_when_it_exists_and_unique_fields_are_valid() {

        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val ownerId = UUID.randomUUID()
        val existing = SimCard(id, ownerId, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001")
        val command = UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002", ownerId)
        val updated = SimCard(id, ownerId, createdAt, updatedAt, command.iccid, command.msisdn, command.imsi)

        whenever(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(existing)
        whenever(simCardPersistence.save(any())).thenReturn(updated)

        val result = simCardUseCase.update(command)

        assertThat(result.iccid).isEqualTo("8901000000000000001")
        assertThat(result.msisdn).isEqualTo("351910000002")
        assertThat(result.imsi).isEqualTo("268010000000002")
    }

    @Test
    @DisplayName("Should fail updating when sim card does not exist")
    fun should_fail_updating_when_sim_card_does_not_exist() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val command = UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002", ownerId)

        whenever(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(null)

        assertThatThrownBy { simCardUseCase.update(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("SIM card not found for id: $id")
    }

    @Test
    @DisplayName("Should fail updating when MSISDN or IMSI belongs to another sim card")
    fun should_fail_updating_when_unique_field_belongs_to_another_sim_card() {

        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val ownerId = UUID.randomUUID()
        val existing = SimCard(id, ownerId, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001")
        val command = UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002", ownerId)

        whenever(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(existing)
        whenever(simCardPersistence.save(any())).thenThrow(ConflictException("A SIM card with the provided ICCID, MSISDN or IMSI already exists."))

        assertThatThrownBy { simCardUseCase.update(command) }
            .isInstanceOf(ConflictException::class.java)

        verify(simCardPersistence).save(any())
    }

    @Test
    @DisplayName("Should get sim card by id when it exists")
    fun should_get_sim_card_by_id_when_it_exists() {

        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val ownerId = UUID.randomUUID()
        val simCard = SimCard(id, ownerId, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001")

        whenever(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(simCard)

        val result = simCardUseCase.getById(GetSimCardByIdCommand(id, ownerId))

        assertThat(result.iccid).isEqualTo(simCard.iccid)
        assertThat(result.msisdn).isEqualTo(simCard.msisdn)
        assertThat(result.imsi).isEqualTo(simCard.imsi)
    }

    @Test
    @DisplayName("Should fail getting sim card by id when it does not exist")
    fun should_fail_getting_sim_card_by_id_when_it_does_not_exist() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        whenever(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(null)

        assertThatThrownBy { simCardUseCase.getById(GetSimCardByIdCommand(id, ownerId)) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("SIM card not found for id: $id")
    }

    @Test
    @DisplayName("Should delete sim card by id when it exists")
    fun should_delete_sim_card_by_id_when_it_exists() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val existing = SimCard(id, ownerId, Instant.now(), Instant.now(), "8901000000000000001", "351910000001", "268010000000001")

        whenever(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(existing)

        simCardUseCase.deleteById(DeleteSimCardByIdCommand(id, ownerId))

        verify(simCardPersistence).deleteByIdAndOwnerId(id, ownerId)
    }

    @Test
    @DisplayName("Should fail deleting sim card by id when it does not exist")
    fun should_fail_deleting_sim_card_by_id_when_it_does_not_exist() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        whenever(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(null)

        assertThatThrownBy { simCardUseCase.deleteById(DeleteSimCardByIdCommand(id, ownerId)) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("SIM card not found for id: $id")

        verify(simCardPersistence, never()).deleteByIdAndOwnerId(id, ownerId)
    }

}
