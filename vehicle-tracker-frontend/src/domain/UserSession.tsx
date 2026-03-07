export interface UserSession {

    id: string
    accessToken: string
    expiresIn: number
    refreshExpiresIn: number
    refreshToken: string
    idToken: string
    scope: string[]

}