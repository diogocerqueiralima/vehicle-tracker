package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.SimCardUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateSimCardRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SimCardControllerTest {

    @Mock
    private lateinit var simCardUseCase: SimCardUseCase

    @InjectMocks
    private lateinit var simCardController: SimCardController

    @Test
    @DisplayName("Should create sim card and return created response")
    fun should_create_sim_card_and_return_created_response() {

        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val result = SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001")
        whenever(simCardUseCase.create(any())).thenReturn(result)

        val userId = UUID.randomUUID()
        val request = CreateSimCardRequestDTO("8901000000000000001", "351910000001", "268010000000001")

        val response = simCardController.create(userId.toString(), request)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("SIM card created successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.id).isEqualTo(result.id)
        assertThat(body.data.createdAt).isEqualTo(result.createdAt)
        assertThat(body.data.updatedAt).isEqualTo(result.updatedAt)
        assertThat(body.data.iccid).isEqualTo(result.iccid)
        assertThat(body.data.msisdn).isEqualTo(result.msisdn)
        assertThat(body.data.imsi).isEqualTo(result.imsi)
    }

    @Test
    @DisplayName("Should update sim card and return ok response")
    fun should_update_sim_card_and_return_ok_response() {

        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val result = SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000002", "268010000000002")
        whenever(simCardUseCase.update(any())).thenReturn(result)

        val userId = UUID.randomUUID()
        val request = UpdateSimCardRequestDTO("8901000000000000001", "351910000002", "268010000000002")

        val response = simCardController.update(id, userId.toString(), request)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("SIM card updated successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.id).isEqualTo(result.id)
        assertThat(body.data.createdAt).isEqualTo(result.createdAt)
        assertThat(body.data.updatedAt).isEqualTo(result.updatedAt)
        assertThat(body.data.iccid).isEqualTo(result.iccid)
        assertThat(body.data.msisdn).isEqualTo("351910000002")
        assertThat(body.data.imsi).isEqualTo("268010000000002")
    }

    @Test
    @DisplayName("Should get sim card by id and return ok response")
    fun should_get_sim_card_by_id_and_return_ok_response() {

        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val result = SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000002", "268010000000002")
        whenever(simCardUseCase.getById(any())).thenReturn(result)

        val userId = UUID.randomUUID()
        val response = simCardController.getById(id, userId.toString())

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("SIM card fetched successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.id).isEqualTo(result.id)
        assertThat(body.data.createdAt).isEqualTo(result.createdAt)
        assertThat(body.data.updatedAt).isEqualTo(result.updatedAt)
        assertThat(body.data.iccid).isEqualTo(result.iccid)
        assertThat(body.data.msisdn).isEqualTo("351910000002")
        assertThat(body.data.imsi).isEqualTo("268010000000002")
    }

    @Test
    @DisplayName("Should delete sim card by id and return ok response")
    fun should_delete_sim_card_by_id_and_return_ok_response() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val response = simCardController.deleteById(id, userId.toString())

        verify(simCardUseCase).deleteById(any())
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("SIM card deleted successfully.")
        assertThat(body.data).isNull()
    }

}
