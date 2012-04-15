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
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @version $Rev$ $Date$
 */
public class MetaAnnotatedConstructorTest extends TestCase {

    public void test() throws Exception {

        final Class<?>[] classes = new Class[]{Square.class, Circle.class, Triangle.class, Oval.class, Store.class, Farm.class, None.class};

        final Map<String, Annotated<Constructor>> map = new HashMap<String, Annotated<Constructor>>();

        for (Class<?> clazz : classes) {
            final MetaAnnotatedClass<?> annotatedClass = new MetaAnnotatedClass(clazz);

            for (MetaAnnotatedConstructor constructor : annotatedClass.getConstructors()) {
                map.put(annotatedClass.getSimpleName().toLowerCase(), constructor);
            }
        }

        // Check the positive scenarios
        {
            final java.lang.reflect.AnnotatedElement element = map.get("circle");
            assertNotNull(element);

            assertTrue(element.isAnnotationPresent(Color.class));
            assertTrue(element.getAnnotation(Color.class) != null);
            assertTrue(contains(Color.class, element.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, element.getAnnotations()));
            assertEquals("white", element.getAnnotation(Color.class).value());

            assertTrue(element.isAnnotationPresent(Red.class));
            assertTrue(element.getAnnotation(Red.class) != null);
            assertTrue(contains(Red.class, element.getDeclaredAnnotations()));
            assertTrue(contains(Red.class, element.getAnnotations()));

            assertEquals(2, element.getDeclaredAnnotations().length);
            assertEquals(2, element.getAnnotations().length);
        }

        {
            final java.lang.reflect.AnnotatedElement target = map.get("square");
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("red", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Red.class));
            assertTrue(target.getAnnotation(Red.class) != null);
            assertTrue(contains(Red.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Red.class, target.getAnnotations()));

            assertEquals(1, target.getDeclaredAnnotations().length);
            assertEquals(2, target.getAnnotations().length);
        }

        {
            final java.lang.reflect.AnnotatedElement annotated = map.get("triangle");
            assertNotNull(annotated);

            assertTrue(annotated.isAnnotationPresent(Color.class));
            assertTrue(annotated.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, annotated.getAnnotations()));
            assertEquals("red", annotated.getAnnotation(Color.class).value());

            assertTrue(annotated.isAnnotationPresent(Red.class));
            assertTrue(annotated.getAnnotation(Red.class) != null);
            assertTrue(!contains(Red.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Red.class, annotated.getAnnotations()));

            assertTrue(annotated.isAnnotationPresent(Crimson.class));
            assertTrue(annotated.getAnnotation(Crimson.class) != null);
            assertTrue(contains(Crimson.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Crimson.class, annotated.getAnnotations()));

