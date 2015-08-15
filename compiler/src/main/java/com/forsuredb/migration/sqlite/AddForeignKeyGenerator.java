package com.forsuredb.migration.sqlite;

import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableInfo;

import java.util.LinkedList;
import java.util.List;

public class AddForeignKeyGenerator extends QueryGenerator {

    private final TableInfo table;
    private final ColumnInfo column;
    private final List<TableInfo> allTables;

    public AddForeignKeyGenerator(TableInfo table, ColumnInfo column, List<TableInfo> allTables) {
        super(table.getTableName(), MigrationType.ADD_FOREIGN_KEY_REFERENCE);
        this.table = table;
        this.column = column;
        this.allTables = allTables;
    }

    @Override
    public List<String> generate() {
        List<String> retList = new LinkedList<>();

        retList.add(dropTempTableQuery());
        retList.addAll(addNewColumnWithoutForeignKeyQuery());
        retList.add(createTempTableQuery());
        retList.add(dropThisTableQuery());
        retList.add(recreateThisTableWithForeignKey());
        retList.addAll(allColumnAdditionQueries());
        retList.add(reinsertDataQuery());
        retList.add(dropTempTableQuery());

        return retList;
    }

    private String dropTempTableQuery() {
        return "DROP TABLE IF EXISTS " + tempTableName() + ";";
    }

    private List<String> addNewColumnWithoutForeignKeyQuery() {
        return new com.forsuredb.migration.sqlite.AddColumnGenerator(table.getTableName(), column).generate();
    }

    private String createTempTableQuery() {
        StringBuffer buf = new StringBuffer("CREATE TEMP TABLE ").append(tempTableName()).append(" AS SELECT ");
        appendCommaSeparatedColumnNames(buf);
        return buf.append(" FROM ").append(getTableName()).append(";").toString();
    }

    private String dropThisTableQuery() {
        return "DROP TABLE IF EXISTS " + getTableName();
    }

    private String recreateThisTableWithForeignKey() {
        StringBuffer buf = new StringBuffer(new CreateTableGenerator(getTableName()).generate().get(0));
        return buf.delete(buf.length() - 2, buf.length())   // <-- removes );
                  .append(", ").append(column.getColumnName())
                  .append(" ").append(com.forsuredb.migration.sqlite.TypeTranslator.from(column.getType()).getSqlString())
                  .append(", FOREIGN KEY(").append(column.getColumnName())
                  .append(") REFERENCES ").append(getForeignTableName())
                  .append("(").append(getForeignColumnName())
                  .append("));")
                  .toString();
    }

    private List<String> allColumnAdditionQueries() {
        List<String> retList = new LinkedList<>();
        for (ColumnInfo columnInfo : table.getColumns()) {
            if (idFieldOrThisColumn(columnInfo)) {
                continue;
            }

            retList.addAll(new com.forsuredb.migration.sqlite.AddColumnGenerator(getTableName(), columnInfo).generate());
        }

        return retList;
    }

    private String reinsertDataQuery() {
        StringBuffer buf = new StringBuffer("INSERT INTO ").append(getTableName()).append(" SELECT ");
        appendCommaSeparatedColumnNames(buf);
        return buf.append(" FROM ").append(tempTableName()).append(";").toString();
    }

    private void appendCommaSeparatedColumnNames(StringBuffer buf) {
        buf.append("_id, ").append(column.getColumnName());
        for (ColumnInfo columnInfo : table.getColumns()) {
            if (idFieldOrThisColumn(columnInfo)) {
                continue;
            }

            buf.append(", ").append(columnInfo.getColumnName());
        }
    }

    private String tempTableName() {
        return "temp_" + getTableName();
    }

    private String getForeignTableName() {
        Object uncasted = column.getAnnotation(ForeignKey.class).property("apiClass").uncasted();
        for (TableInfo table : allTables) {
            if (table.getQualifiedClassName().equals(uncasted.toString())) {
                return table.getTableName();
            }
        }

        return "";
    }

    private String getForeignColumnName() {
        return column.getAnnotation(ForeignKey.class).property("columnName").as(String.class);
    }

    private boolean idFieldOrThisColumn(ColumnInfo columnInfo) {
        return "_id".equals(columnInfo.getColumnName())
                || column.getColumnName().equals(columnInfo.getColumnName());
    }
}
