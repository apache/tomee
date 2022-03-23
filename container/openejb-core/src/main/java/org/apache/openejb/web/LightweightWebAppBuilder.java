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

package org.apache.openejb.web;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClassListInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.FilterInfo;
import org.apache.openejb.assembler.classic.InjectionBuilder;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.ListenerInfo;
import org.apache.openejb.assembler.classic.ParamValueInfo;
import org.apache.openejb.assembler.classic.PortInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Event;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.web.lifecycle.test.MockServletContext;
import org.apache.webbeans.web.lifecycle.test.MockServletContextEvent;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.ws.rs.core.Application;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

public class LightweightWebAppBuilder implements WebAppBuilder {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, LightweightWebAppBuilder.class);

    private static Method addServletMethod;
    private static Method removeServletMethod;
    private static Method addFilterMethod;
    private static Method removeFilterMethod;
    private static Method addDefaults;

    static {
        try {
            final Class<?> utilClass = Class.forName("org.apache.openejb.server.httpd.util.HttpUtil", true/*setFactory()*/, LightweightWebAppBuilder.class.getClassLoader());
            addServletMethod = utilClass.getMethod("addServlet", String.class, WebContext.class, String.class);
            removeServletMethod = utilClass.getMethod("removeServlet", String.class, WebContext.class);
            addFilterMethod = utilClass.getMethod("addFilter", String.class, WebContext.class, String.class, FilterConfig.class);
            removeFilterMethod = utilClass.getMethod("removeFilter", String.class, WebContext.class);
            addDefaults = utilClass.getMethod("addDefaultsIfAvailable", WebContext.class);
        } catch (final Exception e) {
            LOGGER.info("Web features will not be available, add openejb-http if you need them");
        }
    }

    private final Map<WebAppInfo, DeployedWebObjects> servletDeploymentInfo = new HashMap<>();
    private final Map<WebAppInfo, List<Object>> listeners = new HashMap<>();
    private final Map<WebAppInfo, ServletContextEvent> servletContextEvents = new HashMap<>();
    private final Map<String, ClassLoader> loaderByWebContext = new HashMap<>();

    public void setClassLoader(final String id, final ClassLoader loader) {
        loaderByWebContext.put(id, loader);
    }

    public void removeClassLoader(final String id) {
        loaderByWebContext.remove(id);
    }

    @Override
    public void deployWebApps(final AppInfo appInfo, final ClassLoader appClassLoader) throws Exception {

        final CoreContainerSystem cs = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        final AppContext appContext = cs.getAppContext(appInfo.appId);
        if (appContext == null) {
            throw new OpenEJBRuntimeException("Can't find app context for " + appInfo.appId);
        }

        for (final WebAppInfo webAppInfo : appInfo.webApps) {
            ClassLoader classLoader = loaderByWebContext.get(webAppInfo.moduleId);
            if (classLoader == null) {
                classLoader = appClassLoader;
            }

            final Set<Injection> injections = new HashSet<>(appContext.getInjections());
            injections.addAll(new InjectionBuilder(classLoader).buildInjections(webAppInfo.jndiEnc));

            final List<BeanContext> beanContexts;
            if (!appInfo.webAppAlone) { // add module bindings in app
                final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                beanContexts = assembler.initEjbs(classLoader, appInfo, appContext, injections, new ArrayList<>(), webAppInfo.moduleId);
                appContext.getBeanContexts().addAll(beanContexts);
            } else {
                beanContexts = null;
            }

            final Map<String, Object> bindings = new HashMap<>();
            bindings.putAll(appContext.getBindings());
            bindings.putAll(new JndiEncBuilder(webAppInfo.jndiEnc, injections, webAppInfo.moduleId, "Bean", null, webAppInfo.uniqueId, classLoader, appInfo.properties).buildBindings(JndiEncBuilder.JndiScope.comp));

            final WebContext webContext = new WebContext(appContext);
            webContext.setBindings(bindings);
            webContext.getBindings().putAll(new JndiEncBuilder(webAppInfo.jndiEnc, injections, webAppInfo.moduleId, "Bean", null, webAppInfo.uniqueId, classLoader, appInfo.properties).buildBindings(JndiEncBuilder.JndiScope.comp));
            webContext.setJndiEnc(WebInitialContext.create(bindings, appContext.getGlobalJndiContext()));
            webContext.setClassLoader(classLoader);
            webContext.setId(webAppInfo.moduleId);
            webContext.setContextRoot(webAppInfo.contextRoot);
            webContext.setHost(webAppInfo.host);
            webContext.getInjections().addAll(injections);
            webContext.setInitialContext(new EmbeddedInitialContext(webContext.getJndiEnc(), webContext.getBindings()));

            final ServletContext component = SystemInstance.get().getComponent(ServletContext.class);
            final ServletContextEvent sce = component == null ? new MockServletContextEvent() :
                    new ServletContextEvent(new LightServletContext(component, webContext.getClassLoader()));
            servletContextEvents.put(webAppInfo, sce);
            webContext.setServletContext(sce.getServletContext());
            SystemInstance.get().fireEvent(new EmbeddedServletContextCreated(sce.getServletContext()));

            appContext.getWebContexts().add(webContext);
            cs.addWebContext(webContext);

            if (!appInfo.webAppAlone && hasCdi(appInfo)) {
                final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                new CdiBuilder().build(appInfo, appContext, beanContexts, webContext);
                assembler.startEjbs(true, beanContexts);
            }

            // listeners
            for (final ListenerInfo listener : webAppInfo.listeners) {
                final Class<?> clazz = webContext.getClassLoader().loadClass(listener.classname);
                final Object instance = webContext.newInstance(clazz);
                if (ServletContextListener.class.isInstance(instance)) {
                    switchServletContextIfNeeded(sce.getServletContext(), new Runnable() {
                        @Override
                        public void run() {
                            ((ServletContextListener) instance).contextInitialized(sce);
                        }
                    });
                }

                List<Object> list = listeners.computeIfAbsent(webAppInfo, k -> new ArrayList<>());
                list.add(instance);
            }
            for (final ClassListInfo info : webAppInfo.webAnnotatedClasses) {
                final String url = info.name;
                for (final String filterPath : info.list) {
                    final Class<?> clazz = loadFromUrls(webContext.getClassLoader(), url, filterPath);
                    final WebListener annotation = clazz.getAnnotation(WebListener.class);
                    if (annotation != null) {
                        final Object instance = webContext.newInstance(clazz);
                        if (ServletContextListener.class.isInstance(instance)) {
                            switchServletContextIfNeeded(sce.getServletContext(), new Runnable() {
                                @Override
                                public void run() {
                                    ((ServletContextListener) instance).contextInitialized(sce);
                                }
                            });
                        }

                        List<Object> list = listeners.computeIfAbsent(webAppInfo, k -> new ArrayList<>());
                        list.add(instance);
                    }
                }
            }

            final DeployedWebObjects deployedWebObjects = new DeployedWebObjects();
            deployedWebObjects.webContext = webContext;
            servletDeploymentInfo.put(webAppInfo, deployedWebObjects);

            if (webContext.getWebBeansContext() != null && webContext.getWebBeansContext().getBeanManagerImpl().isInUse()) {
                final Thread thread = Thread.currentThread();
                final ClassLoader old = thread.getContextClassLoader();
                thread.setContextClassLoader(webContext.getClassLoader());
                try {
                    OpenEJBLifecycle.class.cast(webContext.getWebBeansContext().getService(ContainerLifecycle.class)).startServletContext(sce.getServletContext());
                } finally {
                    thread.setContextClassLoader(old);
                }
            }

            if (addServletMethod == null) { // can't manage filter/servlets
                continue;
            }

            // register filters
            for (final FilterInfo info : webAppInfo.filters) {
                switchServletContextIfNeeded(sce.getServletContext(), new Runnable() {
                    @Override
                    public void run() {
                        for (final String mapping : info.mappings) {
                            final FilterConfig config = new SimpleFilterConfig(sce.getServletContext(), info.name, info.initParams);
                            try {
                                addFilterMethod.invoke(null, info.classname, webContext, mapping, config);
                                deployedWebObjects.filterMappings.add(mapping);
                            } catch (final Exception e) {
                                LOGGER.warning(e.getMessage(), e);
                            }
                        }
                    }
                });
            }
            for (final ClassListInfo info : webAppInfo.webAnnotatedClasses) {
                final String url = info.name;
                for (final String filterPath : info.list) {
                    final Class<?> clazz = loadFromUrls(webContext.getClassLoader(), url, filterPath);
                    final WebFilter annotation = clazz.getAnnotation(WebFilter.class);
                    if (annotation != null) {
                        final Properties initParams = new Properties();
                        for (final WebInitParam param : annotation.initParams()) {
                            initParams.put(param.name(), param.value());
                        }

                        final FilterConfig config = new SimpleFilterConfig(sce.getServletContext(), info.name, initParams);
                        for (final String[] mappings : asList(annotation.urlPatterns(), annotation.value())) {
                            switchServletContextIfNeeded(sce.getServletContext(), new Runnable() {
                                @Override
                                public void run() {
                                    for (final String mapping : mappings) {
                                        try {
                                            addFilterMethod.invoke(null, clazz.getName(), webContext, mapping, config);
                                            deployedWebObjects.filterMappings.add(mapping);
                                        } catch (final Exception e) {
                                            LOGGER.warning(e.getMessage(), e);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }

            final Map<String, PortInfo> ports = new TreeMap<>();
            for (final PortInfo port : webAppInfo.portInfos) {
                ports.put(port.serviceLink, port);
            }

            // register servlets
            for (final ServletInfo info : webAppInfo.servlets) {
                if ("true".equalsIgnoreCase(appInfo.properties.getProperty("openejb.jaxrs.on", "true"))) {
                    // skip jaxrs servlets
                    boolean skip = false;
                    for (final ParamValueInfo pvi : info.initParams) {
                        if ("jakarta.ws.rs.Application".equals(pvi.name) || Application.class.getName().equals(pvi.name)) {
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
                        } catch (final Exception e) {
                            // no-op
                        }
                    }
                }

                // If POJO web services, it will be overriden with WsServlet
                if (ports.containsKey(info.servletName) || ports.containsKey(info.servletClass)) {
                    continue;
                }

                // deploy
                for (final String mapping : info.mappings) {
                    switchServletContextIfNeeded(sce.getServletContext(), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                addServletMethod.invoke(null, info.servletClass, webContext, mapping);
                                deployedWebObjects.mappings.add(mapping);
                            } catch (final Exception e) {
                                LOGGER.warning(e.getMessage(), e);
                            }
                        }
                    });
                }
            }

            for (final ClassListInfo info : webAppInfo.webAnnotatedClasses) {
                final String url = info.name;
                for (final String servletPath : info.list) {
                    final Class<?> clazz = loadFromUrls(webContext.getClassLoader(), url, servletPath);
                    final WebServlet annotation = clazz.getAnnotation(WebServlet.class);
                    if (annotation != null) {
                        for (final String[] mappings : asList(annotation.urlPatterns(), annotation.value())) {
                            switchServletContextIfNeeded(sce.getServletContext(), new Runnable() {
                                @Override
                                public void run() {
                                    for (final String mapping : mappings) {
                                        try {
                                            addServletMethod.invoke(null, clazz.getName(), webContext, mapping);
                                            deployedWebObjects.mappings.add(mapping);
                                        } catch (final Exception e) {
                                            LOGGER.warning(e.getMessage(), e);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }

            if (addDefaults != null && tryJsp()) {
                addDefaults.invoke(null, webContext);
                deployedWebObjects.mappings.add("*\\.jsp");
            }
        }
    }

    private boolean hasCdi(final AppInfo appInfo) {
        for (final EjbJarInfo jar : appInfo.ejbJars) {
            if (jar.beans != null) {
                return true;
            }
        }
        return false;
    }

    // not thread safe but fine in embedded mode which is the only mode of this builder
    private void switchServletContextIfNeeded(final ServletContext sc, final Runnable runnable) {
        if (sc == null) {
            runnable.run();
            return;
        }
        final SystemInstance systemInstance = SystemInstance.get();
        final ServletContext old = systemInstance.getComponent(ServletContext.class);
        systemInstance.setComponent(ServletContext.class, sc);
        try {
            runnable.run();
        } finally {
            if (old == null) {
                systemInstance.removeComponent(ServletContext.class);
            } else {
                systemInstance.setComponent(ServletContext.class, old);
            }
        }
    }

    private static boolean tryJsp() {
        return "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.embedded.try-jsp", "true"));
    }

    public Collection<Object> listenersFor(final String context) {
        for (final Map.Entry<WebAppInfo, List<Object>> info : listeners.entrySet()) {
            if (context != null && context.replace("/", "").equals(info.getKey().contextRoot.replace("/", ""))) {
                return info.getValue();
            }
        }
        return null;
    }

    private static Class<?> loadFromUrls(final ClassLoader loader, final String url, final String path) throws ClassNotFoundException {
        final String classname;
        if (path.startsWith("archive:") && path.contains(".war/")) {
            classname = path.substring(path.indexOf(".war") + ".war".length() + 1);
        } else if ("jar:file://!/WEB-INF/classes/".equals(url) && path.contains("classes/")) {
            classname = path.substring(path.lastIndexOf("classes/") + "classes/".length());
        } else {
            classname = path.substring(url.length());
        }

        try { // in WEB-INF/classes
            return loader.loadClass(className(classname));
        } catch (final ClassNotFoundException cnfe) { // in a dependency (jar)
            return loader.loadClass(className(path.substring(path.indexOf('!') + 2)));
        }
    }

    private static String className(final String value) {
        return value.substring(0, value.length() - ".class".length()).replace(File.separatorChar, '/').replace('/', '.');
    }

    @Override
    public void undeployWebApps(final AppInfo appInfo) throws Exception {
        for (final WebAppInfo webAppInfo : appInfo.webApps) {
            final DeployedWebObjects context = servletDeploymentInfo.remove(webAppInfo);
            final ServletContextEvent sce = servletContextEvents.remove(webAppInfo);
            final List<Object> listenerInstances = listeners.remove(webAppInfo);

            if (addServletMethod != null) {
                switchServletContextIfNeeded(sce.getServletContext(), new Runnable() {
                    @Override
                    public void run() {
                        for (final String mapping : context.mappings) {
                            try {
                                removeServletMethod.invoke(null, mapping, context.webContext);
                            } catch (final Exception e) {
                                // no-op
                            }
                        }

                        for (final String mapping : context.filterMappings) {
                            try {
                                removeFilterMethod.invoke(null, mapping, context.webContext);
                            } catch (final Exception e) {
                                // no-op
                            }
                        }
                    }
                });
            }

            if (listenerInstances != null) {
                for (final Object instance : listenerInstances) {
                    if (ServletContextListener.class.isInstance(instance)) {
                        switchServletContextIfNeeded(sce.getServletContext(), new Runnable() {
                            @Override
                            public void run() {
                                ((ServletContextListener) instance).contextDestroyed(sce);
                            }
                        });
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
        public List<String> mappings = new ArrayList<>();
        public List<String> filterMappings = new ArrayList<>();
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
            } catch (final NameNotFoundException nnfe) {
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

        @SuppressWarnings("unchecked")
        @Override
        public Enumeration<String> getInitParameterNames() {
            return new ArrayEnumeration(params.keySet());
        }
    }

    @Event
    public static class EmbeddedServletContextCreated {
        private final ServletContext context;

        public EmbeddedServletContextCreated(ServletContext context) {
            this.context = context;
        }

        public ServletContext getContext() {
            return context;
        }

        @Override
        public String toString() {
            return "EmbeddedServletContextCreated{" +
                    "context=" + context +
                    '}';
        }
    }

    public static class LightServletContext extends MockServletContext {
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();
        private final ServletContext delegate; // EmbeddedServletContext has some resource handling we want to reuse here, TODO: move it here?
        private final ClassLoader loader;

        public LightServletContext(final ServletContext delegate, final ClassLoader loader) {
            this.delegate = delegate;
            this.loader = loader;
        }

        @Override
        public ClassLoader getClassLoader() {
            return loader;
        }

        @Override
        public URL getResource(final String path) throws MalformedURLException {
            return delegate.getResource(path);
        }

        @Override
        public InputStream getResourceAsStream(final String path) {
            return delegate.getResourceAsStream(path);
        }

        @Override
        public int getMajorVersion() {
            return 3;
        }

        @Override
        public int getEffectiveMajorVersion() {
            return 3;
        }

        @Override
        public String getVirtualServerName() {
            return "openejb-embedded";
        }

        @Override
        public void setAttribute(final String name, final Object object) {
            attributes.put(name, object);
        }

        @Override
        public Object getAttribute(final String name) {
            final Object o = attributes.get(name);
            return o == null ? delegate.getAttribute(name) : o;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            final Set<String> c = new HashSet<>(attributes.keySet());
            c.addAll(Collections.list(delegate.getAttributeNames()));
            return Collections.enumeration(c);
        }

        @Override
        public String getServerInfo() {
            return "EmbeddedOpenEJB/" + OpenEjbVersion.get().getVersion();
        }
    }
}
