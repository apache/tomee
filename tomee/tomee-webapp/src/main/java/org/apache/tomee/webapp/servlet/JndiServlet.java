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

package org.apache.tomee.webapp.servlet;

import org.apache.openejb.util.proxy.LocalBeanProxyGeneratorImpl;
import org.apache.tomee.webapp.JsonExecutor;

import javax.naming.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class JndiServlet extends HttpServlet {

    private Map<String, Object> buildNode(NameClassPair pair, Context ctx) throws NamingException {
        final String name = pair.getName();
        final Object obj = ctx.lookup(name);

        final String beanType;
        if (obj instanceof Context) {
            beanType = "CONTEXT";

        } else if (obj instanceof java.rmi.Remote
                || obj instanceof org.apache.openejb.core.ivm.IntraVmProxy
                || (obj != null && LocalBeanProxyGeneratorImpl.isLocalBean(obj.getClass()))) {
            beanType = "BEAN";
        } else {
            beanType = "OTHER";
        }

        final Map<String, Object> node = new HashMap<String, Object>();
        node.put("name", name);
        node.put("type", beanType);
        if ("CONTEXT".equals(beanType)) {
            node.put("children", Collections.emptyList());
        }
        return node;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        JsonExecutor.execute(resp, new JsonExecutor.Executor() {

            @Override
            public void call(Map<String, Object> json) throws Exception {
                final List<Map<String, Object>> objs = new ArrayList<Map<String, Object>>();
                json.put("objs", objs);

                final Properties p = new Properties();
                p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
                p.put("openejb.loader", "embed");

                final Context ctx = new InitialContext(p);
                final NamingEnumeration<NameClassPair> namingEnumeration = ctx.list("");

                while (namingEnumeration.hasMoreElements()) {
                    objs.add(buildNode(namingEnumeration.next(), ctx));
                }
            }
        });

    }
}
