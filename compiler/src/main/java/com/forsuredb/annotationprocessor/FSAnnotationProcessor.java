package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.FSTable;
import com.forsuredb.annotation.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;


/**
 * <p>
 *     Currently, this only prints output to to the console at compile time. Eventually, it will generate classes at compile time.
 * </p>
 */
@SupportedAnnotationTypes("com.forsuredb.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FSAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processAnnotations(FSTable.class, roundEnv);
        processAnnotations(FSColumn.class, roundEnv);
        processAnnotations(FSColumn.class, roundEnv);
        processAnnotations(PrimaryKey.class, roundEnv);
        return true;
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
