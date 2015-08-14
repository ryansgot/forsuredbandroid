package com.forsuredb.annotationprocessor;

public class Caster {

    private Object uncasted;

    /*package*/ Caster(Object uncasted) {
        this.uncasted = uncasted;
    }

    public Object uncasted() {
        return uncasted;
    }

    public <T> T as(Class<T> cls) {
        return (T) uncasted;
    }
}
