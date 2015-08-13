package com.forsuredb.annotationprocessor;

import java.util.List;

/*package*/ class EmptyLineGenerator extends XmlGenerator {
    /*package*/ EmptyLineGenerator() {
        super(-1, null, null);
    }

    @Override
    protected String generateQuery(DBType dbType, List<TableInfo> allTables) {
        return null;
    }
}
