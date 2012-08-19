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

import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
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
    public static final String DEFAULT_ASYNCHRONOUS_POOL_ID = "asynchronous-pool";
    public static final String ASYNCHRONOUS_POOL_CORE_SIZE = DEFAULT_ASYNCHRONOUS_POOL_ID + ".core-size";
    public static final String ASYNCHRONOUS_POOL_MAX_SIZE = DEFAULT_ASYNCHRONOUS_POOL_ID + ".max-size";
    public static final String ASYNCHRONOUS_POOL_KEEP_ALIVE = DEFAULT_ASYNCHRONOUS_POOL_ID + ".keep-alive";

    private static final int DEFAULT_CORE_POOL_SIZE = 10;
    private static final int DEFAULT_MAX_POOL_SIZE = 20;
    private static final int DEFAULT_KEEP_ALIVE = 60;

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

    public AppContext(String id, SystemInstance systemInstance, ClassLoader classLoader, Context globalJndiContext, Context appJndiContext, boolean standaloneModule, Collection<ServiceInfo> configuration) {
        super(id, systemInstance.getOptions());
        this.classLoader = classLoader;
        this.systemInstance = systemInstance;
        this.globalJndiContext = globalJndiContext;
        this.appJndiContext = appJndiContext;
        this.standaloneModule = standaloneModule;
        this.blockingQueue = new LinkedBlockingQueue<Runnable>();

        // pool config
        int corePoolSize = DEFAULT_CORE_POOL_SIZE;
        int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        int keepAlive = DEFAULT_KEEP_ALIVE;

        ServiceInfo appConfig = ServiceInfos.find(configuration, id);
        if (appConfig != null) {
            corePoolSize = Integer.parseInt(appConfig.properties.getProperty(ASYNCHRONOUS_POOL_CORE_SIZE, Integer.toString(corePoolSize)).trim());
            maxPoolSize = Integer.parseInt(appConfig.properties.getProperty(ASYNCHRONOUS_POOL_MAX_SIZE, Integer.toString(maxPoolSize)).trim());
            keepAlive = Integer.parseInt(appConfig.properties.getProperty(ASYNCHRONOUS_POOL_KEEP_ALIVE, Integer.toString(keepAlive)).trim());
        } else {
            appConfig = ServiceInfos.find(configuration, DEFAULT_ASYNCHRONOUS_POOL_ID);
            if (appConfig != null) {
                int l = DEFAULT_ASYNCHRONOUS_POOL_ID.length() + 1;
                corePoolSize = Integer.parseInt(appConfig.properties.getProperty(ASYNCHRONOUS_POOL_CORE_SIZE.substring(l), Integer.toString(corePoolSize)).trim());
                maxPoolSize = Integer.parseInt(appConfig.properties.getProperty(ASYNCHRONOUS_POOL_MAX_SIZE.substring(l), Integer.toString(maxPoolSize)).trim());
                keepAlive = Integer.parseInt(appConfig.properties.getProperty(ASYNCHRONOUS_POOL_KEEP_ALIVE.substring(l), Integer.toString(keepAlive)).trim());
            }
        }

        corePoolSize = getOptions().get(ASYNCHRONOUS_POOL_CORE_SIZE, corePoolSize);
        maxPoolSize = getOptions().get(ASYNCHRONOUS_POOL_MAX_SIZE, maxPoolSize);
        keepAlive = getOptions().get(ASYNCHRONOUS_POOL_KEEP_ALIVE, keepAlive);

        this.asynchPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAlive, TimeUnit.SECONDS, blockingQueue, new DaemonThreadFactory("@Asynch", id));
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
