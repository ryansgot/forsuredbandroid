package com.forsuredb.migration;

import com.forsuredb.FSLogger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class ParserTest {

    private static final String TEST_RES = "src/test/resources";

    private Parser.OnMigrationLineListener omll;
    private Parser parser;

    private String migrationFile;
    private int numMigrations;

    public ParserTest(String migrationFile, int numMigrations) {
        this.migrationFile = migrationFile;
        this.numMigrations = numMigrations;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"create_table_migration.xml", 1},
                {"alter_table_add_column_migration.xml", 1},
                {"alter_table_add_foreign_key_migration.xml", 9}
        });
    }

    @Before
    public void setUp() {
        omll = Mockito.mock(Parser.OnMigrationLineListener.class);
        parser = new Parser(omll, new FSLogger.DefaultFSLogger());
    }

    @Test
    public void testParserFindsEachMigrationLine() throws Exception {
        parser.parseMigration(TEST_RES + File.separator + migrationFile);
        Mockito.verify(omll, Mockito.times(numMigrations)).onMigrationLine(Mockito.any(Migration.class));
    }
}
