/**
 *
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
package org.apache.openejb.spring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.dynamic.PassthroughFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OpenEjbVersion;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Exported
public class OpenEJB implements ApplicationContextAware {
    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources");

    /**
     * Properties added to the OpenEJB SystemInstance on startup.
     */
    private final Properties properties = new Properties();

    /**
     * The TransactionManager to be used by the OpenEJB server, or null for the
     * default TransactionManager.
     */
    private TransactionManager transactionManager;

    /**
     * The SecurityService to be used by the OpenEJB server, or null for the
     * default SecurityService.
     */
    private SecurityService securityService;

    /**
     * Containers to add to the OpenEJB server.
     */
    private final Collection<ContainerProvider> containers = new ArrayList<ContainerProvider>();

    /**
     * Resources to add to the OpenEJB server.
     */
    private final Collection<ResourceProvider> resources = new ArrayList<ResourceProvider>();

    /**
     * Should the beans in the Spring context be imported into OpenEJB as resources?
     */
    private boolean importContext = true;

    /**
     * Is this bean starting?
     */
    private boolean starting;

    /**
     * While OpenEJB is starting any applications that are deployed are queued up until startup is complete.
     */
    private final List<AbstractApplication> applicationsToDeploy = new ArrayList<AbstractApplication>();

    /**
     * The application context to scan when importing Beans.
     */
    private ApplicationContext applicationContext;

    /**
     * The IDs of the resources we have already imported
     */
    private final Set<String> importedResourceIds = new TreeSet<String>();

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public Collection<ContainerProvider> getContainers() {
        return containers;
    }

    public void setContainers(Collection<? extends ContainerProvider> containers) {
        this.containers.clear();
        this.containers.addAll(containers);
    }

    public Collection<ResourceProvider> getResources() {
        return resources;
    }

    public void setResources(Collection<? extends ResourceProvider> resources) {
        this.resources.clear();
        this.resources.addAll(resources);
    }

    public boolean isImportContext() {
        return importContext;
    }

    public void setImportContext(boolean importContext) {
        this.importContext = importContext;
    }

    public Context getInitialContext() throws NamingException {
        if (!org.apache.openejb.OpenEJB.isInitialized()) {
            throw new IllegalStateException("Not started");
        }

        Context context = new org.apache.openejb.core.ivm.naming.InitContextFactory().getInitialContext(properties);
        return context;
    }

    public boolean isStarting() {
        return starting;
    }

    public boolean isStarted() {
        return SystemInstance.get().getComponent(ContainerSystem.class) != null;
    }

    @PostConstruct
    public void start() throws OpenEJBException {
        // Transaction mananager and system instance can only be set once per SystemInstance (one per ClassLoader)
        if (isStarted()) {
            if (transactionManager != null) {
                throw new OpenEJBException("TransactionManager can not be set because OpenEJB has already been initalized");
            }
            if (securityService != null) {
                throw new OpenEJBException("SecurityService can not be set because OpenEJB has already been initalized");
            }
        }

        //
        // Is this bean already starting?  This helps avoid anoying spring loop backs.
        //
        if (starting) {
            throw new OpenEJBException("OpenEJB already starting");
        }
        starting = true;

        //
        // System Instance
        //
        SystemInstance system = SystemInstance.get();
        system.getProperties().putAll(properties);

        // do not deploy applications in claspath
        system.setProperty("openejb.deployments.classpath", "false");

        // we are in embedded mode
        system.setProperty("openejb.embedded", "true");

        //
        // Add TransactionManager and SecurityService to OpenEJB
        //
        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        Assembler assembler;
        if (isStarted()) {
            assembler = SystemInstance.get().getComponent(Assembler.class);
        } else {
            //
            // Startup message
            //
            OpenEjbVersion versionInfo = OpenEjbVersion.get();

            if (properties.getProperty("openejb.nobanner") == null) {
                System.out.println("Apache OpenEJB " + versionInfo.getVersion() + "    build: " + versionInfo.getDate() + "-" + versionInfo.getTime());
                System.out.println("" + versionInfo.getUrl());
            }

            Logger logger2 = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
            logger2.info("startup.banner", versionInfo.getUrl(), new Date(), versionInfo.getCopyright(),
                    versionInfo.getVersion(), versionInfo.getDate(), versionInfo.getTime());

            logger.info("openejb.home = " + SystemInstance.get().getHome().getDirectory().getAbsolutePath());
            logger.info("openejb.base = " + SystemInstance.get().getBase().getDirectory().getAbsolutePath());

            Properties props = new Properties(SystemInstance.get().getProperties());

            if (properties.isEmpty()) {
                logger.debug("startup.noInitializationProperties");
            } else {
                props.putAll(properties);
            }

            //
            // Assembler
            //
            assembler = new Assembler();
            assembler.createProxyFactory(configurationFactory.configureService(ProxyFactoryInfo.class));

            //
            // Transaction Manager
            //
            TransactionManager transactionManager = getTransactionManager();
            if (transactionManager == null) {
                transactionManager = getBeanForType(applicationContext, TransactionManager.class);
            }
            if (transactionManager != null) {
                TransactionServiceInfo info = initPassthrough(new TransactionServiceInfo(), "TransactionManager", transactionManager);
                assembler.createTransactionManager(info);
            }

            //
            // Security Service
            //
            SecurityService securityService = getSecurityService();
            if (securityService == null) {
                securityService = getBeanForType(applicationContext, SecurityService.class);
            }
            if (securityService != null) {
                SecurityServiceInfo info = initPassthrough(new SecurityServiceInfo(), "SecurityService", securityService);
                assembler.createSecurityService(info);
            }
        }

        //
        // Resources
        //
        for (Object resourceProvider : applicationContext.getBeansOfType(ResourceProvider.class).values()) {
            resources.add((ResourceProvider) resourceProvider);
        }
        for (ResourceProvider resourceProvider : getResources()) {
            ResourceInfo info = configurationFactory.configureService(resourceProvider.getResourceDefinition(), ResourceInfo.class);
            importedResourceIds.add(info.id);
            assembler.createResource(info);
        }
        if (isImportContext() && applicationContext != null) {
            for (String beanName : applicationContext.getBeanDefinitionNames()) {
                if (!importedResourceIds.contains(beanName)) {
                    Class beanType = applicationContext.getType(beanName);
                    Class factoryType = applicationContext.getType("&" + beanName);
                    if (isImportableType(beanType, factoryType)) {
                        SpringReference factory = new SpringReference(applicationContext, beanName, beanType);

                        ResourceInfo info = initPassthrough(beanName, new ResourceInfo(), "Resource", factory);
                        info.types = getTypes(beanType);
                        assembler.createResource(info);
                    }
                }
            }
        }

        //
        // Containers
        //
        for (Object containerProvider : applicationContext.getBeansOfType(ContainerProvider.class).values()) {
            containers.add((ContainerProvider) containerProvider);
        }
        for (ContainerProvider containerProvider: getContainers()) {
            ContainerInfo info = configurationFactory.createContainerInfo(containerProvider.getContainerDefinition());
            assembler.createContainer(info);
        }

        //
        // Done
        //
        starting = false;
        logger.debug("startup.ready");

        List<AbstractApplication> applicationsToDeploy = new ArrayList<AbstractApplication>(this.applicationsToDeploy);
        this.applicationsToDeploy.clear();
        for (AbstractApplication application : applicationsToDeploy) {
            application.deployApplication();
        }
    }

    public void deployApplication(AbstractApplication application) throws OpenEJBException {
        if (isStarting() || !isStarted()) {
            applicationsToDeploy.add(application);
        } else {
            application.deployApplication();
        }
    }

    public void printContainerSystem() {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (logger.isDebugEnabled()) {
            //
            // Log Containers
            //
            logger.debug("startup.debugContainers", containerSystem.containers().length);
            if (containerSystem.containers().length > 0) {
                logger.debug("startup.debugContainersType");
                for (Container container : containerSystem.containers()) {
                    String entry = "   ";
                    switch (container.getContainerType()) {
                        case BMP_ENTITY:
                            entry += "BMP ENTITY  ";
                            break;
                        case CMP_ENTITY:
                            entry += "CMP ENTITY  ";
                            break;
                        case STATEFUL:
                            entry += "STATEFUL    ";
                            break;
                        case STATELESS:
                            entry += "STATELESS   ";
                            break;
                        case MESSAGE_DRIVEN:
                            entry += "MESSAGE     ";
                            break;
                    }
                    entry += container.getContainerID();
                    logger.debug("startup.debugEntry", entry);
                }
            }

            //
            // Log Deployments
            //
            logger.debug("startup.debugDeployments", containerSystem.deployments().length);
            if (containerSystem.deployments().length > 0) {
                logger.debug("startup.debugDeploymentsType");
                for (DeploymentInfo deployment : containerSystem.deployments()) {
                    String entry = "   ";
                    switch (deployment.getComponentType()) {
                        case BMP_ENTITY:
                            entry += "BMP_ENTITY  ";
                            break;
                        case CMP_ENTITY:
                            entry += "CMP_ENTITY  ";
                            break;
                        case STATEFUL:
                            entry += "STATEFUL    ";
                            break;
                        case STATELESS:
                            entry += "STATELESS   ";
                            break;
                        case SINGLETON:
                            entry += "SINGLETON   ";
                            break;
                        case MANAGED:
                            entry += "MANAGED     ";
                            break;
                        case MESSAGE_DRIVEN:
                            entry += "MESSAGE     ";
                            break;
                    }
                    entry += deployment.getDeploymentID();
                    logger.debug("startup.debugEntry", entry);
                }
            }
        }
    }

    private <T> T getBeanForType(ApplicationContext applicationContext, Class<T> type) throws OpenEJBException {
        String[] names = applicationContext.getBeanNamesForType(type);
        if (names.length == 0) {
            return null;
        }
        if (names.length > 1) {
            throw new OpenEJBException("Multiple " + type.getSimpleName() + " beans in application context: " + Arrays.toString(names));
        }

        String name = names[0];
        importedResourceIds.add(name);
        return (T) applicationContext.getBean(name);
    }

    private boolean isImportableType(Class type, Class factoryType) {
        return !type.isAnnotationPresent(Exported.class) &&
                !BeanPostProcessor.class.isAssignableFrom(type) &&
                !BeanFactoryPostProcessor.class.isAssignableFrom(type) &&
                (factoryType == null || !factoryType.isAnnotationPresent(Exported.class));
    }

    private <T extends ServiceInfo> T initPassthrough(T info, String serviceType, Object instance) {
        return initPassthrough("Spring Supplied " + serviceType, info, serviceType, instance);
    }

    private <T extends ServiceInfo> T initPassthrough(String id, T info, String serviceType, Object instance) {
        info.id = id;
        info.service = serviceType;
        info.types = getTypes(instance.getClass());
        PassthroughFactory.add(info, instance);
        return info;
    }

    private List<String> getTypes(Class type) {
        LinkedHashSet<String> types = new LinkedHashSet<String>();
        addTypes(type, types);
        return new ArrayList<String>(types);
    }

    private void addTypes(Class clazz, LinkedHashSet<String> types) {
        if (clazz == null || Object.class.equals(clazz) || Serializable.class.equals(clazz)) {
            return;
        }
        if (types.add(clazz.getName())) {
            addTypes(clazz.getSuperclass(), types);
            for (Class intf : clazz.getInterfaces()) {
                addTypes(intf, types);
            }
        }
    }
}
