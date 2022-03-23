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
package org.apache.tomee.catalina;

import org.apache.catalina.core.DefaultInstanceManager;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.descriptor.web.Injectable;
import org.apache.tomcat.util.descriptor.web.InjectionTarget;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansCreationException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.naming.NamingException;
import jakarta.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class JavaeeInstanceManager implements InstanceManager {
    private final WebContext webContext;
    private final StandardContext webapp;
    private final String[] skipContainerTags;
    private final String[] skipPrefixes;
    private volatile InstanceManager defaultInstanceManager;

    public JavaeeInstanceManager(final StandardContext webapp, final WebContext webContext) {
        this.webContext = webContext;
        this.webapp = webapp;
        this.skipContainerTags = SystemInstance.get().getProperty(
                "tomee.tomcat.instance-manager.skip-container-tags", "org.apache.taglibs.standard.,jakarta.servlet.jsp.jstl.").split(" *, *");
        final String[] skipCdi = SystemInstance.get().getProperty("tomee.tomcat.instance-manager.skip-cdi", "").split(" *, *");
        this.skipPrefixes = skipCdi.length == 1 && skipCdi[0].isEmpty() ? new String[0] : skipCdi;
    }

    public ServletContext getServletContext() {
        return webContext == null ? null : webContext.getServletContext();
    }

    @Override
    public Object newInstance(final Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException {
        try {
            final String name = clazz.getName();
            if ("org.apache.tomcat.websocket.server.WsHttpUpgradeHandler".equals(name)
                    || "org.apache.tomee.myfaces.TomEEMyFacesContextListener".equals(name)
                    || "org.apache.openejb.server.httpd.EEFilter".equals(name)
                    || "org.apache.catalina.servlets.DefaultServlet".equals(name)
                    || "org.apache.jasper.servlet.JspServlet".equals(name)) {
                return clazz.newInstance();
            }

            final Object object = isSkip(name, skipPrefixes) ? clazz.newInstance() : webContext.newInstance(clazz);
            if (isJsp(clazz)) {
                initDefaultInstanceMgr();
                defaultInstanceManager.newInstance(object);
            }
            postConstruct(object, clazz);
            return object;
        } catch (final OpenEJBException | WebBeansCreationException | WebBeansConfigurationException e) {
            throw (InstantiationException) new InstantiationException(e.getMessage()).initCause(e);
        }
    }

    private void initDefaultInstanceMgr() {
        if (defaultInstanceManager == null) { // lazy cause can not be needed
            synchronized (this) {
                if (defaultInstanceManager == null) {
                    defaultInstanceManager = new DefaultInstanceManager(
                            webapp.getNamingContextListener().getEnvContext(),
                            TomcatInjections.buildInjectionMap(webapp.getNamingResources()), webapp,
                            ParentClassLoaderFinder.Helper.get());
                }
            }
        }
    }

    private boolean isJsp(final Class<?> type) {
        return type.getSuperclass().getName().equals("org.apache.jasper.runtime.HttpJspBase");
    }

    public WebContext.Instance newWeakableInstance(final Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException {
        try {
            final WebContext.Instance object = webContext.newWeakableInstance(clazz);
            postConstruct(object.getValue(), clazz);
            return object;
        } catch (final OpenEJBException | WebBeansCreationException | WebBeansConfigurationException e) {
            throw (InstantiationException) new InstantiationException(e.getMessage()).initCause(e);
        }
    }

    @Override
    public Object newInstance(final String className) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return newInstance(className, webContext.getClassLoader());
    }

    @Override
    public Object newInstance(final String className, final ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return newInstance(classLoader.loadClass(className));
    }

    @Override
    public void newInstance(final Object o) throws IllegalAccessException, InvocationTargetException, NamingException {
        final String name = o.getClass().getName();
        if ("org.apache.tomee.webservices.CXFJAXRSFilter".equals(name)
                || "org.apache.tomcat.websocket.server.WsFilter".equals(name)
                || isSkip(name, skipContainerTags)) {
            return;
        }
        try {
            if (!isSkip(name, skipPrefixes)) {
                webContext.inject(o);
            }
            postConstruct(o, o.getClass());
        } catch (final OpenEJBException e) {
            destroyInstance(o);
            throw new InjectionFailedException(e);
        }
    }

    private boolean isSkip(final String name, final String[] prefixes) {
        for (final String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroyInstance(final Object o) throws IllegalAccessException, InvocationTargetException {
        if (o == null) {
            return;
        }
        final String name = o.getClass().getName();
        if ("org.apache.tomcat.websocket.server.WsHttpUpgradeHandler".equals(name)
                || "org.apache.tomee.myfaces.TomEEMyFacesContextListener".equals(name)
                || "org.apache.openejb.server.httpd.EEFilter".equals(name)
                || "org.apache.catalina.servlets.DefaultServlet".equals(name)
                || "org.apache.jasper.servlet.JspServlet".equals(name)) {
            return;
        }

        final Object unwrapped = unwrap(o);
        try {
            if (isJsp(o.getClass())) {
                defaultInstanceManager.destroyInstance(o);
            }
            preDestroy(unwrapped, unwrapped.getClass());
        } finally {
            webContext.destroy(unwrapped);
            if (unwrapped != o) { // PojoEndpointServer, they create and track a cc so release it
                webContext.destroy(o);
            }
        }
    }

    private Object unwrap(final Object o) {
        return "org.apache.tomcat.websocket.pojo.PojoEndpointServer".equals(o.getClass().getName()) ?
                WebSocketTypes.unwrapWebSocketPojo(o) : o;
    }

    public void inject(final Object o) {
        try {
            webContext.inject(o);
        } catch (final OpenEJBException e) {
            throw new InjectionFailedException(e);
        }
    }

    /**
     * Call postConstruct method on the specified instance recursively from deepest superclass to actual class.
     *
     * @param instance object to call postconstruct methods on
     * @param clazz    (super) class to examine for postConstruct annotation.
     * @throws IllegalAccessException                      if postConstruct method is inaccessible.
     * @throws java.lang.reflect.InvocationTargetException if call fails
     */
    public void postConstruct(final Object instance, final Class<?> clazz)
            throws IllegalAccessException, InvocationTargetException {
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class) {
            postConstruct(instance, superClass);
        }

        final Method[] methods = clazz.getDeclaredMethods();

        Method postConstruct = null;
        for (final Method method : methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if ((postConstruct != null)
                        || (method.getParameterTypes().length != 0)
                        || (Modifier.isStatic(method.getModifiers()))
                        || (method.getExceptionTypes().length > 0)
                        || (!method.getReturnType().getName().equals("void"))) {
                    throw new IllegalArgumentException("Invalid PostConstruct annotation. @PostConstruct methods "
                            + "should respect the following constraints:\n"
                            + "- no parameter (" + (method.getParameterTypes().length == 0) + ")\n"
                            + "- no exception should be declared (" + (method.getExceptionTypes().length == 0) + ")\n"
                            + "- should return void (" + method.getReturnType().getName().equals("void") + ")\n"
                            + "- should not be static (" + !Modifier.isStatic(method.getModifiers()) + ")\n");
                }
                postConstruct = method;
            }
        }

        // At the end the postconstruct annotated
        // method is invoked
        if (postConstruct != null) {
            final boolean accessibility = postConstruct.isAccessible();
            postConstruct.setAccessible(true);
            postConstruct.invoke(instance);
            postConstruct.setAccessible(accessibility);
        }

    }


    /**
     * Call preDestroy method on the specified instance recursively from deepest superclass to actual class.
     *
     * @param instance object to call preDestroy methods on
     * @param clazz    (super) class to examine for preDestroy annotation.
     * @throws IllegalAccessException                      if preDestroy method is inaccessible.
     * @throws java.lang.reflect.InvocationTargetException if call fails
     */
    protected void preDestroy(final Object instance, final Class<?> clazz)
            throws IllegalAccessException, InvocationTargetException {
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class) {
            preDestroy(instance, superClass);
        }

        final Method[] methods = clazz.getDeclaredMethods();
        Method preDestroy = null;
        for (final Method method : methods) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                if ((method.getParameterTypes().length != 0)
                        || (Modifier.isStatic(method.getModifiers()))
                        || (method.getExceptionTypes().length > 0)
                        || (!method.getReturnType().getName().equals("void"))) {
                    throw new IllegalArgumentException("Invalid PreDestroy annotation");
                }
                preDestroy = method;
                break;
            }
        }

        // At the end the postconstruct annotated
        // method is invoked
        if (preDestroy != null) {
            final boolean accessibility = preDestroy.isAccessible();
            preDestroy.setAccessible(true);
            preDestroy.invoke(instance);
            preDestroy.setAccessible(accessibility);
        }
    }

    private static final class WebSocketTypes { // extracted for lazy loading
        private static final WebSocketTypes WEB_SOCKET_TYPES = new WebSocketTypes();
        private final Method getPojo;

        private WebSocketTypes() {
            Method tmp;
            try {
                tmp = WebSocketTypes.class.getClassLoader()
                        .loadClass("org.apache.tomcat.websocket.pojo.PojoEndpointBase")
                        .getDeclaredMethod("getPojo");
                tmp.setAccessible(true);
            } catch (final NoSuchMethodException e) {
                if ("true".equals(SystemInstance.get().getProperty("tomee.websocket.skip", "false"))) {
                    tmp = null;
                } else {
                    throw new IllegalStateException(e);
                }
            } catch (final ClassNotFoundException e) {
                tmp = null; // no websocket support
            }
            getPojo = tmp;
        }

        private static Object unwrapWebSocketPojo(final Object o) {
            try {
                return WEB_SOCKET_TYPES.getPojo == null ? o : WEB_SOCKET_TYPES.getPojo.invoke(o);
            } catch (final IllegalAccessException | InvocationTargetException | NullPointerException e) {
                return o;
            }
        }
    }

    private static final class TomcatInjections { // load when needed
        private TomcatInjections() {
            // no-op
        }

        private static Map<String, Map<String, String>> buildInjectionMap(final NamingResourcesImpl namingResources) {
            final Map<String, Map<String, String>> injectionMap = new HashMap<>();
            for (final Injectable resource : namingResources.findLocalEjbs()) {
                addInjectionTarget(resource, injectionMap);
            }
            for (final Injectable resource : namingResources.findEjbs()) {
                addInjectionTarget(resource, injectionMap);
            }
            for (final Injectable resource : namingResources.findEnvironments()) {
                addInjectionTarget(resource, injectionMap);
            }
            for (final Injectable resource : namingResources.findMessageDestinationRefs()) {
                addInjectionTarget(resource, injectionMap);
            }
            for (final Injectable resource : namingResources.findResourceEnvRefs()) {
                addInjectionTarget(resource, injectionMap);
            }
            for (final Injectable resource : namingResources.findResources()) {
                addInjectionTarget(resource, injectionMap);
            }
            for (final Injectable resource : namingResources.findServices()) {
                addInjectionTarget(resource, injectionMap);
            }
            return injectionMap;
        }

        private static void addInjectionTarget(final Injectable resource, final Map<String, Map<String, String>> injectionMap) {
            final List<InjectionTarget> injectionTargets = resource.getInjectionTargets();
            if (injectionTargets != null && !injectionTargets.isEmpty()) {
                final String jndiName = resource.getName();
                for (final InjectionTarget injectionTarget : injectionTargets) {
                    final String clazz = injectionTarget.getTargetClass();
                    Map<String, String> injections = injectionMap.get(clazz);
                    if (injections == null) {
                        injections = new HashMap<>();
                        injectionMap.put(clazz, injections);
                    }
                    injections.put(injectionTarget.getTargetName(), jndiName);
                }
            }
        }
    }
}
