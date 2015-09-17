package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.ForeignKey;

import javax.lang.model.element.ExecutableElement;

public class ForeignKeyInfo {

    private final ForeignKey.ChangeAction updateAction;
    private final ForeignKey.ChangeAction deleteAction;
    private String tableName;     // <-- may not be known on creation
    private final String columnName;
    private final String apiClassName;     // <-- may not be known on creation

    public ForeignKeyInfo(ForeignKey.ChangeAction updateAction,
                          ForeignKey.ChangeAction deleteAction,
                          String tableName,
                          String columnName,
                          String apiClassName) {
        this.updateAction = updateAction;
        this.deleteAction = deleteAction;
        this.tableName = tableName;
        this.columnName = columnName;
        this.apiClassName = apiClassName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ForeignKeyInfo from(ExecutableElement ee) {
        return null;
    }

    @Override
    public String toString() {
        return new StringBuilder(ForeignKeyInfo.class.getSimpleName())
                .append("{updateAction=").append(updateAction.toString())
                .append(", deleteAction=").append(deleteAction.toString())
                .append(", tableName=").append(tableName)
                .append(", columnName=").append(columnName)
                .append(", apiClassName=").append(apiClassName)
                .append("}").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ForeignKeyInfo)) {
            return false;
        }

        ForeignKeyInfo other = (ForeignKeyInfo) o;
        if (!isSame(updateAction, other.getUpdateAction())) {
            return false;
        }
        if (!isSame(deleteAction, other.getDeleteAction())) {
            return false;
        }
        if (!isSame(tableName, other.getTableName())) {
            return false;
        }
        if (!isSame(apiClassName, other.getApiClassName())) {
            return false;
        }
        return isSame(columnName, other.getColumnName());
    }

    @Override
    public int hashCode() {
        int result = 41;
        result = 37 * result + deleteAction.getValue();
        result = 37 * result + updateAction.getValue();
        result = 37 * result + (tableName == null ? 0 : tableName.hashCode());
        result = 37 * result + (apiClassName == null ? 0 : apiClassName.hashCode());
        result = 37 * result + (columnName == null ? 0 : columnName.hashCode());
        return result;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ForeignKey.ChangeAction getUpdateAction() {
        return updateAction;
    }

    public ForeignKey.ChangeAction getDeleteAction() {
        return deleteAction;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getApiClassName() {
        return apiClassName;
    }

    private boolean isSame(Object thisMember, Object otherMember) {
        if (thisMember == null) {
            return otherMember == null;
        }
        return thisMember.equals(otherMember);
    }

    public static class Builder {

        private ForeignKey.ChangeAction updateAction = ForeignKey.ChangeAction.CASCADE;
        private ForeignKey.ChangeAction deleteAction = ForeignKey.ChangeAction.CASCADE;
        private String tableName;
        private String columnName;
        private String apiClassName;

        public Builder updateAction(ForeignKey.ChangeAction updateAction) {
            this.updateAction = updateAction;
            return this;
        }

        public Builder deleteAction(ForeignKey.ChangeAction deleteAction) {
            this.deleteAction = deleteAction;
            return this;
        }

        public Builder tableName(String foreignKeyTableName) {
            this.tableName = foreignKeyTableName;
            return this;
        }

        public Builder columnName(String foreignKeyColumnName) {
            this.columnName = foreignKeyColumnName;
            return this;
        }

        public Builder foreignKeyApiClassName(String foreignKeyApiClassName) {
            this.apiClassName = foreignKeyApiClassName;
            return this;
        }

        public ForeignKeyInfo build() {
            if (!canBuild()) {
                ForeignKeyInfo badInfo = new ForeignKeyInfo(updateAction, deleteAction, tableName, columnName, apiClassName);
                throw new IllegalStateException("Cannot build: columnName cannot be null or empty and either\n1. apiClassName is neither null nor empty, or\n2. tableName is neither null nor empty.\nWould have built: " + badInfo.toString());
            }
            return new ForeignKeyInfo(updateAction, deleteAction, tableName, columnName, apiClassName);
        }

        private boolean canBuild() {
            return columnName != null
                    && !columnName.isEmpty()
                    && updateAction != null
                    && deleteAction != null
                    && (apiClassName != null && !apiClassName.isEmpty())
                        || (tableName != null && !tableName.isEmpty());
        }
    }
}
