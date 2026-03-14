"use client"

import { bootstrapCertificateService } from "@/services/BootstrapCertificateService"
import React, {createContext, useContext, useMemo} from "react";

interface BootstrapCertificateServiceContextProps {
    service: ReturnType<typeof bootstrapCertificateService>
}

export interface BootstrapCertificateServiceProviderProps {
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

export function BootstrapCertificateServiceProvider({children}: BootstrapCertificateServiceProviderProps) {

    const service = useMemo(() => {
        return bootstrapCertificateService()
    }, [])

    return (
        <BootstrapCertificateServiceContext.Provider value={{service}}>
            {children}
        </BootstrapCertificateServiceContext.Provider>
    )
}