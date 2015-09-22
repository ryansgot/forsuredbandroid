package com.forsuredb.annotationprocessor;

public class JoinInfo {

    private final TableInfo parentTable;
    private final ColumnInfo parentColumn;
    private final TableInfo childTable;
    private final ColumnInfo childColumn;

    private JoinInfo(TableInfo parentTable, ColumnInfo parentColumn, TableInfo childTable, ColumnInfo childColumn) {
        this.parentTable = parentTable;
        this.parentColumn = parentColumn;
        this.childTable = childTable;
        this.childColumn = childColumn;
    }

    @Override
    public String toString() {
        return new StringBuilder(JoinInfo.class.getSimpleName()).append("{parentTable=")
                .append(parentTable.toString()).append(", parentColumn=")
                .append(parentColumn.toString()).append(", childTable=")
                .append(childTable.toString()).append(", childColumn=")
                .append(childColumn.toString()).append("}")
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public TableInfo getParentTable() {
        return parentTable;
    }

    public ColumnInfo getParentColumn() {
        return parentColumn;
    }

    public TableInfo getChildTable() {
        return childTable;
    }

    public ColumnInfo getChildColumn() {
        return childColumn;
    }

    public static class Builder {

        private TableInfo parentTable;
        private ColumnInfo parentColumn;
        private TableInfo childTable;
        private ColumnInfo childColumn;

        public Builder parentTable(TableInfo parentTable) {
            this.parentTable = parentTable;
            return this;
        }

        public Builder parentColumn(ColumnInfo parentColumn) {
            this.parentColumn = parentColumn;
            return this;
        }

        public Builder childTable(TableInfo childTable) {
            this.childTable = childTable;
            return this;
        }

        public Builder childColumn(ColumnInfo childColumn) {
            this.childColumn = childColumn;
            return this;
        }

        public JoinInfo build() {
            if (canBuild()) {
                return new JoinInfo(parentTable, parentColumn, childTable, childColumn);
            }
            throw new IllegalStateException("Cannot have null parentTable, parentColumn, childTable, or childColumn");
        }

        private boolean canBuild() {
            return parentTable != null && parentColumn != null && childTable != null && childColumn != null;
        }
    }
}
