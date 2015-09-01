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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class AnnotationReporter {

    private final ProcessingEnvironment processingEnv;

    public AnnotationReporter(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    private <T extends Annotation> void processAnnotations(Class<T> annotationClass, RoundEnvironment roundEnv) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(annotationClass)) {
            final T annotationInstance = elem.getAnnotation(annotationClass);
            reportAnnotationFound(annotationClass.getSimpleName(), elem);
            reportAnnotationEnclosingElements(elem);
            reportAnnotationMethods(annotationInstance, annotationClass.getSimpleName());
        }
    }

    private void reportAnnotationFound(String clsSimpleName, Element elem) {
        final String note = clsSimpleName + " annotation found in " + elem.getSimpleName();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, note);
    }

    private void reportAnnotationEnclosingElements(Element elem) {
        for (Element enclosingElement : elem.getEnclosedElements()) {
            final String note = "\tEnclosing element: " + enclosingElement.getSimpleName();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, note);
        }
    }

    private <T extends Annotation> void reportAnnotationMethods(T annotationInstance, String clsSimpleName) {
        for (Method method : annotationInstance.getClass().getDeclaredMethods()) {
            final String note = "\t" + clsSimpleName + " annotation method: " + method.getGenericReturnType() + " " + method.getName();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, note);
            reportMethodInvocation(method, annotationInstance);
        }
    }

    private <T extends Annotation> void reportMethodInvocation(Method method, T annotationInstance) {
        if (method.getParameterTypes().length == 0) {
            try {
                method.setAccessible(true);
                final String note = "\t\t" + "invocation returns: " + method.invoke(annotationInstance);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, note);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            final String note = "\t\t" + "invocation returns: cannot invoke--requires parameters of type: " + Arrays.toString(method.getGenericParameterTypes());
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, note);
        }
    }
}
