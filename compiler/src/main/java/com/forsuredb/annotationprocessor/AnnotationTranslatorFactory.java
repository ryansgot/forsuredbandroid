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
