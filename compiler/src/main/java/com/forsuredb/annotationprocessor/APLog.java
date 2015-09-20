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
package com.forsuredb.annotationprocessor;

import javax.annotation.processing.ProcessingEnvironment;

public class APLog {

    private static final String LOG_TAG = APLog.class.getSimpleName();

    private static ProcessingEnvLogger log;

    public static void init(ProcessingEnvironment processingEnv) {
        if (log == null) {
            log = new ProcessingEnvLogger(processingEnv);
            i(LOG_TAG, "initialized APLog");
        }
    }

    public static void e(String tag, String message) {
        log.e(combine(tag, message));
    }

    public static void i(String tag, String message) {
        log.i(combine(tag, message));
    }

    public static void o(String tag, String message) {
        log.o(combine(tag, message));
    }

    public static void w(String tag, String message) {
        log.w(combine(tag, message));
    }

    private static String combine(String tag, String message) {
        return "[" + tag + "] " + message;
    }
}
