/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.catalina.remote;

import org.apache.catalina.Context;
import org.apache.catalina.Loader;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.URLClassLoader;

public class ServerClassLoaderLoader implements Loader {
    private final Context container;
    private final FakeWebAppLoader classloader;

    public ServerClassLoaderLoader(final Context tomEERemoteWebapp) {
        container = tomEERemoteWebapp;
        classloader = new FakeWebAppLoader(ServerClassLoaderLoader.class.getClassLoader());
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
