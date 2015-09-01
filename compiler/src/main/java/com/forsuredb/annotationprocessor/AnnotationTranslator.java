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
