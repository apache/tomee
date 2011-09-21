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

import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.webbeans.config.WebBeansContext;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
*/
public class AppContext extends DeploymentContext {

    private final SystemInstance systemInstance;
    private final ClassLoader classLoader;
    private final Context globalJndiContext;
    private final Context appJndiContext;
    private final boolean standaloneModule;
    private boolean cdiEnabled = false;
    private WebBeansContext webBeansContext;
    private final Collection<Injection> injections = new HashSet<Injection>();
    private final Map<String, Object> bindings = new HashMap<String, Object>();

    private BlockingQueue<Runnable> blockingQueue;
    private ExecutorService asynchPool;

    // TODO perhaps to be deleted
    private final List<BeanContext> beanContexts = new ArrayList<BeanContext>();
    private final List<WebContext> webContexts = new ArrayList<WebContext>();

    public AppContext(String id, SystemInstance systemInstance, ClassLoader classLoader, Context globalJndiContext, Context appJndiContext, boolean standaloneModule) {
        super(id, systemInstance.getOptions());
        this.classLoader = classLoader;
        this.systemInstance = systemInstance;
        this.globalJndiContext = globalJndiContext;
        this.appJndiContext = appJndiContext;
        this.standaloneModule = standaloneModule;
        this.blockingQueue = new LinkedBlockingQueue<Runnable>();
        this.asynchPool = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, blockingQueue, new DaemonThreadFactory("@Asynch", id));
    }

    public Collection<Injection> getInjections() {
        return injections;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public BeanManager getBeanManager() {
        return webBeansContext.getBeanManagerImpl();
    }

    public WebBeansContext getWebBeansContext() {
        return webBeansContext;
    }

    public void setWebBeansContext(WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
    }

    public List<WebContext> getWebContexts() {
        return webContexts;
    }


    public boolean isCdiEnabled() {
        return cdiEnabled;
    }

    public void setCdiEnabled(boolean cdiEnabled) {
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

    /**
     *  Asynchronous Invocation Thread Pool Methods
     */
    public Future<Object> submitTask(Callable<Object> callable){
        return asynchPool.submit(callable);
    }

    public boolean removeTask(Runnable task) {
        return blockingQueue.remove(task);
    }

}
