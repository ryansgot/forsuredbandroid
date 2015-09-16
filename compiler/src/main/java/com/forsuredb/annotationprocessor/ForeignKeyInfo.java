package com.forsuredb.annotationprocessor;

import javax.lang.model.element.ExecutableElement;

public class ForeignKeyInfo {

    private final boolean cascadeUpdate;
    private final boolean cascadeDelete;
    private String foreignKeyTableName;     // <-- may not be known on creation
    private final String foreignKeyColumnName;
    private final String foreignKeyApiClassName;

    public ForeignKeyInfo(boolean cascadeUpdate,
                          boolean cascadeDelete,
                          String foreignKeyTableName,
                          String foreignKeyColumnName,
                          String foreignKeyApiClassName) {
        this.cascadeUpdate = cascadeUpdate;
        this.cascadeDelete = cascadeDelete;
        this.foreignKeyTableName = foreignKeyTableName;
        this.foreignKeyColumnName = foreignKeyColumnName;
        this.foreignKeyApiClassName = foreignKeyApiClassName;
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
                .append("{cascadeUpdate=").append(cascadeUpdate)
                .append(", cascadeDelete=").append(cascadeDelete)
                .append(", foreignKeyTableName=").append(foreignKeyTableName)
                .append(", foreignKeyColumnName=").append(foreignKeyColumnName)
                .append(", foreignKeyApiClassName=").append(foreignKeyApiClassName)
                .append("}").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ForeignKeyInfo)) {
            return false;
        }

        ForeignKeyInfo other = (ForeignKeyInfo) o;
        if (cascadeUpdate != other.cascadeUpdate()) {
            return false;
        }
        if (cascadeDelete != other.cascadeDelete()) {
            return false;
        }
        if (!isSame(foreignKeyTableName, other.getForeignKeyTableName())) {
            return false;
        }
        if (!isSame(foreignKeyApiClassName, other.getForeignKeyApiClassName())) {
            return false;
        }
        return isSame(foreignKeyColumnName, other.getForeignKeyColumnName());
    }

    @Override
    public int hashCode() {
        int result = 41;
        result = 37 * result + (cascadeDelete ? 1 : 0);
        result = 37 * result + (cascadeUpdate ? 1 : 0);
        result = 37 * result + (foreignKeyTableName == null ? 0 : foreignKeyTableName.hashCode());
        result = 37 * result + (foreignKeyApiClassName == null ? 0 : foreignKeyApiClassName.hashCode());
        result = 37 * result + (foreignKeyColumnName == null ? 0 : foreignKeyColumnName.hashCode());
        return result;
    }

    public void setForeignKeyTableName(String foreignKeyTableName) {
        this.foreignKeyTableName = foreignKeyTableName;
    }

    public boolean cascadeUpdate() {
        return cascadeUpdate;
    }

    public boolean cascadeDelete() {
        return cascadeDelete;
    }

    public String getForeignKeyTableName() {
        return foreignKeyTableName;
    }

    public String getForeignKeyColumnName() {
        return foreignKeyColumnName;
    }

    public String getForeignKeyApiClassName() {
        return foreignKeyApiClassName;
    }

    private boolean isSame(Object thisMember, Object otherMember) {
        if (thisMember == null) {
            return otherMember == null;
        }
        return thisMember.equals(otherMember);
    }

    public static class Builder {

        private boolean cascadeUpdate = true;
        private boolean cascadeDelete = true;
        private String foreignKeyTableName;
        private String foreignKeyColumnName;
        private String foreignKeyApiClassName;

        public Builder cascadeUpdate(boolean cascadeUpdate) {
            this.cascadeUpdate = cascadeUpdate;
            return this;
        }

        public Builder cascadeDelete(boolean cascadeDelete) {
            this.cascadeDelete = cascadeDelete;
            return this;
        }

        public Builder foreignKeyTableName(String foreignKeyTableName) {
            this.foreignKeyTableName = foreignKeyTableName;
            return this;
        }

        public Builder foreignKeyColumnName(String foreignKeyColumnName) {
            this.foreignKeyColumnName = foreignKeyColumnName;
            return this;
        }

        public Builder foreignKeyApiClassName(String foreignKeyApiClassName) {
            this.foreignKeyApiClassName = foreignKeyApiClassName;
            return this;
        }

        public ForeignKeyInfo build() {
            if (!canBuild()) {
                ForeignKeyInfo badInfo = new ForeignKeyInfo(cascadeUpdate, cascadeDelete, foreignKeyTableName, foreignKeyColumnName, foreignKeyApiClassName);
                throw new IllegalStateException("Cannot build: foreignKeyColumnName cannot be null or empty and either\n1. foreignKeyApiClassName is neither null nor empty, or\n2. foreignKeyTableName is neither null nor empty.\nWould have built: " + badInfo.toString());
            }
            return new ForeignKeyInfo(cascadeUpdate, cascadeDelete, foreignKeyTableName, foreignKeyColumnName, foreignKeyApiClassName);
        }

        private boolean canBuild() {
            return foreignKeyColumnName != null
                    && !foreignKeyColumnName.isEmpty()
                    && (foreignKeyApiClassName != null && !foreignKeyApiClassName.isEmpty())
                        || (foreignKeyTableName != null && !foreignKeyTableName.isEmpty());
        }
    }
}
