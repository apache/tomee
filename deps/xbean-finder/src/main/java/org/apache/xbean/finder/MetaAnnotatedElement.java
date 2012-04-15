/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.finder;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
* @version $Rev$ $Date$
*/
public class MetaAnnotatedElement<T extends AnnotatedElement> implements  AnnotatedElement, MetaAnnotated<T> {
    protected final Map<Class<? extends Annotation>, MetaAnnotation<?>> annotations = new HashMap<Class<? extends Annotation>, MetaAnnotation<?>>();
    protected final T target;

    public MetaAnnotatedElement(T element) {
        this(element, unroll(element));
    }

    MetaAnnotatedElement(T target, Map<Class<? extends Annotation>, MetaAnnotation<?>> annotations) {
        this.target = target;
        this.annotations.putAll(annotations);
    }

    public T get() {
        return target;
    }

    public Annotation[] getDeclaredAnnotations() {
        return target.getDeclaredAnnotations();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return annotations.containsKey(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        MetaAnnotation<T> annotation = (MetaAnnotation<T>) annotations.get(annotationClass);
        return (annotation == null) ? null : annotation.get();
    }

    public Annotation[] getAnnotations() {
        Annotation[] annotations = new Annotation[this.annotations.size()];

        int i = 0;
        for (MetaAnnotation annotation : this.annotations.values()) {
            annotations[i++] = annotation.get();
        }

        return annotations;
    }

    public Collection<MetaAnnotation<?>> getMetaAnnotations() {
        return Collections.unmodifiableCollection(annotations.values());
    }

    @Override
    public boolean equals(Object obj) {
        return get().equals(obj);
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

    @Override
    public String toString() {
        return get().toString();
    }


    private static void unroll(Class<? extends Annotation> clazz, int depth, Map<Class<? extends Annotation>, MetaAnnotation<?>> found) {
        if (!isMetaAnnotation(clazz)) return;

        for (Annotation annotation : getDeclaredMetaAnnotations(clazz)) {
            Class<? extends Annotation> type = annotation.annotationType();

            MetaAnnotation existing = found.get(type);

            if (existing != null) {

                if (existing.getDepth() > depth) {

                    // OVERWRITE

                    found.put(type, new MetaAnnotation(annotation, depth));

                    unroll(type, depth + 1, found);

                } else if (existing.getDepth() < depth) {

                    // IGNORE

                    // ignore, what we have already is higher priority

                } else {

                    // CONFLICT

                    // They are the same depth and therefore conflicting
                    existing.getConflicts().add(new MetaAnnotation(annotation, depth));

                }

            } else {

                // NEW

                found.put(type, new MetaAnnotation(annotation, depth));

                unroll(type, depth + 1, found);

            }
        }
    }

    private static Collection<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> clazz) {

        Map<Class, Annotation> map = new HashMap<Class, Annotation>();

        // pull in the annotations declared on this annotation

        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            map.put(annotation.annotationType(), annotation);
        }

        List<Annotation[]> groups = new ArrayList<Annotation[]>();

        Class<? extends Annotation> metatype = getMetatype(clazz);
        if (metatype != null) {
            try {
                Class<?> def = clazz.getClassLoader().loadClass(clazz.getName() + "$$");

                List<AnnotatedElement> elements = new ArrayList<AnnotatedElement>();

                elements.addAll(asList(def.getDeclaredFields()));
                elements.addAll(asList(def.getDeclaredConstructors()));
                elements.addAll(asList(def.getDeclaredMethods()));

                for (Method method : def.getDeclaredMethods()) {
                    for (Annotation[] array : method.getParameterAnnotations()) {
                        groups.add(array);
                    }
                }

                for (Constructor constructor : def.getDeclaredConstructors()) {
                    for (Annotation[] array : constructor.getParameterAnnotations()) {
                        groups.add(array);
                    }
                }

                for (AnnotatedElement element : elements) {
                    groups.add(element.getDeclaredAnnotations());
                }

                for (Annotation[] annotations : groups) {
                    if (contains(annotations, clazz)) {
                        for (Annotation annotation : annotations) {
                            map.put(annotation.annotationType(), annotation);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                // inner class is optional
            }
        }

        map.remove(Target.class);
        map.remove(Retention.class);
        map.remove(Documented.class);
        map.remove(metatype);
        map.remove(clazz);

        return map.values();
    }

    private static boolean contains(Annotation[] annotations, Class<? extends Annotation> clazz) {
        for (Annotation annotation : annotations) {
            if (clazz.equals(annotation.annotationType())) return true;
        }
        return false;
    }

    private static Class<? extends Annotation> getMetatype(Class<? extends Annotation> clazz) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();

            if (isMetatypeAnnotation(type)) return type;
        }

        return null;
    }

    private static boolean isMetaAnnotation(Class<? extends Annotation> clazz) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (isMetatypeAnnotation(annotation.annotationType())) return true;
        }

        return false;
    }

    private static boolean isMetatypeAnnotation(Class<? extends Annotation> type) {
        if (type.getName().equals("javax.annotation.Metatype")) return true;
        if (isSelfAnnotated(type, "Metatype")) return true;

        for (Annotation annotation : type.getAnnotations()) {
            if (type.getName().equals("javax.annotation.Metaroot")) return true;
            if (isSelfAnnotated(annotation.annotationType(), "Metaroot")) return true;
        }

        return false;
    }

    private static boolean isSelfAnnotated(Class<? extends Annotation> type, String name) {
        return type.isAnnotationPresent(type) && type.getSimpleName().equals(name) && validTarget(type);
    }

    private static boolean validTarget(Class<? extends Annotation> type) {
        final Target target = type.getAnnotation(Target.class);

        if (target == null) return false;

        final ElementType[] targets = target.value();

        return targets.length == 1 && targets[0] == ElementType.ANNOTATION_TYPE;
    }

    protected static Map<Class<? extends Annotation>, MetaAnnotation<?>> unroll(AnnotatedElement element) {
        return unroll(element.getDeclaredAnnotations());
    }

    protected static Map<Class<? extends Annotation>, MetaAnnotation<?>> unroll(Annotation[] annotations) {
        final Map<Class<? extends Annotation>, MetaAnnotation<?>> map = new HashMap<Class<? extends Annotation>, MetaAnnotation<?>>();

        for (Annotation annotation : annotations) {

            map.put(annotation.annotationType(), new MetaAnnotation(annotation, 0));

            unroll(annotation.annotationType(), 1, map);

        }

        return map;
    }

    protected Annotation[][] unrollParameters(Annotation[][] parameterAnnotations) {
        final Annotation[][] unrolledParameters = new Annotation[parameterAnnotations.length][];

        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            final Map<Class<? extends Annotation>, MetaAnnotation<?>> map = unroll(annotations);

            int j = 0;

            final Annotation[] unrolled = new Annotation[map.size()];
            for (MetaAnnotation<?> metaAnnotation : map.values()) {
                unrolled[j++] = metaAnnotation.get();
            }

            unrolledParameters[i++] = unrolled;
        }
        return unrolledParameters;
    }
}
