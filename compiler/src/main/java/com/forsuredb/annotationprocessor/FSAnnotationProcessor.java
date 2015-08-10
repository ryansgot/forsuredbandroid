package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSTable;

import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
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

        for (TypeElement te : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(FSTable.class))) {
            if (te.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }

            createSetterApi(te, ve);
        }

        Map<String, String> foreignTableClassNameToTableNameMap = buildForeignTableClassNameToTableNameMap(roundEnv);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "FSAnnotationProcessor: foreignTableClassNameToTableNameMap = " + foreignTableClassNameToTableNameMap.toString());

        for (TypeElement te : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(FSTable.class))) {
            if (te.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }
            createMigrations(te, ve, foreignTableClassNameToTableNameMap);
        }
    }

    private void createMigrations(TypeElement intf, VelocityEngine ve, Map<String, String> foreignTableClassNameToTableNameMap) {
        MigrationGenerator.Builder mGenBuilder = MigrationGenerator.builder().intf(intf)
                .processingEnv(processingEnv)
                .velocityEngine(ve);
        for (Map.Entry<String, String> entry : foreignTableClassNameToTableNameMap.entrySet()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "FSAnnotationProcessor: adding foreign table: " + entry.getKey() + "=" + entry.getValue());
            mGenBuilder.addForeignTable(entry.getKey(), entry.getValue());
        }
        mGenBuilder.build().generate(foreignTableClassNameToTableNameMap, "migration.vm");
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

    private Map<String, String> buildForeignTableClassNameToTableNameMap(RoundEnvironment roundEnv) {
        Map<String, String> ret = new HashMap<>();

        Set<? extends TypeElement> tableTypes = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(FSTable.class));
        for (TypeElement te : tableTypes) {
            ret.put(te.getQualifiedName().toString(), te.getAnnotation(FSTable.class).value());
        }

        return ret;
    }
}
