package org.openejb;

import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.transaction.TransactionManager;

import org.openejb.spi.ApplicationServer;
import org.openejb.spi.Assembler;
import org.openejb.spi.ContainerSystem;
import org.openejb.spi.SecurityService;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;
import org.openejb.loader.SystemInstance;

public final class OpenEJB {

    private static ContainerSystem containerSystem;
    private static SecurityService securityService;
    private static ApplicationServer applicationServer;
    private static TransactionManager transactionManager;
    private static Properties props;
    private static boolean initialized;
    private static Logger logger;
    private static Messages messages = new Messages("org.openejb.util.resources");

    public static void destroy() {
        // Very un-thread-safe
        containerSystem = null;
        securityService = null;
        applicationServer = null;
        transactionManager = null;
        props = null;
        initialized = false;
        logger = null;
    }
    public static void init(Properties props)
            throws OpenEJBException {
        init(props, null);
    }

    public static void init(Properties initProps, ApplicationServer appServer) throws OpenEJBException {
        try {
            SystemInstance.init(initProps);
        } catch (Exception e) {
            throw new OpenEJBException(e);
        }
        if (initialized) {
            String msg = messages.message("startup.alreadyInitialzied");
            logger.i18n.error(msg);
            throw new OpenEJBException(msg);
        } else {

            JarUtils.setHandlerSystemProperty();

            Logger.initialize(initProps);

            logger = Logger.getInstance("OpenEJB.startup", "org.openejb.util.resources");
        }

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

        initialized = true;

        logger.i18n.info("startup.ready");

        String loader = initProps.getProperty("openejb.loader"), nobanner = initProps.getProperty("openejb.nobanner");
        if (nobanner == null && (loader == null || (loader != null && loader.startsWith("tomcat")))) {
            System.out.println(messages.message("startup.ready"));
        }
    }

    public static TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public static SecurityService getSecurityService() {
        return securityService;
    }

    public static ApplicationServer getApplicationServer() {
        return applicationServer;
    }

    public static DeploymentInfo getDeploymentInfo(Object id) {
        return containerSystem.getDeploymentInfo(id);
    }

    public static DeploymentInfo [] deployments() {
        return containerSystem.deployments();
    }

    public static Container getContainer(Object id) {
        return containerSystem.getContainer(id);
    }

    public static Container [] containers() {
        if (containerSystem == null) {// Something went wrong in the configuration.
            logger.i18n.warning("startup.noContainersConfigured");
            return null;
        } else {
            return containerSystem.containers();
        }
    }

    public static javax.naming.Context getJNDIContext() {
        return containerSystem.getJNDIContext();
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
