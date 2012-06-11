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

import org.apache.openejb.util.OpenEJBScripter;
import org.apache.tomee.webapp.JsonExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SystemInfoServlet extends HttpServlet {
    private static final TomEEVersion VERSION;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        JsonExecutor.execute(req, resp, new JsonExecutor.Executor() {
            @Override
            public void call(Map<String, Object> json) throws Exception {

                final Map<String, Object> systemProperties = new HashMap<String, Object>();
                json.put("systemProperties", systemProperties);

                final Set<String> props = System.getProperties().stringPropertyNames();
                for (String propName : props) {
                    systemProperties.put(propName, System.getProperty(propName));
                }

                json.put("env", System.getenv());

                {
                    Map<String,Object> serverVersion = new HashMap<String, Object>();
                    json.put("tomee", serverVersion);

                    serverVersion.put("name", VERSION.getName());
                    serverVersion.put("hasMdbs", VERSION.hasMdbs());
                    serverVersion.put("hasWebservices", VERSION.hasWebservices());
                }

                {
                    final RuntimeMXBean runtimemxBean = ManagementFactory.getRuntimeMXBean();
                    final List<String> arguments = runtimemxBean.getInputArguments();

                    json.put("jvmArguments", arguments);
                }

                final Principal principal = req.getUserPrincipal();
                if (principal != null) {
                    json.put("user", principal.getName());
                }

                json.put("supportedScriptLanguages", OpenEJBScripter.getSupportedLanguages());
            }
        });
    }

    public enum TomEEVersion {
        WEBPROFILE(false, false, "Web Profile"), JAXRS(true, false, "JAX-RS"), PLUS(true, true, "+");

        private final boolean webservices;
        private final boolean mdbs;
        private final String name;

        private TomEEVersion(final boolean webservices, final boolean mdbs, final String name) {
            this.webservices = webservices;
            this.mdbs = mdbs;
            this.name = name;
        }

        public boolean hasWebservices() {
            return webservices;
        }

        public boolean hasMdbs() {
            return mdbs;
        }

        public String getName() {
            return name;
        }
    }

    static {
        final ClassLoader cl = SystemInfoServlet.class.getClassLoader();
        boolean mdbs;
        try {
            cl.loadClass("org.apache.activemq.ra.ActiveMQActivationSpec");
            mdbs = true;
        } catch (ClassNotFoundException e) {
            mdbs = false;
        }

        boolean webservices;
        try {
            cl.loadClass("org.apache.openejb.server.rest.RESTService");
            webservices = true;
        } catch (ClassNotFoundException e) {
            webservices = false;
        }

        if (webservices && mdbs) {
            VERSION = TomEEVersion.PLUS;
        } else if (webservices) {
            VERSION = TomEEVersion.JAXRS;
        } else {
            VERSION = TomEEVersion.WEBPROFILE;
        }
    }
}
