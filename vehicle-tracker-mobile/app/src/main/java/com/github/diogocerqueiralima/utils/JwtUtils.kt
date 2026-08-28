package com.github.diogocerqueiralima.utils

import com.auth0.android.jwt.JWT
import com.github.diogocerqueiralima.domain.exceptions.InvalidIdentityTokenException
import com.github.diogocerqueiralima.domain.model.UserIdentity
import java.util.UUID

/**
 * Decodes the identity information from a given UserSession's identity token.
 *
 * @param token The token from which to extract user identity information.
 * @return A UserIdentity object containing the decoded user information.
 * @throws InvalidIdentityTokenException when the "sub" claim is missing or is not a valid UUID.
 */
fun decodeIdentity(token: String): UserIdentity {

    val claims = JWT(token)
    val sub = claims.getClaim("sub").asString()
        ?: throw InvalidIdentityTokenException("Identity token is missing the \"sub\" claim.")

    val id = try {
        UUID.fromString(sub)
    } catch (e: IllegalArgumentException) {
        throw InvalidIdentityTokenException("Identity token \"sub\" claim is not a valid UUID: $sub")
    }

    return UserIdentity(
        id = id,
        email = claims.getClaim("email").asString(),
        name = claims.getClaim("given_name").asString()
    )
}