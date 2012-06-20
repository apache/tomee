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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina;

import java.beans.PropertyChangeListener;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;

public class LazyStopLoader implements Loader, Lifecycle {
    private final Loader delegate;
    private ClassLoader classLoader;

    public LazyStopLoader(Loader loader) {
        delegate = loader;
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        if (delegate instanceof Lifecycle) {
            ((Lifecycle) delegate).addLifecycleListener(listener);
        }
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        if (delegate instanceof Lifecycle) {
            return ((Lifecycle) delegate).findLifecycleListeners();
        }
        return new LifecycleListener[0];
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        if (delegate instanceof Lifecycle) {
            ((Lifecycle) delegate).removeLifecycleListener(listener);
        }
    }

    @Override
    public void init() throws LifecycleException {
        if (delegate instanceof Lifecycle) {
            ((Lifecycle) delegate).init();
        }
    }

    @Override
    public void start() throws LifecycleException {
        if (delegate instanceof Lifecycle) {
            ((Lifecycle) delegate).start();
        }
    }

    @Override
    public void stop() throws LifecycleException {
        classLoader = delegate.getClassLoader();
        if (delegate instanceof Lifecycle) {
            ((Lifecycle) delegate).stop();
        }
    }

    @Override
    public void destroy() throws LifecycleException {
        if (delegate instanceof Lifecycle) {
            ((Lifecycle) delegate).destroy();
        }
    }

    @Override
    public LifecycleState getState() {
        if (delegate instanceof Lifecycle) {
            return ((Lifecycle) delegate).getState();
        }
        return null;
    }

    @Override
    public String getStateName() {
        if (delegate instanceof Lifecycle) {
            return ((Lifecycle) delegate).getStateName();
        }
        return null;
    }

    @Override
    public void backgroundProcess() {
        delegate.backgroundProcess();
    }

    @Override
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }

    @Override
    public Container getContainer() {
        return delegate.getContainer();
    }

    @Override
    public void setContainer(Container container) {
        delegate.setContainer(container);
    }

    @Override
    public boolean getDelegate() {
        return delegate.getDelegate();
    }

    @Override
    public void setDelegate(boolean delegate) {
        this.delegate.setDelegate(delegate);
    }

    @Override
    public String getInfo() {
        return delegate.getInfo();
    }

    @Override
    public boolean getReloadable() {
        return delegate.getReloadable();
    }

    @Override
    public void setReloadable(boolean reloadable) {
        delegate.setReloadable(reloadable);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    @Override
    public void addRepository(String repository) {
        delegate.addRepository(repository);
    }

    @Override
    public String[] findRepositories() {
        return delegate.findRepositories();
    }

    @Override
    public boolean modified() {
        return delegate.modified();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }

    public ClassLoader getStopClassLoader() {
        return classLoader;
    }
}
