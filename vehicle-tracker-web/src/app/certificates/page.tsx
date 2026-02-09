import DataTable, {DataTableHeader} from "@/components/DataTable";

export default function Certificates() {

    const certificates = [
        {
            id: 1,
            name: "Certificado de Segurança Veicular",
            issuedBy: "Instituto de Pesquisas Tecnológicas (IPT)",
            issueDate: "2023-01-15",
            expirationDate: "2025-01-15",
            status: "Válido"
        },
        {
            id: 2,
            name: "Certificado de Conformidade Ambiental",
            issuedBy: "Agência de Proteção Ambiental (APA)",
            issueDate: "2022-06-10",
            expirationDate: "2024-06-10",
            status: "Válido"
        },
        {
            id: 3,
            name: "Certificado de Inspeção Veicular",
            issuedBy: "Departamento de Trânsito (DETRAN)",
            issueDate: "2021-11-20",
            expirationDate: "2023-11-20",
            status: "Expirado"
        }
    ]

    const header: DataTableHeader = {
        items: [
            { name: "name", label: "Nome do Certificado" },
            { name: "issuedBy", label: "Emitido por" },
            { name: "issueDate", label: "Data de Emissão" },
            { name: "expirationDate", label: "Data de Expiração" },
            { name: "status", label: "Estado" }
        ]
    }

    return (

        <div className={`flex flex-col gap-8`}>

            <h1 className={`text-4xl font-bold`}>Certificados</h1>

            <DataTable header={header} content={certificates} />

        </div>

    )
}