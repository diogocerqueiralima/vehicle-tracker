import {LoginSession} from "@/domain/LoginSession";
import CLoginSessionRepository from "@/repositories/LoginSessionRepository"
import CUserSessionRepository from "@/repositories/UserSessionRepository"
import {createHash, randomBytes} from "node:crypto";
import userSessionRepository from "@/repositories/UserSessionRepository";
import loginSessionRepository from "@/repositories/LoginSessionRepository";
import {UserSession} from "@/domain/UserSession";

class AuthenticationService {

    constructor(
        private loginSessionRepository: typeof CLoginSessionRepository,
        private userSessionRepository: typeof CUserSessionRepository
    ) {}

    async handleRedirect(state: string | null, code: string | null) {

        // 1. Validate input and retrieve session

        if (!code) {
            console.error("Code not found in URL")
            throw new Error(`Code not found in URL: ${code}`)
        }

        if (!state) {
            console.error("State not found in URL")
            throw new Error(`State not found in URL: ${state}`)
        }

        const loginSession = await this.loginSessionRepository.get(state)

        if (!loginSession) {
            console.error("Session not found for state")
            throw new Error(`Session not found for state: ${state}`)
        }

        // 2. Delete session to prevent reuse

        await this.loginSessionRepository.delete(state)

        // 3. Exchange code for tokens

        const tokenResponse = await fetch(process.env.TOKEN_URI!, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                grant_type: "authorization_code",
                client_id: process.env.CLIENT_ID!,
                redirect_uri: process.env.REDIRECT_URI!,
                code_verifier: loginSession.codeVerifier,
                code
            })
        })

        if (!tokenResponse.ok) {
            console.error("Failed to exchange code for tokens")
            throw new Error(`Token not found in URL: ${tokenResponse.ok}`)
        }

        const tokenData = await tokenResponse.json()

        // 4. Create user session and save it

        const session: UserSession = {
            id: randomBytes(16).toString("hex"),
            accessToken: tokenData.access_token,
            expiresIn: tokenData.expires_in,
            refreshExpiresIn: tokenData.refresh_expires_in,
            refreshToken: tokenData.refresh_token,
            idToken: tokenData.id_token,
            scope: tokenData.scope.split(" ")
        }

        await this.userSessionRepository.save(session)

        return session
    }

    async buildAuthorizationUrl() {

        // 1. Generate code verifier, code challenge and state

        const codeVerifier = this.generateCodeVerifier()
        const codeChallenge = await this.generateCodeChallenge(codeVerifier)
        const state = this.generateState()

        // 2. Generate session and save it

        const session: LoginSession = {
            state,
            codeVerifier
        }

        await this.loginSessionRepository.save(session)

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

        return `${process.env.AUTHORIZATION_URI}?${params.toString()}`
    }

    private generateState() {
        return randomBytes(16).toString("hex")
    }

    private generateCodeVerifier() {
        return randomBytes(32).toString("base64url")
    }

    private async generateCodeChallenge(codeVerifier: string) {
        const hash = createHash("sha256").update(codeVerifier).digest()
        return hash.toString("base64url")
    }

}

const authenticationService = new AuthenticationService(loginSessionRepository, userSessionRepository)
export default authenticationService