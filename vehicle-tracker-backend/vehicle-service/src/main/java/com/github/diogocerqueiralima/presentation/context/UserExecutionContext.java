package com.github.diogocerqueiralima.presentation.context;

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

    public static UserExecutionContext create(UUID userId) {
        return new UserExecutionContext(Type.USER, userId);
    }

}
