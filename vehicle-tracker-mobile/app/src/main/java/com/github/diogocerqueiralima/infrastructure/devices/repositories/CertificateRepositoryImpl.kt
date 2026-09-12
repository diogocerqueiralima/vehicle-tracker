package com.github.diogocerqueiralima.infrastructure.devices.repositories

import com.github.diogocerqueiralima.BuildConfig
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException
import com.github.diogocerqueiralima.domain.devices.repositories.CertificateRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

/**
 * Implementation of [CertificateRepository] using [HttpClient] to have the device's certificate
 * signing request signed by the Identity Service. The signed-in user's bearer token is attached by
 * the client itself, and is the trust the service signs on.
 */
class CertificateRepositoryImpl(private val httpClient: HttpClient) : CertificateRepository {

    private companion object {

        const val CERTIFICATES_BASE_URI = "${BuildConfig.IDENTITY_SERVICE_URI}/certificates"
        const val CERTIFICATE_SIGNING_REQUEST_URI = "$CERTIFICATES_BASE_URI/sign"

        const val CSR_PART_NAME = "csr"
        const val CSR_FILENAME = "csr.pem"

    }

    override suspend fun sign(csr: ByteArray): ByteArray {

        // 1. Upload the request as the file part the signing endpoint expects, byte for byte.
        val response = httpClient.post(CERTIFICATE_SIGNING_REQUEST_URI) {

            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            CSR_PART_NAME,
                            csr,
                            Headers.build {
                                append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                                append(HttpHeaders.ContentDisposition, "filename=\"$CSR_FILENAME\"")
                            }
                        )
                    }
                )
            )

        }

        // 2. The client is not configured to throw on error statuses, so the body of a rejected
        // request would otherwise be handed back as if it were a certificate.
        if (!response.status.isSuccess()) {
            throw InternalErrorException("Failed to sign the certificate signing request: ${response.status}")
        }

        // 3. The certificate comes back as the raw PEM, which is what the device is written.
        return response.body<ByteArray>()
    }

}
