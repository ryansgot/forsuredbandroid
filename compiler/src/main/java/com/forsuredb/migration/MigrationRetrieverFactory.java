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
package com.forsuredb.migration;

import com.forsuredb.api.FSLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class MigrationRetrieverFactory {

    private final FSLogger log;

    public MigrationRetrieverFactory() {
        this(null);
    }

    public MigrationRetrieverFactory(FSLogger log) {
        this.log = log == null ? new FSLogger.SilentLog() : log;
    }

    public MigrationRetriever fromStream(final InputStream inputStream) {
        return new MigrationRetriever() {

            final MigrationAccumulator ma = new MigrationAccumulator();

            @Override
            public List<Migration> getMigrations() {
                if (ma.getMigrations() != null) {
                    return ma.getMigrations();
                }

                new Parser(ma, log).parseMigration(inputStream);
                return ma.getMigrations() == null ? Collections.EMPTY_LIST : ma.getMigrations();
            }
        };
    }

    public MigrationRetriever fromDirectory(final String directoryName) {
        return new DirectoryMigrationsRetriever(directoryName);
    }

    private static class MigrationAccumulator implements Parser.OnMigrationLineListener {

        private List<Migration> migrations;

        @Override
        public void onMigrationLine(Migration migration) {
            if (migrations == null) {
                migrations = new LinkedList<Migration>();
            }
            migrations.add(migration);
        }

        public List<Migration> getMigrations() {
            return migrations;
        }
    }

    private class DirectoryMigrationsRetriever implements MigrationRetriever {

        private final File directory;
        private final MigrationAccumulator ma = new MigrationAccumulator();

        public DirectoryMigrationsRetriever(String directory) {
            this.directory = directory == null ? null : new File(directory);
        }

        @Override
        public List<Migration> getMigrations() {
            if (!validDirectory()) {
                log.w("directory " + directory + " either doesn't exist or isn't a directory");
                return Collections.EMPTY_LIST;
            }

            if (ma.getMigrations() != null) {
                return ma.getMigrations();
            }

            log.i("Looking for migrations in " + directory.getPath());
            final Parser parser = new Parser(ma, log);
            final PriorityQueue<File> orderedFiles = new PriorityQueue<>(filterMigrations(directory.listFiles()));
            try {
                while (orderedFiles.size() > 0) {
                    parser.parseMigration(orderedFiles.remove());
                }
            } catch (FileNotFoundException fnfe) {
                log.e("Could not parse migrations: " + fnfe.getMessage());
            }

            return ma.getMigrations() == null ? Collections.EMPTY_LIST : ma.getMigrations();
        }

        private List<File> filterMigrations(File[] files) {
            if (files == null) {
                return Collections.EMPTY_LIST;
            }

            List<File> retList = new LinkedList<>();
            for (File f : files) {
                if (isMigration(f)) {
                    retList.add(f);
                }
            }

            return retList;
        }

        private boolean isMigration(File f) {
            return f != null && f.exists() && f.isFile() && f.getPath().endsWith("migration.xml");
        }

        private boolean validDirectory() {
            return directory != null && directory.exists() && directory.isDirectory();
        }
    }
}
