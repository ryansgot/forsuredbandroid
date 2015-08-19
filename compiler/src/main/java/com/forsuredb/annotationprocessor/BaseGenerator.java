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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

public abstract class BaseGenerator<F extends FileObject> implements Generator {

    private final ProcessingEnvironment processingEnv;

    public BaseGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean generate(String templateResource, VelocityEngine ve) {
        if (templateResource == null || templateResource.isEmpty()) {
            printMessage(Diagnostic.Kind.ERROR, "error creating from resource: " + templateResource);
            return false;
        }

        try {
            applyTemplate(templateResource, ve);
        } catch (ResourceNotFoundException | ParseErrorException | MethodInvocationException exception) {
            printMessage(Diagnostic.Kind.ERROR, "error creating from resource: " + templateResource + ": " + exception.getMessage());
            return false;
        }

        return true;
    }

    private void applyTemplate(String templateResource, VelocityEngine ve) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Writer writer = null;
        VelocityContext vc = createVelocityContext();
        if (vc == null) {
            return;
        }

        try {
            final Template template = ve.getTemplate(templateResource);
            F fo = createFileObject(processingEnv);
            printMessage(Diagnostic.Kind.NOTE, "creating source file: " + fo.getName());
            writer = fo.openWriter();

            printMessage(Diagnostic.Kind.NOTE, "applying velocity template: " + template.getName());
            template.merge(vc, writer);
        } catch (IOException ioe) {
            printMessage(Diagnostic.Kind.ERROR, "Could not output to file: " + ioe.getMessage());
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

    protected abstract F createFileObject(ProcessingEnvironment processingEnv) throws IOException;
    protected abstract VelocityContext createVelocityContext();

    protected ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    // Wrap the processing environment for the purpose of making the printing code more readable.

    protected void printMessage(Diagnostic.Kind kind, String message) {
        processingEnv.getMessager().printMessage(kind, message);
    }

    protected void printMessage(Diagnostic.Kind kind, String message, Element e) {
        processingEnv.getMessager().printMessage(kind, message, e);
    }

    protected void printMessage(Diagnostic.Kind kind, String message, Element e, AnnotationMirror am) {
        processingEnv.getMessager().printMessage(kind, message, e, am);
    }

    protected void printMessage(Diagnostic.Kind kind, String message, Element e, AnnotationMirror am, AnnotationValue av) {
        processingEnv.getMessager().printMessage(kind, message, e, am, av);
    }
}
