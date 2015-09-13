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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     Use the FSColumn annotation on methods defined in your extensions of
 *     {@link com.forsuredb.api.FSGetApi FSGetApi} in order to specify the column name associated
 *     with the method.
 * </p>
 * <p>
 *     Note that <i>It is NOT required to use this annotation</i>. However, it is suggested as
 *     Java convention around method names usually differs from convention around column names in
 *     database tables. Unlike most of the annotations defined in com.forsuredb.annotation, FSColumn
 *     is {@link RetentionPolicy#RUNTIME retained at runtime}. However, future versions may differ.
 * </p>
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FSColumn {
    /**
     * @return the name of the column
     */
    String value();
}
