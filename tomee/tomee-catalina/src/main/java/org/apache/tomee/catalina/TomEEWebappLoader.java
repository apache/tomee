/**
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

package org.apache.tomee.catalina;

import java.net.URLClassLoader;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.loader.event.ComponentAdded;
import org.apache.openejb.loader.event.ComponentRemoved;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.openejb.util.URLs;
import org.apache.tomcat.util.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tomcat.util.modeler.Registry;

public class TomEEWebappLoader extends WebappLoader {
    private ClassLoader appClassLoader;
    private TomEEClassLoader tomEEClassLoader;
    private String appPath;

    public TomEEWebappLoader(final String appId, final ClassLoader classLoader) {
        this.appPath = appId;
        appClassLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return tomEEClassLoader;
    }

    @Override
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        final WebappClassLoader webappCl = (WebappClassLoader) super.getClassLoader();
        tomEEClassLoader = new TomEEClassLoader(appPath, appClassLoader, webappCl);
        try {
            DirContextURLStreamHandler.bind(tomEEClassLoader, getContainer().getResources());
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            throw new LifecycleException("start: ", t);
        }
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        setState(LifecycleState.STOPPING);

        // Remove context attributes as appropriate
        if (getContainer() instanceof Context) {
            ServletContext servletContext =
                    ((Context) getContainer()).getServletContext();
            servletContext.removeAttribute(Globals.CLASS_PATH_ATTR);
        }

        // Throw away our current class loader
        final ClassLoader parent = tomEEClassLoader.webapp.getParent();
        if (parent instanceof WebappClassLoader && !((WebappClassLoader) parent).isStarted()) {
            tomEEClassLoader.webapp.setDelegate(false);
        }

        if (tomEEClassLoader.webapp.isStarted()) {
            tomEEClassLoader.webapp.stop();
        }

        if (appClassLoader instanceof WebappClassLoader && ((WebappClassLoader) appClassLoader).isStarted()) {
            ((WebappClassLoader) appClassLoader).stop();
        }

        DirContextURLStreamHandler.unbind(tomEEClassLoader);

        try {
            StandardContext ctx = (StandardContext) getContainer();
            String contextName = ctx.getName();
            if (!contextName.startsWith("/")) {
                contextName = "/" + contextName;
            }
            ObjectName cloname = new ObjectName
                    (MBeanUtils.getDomain(ctx) + ":type=WebappClassLoader,context="
                            + contextName + ",host=" + ctx.getParent().getName());
            Registry.getRegistry(null, null).unregisterComponent(cloname);
        } catch (Exception e) {
            e.printStackTrace(); // TODO
        }

        tomEEClassLoader = null;
    }

    public static class TomEEClassLoader extends URLClassLoader {
        private ClassLoader app;
        private WebappClassLoader webapp;
        private String appPath;
        private final HashMap<Class, Object> components = new HashMap<Class, Object>();

        public TomEEClassLoader(final String appId, final ClassLoader appCl, final WebappClassLoader webappCl) {
            super(webappCl.getURLs(), webappCl); // in fact this classloader = webappclassloader since we add nothing to this
            this.appPath = appId;
            this.app = appCl; // only used to manage resources since webapp.getParent() should be app
            this.webapp = webappCl;
        }

        public <T> T getComponent(final Class<T> type) {
            return (T) components.get(type);
        }

        public <T> T removeComponent(final Class<T> type) {
            return (T) components.remove(type);
        }

        /**
         * @param type the class type of the component required
         */
        public <T> T setComponent(final Class<T> type, final T value) {
            return (T) components.put(type, value);
        }

        /**
         * we totally override this method to be able to remove duplicated resources.
         *
         * @param name
         * @return
         * @throws IOException
         */
        @Override
        public Enumeration<URL> getResources(final String name) throws IOException {
            // DMB: On inspection I was seeing three copies of the same resource
            // due to the app.getResources and webapp.getResources call.
            // Switching from a list to a form of set trims the duplicates
            final Map<String, URL> urls = new HashMap<String, URL>();


            if (webapp.isStarted() || webapp.getParent() == null) { // we set a parent so if it is null webapp was detroyed
                add(urls, app.getResources(name));
                add(urls, webapp.getResources(name));
                return new ArrayEnumeration(clear(urls.values()));
            }
            return app.getResources(name);
        }

        private List<URL> clear(Iterable<URL> urls) { // take care of antiJarLocking
            final List<URL> clean = new ArrayList<URL>();
            for (URL url : urls) {
                final String urlStr = url.toExternalForm();
                URL jarUrl = null;
                if (urlStr.contains("!")) {
                    try {
                        jarUrl = new URL(urlStr.substring(0, urlStr.lastIndexOf('!')) + "!/");
                    } catch (MalformedURLException e) {
                        // ignored
                    }
                }

                if (jarUrl != null) {
                    final URL cachedFile = ClassLoaderUtil.getUrlKeyCached(appPath, file(jarUrl));
                    if (cachedFile != null) {
                        URL resource = null;
                        try {
                            resource = new URL("jar:file:" + cachedFile.getFile() + urlStr.substring(urlStr.lastIndexOf('!')));
                        } catch (MalformedURLException e) {
                            // ignored
                        }
                        if (resource != null && !clean.contains(resource)) {
                            clean.add(resource);
                        }
                    } else {
                        // DMB: Unsure if this is the correct hanlding of the else case,
                        // but in OSX the getUrlKeyCached returns null so the url was
                        // being ignored
                        clean.add(url);
                    }

                } else if (!clean.contains(url)) {
                    clean.add(url);
                }
            }
            return clean;
        }

        private void add(Map<String, URL> urls, Enumeration<URL> enumUrls) {
            try {
                while (enumUrls.hasMoreElements()) {
                    final URL url = enumUrls.nextElement();
                    urls.put(url.toExternalForm(), url);
                }
            } catch (IllegalStateException ese) {
                // ignored: if jars are already closed...shutdown for instance
            }
        }

        private static File file(URL jarUrl) {
            return URLs.toFile(jarUrl);
        }

        // act as app classloader, don't change it without testing against a BeanManagerHolder implementation

        @Override
        public boolean equals(Object other) {
            return other == this; // don't add: || app.equals(other);
        }

        @Override
        public int hashCode() {
            return app.hashCode();
        }
    }
}
