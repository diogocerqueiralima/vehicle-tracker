import Redis from "ioredis";
import {UserSession} from "@/domain/UserSession";

class UserSessionRepository {

    private redis = new Redis({
        host: process.env.REDIS_HOST,
        port: parseInt(process.env.REDIS_PORT!)
    })

    /**
     *
     * Get the user session by state. If the session does not exist or has expired, it returns null.
     *
     * @param state the state of the user session
     */
    async get(state: string) {
        const data = await this.redis.get(state)
        if (data == null) return null
        return JSON.parse(data) as UserSession
    }

    /**
     *
     * Save the user session in Redis with an expiration time.
     * The session will be automatically deleted after the expiration time.
     *
     * @param session the user session to be saved
     */
    async save(session: UserSession) {
        await this.redis.set(session.id, JSON.stringify(session), "PX", session.refreshExpiresIn)
    }

}

const userSessionRepository = new UserSessionRepository()
export default userSessionRepository