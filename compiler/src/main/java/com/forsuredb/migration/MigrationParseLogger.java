package com.forsuredb.migration;

public interface MigrationParseLogger {
    void e(String message);
    void i(String message);
    void w(String message);
    void o(String message);

    class SilentLog implements MigrationParseLogger {
        public void e(String message) {}
        public void i(String message) {}
        public void w(String message) {}
        public void o(String message) {}
    }

    class DefaultLogger implements MigrationParseLogger {

        @Override
        public void e(String message) {
            System.out.println("[MIGRATION_PARSER_ERROR]: " + message);
        }

        @Override
        public void i(String message) {
            System.out.println("[MIGRATION_PARSER_INFO]: " + message);
        }

        @Override
        public void w(String message) {
            System.out.println("[MIGRATION_PARSER_WARNING]: " + message);

        }

        @Override
        public void o(String message) {
            System.out.println("[MIGRATION_PARSER_OTHER]: " + message);
        }
    }
}
