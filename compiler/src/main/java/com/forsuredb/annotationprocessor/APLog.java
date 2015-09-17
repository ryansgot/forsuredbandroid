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
