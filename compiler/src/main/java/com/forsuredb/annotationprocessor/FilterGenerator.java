package com.forsuredb.annotationprocessor;

import com.forsuredb.api.FSBetween;
import com.forsuredb.api.FSRecordResolver;

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

public class FilterGenerator extends BaseGenerator<JavaFileObject> {

    private static final String LOG_TAG = FilterGenerator.class.getSimpleName();
    private static final Map<String, EnumSet<FilterMethodInfo>> filterMethodSetMap = new HashMap<>();
    private static final Map<String, String> parameterTypeMap = new HashMap<>();
    static {
        filterMethodSetMap.put(boolean.class.getName(), FilterMethodInfo.BOOLEAN_METHODS);
        filterMethodSetMap.put(Boolean.class.getName(), FilterMethodInfo.BOOLEAN_METHODS);
        filterMethodSetMap.put(int.class.getName(), FilterMethodInfo.NUMBER_METHODS);
        filterMethodSetMap.put(Integer.class.getName(), FilterMethodInfo.NUMBER_METHODS);
        filterMethodSetMap.put(long.class.getName(), FilterMethodInfo.NUMBER_METHODS);
        filterMethodSetMap.put(Long.class.getName(), FilterMethodInfo.NUMBER_METHODS);
        filterMethodSetMap.put(double.class.getName(), FilterMethodInfo.NUMBER_METHODS);
        filterMethodSetMap.put(Double.class.getName(), FilterMethodInfo.NUMBER_METHODS);
        filterMethodSetMap.put(BigDecimal.class.getName(), FilterMethodInfo.NUMBER_METHODS);
        filterMethodSetMap.put(String.class.getName(), FilterMethodInfo.STRING_METHODS);
        filterMethodSetMap.put(Date.class.getName(), FilterMethodInfo.DATE_METHODS);

        parameterTypeMap.put(boolean.class.getName(), Boolean.class.getName());
        parameterTypeMap.put(Boolean.class.getName(), Boolean.class.getName());
        parameterTypeMap.put(int.class.getName(), Integer.class.getName());
        parameterTypeMap.put(Integer.class.getName(), Integer.class.getName());
        parameterTypeMap.put(long.class.getName(), Long.class.getName());
        parameterTypeMap.put(Long.class.getName(), Long.class.getName());
        parameterTypeMap.put(double.class.getName(), Double.class.getName());
        parameterTypeMap.put(Double.class.getName(), Double.class.getName());
        parameterTypeMap.put(BigDecimal.class.getName(), BigDecimal.class.getName());
        parameterTypeMap.put(String.class.getName(), String.class.getName());
        parameterTypeMap.put(Date.class.getName(), Date.class.getName());
    }

    private final TableInfo tableInfo;
    private final String resultParameter;

    public FilterGenerator(TableInfo tableInfo, String resultParameter, ProcessingEnvironment processingEnv) {
        super("filter.vm", processingEnv);
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
        if (resultParameter != null && !resultParameter.isEmpty()) {
            vc.put("resultParameter", resultParameter);
        }
        vc.put("className", getOutputClassName(false));
        vc.put("packageName", tableInfo.getPackageName());
        vc.put("methodDefinitions", createMethodDefinitions());
        return vc;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return (fullyQualified ? tableInfo.getQualifiedClassName() : tableInfo.getSimpleClassName()) + "Filter";
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

        EnumSet<FilterMethodInfo> filterMethods = filterMethodSetMap.get(column.getQualifiedType());
        if (filterMethods == null) {
            APLog.w(LOG_TAG, "could not find filter method set for type: " + column.getQualifiedType());
            return retList;
        }

        for (FilterMethodInfo fmi : filterMethods.toArray(new FilterMethodInfo[]{})) {
            retList.add(new StringBuilder("@FSColumn(\"").append(column.getColumnName())
                    .append("\") ").append(fmi.getReturnType())
                    .append("<").append(resultParameter)
                    .append(fmi.secondParameterPrimitive() ? ", " + parameterTypeMap.get(column.getQualifiedType())
                            : fmi.secondParameterFilter() ? ", " + getOutputClassName(false) : "")
                    .append(fmi.hasThirdParameter() ? ", " + getOutputClassName(false) : "")
                    .append("> by").append(WordUtils.capitalize(column.getMethodName())).append(fmi.getSuffix())
                    .append("(").append(column.getQualifiedType())
                    .append(" ").append(fmi.shouldUseLowerForArgument() ? "lower" : column.getMethodName())
                    .append(");").toString());
        }

        return retList;
    }

