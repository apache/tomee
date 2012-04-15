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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @version $Rev$ $Date$
 */
public class MetaAnnotatedMethodTest extends TestCase {

    public void test() throws Exception {
        AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(Square.class, Circle.class, Triangle.class, Oval.class, Store.class, Farm.class, None.class)).link();

        Map<String, Annotated<Method>> map = new HashMap<String, Annotated<Method>>();

        List<Annotated<Method>> methods = finder.findMetaAnnotatedMethods(Color.class);
        for (Annotated<Method> method : methods) {
            Annotated<Method> oldValue = map.put(method.get().getName(), method);
            assertNull("no duplicates allowed", oldValue);
        }

        // Check the negative scenario
        assertFalse(map.containsKey("none"));

        // Check the positive scenarios
        {
            Annotated<Method> target = map.get("circle");
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
            Annotated<Method> target = map.get("square");
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
            Annotated<Method> target = map.get("triangle");
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
            Annotated<Method> target = map.get("store");
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
            Annotated<Method> target = map.get("farm");
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

    @Target({METHOD})
    @Retention(RUNTIME)
    public static @interface Color {
        String value () default "";
    }

    @Metatype
    @Target({METHOD})
    @Retention(RUNTIME)
    public static @interface Red {
        public interface $ {

            @Red
            @Color("red")  // one level deep
            public void method();
        }
    }

    @Metatype
    @Target({METHOD})
    @Retention(RUNTIME)
    public static @interface Crimson {
        public interface $ {

            @Crimson
            @Red  // two levels deep
            public void method();
        }
    }

    // Green is intentionally not used in the classes
    // passed directly to the finder to ensure that
    // the finder is capable of following the path to
    // the root annotation even when some of the
    // annotations in the path are not strictly part
    // of the archive
    @Metatype
    @Target({METHOD})
    @Retention(RUNTIME)
    public static @interface Green {
        public interface $ {

            @Green
            @Color("green")  // two levels deep
            public void method();
        }
    }
    
    @Metatype
    @Target({METHOD})
    @Retention(RUNTIME)
    public static @interface DarkGreen {
        public interface $ {

            @DarkGreen
            @Green
            public void method();
        }
    }

    
    @Metatype
    @Target({METHOD})
    @Retention(RUNTIME)
    public static @interface Forrest {
        public interface $ {

            @Forrest
            @DarkGreen
            public void method();
        }
    }

    @Metatype
    @Target({METHOD})
    @Retention(RUNTIME)
    public static @interface Chicken {
        public interface $ {

            @Chicken
            @Color("chicken")
            @Egg
            public void method();
        }
    }

    @Metatype
    @Target({METHOD})
    @Retention(RUNTIME)
    public static @interface Egg {
        public interface $ {

            @Egg
            @Color("egg")
            @Chicken
            public void method();
        }
    }

    public static class Square {

        @Red // -> @Color
        public void square(String s, int i){}
    }

    public static class Circle {

        @Red // will be covered up by @Color
        @Color("white")
        public void circle(int i){}
    }

    public static class Triangle {

        @Crimson // -> @Red -> @Color
        public void triangle(boolean... b){}
    }

    public static class Oval {

        @Forrest // -> @Green -> @Color
        public void oval(boolean... b){}
    }

    // always good to have a fake in there
    public static class None {

        public void none(List<String> l){}
    }

    public static class Store {

        @Egg
        public void store(){}

    }

    public static class Farm {

        @Chicken
        public void farm(){}

    }

}