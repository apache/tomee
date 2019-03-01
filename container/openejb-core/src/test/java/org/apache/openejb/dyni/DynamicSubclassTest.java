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
package org.apache.openejb.dyni;

import org.apache.openejb.util.Join;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Implementation of methods and constructors:
 * <p/>
 * - Ensure all abstract methods were implemented to delegate to the InvocationHandler method
 * - Ensure all constructors were carried forward to subclass
 * <p/>
 * Preservation of annotations
 * <p/>
 * - Ensure all annotations from the immediate parent class were copied
 * - Ensure all annotations from parent constructors and constructor params were copied
 * - Ensure all annotations from abstract ancestor methods and method params were copied
 *
 * @version $Rev$ $Date$
 */
public class DynamicSubclassTest extends Assert {

    private static Invocation invocation;

    @Test
    public void test() throws Exception {
        final URLClassLoader loader = new URLClassLoader(new URL[0]);

        final Class subclass = DynamicSubclass.createSubclass(Blue.class, loader);

        final Constructor constructor = subclass.getConstructor(long.class);
        final Blue blue = (Blue) constructor.newInstance(1L);

        final Class<?> generatedClass = blue.getClass();
        assertNotEquals(Blue.class, generatedClass);

        // Were class, constructor and constructor parameter annotations copied?
        {
            // class annotations?
            assertEquals("blue", generatedClass.getAnnotation(Circle.class).value());

            // constructor annotations?
            assertEquals("blue()", generatedClass.getConstructor().getAnnotation(Oval.class).value());
            assertEquals("blue(long)", generatedClass.getConstructor(long.class).getAnnotation(Oval.class).value());

            // constructor parameter annotations?
            final Annotation annotation = generatedClass.getConstructor(long.class).getParameterAnnotations()[0][0];
            assertEquals("1", ((Triangle) annotation).value());
        }

        { // blue method
            blue.blue(1);
            assertNotNull(invocation);
            final Method method = Blue.class.getDeclaredMethod("blue", int.class);
            assertEquals(invocation.getMethod(), method);
            assertEquals("1", Join.join(",", invocation.args));
            assertEquals("blue", method.getAnnotation(Square.class).value());
            assertEquals("blue", ((Triangle) method.getParameterAnnotations()[0][0]).value());
        }

        { // green method
            blue.green("hello");
            assertNotNull(invocation);
            final Method method = Green.class.getDeclaredMethod("green", String.class);
            assertEquals(invocation.getMethod(), method);
            assertEquals("hello", Join.join(",", invocation.args));
            assertEquals("green", method.getAnnotation(Square.class).value());
            assertEquals("green", ((Triangle) method.getParameterAnnotations()[0][0]).value());
        }

        { // blue method
            blue.red(URI.create("foo://bar"));
            assertNotNull(invocation);
            final Method method = Red.class.getDeclaredMethod("red", URI.class);
            assertEquals(invocation.getMethod(), method);
            assertEquals("foo://bar", Join.join(",", invocation.getArgs()));
            assertEquals("red", method.getAnnotation(Square.class).value());
            assertEquals("red", ((Triangle) method.getParameterAnnotations()[0][0]).value());
        }
    }

    public static class Invocation {
        private final Object proxy;
        private final Method method;
        private final Object[] args;

        public Invocation(final Object proxy, final Method method, final Object[] args) {
            this.proxy = proxy;
            this.method = method;
            this.args = args;
        }

        public Object getProxy() {
            return proxy;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArgs() {
            return args;
        }
    }

    public static abstract class Color implements InvocationHandler {

        public Color() {
        }

        public Color(final URI uri, final long foo) {
        }

        // TODO: check to ensure this method is implemented, issue validation failure if not
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            invocation = new Invocation(proxy, method, args);
            return null;
        }
    }

    @Circle("red")
    public static abstract class Red extends Color {

        @Square("red")
        public abstract void red(@Triangle("red") URI uri);
    }

    @Circle("green")
    public static abstract class Green extends Red {

        @Square("green")
        public abstract void green(@Triangle("green") String v);
    }

    @Circle("blue")
    public static abstract class Blue extends Green {

        @Oval("blue()")
        public Blue() {
        }

        @Oval("blue(long)")
        public Blue(@Triangle("1") final long l) {
        }

        @Square("blue")
        public abstract void blue(@Triangle("blue") int v);
    }


    @Target(value = ElementType.TYPE)
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface Circle {
        String value();
    }

    @Target(value = ElementType.CONSTRUCTOR)
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface Oval {
        String value();
    }

    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface Square {
        String value();
    }

    @Target(value = ElementType.PARAMETER)
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface Triangle {
        String value();
    }


}
