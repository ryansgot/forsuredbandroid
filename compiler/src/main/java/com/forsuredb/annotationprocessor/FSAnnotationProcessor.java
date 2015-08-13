package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSTable;

import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        Set<TypeElement> tableTypes = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(FSTable.class));
        List<TableInfo> allTables = gatherTableInfo(tableTypes);
        VelocityEngine ve = createVelocityEngine();
        createSetterApis(ve, tableTypes);


        if (Boolean.getBoolean("createMigrations")) {
            createMigrations(ve, allTables);
        }
    }

    private void createMigrations(VelocityEngine ve, List<TableInfo> allTables) {
        for (TableInfo tableInfo : allTables) {
            new MigrationGenerator(tableInfo, allTables, processingEnv).generate("migration_resource.vm", ve);
        }
    }

    private void createSetterApis(VelocityEngine ve, Set<TypeElement> tableTypes) {
        for (TypeElement te : tableTypes) {
            if (te.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }

            createSetterApi(te, ve);
        }
    }

    private List<TableInfo> gatherTableInfo(Set<TypeElement> tableTypes) {
        List<TableInfo> ret = new ArrayList<>();
        for (TypeElement te : tableTypes) {
            if (te.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }

            final TableInfo table = new TableInfo(te);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, table.toString());
            ret.add(table);
        }

        return ret;
    }

    private void createSetterApi(TypeElement intf, VelocityEngine ve) {
        final String intfName = intf.getSimpleName().toString();
        final String pkgName = ((PackageElement) intf.getEnclosingElement()).getQualifiedName().toString();

        CodeGenerator.builder().processingEnv(processingEnv)
                .velocityEngine(ve)
                .className(intfName + "Setter")
                               .enclosedElements(intf.getEnclosedElements())
                               .pkgName(pkgName)
                               .build()
                               .generate("setter_interface.vm");
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
