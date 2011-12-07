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
package org.apache.openejb.core.osgi.impl;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.loader.SystemInstance;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Rev$ $Date$
 */
public class Deployer implements BundleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Deployer.class);

    private Map<Bundle, List<ServiceRegistration>> registrations = new ConcurrentHashMap<Bundle, List<ServiceRegistration>>();
    private Map<Bundle, AppContext> appContexts = new ConcurrentHashMap<Bundle, AppContext>();

    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STARTED:
                deploy(event.getBundle());
                break;
            case BundleEvent.STOPPED:
                undeploy(event.getBundle());
                break;
        }
    }

    private void deploy(Bundle bundle) {
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        final ClassLoader osgiCl = new OSGIClassLoader(bundle);
        Thread.currentThread().setContextClassLoader(osgiCl);

        try {
            try {
                try {
                    DeploymentLoader deploymentLoader = new DeploymentLoader();
                    AppModule appModule = deploymentLoader.load(osgiCl); // here file doesn't mean anything
                    LOGGER.info("deploying bundle #" + bundle.getBundleId() + " as an EJBModule");

                    ConfigurationFactory configurationFactory = new ConfigurationFactory();
                    AppInfo appInfo = configurationFactory.configureApplication(appModule);

                    Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                    AppContext appContext = assembler.createApplication(appInfo);
                    appContexts.put(bundle, appContext);
                    LOGGER.info("Application deployed: " + appInfo.path);

                    registrations.put(bundle, new ArrayList<ServiceRegistration>());
                    registerService(bundle, appContext);

                } catch (UnknownModuleTypeException unknowException) {
                    LOGGER.info("bundle #" + bundle.getBundleId() + " is not an EJBModule");
                } catch(Exception ex) {
                    LOGGER.error("can't deploy bundle #" + bundle.getBundleId(), ex);
                }
            } catch (Exception ex1) {
                LOGGER.error("can't deploy bundle #" + bundle.getBundleId(), ex1);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void undeploy(Bundle bundle) {
        if (registrations.containsKey(bundle)) {
            for (ServiceRegistration registration : registrations.get(bundle)) {
                registration.unregister();
            }
            registrations.remove(bundle);
        }

        if (appContexts.containsKey(bundle)) {
            try {
                SystemInstance.get().getComponent(Assembler.class).destroyApplication(appContexts.remove(bundle));
            } catch (UndeployException e) {
                LOGGER.error("can't undeployer the bundle", e);
            }
        }
        LOGGER.info(String.format("[Deployer] Bundle %s has been stopped", bundle.getSymbolicName()));
    }

    /**
     * Register OSGi Service for EJB so calling the service will actually call the EJB
     *
     * @param bundle the deployed bundle
     * @param appContext the appcontext to search EJBs
     */
    private void registerService(Bundle bundle, AppContext appContext) {
        LOGGER.info("Registering remote EJBs as OSGi services");
        BundleContext context = bundle.getBundleContext();
        for (BeanContext beanContext : appContext.getBeanContexts()) {
            try {
                if (beanContext.getBusinessRemoteInterface() != null) {
                    LOGGER.error(String.format("registering: %s", beanContext.getEjbName()));
                    registerService(beanContext, context, beanContext.getBusinessRemoteInterfaces());
                }
                if (beanContext.getBusinessLocalInterface() != null) {
                    // don't register it since it should only be used in the bundle itself
                    // registerService(beanContext, context, beanContext.getBusinessLocalInterfaces());
                }
                if (beanContext.isLocalbean()) {
                    // don't register it since it should only be used in the bundle itself
                    // registerService(beanContext, context, Arrays.asList(beanContext.getBusinessLocalBeanInterface()));
                }
            } catch (Exception e) {
                LOGGER.error(String.format("[Deployer] can't register: %s", beanContext.getEjbName()));
            }
        }
    }

    private void registerService(BeanContext beanContext, BundleContext context, List<Class> interfaces) {
        if (!interfaces.isEmpty()) {
            Class<?>[] itfs = interfaces.toArray(new Class<?>[interfaces.size()]);
            try {
                Object service = Proxy.newProxyInstance(itfs[0].getClassLoader(), itfs, new Handler(beanContext));
                registrations.get(context.getBundle()).add(context.registerService(str(itfs), service, new Properties()));
                LOGGER.info(String.format("EJB registered: %s for interfaces %s", beanContext.getEjbName(), interfaces));
            } catch (IllegalArgumentException iae) {
                LOGGER.error(String.format("can't register: %s for interfaces %s", beanContext.getEjbName(), interfaces));
            }
        }
    }

    private String[] str(Class<?>[] itfs) {
        String[] itfsStr = new String[itfs.length];
        for (int i = 0; i < itfs.length; i++) {
            itfsStr[i] = itfs[i].getName();
        }
        return itfsStr;
    }

    private static class Handler implements InvocationHandler {
        private BeanContext beanContext;

        public Handler(BeanContext bc) {
            beanContext = bc;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final RpcContainer container = RpcContainer.class.cast(beanContext.getContainer());
            return container.invoke(beanContext.getDeploymentID(),
                    beanContext.getInterfaceType(method.getDeclaringClass()),
                    method.getDeclaringClass(), method, args, null);
        }
    }

    private static class OSGIClassLoader extends ClassLoader {
        private final Bundle backingBundle;

        public OSGIClassLoader(Bundle bundle) {
            super(null);
            backingBundle = bundle;
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            try {
                return this.backingBundle.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]", cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle [" + backingBundle + "]");
                e.initCause(ncdfe);
                throw e;
            }
        }

        protected URL findResource(String name) {
            return backingBundle.getResource(name);
        }

        protected Enumeration findResources(String name) throws IOException {
            return backingBundle.getResources(name);
        }

        public URL getResource(String name) {
            return findResource(name);
        }

        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        public String toString() {
            return "OSGIClassLoader for [" + backingBundle + "]";
        }
    }
}
