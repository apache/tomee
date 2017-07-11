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

package org.apache.openejb.arquillian.tests.naming;

import org.apache.openejb.core.interceptor.Interceptor;
import org.apache.openejb.core.ivm.naming.SystemComponentReference;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Stateless
@LocalBean
public class NamingBean {
    private interface ListOperation {
        NamingEnumeration<? extends NameClassPair> execute(Context context, String address) throws NamingException;
    }

    public void verifyContextList() throws NamingException {
        verifyContextListInternal(new ListOperation() {
            @Override
            public NamingEnumeration<NameClassPair> execute(Context context, String address) throws NamingException {
                return context.list(address);
            }
        });
    }

    public void verifyContextListBindings() throws NamingException {
        verifyContextListInternal(new ListOperation() {
            @Override
            public NamingEnumeration<Binding> execute(Context context, String address) throws NamingException {
                return context.listBindings(address);
            }
        });
    }

    private void verifyContextListInternal(ListOperation listOperation) throws NamingException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final PrintWriter logSink = new PrintWriter(buffer);

        final InitialContext ctx = new InitialContext();
        final boolean hasErrors = listContext(ctx, "", listOperation, logSink);
        logSink.flush();

        if (hasErrors) {
            throw new IllegalStateException("Failed to lookup some of the listed entries:\n" + getPrintedJndiTree(buffer));
        }
    }

    private String getPrintedJndiTree(ByteArrayOutputStream buffer) {
        try {
            return buffer.toString("utf-8");
        } catch (UnsupportedEncodingException e) {
            return null; //should never happen
        }
    }

    /*
    * Verifies TOMEE-2087: context.list()/listBindings()
    * returns more entries than it should. The false positives
    * cannot be looked up, so try to execute the context and look
    * up every name-class pair. If the lookup fails, then execute()
    * returns incorrect results.
    */
    private static boolean listContext(Context context, String ctxName, ListOperation listOperation, PrintWriter writer) throws javax.naming.NamingException {
        writer.println("\n### Context: " + ctxName);
        boolean hasErrors = false;

        final Map<Context, String> subContexts = new HashMap<>();
        final String namespace = context instanceof InitialContext ? "java:" : "";

        final NamingEnumeration<? extends NameClassPair> content = listOperation.execute(context, namespace);
        while (content.hasMoreElements()) {
            final NameClassPair nameClassPair = content.nextElement();
            final String name = nameClassPair.getName();
            final String className = nameClassPair.getClassName();

            writer.print("Name: " + name);
            writer.print("\tClass=" + className);
            writer.print("\t[looking up: " + (namespace + name) + "]");

            /*
             *  Skip the SystemComponentReference because they might internally
             *  throw NameNotFoundException althoug being correctly bound to the
             *  provided name.
             */
            if (!SystemComponentReference.class.getName().equals(className)) {
                try {
                    final Object object = context.lookup(namespace + name);
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
            }

            writer.println();
        }
        writer.println();

        for (Context subContext : subContexts.keySet()) {
            hasErrors |= listContext(subContext, subContexts.get(subContext), listOperation, writer);
        }

        return hasErrors;
    }

}
