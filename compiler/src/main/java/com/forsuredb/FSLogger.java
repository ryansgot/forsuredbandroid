package com.forsuredb;

public interface FSLogger {
    void e(String message);
    void i(String message);
    void w(String message);
    void o(String message);

    class SilentLog implements FSLogger {
        public void e(String message) {}
        public void i(String message) {}
        public void w(String message) {}
        public void o(String message) {}
    }

    class DefaultFSLogger implements FSLogger {

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
