package com.forsuredb.migration;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * <p>
 *     Retrieves all Migrations from the *migration.xml files in the directory listed. Subdirectories are
 *     not searched for migration files.
 * </p>
 */
public class MigrationRetriever {

    public interface FileRetriever {
        List<File> files();
        File migrationDirectory();
    }

    private final FileRetriever fr;
    private final MigrationParseLogger log;
    private List<Migration> migrations;

    /**
     * <p>
     *     MigrationRetriever which performs no logging
     * </p>
     * @param fr
     */
    public MigrationRetriever(FileRetriever fr) {
        this(fr, null);
    }

    /**
     * <p>
     *     MigrationRetriever which performs logging if log is not null
     * </p>
     * @param fr
     * @param log
     */
    public MigrationRetriever(FileRetriever fr, MigrationParseLogger log) {
        if (fr == null) {
            throw new IllegalStateException("FileRetriever cannot be null");
        }
        this.fr = fr;
        this.log = log == null ? new MigrationParseLogger.SilentLog() : log;
    }

    public static MigrationRetriever defaultRetriever(FileRetriever fr) {
        return new MigrationRetriever(fr, new MigrationParseLogger.DefaultLogger());
    }

    public List<Migration> orderedMigrations() {
        if (migrations == null) {
            createMigrations();
        }
        return migrations;
    }

    private void createMigrations() {
        log.i("Looking for migrations in " + fr.migrationDirectory());
        migrations = new LinkedList<>();
        Parser parser = new Parser(new Parser.OnMigrationLineListener() {
            @Override
            public void onMigrationLine(Migration migration) {
                migrations.add(migration);
            }
        }, log);

        final PriorityQueue<File> orderedMigrationFiles = new PriorityQueue<>(filterMigrations(fr.files()));
        while (orderedMigrationFiles.size() > 0) {
            parser.parseMigrationFile(orderedMigrationFiles.remove());
        }
    }

    private List<File> filterMigrations(List<File> files) {
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
}
