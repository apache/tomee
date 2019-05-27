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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.ivm.naming;

import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.NameNode;
import org.apache.openejb.util.Contexts;
import org.junit.Test;

import javax.naming.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class IvmContextTest {
    @Test
    public void rebind() throws NamingException {
        final IvmContext root = IvmContext.createRootContext();
        root.rebind("global/App.EAR/foo", "test");
        final Context last = Contexts.createSubcontexts(root, "global/App.EAR/foo");
        last.rebind("foo", "test");

        // first ensure all is bound correctly
        assertEquals("test", root.lookup("global/App.EAR/foo"));
        assertEquals("test", last.lookup("foo"));

        // even after cache clearance
        last.unbind("missing"); // clear cache
        assertEquals("test", root.lookup("global/App.EAR/foo"));
        assertEquals("test", last.lookup("foo"));

        // now rebound, shouldnt throw any exception
        final Context lastContext = Contexts.createSubcontexts(root, "global/App.EAR/foo");
        lastContext.rebind("foo", "test2");
        assertSame(lastContext, last);
        root.rebind("global/App.EAR/foo", "test2");
        assertEquals("test2", root.lookup("global/App.EAR/foo"));
        assertEquals("test2", last.lookup("foo"));

        // even after cache clearance
        lastContext.unbind("missing");
        assertEquals("test2", root.lookup("global/App.EAR/foo"));
        assertEquals("test2", last.lookup("foo"));
    }

    @Test
    public void unbind() throws NamingException {
        final IvmContext context = new IvmContext();
        context.bind("global/foo/Bar", "Bar");

        assertEquals("Bar", context.lookup("global/foo/Bar"));

        context.unbind("global/foo/Bar");

        try {
            context.lookup("global/foo/Bar");
            fail();
        } catch (final NamingException ne) {
            // ok
        }

        try {
            final Context subCtx = (Context) context.lookup("global/foo");
            subCtx.lookup("Bar");
            fail();
        } catch (final NamingException ne) {
            // ok
        }
    }

    @Test
    public void unbindWithSubContext() throws NamingException {
        final IvmContext context = new IvmContext("");
        context.bind("global/foo/Bar", "Bar");

        Contexts.createSubcontexts(context, "global/foo/Bar");

        assertEquals("Bar", context.lookup("global/foo/Bar"));

        context.unbind("global/foo/Bar");

        try {
            context.lookup("global/foo/Bar");
            fail();
        } catch (final NamingException ne) {
            // ok
        }

        try {
            ((Context) ((Context) context.lookup("global")).lookup("foo"))
                    .lookup("Bar");
            fail();
        } catch (final NamingException ne) {
            // ok
        }
    }

    private void check(final NameNode node) {
        if (node == null) {
            return;
        }

        final int atomicHash = getAtomicHash(node);

        checkLess(node, node.getLessTree(), atomicHash);
        checkGrtr(node.getGrtrTree(), atomicHash);

        check(node.getLessTree());
        check(node.getGrtrTree());
    }

    private void checkLess(final NameNode reference, final NameNode node, final int hash) {
        if (node == null) {
            return;
        }

        if (getAtomicHash(node) >= hash) {
            throw new IllegalStateException(node + " >= " + reference);
        }

        checkLess(node, node.getLessTree(), hash);
        checkLess(node, node.getGrtrTree(), hash);
    }

    private void checkGrtr(final NameNode node, final int hash) {
        if (node == null) {
            return;
        }

        if (getAtomicHash(node) <= hash) {
            throw new IllegalStateException();
        }

        checkGrtr(node.getLessTree(), hash);
        checkGrtr(node.getGrtrTree(), hash);
    }

    private int getAtomicHash(final NameNode node) {
        try {
            final Field field = NameNode.class.getDeclaredField("atomicHash");
            field.setAccessible(true);
            return Integer.class.cast(field.get(node));
        } catch (Exception e) {
            throw new AssertionError();
        }
    }

    /*
     * verify that list() will return only the subcontexts
     */
    @Test
    public void testContextList_rootContexts() throws NamingException {
        final Map<String, String> expected = new HashMap<>();
        expected.put("global", "global");
        expected.put("app", "app");
        expected.put("module", "module");

        verifyListedContent(expected, "");
    }

    /*
     * verify that list() will return the properties from
     * both the normal and federated contexts
     */
    @Test
    public void testContextList_moduleEnvProperties() throws NamingException {
        final Map<String, String> expected = new HashMap<>();
        expected.put("federated-prop-1", "federated-prop-1");
        expected.put("federated-prop-2", "federated-prop-2");
        expected.put("federated-prop-3", "federated-prop-3");
        expected.put("prop-1", "prop-1");
        expected.put("prop-2", "prop-2");
        expected.put("prop-3", "prop-3");

        verifyListedContent(expected, "module/env/properties");
    }

    /*
     * verify that list() will return the correct number of contexts and properties
     * Some of the contexts have two internal instances -> i.e there is a federated
     * and ordinary "configurations"
     */
    @Test
    public void testContextList_moduleEnv() throws NamingException {
        final Map<String, String> expected = new HashMap<>();
        expected.put("properties", "properties");
        expected.put("configurations", "configurations");
        expected.put("env-1", "env-1");
        expected.put("env-2", "env-2");
        expected.put("env-3", "env-3");

        verifyListedContent(expected, "module/env");
    }

    public void verifyListedContent(Map<String, String> expected, String address) throws NamingException {
        final IvmContext root = createTestIvmContext();
        final NamingEnumeration<NameClassPair> resultSet = root.list(address);

        int numberOfListedItems = 0;
        while (resultSet.hasMoreElements()) {
            final NameClassPair nameClassPair = resultSet.nextElement();
            final String name = nameClassPair.getName();
            final Object expectedObject = expected.get(name);
            assertNotNull("The expected set does not contain object with name: " + name, expectedObject);

            //Intentionally use the whole address and lookup from the root node: TOMEE-2087
            final Object actualObject = root.lookup(address + "/" + name);

            if (actualObject instanceof IvmContext) {
                assertEquals(expectedObject, ((IvmContext) actualObject).mynode.getAtomicName());
            } else {
                assertEquals(expectedObject, actualObject);
            }
            numberOfListedItems++;
        }

        if (numberOfListedItems != expected.size()) {
            fail("IvmContext.list() returned different number of elements than expected. " +
                    "Expected=" + expected.size() + "\tActual=" + numberOfListedItems);
        }
    }

    @Test
    public void testContextList() throws NamingException {
        final IvmContext root = createTestIvmContext();
        final ByteArrayOutputStream logBuffer = new ByteArrayOutputStream();
        final PrintWriter logWriter = new PrintWriter(logBuffer);

        final boolean hasErrors = listContext(root, "", logWriter);
        logWriter.flush();

        assertFalse(logBuffer.toString(), hasErrors);
    }

    private IvmContext createTestIvmContext() throws NamingException {
        final IvmContext root = IvmContext.createRootContext();
        root.createSubcontext("global");
        root.createSubcontext("module");
        root.createSubcontext("app");

        root.bind("global/GlobalBean-1", "GlobalBean-1");
        root.bind("global/GlobalBean-2", "GlobalBean-2");
        root.bind("app/AppBean-1", "AppBean-1");
        root.bind("app/AppBean-2", "AppBean-2");

        root.bind("module/env/properties/prop-1", "prop-1");
        root.bind("module/env/properties/prop-2", "prop-2");
        root.bind("module/env/properties/prop-3", "prop-3");

        final IvmContext federatedProperties = new IvmContext();
        root.bind("module/env/properties", federatedProperties);
        federatedProperties.bind("federated-prop-1", "federated-prop-1");
        federatedProperties.bind("federated-prop-2", "federated-prop-2");
        federatedProperties.bind("federated-prop-3", "federated-prop-3");

        final IvmContext federatedConfigurations = new IvmContext();
        root.bind("module/env/configurations", federatedConfigurations);
        federatedConfigurations.bind("federated-conf-1", "federated-conf-1");
        federatedConfigurations.bind("federated-conf-2", "federated-conf-2");
        federatedConfigurations.bind("federated-conf-3", "federated-conf-3");

        root.bind("module/env/configurations/conf-1", "conf-1");
        root.bind("module/env/configurations/conf-2", "conf-2");
        root.bind("module/env/configurations/conf-3", "conf-3");

        root.bind("module/env/env-1", "env-1");
        root.bind("module/env/env-2", "env-2");
        root.bind("module/env/env-3", "env-3");

        root.bind("module/Bean-1", "Bean-1");
        root.bind("module/Bean-2", "Bean-2");
        root.bind("module/Bean-3", "Bean-3");

        return root;
    }

    private static boolean listContext(Context context, String ctxName, PrintWriter writer) throws javax.naming.NamingException {
        writer.println("\n### Context: " + ctxName);
        boolean hasErrors = false;

        final Map<Context, String> subContexts = new HashMap<>();
        final NamingEnumeration<? extends NameClassPair> content = context.list("");

        while (content.hasMoreElements()) {
            final NameClassPair nameClassPair = content.nextElement();
            final String name = nameClassPair.getName();
            final String className = nameClassPair.getClassName();

            writer.print("Name: " + name);
            writer.print("\tClass=" + className);
            writer.print("\t[looking up: " + (ctxName + "/" + name) + "]");

            try {
                //Intentionally lookup from the "current" context object using the relative name: TOMEE-2087
                final Object object = context.lookup(name);
                if (object instanceof Context) {
                    subContexts.put((Context) object, ctxName + "/" + name);
                }

                if (className.endsWith("Reference")) {
                    writer.print("\t[Reference]: " + object);
                } else {
                    writer.print("\t[Value]: " + object);
                }
            } catch (Exception ex) {
                writer.print("Failed to lookup: " + ctxName + "/" + name + "\tError: " + ex);
                hasErrors = true;
            }

            writer.println();
        }
        writer.println();

        for (Map.Entry<Context, String> contextStringEntry : subContexts.entrySet()) {
            hasErrors |= listContext(contextStringEntry.getKey(), contextStringEntry.getValue(), writer);
        }

        return hasErrors;
    }

    @Test(expected = NameAlreadyBoundException.class)
    public void testBindThrowsNameAlreadyBoundException() throws Exception{
        IvmContext root = IvmContext.createRootContext();
        root.bind("a/b/object", new Object());

        IvmContext b = (IvmContext) root.lookup("a/b");
        //already bound from root -> must fail
        b.bind("object", new Object());
    }

    @Test
    public void testBindRelativeToRootAndLookupRelativeToTheCurrentContext() throws Exception{
        final IvmContext root = IvmContext.createRootContext();
        final Object boundObject = new Object();
        root.bind("a/b/object", boundObject);

        IvmContext b = (IvmContext) root.lookup("a/b");
        final Object lookedUpObject = b.lookup("object");
        assertSame(boundObject, lookedUpObject);
    }

    @Test
    public void testBindRelativeToRootAndLookupRelativeToRoot() throws Exception{
        final IvmContext root = IvmContext.createRootContext();
        final Object boundObject = new Object();
        root.bind("a/b/object", boundObject);

        final Object lookedUpObject = root.lookup("a/b/object");
        assertSame(boundObject, lookedUpObject);
    }

    @Test
    public void testBindRelativeToCurrentContextAndLookupRelativeToRoot() throws Exception{
        final IvmContext root = IvmContext.createRootContext();
        root.bind("a/b/c", null);

        final Object boundObject = new Object();
        IvmContext b = (IvmContext) root.lookup("a/b");
        b.bind("object", boundObject);


        final Object lookedUpObject = root.lookup("a/b/object");
        assertSame(boundObject, lookedUpObject);
    }

    @Test(expected = NameNotFoundException.class)
    public void testBindFromRootUnbindFromCurrentContext() throws Exception{
        IvmContext root = IvmContext.createRootContext();
        root.bind("a/b/object", new Object());

        IvmContext b = (IvmContext) root.lookup("a/b");
        b.unbind("object");

        //must fail with NameNotFoundException
        Object object = root.lookup("a/b/object");

        fail("Lookup should have failed. Instead it returned: " + object);
    }

    @Test(expected = NameNotFoundException.class)
    public void testBindFromRootUnbindFromRoot() throws Exception{
        IvmContext root = IvmContext.createRootContext();
        root.bind("a/b/object", new Object());
        root.unbind("a/b/object");

        //must fail with NameNotFoundException
        Object object = root.lookup("a/b/object");

        fail("Lookup should have failed. Instead it returned: " + object);
    }

    @Test
    public void testBindFromRootSetsTheCorrectParent_lookupRelativeToRoot() throws Exception{
        final IvmContext root = IvmContext.createRootContext();
        root.bind("a/b/c", null);

        IvmContext a = (IvmContext) root.lookup("a");
        //TODO Do we want ROOT to be a parent to the top-level contexts ?
        requireCorrectParentChildRelationship(null, a);

        IvmContext b = (IvmContext) root.lookup("a/b");
        requireCorrectParentChildRelationship(a, b);
    }

    @Test
    public void testBindFromRootSetsTheCorrectParent_lookupRelativeToTheCurrentNode() throws Exception{
        final IvmContext root = IvmContext.createRootContext();
        root.bind("a/b/c", null);

        IvmContext a = (IvmContext) root.lookup("a");
        //TODO Do we want ROOT to be a parent to the top-level contexts ?
        requireCorrectParentChildRelationship(null, a);

        IvmContext b = (IvmContext) a.lookup("b");
        requireCorrectParentChildRelationship(a, b);
    }

    @Test
    public void testBindFromCurrentContextSetsTheCorrectParent_lookupRelativeToTheCurrentNode() throws Exception{
        final IvmContext root = IvmContext.createRootContext();
        root.bind("a/b/c", null);

        IvmContext a = (IvmContext) root.lookup("a");
        a.bind("object", null);
        requireCorrectParentChildRelationship(a, (IvmContext) a.lookup("object"));

        IvmContext b = (IvmContext) a.lookup("b");
        b.bind("object", null);
        requireCorrectParentChildRelationship(b, (IvmContext) b.lookup("object"));
    }

    private void requireCorrectParentChildRelationship(IvmContext parent, IvmContext child){
        final NameNode parentNode = null == parent ? null : parent.mynode;
        final NameNode childNode = child.mynode;

        assertSame(parentNode, childNode.getParent());
    }
}
