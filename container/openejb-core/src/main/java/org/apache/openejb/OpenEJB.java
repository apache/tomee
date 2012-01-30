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

import java.util.Date;
import java.util.Properties;

import javax.transaction.TransactionManager;

import org.apache.openejb.assembler.classic.DeploymentExceptionManager;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.spi.Assembler;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.openejb.util.OptionsLog;
import org.apache.openejb.util.SafeToolkit;

/**
 * @version $Rev$ $Date$
 */
public final class OpenEJB {

    private static Instance instance;

    private OpenEJB() {
    }

    public static ApplicationServer getApplicationServer() {
        return SystemInstance.get().getComponent(ApplicationServer.class);
    }

    public static TransactionManager getTransactionManager(){
        return SystemInstance.get().getComponent(TransactionManager.class);
    }

    public static class Instance {
        private static Messages messages = new Messages("org.apache.openejb.util.resources");
        private final Throwable initialized;

        /**
         * 1 usage
         * org.apache.openejb.core.ivm.naming.InitContextFactory
         */
        public Instance(Properties props) throws OpenEJBException {
            this(props, new org.apache.openejb.core.ServerFederation());
        }

        /**
         * 2 usages
         */
        public Instance(Properties initProps, ApplicationServer appServer) throws OpenEJBException {
            if (appServer == null) {
                throw new IllegalArgumentException("appServer must not be null");
            }
            initialized = new InitializationException("Initialized at "+new Date()).fillInStackTrace();

            Logger.configure();
            Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources");

            try {
                SystemInstance.init(initProps);
                OptionsLog.install();
            } catch (Exception e) {
                throw new OpenEJBException(e);
            }
            SystemInstance system = SystemInstance.get();

            system.setComponent(DeploymentExceptionManager.class, new DeploymentExceptionManager());

            system.setComponent(ApplicationServer.class, appServer);
            //OWB support.  The classloader has to be able to load all OWB components including the ones supplied by OpenEjb.
            CdiBuilder.initializeOWB(getClass().getClassLoader());
            
            OpenEjbVersion versionInfo = OpenEjbVersion.get();
            if (!system.getOptions().get("openejb.nobanner", true)) {
                versionInfo.print(System.out);
            }

            Logger logger2 = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
            final String[] bannerValues = new String[] {
                    null, versionInfo.getUrl(), new Date().toString(), versionInfo.getCopyright(),
                    versionInfo.getVersion(), versionInfo.getDate(), versionInfo.getTime(), null
            };
            for (int i = 0; i < bannerValues.length; i++) {
                if (bannerValues[i] == null) {
                    logger2.info("startup.banner." + i);
                } else {
                    logger2.info("startup.banner." + i, bannerValues[i]);
                }
            }

            logger.info("openejb.home = " + SystemInstance.get().getHome().getDirectory().getAbsolutePath());
            logger.info("openejb.base = " + SystemInstance.get().getBase().getDirectory().getAbsolutePath());

            String className = system.getOptions().get("openejb.assembler", "org.apache.openejb.assembler.classic.Assembler");

            logger.debug("startup.instantiatingAssemblerClass", className);
            
            Assembler assembler;
            try {
                assembler = (Assembler) SafeToolkit.getToolkit("OpenEJB").newInstance(className);
            } catch (OpenEJBException oe) {
                logger.fatal("startup.assemblerCannotBeInstantiated", oe);
                throw oe;
            } catch (Throwable t) {
                String msg = messages.message("startup.openejbEncounteredUnexpectedError");
                logger.fatal(msg, t);
                throw new OpenEJBException(msg, t);
            }

            try {
                assembler.init(system.getProperties());
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

            ContainerSystem containerSystem = assembler.getContainerSystem();
            if (containerSystem == null) {
                String msg = messages.message("startup.assemblerReturnedNullContainer");
                logger.fatal(msg);
                throw new OpenEJBException(msg);
            }

            system.setComponent(ContainerSystem.class, containerSystem);

            if (logger.isDebugEnabled()) {
                logger.debug("startup.debugContainers", containerSystem.containers().length);

                if (containerSystem.containers().length > 0) {
                    Container[] c = containerSystem.containers();
                    logger.debug("startup.debugContainersType");
                    for (int i = 0; i < c.length; i++) {
                        String entry = "   ";
                        switch (c[i].getContainerType()) {
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
                        entry += c[i].getContainerID();
                        logger.debug("startup.debugEntry", entry);
                    }
                }

                logger.debug("startup.debugDeployments", containerSystem.deployments().length);
                if (containerSystem.deployments().length > 0) {
                    logger.debug("startup.debugDeploymentsType");
                    BeanContext[] d = containerSystem.deployments();
                    for (int i = 0; i < d.length; i++) {
                        String entry = "   ";
                        switch (d[i].getComponentType()) {
                            case BMP_ENTITY:
                                entry += "BMP_ENTITY  ";
                                break;
                            case CMP_ENTITY:
                                entry += "CMP_ENTITY  ";
                                break;
                            case STATEFUL:
                                entry += "STATEFUL    ";
                                break;
                            case MANAGED:
                                entry += "MANAGED     ";
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
                        entry += d[i].getDeploymentID();
                        logger.debug("startup.debugEntry", entry);
                    }
                }
            }

            SecurityService securityService = assembler.getSecurityService();
            if (securityService == null) {
                String msg = messages.message("startup.assemblerReturnedNullSecurityService");
                logger.fatal(msg);
                throw new OpenEJBException(msg);
            } else {
                logger.debug("startup.securityService", securityService.getClass().getName());
            }
            system.setComponent(SecurityService.class, securityService);

            TransactionManager transactionManager = assembler.getTransactionManager();
            if (transactionManager == null) {
                String msg = messages.message("startup.assemblerReturnedNullTransactionManager");
                logger.fatal(msg);
                throw new OpenEJBException(msg);
            } else {
                logger.debug("startup.transactionManager", transactionManager.getClass().getName());
            }
            system.setComponent(TransactionManager.class, transactionManager);

            logger.debug("startup.ready");

        }

        public Throwable getInitialized() {
            return initialized;
        }
    }

    public static void destroy() {
        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        if (assembler != null) assembler.destroy();
        SystemInstance.reset();
        instance = null;
    }

    /**
     * 1 usage
     * org.apache.openejb.core.ivm.naming.InitContextFactory
     */
    public static void init(Properties props) throws OpenEJBException {
        init(props, null);
    }

    private static Messages messages = new Messages("org.apache.openejb.util.resources");
    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources");

    /**
     * 2 usages
     */
    public static void init(Properties initProps, ApplicationServer appServer) throws OpenEJBException {
        if (isInitialized()) {
            if (instance != null){
                String msg = messages.message("startup.alreadyInitialized");
                logger.error(msg, instance.initialized);
                throw new OpenEJBException(msg, instance.initialized);
            } else {
                String msg = messages.message("startup.alreadyInitialized");
                logger.error(msg);
                throw new OpenEJBException(msg);
            }
        } else {
            instance = appServer == null ? new Instance(initProps) : new Instance(initProps, appServer);
        }
    }

    /**
     * 1 usages
     */
    public static boolean isInitialized() {
        return instance != null || SystemInstance.get().getComponent(ContainerSystem.class) != null;
    }

    public static class InitializationException extends Exception {
        public InitializationException(String message)
        {
            super(message);
        }
    }
}
