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

    private final String migrationDirectory;
    private final Parser parser;
    private final MigrationParseLogger log;
    private final List<Migration> migrations = new LinkedList<>();

    /**
     * <p>
     *     MigrationRetriever which performs no logging
     * </p>
     * @param migrationDirectory
     */
    public MigrationRetriever(String migrationDirectory) {
        this(migrationDirectory, null);
    }

    /**
     * <p>
     *     MigrationRetriever which performs logging if log is not null
     * </p>
     * @param migrationDirectory
     * @param log
     */
    public MigrationRetriever(String migrationDirectory, MigrationParseLogger log) {
        this.migrationDirectory = migrationDirectory;
        this.log = log == null ? new MigrationParseLogger.SilentLog() : log;
        this.parser = new Parser(new Parser.OnMigrationLineListener() {
            @Override
            public void onMigrationLine(Migration migration) {
                migrations.add(migration);
            }
        }, log);
    }

    public static MigrationRetriever defaultRetriever(String migrationDirectory) {
        return new MigrationRetriever(migrationDirectory, new MigrationParseLogger.DefaultLogger());
    }

    public List<Migration> orderedMigrations() {
        log.i("Looking for migrations in " + migrationDirectory);
        File migrationDir = new File(migrationDirectory);
        if (!migrationDir.exists()) {
            log.e("Input migration directory did not exist--cannot use old migrations!");
            return Collections.EMPTY_LIST;
        }
        if (!migrationDir.isDirectory()) {
            log.e("Input migration directory is not a directory--cannot use old migrations!");
            return Collections.EMPTY_LIST;
        }

        final PriorityQueue<File> orderedMigrationFiles = createOrderedMigrationFiles(migrationDir);
        final List<Migration> retList = new LinkedList<>();
        while (orderedMigrationFiles.size() > 0) {
            parser.parseMigrationFile(orderedMigrationFiles.remove());
        }

        return retList;
    }

    private PriorityQueue<File> createOrderedMigrationFiles(File migrationDir) {
        PriorityQueue<File> retQueue = new PriorityQueue<>();
        for (File f : migrationDir.listFiles()) {
            if (!isMigration(f)) {
                continue;
            }

            log.i("found migration: " + f.getName());
            retQueue.add(f);
        }
        return retQueue;
    }

    private boolean isMigration(File f) {
        return f.exists() && f.isFile() && f.getPath().endsWith("migration.xml");
    }
}
