package org.openejb;

import org.openejb.loader.SystemInstance;
import org.openejb.spi.ApplicationServer;
import org.openejb.spi.Assembler;
import org.openejb.spi.ContainerSystem;
import org.openejb.spi.SecurityService;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;

import javax.naming.Context;
import javax.transaction.TransactionManager;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

public final class OpenEJB {

    private static Instance instance;

    public static class Instance {
        private static Messages messages = new Messages("org.openejb.util.resources");

        private final ContainerSystem containerSystem;
        private final SecurityService securityService;
        private final ApplicationServer applicationServer;
        private final TransactionManager transactionManager;
        private final Properties props;
        private final Logger logger;

        /**
         * 1 usage
         * org.openejb.core.ivm.naming.InitContextFactory
         */
        public Instance(Properties props) throws OpenEJBException {
            this(props, null);
        }

        /**
         * 2 usages
         */
        public Instance(Properties initProps, ApplicationServer appServer) throws OpenEJBException {
            try {
                SystemInstance.init(initProps);
            } catch (Exception e) {
                throw new OpenEJBException(e);
            }

            JarUtils.setHandlerSystemProperty();

            Logger.initialize(initProps);

            logger = Logger.getInstance("OpenEJB.startup", "org.openejb.util.resources");

            /*
           * Output startup message
           */
            Properties versionInfo = new Properties();

            try {
                versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            } catch (java.io.IOException e) {
            }
            if (initProps.getProperty("openejb.nobanner") == null) {
                System.out.println("OpenEJB " + versionInfo.get("version") + "    build: " + versionInfo.get("date") + "-" + versionInfo.get("time"));
                System.out.println("" + versionInfo.get("url"));
            }

            logger.i18n.info("startup.banner", versionInfo.get("url"), new Date(), versionInfo.get("copyright"),
                    versionInfo.get("version"), versionInfo.get("date"), versionInfo.get("time"));

            logger.info("openejb.home = " + SystemInstance.get().getHome().getDirectory().getAbsolutePath());
            logger.info("openejb.base = " + SystemInstance.get().getBase().getDirectory().getAbsolutePath());

            /* DMB: This is causing bug 725781.
             * I can't even remember why we decided to add a default security manager.
             * the testsuite runs fine without it, so out it goes for now.
            SecurityManager sm = System.getSecurityManager();
            if (sm == null) {
                try{
                    logger.i18n.debug( "startup.noSecurityManagerInstalled" );
                    System.setSecurityManager(new SecurityManager(){
                        public void checkPermission(Permission perm) {}
                        public void checkPermission(Permission perm, Object context) {}

                    });
                } catch (Exception e){
                    logger.i18n.warning( "startup.couldNotInstalllDefaultSecurityManager", e.getClass().getName(), e.getMessage() );
                }
            }
            */

            props = new Properties(System.getProperties());

            if (initProps == null) {
                logger.i18n.debug("startup.noInitializationProperties");
            } else {
                props.putAll(initProps);
            }

            if (appServer == null) logger.i18n.warning("startup.noApplicationServerSpecified");
            applicationServer = appServer;

            SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB");

            /* Uses the EnvProps.ASSEMBLER property to obtain the Assembler impl.
               Default is org.openejb.alt.assembler.classic.Assembler */
            String className = props.getProperty(EnvProps.ASSEMBLER);
            if (className == null) {
                className = props.getProperty("openejb.assembler", "org.openejb.alt.assembler.classic.Assembler");
            } else {
                logger.i18n.warning("startup.deprecatedPropertyName", EnvProps.ASSEMBLER);
            }

            logger.i18n.debug("startup.instantiatingAssemberClass", className);
            Assembler assembler = null;

            try {
                assembler = (Assembler) toolkit.newInstance(className);
            } catch (OpenEJBException oe) {
                logger.i18n.fatal("startup.assemblerCannotBeInstanitated", oe);
                throw oe;
            } catch (Throwable t) {
                String msg = messages.message("startup.openEjbEncounterUnexpectedError");
                logger.i18n.fatal(msg, t);
                throw new OpenEJBException(msg, t);
            }

            try {
                assembler.init(props);
            } catch (OpenEJBException oe) {
                logger.i18n.fatal("startup.assemblerFailedToInitialize", oe);
                throw oe;
            } catch (Throwable t) {
                String msg = messages.message("startup.assemblerEncounterUnexpectedError");
                logger.i18n.fatal(msg, t);
                throw new OpenEJBException(msg, t);
            }

            try {
                assembler.build();
            } catch (OpenEJBException oe) {
                logger.i18n.fatal("startup.assemblerFailedToBuild", oe);
                throw oe;
            } catch (Throwable t) {
                String msg = messages.message("startup.assemblerEncounterUnexpectedBuildError");
                logger.i18n.fatal(msg, t);
                throw new OpenEJBException(msg, t);
            }

            containerSystem = assembler.getContainerSystem();
            if (containerSystem == null) {
                String msg = messages.message("startup.assemblerReturnedNullContainer");
                logger.i18n.fatal(msg);
                throw new OpenEJBException(msg);
            }

            if (logger.isDebugEnabled()) {
                logger.i18n.debug("startup.debugContainers", new Integer(containerSystem.containers().length));

                if (containerSystem.containers().length > 0) {
                    Container[] c = containerSystem.containers();
                    logger.i18n.debug("startup.debugContainersType");
                    for (int i = 0; i < c.length; i++) {
                        String entry = "   ";
                        switch (c[i].getContainerType()) {
                            case Container.ENTITY:
                                entry += "ENTITY      ";
                                break;
                            case Container.STATEFUL:
                                entry += "STATEFUL    ";
                                break;
                            case Container.STATELESS:
                                entry += "STATELESS   ";
                                break;
                        }
                        entry += c[i].getContainerID();
                        logger.i18n.debug("startup.debugEntry", entry);
                    }
                }

                logger.i18n.debug("startup.debugDeployments", new Integer(containerSystem.deployments().length));
                if (containerSystem.deployments().length > 0) {
                    logger.i18n.debug("startup.debugDeploymentsType");
                    DeploymentInfo[] d = containerSystem.deployments();
                    for (int i = 0; i < d.length; i++) {
                        String entry = "   ";
                        switch (d[i].getComponentType()) {
                            case DeploymentInfo.BMP_ENTITY:
                                entry += "BMP_ENTITY  ";
                                break;
                            case DeploymentInfo.CMP_ENTITY:
                                entry += "CMP_ENTITY  ";
                                break;
                            case DeploymentInfo.STATEFUL:
                                entry += "STATEFUL    ";
                                break;
                            case DeploymentInfo.STATELESS:
                                entry += "STATELESS   ";
                                break;
                        }
                        entry += d[i].getDeploymentID();
                        logger.i18n.debug("startup.debugEntry", entry);
                    }
                }
            }

            securityService = assembler.getSecurityService();
            if (securityService == null) {
                String msg = messages.message("startup.assemblerReturnedNullSecurityService");
                logger.i18n.fatal(msg);
                throw new OpenEJBException(msg);
            } else {
                logger.i18n.debug("startup.securityService", securityService.getClass().getName());
            }

            transactionManager = assembler.getTransactionManager();
            if (transactionManager == null) {
                String msg = messages.message("startup.assemblerReturnedNullTransactionManager");
                logger.i18n.fatal(msg);
                throw new OpenEJBException(msg);
            } else {
                logger.i18n.debug("startup.transactionManager", transactionManager.getClass().getName());
            }

            logger.i18n.info("startup.ready");

            String loader = initProps.getProperty("openejb.loader"), nobanner = initProps.getProperty("openejb.nobanner");
            if (nobanner == null && (loader == null || (loader != null && loader.startsWith("tomcat")))) {
                System.out.println(messages.message("startup.ready"));
            }
        }

