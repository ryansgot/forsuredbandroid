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
package com.forsuredb.annotationprocessor;

import com.forsuredb.api.Finder;

import org.apache.commons.lang.WordUtils;
import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

public class FinderGenerator extends BaseGenerator<JavaFileObject> {

    private static final String LOG_TAG = FinderGenerator.class.getSimpleName();
    private static final Map<String, EnumSet<FinderMethodInfo>> finderMethodSetMap = new HashMap<>();
    static {
        finderMethodSetMap.put(boolean.class.getName(), FinderMethodInfo.BOOLEAN_METHODS);
        finderMethodSetMap.put(Boolean.class.getName(), FinderMethodInfo.BOOLEAN_METHODS);
        finderMethodSetMap.put(int.class.getName(), FinderMethodInfo.NUMBER_METHODS);
        finderMethodSetMap.put(Integer.class.getName(), FinderMethodInfo.NUMBER_METHODS);
        finderMethodSetMap.put(long.class.getName(), FinderMethodInfo.NUMBER_METHODS);
        finderMethodSetMap.put(Long.class.getName(), FinderMethodInfo.NUMBER_METHODS);
        finderMethodSetMap.put(double.class.getName(), FinderMethodInfo.NUMBER_METHODS);
        finderMethodSetMap.put(Double.class.getName(), FinderMethodInfo.NUMBER_METHODS);
        finderMethodSetMap.put(BigDecimal.class.getName(), FinderMethodInfo.NUMBER_METHODS);
        finderMethodSetMap.put(String.class.getName(), FinderMethodInfo.STRING_METHODS);
        finderMethodSetMap.put(Date.class.getName(), FinderMethodInfo.DATE_METHODS);
    }

    private final TableInfo tableInfo;
    private final String resultParameter;

