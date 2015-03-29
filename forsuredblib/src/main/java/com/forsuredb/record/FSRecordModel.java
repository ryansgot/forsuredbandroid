package com.forsuredb.record;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *     Performs reflectivity bits that exist for all models
 * </p>
 */
public abstract class FSRecordModel {

    private final Map<String, Class> columnClassMap;

    public FSRecordModel() {
        columnClassMap = setupColumns();
    }

    public Class getClassOf(String columnName) {
        return columnName == null ? null : columnClassMap.get(columnName);
    }

    private Map<String, Class> setupColumns() {
        final Map<String, Class> retMap = new HashMap<String, Class>();
        for (Field field : this.getClass().getDeclaredFields()) {
            final String columnName = field.isAnnotationPresent(As.class) ? field.getAnnotation(As.class).value() : field.getName();
            retMap.put(columnName, field.getType());
        }
        return retMap;
    }
}
