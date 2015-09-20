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
package com.forsuredb.api;

/**
 * <p>
 *     Describes a WHERE clause of an SQL query in (possibly) two parts, a {@link #where() where()}
 *     String that perhaps has '?' characters to be replaced by the array of String returned by
 *     {@link #replacements() replacements()}.
 * </p>
 */
public interface FSSelection {

    class SelectAll implements FSSelection {

        @Override
        public String where() {
            return "";
        }

        @Override
        public String[] replacements() {
            return new String[0];
        }
    }

    /**
     * @return the String where clause, possibly including '?' characters to be replaced by Strings
     * in the {@link #replacements() replacements()} String array
     */
    String where();

    /**
     * @return the array of Strings that are to replace all '?' characters in String returned by
     * {@link #where() where()}.
     */
    String[] replacements();
}
