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

package org.apache.openejb.util;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public final class JndiTreeBrowser {
    private static final String ROOT = "";

    private final Context context;
    private final String path;

    private JndiTreeBrowser(final Context ctx, final String name) {
        path = name;
        context = ctx;
    }

    private JndiTreeBrowser(final Context ctx) {
        this(ctx, ROOT);
    }

    private JndiTreeBrowser(final String name) throws NamingException {
        this(new InitialContext(), name);
    }

    private JndiTreeBrowser() throws NamingException {
        this(new InitialContext(), ROOT);
    }

    private void runOnTree(final JndiNodeWorker worker) throws NamingException {
        final NamingEnumeration<Binding> ne = context.listBindings(ROOT);
        while (ne.hasMoreElements()) {
            final Binding current = ne.next();
            final Object obj = current.getObject();
            worker.doWork(path, current.getName(), obj);
            if (obj instanceof Context) {
                runOnJndiTree((Context) obj, worker,
                    path + '/' + current.getName());
            }
        }
    }

    private void runOnJndiTree(final Context ctx, final JndiNodeWorker worker, final String prefix)
        throws NamingException {
        new JndiTreeBrowser(ctx, prefix).runOnTree(worker);
    }

    public static void runOnJndiTree(final Context ctx, final JndiNodeWorker worker) throws NamingException {
        new JndiTreeBrowser(ctx).runOnTree(worker);
    }

    public static void log(final Context ctx) throws NamingException {
        new JndiTreeBrowser(ctx).runOnTree(new LogJndiWorker(null));
    }

    public static void log(final Context ctx, final String foo) throws NamingException {
        new JndiTreeBrowser(ctx).runOnTree(new LogJndiWorker(foo));
    }

    private interface JndiNodeWorker {
        void doWork(String path, String name, Object obj);
    }

    private static class LogJndiWorker implements JndiNodeWorker {
        private final String filter;

        public LogJndiWorker(final String filter) {
            this.filter = filter;
        }

        @Override
        public void doWork(final String path, final String name, final Object obj) {
            final String complete = path + "/" + name;
            if (filter == null || complete.contains(filter)) {
                System.out.println(complete);
            }
        }
    }
}