            assertEquals(1, annotated.getDeclaredAnnotations().length);
            assertEquals(3, annotated.getAnnotations().length);
        }

        { // Circular - Egg wins
            final java.lang.reflect.AnnotatedElement annotated = map.get("store");
            assertNotNull(annotated);

            assertTrue(annotated.isAnnotationPresent(Color.class));
            assertTrue(annotated.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, annotated.getAnnotations()));
            assertEquals("egg", annotated.getAnnotation(Color.class).value());

            assertTrue(annotated.isAnnotationPresent(Egg.class));
            assertTrue(annotated.getAnnotation(Egg.class) != null);
            assertTrue(contains(Egg.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Egg.class, annotated.getAnnotations()));

            assertTrue(annotated.isAnnotationPresent(Chicken.class));
            assertTrue(annotated.getAnnotation(Chicken.class) != null);
            assertTrue(!contains(Chicken.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Chicken.class, annotated.getAnnotations()));

            assertEquals(1, annotated.getDeclaredAnnotations().length);
            assertEquals(3, annotated.getAnnotations().length);
        }

        { // Circular - Chicken wins
            final java.lang.reflect.AnnotatedElement annotated = map.get("farm");
            assertNotNull(annotated);

            assertTrue(annotated.isAnnotationPresent(Color.class));
            assertTrue(annotated.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, annotated.getAnnotations()));
            assertEquals("chicken", annotated.getAnnotation(Color.class).value());

            assertTrue(annotated.isAnnotationPresent(Egg.class));
            assertTrue(annotated.getAnnotation(Egg.class) != null);
            assertTrue(!contains(Egg.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Egg.class, annotated.getAnnotations()));

            assertTrue(annotated.isAnnotationPresent(Chicken.class));
            assertTrue(annotated.getAnnotation(Chicken.class) != null);
            assertTrue(contains(Chicken.class, annotated.getDeclaredAnnotations()));
            assertTrue(contains(Chicken.class, annotated.getAnnotations()));

            assertEquals(1, annotated.getDeclaredAnnotations().length);
            assertEquals(3, annotated.getAnnotations().length);
        }

    }

    private boolean contains(Class<? extends Annotation> type, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (type.isAssignableFrom(annotation.annotationType())) return true;
        }
        return false;
    }

    // 100% your own annotations, even the @Metatype annotation
    // Any annotation called @Metatype and annotated with itself works
    @Metatype
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ANNOTATION_TYPE)
    public @interface Metatype {
    }

    @Target({CONSTRUCTOR})
    @Retention(RUNTIME)
    public static @interface Color {
        String value() default "";
    }

    @Metatype
    @Target({CONSTRUCTOR})
    @Retention(RUNTIME)
    public static @interface Red {
        public class $ {

            @Red
            @Color("red")  // one level deep
            public $(){}
        }
    }

    @Metatype
    @Target({CONSTRUCTOR})
    @Retention(RUNTIME)
    public static @interface Crimson {
        public class $ {

            @Crimson
            @Red  // two levels deep
            public $(){}
        }
    }

    // Green is intentionally not used in the classes
    // passed directly to the finder to ensure that
    // the finder is capable of following the path to
    // the root annotation even when some of the
    // annotations in the path are not strictly part
    // of the archive
    @Metatype
    @Target({CONSTRUCTOR})
    @Retention(RUNTIME)
    public static @interface Green {
        public class $ {

            @Green
            @Color("green")  // two levels deep
            public $(){}
        }
    }

    @Metatype
    @Target({CONSTRUCTOR})
    @Retention(RUNTIME)
    public static @interface DarkGreen {
        public class $ {

            @DarkGreen
            @Green
            public $(){}
        }
    }


    @Metatype
    @Target({CONSTRUCTOR})
    @Retention(RUNTIME)
    public static @interface Forrest {
        public class $ {

            @Forrest
            @DarkGreen
            public $(){}
        }
    }

    @Metatype
    @Target({CONSTRUCTOR})
    @Retention(RUNTIME)
    public static @interface Chicken {
        public class $ {

            @Chicken
            @Color("chicken")
            @Egg
            public $(){}
        }
    }

    @Metatype
    @Target({CONSTRUCTOR})
    @Retention(RUNTIME)
    public static @interface Egg {
        public class $ {

            @Egg
            @Color("egg")
            @Chicken
            public $(){}
        }
    }

    public static class Square {

        @Red // -> @Color
        public Square(String s, int i) {
        }
    }

    public static class Circle {

        @Red // will be covered up by @Color
        @Color("white")
        public Circle(int i) {
        }
    }

    public static class Triangle {

        @Crimson // -> @Red -> @Color
        public Triangle(boolean... b) {
        }
    }

    public static class Oval {

        @Forrest // -> @Green -> @Color
        public Oval(boolean... b) {
        }
    }

    // always good to have a fake in there
    public static class None {

        public None(List<String> l) {
        }
    }

    public static class Store {

        @Egg
        public Store() {
        }

    }

    public static class Farm {

        @Chicken
        public Farm() {
        }

    }

}