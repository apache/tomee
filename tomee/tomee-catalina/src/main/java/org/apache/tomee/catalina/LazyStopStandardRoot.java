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

import org.apache.catalina.Context;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.TrackedWebResource;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.catalina.webresources.StandardRoot;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class LazyStopStandardRoot implements WebResourceRoot, JmxEnabled {
    private final WebResourceRoot delegate;
    private final boolean isJmxEnabled;
    private final boolean isLifecycleMBeanBase;

    public LazyStopStandardRoot(final WebResourceRoot delegate) {
        this.delegate = delegate;
        this.isJmxEnabled = JmxEnabled.class.isInstance(delegate);
        this.isLifecycleMBeanBase = LifecycleMBeanBase.class.isInstance(delegate);
    }

    @Override
    public String[] list(final String path) {
        return delegate.list(path);
    }

    @Override
    public Set<String> listWebAppPaths(final String path) {
        return delegate.listWebAppPaths(path);
    }

    @Override
    public boolean mkdir(final String path) {
        return delegate.mkdir(path);
    }

    @Override
    public boolean write(final String path, final InputStream is, final boolean overwrite) {
        return delegate.write(path, is, overwrite);
    }

    @Override
    public WebResource getResource(final String path) {
        return delegate.getResource(path);
    }

    @Override
    public WebResource getClassLoaderResource(final String path) {
        return delegate.getClassLoaderResource(path);
    }

    @Override
    public WebResource[] getClassLoaderResources(final String path) {
        return delegate.getClassLoaderResources(path);
    }

    @Override
    public WebResource[] getResources(final String path) {
        return delegate.getResources(path);
    }

    @Override
    public WebResource[] listResources(final String path) {
        return delegate.listResources(path);
    }

    @Override
    public void createWebResourceSet(final ResourceSetType type, final String webAppMount, final URL url, final String internalPath) {
        delegate.createWebResourceSet(type, webAppMount, url, internalPath);
    }

    @Override
    public void createWebResourceSet(final ResourceSetType type, final String webAppMount, final String base, final String archivePath, final String internalPath) {
        delegate.createWebResourceSet(type, webAppMount, base, archivePath, internalPath);
    }

    @Override
    public void addPreResources(final WebResourceSet webResourceSet) {
        delegate.addPreResources(webResourceSet);
    }

    @Override
    public WebResourceSet[] getPreResources() {
        return delegate.getPreResources();
    }

    @Override
    public void addJarResources(final WebResourceSet webResourceSet) {
        delegate.addJarResources(webResourceSet);
    }

    @Override
    public WebResourceSet[] getJarResources() {
        return delegate.getJarResources();
    }

    @Override
    public void addPostResources(final WebResourceSet webResourceSet) {
        delegate.addPostResources(webResourceSet);
    }

    @Override
    public WebResourceSet[] getPostResources() {
        return delegate.getPostResources();
    }

    @Override
    public void setAllowLinking(final boolean allowLinking) {
        delegate.setAllowLinking(allowLinking);
    }

    @Override
    public boolean getAllowLinking() {
        return delegate.getAllowLinking();
    }

    @Override
    public void setCachingAllowed(final boolean cachingAllowed) {
        delegate.setCachingAllowed(cachingAllowed);
    }

    @Override
    public boolean isCachingAllowed() {
        return delegate.isCachingAllowed();
    }

    @Override
    public long getCacheTtl() {
        return delegate.getCacheTtl();
    }

    @Override
    public void setCacheTtl(final long cacheTtl) {
        delegate.setCacheTtl(cacheTtl);
    }

    @Override
    public long getCacheMaxSize() {
        return delegate.getCacheMaxSize();
    }

    @Override
    public void setCacheMaxSize(final long cacheMaxSize) {
        delegate.setCacheMaxSize(cacheMaxSize);
    }

    @Override
    public void setCacheObjectMaxSize(final int cacheObjectMaxSize) {
        delegate.setCacheObjectMaxSize(cacheObjectMaxSize);
    }

    @Override
    public int getCacheObjectMaxSize() {
        return delegate.getCacheObjectMaxSize();
    }

    @Override
    public void setTrackLockedFiles(final boolean trackLockedFiles) {
        delegate.setTrackLockedFiles(trackLockedFiles);
    }

    @Override
    public boolean getTrackLockedFiles() {
        return delegate.getTrackLockedFiles();
    }

    public List<String> getTrackedResources() { // IDE?
        return StandardRoot.class.cast(delegate).getTrackedResources();
    }

    @Override
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    public void setContext(final Context context) {
        delegate.setContext(context);
    }

    @Override
    public void backgroundProcess() {
        delegate.backgroundProcess();
    }

    @Override
    public void gc() {
        delegate.gc();
    }

    @Override
    public void registerTrackedResource(final TrackedWebResource trackedResource) {
        delegate.registerTrackedResource(trackedResource);
    }

    @Override
    public void deregisterTrackedResource(final TrackedWebResource trackedResource) {
        delegate.deregisterTrackedResource(trackedResource);
    }

    @Override
    public List<URL> getBaseUrls() {
        return delegate.getBaseUrls();
    }

    @Override
    public void setDomain(final String domain) {
        if (isJmxEnabled) {
            JmxEnabled.class.cast(delegate).setDomain(domain);
        }
    }

    @Override
    public String getDomain() {
        if (isJmxEnabled) {
            return JmxEnabled.class.cast(delegate).getDomain();
        }
        return null;
    }

    @Override
    public ObjectName getObjectName() {
        if (isJmxEnabled) {
            return JmxEnabled.class.cast(delegate).getObjectName();
        }
        return null;
    }

    @Override
    public void postDeregister() {
        if (isLifecycleMBeanBase) {
            LifecycleMBeanBase.class.cast(delegate).postDeregister();
        }
    }

    @Override
    public void postRegister(final Boolean registrationDone) {
        if (isLifecycleMBeanBase) {
            LifecycleMBeanBase.class.cast(delegate).postRegister(registrationDone);
        }
    }

    @Override
    public void preDeregister() throws Exception {
        if (isLifecycleMBeanBase) {
            LifecycleMBeanBase.class.cast(delegate).preDeregister();
        }
    }

    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {
        if (isLifecycleMBeanBase) {
            return LifecycleMBeanBase.class.cast(delegate).preRegister(server, name);
        }
        return name;
    }

    @Override
    public void addLifecycleListener(final LifecycleListener listener) {
        delegate.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return delegate.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(final LifecycleListener listener) {
        delegate.removeLifecycleListener(listener);
    }

    @Override
    public LifecycleState getState() {
        return delegate.getState();
    }

    @Override
    public String getStateName() {
        return delegate.getStateName();
    }

    @Override
    public void init() throws LifecycleException {
        delegate.init();
    }

    @Override
    public void start() throws LifecycleException {
        delegate.start();
    }

    @Override
    public void stop() throws LifecycleException {
        // delegate.stop();
    }

    @Override
    public void destroy() throws LifecycleException {
        // delegate.destroy();
    }

    public void internalDestroy() throws LifecycleException {
        if (LifecycleState.STARTED == delegate.getState()) {
            internalStop();
        }
        delegate.destroy();
    }

    public void internalStop() throws LifecycleException {
        delegate.stop();
    }

    @Override
    public String getArchiveIndexStrategy() {
        return delegate.getArchiveIndexStrategy();
    }

    @Override
    public void setArchiveIndexStrategy( String archiveIndexStrategy ) {
        delegate.setArchiveIndexStrategy( archiveIndexStrategy );
    }

    @Override
    public WebResourceRoot.ArchiveIndexStrategy getArchiveIndexStrategyEnum() {
        return delegate.getArchiveIndexStrategyEnum();
    }

    public WebResourceRoot getDelegate() {
        return delegate;
    }
}
