package com.fsryan.forsuredb;

/**
 * workaround for the fact that Parameterized Runner on Android 22, the input argument null is being
 * detected as a null string, causing an intantiation exception. This, for some reason, does not
 * happen on Android24+. But this is here to wrap any type so that it can be nullable without
 * defaulting to null string. Also a null String was getting detected as the String "null"
 */
public class NullableHack<T> {

    private final T value;

    private NullableHack(T value) {
        this.value = value;
    }

    public static <T> NullableHack<T> forNull() {
        return create(null);
    }

    public static <T> NullableHack<T> create(T t) {
        return new NullableHack<>(t);
    }


    public T get() {
        return value;
    }

}
