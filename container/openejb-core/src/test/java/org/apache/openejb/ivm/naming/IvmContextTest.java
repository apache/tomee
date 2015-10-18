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

import javax.naming.Context;
import javax.naming.NamingException;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class IvmContextTest {
    @Test
    public void rebind() throws NamingException {
        final IvmContext root = IvmContext.createRootContext();
        root.rebind("global/App.EAR/foo", "test");
        final Context last = Contexts.createSubcontexts(root, "global/App.EAR/foo");
        last.bind("foo", "test");

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

        Contexts.createSubcontexts(context, "global/foo/Bar").bind("Bar", "Foo");

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
}
