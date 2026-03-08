import {NextRequest, NextResponse} from "next/server"
import {getToken} from "next-auth/jwt";

const BACKEND_URL = process.env.BACKEND_URL!

async function proxyRequest(req: NextRequest, context: { params: Promise<{ path: string[] }> }) {

    // 1. Extract the path and query parameters from the incoming request
    const params = await context.params
    const path = params.path.join("/")
    const url = new URL(req.url)
    const queryString = url.search

    // 2. Prepare the headers and body for the proxied request
    let body = undefined
    if (req.method !== "GET" && req.method !== "HEAD") {
        body = await req.text()
    }

    const headers: Headers = req.headers

    // 3. Get the access token from the session and add it to the headers
    const token = await getToken({req, secret: process.env.NEXTAUTH_SECRET})
    if (token) {
        headers.set('Authorization', `Bearer ${token.accessToken}`)
    }

    // 4. Make the proxied request to the backend API
    const response = await fetch(`${BACKEND_URL}/${path}${queryString}`, {
        method: req.method,
        headers,
        body: body,
    })

    // 5. Return the response from the backend API to the client
    const data = await response.text()
    return new NextResponse(data, { status: response.status, headers: response.headers })
}

export const GET = proxyRequest
export const POST = proxyRequest
export const PUT = proxyRequest
export const DELETE = proxyRequest