    private enum FilterMethodInfo {
        AFTER("After", FSRecordResolver.class.getName()),
        AFTER_INCLUSIVE("AfterInclusive", FSRecordResolver.class.getName()),
        BEFORE("Before", FSRecordResolver.class.getName()),
        BEFORE_INCLUSIVE("BeforeInclusive", FSRecordResolver.class.getName()),
        BETWEEN("Between", FSBetween.class.getName()),
        BETWEEN_INCLUSIVE("BetweenInclusive", FSBetween.class.getName()),
        GREATER_THAN("GreaterThan", FSRecordResolver.class.getName()),
        GREATER_THAN_INCLUSIVE("GreaterThanInclusive", FSRecordResolver.class.getName()),
        IS("", FSRecordResolver.class.getName()),
        IS_NOT("Not", FSRecordResolver.class.getName()),
        LIKE("Like", FSRecordResolver.class.getName()),
        LESS_THAN("LessThan", FSRecordResolver.class.getName()),
        LESS_THAN_INCLUSIVE("LessThanInclusive", FSRecordResolver.class.getName());

        public static final EnumSet<FilterMethodInfo> DATE_METHODS = EnumSet.of(AFTER, AFTER_INCLUSIVE, BEFORE, BEFORE_INCLUSIVE, BETWEEN, BETWEEN_INCLUSIVE, IS, IS_NOT);
        public static final EnumSet<FilterMethodInfo> NUMBER_METHODS = EnumSet.of(GREATER_THAN, GREATER_THAN_INCLUSIVE, LESS_THAN, LESS_THAN_INCLUSIVE, BETWEEN, BETWEEN_INCLUSIVE, IS, IS_NOT);
        public static final EnumSet<FilterMethodInfo> BOOLEAN_METHODS = EnumSet.of(IS);
        public static final EnumSet<FilterMethodInfo> STRING_METHODS = EnumSet.of(GREATER_THAN, GREATER_THAN_INCLUSIVE, LESS_THAN, LESS_THAN_INCLUSIVE, LIKE, BETWEEN, BETWEEN_INCLUSIVE, IS, IS_NOT);
        private static final EnumSet<FilterMethodInfo> SECOND_PARAMETER_PRIMITIVE = EnumSet.of(BETWEEN, BETWEEN_INCLUSIVE);
        private static final EnumSet<FilterMethodInfo> SECOND_PARAMETER_FILTER = EnumSet.of(AFTER, AFTER_INCLUSIVE, BEFORE, BEFORE_INCLUSIVE, GREATER_THAN, GREATER_THAN_INCLUSIVE, IS, IS_NOT, LIKE, LESS_THAN, LESS_THAN_INCLUSIVE);
        private static final EnumSet<FilterMethodInfo> THREE_PARAMETER_METHODS = EnumSet.of(BETWEEN, BETWEEN_INCLUSIVE);
        private static final EnumSet<FilterMethodInfo> LOWER_ARGUMENT_OVERRIDE_METHODS = EnumSet.of(BETWEEN, BETWEEN_INCLUSIVE);

        private String suffix;
        private String returnType;

        FilterMethodInfo(String suffix, String returnType) {
            this.suffix = suffix;
            this.returnType = returnType;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getReturnType() {
            return returnType;
        }

        public boolean secondParameterPrimitive() {
            return SECOND_PARAMETER_PRIMITIVE.contains(this);
        }

        public boolean secondParameterFilter() {
            return SECOND_PARAMETER_FILTER.contains(this);
        }

        public boolean hasThirdParameter() {
            return THREE_PARAMETER_METHODS.contains(this);
        }

        public boolean shouldUseLowerForArgument() {
            return LOWER_ARGUMENT_OVERRIDE_METHODS.contains(this);
        }
    }
}
