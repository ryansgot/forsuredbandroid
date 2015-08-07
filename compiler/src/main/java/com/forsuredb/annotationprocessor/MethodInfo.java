package com.forsuredb.annotationprocessor;

import java.lang.reflect.Type;

/*package*/ class MethodInfo {

    private final String name;
    private final Type returnType;
    private final ParameterInfo parameters;

    private MethodInfo(String name, Type returnType, ParameterInfo parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public ParameterInfo getParameters() {
        return parameters;
    }

    private static class Builder {
        private String name;
        private Type returnType;
        private ParameterInfo parameters;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder returnType(Type returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder parameters(ParameterInfo parameters) {
            this.parameters = parameters;
            return this;
        }

        public MethodInfo build() {
            return new MethodInfo(name, returnType, parameters);
        }
    }
}
