/*
   forsuredb, an object relational mapping tool

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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

/**
 * <p>
 *     The base class for Generators that generate new classes, source, or resources using a
 *     {@link VelocityEngine VelocityEngine}.
 * </p>
 * @param <F>
 * @author Ryan Scott
 */
public abstract class BaseGenerator<F extends FileObject> {

    private final ProcessingEnvironment processingEnv;

    // TODO: make the constructor take the templateResource argument instead of the generate method
    public BaseGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * <p>
     *     Use the {@link VelocityEngine VelocityEngine} to generate a new class, source, or
     *     resource file based upon the template passed in
     * </p>
     * @param templateResource The Velocity Templating Language (VTL) resource to use
     * @param ve
     * @return the success/failure status of the generation. true if successful--false if unsuccessful.
     */
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

    /**
     * @param processingEnv use this to get the filer for creating the new class, source, or resource
     * @return
     * @throws IOException
     */
    protected abstract F createFileObject(ProcessingEnvironment processingEnv) throws IOException;

    /**
     *
     * @return
     */
    protected abstract VelocityContext createVelocityContext();

    // TODO: eliminate the need for this method by just using an FSLogger
    protected ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    //TODO: eliminate the need for these convenience methods by just using an FSLogger
    // Wrap the processing environment for the purpose of making the printing code more readable.

    /**
     * <p>
     *     Convenience method for printing message to the console
     * </p>
     * @param kind
     * @param message
     */
    protected void printMessage(Diagnostic.Kind kind, String message) {
        processingEnv.getMessager().printMessage(kind, message);
    }

    /**
     * <p>
     *     Convenience method for printing message to the console
     * </p>
     * @param kind
     * @param message
     */
    protected void printMessage(Diagnostic.Kind kind, String message, Element e) {
        processingEnv.getMessager().printMessage(kind, message, e);
    }

    /**
     * <p>
     *     Convenience method for printing message to the console
     * </p>
     * @param kind
     * @param message
     */
    protected void printMessage(Diagnostic.Kind kind, String message, Element e, AnnotationMirror am) {
        processingEnv.getMessager().printMessage(kind, message, e, am);
    }

    /**
     * <p>
     *     Convenience method for printing message to the console
     * </p>
     * @param kind
     * @param message
     */
    protected void printMessage(Diagnostic.Kind kind, String message, Element e, AnnotationMirror am, AnnotationValue av) {
        processingEnv.getMessager().printMessage(kind, message, e, am, av);
    }
}
