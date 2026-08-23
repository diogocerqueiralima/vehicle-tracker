package com.github.diogocerqueiralima.utils

import com.auth0.android.jwt.JWT
import com.github.diogocerqueiralima.domain.model.UserIdentity
import java.util.UUID

/**
 * Decodes the identity information from a given UserSession's identity token.
 *
 * @param token The token from which to extract user identity information.
 * @return A UserIdentity object containing the decoded user information.
 */
fun decodeIdentity(token: String): UserIdentity {

    val claims = JWT(token)

    return UserIdentity(
        id = UUID.fromString(claims.getClaim("sub").asString()),
        email = claims.getClaim("email").asString(),
        name = claims.getClaim("given_name").asString()
    )
}