package com.forsuredb.migration;

import java.util.List;

public interface MigrationRetriever {
    List<Migration> getMigrations();
}
