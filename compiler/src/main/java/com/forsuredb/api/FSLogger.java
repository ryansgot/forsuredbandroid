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
 *     Interface for loggers used by the forsuredbcompiler
 * </p>
 * @author Ryan Scott
 */
public interface FSLogger {

    SilentLog SILENT_LOG = new SilentLog();

    void e(String message);
    void i(String message);
    void w(String message);
    void o(String message);

    /**
     * <p>
     *     Does nothing.
     * </p>
     */
    class SilentLog implements FSLogger {
        public void e(String message) {}
        public void i(String message) {}
        public void w(String message) {}
        public void o(String message) {}
    }

    /**
     * <p>
     *     Prints log messages to {@link System#out System.out}
     * </p>
     */
    class DefaultFSLogger implements FSLogger {

        //TODO: make a constructor that has a tag and replace the literal

        @Override
        public void e(String message) {
            System.out.println("[MIGRATION_PARSER_ERROR]: " + message);
        }

        @Override
        public void i(String message) {
            System.out.println("[MIGRATION_PARSER_INFO]: " + message);
        }

        @Override
        public void w(String message) {
            System.out.println("[MIGRATION_PARSER_WARNING]: " + message);

        }

        @Override
        public void o(String message) {
            System.out.println("[MIGRATION_PARSER_OTHER]: " + message);
        }
    }
}
