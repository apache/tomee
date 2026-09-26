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

package org.apache.openejb;

import org.apache.openejb.async.AsynchronousPool;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.webbeans.config.WebBeansContext;

import jakarta.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class AppContext extends DeploymentContext {
    private final long startTime = System.currentTimeMillis();
    private final SystemInstance systemInstance;
    private final ClassLoader classLoader;
    private final Context globalJndiContext;
    private final Context appJndiContext;
    private final boolean standaloneModule;
    private boolean cdiEnabled;
    private boolean readOnlyAppNamingContext;
    private final AtomicInteger pendingLateModules = new AtomicInteger();
    private WebBeansContext webBeansContext;
    private final Collection<Injection> injections = new HashSet<>();
    private final Map<String, Object> bindings = new HashMap<>();

    // TODO perhaps to be deleted
    private final List<BeanContext> beanContexts = new ArrayList<>();
    private final List<WebContext> webContexts = new ArrayList<>();

    public AppContext(final String id, final SystemInstance systemInstance, final ClassLoader classLoader, final Context globalJndiContext, final Context appJndiContext, final boolean standaloneModule) {
        super(id, systemInstance.getOptions());
        this.classLoader = classLoader;
        this.systemInstance = systemInstance;
        this.globalJndiContext = globalJndiContext;
        this.appJndiContext = appJndiContext;
        this.standaloneModule = standaloneModule;
    }

    /**
     * Whether the component naming contexts of this application must be marked read only once its
     * deployments have been created, as required by EE.5.3.4 and Enterprise Beans 10.4.4. The flag is
     * recorded when the application is configured but only applied after the container's own bindings
     * are done, so that modules deployed later - the web modules of an ear, for instance - are covered
     * too.
     */
    public boolean isReadOnlyAppNamingContext() {
        return readOnlyAppNamingContext;
    }

    public void setReadOnlyAppNamingContext(final boolean readOnlyAppNamingContext) {
        this.readOnlyAppNamingContext = readOnlyAppNamingContext;
    }

    /**
     * How many modules are still to be deployed after createApplication returns - the web modules of
     * an ear, which TomcatWebAppBuilder starts one by one. The application scoped naming context stays
     * writable until the last of them is in, so the container can keep binding into it.
     */
    public void setPendingLateModules(final int count) {
        pendingLateModules.set(count);
    }

    /**
     * Consumes one deployment pass.
     *
     * @return true when no further module can bind into the application naming context
     */
    public boolean lastModuleDeployed() {
        // the count is how many passes are still to come after this one, so a pass only closes the
        // application context when there is nothing left to consume
        return pendingLateModules.get() <= 0 || pendingLateModules.getAndDecrement() <= 0;
    }

    public Collection<Injection> getInjections() {
        return injections;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public BeanManager getBeanManager() {
        if (webBeansContext == null) {
            return null;
        }
        return webBeansContext.getBeanManagerImpl();
    }

    public WebBeansContext getWebBeansContext() {
        return webBeansContext;
    }

    public void setWebBeansContext(final WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
    }

    public List<WebContext> getWebContexts() {
        return webContexts;
    }


    public boolean isCdiEnabled() {
        return cdiEnabled;
    }

    public void setCdiEnabled(final boolean cdiEnabled) {
        this.cdiEnabled = cdiEnabled;
    }

    @Override
    public String getId() {
        return super.getId();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Deprecated
    public List<BeanContext> getDeployments() {
        return getBeanContexts();
    }

    public List<BeanContext> getBeanContexts() {
        return beanContexts;
    }

    public SystemInstance getSystemInstance() {
        return systemInstance;
    }

    public Context getAppJndiContext() {
        return appJndiContext;
    }

    public Context getGlobalJndiContext() {
        return globalJndiContext;
    }

    public boolean isStandaloneModule() {
        return standaloneModule;
    }

    public AsynchronousPool getAsynchronousPool() {
        return get(AsynchronousPool.class);
    }

    public long getStartTime() {
        return startTime;
    }
}
