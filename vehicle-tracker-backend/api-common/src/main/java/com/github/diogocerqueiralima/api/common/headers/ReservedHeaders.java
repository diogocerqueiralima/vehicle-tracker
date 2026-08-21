package com.github.diogocerqueiralima.api.common.headers;

/**
 * HTTP headers reserved for user-context propagation between the api-gateway and internal
 * services. The api-gateway strips these from inbound client requests and re-injects them
 * from the validated JWT, so internal services can trust them without parsing the token.
 */
public final class ReservedHeaders {

    private ReservedHeaders() {}

    // User id header
    public static final String USER_ID = "X-User-Id";

    // User roles header
    public static final String USER_ROLES = "X-User-Roles";

    // Username header
    public static final String USER_USERNAME = "X-User-Username";

}
