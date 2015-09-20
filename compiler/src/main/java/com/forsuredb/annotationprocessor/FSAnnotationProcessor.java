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

/**
 * <p>
 *     FSAnnotationProcessor is the guts of the forsuredbcompiler project. When you compile, the
 *     {@link #process(Set, RoundEnvironment)} gets called, and the annotation processing begins.
 * </p>
 * @author Ryan Scott
 */
@SupportedAnnotationTypes("com.forsuredb.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FSAnnotationProcessor extends AbstractProcessor {

    private static final String LOG_TAG = FSAnnotationProcessor.class.getSimpleName();

    private static boolean setterApisCreated = false;          // <-- maintain state so setter APIs don't have to be created more than once
    private static boolean migrationsCreated = false;          // <-- maintain state so migrations don't have to be created more than once
    private static boolean tableCreatorClassCreated = false;   // <-- maintain state so TableCreator class does not have to be created more than once
    private static boolean finderClassesCreated = false;       // <-- maintain state so finder classes don't have to be created more than once
    private static boolean forSureClassCreated = false;        // <-- maintain state so ForSure doesn't have to be created more than once

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<TypeElement> tableTypes = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(FSTable.class));
        if (tableTypes == null || tableTypes.size() == 0) {
            return true;
        }

        APLog.init(processingEnv);
        AnnotationTranslatorFactory.init(processingEnv);

        APLog.i(LOG_TAG, "Running FSAnnotationProcessor");
        processFSTableAnnotations(tableTypes);

        return true;
    }

    private void processFSTableAnnotations(Set<TypeElement> tableTypes) {
        ProcessingContext pc = new ProcessingContext(tableTypes);
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
        if (!finderClassesCreated) {
            createFinderClasses(ve, pc);
        }
        if (!forSureClassCreated) {
            createForSureClass(ve, pc);
        }
    }

    private void createSetterApis(VelocityEngine ve, ProcessingContext pc) {
        String resultParameter = System.getProperty("resultParameter");
        for (TableInfo tableInfo : pc.allTables()) {
            new SetterGenerator(tableInfo, resultParameter, processingEnv).generate(ve);
        }
        setterApisCreated = true;   // <-- maintain state so setter APIs don't have to be created more than once
    }

    private void createMigrations(VelocityEngine ve, ProcessingContext pc) {
        String migrationDirectory = System.getProperty("migrationDirectory");
        APLog.i(LOG_TAG, "got migration directory: " + migrationDirectory);
        new MigrationGenerator(pc, migrationDirectory, processingEnv).generate(ve);
        migrationsCreated = true;   // <-- maintain state so migrations don't have to be created more than once
    }

    private void createTableCreatorClass(VelocityEngine ve, ProcessingContext pc) {
        String applicationPackageName = System.getProperty("applicationPackageName");
        APLog.i(LOG_TAG, "got applicationPackageName: " + applicationPackageName);
        new TableCreatorGenerator(processingEnv, applicationPackageName, pc).generate(ve);
        tableCreatorClassCreated = true;    // <-- maintain state so TableCreator class does not have to be created more than once
    }

    private void createFinderClasses(VelocityEngine ve, ProcessingContext pc) {
        String resultParameter = System.getProperty("resultParameter");
        for (TableInfo tableInfo : pc.allTables()) {
            new FinderGenerator(tableInfo, resultParameter, processingEnv).generate(ve);
        }
        finderClassesCreated = true;    // <-- maintain state so finder classes don't have to be created more than once
    }

    private void createForSureClass(VelocityEngine ve, ProcessingContext pc) {
        String resultParameter = System.getProperty("resultParameter");
        String applicationPackageName = System.getProperty("applicationPackageName");
        new ForSureGenerator(pc.allTables(), applicationPackageName, resultParameter, processingEnv).generate(ve);
        forSureClassCreated = true; // <-- maintain state so ForSure doesn't have to be created more than once
    }

    private VelocityEngine createVelocityEngine() {
        Properties props = new Properties();
        URL url = this.getClass().getClassLoader().getResource("velocity.properties");

        InputStream in = null;
        try {
            in = url.openStream();
            props.load(in);
        } catch (IOException | NullPointerException exception) {
            APLog.e(LOG_TAG, "Could not load velocity.properties:" + exception.getMessage());
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
