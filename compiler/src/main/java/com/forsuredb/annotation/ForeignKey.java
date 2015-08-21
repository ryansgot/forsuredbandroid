package com.forsuredb.annotation;

import com.forsuredb.api.FSGetApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForeignKey {
    Class<? extends FSGetApi> apiClass();
    String columnName();
}
