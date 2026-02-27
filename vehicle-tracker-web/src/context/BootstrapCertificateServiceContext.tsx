import { bootstrapCertificateService } from "@/services/BootstrapCertificateService"
import React, {createContext, useContext, useMemo} from "react";

interface BootstrapCertificateServiceContextProps {
    service: ReturnType<typeof bootstrapCertificateService>
}

export interface BootstrapCertificateServiceProviderProps {
    url: string
    children: React.ReactNode
}

const BootstrapCertificateServiceContext = createContext<BootstrapCertificateServiceContextProps | null>(null)

export function useBootstrapCertificateService() {

    const context = useContext(BootstrapCertificateServiceContext)

    if (!context) {
        throw new Error(
            "useBootstrapCertificateService must be used within BootstrapCertificateServiceProvider"
        )
    }

    return context.service
}

export function BootstrapCertificateServiceProvider({children, url}: BootstrapCertificateServiceProviderProps) {

    const service = useMemo(() => {
        return bootstrapCertificateService({ url })
    }, [url])

    return (
        <BootstrapCertificateServiceContext.Provider value={{service}}>
            {children}
        </BootstrapCertificateServiceContext.Provider>
    )
}