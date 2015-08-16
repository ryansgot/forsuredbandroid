package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSTable;

import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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
        List<TableInfo> allTables = new TableContextCreator(tableTypes).createTableInfo(processingEnv);
        VelocityEngine ve = createVelocityEngine();

        createSetterApis(ve, allTables);
        if (Boolean.getBoolean("createMigrations")) {
            new MigrationGenerator(allTables, processingEnv).generate("migration_resource.vm", ve);
        }
    }

    private void createSetterApis(VelocityEngine ve, List<TableInfo> allTables) {
        String resultParameter = System.getProperty("resultParameter");
        for (TableInfo tableInfo : allTables) {
            new SetterGenerator(tableInfo, resultParameter, processingEnv).generate("setter_interface.vm", ve);
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
