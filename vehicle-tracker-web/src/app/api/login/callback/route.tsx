import {NextResponse} from "next/server";
import authenticationService from "@/services/AuthenticationService";

export async function GET(request: Request) {

    const url = new URL(request.url)
    const code = url.searchParams.get("code")
    const state = url.searchParams.get("state")

    try {

        const session = await authenticationService.handleRedirect(state, code)
        const response = NextResponse.redirect("http://localhost:3000")

        response.cookies.set({
            name: "my_tracker_session",
            value: session.id,
            httpOnly: true,
            secure: true,
            sameSite: "strict",
            maxAge: session.refreshExpiresIn
        })

        return response

    } catch (error) {
        console.error(error)
        return NextResponse.redirect("http://localhost:3000")
    }

}