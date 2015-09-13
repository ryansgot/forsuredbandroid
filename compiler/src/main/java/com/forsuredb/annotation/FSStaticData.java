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
 *     Use the FSStaticData annotation on an {@link com.forsuredb.api.FSGetApi FSGetApi} extension
 *     to direct the compiler to the static data XML asset you have prepared for this table. For an
 *     example, {@link #asset() asset()}
 * </p>
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface FSStaticData {

    /**
     * <p>
     *     <i>This string must define the asset relative to your project's assets directory, and the
     *     asset must be an XML file as below:</i>
     * </p>
     * <pre>{@code
     * <resources>
     *     <recordName column1="column1_value" column2="column2_value" column3="column3_value" />
     *     <recordName column1="column1_value" column2="column2_value" column3="column3_value" />
     * </resources>
     * }</pre>
     * @return The filename of the XML asset that defines static data for the table defined in the
     * {@link com.forsuredb.api.FSGetApi FSGetApi} extension that this FSStaticData annotation
     * annotates.
     */
    String asset();

    /**
     * @return The XML detail name whose attributes define the static data records for the table
     * defined in the {@link com.forsuredb.api.FSGetApi FSGetApi} extension that this FSStaticData
     * annotation annotates. See {@link #asset() asset()} for an example.
     */
    String recordName();
}
