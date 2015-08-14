package com.forsuredb.annotationprocessor;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

public class MetaData {

    private final Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotationMap = new HashMap<>();

    /*package*/ MetaData(List<? extends AnnotationMirror> annotations) {
        if (annotations != null) {
            for (AnnotationMirror annotation : annotations) {
                annotationMap.put(annotation.getAnnotationType().toString(), annotation.getElementValues());
            }
        }
    }

    @Override
    public String toString() {
        return new StringBuffer("MetaData{annotationMap=").append(annotationMap.toString()).append("}").toString();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationCls) {
        return annotationCls == null ? false : annotationMap.containsKey(annotationCls.getName());
    }

    public AnnotationTranslator get(Class<? extends Annotation> annotationCls) {
        if (!isAnnotationPresent(annotationCls)) {
            return new AnnotationTranslator();
        }

        return new AnnotationTranslator(getAnnotation(annotationCls));
    }

    private Map<? extends ExecutableElement, ? extends AnnotationValue> getAnnotation(Class<? extends Annotation> annotationCls) {
        return annotationCls == null ? null : annotationMap.get(annotationCls.getName());
    }

    public static class AnnotationTranslator {

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
}
