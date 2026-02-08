"use client"

import React, {createContext, useContext } from "react";

export interface AuthenticationContextProps {

    login: () => void
    logout: () => Promise<void>

}

const AuthenticationContext = createContext<AuthenticationContextProps | null>(null)

export function useAuthentication() {

    const context = useContext(AuthenticationContext)

    if (!context) {
        throw new Error("useAuthentication must be used within an AuthenticationProvider")
    }

    return context
}

export default function AuthenticationProvider({ children }: { children: React.ReactNode }) {

    const props: AuthenticationContextProps = {
        login: () => window.location.href = "/api/login",
        logout: async () => {
            // Implement logout logic here
        }
    }

    return (
        <AuthenticationContext.Provider value={props}>
            {children}
        </AuthenticationContext.Provider>
    )

}