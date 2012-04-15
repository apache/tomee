/*
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

import junit.framework.TestCase;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @version $Rev$ $Date$
 */
public class MetaAnnotatedMethodParametersTest extends TestCase {

    public void test() throws Exception {

        final Class<?>[] classes = new Class[]{Square.class, Circle.class, Triangle.class, Oval.class, Store.class, Farm.class, None.class};

        final Map<String, Annotated<Method>> map = new HashMap<String, Annotated<Method>>();

        for (Class<?> clazz : classes) {
            final MetaAnnotatedClass<?> annotatedClass = new MetaAnnotatedClass(clazz);

            for (MetaAnnotatedMethod method : annotatedClass.getMethods()) {
                map.put(method.getName(), method);
            }
        }

        // Check the positive scenarios
        {
            Annotation[] annotations = getAnnotations(map, "circle");

            assertTrue(contains(Color.class, annotations));
            assertTrue(contains(Red.class, annotations));

            assertEquals("white", get(Color.class, annotations).value());

            assertEquals(2, annotations.length);
        }

        {
            Annotation[] annotations = getAnnotations(map, "square");

            assertTrue(contains(Color.class, annotations));
            assertTrue(contains(Red.class, annotations));

            assertEquals("red", get(Color.class, annotations).value());

            assertEquals(2, annotations.length);
        }

        {
            Annotation[] annotations = getAnnotations(map, "triangle");

            assertTrue(contains(Color.class, annotations));
            assertTrue(contains(Red.class, annotations));
            assertTrue(contains(Crimson.class, annotations));

            assertEquals("red", get(Color.class, annotations).value());

            assertEquals(3, annotations.length);
        }


        { // Circular - Egg wins
            Annotation[] annotations = getAnnotations(map, "store");

            assertTrue(contains(Color.class, annotations));
            assertTrue(contains(Egg.class, annotations));
            assertTrue(contains(Chicken.class, annotations));

            assertEquals("egg", get(Color.class, annotations).value());

            assertEquals(3, annotations.length);
        }


        { // Circular - Chicken wins
            Annotation[] annotations = getAnnotations(map, "farm");

            assertTrue(contains(Color.class, annotations));
            assertTrue(contains(Egg.class, annotations));
            assertTrue(contains(Chicken.class, annotations));

            assertEquals("chicken", get(Color.class, annotations).value());

            assertEquals(3, annotations.length);
        }

    }

    private Annotation[] getAnnotations(Map<String, Annotated<Method>> map, String key) {
        final MetaAnnotatedMethod method = (MetaAnnotatedMethod) map.get(key);

        assertNotNull(method);

        return method.getParameterAnnotations()[0];
    }

    public <T extends Annotation> T get(Class<T> type, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == type) return (T) annotation;
        }
        return null;
    }

    private boolean contains(Class<? extends Annotation> type, Annotation[] annotations) {
        return get(type, annotations) != null;
    }

    // 100% your own annotations, even the @Metatype annotation
    // Any annotation called @Metatype and annotated with itself works
    @Metatype
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ANNOTATION_TYPE)
    public @interface Metatype {
    }

    @Target({PARAMETER})
    @Retention(RUNTIME)
    public static @interface Color {
        String value() default "";
    }

    @Metatype
    @Target({PARAMETER})
    @Retention(RUNTIME)
    public static @interface Red {
        public interface $ {

            // one level deep
            public void method(
                    @Red
                    @Color("red")
                    Object object);
        }
    }

    @Metatype
    @Target({PARAMETER})
    @Retention(RUNTIME)
    public static @interface Crimson {
        public interface $ {

            // two levels deep
            public void method(
                    @Crimson
                    @Red
                    Object object);
        }
    }

    // Green is intentionally not used in the classes
    // passed directly to the finder to ensure that
    // the finder is capable of following the path to
    // the root annotation even when some of the
    // annotations in the path are not strictly part
    // of the archive
    @Metatype
    @Target({PARAMETER})
    @Retention(RUNTIME)
    public static @interface Green {
        public interface $ {

            // two levels deep
            public void method(
                    @Green
                    @Color("green")
                    Object object);
        }
    }

    @Metatype
    @Target({PARAMETER})
    @Retention(RUNTIME)
    public static @interface DarkGreen {
        public interface $ {

            public void method(
                    @DarkGreen
                    @Green
                    Object object);
        }
    }


    @Metatype
    @Target({PARAMETER})
    @Retention(RUNTIME)
    public static @interface Forrest {
        public interface $ {

            public void method(
                    @Forrest
                    @DarkGreen
                    Object object);
        }
    }

    @Metatype
    @Target({PARAMETER})
    @Retention(RUNTIME)
    public static @interface Chicken {
        public interface $ {

            public void method(
                    @Chicken
                    @Color("chicken")
                    @Egg
                    Object object);
        }
    }

    @Metatype
    @Target({PARAMETER})
    @Retention(RUNTIME)
    public static @interface Egg {
        public interface $ {

            public void method(
                    @Egg
                    @Color("egg")
                    @Chicken
                    Object object);
        }
    }

    public static class Square {

        public void square(@Red Object object) {
        }
    }

    public static class Circle {

        public void circle(@Red @Color("white") Object object) {
        }
    }

    public static class Triangle {

        public void triangle(@Crimson Object object) {
        }
    }

    public static class Oval {

        public void oval(@Forrest Object object) {
        }
    }

    // always good to have a fake in there
    public static class None {

        public void none(Object object) {
        }
    }

    public static class Store {


        public void store(@Egg Object object) {
        }

    }

    public static class Farm {


        public void farm(@Chicken Object object) {
        }

    }

}