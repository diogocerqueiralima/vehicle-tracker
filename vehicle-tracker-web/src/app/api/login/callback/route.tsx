import {NextResponse} from "next/server";
import authenticationService from "@/services/AuthenticationService";

export async function GET(request: Request) {

    // 1. Get code and state from URL, if state does not match or there is no code, return

    const url = new URL(request.url)
    const code = url.searchParams.get("code")
    const state = url.searchParams.get("state")

    if (!code) {
        console.error("Code not found in URL")
        return NextResponse.redirect("http://localhost:3000")
    }

    if (!state) {
        console.error("State not found in URL")
        return NextResponse.redirect("http://localhost:3000")
    }

    const session = authenticationService.get(state)

    if (!session) {
        console.error("Session not found for state")
        return NextResponse.redirect("http://localhost:3000")
    }

    // 2. Exchange code for tokens

    const tokenResponse = await fetch(process.env.TOKEN_URI!, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({
            grant_type: "authorization_code",
            client_id: process.env.CLIENT_ID!,
            redirect_uri: process.env.REDIRECT_URI!,
            code_verifier: session.codeVerifier,
            code
        })
    })

    if (!tokenResponse.ok) {
        console.error("Failed to exchange code for tokens")
        return NextResponse.redirect("http://localhost:3000")
    }

    const tokenData = await tokenResponse.json()

    // 3. Save tokens in session or database (not implemented here)

    console.log("Tokens received:", tokenData)

    return NextResponse.redirect("http://localhost:3000")
}