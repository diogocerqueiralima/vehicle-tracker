package com.github.diogocerqueiralima.presentation.context;

public class InternalExecutionContext extends ExecutionContext {

    private InternalExecutionContext(Type type) {
        super(type);
    }

    public static InternalExecutionContext create() {
        return new InternalExecutionContext(Type.INTERNAL);
    }

}
