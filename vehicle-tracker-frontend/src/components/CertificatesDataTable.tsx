"use client"

import {ContentHeaderItem} from "@/components/datatable/DataTableContent";
import {createDataTable} from "@/components/datatable/DataTable";
import {BootstrapCertificate} from "@/domain/BootstrapCertificate";
import {useBootstrapCertificateService} from "@/context/BootstrapCertificateServiceContext";

const DataTable = createDataTable<BootstrapCertificate>()

export function CertificatesDataTable() {

    const headerItems: ContentHeaderItem<BootstrapCertificate>[] = [
        { name: "serialNumber", label: "Número de Série" },
        { name: "subject", label: "Sujeito" },
        { name: "issuedAt", label: "Data de emissão" },
        { name: "expiresAt", label: "Data de expiração" },
        {
            name: "revoked",
            label: "Revogado",
            render: (certificate) => (
                <span
                    className={`px-2 py-1 rounded-full text-xs font-medium ${
                        certificate.revoked ? "bg-negative-muted text-negative" : "bg-positive-muted text-positive"
                    }`}
                >
                    {certificate.revoked ? "Sim" : "Não"}
                </span>
            )
        },
        {
            name: "used",
            label: "Usado",
            render: (certificate) => (
                <span
                    className={`px-2 py-1 rounded-full text-xs font-medium ${
                        certificate.used ? "bg-negative-muted text-negative" : "bg-positive-muted text-positive"
                    }`}
                >
                    {certificate.used ? "Sim" : "Não"}
                </span>
            )
        }
    ]

    const bootstrapCertificateService = useBootstrapCertificateService()

    return (
        <DataTable.Root getPage={bootstrapCertificateService.getPage}>
            <DataTable.Header title={"Certificados"} />
            <DataTable.Content headerItems={headerItems} />
            <DataTable.Footer />
        </DataTable.Root>
    )

}