        /**
         * 26 usages
         */
        public TransactionManager getTransactionManager() {
            return transactionManager;
        }

        /**
         * 9 usages
         */
        public SecurityService getSecurityService() {
            return securityService;
        }

        /**
         * 5 usages
         * all in org.openejb.core.ivm
         */
        public ApplicationServer getApplicationServer() {
            return applicationServer;
        }

        /**
         * 1 usage
         * org.openejb.core.ivm.BaseEjbProxyHandler
         */
        public DeploymentInfo getDeploymentInfo(Object id) {
            return containerSystem.getDeploymentInfo(id);
        }

        /**
         * 2 usages
         * org.openejb.server.ejbd.DeploymentIndex
         * org.openejb.server.telnet.Ls
         */
        public DeploymentInfo [] deployments() {
            return containerSystem.deployments();
        }

        /**
         * 1 usages
         * org.openejb.server.telnet.Ls
         */
        public Container [] containers() {
            if (containerSystem == null) {// Something went wrong in the configuration.
                logger.i18n.warning("startup.noContainersConfigured");
                return null;
            } else {
                return containerSystem.containers();
            }
        }

        /**
         * 7 usages
         * org.openejb.core.ivm.naming  (3)
         * org.openejb.core.ivm.naming.java (2)
         * org.openejb.core.server.ejbd.JndiRequestHandler (1)
         * org.openejb.core.server.telnet.Lookup (1)
         */
        public javax.naming.Context getJNDIContext() {
            return containerSystem.getJNDIContext();
        }
    }

    public static void destroy() {
        instance = null;
    }

    /**
     * 1 usage
     * org.openejb.core.ivm.naming.InitContextFactory
     */
    public static void init(Properties props) throws OpenEJBException {
        init(props, null);
    }

    private static Messages messages = new Messages("org.openejb.util.resources");
    private static Logger logger = Logger.getInstance("OpenEJB.startup", "org.openejb.util.resources");

    /**
     * 2 usages
     */
    public static void init(Properties initProps, ApplicationServer appServer) throws OpenEJBException {
        if (instance != null) {

            String msg = messages.message("startup.alreadyInitialzied");
            logger.i18n.error(msg);
            throw new OpenEJBException(msg);
        } else {
            instance = new Instance(initProps, appServer);
        }
    }

    public static SecurityService getSecurityService() {
        return instance.getSecurityService();
    }

    public static ApplicationServer getApplicationServer() {
        return instance.getApplicationServer();
    }

    public static DeploymentInfo getDeploymentInfo(Object id) {
        return instance.getDeploymentInfo(id);
    }

    public static DeploymentInfo[] deployments() {
        return instance.deployments();
    }

    public static Container[] containers() {
        return instance.containers();
    }

    public static Context getJNDIContext() {
        return instance.getJNDIContext();
    }

    /**
     * 1 usages
     */
    public static boolean isInitialized() {
        return instance != null;
    }
}
