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
 *     Use the FSTable annotation on your extensions of {@link com.forsuredb.api.FSGetApi FSGetApi}
 *     in order to tell the compiler that this interface defines a table. <i>This is required.</i>
 * </p>
 * <p>
 *     Unlike most of the annotations defined in com.forsuredb.annotation, FSTable is
 *     {@link RetentionPolicy#RUNTIME retained at runtime}. However, future versions may differ, so
 *     do not depend upon this.
 * </p>
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FSTable {

   /**
    * @return The name of the table
    */
    String value();
}
