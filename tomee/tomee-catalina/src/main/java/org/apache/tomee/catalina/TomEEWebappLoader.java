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
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.classloader.ClassLoaderComparator;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.modeler.Registry;

import javax.management.ObjectName;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static class TomEEClassLoader extends URLClassLoader implements ClassLoaderComparator {
        private ClassLoader app;
        private WebappClassLoader webapp;
        private String appPath;

        public TomEEClassLoader(final String appId, final ClassLoader appCl, final WebappClassLoader webappCl) {
            super(enrichedUrls(webappCl.getURLs(), webappCl), webappCl); // in fact this classloader = webappclassloader since we add nothing to this
            this.appPath = appId;
            this.app = appCl; // only used to manage resources since webapp.getParent() should be app
            this.webapp = webappCl;
        }

        private static URL[] enrichedUrls(final URL[] urLs, final ClassLoader cl) {
            final List<Integer> skipped = new ArrayList<Integer>();

            // while we are here validate the urls regading tomee rules
            for (int i = 0; i < urLs.length; i++) {
                final File file;
                try {
                    file = URLs.toFile(urLs[i]);
                } catch (IllegalStateException ise) {
                    continue;
                }

                try {
                    if (!TomEEClassLoaderEnricher.validateJarFile(file)) {
                        skipped.add(i);
                    }
                } catch (IOException e) {
                    // ignored
                }
            }

            final URL[] additional = SystemInstance.get().getComponent(WebAppEnricher.class).enrichment(cl);
            final Set<URL> returnedUrls = new HashSet<URL>();
            for (int i = 0; i < urLs.length; i++) {
                if (!skipped.contains(i)) {
                    returnedUrls.add(urLs[i]);
                }
            }
            for (int i = 0; i < additional.length; i++) {
                returnedUrls.add(additional[i]);
            }
            return returnedUrls.toArray(new URL[returnedUrls.size()]);
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


            final Enumeration<URL> result;

            if (webapp.isStarted() || webapp.getParent() == null) { // we set a parent so if it is null webapp was detroyed
                add(urls, app.getResources(name));
                add(urls, webapp.getResources(name));
                result = new ArrayEnumeration(clear(urls.values()));
            } else {
                result = app.getResources(name);
            }

            if (TomEEClassLoaderEnricher.isSlf4jQuery(name)) {
                return TomEEClassLoaderEnricher.filterSlf4jImpl(result);
            }

            return result;
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
                    final URL cachedFile = ClassLoaderUtil.getUrlKeyCached(appPath, URLs.toFile(jarUrl));
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

        // act as app classloader, don't change it without testing against a BeanManagerHolder implementation

        @Override
        public boolean equals(final Object other) {
            return other == this || app.equals(other); // to be consistent with hashcode() used by maps used in BbeanManagerHolders
        }

        @Override
        public int hashCode() {
            return app.hashCode();
        }

        @Override // used to add cdi beans in the webbeanscontext, don't add parent beans
        public boolean isSame(final ClassLoader classLoader) {
            return classLoader == this || (webapp != null && webapp.equals(classLoader)); // not equals ;)
        }
    }
}
