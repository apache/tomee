/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.enhance;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Tests that {@link Reflection#getDeclaredMethod(Class, String, Class)}
 * returns the most-derived class's method when called from a type hierarchy.
 * See OPENJPA-251.
 */
public class TestGetDeclaredMethod extends TestCase {

    public void testGetDeclaredMethod() {
        Method meth =
            Reflection.getDeclaredMethod(Impl.class, "getObject", null);
        assertEquals(Impl.class, meth.getDeclaringClass());
        assertEquals(String.class, meth.getReturnType());
    }

    public void testMostDerived() throws NoSuchMethodException {
        Method impl = Impl.class.getDeclaredMethod("getObject", null);
        Method iface = Iface.class.getDeclaredMethod("getObject", null);
        Method other = Other.class.getDeclaredMethod("getObject", null);
        assertEquals(Impl.class, Reflection.mostDerived(impl, iface)
            .getDeclaringClass());
        assertEquals(Impl.class, Reflection.mostDerived(iface, impl)
            .getDeclaringClass());
        try {
            Reflection.mostDerived(iface, other);
            fail("'iface' and 'other' are not from related types");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testGenerics() throws NoSuchMethodException {
        List<Method> meths = new ArrayList<Method>();
        for (Method meth : GenericsImpl.class.getDeclaredMethods()) {
            if ("getObject".equals(meth.getName()))
                meths.add(meth);
        }
        assertEquals(2, meths.size());
        assertEquals(String.class, Reflection.mostDerived(meths.get(0),
            meths.get(1)).getReturnType());
    }
    
    interface Iface {
        Object getObject();
    }

    static class Impl implements Iface {
        public String getObject() {
            return "string";
        }
    }

    static class Other {
        public String getObject() {
            return "other";
        }
    }

    interface GenericsIface<T> {
        public T getObject();
    }

    static class GenericsImpl implements GenericsIface {
        public String getObject() {
            return null;
        }
    }
}
