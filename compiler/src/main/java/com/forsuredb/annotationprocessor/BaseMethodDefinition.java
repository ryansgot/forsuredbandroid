package com.forsuredb.annotationprocessor;

/*package*/ abstract class BaseMethodDefinition {

    private static final String TAB = "    ";

    private final String scope;
    private final String returnType;
    private final String name;
    private final Pair<String, String>[] parameters;
    private final int baseTabs;

    public BaseMethodDefinition(String scope, String returnType, String name, Pair<String, String>... parameters) {
        this(scope, returnType, name, 1, parameters);
    }

    public BaseMethodDefinition(String scope, String returnType, String name, int baseTabs, Pair<String, String>... parameters) {
        this.scope = scope;
        this.returnType = returnType;
        this.name = name;
        this.baseTabs = baseTabs;
        this.parameters = parameters;
    }

    protected String signature() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < baseTabs; i++) {
            buf.append(TAB);
        }

        buf.append(scope).append(" ").append(returnType).append(" ").append(name).append("(");

        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(parameters[i].first).append(" ").append(parameters[i].second);
        }

        return buf.append(")").toString();
    }

    protected String newLine(int tabs) {
        final StringBuilder buf = new StringBuilder("\n");
        for (int i = 0; i < tabs; i++) {
            buf.append(TAB);
        }
        return buf.toString();
    }
}
