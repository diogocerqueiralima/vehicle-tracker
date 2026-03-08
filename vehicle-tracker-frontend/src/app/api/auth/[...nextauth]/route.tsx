import NextAuth, {AuthOptions} from "next-auth"
import KeycloakProvider from "next-auth/providers/keycloak";

export const authOptions: AuthOptions = {
    providers: [
        KeycloakProvider({
            clientId: process.env.KEYCLOAK_CLIENT_ID!,
            clientSecret: "",
            issuer: process.env.KEYCLOAK_ISSUER_URL!
        })
    ],
    secret: process.env.NEXTAUTH_SECRET,
    session: { strategy: "jwt" },
    callbacks: {
        async jwt({ token, account }) {

            if (account) {
                token.accessToken = account.access_token
                token.refreshToken = account.refresh_token
            }

            return token
        },
        async session({ session }) {
            return session
        }
    }
}

const handler = NextAuth(authOptions)

export { handler as GET, handler as POST }