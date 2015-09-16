/*
   forsuredb, an object relational mapping tool

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.forsuredb.annotation;

import com.forsuredb.api.FSGetApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     Use the ForeignKey annotation on methods in your {@link FSGetApi FSGetApi} extension in order
 *     to indicate that a column is a foreign key to another table, defined by
 *     {@link #apiClass() apiClass()}, on its column, defined by {@link #columnName() columName()}.
 * </p>
 * <p>
 *     Note that there is currently a limitation that you can only put one ForeignKey annotation per
 *     {@link FSGetApi FSGetApi} extension per time that you run ./gradlew dbmigrate. Unfortunately,
 *     too, this will not generate a compilation error. You'll have to wait until your database
 *     migrations occur for a {@link RuntimeException RuntimeException} to get generated.
 * </p>
 *
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ForeignKey {

    /**
     * @return the {@link FSGetApi FSGetApi} class that defines the table to which this
     * {@link ForeignKey ForeignKey} points
     */
    Class<? extends FSGetApi> apiClass();

    /**
     * @return The name of the column in the table, defined by {@link #apiClass() apiClass()}, to
     * which this {@link ForeignKey ForeignKey} points
     */
    String columnName();

    /**
     * <p>
     *     default behavior is to cascade updates
     * </p>
     * @return true if updates should be cascaded; false if updates should not be cascaded
     */
    boolean cascadeUpdate() default true;

    /**
     * <p>
     *     default behavior is to cascade deletes
     * </p>
     * @return true if deletes should be cascaded; false if deletes should not be cascaded
     */
    boolean cascadeDelete() default true;
}
