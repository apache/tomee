package org.openejb.assembler.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.apache.xbean.spring.context.SpringApplicationContext;
import org.openejb.Container;
import org.openejb.EnvProps;
import org.openejb.OpenEJBException;
import org.openejb.assembler.classic.ContainerInfo;
import org.openejb.assembler.classic.ContainerSystemInfo;
import org.openejb.assembler.classic.OpenEjbConfiguration;
import org.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.openejb.core.DeploymentInfo;
import org.openejb.loader.SystemInstance;
import org.openejb.spi.SecurityService;
import org.openejb.util.OpenEJBErrorHandler;
import org.openejb.util.SafeToolkit;
import org.openejb.util.proxy.ProxyFactory;
import org.openejb.util.proxy.ProxyManager;

public class Assembler extends AssemblerTool implements org.openejb.spi.Assembler {
    private ProxyFactory proxyFactory;
    private org.openejb.core.ContainerSystem containerSystem;
    private TransactionManager transactionManager;
    private SecurityService securityService;

    private final SafeToolkit toolkit = SafeToolkit.getToolkit("Assembler");
    private OpenEjbConfiguration config;

    public Assembler() {
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    private void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
        ProxyManager.registerFactory("ivm_server", this.proxyFactory);
        ProxyManager.setDefaultFactory("ivm_server");
    }

