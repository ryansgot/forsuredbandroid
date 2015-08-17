package com.forsuredb.annotationprocessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class TableContextCreator {

    private final Set<TypeElement> tableTypes = new HashSet<>();

    public TableContextCreator(Set<TypeElement> tableTypes) {
        this.tableTypes.addAll(tableTypes);
    }

    public List<TableInfo> createTableInfo() {
        return createTableInfo(null);
    }

    public List<TableInfo> createTableInfo(ProcessingEnvironment processingEnv) {
        List<TableInfo> allTables = gatherInitialInfo();
        for (TableInfo table : allTables) {
            for (ColumnInfo column : table.getColumns()) {
                column.enrichWithForeignTableInfo(allTables);
            }
            if (processingEnv != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, table.toString());
            }
        }
        return allTables;
    }


    private List<TableInfo> gatherInitialInfo() {
        List<TableInfo> ret = new ArrayList<>();
        for (TypeElement te : tableTypes) {
            if (te.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }

            ret.add(TableInfo.from(te));
        }

        return ret;
    }
}
