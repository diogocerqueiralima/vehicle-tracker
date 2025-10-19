package com.github.diogocerqueiralima.presentation.context;

public class InternalExecutionContext extends ExecutionContext {

    private final String serviceName;

    private InternalExecutionContext(Type type, String serviceName) {
        super(type);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public static InternalExecutionContext create(String serviceName) {
        return new InternalExecutionContext(Type.INTERNAL, serviceName);
    }

}
