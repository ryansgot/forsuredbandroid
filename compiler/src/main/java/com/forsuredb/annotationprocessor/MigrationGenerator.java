package com.forsuredb.annotationprocessor;

import com.forsuredb.Migration;
import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.FSTable;
import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.annotation.PrimaryKey;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/*package*/ class MigrationGenerator {

    private final Date date;
    private final TypeElement intf;
    private final ProcessingEnvironment processingEnv;
    private final PriorityQueue<Migration> migrations = new PriorityQueue<>();
    private final VelocityEngine velocityEngine;
    private final VelocityContext velocityContext;

    private final String tableName;

    private MigrationGenerator(TypeElement intf, Collection<Migration> migrations, Map<String, String> foreignTableClassNameToTableNameMap, VelocityEngine velocityEngine, ProcessingEnvironment processingEnv)  {
        date = new Date();
        this.intf = intf;
        this.tableName = getTableName(intf);
        this.processingEnv = processingEnv;
        this.velocityEngine = velocityEngine;
        this.velocityContext = createVelocityContext(foreignTableClassNameToTableNameMap, intf);
        if (migrations != null) {
            this.migrations.addAll(migrations);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private VelocityContext createVelocityContext(Map<String, String> foreignTableClassNameToTableNameMap, TypeElement intf) {
        VelocityContext vc = new VelocityContext();
        vc.put("dateInMillis",  + date.getTime());
        vc.put("tableName", getTableName(intf));
        vc.put("className", getSimpleOutputClassName(intf));
        vc.put("packageName", getQualifiedPackageName(intf));
        vc.put("tableCreateQuery", buildTableCreateQuery(foreignTableClassNameToTableNameMap));
        return vc;
    }

    public boolean generate(Map<String, String> foreignTableClassNameToTableNameMap, String templateResource) {
        if (templateResource == null || templateResource.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot generate migration class: " + getQualifiedOutputClassName(intf) + " from resource: " + templateResource);
            return false;
        }

        try {
            applyTemplate(foreignTableClassNameToTableNameMap, templateResource);
        } catch (ResourceNotFoundException | ParseErrorException | MethodInvocationException exception) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not output to " + getQualifiedOutputClassName(intf) + ".java: " + exception.getMessage());
            return false;
        }

        return true;
    }

    private void applyTemplate(Map<String, String> foreignTableClassNameToTableNameMap, String templateResource) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Writer writer = null;
        try {
            final Template template = velocityEngine.getTemplate(templateResource);
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(getQualifiedOutputClassName(intf));

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "creating source file: " + getQualifiedOutputClassName(intf));
            writer = jfo.openWriter();

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "applying velocity template: " + template.getName());
            template.merge(velocityContext, writer);
        } catch (IOException ioe) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not output to " + getQualifiedOutputClassName(intf) + ".java: " + ioe.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioeClose) {
                    // do nothing
                }
            }
        }
    }

    private String getQualifiedOutputClassName(TypeElement intf) {
        return intf.getQualifiedName() + "Migration" + date.getTime();
    }

    private String getSimpleOutputClassName(TypeElement intf) {
        return intf.getSimpleName() + "Migration" + date.getTime();
    }

    private String getQualifiedPackageName(TypeElement intf) {
        return ((PackageElement) intf.getEnclosingElement()).getQualifiedName().toString();
    }

    // Private methods to help with making the table create query

    private String buildTableCreateQuery(Map<String, String> foreignTableClassNameToTableNameMap) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Building table create query for intf = " + intf.getSimpleName());
        StringBuffer queryBuffer = new StringBuffer("CREATE TABLE ").append(tableName).append("(");

        for (Element e : intf.getEnclosedElements()) {
            if (!isMethodElement(e)) {
                continue;
            }
            appendColumnDefinitionTo(queryBuffer, (ExecutableElement) e);
            queryBuffer.append(", ");
        }
        queryBuffer.delete(queryBuffer.length() - 2, queryBuffer.length()); // <-- remove final ", "
        appendForeignKeysLineTo(queryBuffer, foreignTableClassNameToTableNameMap);
        return queryBuffer.append(");").toString();
    }

    private boolean isMethodElement(Element e) {
        return e != null && e.getKind() == ElementKind.METHOD;
    }

    private void appendColumnDefinitionTo(StringBuffer queryBuffer, ExecutableElement methodElement) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Appending column definition for method: " + methodElement.getSimpleName() + "; buffer = " + queryBuffer.toString());
        final TypeTranslator typeTranslator = TypeTranslator.getFrom(methodElement.getReturnType());
        if (typeTranslator == null) {
            throw new IllegalStateException("typeTranslator given methodElement: " + methodElement.getSimpleName() + " with return type: " + methodElement.getReturnType() + " was null");
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "typeTranslator given methodElement: " + methodElement.getSimpleName() + " with return type: " + methodElement.getReturnType() + ", got " + typeTranslator.toString());
        }

        queryBuffer.append(getColumnName(methodElement)).append(" ")
                .append(typeTranslator == null ? "" : typeTranslator.getSQLiteTypeString())
                .append(isA(PrimaryKey.class, methodElement) ? " " + Keyword.PRIMARY_KEY.getSqliteDefinition() : "");
    }

    private void appendForeignKeysLineTo(StringBuffer queryBuffer, Map<String, String> foreignTableClassNameToTableNameMap) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Appending foreignKeysLine: buffer = " + queryBuffer.toString());
        for (Element e : ElementFilter.methodsIn(intf.getEnclosedElements())) {
            ExecutableElement methodElement = (ExecutableElement) e;
            if (!isA(FSColumn.class , methodElement) || !isA(ForeignKey.class, methodElement)) {
                continue;   // <-- only operate on foreign keys
            }

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Appending foreignKey definition for method: " + methodElement.getSimpleName());

            final ForeignKey foreignKey = methodElement.getAnnotation(ForeignKey.class);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "got foreignKey annotation: " + foreignKey.toString());
            final String foreignTable = getForeignTableName(foreignTableClassNameToTableNameMap, foreignKey);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "got foreignKey FSTable annotation: " + foreignTable.toString());
            if (foreignTable == null) {
                throw new IllegalStateException("Could not find foreign table for foreign key: " + getColumnName(methodElement));
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Got foreign key table: " + foreignTable + "; foreign column: " + foreignKey.columnName());

            queryBuffer.append(", FOREIGN KEY(")
                    .append(getColumnName(methodElement))
                    .append(") REFERENCES ")
                    .append(foreignTable)
                    .append("(").append(foreignKey.columnName())
                    .append(")");
        }
    }

    private String getForeignTableName(Map<String, String> foreignTableClassNameToTableNameMap, ForeignKey foreignKey) {
        TypeMirror fKeyClassType = null;
        try {
            foreignKey.apiClass();  // <-- trigger the MirroredTypeException
        } catch( MirroredTypeException mte ) {
            fKeyClassType = mte.getTypeMirror();
        }

        return foreignTableClassNameToTableNameMap.get(fKeyClassType.toString());
    }

    private String getColumnName(ExecutableElement methodElement) {
        if (!isA(FSColumn.class, methodElement)) {
            return methodElement.getSimpleName().toString();
        }
        final String columnName = methodElement.getAnnotation(FSColumn.class).value();
        return columnName == null || columnName.isEmpty() ? methodElement.getSimpleName().toString() : columnName;
    }

    private boolean isA(Class<? extends Annotation> annotationCls, ExecutableElement methodElement) {
        return annotationCls != null
                && methodElement != null
                && methodElement.getKind() == ElementKind.METHOD
                && methodElement.getAnnotation(annotationCls) != null;
    }

    private String getTableName(TypeElement intf) {
        return intf.getAnnotation(FSTable.class).value();
    }

    private enum TypeTranslator {
        BIG_DECIMAL(BigDecimal.class.getName(), "REAL"),
        BOOLEAN("boolean", "INTEGER"),
        BOOLEAN_WRAPPER(Boolean.class.getName(), "INTEGER"),
        BYTE_ARRAY("byte[]", "BLOB"),
        DOUBLE("double", "REAL"),
        DOUBLE_WRAPPER(Double.class.getName(), "REAL"),
        FLOAT("float", "REAL"),
        FLOAT_WRAPPER(Float.class.getName(), "REAL"),
        INT("int", "INTEGER"),
        INT_WRAPPER(Integer.class.getName(), "INTEGER"),
        LONG("long", "INTEGER"),
        LOG_WRAPPER(Long.class.getName(), "INTEGER"),
        STRING(String.class.getName(), "TEXT");

        private String typeMirrorString;
        private String sqliteTypeString;

        TypeTranslator(String typeMirrorString, String sqliteTypeString) {
            this.typeMirrorString = typeMirrorString;
            this.sqliteTypeString = sqliteTypeString;
        }

        public static TypeTranslator getFrom(TypeMirror typeMirror) {
            if (typeMirror == null) {
                return null;
            }

            for (TypeTranslator typeTranslator : TypeTranslator.values()) {
                if (typeTranslator.getTypeMirrorString().equals(typeMirror.toString())) {
                    return typeTranslator;
                }
            }

            return null;
        }

        public String getTypeMirrorString() {
            return typeMirrorString;
        }

        public String getSQLiteTypeString() {
            return sqliteTypeString;
        }
    }

    private enum Keyword {
        PRIMARY_KEY("PRIMARY KEY");

        private String sqliteDefinition;

        Keyword(String sqliteDefinition) {
            this.sqliteDefinition = sqliteDefinition;
        }

        public String getSqliteDefinition() {
            return sqliteDefinition;
        }
    }

    public static class Builder {

        private TypeElement intf;
        private ProcessingEnvironment processingEnv;
        private VelocityEngine velocityEngine;
        // TODO: handle migrations other than the creation of a new table
        private final Collection<Migration> migrations = new LinkedList<>();
        private final Map<String, String> foreignTableClassNameToTableNameMap = new HashMap<>();

        private Builder() {}

        public Builder intf(TypeElement intf) {
            this.intf = intf;
            return this;
        }

        public Builder addForeignTable(String foreignTableClassName, String foreignTableName) {
            if (canAddForeignTable(foreignTableClassName, foreignTableName)) {
                foreignTableClassNameToTableNameMap.put(foreignTableClassName, foreignTableName);
            }
            return this;
        }

        public Builder velocityEngine(VelocityEngine velocityEngine) {
            this.velocityEngine = velocityEngine;
            return this;
        }

        public Builder processingEnv(ProcessingEnvironment processingEnv) {
            this.processingEnv = processingEnv;
            return this;
        }

        public Builder addMigration(Migration migration) {
            if (migration != null) {
                migrations.add(migration);
            }
            return this;
        }

        public Builder addMigrations(Collection<Migration> migrations) {
            if (migrations != null) {
                migrations.addAll(migrations);
            }
            return this;
        }

        public MigrationGenerator build() {
            return new MigrationGenerator(intf, migrations, foreignTableClassNameToTableNameMap, velocityEngine, processingEnv);
        }

        private boolean canAddForeignTable(String foreignTableClassName, String foreignTableName) {
            return foreignTableClassName != null
                    && !foreignTableClassName.isEmpty()
                    && foreignTableName != null
                    && !foreignTableName.isEmpty();
        }
    }
}
