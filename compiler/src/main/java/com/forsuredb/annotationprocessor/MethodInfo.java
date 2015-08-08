package com.forsuredb.annotationprocessor;

import javax.lang.model.type.TypeMirror;

/*package*/ class MethodInfo {

    private final String name;
    private final String columnName;
    private final TypeMirror returnType;
    private final ParameterInfo parameters;

    private MethodInfo(String name, String columnName, TypeMirror returnType, ParameterInfo parameters) {
        this.name = name;
        this.columnName = columnName;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getColumnName() {
        return columnName;
    }

    public TypeMirror getReturnType() {
        return returnType;
    }

    public ParameterInfo getParameters() {
        return parameters;
    }

    /*package*/ static class Builder {
        private String name;
        private String columnName;
        private TypeMirror returnType;
        private ParameterInfo parameters;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder columnName(String columnName) {
            this.columnName = columnName;
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
            return new MethodInfo(name, columnName, returnType, parameters);
        }
    }
}
