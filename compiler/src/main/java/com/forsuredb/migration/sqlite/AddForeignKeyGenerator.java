package com.forsuredb.migration.sqlite;

import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddForeignKeyGenerator extends QueryGenerator {

    private final TableInfo table;
    private final ColumnInfo column;

    public AddForeignKeyGenerator(TableInfo table, ColumnInfo column) {
        super(table.getTableName(), MigrationType.ADD_FOREIGN_KEY_REFERENCE);
        this.table = table;
        this.column = column;
    }

    @Override
    public List<String> generate() {
        List<String> retList = new LinkedList<>();

        retList.add(dropTempTableQuery());
        retList.addAll(addNewColumnWithoutForeignKeyQuery());
        retList.add(createTempTableQuery());
        retList.add(dropThisTableQuery());
        retList.add(recreateTableWithAllForeignKeysQuery());
        retList.addAll(allColumnAdditionQueries());
        retList.add(reinsertDataQuery());
        retList.add(dropTempTableQuery());

        return retList;
    }

    @Override
    public Map<String, String> getAdditionalAttributes() {
        Map<String, String> ret = new HashMap<>();
        ret.put("column", column.getColumnName());
        ret.put("column_type", column.getQualifiedType());
        ret.put("foreign_key_table", column.getForeignKeyTableName());
        ret.put("foreign_key_column", column.getForeignKeyColumnName());
        return ret;
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

    private String recreateTableWithAllForeignKeysQuery() {
        StringBuffer buf = new StringBuffer(new CreateTableGenerator(getTableName()).generate().get(0));
        buf.delete(buf.length() - 2, buf.length());   // <-- removes );

        List<ColumnInfo> foreignKeyColumns = table.getForeignKeyColumns();
        for (ColumnInfo foreignKeyColumn : foreignKeyColumns) {
            buf.append(", ").append(foreignKeyColumn.getColumnName())
                    .append(" ").append(TypeTranslator.from(column.getQualifiedType()).getSqlString());
        }

        for (ColumnInfo foreignKeyColumn : foreignKeyColumns) {
            buf.append(", FOREIGN KEY(").append(foreignKeyColumn.getColumnName())
                    .append(") REFERENCES ").append(foreignKeyColumn.getForeignKeyTableName())
                    .append("(").append(foreignKeyColumn.getForeignKeyColumnName())
                    .append(")");
        }

        return buf.append(");").toString();
    }

    private List<String> allColumnAdditionQueries() {
        List<String> retList = new LinkedList<>();
        for (ColumnInfo columnInfo : table.getNonForeignKeyColumns()) {
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

        // foreign keys will get lumped into the TABLE CREATE query, so they need to be first
        for (ColumnInfo columnInfo : table.getForeignKeyColumns()) {
            if (idFieldOrThisColumn(columnInfo)) {
                continue;
            }
            buf.append(", ").append(columnInfo.getColumnName());
        }

        // non foreign keys will get added back in this same order, after all the foreign keys are added
        for (ColumnInfo columnInfo : table.getNonForeignKeyColumns()) {
            if (idFieldOrThisColumn(columnInfo)) {
                continue;
            }
            buf.append(", ").append(columnInfo.getColumnName());
        }
    }

    private String tempTableName() {
        return "temp_" + getTableName();
    }

    private boolean idFieldOrThisColumn(ColumnInfo columnInfo) {
        return "_id".equals(columnInfo.getColumnName())
                || column.getColumnName().equals(columnInfo.getColumnName());
    }
}
