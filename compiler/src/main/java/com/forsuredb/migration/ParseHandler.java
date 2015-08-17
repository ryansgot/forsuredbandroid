package com.forsuredb.migration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*package*/ class ParseHandler extends DefaultHandler {

    private final MigrationParseLogger log;
    private final Parser.OnMigrationLineListener listener;

    /*package*/ ParseHandler(Parser.OnMigrationLineListener listener, MigrationParseLogger log) {
        if (listener == null) {
            throw new IllegalArgumentException("OnMigrationListener cannot be null");
        }
        this.listener = listener;
        this.log = log;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!isMigration(qName)) {
            return;
        }

        log.i("found migration");
        Migration migration = getMigrationFrom(attributes);
        if (migration != null) {
            listener.onMigrationLine(migration);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        log.i("End Element :" + qName + " with localName: " + localName);
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        log.i("characters: " + new String(ch, start, length));
    }

    private boolean isMigration(String qName) {
        return qName != null && "migration".equals(qName);
    }

    private Migration getMigrationFrom(Attributes attributes) {
        Migration.Builder mb = Migration.builder();
        for (int i = 0; i < attributes.getLength(); i++) {
            switch(attributes.getQName(i)) {
                case "db_version":
                    try {
                        mb.dbVersion(Integer.parseInt(attributes.getValue(i)));
                    } catch (NumberFormatException nfe) {
                        log.e("NumberFormatException when getting db_version: " + nfe.getMessage());
                        return null;
                    }
                    break;
                case "table_name":
                    mb.tableName(attributes.getValue(i));
                    break;
                case "query":
                    mb.query(attributes.getValue(i));
                    break;
                case "migration_type":
                    mb.migrationType(QueryGenerator.MigrationType.from(attributes.getValue(i)));
                    break;
                case "column":
                    mb.columnName(attributes.getValue(i));
                    break;
                case "column_type":
                    mb.columnQualifiedType(attributes.getValue(i));
                    break;
                case "foreign_key_table":
                    mb.foreignKeyTable(attributes.getValue(i));
                    break;
                case "foreign_key_column":
                    mb.foreignKeyColumn(attributes.getValue(i));
                    break;
                case "is_last_in_set":
                    mb.isLastInSet(Boolean.valueOf(attributes.getValue(i)));
                    break;
                default:
                    log.i("ParseHandler not using: " + attributes.getQName(i) + "=" + attributes.getValue(i));
            }
        }
        return mb.build();
    }
}
