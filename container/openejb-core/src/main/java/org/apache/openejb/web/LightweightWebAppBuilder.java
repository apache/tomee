/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.web;

import org.apache.openejb.AppContext;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClassListInfo;
import org.apache.openejb.assembler.classic.InjectionBuilder;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LightweightWebAppBuilder implements WebAppBuilder {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, LightweightWebAppBuilder.class);

    private static final Method addServletMethod;
    private static final Method removeServletMethod;

    static {
        try {
            final Class<?> utilClass = LightweightWebAppBuilder.class.getClassLoader().loadClass("org.apache.openejb.server.httpd.util.HttpUtil");
            addServletMethod = utilClass.getMethod("addServlet", String.class, WebContext.class, String.class);
            removeServletMethod = utilClass.getMethod("removeServlet", String.class, WebContext.class);
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private final Map<WebAppInfo, DeployedServlet> deploymentInfo = new HashMap<WebAppInfo, DeployedServlet>();

    @Override
    public void deployWebApps(final AppInfo appInfo, final ClassLoader classLoader) throws Exception {
        final CoreContainerSystem cs = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        final AppContext appContext = cs.getAppContext(appInfo.appId);
        if (appContext == null) {
            throw new OpenEJBRuntimeException("Can't find app context for " + appInfo.appId);
        }

        for (WebAppInfo webAppInfo : appInfo.webApps) {
            final Collection<Injection> injections = appContext.getInjections();
            injections.addAll(new InjectionBuilder(classLoader).buildInjections(webAppInfo.jndiEnc));

            final Map<String, Object> bindings = new HashMap<String, Object>();
            bindings.putAll(appContext.getBindings());
            bindings.putAll(new JndiEncBuilder(webAppInfo.jndiEnc, injections, webAppInfo.moduleId, "Bean", null, webAppInfo.uniqueId, classLoader).buildBindings(JndiEncBuilder.JndiScope.comp));

            final WebContext webContext = new WebContext(appContext);
            webContext.setBindings(bindings);
            webContext.getBindings().putAll(new JndiEncBuilder(webAppInfo.jndiEnc, injections, webAppInfo.moduleId, "Bean", null, webAppInfo.uniqueId, classLoader).buildBindings(JndiEncBuilder.JndiScope.comp));
            webContext.setJndiEnc(WebInitialContext.create(bindings, appContext.getGlobalJndiContext()));
            webContext.setClassLoader(classLoader);
            webContext.setId(webAppInfo.moduleId);
            webContext.setContextRoot(webAppInfo.contextRoot);
            webContext.getInjections().addAll(injections);
            webContext.setInitialContext(new EmbeddedInitialContext(webContext.getJndiEnc(), webContext.getBindings()));

            appContext.getWebContexts().add(webContext);
            cs.addWebContext(webContext);

            final DeployedServlet deployedServlet = new DeployedServlet();
            deployedServlet.webContext = webContext;

            // register servlets
            for (ServletInfo info : webAppInfo.servlets) {
                for (String mapping : info.mappings) {
                    try {
                        addServletMethod.invoke(null, info.servletClass, webContext, mapping);
                        deployedServlet.mappings.add(mapping);
                    } catch (Exception e) {
                        LOGGER.warning(e.getMessage(), e);
                    }
                }
            }
            for (ClassListInfo info : webAppInfo.webAnnotatedClasses) {
                final String url = info.name;
                for (String servletPath : info.list) {
                    String classname = servletPath.substring(url.length()).replace(File.separatorChar, '/').replace('/', '.');
                    classname = classname.substring(0, classname.length() - ".class".length());

                    final Class<?> clazz = webContext.getClassLoader().loadClass(classname);
                    final WebServlet annotation = clazz.getAnnotation(WebServlet.class);
                    if (annotation != null) {
                        for (String mapping: annotation.urlPatterns()) {
                            try {
                                addServletMethod.invoke(null, classname, webContext, mapping);
                                deployedServlet.mappings.add(mapping);
                            } catch (Exception e) {
                                LOGGER.warning(e.getMessage(), e);
                            }
                        }
                    }
                }
            }

            deploymentInfo.put(webAppInfo, deployedServlet);
        }
    }

    @Override
    public void undeployWebApps(final AppInfo appInfo) throws Exception {
        for (WebAppInfo webAppInfo : appInfo.webApps) {
            final DeployedServlet context = deploymentInfo.remove(webAppInfo);

            for (String mapping : context.mappings) {
                try {
                    removeServletMethod.invoke(null, mapping, context.webContext);
                } catch (Exception e) {
                    // no-op
                }
            }
        }
    }

    @Override
    public Map<ClassLoader, Map<String, Set<String>>> getJsfClasses() {
        return Collections.emptyMap(); // while we don't manage servlet in embedded mode we don't need it
    }

    private static class DeployedServlet {
        public List<String> mappings = new ArrayList<String>();
        public WebContext webContext;
    }

    private static class EmbeddedInitialContext implements Context {
        private final Context delegate;
        private final Map<String, Object> bindings;

        public EmbeddedInitialContext(final Context jndiEnc, final Map<String, Object> bindings) {
            this.delegate = jndiEnc;
            this.bindings = bindings;
        }

        @Override
        public Object lookup(final Name name) throws NamingException {
            return lookup(name.toString());
        }

        @Override
        public Object lookup(final String name) throws NamingException {
            try {
                return delegate.lookup(name);
            } catch (NameNotFoundException nnfe) {
                return bindings.get(name);
            }
        }

        @Override
        public void bind(final Name name, final Object obj) throws NamingException {
            // no-op
        }

        @Override
        public void bind(final String name, final Object obj) throws NamingException {
            // no-op
        }

        @Override
        public void rebind(final Name name, final Object obj) throws NamingException {
            // no-op
        }

        @Override
        public void rebind(final String name, final Object obj) throws NamingException {
            // no-op
        }

        @Override
        public void unbind(final Name name) throws NamingException {
            // no-op
        }

        @Override
        public void unbind(final String name) throws NamingException {
            // no-op
        }

        @Override
        public void rename(final Name oldName, final Name newName) throws NamingException {
            // no-op
        }

        @Override
        public void rename(final String oldName, final String newName) throws NamingException {
            // no-op
        }

        @Override
        public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
            return null;
        }

        @Override
        public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
            return null;
        }

        @Override
        public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
            return null;
        }

        @Override
        public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
            return null;
        }

        @Override
        public void destroySubcontext(final Name name) throws NamingException {
            // no-op
        }

        @Override
        public void destroySubcontext(final String name) throws NamingException {
            // no-op
        }

        @Override
        public Context createSubcontext(final Name name) throws NamingException {
            return null;
        }

        @Override
        public Context createSubcontext(final String name) throws NamingException {
            return null;
        }

        @Override
        public Object lookupLink(final Name name) throws NamingException {
            return null;
        }

        @Override
        public Object lookupLink(final String name) throws NamingException {
            return null;
        }

        @Override
        public NameParser getNameParser(final Name name) throws NamingException {
            return null;
        }

        @Override
        public NameParser getNameParser(final String name) throws NamingException {
            return null;
        }

        @Override
        public Name composeName(final Name name, final Name prefix) throws NamingException {
            return null;
        }

        @Override
        public String composeName(final String name, final String prefix) throws NamingException {
            return null;
        }

        @Override
        public Object addToEnvironment(final String propName, final Object propVal) throws NamingException {
            return null;
        }

        @Override
        public Object removeFromEnvironment(final String propName) throws NamingException {
            return null;
        }

        @Override
        public Hashtable<?, ?> getEnvironment() throws NamingException {
            return null;
        }

        @Override
        public void close() throws NamingException {
            // no-op
        }

        @Override
        public String getNameInNamespace() throws NamingException {
            return null;
        }
    }
}
