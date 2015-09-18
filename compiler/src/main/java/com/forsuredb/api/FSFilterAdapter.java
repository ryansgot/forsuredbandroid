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

import com.forsuredb.annotation.FSColumn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     Adapter capable of creating an implementation of any {@link FSSaveApi FSSaveApi}
 *     extension
 * </p>
 */
public class FSFilterAdapter {

    public static <U, F extends FSFilter<U>> F create(Class<F> filterClass, FSRecordResolver<U, F> recordResolver) {
        return (F) Proxy.newProxyInstance(filterClass.getClassLoader(), new Class<?>[]{filterClass}, new Handler<>(recordResolver));
    }

    private static class Handler<U, F extends FSFilter<U>> implements InvocationHandler {

        private final StringBuffer whereBuf;
        private final List<String> replacementList;

        private final FSRecordResolver<U, F> recordResolver;

        public Handler(FSRecordResolver<U, F> recordResolver) {
            this.recordResolver = recordResolver;
            whereBuf = new StringBuffer();
            replacementList = new ArrayList<>();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            String methodName = method.getName();
            if (methodName.startsWith("by")) {  // <-- adding a filter
                final String column = getColumnName(method);
                if (shouldReturnBetween(methodName)) {
                    whereBuf.append(whereBuf.length() == 0 ? column : " AND " + column)
                            .append(methodName.endsWith("Between") ? " > " : " >= ").append("?");
                    replacementList.add(args[0].toString());
                    return createBetween(column, proxy);
                } else {
                    whereBuf.append(whereBuf.length() == 0 ? column : " AND " + column)
                            .append(operation(methodName)).append(" ?");
                    replacementList.add(args[0].toString());
                }
            }

            return createSelection();
        }

        private boolean shouldReturnBetween(String methodName) {
            return methodName.endsWith("Between") || methodName.endsWith("BetweenInclusive");
        }

        private FSSelection createSelection() {
            return new FSSelection() {

                String where = whereBuf.toString();
                String[] replacements = replacementList.toArray(new String[]{});

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

        private FSBetween<U, Object, F> createBetween(final String column, final Object proxy) {
            return new FSBetween<U, Object, F>() {

                @Override
                public FSRecordResolver<U, F> and(Object upper) {
                    whereBuf.append(" AND ").append(column).append(" < ?");
                    replacementList.add(upper.toString());
                    return createRecordResolver(proxy);
                }

                @Override
                public FSRecordResolver<U, F> andInclusive(Object upper) {
                    whereBuf.append(" AND ").append(column).append(" <= ?");
                    replacementList.add(upper.toString());
                    return createRecordResolver(proxy);
                }
            };
        }

        private FSRecordResolver<U, F> createRecordResolver(final Object proxy) {
            return new FSRecordResolver<U, F>() {
                @Override
                public <S extends FSSaveApi<U>> S setter() {
                    return recordResolver.setter();
                }

                @Override
                public <T extends FSGetApi> T getter() {
                    return recordResolver.getter();
                }

                @Override
                public <R extends Retriever> R retrieve() {
                    return recordResolver.retrieve();
                }

                @Override
                public F find() {
                    return recordResolver.find();
                }

                @Override
                public F also() {
                    return (F) proxy;
                }
            };
        }

        private String operation(String methodName) {
            if (methodName.endsWith("Not")) {
                return "!=";
            } else if (methodName.endsWith("LessThan") || methodName.endsWith("Before")) {
                return "<";
            } else if (methodName.endsWith("LessThanInclusive") || methodName.endsWith("BeforeInclusive")) {
                return "<=";
            } else if (methodName.endsWith("GreaterThan") || methodName.endsWith("After")) {
                return ">";
            } else if (methodName.endsWith("GreaterThanInclusive") || methodName.endsWith("AfterInclusive")) {
                return ">=";
            }
            return "=";
        }

        private  String getColumnName(Method m) {
            return m.isAnnotationPresent(FSColumn.class) ? m.getAnnotation(FSColumn.class).value() : m.getName();
        }
    }
}
