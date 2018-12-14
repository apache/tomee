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
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.authenticator.DigestAuthenticator;
import org.apache.catalina.authenticator.NonLoginAuthenticator;
import org.apache.catalina.authenticator.SSLAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardWrapper;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.webservices.WsRegistry;
import org.apache.openejb.server.webservices.WsServlet;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomee.catalina.IgnoredStandardContext;
import org.apache.tomee.catalina.OpenEJBValve;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.tomee.catalina.TomcatWebAppBuilder;
import org.apache.tomee.loader.TomcatHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

public class TomcatWsRegistry implements WsRegistry {
    private static final String WEBSERVICE_SUB_CONTEXT = forceSlash(SystemInstance.get().getOptions().get("tomee.jaxws.subcontext", "/webservices"));

    private static final boolean WEBSERVICE_OLDCONTEXT_ACTIVE = SystemInstance.get().getOptions().get("tomee.jaxws.oldsubcontext", false);
    private static final String TOMEE_JAXWS_SECURITY_ROLE_PREFIX = "tomee.jaxws.security-role.";

    private final Map<Key, Context> webserviceContexts = new ConcurrentHashMap<>();
    private final Map<String, Integer> fakeContextReferences = new ConcurrentHashMap<>();

    private Engine engine;
    private List<Connector> connectors;

