package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.MigrationParseLogger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

/*package*/ class MigrationReadLog implements MigrationParseLogger {

    private ProcessingEnvironment processingEnv;

    /*package*/ MigrationReadLog(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public void e(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    @Override
    public void i(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Override
    public void w(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);

    }

    @Override
    public void o(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, message);
    }
}
