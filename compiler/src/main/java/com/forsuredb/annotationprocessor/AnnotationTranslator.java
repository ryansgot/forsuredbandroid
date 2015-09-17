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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

/**
 * <p>
 *     Translates from all elements of an {@link AnnotationMirror AnnotationMirror} to properties
 *     that can be referenced by their String names. If you pass in {@link AnnotationMirror#getElementValues()},
 *     then only the non-default values will be translated.
 * </p>
 * @author Ryan Scott
 * @see AnnotationTranslatorFactory
 */
/*package*/ class AnnotationTranslator {

    private final Map<String, Object> annotation = new HashMap<>();

    /*package*/ AnnotationTranslator(Map<? extends ExecutableElement, ? extends AnnotationValue> elementToValueMap) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementToValueMap.entrySet()) {
            ExecutableElement ee = entry.getKey();
            AnnotationValue av = entry.getValue();
            this.annotation.put(ee.getSimpleName().toString(), av.getValue());
        }
    }

    /**
     * @param property essentially invokes the method of the annotation
     * @return A {@link Caster Caster} that can give you the uncasted value or the value cast to the
     * Type you wish
     */
    public Caster property(String property) {
        return new Caster(annotation.get(property));
    }

    /**
     * <p>
     *     Gives the caller the choice of how it would like to get the property--either as an object
     *     ({@link #uncasted() uncasted()}) or casted to some other class.
     * </p>
     * @author Ryan Scott
     */
    /*package*/ static class Caster {

        private Object uncasted;

        /*package*/ Caster(Object uncasted) {
            this.uncasted = uncasted;
        }

        public Object uncasted() {
            return uncasted;
        }

        public String asString() {
            return uncasted == null ? "null" : uncasted.toString();
        }

        /**
         * <p>
         *     Throws a {@link ClassCastException ClassCastException} if the type parameter cannot
         *     be used to cast the underlying object.
         * </p>
         * @param cls The class of the type you would like to return
         * @param <T> The type you would like to return
         * @return The property cast to T
         */
        public <T> T as(Class<T> cls) {
            return (T) uncasted;
        }
    }
}
