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
package org.apache.openejb.core.ivm.naming;

import junit.framework.TestCase;

import java.util.Map;

import org.apache.openejb.util.Debug;

import javax.naming.*;
import javax.naming.NamingException;

/**
 * @version $Rev$ $Date$
 */
public class IvmContextTest extends TestCase {
    public void test1() throws Exception {

        IvmContext context = new IvmContext();
        context.bind("root/one", 1);
        context.bind("root/two", 2);
        context.bind("root/three", 3);

        assertContextEntry(context, "one", 1);
        assertContextEntry(context, "two", 2);
        assertContextEntry(context, "three", 3);

        context.unbind("root/one");

        try {
            context.lookup("one");
            fail("name should be unbound");
        } catch (javax.naming.NameNotFoundException e) {
            // pass
        }

        // The other entries should still be there
//        assertContextEntry(context, "one", 1);
        assertContextEntry(context, "two", 2);
        assertContextEntry(context, "three", 3);

        Map<String, Object> map = Debug.contextToMap(context);
        assertFalse("name should not appear in bindings list", map.containsKey("one"));
    }

    public void test2() throws Exception {

        IvmContext context = new IvmContext();
        context.bind("root/one", 1);
        context.bind("root/two", 2);
        context.bind("root/three", 3);

        assertContextEntry(context, "one", 1);
        assertContextEntry(context, "two", 2);
        assertContextEntry(context, "three", 3);

        context.unbind("root/two");

        try {
            context.lookup("two");
            fail("name should be unbound");
        } catch (javax.naming.NameNotFoundException e) {
            // pass
        }

        // The other entries should still be there
        assertContextEntry(context, "one", 1);
        assertContextEntry(context, "three", 3);

        Map<String, Object> map = Debug.contextToMap(context);
        assertFalse("name should not appear in bindings list", map.containsKey("two"));
    }

    public void test3() throws Exception {

        IvmContext context = new IvmContext();
        context.bind("root/veggies/tomato/roma", 33);
        context.bind("root/fruit/apple/grannysmith", 22);
        context.bind("root/fruit/orange/mandarin", 44);

        assertContextEntry(context, "veggies/tomato/roma", 33);
        assertContextEntry(context, "fruit/apple/grannysmith", 22);
        assertContextEntry(context, "fruit/orange/mandarin", 44);

        context.unbind("root/fruit/apple/grannysmith");
        context.prune("fruit");

        context.unbind("root/veggies/tomato/roma");
        context.prune("veggies");

        try {
            context.lookup("fruit/apple/grannysmith");
            fail("name should be unbound");
        } catch (javax.naming.NameNotFoundException pass) {
        }
        try {
            context.lookup("veggies/tomato/roma");
            fail("name should be unbound");
        } catch (javax.naming.NameNotFoundException pass) {
        }
        try {
            context.lookup("veggies/tomato");
            fail("name should be unbound");
        } catch (javax.naming.NameNotFoundException pass) {
        }

        try {
            context.lookup("veggies/fruit");
            fail("name should be unbound");
        } catch (javax.naming.NameNotFoundException pass) {
        }

        Map<String, Object> map = Debug.contextToMap(context);
        assertFalse("name should not appear in bindings list", map.containsKey("veggies/tomato/roma"));
    }

    public void testAlreadyBound() throws Exception {

        IvmContext context = new IvmContext();
        context.bind("root/number", 2);
        try {
            context.bind("root/number", 3);
            fail("A NameAlreadyBoundException should have been thrown");
        } catch (NameAlreadyBoundException e) {
            // pass
        }

    }

    public void test() throws Exception {

        IvmContext context = new IvmContext();
        context.bind("root/comp/env/rate/work/doc/lot/pop", new Integer(1));
        context.bind("root/comp/env/rate/work/doc/lot/price", new Integer(2));
        context.bind("root/comp/env/rate/work/doc/lot/break/story", new Integer(3));

        Object o = context.lookup("comp/env/rate/work/doc/lot/pop");
        assertNotNull(o);
        assertTrue(o instanceof Integer);
        assertEquals(o, new Integer(1));

        context.unbind("root/comp/env/rate/work/doc/lot/pop");

        try {
            context.lookup("comp/env/rate/work/doc/lot/pop");
            fail("name should be unbound");
        } catch (javax.naming.NameNotFoundException e) {
            // pass
        }

        Map<String, Object> map = Debug.contextToMap(context);
        assertFalse("name should not appear in bindings list", map.containsKey("comp/env/rate/work/doc/lot/pop"));
    }

    private void assertContextEntry(Context context, String s, Object value) throws javax.naming.NamingException {
        try {
            Object two = context.lookup(s);
            assertNotNull(two);
            assertEquals(two, value);
            // pass
        } catch (NameNotFoundException e) {
            fail("name '"+ s +"' not found.");
        }
    }

}
