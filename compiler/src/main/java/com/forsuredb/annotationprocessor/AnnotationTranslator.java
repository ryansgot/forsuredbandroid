package com.forsuredb.annotationprocessor;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

/*package*/ class AnnotationTranslator {

    private final Map<String, Object> annotation = new HashMap<>();

    /*package*/ AnnotationTranslator() {}

    /*package*/ AnnotationTranslator(Map<? extends ExecutableElement, ? extends AnnotationValue> annotation) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.entrySet()) {
            ExecutableElement ee = entry.getKey();
            AnnotationValue av = entry.getValue();
            this.annotation.put(ee.getSimpleName().toString(), av.getValue());
        }
    }

    public Caster property(String property) {
        return new Caster(annotation.get(property));
    }
}
