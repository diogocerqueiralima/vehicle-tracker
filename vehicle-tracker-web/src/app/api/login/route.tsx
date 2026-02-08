import {NextResponse} from "next/server";
import {LoginSession} from "@/domain/LoginSession";
import authenticationService from "@/services/AuthenticationService";

export async function GET() {

    // 1. Generate code verifier, code challenge and state

    const codeVerifier = authenticationService.generateCodeVerifier()
    const codeChallenge = await authenticationService.generateCodeChallenge(codeVerifier)
    const state = authenticationService.generateState()

    // 2. Generate session and save it

    const session: LoginSession = {
        state,
        codeVerifier
    }

    authenticationService.save(session)

    // 3. Build parameters for authorization URL

    const params = new URLSearchParams({
        response_type: "code",
        client_id: process.env.CLIENT_ID!,
        redirect_uri: process.env.REDIRECT_URI!,
        scope: "openid",
        code_challenge_method: "S256",
        code_challenge: codeChallenge,
        state
    });

    return NextResponse.redirect(`${process.env.AUTHORIZATION_URI}?${params.toString()}`)
}