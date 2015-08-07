package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSColumn;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.ElementKind;
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
                          String pkgName,
                          List<? extends Element> enclosedElements) {
        this.processingEnv = processingEnv;
        this.velocityEngine = velocityEngine;
        this.fqClassName = pkgName + "." + className;
        velocityContext = createVelocityContext(className, pkgName, enclosedElements);
    }

    public static Builder builder() {
        return new Builder();
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

    private VelocityContext createVelocityContext(String className, String pkgName, List<? extends Element> enclosedElements) {
        VelocityContext vc = new VelocityContext();
        vc.put("className", className);
        vc.put("packageName", pkgName);
        vc.put("methodDefinitions", getMethodDefinitions(enclosedElements));
        return vc;
    }

    private List<String> getMethodDefinitions(List<? extends Element> enclosedElements) {
        List<MethodInfo> methodInfoList = createMethodInfoList(enclosedElements);
        List<String> retList = new ArrayList<String>();
        while (methodInfoList.size() > 0) {
            MethodInfo m = methodInfoList.remove(0);
            retList.add("void " + m.getName() + "(" + m.getReturnTypeStr() + " " + m.getName() + ");");
        }
        return retList;
    }

    private List<MethodInfo> createMethodInfoList(List<? extends Element> enclosedElements) {
        if (enclosedElements == null) {
            return Collections.EMPTY_LIST;
        }

        List<MethodInfo> retList = new LinkedList<>();
        for (Element e : enclosedElements) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }

            final ExecutableElement methodElement = (ExecutableElement) e;
            FSColumn column = methodElement.getAnnotation(FSColumn.class);
            if (column == null ) {
                continue;
            }

            retList.add(MethodInfo.builder().name(methodElement.getSimpleName().toString())
                                            .returnType(methodElement.getReturnType())
                                            .parameters(getParameters(methodElement))
                                            .build());
        }

        return retList;
    }

    private ParameterInfo getParameters(ExecutableElement methodElement) {
        ParameterInfo.Builder piBuilder = ParameterInfo.builder();
        for (VariableElement parameter : methodElement.getParameters()) {
            piBuilder.addParameter(parameter.asType(), parameter.getSimpleName().toString());
        }
        return piBuilder.build();
    }

    public static class Builder {

        private ProcessingEnvironment processingEnv;
        private VelocityEngine velocityEngine;
        private String className;
        private String pkgName;
        private List<? extends Element> enclosedElements;

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

        public Builder enclosedElements(List<? extends Element> enclosedElements) {
            this.enclosedElements = enclosedElements;
            return this;
        }

        public CodeGenerator build() {
            return new CodeGenerator(processingEnv,
                                     velocityEngine,
                                     className,
                                     pkgName,
                                     enclosedElements == null ? Collections.EMPTY_LIST : enclosedElements);
        }
    }
}
