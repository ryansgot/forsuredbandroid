package com.forsuredb.annotationprocessor;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/*package*/ class ParameterInfo {
    private final List<Type> types;
    private final List<String> names;

    private ParameterInfo(List<Type> types, List<String> names) {
        this.types = types == null ? Collections.EMPTY_LIST : types;
        this.names = names == null ? Collections.EMPTY_LIST : names;
    }

    public Type getTypeAt(int position) {
        return position >= 0 && position < types.size() ? types.get(position) : null;
    }

    public String getNameAt(int position) {
        return position >= 0 && position < names.size() ? names.get(position) : null;
    }

    public static class Builder {
        private final List<Type> types = new LinkedList<>();
        private final List<String> names = new LinkedList<>();

        public Builder addParameter(Type type, String name) {
            if (type != null && name != null) {
                types.add(type);
                names.add(name);
            }

            return this;
        }

        public ParameterInfo build() {
            return new ParameterInfo(types, names);
        }
    }
}
