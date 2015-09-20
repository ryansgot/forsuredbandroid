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
import javax.tools.FileObject;

/**
 * <p>
 *     The base class for Generators that generate new classes, source, or resources using a
 *     {@link VelocityEngine}.
 * </p>
 * @param <F> some extension of {@link FileObject} that defines where to generate the file
 * @author Ryan Scott
 */
public abstract class BaseGenerator<F extends FileObject> {

    private final ProcessingEnvironment processingEnv;
    private final String templateResource;

    public BaseGenerator(String templateResource, ProcessingEnvironment processingEnv) {
        this.templateResource = templateResource;
        this.processingEnv = processingEnv;
    }

    /**
     * <p>
     *     Use the {@link VelocityEngine} to generate a new class, source, or resource file based upon
     *     the template passed in
     * </p>
     * @param ve The {@link VelocityEngine} used to create the class, source, or resource file
     * @return the success/failure status of the generation. true if successful--false if unsuccessful.
     */
    public boolean generate(VelocityEngine ve) {
        if (templateResource == null || templateResource.isEmpty()) {
            APLog.e(this.getClass().getSimpleName(), "error creating from resource: " + templateResource);
            return false;
        }

        try {
            applyTemplate(ve);
        } catch (ResourceNotFoundException | ParseErrorException | MethodInvocationException exception) {
            APLog.e(this.getClass().getSimpleName(), "error creating from resource: " + templateResource + ": " + exception.getMessage());
            return false;
        }

        return true;
    }

    private void applyTemplate(VelocityEngine ve) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Writer writer = null;
        VelocityContext vc = createVelocityContext();
        if (vc == null) {
            return;
        }

        try {
            final Template template = ve.getTemplate(templateResource);
            F fo = createFileObject(processingEnv);
            APLog.i(this.getClass().getSimpleName(), "creating source file: " + fo.getName());
            writer = fo.openWriter();

            APLog.i(this.getClass().getSimpleName(), "applying velocity template: " + template.getName());
            template.merge(vc, writer);
        } catch (IOException ioe) {
            APLog.e(this.getClass().getSimpleName(), "Could not output to file: " + ioe.getMessage());
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
     * @param processingEnv use this to get the {@link javax.annotation.processing.Filer}
     *                      for creating the new class, source, or resource
     * @return Some extension of FileObject for a class, source, or resource file. This should be
     * JavaObject for generating java source files
     * @throws IOException
     */
    protected abstract F createFileObject(ProcessingEnvironment processingEnv) throws IOException;

    /**
     * @return the {@link VelocityContext} to be used by the {@link VelocityEngine} and applied to the
     * template file
     */
    protected abstract VelocityContext createVelocityContext();
}
