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
 *     Use the ForeignKey annotation on methods in your {@link FSGetApi} extension in order
 *     to indicate that a column is a foreign key to another table, defined by
 *     {@link #apiClass()}, on its column, defined by {@link #columnName()}. The default
 *     {@link ChangeAction} is CASCADE.
 * </p>
 * <p>
 *     Note that there is currently a limitation that you can only put one ForeignKey annotation per
 *     {@link FSGetApi} extension per time that you run ./gradlew dbmigrate. Unfortunately,
 *     too, this will not generate a compilation error. You'll have to wait until your database
 *     migrations occur for a {@link RuntimeException RuntimeException} to get generated.
 * </p>
 *
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ForeignKey {

    enum ChangeAction {
        NO_ACTION(0), RESTRICT(1), SET_NULL(2), SET_DEFAULT(3), CASCADE(4);

        private int value;

        ChangeAction(int value) {
            this.value = value;
        }

        public static ChangeAction from(String changeAction) {
            if (changeAction == null || changeAction.isEmpty()) {
                return NO_ACTION;
            }
            for (ChangeAction action : ChangeAction.values()) {
                if (isStringMatch(action, changeAction)) {
                    return action;
                }
            }
            return NO_ACTION;
        }

        @Override
        public String toString() {
            return name().replace("_", " ");
        }

        public int getValue() {
            return value;
        }

        private static boolean isStringMatch(ChangeAction action, String string) {
            return action.toString().equalsIgnoreCase(string) || action.name().equalsIgnoreCase(string);
        }
    }

    /**
     * @return the {@link FSGetApi} class that defines the table to which this
     * {@link ForeignKey} points
     */
    Class<? extends FSGetApi> apiClass();

    /**
     * @return The name of the column in the table, defined by {@link #apiClass() apiClass()}, to
     * which this {@link ForeignKey} points
     */
    String columnName();

    /**
     * <p>
     *     default behavior is to cascade updates by returning {@link ChangeAction#CASCADE} unless
     *     otherwise specified.
     * </p>
     * @return the {@link ChangeAction} that should take place if the foreign key column is updated
     */
    ChangeAction updateAction() default ChangeAction.CASCADE;

    /**
     * <p>
     *     default behavior is to cascade deletes by returning {@link ChangeAction#CASCADE} unless
     *     otherwise specified.
     * </p>
     * @return the {@link ChangeAction} that should take place if the foreign key record is deleted
     */
    ChangeAction deleteAction() default ChangeAction.CASCADE;
}
