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
package org.apache.tomee.catalina.remote;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.Wrapper;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.ServerServlet;
import org.apache.tomee.catalina.IgnoredStandardContext;
import org.apache.tomee.catalina.OpenEJBValve;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.URLClassLoader;

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
        addValve(new OpenEJBValve()); // Ensure security context is reset (ThreadLocal) for each request
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
        private final TomEERemoteWebapp container;
        private final FakeWebAppLoader classloader;

        public ServerClassLoaderLoader(final TomEERemoteWebapp tomEERemoteWebapp) {
            container = tomEERemoteWebapp;
            classloader = new FakeWebAppLoader(OpenEJB.class.getClassLoader());
        }

        @Override
        public void backgroundProcess() {
            // no-op
        }

        @Override
        public ClassLoader getClassLoader() {
            return classloader;
        }

        @Override
        public Context getContext() {
            return container;
        }

        @Override
        public void setContext(final Context context) {
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
        public boolean modified() {
            return false;
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            // no-op
        }
    }

    // mainly for StandardContext.setClassLoaderProperty() otherwise OpenEJB.class.getClassLoader() would be fine
    public static class FakeWebAppLoader extends URLClassLoader {
        // ignored but validated by tomcat, avoid warnings
        private boolean clearReferencesHttpClientKeepAliveThread;
        private boolean clearReferencesStopThreads;
        private boolean clearReferencesStopTimerThreads;
        private boolean clearReferencesStatic;

        public FakeWebAppLoader(final ClassLoader classLoader) {
            super(new URL[0], classLoader);
        }

        public boolean isClearReferencesHttpClientKeepAliveThread() {
            return clearReferencesHttpClientKeepAliveThread;
        }

        public void setClearReferencesHttpClientKeepAliveThread(final boolean clearReferencesHttpClientKeepAliveThread) {
            this.clearReferencesHttpClientKeepAliveThread = clearReferencesHttpClientKeepAliveThread;
        }

        public boolean isClearReferencesStopThreads() {
            return clearReferencesStopThreads;
        }

        public void setClearReferencesStopThreads(final boolean clearReferencesStopThreads) {
            this.clearReferencesStopThreads = clearReferencesStopThreads;
        }

        public boolean isClearReferencesStopTimerThreads() {
            return clearReferencesStopTimerThreads;
        }

        public void setClearReferencesStopTimerThreads(final boolean clearReferencesStopTimerThreads) {
            this.clearReferencesStopTimerThreads = clearReferencesStopTimerThreads;
        }

        public boolean isClearReferencesStatic() {
            return clearReferencesStatic;
        }

        public void setClearReferencesStatic(final boolean clearReferencesStatic) {
            this.clearReferencesStatic = clearReferencesStatic;
        }
    }
}
