import { LoginSession } from "@/domain/LoginSession"
import Redis from "ioredis";

const LOGIN_SESSION_EXPIRATION_SECONDS = 60

class LoginSessionRepository {

    private redis = new Redis({
        host: process.env.REDIS_HOST,
        port: parseInt(process.env.REDIS_PORT!)
    })

    /**
     *
     * Get the login session by state. If the session does not exist or has expired, it returns null.
     *
     * @param state the state of the login session
     */
    async get(state: string) {
        const data = await this.redis.get(state)
        if (data == null) return null
        return JSON.parse(data) as LoginSession
    }

    /**
     *
     * Save the login session in Redis with an expiration time.
     * The session will be automatically deleted after the expiration time.
     *
     * @param session the login session to be saved
     */
    async save(session: LoginSession) {
        await this.redis.set(session.state, JSON.stringify(session), "EX", LOGIN_SESSION_EXPIRATION_SECONDS)
    }

    /**
     *
     * Delete the login session from Redis. This can be used to invalidate the session after it has been used.
     *
     * @param state the state of the login session to be deleted
     */
    async delete(state: string) {
        await this.redis.del(state)
    }

}

const loginSessionRepository = new LoginSessionRepository()
export default loginSessionRepository