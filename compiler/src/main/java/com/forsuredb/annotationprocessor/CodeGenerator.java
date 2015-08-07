package com.forsuredb.annotationprocessor;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/*package*/ class CodeGenerator {

    private final ProcessingEnvironment processingEnv;
    private final VelocityEngine velocityEngine;
    private final VelocityContext velocityContext;
    private final String fqClassName;

    private CodeGenerator(ProcessingEnvironment processingEnv,
                          VelocityEngine velocityEngine,
                          String className,
                          String pkgName) {
        this.processingEnv = processingEnv;
        this.velocityEngine = velocityEngine;
        this.fqClassName = pkgName + "." + className;
        velocityContext = createVelocityContext(className, pkgName);
    }

    public void generate(String templateResource) {
        if (templateResource == null || templateResource.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot generate class: " + fqClassName + " from resource: " + templateResource);
            return;
        }

        try {
            applyTemplate(templateResource);
        } catch (ResourceNotFoundException | ParseErrorException | MethodInvocationException exception) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not output to " + fqClassName + ".java: " + exception.getMessage());
            return;
        }
    }

    private void applyTemplate(String templateResource) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Writer writer = null;
        try {
            final Template template = velocityEngine.getTemplate(templateResource);
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(fqClassName);

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "creating source file: " + fqClassName);
            writer = jfo.openWriter();

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "applying velocity template: " + template.getName());
            template.merge(velocityContext, writer);
        } catch (IOException ioe) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not output to " + fqClassName + ".java: " + ioe.getMessage());
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

    private VelocityContext createVelocityContext(String className, String pkgName) {
        VelocityContext vc = new VelocityContext();
        vc.put("className", className);
        vc.put("packageName", pkgName);
        return vc;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ProcessingEnvironment processingEnv;
        private VelocityEngine velocityEngine;
        private String className;
        private String pkgName;

        private Builder() {}

        public Builder processingEnv(ProcessingEnvironment processingEnv) {
            this.processingEnv = processingEnv;
            return this;
        }

        public Builder velocityEngine(VelocityEngine velocityEngine) {
            this.velocityEngine = velocityEngine;
            return this;
        }

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder pkgName(String pkgName) {
            this.pkgName = pkgName;
            return this;
        }

        public CodeGenerator build() {
            return new CodeGenerator(processingEnv, velocityEngine, className, pkgName);
        }
    }
}
