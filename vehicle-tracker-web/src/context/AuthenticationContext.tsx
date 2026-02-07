"use client"

import React, {createContext, useContext, useEffect, useMemo, useState} from "react";
import {AuthenticationMachine, Idle, Redirecting, State} from "@/domain/AuthenticationMachine";

export interface AuthenticationContextProps {

    state: State
    login: () => Promise<void>
    logout: () => Promise<void>
    refresh: () => Promise<void>

}

const AuthenticationContext = createContext<AuthenticationContextProps | null>(null)

export function useAuthentication() {

    const context = useContext(AuthenticationContext)

    if (!context) {
        throw new Error("useAuthentication must be used within an AuthenticationProvider")
    }

    return context
}

function getInitialState(): State {

    if (typeof window === 'undefined') {
        console.error("Window is undefined, returning Idle state")
        return new Idle()
    }

    const stateData = sessionStorage.getItem("state");

    if (!stateData) {
        return new Idle()
    }

    const data = JSON.parse(stateData)

    if (data.codeVerifier && data.codeChallenge && data.state) {
        return new Redirecting(data.codeVerifier, data.codeChallenge, data.state)
    }

    console.error("Could not get redirected state: " + JSON.stringify(data))
    return new Idle()
}

export default function AuthenticationProvider({ children }: { children: React.ReactNode }) {

    const [state, setState] = useState(new Idle())
    const machine = useMemo(
        () => new AuthenticationMachine(new Idle(), setState),
        []
    )
    useEffect(() => {

        const initialState = getInitialState()
        machine.setState(initialState)

        machine.handleRedirect()
            .catch(e => console.error("Error handling redirect: " + e))

    }, [machine])

    const props: AuthenticationContextProps = {
        state: state,
        login: async () => await machine.authorize(),
        logout: async () => {
            // Implement logout logic here
        },
        refresh: async () => {
            // Implement refresh logic here
        }
    }

    return (
        <AuthenticationContext.Provider value={props}>
            {children}
        </AuthenticationContext.Provider>
    )

}