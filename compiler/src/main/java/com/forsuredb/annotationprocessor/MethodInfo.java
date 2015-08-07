package com.forsuredb.annotationprocessor;

import javax.lang.model.type.TypeMirror;

/*package*/ class MethodInfo {

    private final String name;
    private final String returnTypeStr;
    private final ParameterInfo parameters;

    private MethodInfo(String name, TypeMirror returnType, ParameterInfo parameters) {
        this.name = name;
        this.returnTypeStr = returnType == null ? "void" : returnType.toString();
        this.parameters = parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getReturnTypeStr() {
        return returnTypeStr;
    }

    public ParameterInfo getParameters() {
        return parameters;
    }

    /*package*/ static class Builder {
        private String name;
        private TypeMirror returnType;
        private ParameterInfo parameters;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder returnType(TypeMirror returnType) {
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
