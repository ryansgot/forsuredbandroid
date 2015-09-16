package com.forsuredb.annotationprocessor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

public class AnnotationTranslatorFactory {

    private static AnnotationTranslatorFactory instance;

    private ProcessingEnvironment processingEnv;

    private AnnotationTranslatorFactory(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public static AnnotationTranslatorFactory init(ProcessingEnvironment processingEnv) {
        if (instance == null) {
            instance = new AnnotationTranslatorFactory(processingEnv);
        }
        return instance;
    }

    public static AnnotationTranslatorFactory inst() {
        if (instance == null) {
            throw new IllegalStateException("Must call init() prior to calling inst()");
        }
        return instance;
    }

    public AnnotationTranslator create(AnnotationMirror am) {
        return new AnnotationTranslator(processingEnv.getElementUtils().getElementValuesWithDefaults(am));
    }
}
