import {ApiResponseDTO} from "@/dto/ApiResponseDTO";
import {BootstrapCertificate} from "@/domain/BootstrapCertificate";
import {PageDTO} from "@/dto/PageDTO";
import {BootstrapCertificateDTO} from "@/dto/BootstrapCertificateDTO";
import {Page} from "@/domain/Page";

export function bootstrapCertificateService() {

    const BOOTSTRAP_CERTIFICATES_PER_PAGE = 10
    const BOOTSTRAP_CERTIFICATES_PATH = "/certificates/bootstrap"

    async function getPage(page: number, serialNumberFilter: string): Promise<Page<BootstrapCertificate>> {

        const response = await fetch(
            `/api/proxy${BOOTSTRAP_CERTIFICATES_PATH}?pageNumber=${page}&pageSize=${BOOTSTRAP_CERTIFICATES_PER_PAGE}&serialNumber=${encodeURIComponent(serialNumberFilter)}`,
            {
                method: "GET"
            }
        )

        if (response.status == 401) {
            throw new Error("Unauthorized. Please log in again.")
        }

        if (response.status == 403) {
            throw new Error("Forbidden. You don't have permission to access this resource.")
        }

        if (response.headers.get("content-type") != "application/json") {
            const text = await response.text()
            throw new Error(`Unexpected response from server: ${text}`)
        }

        const json: ApiResponseDTO<PageDTO<BootstrapCertificateDTO>> = await response.json()

        if (!response.ok) {
            throw new Error(json.message)
        }

        const pageDTO = json.data

        return {
            page: pageDTO.page,
            totalPages: pageDTO.total_pages,
            totalItems: pageDTO.total_items,
            items: pageDTO.items.map(dto => ({
                serialNumber: String(dto.serial_number).replace(".", "").substring(0, 16),
                subject: dto.subject,
                issuedAt: dto.issued_at,
                expiresAt: dto.expires_at,
                revoked: dto.revoked,
                used: dto.used
            }))
        }
    }

    return {
        getPage
    }

}