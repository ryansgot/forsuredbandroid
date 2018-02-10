package com.fsryan.forsuredb;

import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Limits;
import com.fsryan.forsuredb.api.OrderBy;

import java.util.Arrays;
import java.util.List;

public class TestQueryUtil {

    public static class SelectionBuilder {

        private int limitCount = 0;
        private int limitsOffset = 0;
        private boolean limitFromBottom = false;
        private String where = null;
        private String[] replacements = null;

        private SelectionBuilder() {}

        public SelectionBuilder where(String where, String[] replacements) {
            this.where = where;
            this.replacements = replacements;
            return this;
        }

        public SelectionBuilder fromBottom(boolean limitFromBottom) {
            this.limitFromBottom = limitFromBottom;
            return this;
        }

        public SelectionBuilder limitCount(int limitCount) {
            this.limitCount = limitCount;
            return this;
        }

        public SelectionBuilder offset(int offset) {
            this.limitsOffset = offset;
            return this;
        }

        public FSSelection build() {
            return new FSSelection() {
                @Override
                public String where() {
                    return where;
                }

                @Override
                public String[] replacements() {
                    return replacements;
                }

                @Override
                public Limits limits() {
                    if (!limitFromBottom && limitCount == 0 && limitsOffset == 0) {
                        return null;
                    }
                    return new Limits() {
                        @Override
                        public int count() {
                            return limitCount;
                        }

                        @Override
                        public int offset() {
                            return limitsOffset;
                        }

                        @Override
                        public boolean isBottom() {
                            return limitFromBottom;
                        }
                    };
                }
            };
        }
    }

    public static SelectionBuilder selection() {
        return new SelectionBuilder();
    }

    public static FSSelection idSelection(long id) {
        return selection().where("_id=?", new String[] {Long.toString(id)}).build();
    }

    public static List<FSOrdering> orderings(FSOrdering... orderings) {
        return orderings == null ? null : Arrays.asList(orderings);
    }

    public static FSOrdering idOrderingASC(String table) {
        return orderingASC(table, "_id");
    }

    public static FSOrdering idOrderingDESC(String table) {
        return orderingDESC(table, "_id");
    }

    public static FSOrdering orderingASC(String table, String column) {
        return new FSOrdering(table, column, OrderBy.ORDER_ASC);
    }

    public static FSOrdering orderingDESC(String table, String column) {
        return new FSOrdering(table, column, OrderBy.ORDER_DESC);
    }
}
