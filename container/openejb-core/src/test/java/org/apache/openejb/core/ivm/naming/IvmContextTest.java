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
import org.apache.openejb.util.Debug;
import org.apache.openejb.util.Join;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.naming.OperationNotSupportedException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.SystemException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

/**
 * @version $Rev$ $Date$
 */
public class IvmContextTest extends TestCase {
    private Map<String, Integer> map;
    private IvmContext context;

    public void testLookups() throws Exception {
        // lookup
        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            final String name = entry.getKey();
            final Integer expected = entry.getValue();
            visit(context, name, new Visitor() {
                public void visit(final Context context, final String name, final String parentName) throws NamingException {
                    assertLookup("relative lookup " + parentName + " : " + name, context, name, expected);
                }
            });
        }
    }

    public void testList() throws Exception {
        // list
        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            final String name = entry.getKey();
            visit(context, name, new Visitor() {
                public void visit(final Context context, final String name, final String parentName) throws NamingException {

                    final Map<String, Object> expected = new TreeMap<>();

                    for (final Map.Entry<String, Integer> entry : map.entrySet()) {
                        String key = entry.getKey();
                        if (key.startsWith(parentName)) {
                            key = key.substring(parentName.length(), key.length());
                            expected.put(key, entry.getValue());
                        }
                    }

                    final Map<String, Object> actual = list(context);

                    assertEquals("relative list " + parentName + " : " + name, expected, actual);
                }
            });
        }
    }

    public void setUp() throws Exception {
        map = new LinkedHashMap<>();
        map.put("color/orange", 1);
        map.put("color/blue", 2);
        map.put("color/red/scarlet", 3);
        map.put("color/red/crimson", 4);
        map.put("shape", 5);

        context = new IvmContext("/");

        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            context.bind(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, Object> list(final Context context) throws NamingException {
        final Map<String, Object> map = Debug.contextToMap(context);

        // Prune the context entries out
        map.entrySet().removeIf(entry -> entry.getValue() instanceof Context);

        return map;
    }

    private void assertLookup(final String message, final Context context, final String name, final Object expected) {
        try {
            final Object actual = context.lookup(name);
            assertNotNull(message, actual);
            assertEquals(message, expected, actual);
        } catch (final NamingException e) {
            fail(message + " - Exception:" + e.getClass().getName() + " : " + e.getMessage());
        }
    }

    public void test1() throws Exception {

        final IvmContext context = new IvmContext("");
        context.bind("one", 1);
        context.bind("two", 2);
        context.bind("three", 3);

        assertContextEntry(context, "one", 1);
        assertContextEntry(context, "two", 2);
        assertContextEntry(context, "three", 3);

        context.unbind("one");

        try {
            context.lookup("one");
            fail("name should be unbound");
        } catch (final javax.naming.NameNotFoundException e) {
            // pass
        }

        // The other entries should still be there
//        assertContextEntry(context, "one", 1);
        assertContextEntry(context, "two", 2);
        assertContextEntry(context, "three", 3);

        final Map<String, Object> map = list(context);
        assertFalse("name should not appear in bindings list", map.containsKey("one"));
    }

    public void test2() throws Exception {

        final IvmContext context = new IvmContext();
        context.bind("one", 1);
        context.bind("two", 2);
        context.bind("three", 3);

        assertContextEntry(context, "one", 1);
        assertContextEntry(context, "two", 2);
        assertContextEntry(context, "three", 3);

        context.unbind("two");

        try {
            context.lookup("two");
            fail("name should be unbound");
        } catch (final javax.naming.NameNotFoundException e) {
            // pass
        }

        // The other entries should still be there
        assertContextEntry(context, "one", 1);
        assertContextEntry(context, "three", 3);

        final Map<String, Object> map = list(context);
        assertFalse("name should not appear in bindings list", map.containsKey("two"));
    }

    public void test3() throws Exception {

        final IvmContext context = new IvmContext();
        context.bind("veggies/tomato/roma", 33);
        context.bind("fruit/apple/grannysmith", 22);
        context.bind("fruit/orange/mandarin", 44);

        assertContextEntry(context, "veggies/tomato/roma", 33);
        assertContextEntry(context, "fruit/apple/grannysmith", 22);
        assertContextEntry(context, "fruit/orange/mandarin", 44);

        context.unbind("fruit/apple/grannysmith");
        context.prune("fruit");

        context.unbind("veggies/tomato/roma");
        context.prune("veggies");

        try {
            context.lookup("fruit/apple/grannysmith");
            fail("name should be unbound");
        } catch (final javax.naming.NameNotFoundException pass) {
        }
        try {
            context.lookup("veggies/tomato/roma");
            fail("name should be unbound");
        } catch (final javax.naming.NameNotFoundException pass) {
        }
        try {
            context.lookup("veggies/tomato");
            fail("name should be unbound");
        } catch (final javax.naming.NameNotFoundException pass) {
        }

        try {
            context.lookup("veggies/fruit");
            fail("name should be unbound");
        } catch (final javax.naming.NameNotFoundException pass) {
        }

        final Map<String, Object> map = list(context);
        assertFalse("name should not appear in bindings list", map.containsKey("veggies/tomato/roma"));
    }

    public void testAlreadyBound() throws Exception {

        final IvmContext context = new IvmContext();
        context.bind("number", 2);
        try {
            context.bind("number", 3);
            fail("A NameAlreadyBoundException should have been thrown");
        } catch (final NameAlreadyBoundException e) {
            // pass
        }

    }

    public void test() throws Exception {

        final IvmContext context = new IvmContext();
        context.bind("comp/env/rate/work/doc/lot/pop", 1);
        context.bind("comp/env/rate/work/doc/lot/price", 2);
        context.bind("comp/env/rate/work/doc/lot/break/story", 3);

        final Object o = context.lookup("comp/env/rate/work/doc/lot/pop");
        assertNotNull(o);
        assertTrue(o instanceof Integer);
        assertEquals(o, 1);

        context.unbind("comp/env/rate/work/doc/lot/pop");

        try {
            context.lookup("comp/env/rate/work/doc/lot/pop");
            fail("name should be unbound");
        } catch (final javax.naming.NameNotFoundException e) {
            // pass
        }

        final Map<String, Object> map = list(context);
        assertFalse("name should not appear in bindings list", map.containsKey("comp/env/rate/work/doc/lot/pop"));
    }

    public void testReadOnlyThrowsExceptionByDefault() throws NamingException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final IvmContext context = new IvmContext();
        context.setReadOnly(true);

        try {
            context.bind("global/foo/Bar", "Bar");
            fail();
        } catch (OperationNotSupportedException e) {
            // ok
        }
     }
     
     public void testReadOnlyNoException() throws NamingException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final IvmContext context = new IvmContext();
        context.setReadOnly(true);

        String originalValue = System.getProperty(IvmContext.JNDI_EXCEPTION_ON_FAILED_WRITE);
        System.setProperty(IvmContext.JNDI_EXCEPTION_ON_FAILED_WRITE, Boolean.FALSE.toString());
        try {
            Context subContext = context.createSubcontext("global/foo/Bar");
            assertNull(subContext);
        } finally {
            if(originalValue == null) {
                System.clearProperty(IvmContext.JNDI_EXCEPTION_ON_FAILED_WRITE);
            } else {
                System.setProperty(IvmContext.JNDI_EXCEPTION_ON_FAILED_WRITE, originalValue);
            }
            SystemInstance.reset();
        }
     }
     
     public void testReadOnlyAppliedRecursively() throws NamingException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final IvmContext context = new IvmContext();
        Context subContext = context.createSubcontext("global/foo/Bar");

        context.setReadOnly(true);
        if(IvmContext.class.isInstance(subContext)) {
            assertTrue(IvmContext.class.cast(subContext).readOnly);
        } else { 
            throw new IllegalStateException("Naming context " + subContext + " not instance of " + IvmContext.class) ;
        }

    }
     
     /*
      * NameNode#getBinding returns new IvmContext wrapping current read-only context in some cases
      * This test checks whether the "wrapper" is also read only if the current is 
      */
     public void testGetBindingPropagatesReadOnlyFlag() throws NamingException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final IvmContext context = new IvmContext("comp");
        context.bind("env/test", "test");

        context.setReadOnly(true);
        Object result = context.lookup("env");

        if(IvmContext.class.isInstance(result)) {
            assertTrue(IvmContext.class.cast(result).readOnly);
        } else { 
            throw new IllegalStateException("Naming context " + result + " not instance of " + IvmContext.class) ;
        }

    }
     
     /*
      * NameNode#resolve returns new IvmContext wrapping current read-only context on lookup of name bound only in federated context
      * This test checks whether the "wrapper" is also read only if the current is 
      */
     public void testGetFromFederatedContextPropagatesReadOnlyFlag() throws NamingException {
        final IvmContext context = new IvmContext();
        final IvmContext federatedContext = new IvmContext("comp");
        federatedContext.bind("env/test", "test");
        context.bind("", federatedContext);

        context.setReadOnly(true);
        Object result = context.lookup("env");

        if(IvmContext.class.isInstance(result)) {
            assertTrue(IvmContext.class.cast(result).readOnly);
        } else { 
            throw new IllegalStateException("Naming context " + result + " not instance of " + IvmContext.class) ;
        }
    }

    public void testCloseNoExceptionByDefault() throws NamingException {
        final IvmContext context = new IvmContext();
        try {
            context.close();
        } catch (OperationNotSupportedException e) {
            fail();
        }
    }

    public void testCloseDoesNotThrowExceptionIfReadOnly() throws NamingException {
        final IvmContext context = new IvmContext();
        context.setReadOnly(true);
        try {
            context.close();
            //ok
        } catch (OperationNotSupportedException e) {
            fail();
        }
    }
    
   public void testListContextListsAllFederatedContextBindings() throws SystemException, NamingException {
	   //mimic logic from EnterpriseBeanBuilder.build, create compJndiContext and bind in it module, app, global 
	   Context compContext = new IvmContext();
        compContext.bind("java:comp/env/dummy", "dummy");

        Context moduleContext = new IvmContext();
        moduleContext.bind("module/env/test", String.class);
        moduleContext.bind("module/env/sub/test2", String.class);
        Context originalModuleSubContext = (IvmContext)moduleContext.lookup("module");
        compContext.bind("module", originalModuleSubContext);

        Context referencedModuleEnvSubContext = (IvmContext)compContext.lookup("module/env");
        NamingEnumeration<NameClassPair> referencedEnvLookupResult = referencedModuleEnvSubContext.list("");

        boolean testFound= false;
        boolean subFound = false;
        while(referencedEnvLookupResult.hasMore()) {
            String currentName = referencedEnvLookupResult.next().getName();
            if("test".equals(currentName)) {
                testFound = true;
            } else if("sub".equals(currentName)) {
                subFound = true;
            } else {
                fail();
            }
        }
        assertTrue(testFound);
        assertTrue(subFound);
     }

    private void assertContextEntry(final Context context, final String s, final Object expected) throws javax.naming.NamingException {
        assertLookup(context, s, expected);
    }

    public interface Visitor {
        public void visit(Context context, String name, String parentName) throws NamingException;
    }

    private void visit(final Context context, final String name, final Visitor visitor) throws NamingException {
        visit(context, name, "", visitor);
    }

    private void visit(final Context context, final String name, final String parentName, final Visitor visitor) throws NamingException {
        visitor.visit(context, name, parentName);

        final String[] parts = name.split("/");

        if (parts.length > 1) {
            final String thisPart = parts[0];
            final Object o = context.lookup(thisPart);
            assertNotNull(o);
            assertTrue(o instanceof Context);
            visit((Context) o, subpath(parts), parentName + thisPart + "/", visitor);
        }
    }

    private void _visit(final Context context, final String name, final Object expected) throws NamingException {
        // bind
//        try {
//            context.bind(s, expected);
//            fail("should not be allowed to bind");
//        } catch (NameAlreadyBoundException e) {
//            // pass
//        }

        // rebind
//        String tmp = expected.toString() + System.currentTimeMillis();
//        context.rebind(s, tmp);
//        assertLookup(context, s, tmp);

        // unbind
//        context.unbind(s);
//        try {
//            context.lookup(s);
//            fail("name should be unbound");
//        } catch (NameNotFoundException e) {
//            // pass
//        }

        // Restore the original state
//        context.bind(s, expected);
//        assertLookup(context, s, expected);
    }

    private void assertLookup(final Context context, final String s, final Object expected) throws NamingException {
        final Object actual = context.lookup(s);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    private String subpath(final String[] strings) {
        final String[] strings2 = new String[strings.length - 1];
        System.arraycopy(strings, 1, strings2, 0, strings2.length);

        final String path = Join.join("/", strings2);
        return path;
    }
}
