import {CertificatesDataTable} from "@/components/CertificatesDataTable";
import {BootstrapCertificateServiceProvider} from "@/context/BootstrapCertificateServiceContext";

export default function Certificates() {

    return (

        <div className={`flex flex-col gap-8`}>

            <BootstrapCertificateServiceProvider>
                <CertificatesDataTable />
            </BootstrapCertificateServiceProvider>

        </div>

    )
}