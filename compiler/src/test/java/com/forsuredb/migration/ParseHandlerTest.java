package com.forsuredb.migration;

import com.forsuredb.TestData;
import com.forsuredb.api.FSLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static junit.framework.TestCase.assertEquals;

/**
 * <p>
 *     Test whether migration objects are correctly parsed from input XML.<br />
 *     It is unnecessary to test that the correct number of migrations are parsed in this test because that is handled in
 *     ParserTest.
 * </p>
 */
@RunWith(Parameterized.class)
public class ParseHandlerTest {

    private SAXParser saxParser;
    private InputStream stringIn;
    private Migration parsedMigration = null;
    private final Parser.OnMigrationLineListener migrationLineListener = new Parser.OnMigrationLineListener() {
        @Override
        public void onMigrationLine(Migration migration) {
            parsedMigration = migration;
            System.out.println("ParseHandlerTest: " + migration.toString());
        }
    };

    private String parserInput;
    private Migration expectedMigration;

    public ParseHandlerTest(String parserInput, Migration expectedMigration) {
        this.parserInput = parserInput;
        this.expectedMigration = expectedMigration;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // the most basic migration
                {
                        "<migrations><migration table_name=\"" + TestData.TABLE_NAME + "\" query=\"blank\" /></migrations>",
                        Migration.builder().tableName(TestData.TABLE_NAME).query("blank").build()
                },
                // all possible fields on the migration
                {
                        "<migrations><migration table_name=\"" + TestData.TABLE_NAME + "\" query=\"blank\" db_version=\"3\" migration_type=\"" + QueryGenerator.MigrationType.ADD_FOREIGN_KEY_REFERENCE.toString() + "\" column=\"test_column\" column_type=\"java.lang.String\" foreign_key_column=\"f_key_column\" foreign_key_table=\"f_key_table\" is_last_in_set=\"true\" /></migrations>",
                        Migration.builder().tableName(TestData.TABLE_NAME)
                                .query("blank")
                                .dbVersion(3)
                                .migrationType(QueryGenerator.MigrationType.ADD_FOREIGN_KEY_REFERENCE)
                                .columnName("test_column")
                                .columnQualifiedType("java.lang.String")
                                .foreignKeyColumn("f_key_column")
                                .foreignKeyTable("f_key_table")
                                .isLastInSet(true)
                                .build()
                }
        });
    }

    @Before
    public void setUp() throws Exception {
        parsedMigration = null;
        saxParser = SAXParserFactory.newInstance().newSAXParser();
        stringIn = new ByteArrayInputStream(parserInput.getBytes());
    }

    @After
    public void tearDown() throws Exception {
        stringIn.close();
        stringIn = null;
    }

    @Test
    public void shouldDetectCorrectMigration() throws Exception {
        saxParser.parse(stringIn, new ParseHandler(migrationLineListener, new FSLogger.DefaultFSLogger()));
        assertEquals(expectedMigration, parsedMigration);
    }
}
