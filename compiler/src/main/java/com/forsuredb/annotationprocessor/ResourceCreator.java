package com.forsuredb.annotationprocessor;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

/*package*/ class ResourceCreator {

    private final JavaFileManager.Location location;
    private final CharSequence pkg;
    private final CharSequence relativeName;

    public ResourceCreator(final String relativeName) {
        this(System.getProperty("applicationPackageName"), relativeName, StandardLocation.CLASS_OUTPUT);
    }

    public ResourceCreator(String pkg, String relativeName, JavaFileManager.Location location) {
        this.location = location;
        this.pkg = pkg;
        this.relativeName = relativeName;
    }

    public FileObject create(ProcessingEnvironment processingEnv) throws IOException {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "creating resource at location: " + location.getName());
        return processingEnv.getFiler().createResource(location, pkg, relativeName);
    }
}
