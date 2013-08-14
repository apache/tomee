/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina.remote;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.Wrapper;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.ServerServlet;
import org.apache.tomee.catalina.IgnoredStandardContext;
import org.apache.tomee.catalina.OpenEJBValve;

import java.beans.PropertyChangeListener;

public class TomEERemoteWebapp extends IgnoredStandardContext {
    private static final String CONTEXT_NAME = SystemInstance.get().getProperty("tomee.remote.support.context", "/tomee");
    private static final String MAPPING = SystemInstance.get().getProperty("tomee.remote.support.mapping", "/ejb");

    public TomEERemoteWebapp() {
        setDocBase("");
        setParentClassLoader(OpenEJB.class.getClassLoader());
        setDelegate(true);
        setName(CONTEXT_NAME);
        setPath(CONTEXT_NAME);
        setLoader(new ServerClassLoaderLoader(this));
        addValve(new OpenEJBValve()); // ensure security context is resetted (ThreadLocal) for each request
    }

    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        final Wrapper servlet = createWrapper();
        servlet.setName(ServerServlet.class.getSimpleName());
        servlet.setServletClass(ServerServlet.class.getName());
        addChild(servlet);
        addServletMapping(MAPPING, ServerServlet.class.getSimpleName());
    }

    private static class ServerClassLoaderLoader implements Loader {
        private static final String[] EMPTY_ARRAY = new String[0];

        private final TomEERemoteWebapp container;

        public ServerClassLoaderLoader(final TomEERemoteWebapp tomEERemoteWebapp) {
            container = tomEERemoteWebapp;
        }

        @Override
        public void backgroundProcess() {
            // no-op
        }

        @Override
        public ClassLoader getClassLoader() {
            return OpenEJB.class.getClassLoader();
        }

        @Override
        public Container getContainer() {
            return container;
        }

        @Override
        public void setContainer(final Container container) {
            // no-op
        }

        @Override
        public boolean getDelegate() {
            return true;
        }

        @Override
        public void setDelegate(final boolean delegate) {
            // no-op
        }

        @Override
        public String getInfo() {
            return ServerClassLoaderLoader.class.getName() + "/1.0";
        }

        @Override
        public boolean getReloadable() {
            return false;
        }

        @Override
        public void setReloadable(final boolean reloadable) {
            // no-op
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            // no-op
        }

        @Override
        public void addRepository(final String repository) {
            // no-op
        }

        @Override
        public String[] findRepositories() {
            return EMPTY_ARRAY;
        }

        @Override
        public boolean modified() {
            return false;
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            // no-op
        }
    }
}
