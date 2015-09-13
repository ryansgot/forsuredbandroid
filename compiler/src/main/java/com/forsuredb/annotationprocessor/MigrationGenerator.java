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

import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.MigrationRetriever;
import com.forsuredb.migration.MigrationRetrieverFactory;
import com.forsuredb.migration.QueryGenerator;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * <p>
 *     Used for generating the appropriate migration XML resource corresponding to the difference
 *     between the accumulated migrations in the migration directory used to instantiate this object
 *     and the {@link ProcessingContext ProcessingContext} used to instantiate the object.
 * </p>
 * @author Ryan Scott
 */
/*package*/ class MigrationGenerator extends BaseGenerator<FileObject> {

    private final Date date;
    private final ProcessingContext pContext;
    private final MigrationRetriever mr;

    public MigrationGenerator(ProcessingContext pContext, String migrationDirectory, ProcessingEnvironment processingEnv)  {
            super(processingEnv);
            date = new Date();
            this.pContext = pContext;
            mr = new MigrationRetrieverFactory().fromDirectory(migrationDirectory);
    }

    @Override
    protected FileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return new ResourceCreator(getRelativeFileName()).create(processingEnv);
    }

    @Override
    protected VelocityContext createVelocityContext() {
        PriorityQueue<QueryGenerator> queryGenerators = new DiffGenerator(new MigrationContext(mr), getProcessingEnv()).analyzeDiff(pContext);
        printMessage(Diagnostic.Kind.NOTE, "queryGenrators.size() = " + queryGenerators.size());
        if (queryGenerators.size() == 0) {
            return null;
        }

        List<String> migrationXmlList = new ArrayList<>(new XmlGenerator(determineVersion(), queryGenerators).generate());

        VelocityContext vc = new VelocityContext();
        vc.put("baseTag", "migrations");
        vc.put("migrationXmlList", migrationXmlList);
        return vc;
    }

    private int determineVersion() {
        int version = 1;

        for (Migration m : mr.getMigrations()) {
            if (m.getDbVersion() >= version) {
                version = m.getDbVersion() + 1;
            }
        }

        return version;
    }

    private String getRelativeFileName() {
        return date.getTime() + ".migration";
    }
}
