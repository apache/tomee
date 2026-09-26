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
import org.apache.openejb.assembler.classic.InjectionBuilder;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.ListenerInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Event;
import org.apache.openejb.spi.ContainerSystem;
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
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LightweightWebAppBuilder implements WebAppBuilder {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, LightweightWebAppBuilder.class);

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
                final Class<?> clazz;
                try {
                    clazz = webContext.getClassLoader().loadClass(listener.classname);
                } catch (final ClassNotFoundException | NoClassDefFoundError e) {
                    // TOMEE-4642: a listener class the war does not package is reported by the
                    // deployer and must not abort the rest of the application here either.
                    LOGGER.error("Unable to load listener class: " + listener.classname
                                 + " for web application " + webAppInfo.contextRoot, e);
                    continue;
                }
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
            final ServletContextEvent sce = servletContextEvents.remove(webAppInfo);
            final List<Object> listenerInstances = listeners.remove(webAppInfo);

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
        private final ServletContext delegate;
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
