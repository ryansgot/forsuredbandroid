package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSTable;

import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        return true;
    }

    private void processFSTableAnnotations(RoundEnvironment roundEnv) {
        VelocityEngine ve = createVelocityEngine();

        for (Element e : roundEnv.getElementsAnnotatedWith(FSTable.class)) {
            if (e.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }

            createSetterApi((TypeElement) e, ve);
        }
    }

    private void createSetterApi(TypeElement intf, VelocityEngine ve) {
        final String intfName = intf.getSimpleName().toString();
        final String pkgName = ((PackageElement) intf.getEnclosingElement()).getQualifiedName().toString();

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "annotated interface: " + intfName + " in package " + pkgName, intf);
        CodeGenerator generator = CodeGenerator.builder().processingEnv(processingEnv)
                .velocityEngine(ve)
                .className(intfName + "Setter")
                .enclosedElements(intf.getEnclosedElements())
                .pkgName(pkgName)
                .build();
        generator.generate("setter_interface.vm");
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
