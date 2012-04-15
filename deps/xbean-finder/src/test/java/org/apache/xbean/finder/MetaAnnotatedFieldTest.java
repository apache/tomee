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

import junit.framework.TestCase;
import org.apache.xbean.finder.archive.ClassesArchive;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @version $Rev$ $Date$
 */
public class MetaAnnotatedFieldTest extends TestCase {

    public void test() throws Exception {
        AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(Square.class, Circle.class, Triangle.class, Oval.class, Store.class, Farm.class, None.class)).link();

        Map<String, Annotated<Field>> map = new HashMap<String, Annotated<Field>>();

        List<Annotated<Field>> fields = finder.findMetaAnnotatedFields(Color.class);
        for (Annotated<Field> field : fields) {
            Annotated<Field> oldValue = map.put(field.get().getName(), field);
            assertNull("no duplicates allowed", oldValue);
        }

        // Check the negative scenario
        assertFalse(map.containsKey("none"));

        // Check the positive scenarios
        {
            Annotated<Field> target = map.get("circle");
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("white", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Red.class));
            assertTrue(target.getAnnotation(Red.class) != null);
            assertTrue(contains(Red.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Red.class, target.getAnnotations()));

            assertEquals(2, target.getDeclaredAnnotations().length);
            assertEquals(2, target.getAnnotations().length);
        }

        {
            Annotated<Field> target = map.get("square");
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
            Annotated<Field> target = map.get("triangle");
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("red", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Red.class));
            assertTrue(target.getAnnotation(Red.class) != null);
            assertTrue(!contains(Red.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Red.class, target.getAnnotations()));

            assertTrue(target.isAnnotationPresent(Crimson.class));
            assertTrue(target.getAnnotation(Crimson.class) != null);
            assertTrue(contains(Crimson.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Crimson.class, target.getAnnotations()));

            assertEquals(1, target.getDeclaredAnnotations().length);
            assertEquals(3, target.getAnnotations().length);
        }

        { // Circular - Egg wins
            Annotated<Field> target = map.get("store");
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("egg", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Egg.class));
            assertTrue(target.getAnnotation(Egg.class) != null);
            assertTrue(contains(Egg.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Egg.class, target.getAnnotations()));

            assertTrue(target.isAnnotationPresent(Chicken.class));
            assertTrue(target.getAnnotation(Chicken.class) != null);
            assertTrue(!contains(Chicken.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Chicken.class, target.getAnnotations()));

            assertEquals(1, target.getDeclaredAnnotations().length);
            assertEquals(3, target.getAnnotations().length);
        }

        { // Circular - Chicken wins
            Annotated<Field> target = map.get("farm");
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("chicken", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Egg.class));
            assertTrue(target.getAnnotation(Egg.class) != null);
            assertTrue(!contains(Egg.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Egg.class, target.getAnnotations()));

            assertTrue(target.isAnnotationPresent(Chicken.class));
            assertTrue(target.getAnnotation(Chicken.class) != null);
            assertTrue(contains(Chicken.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Chicken.class, target.getAnnotations()));

            assertEquals(1, target.getDeclaredAnnotations().length);
            assertEquals(3, target.getAnnotations().length);
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

    @Target({FIELD})
    @Retention(RUNTIME)
    public static @interface Color {
        String value () default "";
    }

    @Metatype
    @Target({FIELD})
    @Retention(RUNTIME)
    public static @interface Red {
        public class $ {

            @Red
            @Color("red")  // one level deep
            private Object field;
        }
    }

    @Metatype
    @Target({FIELD})
    @Retention(RUNTIME)
    public static @interface Crimson {
        public class $ {

            @Crimson
            @Red  // two levels deep
            private Object field;
        }
    }

    // Green is intentionally not used in the classes
    // passed directly to the finder to ensure that
    // the finder is capable of following the path to
    // the root annotation even when some of the
    // annotations in the path are not strictly part
    // of the archive
    @Metatype
    @Target({FIELD})
    @Retention(RUNTIME)
    public static @interface Green {
        public class $ {

            @Green
            @Color("green")  // two levels deep
            private Object field;
        }
    }
    
    @Metatype
    @Target({FIELD})
    @Retention(RUNTIME)
    public static @interface DarkGreen {
        public class $ {

            @DarkGreen
            @Green
            private Object field;
        }
    }

    
    @Metatype
    @Target({FIELD})
    @Retention(RUNTIME)
    public static @interface Forrest {
        public class $ {

            @Forrest
            @DarkGreen
            private Object field;
        }
    }

    @Metatype
    @Target({FIELD})
    @Retention(RUNTIME)
    public static @interface Chicken {
        public class $ {

            @Chicken
            @Color("chicken")
            @Egg
            private Object field;
        }
    }

    @Metatype
    @Target({FIELD})
    @Retention(RUNTIME)
    public static @interface Egg {
        public class $ {

            @Egg
            @Color("egg")
            @Chicken
            private Object field;
        }
    }

    public static class Square {

        @Red // -> @Color
        private Object square;
    }

    public static class Circle {

        @Red // will be covered up by @Color
        @Color("white")
        private Object circle;
    }

    public static class Triangle {

        @Crimson // -> @Red -> @Color
        private Object triangle;
    }

    public static class Oval {

        @Forrest // -> @Green -> @Color
        private Object oval;
    }

    // always good to have a fake in there
    public static class None {

        private Object none;
    }

    public static class Store {

        @Egg
        private Object store;

    }

    public static class Farm {

        @Chicken
        private Object farm;

    }

}