    public TomcatWsRegistry() {
        final StandardServer standardServer = TomcatHelper.getServer();
        for (final Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                connectors = Arrays.asList(service.findConnectors());
                engine = (Engine) service.getContainer();
                break;
            }
        }
    }

    private static String forceSlash(final String property) {
        if (property == null) {
            return "/";
        }
        if (!property.startsWith("/")) {
            return "/" + property;
        }
        return property;
    }


    @Override
    public List<String> setWsContainer(final HttpListener httpListener,
                                       final ClassLoader classLoader,
                                       String contextRoot, String virtualHost, final ServletInfo servletInfo,
                                       final String realmName, final String transportGuarantee, final String authMethod,
                                       final String moduleId) throws Exception {

        if (virtualHost == null) {
            virtualHost = engine.getDefaultHost();
        }

        final Container host = engine.findChild(virtualHost);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matchiing Host entry in the server.xml?");
        }

        if ("ROOT".equals(contextRoot)) { // doesn't happen in tomee itself but with all our tooling around
            contextRoot = "";
        }
        if (!contextRoot.startsWith("/") && !contextRoot.isEmpty()) {
            contextRoot = "/" + contextRoot;
        }

        final Context context = (Context) host.findChild(contextRoot);
        if (context == null) {
            throw new IllegalArgumentException("Could not find web application context " + contextRoot + " in host " + host.getName());
        }

        final Wrapper wrapper = (Wrapper) context.findChild(servletInfo.servletName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Could not find servlet " + servletInfo.servletName + " in web application context " + context.getName());
        }

        // for Pojo web services, we need to change the servlet class which is the service implementation
        // by the WsServler class
        wrapper.setServletClass(WsServlet.class.getName());
        if (wrapper.getServlet() != null) {
            wrapper.unload(); // deallocate previous one
            wrapper.load(); // reload this one withuot unloading it to keep the instance - unload is called during stop()
            // boolean controlling this method call can't be set to false through API so let do it ourself
            wrapper.getServlet().init(StandardWrapper.class.cast(wrapper)); // or Reflections.set(wrapper, "instanceInitialized", false);
        }

        setWsContainer(context, wrapper, httpListener);

        // add service locations
        final List<String> addresses = new ArrayList<>();
        for (final Connector connector : connectors) {
            for (final String mapping : wrapper.findMappings()) {
                final URI address = new URI(connector.getScheme(), null, host.getName(), connector.getPort(), (contextRoot.startsWith("/") ? "" : "/") + contextRoot + mapping, null, null);
                addresses.add(address.toString());
            }
        }
        return addresses;
    }


    @Override
    public void clearWsContainer(final String contextRoot, String virtualHost, final ServletInfo servletInfo, final String moduleId) {
        if (virtualHost == null) {
            virtualHost = engine.getDefaultHost();
        }

        final Container host = engine.findChild(virtualHost);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matchiing Host entry in the server.xml?");
        }

        final Context context = (Context) host.findChild("/" + contextRoot);
        if (context == null) {
            throw new IllegalArgumentException("Could not find web application context " + contextRoot + " in host " + host.getName());
        }

        final Wrapper wrapper = (Wrapper) context.findChild(servletInfo.servletName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Could not find servlet " + servletInfo.servletName + " in web application context " + context.getName());
        }

        // clear the webservice ref in the servlet context
        final String webServicecontainerId = wrapper.findInitParameter(WsServlet.WEBSERVICE_CONTAINER);
        if (webServicecontainerId != null) {
            context.getServletContext().removeAttribute(webServicecontainerId);
            wrapper.removeInitParameter(WsServlet.WEBSERVICE_CONTAINER);
        }
    }


    // String webContext, String path, HttpListener httpListener, String virtualHost, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader

    @Override
    public List<String> addWsContainer(final HttpListener httpListener,
                                       final ClassLoader classLoader,
                                       final String context, String virtualHost, String path,
                                       final String realmName, final String transportGuarantee, final String authMethod,
                                       final String moduleId) throws Exception {
        if (path == null) {
            throw new NullPointerException("contextRoot is null");
        }
        if (httpListener == null) {
            throw new NullPointerException("httpListener is null");
        }

        // assure context root with a leading slash
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // find the existing host (we do not auto-create hosts)
        if (virtualHost == null) {
            virtualHost = engine.getDefaultHost();
        }
        final Container host = engine.findChild(virtualHost);
        if (host == null) {
            throw new IllegalArgumentException("Invalid virtual host '" + virtualHost + "'.  Do you have a matchiing Host entry in the server.xml?");
        }

        final List<String> addresses = new ArrayList<>();

        // build contexts
        // - old way (/*)
        if (WEBSERVICE_OLDCONTEXT_ACTIVE) {
            deployInFakeWebapp(path, classLoader, authMethod, transportGuarantee, realmName, host, httpListener, addresses, context);
        }

        // - new way (/<webappcontext>/webservices/<name>) if webcontext is specified
        if (context != null) {
            final Context webAppContext = findContext(context, moduleId, host);

            if (webAppContext != null) {
                // sub context = '/' means the service address is provided by webservices
                if (WEBSERVICE_SUB_CONTEXT.equals("/") && path.startsWith("/")) {
                    addServlet(host, webAppContext, path, httpListener, path, addresses, false, moduleId);
                } else if (WEBSERVICE_SUB_CONTEXT.equals("/") && !path.startsWith("/")) {
                    addServlet(host, webAppContext, '/' + path, httpListener, path, addresses, false, moduleId);
                } else {
                    addServlet(host, webAppContext, WEBSERVICE_SUB_CONTEXT + path, httpListener, path, addresses, false, moduleId);
                }
            } else if (!WEBSERVICE_OLDCONTEXT_ACTIVE) { // deploying in a jar
                deployInFakeWebapp(path, classLoader, authMethod, transportGuarantee, realmName, host, httpListener, addresses, context);
            }
        }
        return addresses;
    }

    private Context findContext(final String context, final String moduleId, final Container host) {
        String root = context;
        if ("ROOT".equals(root)) {
            root = "";
        }
        if (!root.startsWith("/") && !root.isEmpty()) {
            root = '/' + root;
        }

        Context webAppContext = null;
        for (final String name : asList(moduleId == null ? null : "/" + moduleId, root)) {
            if (name == null) {
                continue;
            }
            webAppContext = Context.class.cast(host.findChild(name));
            if (webAppContext != null) {
                break;
            }
        }
        return webAppContext;
    }

    private void deployInFakeWebapp(final String path, final ClassLoader classLoader, final String authMethod, final String transportGuarantee,
                                    final String realmName, final Container host, final HttpListener httpListener, final List<String> addresses, final String name) {
        Container context = host.findChild(name);
        if (context == null) {
            context = createNewContext(classLoader, authMethod, transportGuarantee, realmName, name);
            host.addChild(context);
        }

        final Integer ref = fakeContextReferences.get(name);
        if (ref == null) {
            fakeContextReferences.put(name, 0);
        } else {
            fakeContextReferences.put(name, ref + 1);
        }

        String mapping = path;
        if (!mapping.startsWith("/")) { // TODO: check it can happen or move it away
            mapping = '/' + mapping;
        }
        addServlet(host, (Context) context, mapping, httpListener, path, addresses, true, null);
    }

    private static Context createNewContext(final ClassLoader classLoader, String authMethod, String transportGuarantee, final String realmName, final String name) {
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
        ((TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class)).initJ2EEInfo(context);

        // Configure security
        if (authMethod != null) {
            authMethod = authMethod.toUpperCase();
        }
        if (transportGuarantee != null) {
            transportGuarantee = transportGuarantee.toUpperCase();
        }
        if (authMethod == null || "NONE".equals(authMethod)) { //NOPMD
            // ignore none for now as the  NonLoginAuthenticator seems to be completely hosed
        } else if ("BASIC".equals(authMethod) || "DIGEST".equals(authMethod) || "CLIENT-CERT".equals(authMethod)) {

            //Setup a login configuration
            final LoginConfig loginConfig = new LoginConfig();
            loginConfig.setAuthMethod(authMethod);
            loginConfig.setRealmName(realmName);
            context.setLoginConfig(loginConfig);

            //Setup a default Security Constraint
            final String securityRole = SystemInstance.get().getProperty(TOMEE_JAXWS_SECURITY_ROLE_PREFIX + name, "default");
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

            context.getPipeline().addValve(new OpenEJBValve());

        } else {
            throw new IllegalArgumentException("Invalid authMethod: " + authMethod);
        }

        return context;
    }

    private void addServlet(final Container host, final Context context, final String mapping, final HttpListener httpListener, final String path,
                            final List<String> addresses, final boolean fakeDeployment, final String moduleId) {
        // build the servlet
        final Wrapper wrapper = context.createWrapper();
        wrapper.setName("webservice" + path.substring(1));
        wrapper.setServletClass(WsServlet.class.getName());

        // add servlet to context
        context.addChild(wrapper);
        context.addServletMappingDecoded(mapping, wrapper.getName());

        final String webServicecontainerID = wrapper.getName() + WsServlet.WEBSERVICE_CONTAINER + httpListener.hashCode();
        wrapper.addInitParameter(WsServlet.WEBSERVICE_CONTAINER, webServicecontainerID);

        setWsContainer(context, wrapper, httpListener);

        webserviceContexts.put(new Key(path, moduleId), context);

        // register wsdl locations for service-ref resolution
        for (final Connector connector : connectors) {
            final StringBuilder fullContextpath;
            if (!WEBSERVICE_OLDCONTEXT_ACTIVE && !fakeDeployment) {
                String contextPath = context.getPath();
                if (contextPath == null ||  !contextPath.isEmpty()) {
                    if (contextPath != null && !contextPath.startsWith("/")) {
                        contextPath = "/" + contextPath;
                    } else if (contextPath == null) {
                        contextPath = "/";
                    }
                }

                fullContextpath = new StringBuilder(contextPath);
                if (!WEBSERVICE_SUB_CONTEXT.equals("/")) {
                    fullContextpath.append(WEBSERVICE_SUB_CONTEXT);
                }
                fullContextpath.append(path);
            } else {
                fullContextpath = new StringBuilder(context.getPath()).append(path);
            }

            try {
                final URI address = new URI(connector.getScheme(), null, host.getName(), connector.getPort(), fullContextpath.toString(), null, null);
                addresses.add(address.toString());
            } catch (final URISyntaxException ignored) {
                // no-op
            }
        }
    }

    @Override
    public void removeWsContainer(String path, final String moduleId) {
        if (path == null) {
            return;
        }

        // assure context root with a leading slash
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (TomcatHelper.isStopping()) {
            return;
        }

        Context context = webserviceContexts.remove(new Key(path, moduleId));
        if (context == null) { // fake
            context = webserviceContexts.remove(new Key(path, null));
        }

        Integer refs = 1; // > 0 to avoid to destroy the context if not mandatory
        if (context != null) {
            final String name = context.getName();
            refs = fakeContextReferences.remove(name);
            if (refs != null && refs > 0) {
                fakeContextReferences.put(name, refs - 1);
            }
        }

        if ((WEBSERVICE_OLDCONTEXT_ACTIVE || (refs != null && refs == 0)) && context != null) {
            try {
                context.stop();
                context.destroy();
            } catch (final Exception e) {
                throw new TomEERuntimeException(e);
            }
            final Host host = (Host) context.getParent();
            host.removeChild(context);
        } // else let tomcat manages its context
    }

    private void setWsContainer(final Context context, final Wrapper wrapper, final HttpListener wsContainer) {
        // Make up an ID for the WebServiceContainer
        // put a reference the ID in the init-params
        // put the WebServiceContainer in the webapp context keyed by its ID
        final String webServicecontainerID = wrapper.getName() + WsServlet.WEBSERVICE_CONTAINER + wsContainer.hashCode();
        context.getServletContext().setAttribute(webServicecontainerID, wsContainer);
        wrapper.addInitParameter(WsServlet.WEBSERVICE_CONTAINER, webServicecontainerID);
    }

    private static class Key {
        private final String moduleId;
        private final String path;
        private final int hash;

        public Key(final String path, final String moduleId) {
            this.moduleId = moduleId;
            this.path = path;
            this.hash = 31 * (moduleId != null ? moduleId.hashCode() : 0) + path.hashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Key.class != o.getClass()) {
                return false;
            }

            final Key key = Key.class.cast(o);
            return !(moduleId != null ? !moduleId.equals(key.moduleId) : key.moduleId != null) && path.equals(key.path);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "moduleId='" + moduleId + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }
    }
}
