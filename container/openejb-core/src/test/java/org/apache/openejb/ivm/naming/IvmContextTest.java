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
import org.apache.openejb.util.Contexts;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IvmContextTest {
    @Test
    public void unbind() throws NamingException {
        final IvmContext context = new IvmContext();
        context.bind("global/foo/Bar", "Bar");

        assertEquals("Bar", context.lookup("global/foo/Bar"));

        context.unbind("global/foo/Bar");

        try {
            context.lookup("global/foo/Bar");
            fail();
        } catch (NamingException ne) {
            // ok
        }

        try {
            final Context subCtx = (Context) context.lookup("global/foo");
            subCtx.lookup("Bar");
            fail();
        } catch (NamingException ne) {
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
        } catch (NamingException ne) {
            // ok
        }

        try {
            ((Context) ((Context) context.lookup("global")).lookup("foo"))
                .lookup("Bar");
            fail();
        } catch (NamingException ne) {
            // ok
        }
    }
}
