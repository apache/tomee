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
package org.apache.openejb.server.hessian;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.authenticator.DigestAuthenticator;
import org.apache.catalina.authenticator.NonLoginAuthenticator;
import org.apache.catalina.authenticator.SSLAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.util.HttpUtil;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomee.catalina.IgnoredStandardContext;
import org.apache.tomee.catalina.OpenEJBValve;
import org.apache.tomee.catalina.TomcatWebAppBuilder;
import org.apache.tomee.loader.TomcatHelper;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TomcatHessianRegistry implements HessianRegistry {
    private static final String TOMEE_HESSIAN_SECURITY_ROLE_PREFIX = "tomee.hessian.security-role.";

    private final Map<String, Pair<Context, Integer>> fakeContexts = new HashMap<>();

    private Engine engine;
    private List<Connector> connectors;

    public TomcatHessianRegistry() {
        final StandardServer standardServer = TomcatHelper.getServer();
        for (final Service service : standardServer.findServices()) {
            if (Engine.class.isInstance(service.getContainer())) {
                connectors = Arrays.asList(service.findConnectors());
                engine = Engine.class.cast(service.getContainer());
                break;
            }
        }
    }

    @Override
    public String deploy(final ClassLoader loader, final HessianServer listener,
                         final String hostname, final String app,
                         final String authMethod, final String transportGuarantee,
                         final String realmName, final String name) throws URISyntaxException {
        Container host = engine.findChild(hostname);
        if (host == null) {
            host = engine.findChild(engine.getDefaultHost());
            if (host == null) {
                throw new IllegalArgumentException("Invalid virtual host '" + engine.getDefaultHost() + "'.  Do you have a matchiing Host entry in the server.xml?");
            }
        }

        final String contextRoot = contextName(app);
        Context context = Context.class.cast(host.findChild(contextRoot));
        if (context == null) {
            Pair<Context, Integer> fakeContext = fakeContexts.get(contextRoot);
            if (fakeContext != null) {
                context = fakeContext.getLeft();
                fakeContext.setValue(fakeContext.getValue() + 1);
            } else {
                context = Context.class.cast(host.findChild(contextRoot));
                if (context == null) {
                    fakeContext = fakeContexts.get(contextRoot);
                    if (fakeContext == null) {
                        context = createNewContext(loader, authMethod, transportGuarantee, realmName, app);
                        fakeContext = new MutablePair<>(context, 1);
                        fakeContexts.put(contextRoot, fakeContext);
                    } else {
                        context = fakeContext.getLeft();
                        fakeContext.setValue(fakeContext.getValue() + 1);
                    }
                }
            }
        }

        final String servletMapping = generateServletPath(name);

        Wrapper wrapper = Wrapper.class.cast(context.findChild(servletMapping));
        if (wrapper != null) {
            throw new IllegalArgumentException("Servlet " + servletMapping + " in web application context " + context.getName() + " already exists");
        }

        wrapper = context.createWrapper();
        wrapper.setName(HESSIAN.replace("/", "") + "_" + name);
        wrapper.setServlet(new OpenEJBHessianServlet(listener));
        context.addChild(wrapper);
        context.addServletMappingDecoded(servletMapping, wrapper.getName());

        if ("BASIC".equals(authMethod) && StandardContext.class.isInstance(context)) {
            final StandardContext standardContext = StandardContext.class.cast(context);

            boolean found = false;
            for (final Valve v : standardContext.getPipeline().getValves()) {
                if (LimitedBasicValve.class.isInstance(v) || BasicAuthenticator.class.isInstance(v)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                standardContext.addValve(new LimitedBasicValve());
            }
        }

        final List<String> addresses = new ArrayList<>();
        for (final Connector connector : connectors) {
            for (final String mapping : wrapper.findMappings()) {
                final URI address = new URI(connector.getScheme(), null, host.getName(), connector.getPort(), contextRoot + mapping, null, null);
                addresses.add(address.toString());
            }
        }
        return HttpUtil.selectSingleAddress(addresses);
    }

    private static Context createNewContext(final ClassLoader classLoader, final String rAuthMethod, final String rTransportGuarantee, final String realmName, final String name) {
        String path = name;
        if (path == null) {
            path = "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        final StandardContext context = new IgnoredStandardContext();
        context.setPath(path);
        context.setDocBase("");
        context.setParentClassLoader(classLoader);
        context.setDelegate(true);
        context.setName(name);
        TomcatWebAppBuilder.class.cast(SystemInstance.get().getComponent(WebAppBuilder.class)).initJ2EEInfo(context);

        // Configure security
        String authMethod = rAuthMethod;
        if (authMethod != null) {
            authMethod = authMethod.toUpperCase();
        }
        String transportGuarantee = rTransportGuarantee;
        if (transportGuarantee != null) {
            transportGuarantee = transportGuarantee.toUpperCase();
        }
        if (authMethod != null & !"NONE".equals(authMethod)) {
            if ("BASIC".equals(authMethod) || "DIGEST".equals(authMethod) || "CLIENT-CERT".equals(authMethod)) {

                //Setup a login configuration
                final LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(authMethod);
                loginConfig.setRealmName(realmName);
                context.setLoginConfig(loginConfig);

                //Setup a default Security Constraint
                final String securityRole = SystemInstance.get().getProperty(TOMEE_HESSIAN_SECURITY_ROLE_PREFIX + name, "default");
                for (final String role : securityRole.split(",")) {
                    final SecurityCollection collection = new SecurityCollection();
                    collection.addMethod("GET");
                    collection.addMethod("POST");
                    collection.addPattern("/*");
                    collection.setName(role);

                    final SecurityConstraint sc = new SecurityConstraint();
                    sc.addAuthRole("*");
                    sc.addCollection(collection);
                    sc.setAuthConstraint(true);
                    sc.setUserConstraint(transportGuarantee);

                    context.addConstraint(sc);
                    context.addSecurityRole(role);
                }
            }

            //Set the proper authenticator
            switch (authMethod) {
                case "BASIC":
                    context.addValve(new BasicAuthenticator());
                    break;
                case "DIGEST":
                    context.addValve(new DigestAuthenticator());
                    break;
                case "CLIENT-CERT":
                    context.addValve(new SSLAuthenticator());
                    break;
                case "NONE":
                    context.addValve(new NonLoginAuthenticator());
                    break;
            }

            context.getPipeline().addValve(new OpenEJBValve());
        } else {
            throw new IllegalArgumentException("Invalid authMethod: " + authMethod);
        }

        return context;
    }

    private static String generateServletPath(String name) {
        return HESSIAN + name;
    }

    private static String contextName(final String app) {
        if (!app.startsWith("/") && !app.isEmpty()) {
            return "/" + app;
        }
        return app;
    }

    @Override
    public void undeploy(final String hostname, final String app, final String name) {
        Container host = engine.findChild(hostname);
        if (host == null) {
            host = engine.findChild(engine.getDefaultHost());
            if (host == null) {
                throw new IllegalArgumentException("Invalid virtual host '" + engine.getDefaultHost() + "'.  Do you have a matchiing Host entry in the server.xml?");
            }
        }

        final String contextRoot = contextName(app);
        final Pair<Context, Integer> fakeContext = fakeContexts.get(contextRoot);

        if (fakeContext != null) {
            fakeContext.setValue(fakeContext.getValue() - 1);
            if (fakeContext.getValue() == 0) {
                fakeContexts.remove(contextRoot);
                host.removeChild(fakeContext.getKey());
            }
        }
    }

    protected static class LimitedBasicValve extends BasicAuthenticator {
        @Override
        public void invoke(final Request request, final Response response) throws IOException, ServletException {
            final String requestURI = request.getDecodedRequestURI();
            if (requestURI.startsWith(HESSIAN)) {
                if (!authenticate(request, response)) {
                    return;
                }
            }
            getNext().invoke(request, response);
        }
    }
}
