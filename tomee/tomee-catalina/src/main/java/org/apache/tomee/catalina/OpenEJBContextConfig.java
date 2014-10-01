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

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.ApplicationListener;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.WebXml;
import org.apache.catalina.realm.DataSourceRealm;
import org.apache.catalina.startup.ContextConfig;
import org.apache.naming.factory.Constants;
import org.apache.naming.resources.BaseDirContext;
import org.apache.naming.resources.DirContextURLConnection;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.naming.resources.ProxyDirContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClassListInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ParamValueInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.ServiceUtils;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.util.bcel.classfile.AnnotationEntry;
import org.apache.tomcat.util.bcel.classfile.ElementValuePair;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomee.catalina.realm.TomEEDataSourceRealm;
import org.apache.tomee.common.NamingUtil;
import org.apache.tomee.common.ResourceFactory;
import org.apache.tomee.loader.TomcatHelper;
import org.apache.xbean.finder.IAnnotationFinder;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class OpenEJBContextConfig extends ContextConfig {
    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB, OpenEJBContextConfig.class);

    private static final String MYFACES_TOMEEM_CONTAINER_INITIALIZER = "org.apache.tomee.myfaces.TomEEMyFacesContainerInitializer";
    private static final String TOMEE_MYFACES_CONTEXT_LISTENER = "org.apache.tomee.myfaces.TomEEMyFacesContextListener";
    private static final String ADJUST_DATASOURCE_JNDI_NAMES = SystemInstance.get().getProperty("tomee.resources.adjust-web-xml-jndi-name", "true");
    private static final File BASE = SystemInstance.get().getBase().getDirectory();

    private TomcatWebAppBuilder.StandardContextInfo info;
    private IAnnotationFinder finder;
    private ClassLoader tempLoader;

    // processAnnotationXXX is called for each folder of WEB-INF
    // since we store all classes in WEB-INF we will do it only once so use this boolean to avoid multiple processing
    private boolean webInfClassesAnnotationsProcessed;

    public OpenEJBContextConfig(final TomcatWebAppBuilder.StandardContextInfo standardContextInfo) {
        logger.debug("OpenEJBContextConfig({0})", standardContextInfo.toString());
        info = standardContextInfo;
    }

    public void finder(final IAnnotationFinder finder, final ClassLoader tmpLoader) {
        this.finder = finder;
        this.tempLoader = tmpLoader;
    }

    @Override
    public void configureStart() {
        super.configureStart();
        adjustDataSourceNameIfNecessary(); // doing it here to potentially factorize resource id resolution
        addAddedJAXWsServices();
        cleanUpRestServlets();
    }

    private void addAddedJAXWsServices() {
        final WebAppInfo webAppInfo = info.get();
        final AppInfo appInfo = info.app();
        if (webAppInfo == null || appInfo == null || "false".equalsIgnoreCase(appInfo.properties.getProperty("openejb.jaxws.add-missing-servlets", "true"))) {
            return;
        }

        try { // if no jaxws classes are here don't try anything
            OpenEJBContextConfig.class.getClassLoader().loadClass("org.apache.openejb.server.webservices.WsServlet");
        } catch (final ClassNotFoundException e) {
            return;
        } catch (final NoClassDefFoundError e) {
            return;
        }

        for (final ServletInfo servlet : webAppInfo.servlets) {
            if (!servlet.mappings.iterator().hasNext()) { // no need to do anything
                continue;
            }

            for (final ParamValueInfo pv : servlet.initParams) {
                if ("openejb-internal".equals(pv.name) && "true".equals(pv.value)) {
                    if (context.findChild(servlet.servletName) == null) {
                        final Wrapper wrapper = context.createWrapper();
                        wrapper.setName(servlet.servletName);
                        wrapper.setServletClass("org.apache.openejb.server.webservices.WsServlet");

                        // add servlet to context
                        context.addChild(wrapper);
                        context.addServletMapping(servlet.mappings.iterator().next(), wrapper.getName());
                    }
                    break;
                }
            }
        }
    }

    private void cleanUpRestServlets() {
        final WebAppInfo webAppInfo = info.get();
        final AppInfo appInfo = info.app();
        if (webAppInfo == null || appInfo == null || "false".equalsIgnoreCase(appInfo.properties.getProperty("openejb.jaxrs.on", "true"))) {
            return;
        }

        final Container[] children = context.findChildren();
        final Map<String, Container> mappedChildren = new HashMap<String, Container>();
        if (children != null) {
            // index potential rest containers by class to cleanup applications defined as servlet
            for (final Container c : children) {
                if (!(c instanceof StandardWrapper)) {
                    continue;
                }

                final StandardWrapper wrapper = (StandardWrapper) c;

                final String appSpec = wrapper.getInitParameter("javax.ws.rs.Application");
                if (appSpec != null) {
                    mappedChildren.put(appSpec, c);
                } else {
                    final String app = wrapper.getInitParameter(Application.class.getName());
                    if (app != null) {
                        mappedChildren.put(app, c);
                    } else if (wrapper.getServletClass() == null) {
                        try {
                            if (Application.class.isAssignableFrom(
                                    context.getLoader().getClassLoader().loadClass(wrapper.getServletName()))) {
                                context.removeChild(c); // remove directly since it is not in restApplications
                            }
                        } catch (final Exception e) {
                            // no-op
                        }
                    }
                }
            }

            // cleanup
            for (final String clazz : webAppInfo.restApplications) {
                final Container child = mappedChildren.get(clazz);
                try { // remove only "fake" servlets to let users use their own stuff
                    if (child != null) {
                        final String servletClass = StandardWrapper.class.cast(child).getServletClass();
                        if ("org.apache.openejb.server.rest.OpenEJBRestServlet".equals(servletClass) || !HttpServlet.class.isAssignableFrom(info.loader().loadClass(servletClass))) {
                            context.removeChild(child);
                        }
                    }
                } catch (final NoClassDefFoundError e) {
                    context.removeChild(child);
                } catch (final ClassNotFoundException e) {
                    context.removeChild(child);
                }
            }
        }
    }

    @Override
    protected void processContextConfig(final Digester digester, final URL contextXml) {
        try {
            super.processContextConfig(digester, replaceKnownRealmsByTomEEOnes(contextXml));
        } catch (final MalformedURLException e) {
            super.processContextConfig(digester, contextXml);
        }
    }

    private URL replaceKnownRealmsByTomEEOnes(final URL contextXml) throws MalformedURLException {
        return new URL(contextXml.getProtocol(), contextXml.getHost(), contextXml.getPort(), contextXml.getFile(), new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(final URL u) throws IOException {
                final URLConnection c = contextXml.openConnection();
                return new URLConnection(u) {
                    @Override
                    public void connect() throws IOException {
                        c.connect();
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        IO.copy(c.getInputStream(), baos);
                        return new ByteArrayInputStream(new String(baos.toByteArray())
                                        .replace(DataSourceRealm.class.getName(), TomEEDataSourceRealm.class.getName()
                                    ).getBytes());
                    }

                    @Override
                    public String toString() {
                        return c.toString();
                    }
                };
            }
        });
    }

    @Override
    protected void contextConfig(final Digester digester) {
        final NamingResources resources;
        if (context != null) {
            resources = context.getNamingResources();
        } else {
            resources = null;
        }

        if (resources instanceof OpenEJBNamingResource) {
            ((OpenEJBNamingResource) resources).setTomcatResource(true);
        }
        super.contextConfig(digester);
        if (resources instanceof OpenEJBNamingResource) {
            ((OpenEJBNamingResource) resources).setTomcatResource(false);
        }
    }

    private void adjustDataSourceNameIfNecessary() {
        if (context == null || "false".equalsIgnoreCase(ADJUST_DATASOURCE_JNDI_NAMES)) {
            return;
        }

        final NamingResources resources = context.getNamingResources();
        if (resources == null) {
            return;
        }

        final ContextResource[] foundResources = resources.findResources();
        String[] ids = null;
        if (foundResources != null) {
            for (final ContextResource resource : foundResources) {
                if ("javax.sql.DataSource".equals(resource.getType())
                        && !ResourceFactory.class.getName().equals(resource.getProperty(Constants.FACTORY))) {
                    String jndiName = (String) resource.getProperty("mappedName");
                    if (jndiName == null) {
                        jndiName = resource.getName();
                    }
                    if (jndiName == null) {
                        continue;
                    }

                    if (ids == null) {
                        final Properties props = new Properties();
                        final OpenEjbConfiguration runningConfig = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
                        final List<String> resourceIds = new ArrayList<String>();
                        if (runningConfig != null) {
                            for (final ResourceInfo resourceInfo : runningConfig.facilities.resources) {
                                if (ConfigurationFactory.isResourceType(resourceInfo.service, resourceInfo.types, "javax.sql.DataSource")
                                        && ServiceUtils.implies(props, resourceInfo.properties)) {
                                    resourceIds.add(resourceInfo.id);
                                }
                            }
                        }
                        ids = resourceIds.toArray(new String[resourceIds.size()]);
                    }

                    String mostMatchingId = null;
                    for (final String id : ids) {
                        if (id.equals(jndiName)) {
                            mostMatchingId = jndiName;
                            break;
                        } else if (jndiName.endsWith("/" + id) && mostMatchingId == null) {
                            mostMatchingId = id;
                        } else if (id.endsWith("/" + jndiName) && mostMatchingId == null) {
                            mostMatchingId = "openejb/Resource/" + id;
                        }
                    }

                    if (mostMatchingId != null) {
                        resource.setProperty("mappedName", "java:" + mostMatchingId);
                        resource.setProperty(NamingUtil.RESOURCE_ID, "java:" + mostMatchingId);
                        resource.setProperty(Constants.FACTORY, ResourceFactory.class.getName());
                    }
                }
            }
        }
    }

    @Override
    protected WebXml createWebXml() {
        String prefix = "";
        if (context instanceof StandardContext) {
            final StandardContext standardContext = (StandardContext) context;
            prefix = standardContext.getEncodedPath();
            if (prefix.startsWith("/")) {
                prefix = prefix.substring(1);
            }
        }
        return new OpenEJBWebXml(prefix);
    }

    public class OpenEJBWebXml extends WebXml {
        public static final String OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY = "openejb.web.xml.major";

        private String prefix;

        public OpenEJBWebXml(final String prefix) {
            this.prefix = prefix;
        }

        @Override
        public int getMajorVersion() {
            return SystemInstance.get().getOptions().get(prefix + "." + OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY,
                    SystemInstance.get().getOptions().get(OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY, super.getMajorVersion()));
        }
    }

    @Override
    protected void webConfig() {
        TomcatHelper.configureJarScanner(context);

        // read the real config
        super.webConfig();

        if (IgnoredStandardContext.class.isInstance(context)) { // no need of jsf
            return;
        }

        final ClassLoader classLoader = context.getLoader().getClassLoader();

        // add myfaces auto-initializer if mojarra is not present
        try {
            classLoader.loadClass("com.sun.faces.context.SessionMap");
            return;
        } catch (final Throwable ignored) {
            // no-op
        }
        try {
            final Class<?> myfacesInitializer = Class.forName(MYFACES_TOMEEM_CONTAINER_INITIALIZER, true, classLoader);
            final ServletContainerInitializer instance = (ServletContainerInitializer) myfacesInitializer.newInstance();
            context.addServletContainerInitializer(instance, getJsfClasses(context));
            context.addApplicationListener(new ApplicationListener(TOMEE_MYFACES_CONTEXT_LISTENER, false)); // cleanup listener
        } catch (final Exception ignored) {
            // no-op
        } catch (final NoClassDefFoundError error) {
            // no-op
        }
    }

    private Set<Class<?>> getJsfClasses(final Context context) {
        final WebAppBuilder builder = SystemInstance.get().getComponent(WebAppBuilder.class);
        final ClassLoader cl = context.getLoader().getClassLoader();
        final Map<String, Set<String>> scanned = builder.getJsfClasses().get(cl);

        if (scanned == null || scanned.isEmpty()) {
            return null;
        }

        final Set<Class<?>> classes = new HashSet<Class<?>>();
        for (final Set<String> entry : scanned.values()) {
            for (final String name : entry) {
                try {
                    classes.add(cl.loadClass(name));
                } catch (final ClassNotFoundException ignored) {
                    logger.warning("class '" + name + "' was found but can't be loaded as a JSF class");
                }
            }
        }

        return classes;
    }

    @Override // called before processAnnotationsFile so using it as hook to init webInfClassesAnnotationsProcessed
    protected void processServletContainerInitializers(final ServletContext ctx) {
        webInfClassesAnnotationsProcessed = false;
        try {
            super.processServletContainerInitializers(ctx);
            final Iterator<Map.Entry<ServletContainerInitializer,Set<Class<?>>>> iterator = initializerClassMap.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<ServletContainerInitializer, Set<Class<?>>> entry = iterator.next();
                final ServletContainerInitializer sci = entry.getKey();
                final String classname = sci.getClass().getName();
                if (classname.equals("org.apache.myfaces.ee6.MyFacesContainerInitializer")
                        || classname.equals("org.springframework.web.SpringServletContainerInitializer")) {
                    for (final Map.Entry<Class<?>, Set<ServletContainerInitializer>> scanning : typeInitializerMap.entrySet()) {
                        final Set<ServletContainerInitializer> scis = scanning.getValue();
                        if (scis != null && scis.contains(sci)) {
                            scis.remove(sci);
                        }
                    }
                    iterator.remove();
                }
            }

            final ClassLoader loader = context.getLoader().getClassLoader();

            // spring-web (not scanned)
            try {
                final Class<?> initializer = Class.forName("org.springframework.web.SpringServletContainerInitializer", true, loader);
                final ServletContainerInitializer instance = (ServletContainerInitializer) initializer.newInstance();
                typeInitializerMap.put(Class.forName("org.springframework.web.WebApplicationInitializer", true, loader), Collections.singleton(instance));
                initializerClassMap.put(instance, new HashSet<Class<?>>());
            } catch (final Exception ignored) {
                // no-op
            } catch (final NoClassDefFoundError error) {
                // no-op
            }

            // scanned SCIs
            if (typeInitializerMap.size() > 0 && finder != null) {
                for (final Map.Entry<Class<?>, Set<ServletContainerInitializer>> entry : typeInitializerMap.entrySet()) {
                    if (entry.getValue() == null || entry.getValue().isEmpty()) {
                        continue;
                    }

                    final Class<?> annotation = entry.getKey();
                    for (final ServletContainerInitializer sci : entry.getValue()) {
                        if (annotation.isAnnotation()) {
                            try {
                                final Class<? extends Annotation> reloadedAnnotation = Class.class.cast(tempLoader.loadClass(annotation.getName()));
                                addClassesWithRightLoader(loader, sci, finder.findAnnotatedClasses(reloadedAnnotation));
                            } catch (final Throwable th) {
                                // no-op
                            }
                        } else {
                            try {
                                final Class<?> reloadedClass = tempLoader.loadClass(annotation.getName());

                                final List<Class<?>> implementations;
                                if (annotation.isInterface()) {
                                    implementations = List.class.cast(finder.findImplementations(reloadedClass));
                                } else {
                                    implementations = List.class.cast(finder.findSubclasses(reloadedClass));
                                }

                                addClassesWithRightLoader(loader, sci, implementations);
                            } catch (final Throwable th) {
                                // no-op
                            }
                        }
                    }
                }
            }

            // done
            finder = null;
            tempLoader = null;
        } catch (final RuntimeException e) { // if exception occurs we have to clear the threadlocal
            webInfClassesAnnotationsProcessed = false;
            throw e;
        }
    }

    @Override // called after processAnnotationsXX so using it as hook to reset webInfClassesAnnotationsProcessed
    protected void processAnnotations(final Set<WebXml> fragments, final boolean handlesTypesOnly) {
        webInfClassesAnnotationsProcessed = false;
        super.processAnnotations(fragments, handlesTypesOnly);
    }

    @Override
    protected void processAnnotationsFile(final File file, final WebXml fragment, final boolean handlesTypesOnly) {
        try {
            if (NewLoaderLogic.skip(file.toURI().toURL())) {
                return;
            }
        } catch (final MalformedURLException e) {
            // no-op: let it be
        }

        final WebAppInfo webAppInfo = info.get();
        if (webAppInfo == null) {
            super.processAnnotationsFile(file, fragment, handlesTypesOnly);
            return;
        }

        internalProcessAnnotations(file, webAppInfo, fragment);
    }

    @Override
    protected void processAnnotationsUrl(final URL currentUrl, final WebXml fragment, final boolean handlesTypeOnly) {
        if (NewLoaderLogic.skip(currentUrl)) { // we potentially see all common loader urls
            return;
        }

        final WebAppInfo webAppInfo = info.get();
        if (webAppInfo == null) {
            super.processAnnotationsUrl(currentUrl, fragment, handlesTypeOnly);
            return;
        }

        File currentUrlAsFile;
        try {
            currentUrlAsFile = URLs.toFile(currentUrl);
        } catch (final IllegalArgumentException iae) {
            if ("jndi".equals(currentUrl.getProtocol())) {
                String file = currentUrl.getFile();
                try {
                    final URLConnection connection = currentUrl.openConnection();
                    if (connection instanceof DirContextURLConnection) {
                        final DirContextURLStreamHandler handler = DirContextURLStreamHandler.class.cast(Reflections.get(currentUrl, "handler"));
                        final ProxyDirContext dirContext = ProxyDirContext.class.cast(Reflections.get(handler, "context"));
                        final String host = String.class.cast(Reflections.get(dirContext, "hostName"));
                        final String contextPath = String.class.cast(Reflections.get(dirContext, "contextPath"));
                        final Object context = Reflections.get(dirContext, "dirContext");

                        if (BaseDirContext.class.isInstance(context)) {
                            file = file.replace("/" + host + contextPath, BaseDirContext.class.cast(context).getDocBase());
                        } else {
                            throw new OpenEJBRuntimeException("Context not supported: " + context);
                        }

                        currentUrlAsFile = new File(file);
                    } else {
                        throw new OpenEJBRuntimeException("can't find webapp [" + webAppInfo.contextRoot + "], connection is not a DirContextURLConnection " + connection);
                    }
                } catch (final Exception ex) {
                    throw new OpenEJBRuntimeException("can't find webapp [" + webAppInfo.contextRoot + "]", ex);
                }
            } else {
                throw new OpenEJBRuntimeException("protocol not supported '" + currentUrl.getProtocol() + "'");
            }
        }

        internalProcessAnnotations(currentUrlAsFile, webAppInfo, fragment);
    }

    private void internalProcessAnnotations(final File currentUrlAsFile, final WebAppInfo webAppInfo, final WebXml fragment) {
        for (final ClassListInfo webAnnotated : webAppInfo.webAnnotatedClasses) {
            try {
                if (!isIncludedIn(webAnnotated.name, currentUrlAsFile)) {
                    continue;
                }

                internalProcessAnnotationsStream(webAnnotated.list, fragment, false);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private void addClassesWithRightLoader(final ClassLoader loader, final ServletContainerInitializer sci, final List<Class<?>> implementations) throws ClassNotFoundException {
        final Set<Class<?>> classes = initializerClassMap.get(sci);
        for (final Class<?> c : implementations) {
            classes.add(loader.loadClass(c.getName()));
        }
    }

    private void internalProcessAnnotationsStream(final Collection<String> urls, final WebXml fragment, final boolean handlesTypeOnly) {
        for (final String url : urls) {
            InputStream is = null;
            try {
                is = new URL(url).openStream();
                processAnnotationsStream(is, fragment, handlesTypeOnly);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            } finally {
                IO.close(is);
            }
        }
    }

    @Override
    protected void processAnnotationWebServlet(final String className, final AnnotationEntry ae, final WebXml fragment) {
        try {
            super.processAnnotationWebServlet(className, ae, fragment);
        } catch (final IllegalArgumentException iae) {
            // otherwise TCKs are not passing, hope to be able to let it with next TCK versions

            String[] urlPatterns = null;
            for (final ElementValuePair evp : ae.getElementValuePairs()) {
                final String name = evp.getNameString();
                if ("value".equals(name) || "urlPatterns".equals(name)) {
                    urlPatterns = processAnnotationsStringArray(evp.getValue());
                    break;
                }
            }

            if (urlPatterns != null) {
                for (final String pattern : urlPatterns) {
                    if (fragment.getServletMappings().containsKey(pattern)) {
                        logger.warning(iae.getMessage(), iae);
                        return;
                    }
                }
            }

            throw iae;
        }
    }

    private boolean isIncludedIn(final String filePath, final File classAsFile) throws MalformedURLException {
        final File toFile = URLs.toFile(new URL(filePath));
        File file;
        try { // symb links
            file = toFile.getCanonicalFile();
        } catch (final IOException e) {
            file = toFile;
        }

        File current = classAsFile;
        while (current != null && current.exists()) {
            if (current.equals(file)) {
                final File parent = current.getParentFile();
                if ("classes".equals(current.getName()) && parent != null && "WEB-INF".equals(parent.getName())) {
                    if (webInfClassesAnnotationsProcessed) {
                        return false;
                    }
                    webInfClassesAnnotationsProcessed = true;
                    return true;
                }
                return true;
            }
            current = current.getParentFile();
            if (BASE.equals(current)) {
                return false;
            }
        }

        return false;
        /* classAsFile s
        if (current != null && current.isDirectory()) {
            return false;
        }
        return (classAsFile == null
                    || !classAsFile.getName().endsWith(".jar") || !file.getName().endsWith(".jar"))
                && !webInf;
                */

    }
}
