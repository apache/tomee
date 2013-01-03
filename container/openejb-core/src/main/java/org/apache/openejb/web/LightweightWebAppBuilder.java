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
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClassListInfo;
import org.apache.openejb.assembler.classic.FilterInfo;
import org.apache.openejb.assembler.classic.InjectionBuilder;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.ListenerInfo;
import org.apache.openejb.assembler.classic.ParamValueInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.web.lifecycle.test.MockServletContextEvent;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.ws.rs.core.Application;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class LightweightWebAppBuilder implements WebAppBuilder {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, LightweightWebAppBuilder.class);

    private static Method addServletMethod = null;
    private static Method removeServletMethod = null;
    private static Method addFilterMethod = null;
    private static Method removeFilterMethod = null;

    static {
        try {
            final Class<?> utilClass = LightweightWebAppBuilder.class.getClassLoader().loadClass("org.apache.openejb.server.httpd.util.HttpUtil");
            addServletMethod = utilClass.getMethod("addServlet", String.class, WebContext.class, String.class);
            removeServletMethod = utilClass.getMethod("removeServlet", String.class, WebContext.class);
            addFilterMethod = utilClass.getMethod("addFilter", String.class, WebContext.class, String.class, FilterConfig.class);
            removeFilterMethod = utilClass.getMethod("removeFilter", String.class, WebContext.class);
        } catch (Exception e) {
            LOGGER.info("Web features will not be available, add openejb-http if you need them");
        }
    }

    private final Map<WebAppInfo, DeployedWebObjects> servletDeploymentInfo = new HashMap<WebAppInfo, DeployedWebObjects>();
    private final Map<WebAppInfo, List<Object>> listeners = new HashMap<WebAppInfo, List<Object>>();
    private final Map<WebAppInfo, ServletContextEvent> servletContextEvents = new HashMap<WebAppInfo, ServletContextEvent>();

    @Override
    public void deployWebApps(final AppInfo appInfo, final ClassLoader classLoader) throws Exception {

        final CoreContainerSystem cs = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        final AppContext appContext = cs.getAppContext(appInfo.appId);
        if (appContext == null) {
            throw new OpenEJBRuntimeException("Can't find app context for " + appInfo.appId);
        }

        for (WebAppInfo webAppInfo : appInfo.webApps) {
            final Set<Injection> injections = new HashSet<Injection>(appContext.getInjections());
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

            if (!appInfo.webAppAlone) {
                final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                final List<BeanContext> beanContexts = assembler.initEjbs(classLoader, appInfo, appContext, injections, new ArrayList<BeanContext>(), webAppInfo.moduleId);
                appContext.getBeanContexts().addAll(beanContexts);
                new CdiBuilder().build(appInfo, appContext, appContext.getBeanContexts(), webContext);
                assembler.startEjbs(true, beanContexts);
            }

            final ServletContextEvent sce = new MockServletContextEvent();
            servletContextEvents.put(webAppInfo, sce);

            // listeners
            for (ListenerInfo listener : webAppInfo.listeners) {
                final Class<?> clazz = webContext.getClassLoader().loadClass(listener.classname);
                final Object instance = webContext.newInstance(clazz);
                if (ServletContextListener.class.isInstance(instance)) {
                    ((ServletContextListener) instance).contextInitialized(sce);
                }

                List<Object> list = listeners.get(webAppInfo);
                if (list == null) {
                    list = new ArrayList<Object>();
                    listeners.put(webAppInfo, list);
                }
                list.add(instance);
            }

            final DeployedWebObjects deployedWebObjects = new DeployedWebObjects();
            deployedWebObjects.webContext = webContext;
            servletDeploymentInfo.put(webAppInfo, deployedWebObjects);

            if (addServletMethod == null) { // can't manage filter/servlets
                continue;
            }

            // register filters
            for (FilterInfo info : webAppInfo.filters) {
                for (String mapping : info.mappings) {
                    final FilterConfig config = new SimpleFilterConfig(sce.getServletContext(), info.name, info.initParams);
                    try {
                        addFilterMethod.invoke(null, info.classname, webContext, mapping, config);
                        deployedWebObjects.filterMappings.add(mapping);
                    } catch (Exception e) {
                        LOGGER.warning(e.getMessage(), e);
                    }
                }
            }
            for (ClassListInfo info : webAppInfo.webAnnotatedClasses) {
                final String url = info.name;
                for (String filterPath : info.list) {
                    String classname = nameFromUrls(url, filterPath);

                    final Class<?> clazz = webContext.getClassLoader().loadClass(classname);
                    final WebFilter annotation = clazz.getAnnotation(WebFilter.class);
                    if (annotation != null) {
                        final Properties initParams = new Properties();
                        for (WebInitParam param : annotation.initParams()) {
                            initParams.put(param.name(), param.value());
                        }

                        final FilterConfig config = new SimpleFilterConfig(sce.getServletContext(), info.name, initParams);
                        for (String mapping: annotation.urlPatterns()) {
                            try {
                                addFilterMethod.invoke(null, classname, webContext, mapping, config);
                                deployedWebObjects.filterMappings.add(mapping);
                            } catch (Exception e) {
                                LOGGER.warning(e.getMessage(), e);
                            }
                        }
                    }
                }
            }

            // register servlets
            for (ServletInfo info : webAppInfo.servlets) {
                if ("true".equalsIgnoreCase(appInfo.properties.getProperty("openejb.jaxrs.on", "true"))) {
                    // skip jaxrs servlets
                    boolean skip = false;
                    for (ParamValueInfo pvi : info.initParams) {
                        if ("javax.ws.rs.Application".equals(pvi.name) || Application.class.getName().equals(pvi.name)) {
                            skip = true;
                        }
                    }

                    if (skip) {
                        continue;
                    }

                    if (info.servletClass == null) {
                        try {
                            if (Application.class.isAssignableFrom(classLoader.loadClass(info.servletName))) {
                                continue;
                            }
                        } catch (Exception e) {
                            // no-op
                        }
                    }
                } // else let the user manage itself a rest servlet etc...

                // deploy
                for (String mapping : info.mappings) {
                    try {
                        addServletMethod.invoke(null, info.servletClass, webContext, mapping);
                        deployedWebObjects.mappings.add(mapping);
                    } catch (Exception e) {
                        LOGGER.warning(e.getMessage(), e);
                    }
                }
            }
            for (ClassListInfo info : webAppInfo.webAnnotatedClasses) {
                final String url = info.name;
                for (String servletPath : info.list) {
                    String classname = nameFromUrls(url, servletPath);

                    final Class<?> clazz = webContext.getClassLoader().loadClass(classname);
                    final WebServlet annotation = clazz.getAnnotation(WebServlet.class);
                    if (annotation != null) {
                        for (String mapping: annotation.urlPatterns()) {
                            try {
                                addServletMethod.invoke(null, classname, webContext, mapping);
                                deployedWebObjects.mappings.add(mapping);
                            } catch (Exception e) {
                                LOGGER.warning(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    private static String nameFromUrls(final String url, final String path) {
        final String value = path.substring(url.length()).replace(File.separatorChar, '/').replace('/', '.');
        return value.substring(0, value.length() - ".class".length());
    }

    @Override
    public void undeployWebApps(final AppInfo appInfo) throws Exception {
        for (WebAppInfo webAppInfo : appInfo.webApps) {
            final DeployedWebObjects context = servletDeploymentInfo.remove(webAppInfo);
            final ServletContextEvent sce = servletContextEvents.remove(webAppInfo);
            final List<Object> listenerInstances = listeners.remove(webAppInfo);

            if (addServletMethod != null) {
                for (String mapping : context.mappings) {
                    try {
                        removeServletMethod.invoke(null, mapping, context.webContext);
                    } catch (Exception e) {
                        // no-op
                    }
                }

                for (String mapping : context.filterMappings) {
                    try {
                        removeFilterMethod.invoke(null, mapping, context.webContext);
                    } catch (Exception e) {
                        // no-op
                    }
                }
            }

            if (listenerInstances != null) {
                for (Object instance : listenerInstances) {
                    if (ServletContextListener.class.isInstance(instance)) {
                        ((ServletContextListener) instance).contextDestroyed(sce);
                    }
                }
            }
        }
    }

    @Override
    public Map<ClassLoader, Map<String, Set<String>>> getJsfClasses() {
        return Collections.emptyMap(); // while we don't manage servlet in embedded mode we don't need it
    }

    private static class DeployedWebObjects {
        public List<String> mappings = new ArrayList<String>();
        public List<String> filterMappings = new ArrayList<String>();
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

    private static class SimpleFilterConfig implements FilterConfig {
        private final Properties params;
        private final String name;
        private final ServletContext servletContext;

        public SimpleFilterConfig(final ServletContext sc, final String name, final Properties initParams) {
            this.name = name;
            params = initParams;
            servletContext = sc;
        }

        @Override
        public String getFilterName() {
            return name;
        }

        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }

        @Override
        public String getInitParameter(final String name) {
            return params.getProperty(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return new ArrayEnumeration(params.keySet());
        }
    }
}
