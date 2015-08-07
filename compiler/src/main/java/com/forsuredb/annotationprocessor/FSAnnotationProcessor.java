package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSTable;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


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
        processFSTableAnnotations(roundEnv);
//        processAnnotations(FSTable.class, roundEnv);
//        processAnnotations(FSColumn.class, roundEnv);
//        processAnnotations(FSColumn.class, roundEnv);
//        processAnnotations(PrimaryKey.class, roundEnv);
        return true;
    }

    private void processFSTableAnnotations(RoundEnvironment roundEnv) {
        VelocityEngine ve = createVelocityEngine();
//        try {
//            Template template = ve.getTemplate("setter_interface.vm");
//        } catch (ResourceNotFoundException | ParseErrorException exception) {
//            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "couldn't find template in outer method: " + );
//        }

        for (Element e : roundEnv.getElementsAnnotatedWith(FSTable.class)) {
            if (e.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }

            final TypeElement intf = (TypeElement) e;
            final String intfName = intf.getSimpleName().toString();
            final String pkgName = ((PackageElement) intf.getEnclosingElement()).getQualifiedName().toString();

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "annotated interface: " + intfName + " in package " + pkgName, e);
            CodeGenerator generator = CodeGenerator.builder().processingEnv(processingEnv)
                                                             .velocityEngine(ve)
                                                             .className(intfName + "Setter")
                                                             .enclosedElements(intf.getEnclosedElements())
                                                             .pkgName(pkgName)
                                                             .build();
            generator.generate("setter_interface.vm");
        }
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


    private VelocityEngine createVelocityEngine() {
        Properties props = new Properties();
        URL url = this.getClass().getClassLoader().getResource("velocity.properties");

        InputStream in = null;
        try {
            in = url.openStream();
            props.load(in);
        } catch (IOException | NullPointerException exception) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not load velocity.properties:" + exception.getMessage());
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }

        VelocityEngine ve = new VelocityEngine(props);
        ve.init();

        return ve;
    }
}
