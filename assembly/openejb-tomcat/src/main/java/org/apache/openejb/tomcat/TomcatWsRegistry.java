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
package org.apache.openejb.tomcat;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.authenticator.DigestAuthenticator;
import org.apache.catalina.authenticator.NonLoginAuthenticator;
import org.apache.catalina.authenticator.SSLAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.webservices.WsRegistry;
import org.apache.openejb.server.webservices.WsServlet;
import static org.apache.openejb.tomcat.TomcatWebAppBuilder.IGNORE_CONTEXT;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TomcatWsRegistry implements WsRegistry {
    private final Map<String, StandardContext> webserviceContexts = new TreeMap<String, StandardContext>();
    private Engine engine;
    private List<Connector> connectors;

    public TomcatWsRegistry() {
        StandardServer standardServer = (StandardServer) ServerFactory.getServer();
        for (Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                connectors = Arrays.asList(service.findConnectors());
                engine = (Engine) service.getContainer();
                break;
            }
        }
    }

    public List<String> setWsContainer(String virtualHost, String contextRoot, String servletName, HttpListener wsContainer) throws Exception {
        if (virtualHost == null) virtualHost = engine.getDefaultHost();

        Container host = engine.findChild(virtualHost);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matchiing Host entry in the server.xml?");
        }

        Context context = (Context) host.findChild("/" + contextRoot);
        if (context == null) {
            throw new IllegalArgumentException("Could not find web application context " + contextRoot + " in host " + host.getName());
        }

        Wrapper wrapper = (Wrapper) context.findChild(servletName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Could not find servlet " + contextRoot + " in web application context " + context.getName());
        }

        setWsContainer(context, wrapper, wsContainer);

        // add service locations
        List<String> addresses = new ArrayList<String>();
        for (Connector connector : connectors) {
            for (String mapping : wrapper.findMappings()) {
                URI address = new URI(connector.getScheme(), null, host.getName(), connector.getPort(), "/" + contextRoot + mapping, null, null);
                addresses.add(address.toString());
            }
        }
        return addresses;
    }

    public void clearWsContainer(String virtualHost, String contextRoot, String servletName) {
        if (virtualHost == null) virtualHost = engine.getDefaultHost();

        Container host = engine.findChild(virtualHost);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matchiing Host entry in the server.xml?");
        }

        Context context = (Context) host.findChild("/" + contextRoot);
        if (context == null) {
            throw new IllegalArgumentException("Could not find web application context " + contextRoot + " in host " + host.getName());
        }

        Wrapper wrapper = (Wrapper) context.findChild(servletName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Could not find servlet " + contextRoot + " in web application context " + context.getName());
        }

        // clear the webservice ref in the servlet context
        String webServicecontainerId = wrapper.findInitParameter(WsServlet.WEBSERVICE_CONTAINER);
        if (webServicecontainerId != null) {
            context.getServletContext().removeAttribute(webServicecontainerId);
            wrapper.removeInitParameter(WsServlet.WEBSERVICE_CONTAINER);
        }
    }

    public List<String> addWsContainer(String path, HttpListener httpListener, String virtualHost, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        if (path == null) throw new NullPointerException("contextRoot is null");
        if (httpListener == null) throw new NullPointerException("httpListener is null");

        // assure context root with a leading slash
        if (!path.startsWith("/")) path = "/" + path;

        // find the existing host (we do not auto-create hosts)
        if (virtualHost == null) virtualHost = engine.getDefaultHost();
        Container host = engine.findChild(virtualHost);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matchiing Host entry in the server.xml?");
        }

        // build the context
        StandardContext context = new StandardContext();
        context.setPath(path);
        context.setDocBase("");
        context.setParentClassLoader(classLoader);
        context.setDelegate(true);

        // Tomcat has a stupid rule where a life cycle listener must set
        // configured true, or it will treat it as a failed deployment
        context.addLifecycleListener(new LifecycleListener() {
            public void lifecycleEvent(LifecycleEvent event) {
                if (event.getType().equals(Lifecycle.START_EVENT)) {
                    Context context = (Context) event.getLifecycle();
                    context.setConfigured(true);
                }
            }
        });

        // Configure security
        if (authMethod != null) {
            authMethod = authMethod.toUpperCase();
        }
        if (transportGuarantee != null) {
            transportGuarantee = transportGuarantee.toUpperCase();
        }
        if (authMethod == null || "NONE".equals(authMethod)) {
            // ignore none for now as the  NonLoginAuthenticator seems to be completely hosed
        } else if ("BASIC".equals(authMethod) || "DIGEST".equals(authMethod) || "CLIENT-CERT".equals(authMethod)) {

            //Setup a login configuration
            LoginConfig loginConfig = new LoginConfig();
            loginConfig.setAuthMethod(authMethod);
            loginConfig.setRealmName(realmName);
            context.setLoginConfig(loginConfig);

            //Setup a default Security Constraint
            SecurityCollection collection = new SecurityCollection();
            collection.addMethod("GET");
            collection.addMethod("POST");
            collection.addPattern("/*");
            collection.setName("default");
            SecurityConstraint sc = new SecurityConstraint();
            sc.addAuthRole("*");
            sc.addCollection(collection);
            sc.setAuthConstraint(true);
            sc.setUserConstraint(transportGuarantee);
            context.addConstraint(sc);
            context.addSecurityRole("default");

            //Set the proper authenticator
            if ("BASIC".equals(authMethod)) {
                context.addValve(new BasicAuthenticator());
            } else if ("DIGEST".equals(authMethod)) {
                context.addValve(new DigestAuthenticator());
            } else if ("CLIENT-CERT".equals(authMethod)) {
                context.addValve(new SSLAuthenticator());
            } else if ("NONE".equals(authMethod)) {
                context.addValve(new NonLoginAuthenticator());
            }
        } else {
            throw new IllegalArgumentException("Invalid authMethod: " + authMethod);
        }

        // Mark this as a dynamic context that should not be inspected by the TomcatWebAppBuilder
        context.getServletContext().setAttribute(IGNORE_CONTEXT, "true");

        // build the servlet
        Wrapper wrapper = context.createWrapper();
        wrapper.setName("webservice");
        wrapper.setServletClass(WsServlet.class.getName());
        setWsContainer(context, wrapper, httpListener);
        wrapper.addMapping("/*");


        // add add servlet to context
        context.addChild(wrapper);
        context.addServletMapping("/*", "webservice");

        // add context to host
        host.addChild(context);
        webserviceContexts.put(path, context);

        // register wsdl locations for service-ref resolution
        List<String> addresses = new ArrayList<String>();
        for (Connector connector : connectors) {
            URI address = new URI(connector.getScheme(), null, host.getName(), connector.getPort(), path, null, null);
            addresses.add(address.toString());
        }
        return addresses;
    }

    public void removeWsContainer(String path) {
        if (path == null) return;

        // assure context root with a leading slash
        if (!path.startsWith("/")) path = "/" + path;

        StandardContext context = webserviceContexts.remove(path);
        try {
            context.stop();
            context.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Host host = (Host) context.getParent();
        host.removeChild(context);
    }

    private void setWsContainer(Context context, Wrapper wrapper, HttpListener wsContainer) {
        // Make up an ID for the WebServiceContainer
        // put a reference the ID in the init-params
        // put the WebServiceContainer in the webapp context keyed by its ID
        String webServicecontainerID = wrapper.getName() + WsServlet.WEBSERVICE_CONTAINER + wsContainer.hashCode();
        context.getServletContext().setAttribute(webServicecontainerID, wsContainer);
        wrapper.addInitParameter(WsServlet.WEBSERVICE_CONTAINER, webServicecontainerID);
    }
}