    public FinderGenerator(TableInfo tableInfo, String resultParameter, ProcessingEnvironment processingEnv) {
        super("finder_extension.vm", processingEnv);
        this.tableInfo = tableInfo;
        this.resultParameter = resultParameter;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected VelocityContext createVelocityContext() {
        VelocityContext vc = new VelocityContext();
        vc.put("resultParameter", resultParameter);
        vc.put("className", getOutputClassName(false));
        vc.put("packageName", tableInfo.getPackageName());
        vc.put("getApiClass", tableInfo.getQualifiedClassName());
        vc.put("setApiClass", getSetApiClass());
        vc.put("columnConstants", createColumnConstants());
        vc.put("methodDefinitions", createMethodDefinitions());
        return vc;
    }

    private String getSetApiClass() {
        return tableInfo.getQualifiedClassName() + "Setter";
    }

    private String getOutputClassName(boolean fullyQualified) {
        return (fullyQualified ? tableInfo.getQualifiedClassName() : tableInfo.getSimpleClassName()) + "Finder";
    }

    private List<ColumnConstant> createColumnConstants() {
        List<ColumnConstant> ret = new ArrayList<>();
        for (ColumnInfo column : tableInfo.getColumns()) {
            ret.add(new ColumnConstant(column));
        }
        return ret;
    }

    private List<String> createMethodDefinitions() {
        List<String> retList = new ArrayList<>();
        for (ColumnInfo column : tableInfo.getColumns()) {
            retList.addAll(createMethodDefinitions(column));
        }

        return retList;
    }

    private List<String> createMethodDefinitions(ColumnInfo column) {
        List<String> retList = new LinkedList<>();

        EnumSet<FinderMethodInfo> filterMethods = finderMethodSetMap.get(column.getQualifiedType());
        if (filterMethods == null) {
            APLog.w(LOG_TAG, "could not find finder method set for type: " + column.getQualifiedType());
            return retList;
        }

        for (FinderMethodInfo fmi : filterMethods.toArray(new FinderMethodInfo[]{})) {
            retList.add(new StringBuilder("public ").append(returnType(fmi))
                    .append(" by").append(WordUtils.capitalize(column.getMethodName())).append(fmi.getSuffix())
                    .append("(").append(column.getQualifiedType())
                    .append(" ").append(column.getMethodName())
                    .append(") {\n").append(methodImplementation(fmi, column))
                    .append("\n    }").toString());
        }

        return retList;
    }

    private String methodImplementation(FinderMethodInfo fmi, ColumnInfo column) {
        ColumnConstant columnConstant = new ColumnConstant(column);
        return new StringBuilder("        ").append("addToBuff(").append(columnConstant.getName()).append(", ")
                .append("Finder.Operator.").append(fmi.getOperator().name()).append(", ").append(column.getMethodName())
                .append(");\n        ").append("return ")
                .append(fmi.isReturnBetween() ? "createBetween(" + column.getQualifiedType() + ".class, " + columnConstant.getName() + ")" : "conjunction")
                .append(";").toString();
    }

    private String returnType(FinderMethodInfo fmi) {
        return new StringBuilder(fmi.isReturnBetween() ? "Between<" : "Conjunction<").append(resultParameter)
                .append(", ").append(tableInfo.getQualifiedClassName())
                .append(", ").append(getSetApiClass())
                .append(", ").append(getOutputClassName(false))
                .append(">").toString();
    }

    private enum FinderMethodInfo {
        AFTER("After", Finder.Operator.GT),
        AFTER_INCLUSIVE("AfterInclusive", Finder.Operator.GE),
        BEFORE("Before", Finder.Operator.LT),
        BEFORE_INCLUSIVE("BeforeInclusive", Finder.Operator.LE),
        BETWEEN("Between", Finder.Operator.GT),
        BETWEEN_INCLUSIVE("BetweenInclusive", Finder.Operator.GE),
        GREATER_THAN("GreaterThan", Finder.Operator.GT),
        GREATER_THAN_INCLUSIVE("GreaterThanInclusive", Finder.Operator.GE),
        IS("", Finder.Operator.EQ),
        IS_NOT("Not", Finder.Operator.NE),
        LIKE("Like", Finder.Operator.LIKE),
        LESS_THAN("LessThan", Finder.Operator.LT),
        LESS_THAN_INCLUSIVE("LessThanInclusive", Finder.Operator.LE);

        public static final EnumSet<FinderMethodInfo> DATE_METHODS = EnumSet.of(AFTER, AFTER_INCLUSIVE, BEFORE, BEFORE_INCLUSIVE, BETWEEN, BETWEEN_INCLUSIVE, IS, IS_NOT);
        public static final EnumSet<FinderMethodInfo> NUMBER_METHODS = EnumSet.of(GREATER_THAN, GREATER_THAN_INCLUSIVE, LESS_THAN, LESS_THAN_INCLUSIVE, BETWEEN, BETWEEN_INCLUSIVE, IS, IS_NOT);
        public static final EnumSet<FinderMethodInfo> BOOLEAN_METHODS = EnumSet.of(IS);
        public static final EnumSet<FinderMethodInfo> STRING_METHODS = EnumSet.of(GREATER_THAN, GREATER_THAN_INCLUSIVE, LESS_THAN, LESS_THAN_INCLUSIVE, LIKE, BETWEEN, BETWEEN_INCLUSIVE, IS, IS_NOT);
        private static final EnumSet<FinderMethodInfo> RETURN_BETWEEN_METHODS = EnumSet.of(BETWEEN, BETWEEN_INCLUSIVE);

        private String suffix;
        private Finder.Operator operator;

        FinderMethodInfo(String suffix, Finder.Operator operator) {
            this.suffix = suffix;
            this.operator = operator;
        }

        public String getSuffix() {
            return suffix;
        }

        public Finder.Operator getOperator() {
            return operator;
        }

        public boolean isReturnBetween() {
            return RETURN_BETWEEN_METHODS.contains(this);
        }
    }

    private class ColumnConstant {
        private String name;
        private String value;

        public ColumnConstant(ColumnInfo column) {
            this.name = column.getColumnName().toUpperCase();
            this.value = "\"" + column.getColumnName() + "\"";
        }

        @Override
        public String toString() {
            return name + " = " + value;
        }

        public String getName() {
            return name;
        }
    }
}
