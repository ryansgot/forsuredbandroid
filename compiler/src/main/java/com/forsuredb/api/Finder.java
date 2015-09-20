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
package com.forsuredb.api;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public abstract class Finder<U, G extends FSGetApi, S extends FSSaveApi<U>, F extends Finder<U, G, S, F>> {

    public interface Conjunction<U, G extends FSGetApi, S extends FSSaveApi<U>, F extends Finder<U, G, S, F>> {
        Resolver<U, G, S, F> andFinally();
        F and();
        F or();
    }

    public enum Operator {
        EQ("="), NE("!="), LE("<="), LT("<"), GE(">="), GT(">"), LIKE("LIKE");

        private String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private final StringBuffer whereBuf = new StringBuffer();
    private final List<String> replacementsList = new ArrayList<>();
    protected final Conjunction<U, G, S, F> conjunction;

    public Finder(final Resolver<U, G, S, F> resolver) {
        conjunction = new Conjunction<U, G, S, F>() {
            @Override
            public Resolver<U, G, S, F> andFinally() {
                return resolver;
            }

            @Override
            public F and() {
                if (whereBuf.length() > 0) {
                    surroundCurrentWhereWithParens();
                    whereBuf.append(" AND ");
                }
                return (F) Finder.this;
            }

            @Override
            public F or() {
                if (whereBuf.length() > 0) {
                    surroundCurrentWhereWithParens();
                    whereBuf.append("OR");
                }
                return (F) Finder.this;
            }
        };
    }

    public final FSSelection selection() {
        return new FSSelection() {

            String where = whereBuf.toString();
            String[] replacements = replacementsList.toArray(new String[replacementsList.size()]);

            @Override
            public String where() {
                return where;
            }

            @Override
            public String[] replacements() {
                return replacements;
            }
        };
    }

    protected final void addToBuff(String column, Operator operator, Object value) {
        if (!canAddClause(column, operator, value)) {
            return;
        }
        whereBuf.append(whereBuf.length() == 0 ? column : " AND " + column)
                .append(" ").append(operator.getSymbol())
                .append(" ").append(operator == Operator.LIKE ? "%?%" : "?");
        replacementsList.add(value.toString());
    }

    protected final <T> Between<U, G, S, F> createBetween(Class<T> qualifiedType, final String column) {
        return new Between<U, G, S, F>() {
            @Override
            public <T> Conjunction<U, G, S, F> and(T high) {
                addToBuff(column, Operator.LT, high);
                return conjunction;
            }

            @Override
            public <T> Conjunction<U, G, S, F> andInclusive(T high) {
                addToBuff(column, Operator.LE, high);
                return conjunction;
            }
        };
    }

    private boolean canAddClause(String column, Operator operator, Object value) {
        return !Strings.isNullOrEmpty(column) && operator != null && value != null && !value.toString().isEmpty();
    }

    private void surroundCurrentWhereWithParens() {
        String currentWhere = whereBuf.toString();
        whereBuf.delete(0, whereBuf.length() - 1);
        whereBuf.trimToSize();
        whereBuf.append("(").append(currentWhere).append(")");
    }
}
