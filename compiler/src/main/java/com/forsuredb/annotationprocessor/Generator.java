package com.forsuredb.annotationprocessor;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/*package*/ interface Generator {
    boolean generate(String templateResource, VelocityEngine ve);
}
