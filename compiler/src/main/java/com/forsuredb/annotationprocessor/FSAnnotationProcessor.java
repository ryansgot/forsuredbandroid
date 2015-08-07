package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.FSTable;
import com.forsuredb.annotation.PrimaryKey;

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
        for (Element e : roundEnv.getElementsAnnotatedWith(FSTable.class)) {
            if (e.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }

            TypeElement intf = (TypeElement) e;
            PackageElement pkg = (PackageElement) intf.getEnclosingElement();

            String fqIntfName = intf.getQualifiedName().toString();
            String intfName = intf.getSimpleName().toString();
            String pkgName = pkg.getQualifiedName().toString();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "annotated interface: " + intfName + " in package " + pkgName, e);

            if (fqIntfName != null) {

                Properties props = new Properties();
                URL url = this.getClass().getClassLoader().getResource("velocity.properties");
                InputStream in = null;
                try {
                    in = url.openStream();
                    props.load(in);
                } catch (IOException | NullPointerException exception) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not load velocity.properties:" + exception.getMessage(), e);
                    return;
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

                VelocityContext vc = new VelocityContext();

                vc.put("className", intfName + "Setter");
                vc.put("packageName", pkgName);

                Template vt = ve.getTemplate("setter_interface.vm");

                Writer writer = null;
                try {
                    JavaFileObject jfo = processingEnv.getFiler().createSourceFile(fqIntfName + "Setter");
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "creating source file: " + fqIntfName + "Setter");
                    writer = jfo.openWriter();
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "applying velocity template: " + vt.getName());
                    vt.merge(vc, writer);
                } catch (IOException | ResourceNotFoundException | ParseErrorException | MethodInvocationException exception) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not output to " + intfName + "Setter.java: " + exception.getMessage(), e);
                    return;
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException ioe) {
                            // do nothing
                        }
                    }
                }
            }
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

//    private void doit() {
//        String fqClassName = null;
//        String className = null;
//        String packageName = null;
//        Map<String, VariableElement> fields = new HashMap<String, VariableElement>();
//        Map<String, ExecutableElement> methods = new HashMap<String, ExecutableElement>();
//
//        for (Element e : roundEnv.getElementsAnnotatedWith(BeanInfo.class)) {
//
//            if (e.getKind() == ElementKind.CLASS) {
//
//                TypeElement classElement = (TypeElement) e;
//                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
//
//                processingEnv.getMessager().printMessage(
//                        Diagnostic.Kind.NOTE,
//                        "annotated class: " + classElement.getQualifiedName(), e);
//
//                fqClassName = classElement.getQualifiedName().toString();
//                className = classElement.getSimpleName().toString();
//                packageName = packageElement.getQualifiedName().toString();
//
//            } else if (e.getKind() == ElementKind.FIELD) {
//
//                VariableElement varElement = (VariableElement) e;
//
//                processingEnv.getMessager().printMessage(
//                        Diagnostic.Kind.NOTE,
//                        "annotated field: " + varElement.getSimpleName(), e);
//
//                fields.put(varElement.getSimpleName().toString(), varElement);
//
//            } else if (e.getKind() == ElementKind.METHOD) {
//
//                ExecutableElement exeElement = (ExecutableElement) e;
//
//                processingEnv.getMessager().printMessage(
//                        Diagnostic.Kind.NOTE,
//                        "annotated method: " + exeElement.getSimpleName(), e);
//
//                methods.put(exeElement.getSimpleName().toString(), exeElement);
//            }
//        }
//    }
}
