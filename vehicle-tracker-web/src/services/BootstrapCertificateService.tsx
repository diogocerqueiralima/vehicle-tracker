import {ApiResponse} from "@/domain/ApiResponse";
import {BootstrapCertificate} from "@/domain/BootstrapCertificate";
import {Page} from "@/domain/Page";

export interface BootstrapCertificateServiceProps {

    url: string

}

export function bootstrapCertificateService({ url }: BootstrapCertificateServiceProps) {

    const BOOTSTRAP_CERTIFICATES_PER_PAGE = 10
    const BOOTSTRAP_CERTIFICATES_URL = "/api/v1/certificates/bootstrap"

    async function getPage(page: number, serialNumberFilter: string) {

        const response = await fetch(
            `${url}${BOOTSTRAP_CERTIFICATES_URL}?page=${page}&pageSize=${BOOTSTRAP_CERTIFICATES_PER_PAGE}`,
            {
                method: "GET"
            }
        )

        const json: ApiResponse<Page<BootstrapCertificate>> = await response.json()

        if (!response.ok) {
            throw new Error(json.message)
        }

        return json.data
    }

    return {
        getPage
    }

}