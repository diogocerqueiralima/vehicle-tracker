export type State = Idle | Redirecting | Exchanging | Authenticated

export class Idle {}

export class Redirecting {

    codeVerifier: string
    codeChallenge: string
    state: string

    constructor(codeVerifier: string, codeChallenge: string, state: string) {
        this.codeVerifier = codeVerifier;
        this.codeChallenge = codeChallenge;
        this.state = state;
    }

}

export class Exchanging {

    codeVerifier: string
    code: string

    constructor(codeVerifier: string, code: string) {
        this.codeVerifier = codeVerifier;
        this.code = code;
    }

}

export class Authenticated {}

export class AuthenticationMachine {

    state: State
    private readonly onStateChange: (state: State) => void

    constructor(state: State = new Idle(), onStateChange: (state: State) => void) {
        this.state = state
        this.onStateChange = onStateChange
    }

    setState(state: State) {
        this.state = state
        this.onStateChange(state)
    }

    async authorize() {

        // 1. Check if the state is IDLE, if not, return

        if (!(this.state instanceof Idle)) {
            return
        }

        // 2. Generate code verifier, code challenge and state

        const codeVerifier = this.generateCodeVerifier();
        const codeChallenge = await this.generateCodeChallenge(codeVerifier);
        const state = crypto.randomUUID()

        // 3. Change state to redirecting and save into session storage

        this.state = new Redirecting(codeVerifier, codeChallenge, state)
        sessionStorage.setItem("state", JSON.stringify(
            {
                codeVerifier,
                codeChallenge,
                state
            }
        ))

        // 4. Redirect to authorize uri

        const params = new URLSearchParams({
            response_type: "code",
            client_id: process.env.NEXT_PUBLIC_CLIENT_ID!,
            redirect_uri: process.env.NEXT_PUBLIC_REDIRECT_URI!,
            scope: "openid",
            code_challenge_method: "S256",
            code_challenge: codeChallenge,
            state
        });

        window.location.href = `${process.env.NEXT_PUBLIC_AUTHORIZATION_ENDPOINT}?${params}`;
    }

    async handleRedirect() {

        // 1. Check if the state is redirecting, if not, return

        if (!(this.state instanceof Redirecting)) {
            return
        }

        // 2. Get code and state from URL, if state does not match or there is no code, return

        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get("code")
        const state = urlParams.get("state")

        if (state !== this.state.state) {
            console.error("State does not match, potential CSRF attack")
            return
        }

        if (!code) {
            console.error("Code not found in URL")
            return
        }

        // 3. Change state to exchanging and exchange code for token

        this.setState(new Exchanging(this.state.codeVerifier, code))
        await this.exchangeCodeForToken(this.state.codeVerifier, code)
    }

    private async exchangeCodeForToken(codeVerifier: string, code: string) {

        // 1. Prepare request parameters

        const params = new URLSearchParams({
            grant_type: "authorization_code",
            client_id: process.env.NEXT_PUBLIC_CLIENT_ID!,
            redirect_uri: process.env.NEXT_PUBLIC_REDIRECT_URI!,
            code_verifier: codeVerifier,
            code
        })

        // 2. Make request to token endpoint and handle response

        try {

            const response = await fetch(process.env.NEXT_PUBLIC_TOKEN_ENDPOINT!, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: params.toString()
            })

            if (!response.ok) {
                console.error("Token exchange failed: " + response.statusText)
                return
            }

            const data = await response.json()
            console.log("Token exchange successful: ", data)
            this.setState(new Authenticated())

        } catch (e) {
            console.error("Error during token exchange: " + e)
        } finally {
            sessionStorage.removeItem("state")
        }

    }

    private generateCodeVerifier() {

        const randomValues = crypto.getRandomValues(new Uint8Array(32))

        return btoa(String.fromCharCode(...randomValues))
            .replace(/\+/g, "-")
            .replace(/\//g, "_")
            .replace(/=+$/, "");
    }

    private async generateCodeChallenge(codeVerifier: string) {

        const encoder = new TextEncoder();
        const data = encoder.encode(codeVerifier);
        const hash = await crypto.subtle.digest("SHA-256", data);

        return this.arrayBufferToBase64Url(hash);
    }

    private arrayBufferToBase64Url(buffer: ArrayBuffer): string {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary)
            .replace(/\+/g, '-')
            .replace(/\//g, '_')
            .replace(/=+$/, '');
    }

}