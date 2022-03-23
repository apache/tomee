/**
 *
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
package org.apache.openejb.test.servlet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class JndiServlet extends HttpServlet {
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        final ServletOutputStream out = response.getOutputStream();

        final Map<String, Object> bindings = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        try {
            final Context context = (Context) new InitialContext().lookup("java:comp/");
            addBindings("", bindings, context);
        } catch (final NamingException e) {
            throw new ServletException(e);
        }

        out.println("JNDI Context:");
        for (final Map.Entry<String, Object> entry : bindings.entrySet()) {
            if (entry.getValue() != null) {
                out.println("  " + entry.getKey() + "=" + entry.getValue());
            } else {
                out.println("  " + entry.getKey());
            }
        }
    }

    private void addBindings(final String path, final Map<String, Object> bindings, final Context context) {
        try {
            for (final NameClassPair pair : Collections.list(context.list(""))) {
                final String name = pair.getName();
                final String className = pair.getClassName();
                if ("org.apache.naming.resources.FileDirContext$FileResource".equals(className)) {
                    bindings.put(path + name, "<file>");
                } else {
                    try {
                        final Object value = context.lookup(name);
                        if (value instanceof Context) {
                            final Context nextedContext = (Context) value;
                            bindings.put(path + name, "");
                            addBindings(path + name + "/", bindings, nextedContext);
                        } else {
                            bindings.put(path + name, value);
                        }
                    } catch (final NamingException e) {
                        // lookup failed
                        bindings.put(path + name, "ERROR: " + e.getMessage());
                    }
                }
            }
        } catch (final NamingException e) {
            bindings.put(path, "ERROR: list bindings threw an exception: " + e.getMessage());
        }
    }
}
