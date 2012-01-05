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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.openejb.util.ArrayEnumeration;
import org.apache.tomcat.util.ExceptionUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public class TomEEWebappLoader extends WebappLoader {
    private ClassLoader appClassLoader;
    private ClassLoader tomEEClassLoader;

    public TomEEWebappLoader(final ClassLoader classLoader) {
        appClassLoader = classLoader;
    }

    @Override public ClassLoader getClassLoader() {
        return tomEEClassLoader;
    }

    @Override protected void startInternal() throws LifecycleException {
        super.startInternal();
        final ClassLoader webappCl = super.getClassLoader();
        tomEEClassLoader = new TomEEClassLoader(appClassLoader, webappCl);
        try {
             DirContextURLStreamHandler.bind(tomEEClassLoader, getContainer().getResources());
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            throw new LifecycleException("start: ", t);
        }
    }

    public static class TomEEClassLoader extends ClassLoader {
        private ClassLoader app;
        private ClassLoader webapp;

        public TomEEClassLoader(final ClassLoader appCl, final ClassLoader webappCl) {
            super(webappCl); // in fact this classloader = webappclassloader since we add nothing to this
            app = appCl; // only used to manage resources since webapp.getParent() should be app
            webapp = webappCl;
        }

        /**
         * we totally override this method to be able to remove duplicated resources.
         *
         * @param name
         * @return
         * @throws IOException
         */
        @Override public Enumeration<URL> getResources(final String name) throws IOException {
            List<URL> urls = new ArrayList<URL>();

            if (webapp instanceof WebappClassLoader && ((WebappClassLoader) webapp).isStarted() || webapp.getParent() == null) { // we set a parent so if it is null webapp was detroyed
                addIfNotExist(urls, app.getResources(name), true);
                addIfNotExist(urls, webapp.getResources(name), false);
                return new ArrayEnumeration(urls);
            }
            return app.getResources(name);
        }

        private static void addIfNotExist(Collection<URL> urls, Enumeration<URL> enumUrls, boolean force) {
            try {
                while (enumUrls.hasMoreElements()) {
                    URL url = enumUrls.nextElement();
                    if (force || !urls.contains(url)) {
                        urls.add(url);
                    }
                }
            } catch (IllegalStateException ese) {
                // ignored: if jars are already closed...shutdown for instance
            }
        }
    }
}
