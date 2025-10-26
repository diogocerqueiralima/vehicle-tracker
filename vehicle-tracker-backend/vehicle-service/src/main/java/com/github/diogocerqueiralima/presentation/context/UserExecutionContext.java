package com.github.diogocerqueiralima.presentation.context;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class UserExecutionContext extends ExecutionContext {

    private final UUID userId;

    private UserExecutionContext(Type type, UUID userId) {
        super(type);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public static ExecutionContext create(UUID userId) {
        return new UserExecutionContext(Type.USER, userId);
    }

    public static ExecutionContext fromJwt(Jwt jwt) {
        return create(UUID.fromString(jwt.getSubject()));
    }

}
