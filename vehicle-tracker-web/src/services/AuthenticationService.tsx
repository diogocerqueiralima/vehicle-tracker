import {LoginSession} from "@/domain/LoginSession";
import repository from "@/repositories/AuthenticationRepository"
import {createHash, randomBytes} from "node:crypto";

class AuthenticationService {

    constructor(private authenticationRepository: typeof repository) {}

    save(session: LoginSession) {
        this.authenticationRepository.save(session)
    }

    get(state: string): LoginSession | undefined {
        return this.authenticationRepository.get(state)
    }

    generateState() {
        return randomBytes(16).toString("hex")
    }

    generateCodeVerifier() {
        return randomBytes(32).toString("base64url")
    }

    async generateCodeChallenge(codeVerifier: string) {
        const hash = createHash("sha256").update(codeVerifier).digest()
        return hash.toString("base64url")
    }

}

const authenticationService = new AuthenticationService(repository)
export default authenticationService