    public org.openejb.spi.ContainerSystem getContainerSystem() {
        return containerSystem;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    private void setSecurityService(SecurityService securityService) throws NamingException {
        this.securityService = securityService;
        props.put(SecurityService.class.getName(), securityService);
        containerSystem.getJNDIContext().bind("java:openejb/SecurityService", securityService);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    private void setTransactionManager(TransactionManager transactionManager) throws Exception {
        this.transactionManager = transactionManager;
        props.put(TransactionManager.class.getName(), transactionManager);
        getContext().put(TransactionManager.class.getName(), transactionManager);
        SystemInstance.get().setComponent(TransactionManager.class, transactionManager);
        containerSystem.getJNDIContext().bind("java:openejb/TransactionManager", transactionManager);
    }

    public void init(Properties props) throws OpenEJBException {
        this.props = props;

        /* Get Configuration ////////////////////////////*/
        String className = props.getProperty(EnvProps.CONFIGURATION_FACTORY);
        if (className == null) {
            className = props.getProperty("openejb.configurator", "org.openejb.alt.config.ConfigurationFactory");
        }

        OpenEjbConfigurationFactory configFactory = (OpenEjbConfigurationFactory) toolkit.newInstance(className);
        configFactory.init(props);
        config = configFactory.getOpenEjbConfiguration();
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* Add IntraVM JNDI service /////////////////////*/
        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String str = systemProperties.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            String naming = "org.openejb.core.ivm.naming";
            if (str == null) {
                str = naming;
            } else if (str.indexOf(naming) == -1) {
                str = naming + ":" + str;
            }
            systemProperties.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
    }

    private static ThreadLocal<HashMap<String, Object>> context = new ThreadLocal<HashMap<String, Object>>();

    public static HashMap<String, Object> getContext() {
        return context.get();
    }

    public void build() throws OpenEJBException {
        context.set(new HashMap<String, Object>());
        try {
            containerSystem = buildContainerSystem(config);
        } catch (OpenEJBException ae) {
            /* OpenEJBExceptions contain useful information and are debbugable.
             * Let the exception pass through to the top and be logged.
             */
            throw ae;
        } catch (Exception e) {
            /* General Exceptions at this level are too generic and difficult to debug.
             * These exceptions are considered unknown bugs and are fatal.
             * If you get an error at this level, please trap and handle the error
             * where it is most relevant.
             */
            OpenEJBErrorHandler.handleUnknownError(e, "Assembler");
            throw new OpenEJBException(e);
        } finally {
            context.set(null);
        }
    }

    /////////////////////////////////////////////////////////////////////
    ////
    ////    Public Methods Used for Assembly
    ////
    /////////////////////////////////////////////////////////////////////

    /**
     * When given a complete OpenEjbConfiguration graph this method,
     * will construct an entire container system and return a reference to that
     * container system, as ContainerSystem instance.
     * <p/>
     * This method leverage the other assemble and apply methods which
     * can be used independently.
     * <p/>
     * Assembles and returns the {@link org.openejb.core.ContainerSystem} using the
     * information from the {@link OpenEjbConfiguration} object passed in.
     * <pre>
     * This method performs the following actions(in order):
     * <p/>
     * 1  Assembles ProxyFactory
     * 2  Assembles Containers and Deployments
     * 3  Assembles SecurityService
     * 4  Apply method permissions, role refs, and tx attributes
     * 5  Assembles TransactionService
     * 6  Assembles ConnectionManager(s)
     * 7  Assembles Connector(s)
     * </pre>
     *
     * @return ContainerSystem
     * @throws Exception if there was a problem constructing the ContainerSystem.
     * @see OpenEjbConfiguration
     */
    public org.openejb.core.ContainerSystem buildContainerSystem(OpenEjbConfiguration configInfo) throws Exception {
        SpringApplicationContext factory = new ClassPathXmlApplicationContext("META-INF/org.openejb/spring.xml");

        /*[1] Assemble ProxyFactory //////////////////////////////////////////

            This operation must take place first because of interdependencies.
            As DeploymentInfo objects are registered with the ContainerSystem using the
            ContainerSystem.addDeploymentInfo() method, they are also added to the JNDI
            Naming Service for OpenEJB.  This requires that a proxy for the deployed bean's
            EJBHome be created. The proxy requires that the default proxy factory is set.
        */

        ProxyFactory proxyFactory = getBean(factory, ProxyFactory.class);
        setProxyFactory(proxyFactory);
        /*[1]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        //
        // Container system
        //
        containerSystem = getBean(factory, org.openejb.core.ContainerSystem.class);
        SystemInstance.get().setComponent(org.openejb.spi.ContainerSystem.class, containerSystem);

        //
        // Transaction manager
        //
        TransactionManager transactionManager = getBean(factory, TransactionManager.class);
        setTransactionManager(transactionManager);

        //
        // Security system
        //
        SecurityService securityService1 = getBean(factory, SecurityService.class);
        setSecurityService(securityService1);

        /*[4] Apply method permissions, role refs, and tx attributes ////////////////////////////////////*/
        ContainerSystemInfo containerSystemInfo = configInfo.containerSystem;
        ContainerBuilder containerBuilder = new ContainerBuilder(containerSystemInfo, ((AssemblerTool) this).props);
        List<Container> containers = (List<Container>) containerBuilder.build();
        for (int i1 = 0; i1 < containers.size(); i1++) {
            Container container1 = containers.get(i1);
            containerSystem.addContainer(container1.getContainerID(), container1);
            org.openejb.DeploymentInfo[] deployments1 = container1.deployments();
            for (int j = 0; j < deployments1.length; j++) {
                containerSystem.addDeployment((DeploymentInfo) deployments1[j]);
            }
        }

        // roleMapping used later in buildMethodPermissions
        AssemblerTool.RoleMapping roleMapping = new AssemblerTool.RoleMapping(configInfo.facilities.securityService.roleMappings);
        org.openejb.DeploymentInfo [] deployments = containerSystem.deployments();
        for (int i = 0; i < deployments.length; i++) {
            applyMethodPermissions((org.openejb.core.DeploymentInfo) deployments[i], containerSystemInfo.methodPermissions, roleMapping);
            applyTransactionAttributes((org.openejb.core.DeploymentInfo) deployments[i], containerSystemInfo.methodTransactions);
        }

        ArrayList<ContainerInfo> list = new ArrayList<ContainerInfo>();
        if (containerSystemInfo.entityContainers != null) {
            list.addAll(Arrays.asList(containerSystemInfo.entityContainers));
        }
        if (containerSystemInfo.statefulContainers != null) {
            list.addAll(Arrays.asList(containerSystemInfo.statefulContainers));
        }
        if (containerSystemInfo.statelessContainers != null) {
            list.addAll(Arrays.asList(containerSystemInfo.statelessContainers));
        }
        Iterator<ContainerInfo> iterator = list.iterator();
        while (iterator.hasNext()) {
            ContainerInfo container = iterator.next();
            for (int z = 0; z < container.ejbeans.length; z++) {
                DeploymentInfo deployment = (org.openejb.core.DeploymentInfo) containerSystem.getDeploymentInfo(container.ejbeans[z].ejbDeploymentId);
                applySecurityRoleReference(deployment, container.ejbeans[z], roleMapping);
            }
        }
        /*[4]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        if (configInfo.facilities.remoteJndiContexts != null) {
            for (int i = 0; i < configInfo.facilities.remoteJndiContexts.length; i++) {
                javax.naming.InitialContext cntx = assembleRemoteJndiContext(configInfo.facilities.remoteJndiContexts[i]);
                containerSystem.getJNDIContext().bind("java:openejb/remote_jndi_contexts/" + configInfo.facilities.remoteJndiContexts[i].jndiContextId, cntx);
            }

        }

        return containerSystem;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getBean(SpringApplicationContext factory, Class<T> type) throws OpenEJBException {
        // get the main service from the configuration file
        String[] names = factory.getBeanNamesForType(type);
        T bean = null;
        if (names.length == 0) {
            throw new OpenEJBException("No bean of type: " + type.getName() + " found in the bootstrap file: " + factory.getDisplayName());
        }
        bean = (T) factory.getBean(names[0]);
        return bean;
    }
}
