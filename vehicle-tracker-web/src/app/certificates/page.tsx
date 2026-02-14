"use client"

import {createDataTable} from "@/components/datatable/DataTable";
import {ContentHeaderItem} from "@/components/datatable/DataTableContent";

enum CertificateStatus {

    VALID = "Válido",
    EXPIRED = "Expirado"

}

interface Certificate {

    id: number
    name: string
    issuedBy: string
    issueDate: string
    expirationDate: string
    status: CertificateStatus

}

const DataTable = createDataTable<Certificate>()

export default function Certificates() {

    const headerItems: ContentHeaderItem<Certificate>[] = [
        { name: "name", label: "Nome" },
        { name: "issuedBy", label: "Emitido por" },
        { name: "issueDate", label: "Data de emissão" },
        { name: "expirationDate", label: "Data de expiração" },
        {
            name: "status",
            label: "Status",
            render: (certificate) => (
                <span
                    className={`px-2 py-1 rounded-full text-xs font-medium ${
                        certificate.status === CertificateStatus.VALID ? "bg-positive-muted text-positive" : "bg-negative-muted text-negative"
                    }`}
                >
                    {certificate.status}
                </span>
            )
        }
    ]

    const items: Certificate[] = [

        {
            id: 1,
            name: "Certificado de Segurança",
            issuedBy: "Certificadora XYZ",
            issueDate: "2023-01-15",
            expirationDate: "2024-01-15",
            status: CertificateStatus.VALID
        },
        {
            id: 2,
            name: "Certificado de Conformidade",
            issuedBy: "Certificadora ABC",
            issueDate: "2022-05-10",
            expirationDate: "2023-05-10",
            status: CertificateStatus.EXPIRED
        }

    ]

    return (

        <div className={`flex flex-col gap-8`}>

            <DataTable.Root getPage={() => Promise.resolve({items: items, totalPages: 1, totalItems: 2, currentPage: 1})}>
                <DataTable.Header title={"Certificados"} />
                <DataTable.Content headerItems={headerItems} />
                <DataTable.Footer />
            </DataTable.Root>

        </div>

    )
}