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
package org.apache.tomee.webservices;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
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
import org.apache.tomee.catalina.OpenEJBValve;
import org.apache.tomee.loader.TomcatHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.tomee.catalina.BackportUtil.getServlet;
import static org.apache.tomee.catalina.TomcatWebAppBuilder.IGNORE_CONTEXT;

public class TomcatWsRegistry implements WsRegistry {
    private static final String WEBSERVICE_SUB_CONTEXT = System.getProperty("tomee.jaxws.subcontext", "/webservices");
    private static final boolean WEBSERVICE_OLDCONTEXT_ACTIVE = Boolean.getBoolean("tomee.jaxws.oldsubcontext");

    private final Map<String, Context> webserviceContexts = new TreeMap<String, Context>();
    private Engine engine;
    private List<Connector> connectors;

    public TomcatWsRegistry() {
        StandardServer standardServer = TomcatHelper.getServer();
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
            throw new IllegalArgumentException("Could not find servlet " + servletName + " in web application context " + context.getName());
        }

        // for Pojo web services, we need to change the servlet class which is the service implementation
        // by the WsServler class
        wrapper.setServletClass(WsServlet.class.getName());
        if (getServlet(wrapper) != null) {
            wrapper.load();
            wrapper.unload();
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
            throw new IllegalArgumentException("Could not find servlet " + servletName + " in web application context " + context.getName());
        }

        // clear the webservice ref in the servlet context
        String webServicecontainerId = wrapper.findInitParameter(WsServlet.WEBSERVICE_CONTAINER);
        if (webServicecontainerId != null) {
            context.getServletContext().removeAttribute(webServicecontainerId);
            wrapper.removeInitParameter(WsServlet.WEBSERVICE_CONTAINER);
        }
    }

    public List<String> addWsContainer(String webContext, String path, HttpListener httpListener, String virtualHost, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
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

        List<String> addresses = new ArrayList<String>();

        // build contexts
        // - old way (/*)
        if (WEBSERVICE_OLDCONTEXT_ACTIVE) {
            Context context = createNewContext(path, classLoader, authMethod, transportGuarantee, realmName);
            host.addChild(context);
            addServlet(host, context, "/*", httpListener, path, addresses);
        }

        // - new way (/<webappcontext>/webservices/<name>) if webcontext is specified
        if (webContext != null) {
            String root = webContext;
            if (!root.startsWith("/")) {
                root = '/' + root;
            }
            Context webAppContext = (Context) host.findChild(root);
            // sub context = '/' means the service address is provided by webservices
            if (WEBSERVICE_SUB_CONTEXT.equals("/") && path.startsWith("/")) {
                addServlet(host, webAppContext, path, httpListener, path, addresses);
            } else if (WEBSERVICE_SUB_CONTEXT.equals("/") && !path.startsWith("/")) {
                addServlet(host, webAppContext, '/' + path, httpListener, path, addresses);
            } else if (WEBSERVICE_SUB_CONTEXT.startsWith("/")) {
                addServlet(host, webAppContext, WEBSERVICE_SUB_CONTEXT + path, httpListener, path, addresses);
            } else {
                addServlet(host, webAppContext, '/' + WEBSERVICE_SUB_CONTEXT + path, httpListener, path, addresses);
            }
        }
        return addresses;
    }

    private static Context createNewContext(String path, ClassLoader classLoader, String authMethod, String transportGuarantee, String realmName) {
        StandardContext context = new StandardContext();
        context.setPath(path);
        context.setDocBase("");
        context.setParentClassLoader(classLoader);
        context.setDelegate(true);

        // Tomcat has a stupid rule where a life cycle listener must set
        // configured true, or it will treat it as a failed deployment
        context.addLifecycleListener(new LifecycleListener() {
            public void lifecycleEvent(LifecycleEvent event) {
                Context context = (Context) event.getLifecycle();

                if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
                    context.getServletContext().setAttribute(IGNORE_CONTEXT, "true");
                }


                if (event.getType().equals(Lifecycle.START_EVENT) || event.getType().equals(Lifecycle.BEFORE_START_EVENT) || event.getType().equals("configure_start")) {
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

            OpenEJBValve openejbValve = new OpenEJBValve();
            context.getPipeline().addValve(openejbValve);

        } else {
            throw new IllegalArgumentException("Invalid authMethod: " + authMethod);
        }

        return context;
    }

    private void addServlet(Container host, Context context, String mapping, HttpListener httpListener, String path, List<String> addresses) {
        // build the servlet
        Wrapper wrapper = context.createWrapper();
        wrapper.setName("webservice" + path.substring(1));
        wrapper.setServletClass(WsServlet.class.getName());

        // add servlet to context
        context.addChild(wrapper);
        context.addServletMapping(mapping, wrapper.getName());

        String webServicecontainerID = wrapper.getName() + WsServlet.WEBSERVICE_CONTAINER + httpListener.hashCode();
        wrapper.addInitParameter(WsServlet.WEBSERVICE_CONTAINER, webServicecontainerID);

        context.getServletContext().setAttribute(IGNORE_CONTEXT, "true");
        setWsContainer(context, wrapper, httpListener);

        webserviceContexts.put(path, context);

        // register wsdl locations for service-ref resolution
        for (Connector connector : connectors) {
            final StringBuilder fullContextpath;
            if (!WEBSERVICE_OLDCONTEXT_ACTIVE) {
                String contextPath = context.getName();
                if (contextPath != null && !contextPath.startsWith("/")) {
                    contextPath = "/" + contextPath;
                } else if (contextPath == null) {
                    contextPath = "/";
                }

                fullContextpath = new StringBuilder(contextPath);
                if (!WEBSERVICE_SUB_CONTEXT.equals("/")) {
                    fullContextpath.append(WEBSERVICE_SUB_CONTEXT);
                }
                fullContextpath.append(path);
            } else {
                fullContextpath = new StringBuilder(path);
            }

            try {
                URI address = new URI(connector.getScheme(), null, host.getName(), connector.getPort(), fullContextpath.toString(), null, null);
                addresses.add(address.toString());
            } catch (URISyntaxException ignored) {
                // no-op
            }
        }
    }

    public void removeWsContainer(String path) {
        if (path == null) return;

        // assure context root with a leading slash
        if (!path.startsWith("/")) path = "/" + path;

        if (TomcatHelper.isTomcat7() && TomcatHelper.isStopping()) {
            return;
        }

        Context context = webserviceContexts.remove(path);
        if (WEBSERVICE_OLDCONTEXT_ACTIVE) {
            try {
                context.destroy();
                context.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Host host = (Host) context.getParent();
            host.removeChild(context);
        } // else let tomcat manages its context
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
