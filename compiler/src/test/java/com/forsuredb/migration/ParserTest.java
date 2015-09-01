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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;

/**
 * <p>
 *     Test whether the correct amount of migrations is parsed from one of the files in the test resources directory
 * </p>
 */
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
                {
                        "create_table_migration.xml",
                        1
                },
                {
                        "alter_table_add_column_migration.xml",
                        1
                },
                {
                        "alter_table_add_foreign_key_migration.xml",
                        9
                },
                {
                        "alter_table_add_unique_column_migration.xml",
                        2
                }
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
