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

    private static boolean setterApisCreated = false;          // <-- maintain state so setter APIs don't have to be created more than once
    private static boolean migrationsCreated = false;          // <-- maintain state so migrations don't have to be created more than once
    private static boolean tableCreatorClassCreated = false;   // <-- maintain state so TableCreator class does not have to be created more than once

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<TypeElement> tableTypes = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(FSTable.class));
        if (tableTypes == null || tableTypes.size() == 0) {
            return true;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Running FSAnnotationProcessor.process");
        processFSTableAnnotations(tableTypes);
        return true;
    }

    private void processFSTableAnnotations(Set<TypeElement> tableTypes) {
        ProcessingContext pc = new ProcessingContext(tableTypes, processingEnv);
        VelocityEngine ve = createVelocityEngine();

        if (!setterApisCreated) {
            createSetterApis(ve, pc);
        }
        if (!migrationsCreated && Boolean.getBoolean("createMigrations")) {
            createMigrations(ve, pc);
        }
        if (!tableCreatorClassCreated) {
            createTableCreatorClass(ve, pc);
        }
    }

    private void createSetterApis(VelocityEngine ve, ProcessingContext pc) {
        String resultParameter = System.getProperty("resultParameter");
        for (TableInfo tableInfo : pc.allTables()) {
            new SetterGenerator(tableInfo, resultParameter, processingEnv).generate("setter_interface.vm", ve);
        }
        setterApisCreated = true;   // <-- maintain state so setter APIs don't have to be created more than once
    }

    private void createMigrations(VelocityEngine ve, ProcessingContext pc) {
        String migrationDirectory = System.getProperty("migrationDirectory");
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "got migration directory: " + migrationDirectory);
        new MigrationGenerator(pc, migrationDirectory, processingEnv).generate("migration_resource.vm", ve);
        migrationsCreated = true;   // <-- maintain state so migrations don't have to be created more than once
    }

    private void createTableCreatorClass(VelocityEngine ve, ProcessingContext pc) {
        String applicationPackageName = System.getProperty("applicationPackageName");
        new TableCreatorGenerator(processingEnv, applicationPackageName, pc).generate("table_creator.vm", ve);
        tableCreatorClassCreated = true;    // <-- maintain state so TableCreator class does not have to be created more than once
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
