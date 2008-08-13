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

import java.util.Date;
import java.util.Properties;
import java.util.Collection;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.spi.Assembler;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEjbVersion;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Exported
public class OpenEJB implements ApplicationContextAware{
    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources");
    private static Messages messages = new Messages("org.apache.openejb.util.resources");

    private final Properties properties = new Properties();

    /**
     * The assembler for Spring embedded.
     */
    private SpringAssembler assembler;

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
     * The ApplicationServer to be used by the OpenEJB server, or null for the
     * default ApplicationServer.
     */
    private ApplicationServer applicationServer;

    private final Collection<ContainerProvider> containers = new ArrayList<ContainerProvider>();

    private final Collection<Resource> resources = new ArrayList<Resource>();

    private boolean importContext = true;

    private Throwable initialized;
    private ApplicationContext applicationContext;

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

    public ApplicationServer getApplicationServer() {
        return applicationServer;
    }

    public void setApplicationServer(ApplicationServer applicationServer) {
        this.applicationServer = applicationServer;
    }

    public Collection<ContainerProvider> getContainers() {
        return containers;
    }

    public void setContainers(Collection<ContainerProvider> containers) {
        this.containers.clear();
        this.containers.addAll(containers);
    }

    public Collection<Resource> getResources() {
        return resources;
    }

    public void setResources(Collection<Resource> resources) {
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

    public boolean isStarted() {
        return initialized != null || SystemInstance.get().getComponent(ContainerSystem.class) != null;
    }

    @PostConstruct
    public void start() throws OpenEJBException {
        //
        // Already started?
        //
        if (isStarted()) {
            if (initialized != null){
                String msg = messages.message("startup.alreadyInitialized");
                logger.error(msg, initialized);
                throw new OpenEJBException(msg, initialized);
            } else {
                String msg = messages.message("startup.alreadyInitialized");
                logger.error(msg);
                throw new OpenEJBException(msg);
            }
        }
        initialized = new Exception("Initialized at " + new Date()).fillInStackTrace();

        //
        // System Instance
        //
        try {
            SystemInstance.init(properties);
        } catch (Exception e) {
            throw new OpenEJBException(e);
        }
        SystemInstance system = SystemInstance.get();

        // do not deploy applications in claspath
        system.setProperty("openejb.deployments.classpath", "false");

        // we are in embedded mode
        system.setProperty("openejb.embedded", "true");


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
        // Application Server
        //
        if (applicationServer == null) {
            applicationServer = new ServerFederation();
        }
        system.setComponent(ApplicationServer.class, applicationServer);

        //
        // Assembler
        //
        assembler = new SpringAssembler(this);
        SystemInstance.get().setComponent(Assembler.class, assembler);

        try {
            assembler.init(props);
        } catch (OpenEJBException oe) {
            logger.fatal("startup.assemblerFailedToInitialize", oe);
            throw oe;
        } catch (Throwable t) {
            String msg = messages.message("startup.assemblerEncounteredUnexpectedError");
            logger.fatal(msg, t);
            throw new OpenEJBException(msg, t);
        }

        try {
            assembler.build();
        } catch (OpenEJBException oe) {
            logger.fatal("startup.assemblerFailedToBuild", oe);
            throw oe;
        } catch (Throwable t) {
            String msg = messages.message("startup.assemblerEncounterUnexpectedBuildError");
            logger.fatal(msg, t);
            throw new OpenEJBException(msg, t);
        }

        //
        // Container System
        //
        ContainerSystem containerSystem = assembler.getContainerSystem();
        if (containerSystem == null) {
            String msg = messages.message("startup.assemblerReturnedNullContainer");
            logger.fatal(msg);
            throw new OpenEJBException(msg);
        }
        system.setComponent(ContainerSystem.class, containerSystem);
        printContainerSystem(containerSystem);


        //
        // Done
        //
        logger.debug("startup.ready");
    }

    private void printContainerSystem(ContainerSystem containerSystem) {
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

    public Throwable getInitialized() {
        return initialized;
    }
}
