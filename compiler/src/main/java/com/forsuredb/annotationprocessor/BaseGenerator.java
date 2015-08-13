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
import javax.tools.FileObject;

public abstract class BaseGenerator<F extends FileObject> implements Generator {

    private final ProcessingEnvironment processingEnv;

    public BaseGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean generate(String templateResource, VelocityEngine ve) {
        if (templateResource == null || templateResource.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "error creating from resource: " + templateResource);
            return false;
        }

        try {
            applyTemplate(templateResource, ve);
        } catch (ResourceNotFoundException | ParseErrorException | MethodInvocationException exception) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "error creating from resource: " + templateResource + ": " + exception.getMessage());
            return false;
        }

        return true;
    }

    private void applyTemplate(String templateResource, VelocityEngine ve) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Writer writer = null;
        VelocityContext vc = createVelocityContext();
        try {
            final Template template = ve.getTemplate(templateResource);
            F fo = createFileObject(processingEnv);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "creating source file: " + fo.getName());
            writer = fo.openWriter();

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "applying velocity template: " + template.getName());
            template.merge(vc, writer);
        } catch (IOException ioe) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not output to file: " + ioe.getMessage());
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
}
