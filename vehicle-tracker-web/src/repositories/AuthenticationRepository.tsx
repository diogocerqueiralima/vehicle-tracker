import { LoginSession } from "@/domain/LoginSession"

class AuthenticationRepository {

    private sessions = new Map<string, LoginSession>()

    get(state: string) {
        return this.sessions.get(state)
    }

    save(session: LoginSession) {
        this.sessions.set(session.state, session)
    }

    delete(state: string) {
        this.sessions.delete(state)
    }

}


const authenticationRepository = new AuthenticationRepository()
export default authenticationRepository