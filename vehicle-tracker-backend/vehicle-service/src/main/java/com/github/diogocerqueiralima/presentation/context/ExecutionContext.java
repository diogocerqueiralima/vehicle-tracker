package com.github.diogocerqueiralima.presentation.context;

public class ExecutionContext {

    private final Type type;

    protected ExecutionContext(Type type) {
        this.type = type;
    }

    public enum Type {
        USER,
        INTERNAL
    }

    public boolean isUser() {
        return type == Type.USER;
    }

    public boolean isInternal() {
        return type == Type.INTERNAL;
    }

}
