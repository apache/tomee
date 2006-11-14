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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import org.apache.openejb.EnvProps;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.ConnectorReference;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.TransactionManagerWrapper;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.OpenEJBErrorHandler;
import org.apache.openejb.util.SafeToolkit;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.StaticRecipe;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;
import javax.naming.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.io.File;

public class Assembler extends AssemblerTool implements org.apache.openejb.spi.Assembler {

    private org.apache.openejb.core.ContainerSystem containerSystem;
    private TransactionManager transactionManager;
    private org.apache.openejb.spi.SecurityService securityService;

    public org.apache.openejb.spi.ContainerSystem getContainerSystem() {
        return containerSystem;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("Assembler");
    protected OpenEjbConfiguration config;

    //==================================
    // Error messages

    private String INVALID_CONNECTION_MANAGER_ERROR = "Invalid connection manager specified for connector identity = ";

    // Error messages
    //==================================


    public Assembler() {
    }

    public void init(Properties props) throws OpenEJBException {
        this.props = props;

        /* Get Configuration ////////////////////////////*/
        String className = props.getProperty(EnvProps.CONFIGURATION_FACTORY);
        if (className == null) className = props.getProperty("openejb.configurator", "org.apache.openejb.alt.config.ConfigurationFactory");

        OpenEjbConfigurationFactory configFactory = (OpenEjbConfigurationFactory) toolkit.newInstance(className);
        configFactory.init(props);
        config = configFactory.getOpenEjbConfiguration();
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* Add IntraVM JNDI service /////////////////////*/
        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String str = systemProperties.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            String naming = "org.apache.openejb.core.ivm.naming";
            if (str == null) {
                str = naming;
            } else if (str.indexOf(naming) == -1) {
                str = naming + ":" + str;
            }
            systemProperties.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
    }

    private static ThreadLocal context = new ThreadLocal();

    public static void setContext(HashMap map) {
        context.set(map);
    }

    public static HashMap getContext() {
        HashMap map = (HashMap) context.get();
        if (map == null){
            map = new HashMap();
            context.set(map);
        }
        return map;
    }

    public void build() throws OpenEJBException {
        setContext(new HashMap());
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
     * Assembles and returns the {@link org.apache.openejb.core.ContainerSystem} using the
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
     * @param configInfo
     * @return ContainerSystem
     * @throws Exception if there was a problem constructing the ContainerSystem.
     * @throws Exception
     * @see OpenEjbConfiguration
     */
    public org.apache.openejb.core.ContainerSystem buildContainerSystem(OpenEjbConfiguration configInfo) throws Exception {

        /*[1] Assemble ProxyFactory //////////////////////////////////////////

            This operation must take place first because of interdependencies.
            As DeploymentInfo objects are registered with the ContainerSystem using the
            ContainerSystem.addDeploymentInfo() method, they are also added to the JNDI
            Naming Service for OpenEJB.  This requires that a proxy for the deployed bean's
            EJBHome be created. The proxy requires that the default proxy factory is set.
        */

        this.applyProxyFactory(configInfo.facilities.intraVmServer);
        /*[1]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/


        ContainerSystemInfo containerSystemInfo = configInfo.containerSystem;


        containerSystem = new org.apache.openejb.core.ContainerSystem();

        createTransactionManager(configInfo);

        createSecurityService(configInfo);

        /*[6] Assemble Connector(s) //////////////////////////////////////////*/
        HashMap connectionManagerMap = new HashMap();
        // connectors are optional in the openejb_config.dtd
        if (configInfo.facilities.connectionManagers != null) {
            for (int i = 0; i < configInfo.facilities.connectionManagers.length; i++) {
                ConnectionManagerInfo cmInfo = configInfo.facilities.connectionManagers[i];
                ConnectionManager connectionManager = assembleConnectionManager(cmInfo);
                connectionManagerMap.put(cmInfo.connectionManagerId, connectionManager);
            }
        }
        // connectors are optional in the openejb_config.dtd
        if (configInfo.facilities.connectors != null) {
            for (int i = 0; i < configInfo.facilities.connectors.length; i++) {
                ConnectorInfo conInfo = configInfo.facilities.connectors[i];

                ConnectionManager connectionManager = (ConnectionManager) connectionManagerMap.get(conInfo.connectionManagerId);
                if (connectionManager == null)
                    throw new RuntimeException(INVALID_CONNECTION_MANAGER_ERROR + conInfo.connectorId);

                ManagedConnectionFactory managedConnectionFactory = assembleManagedConnectionFactory(conInfo.managedConnectionFactory);

                ConnectorReference reference = new ConnectorReference(connectionManager, managedConnectionFactory);

                containerSystem.getJNDIContext().bind("java:openejb/connector/" + conInfo.connectorId, reference);
            }
        }

        JndiBuilder jndiBuilder = new JndiBuilder(containerSystem.getJNDIContext());

        HashMap<String, DeploymentInfo> deployments2 = new HashMap();
        for (AppInfo appInfo : containerSystemInfo.applications) {
            List<URL> jars = new ArrayList();
            for (EjbJarInfo info : appInfo.ejbJars) jars.add(toUrl(info.jarPath));
            for (ClientInfo info : appInfo.clients) jars.add(toUrl(info.codebase));
            for (String jarPath : appInfo.libs) jars.add(toUrl(jarPath));

            ClassLoader classLoader = new URLClassLoader(jars.toArray(new URL[]{}), org.apache.openejb.OpenEJB.class.getClassLoader());

            EjbJarBuilder ejbJarBuilder = new EjbJarBuilder(classLoader);

            for (EjbJarInfo ejbJar : appInfo.ejbJars) {
                deployments2.putAll(ejbJarBuilder.build(ejbJar));
            }

            for (ClientInfo clientInfo : appInfo.clients) {
                JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(clientInfo.jndiEnc);
                Context context = jndiEncBuilder.build();
                containerSystem.getJNDIContext().bind("java:openejb/client/"+clientInfo.moduleId+"/path", clientInfo.codebase);
                containerSystem.getJNDIContext().bind("java:openejb/client/"+clientInfo.moduleId+"/mainClass", clientInfo.mainClass);
                containerSystem.getJNDIContext().bind("java:openejb/client/"+clientInfo.moduleId+"/enc", context);
            }
        }


        ContainersBuilder containersBuilder = new ContainersBuilder(containerSystemInfo, ((AssemblerTool)this).props);
        List containers = (List) containersBuilder.buildContainers(deployments2);
        for (int i1 = 0; i1 < containers.size(); i1++) {
            Container container1 = (Container) containers.get(i1);
            containerSystem.addContainer(container1.getContainerID(), container1);
            org.apache.openejb.DeploymentInfo[] deployments1 = container1.deployments();
            for (int j = 0; j < deployments1.length; j++) {
                CoreDeploymentInfo deployment = (CoreDeploymentInfo) deployments1[j];
                containerSystem.addDeployment(deployment);
                jndiBuilder.bind(deployment);
            }
        }

        // roleMapping used later in buildMethodPermissions
        AssemblerTool.RoleMapping roleMapping = new AssemblerTool.RoleMapping(configInfo.facilities.securityService.roleMappings);
        org.apache.openejb.DeploymentInfo [] deployments = containerSystem.deployments();
        for (int i = 0; i < deployments.length; i++) {
            applyMethodPermissions((org.apache.openejb.core.CoreDeploymentInfo) deployments[i], containerSystemInfo.methodPermissions, roleMapping);
            applyTransactionAttributes((org.apache.openejb.core.CoreDeploymentInfo) deployments[i], containerSystemInfo.methodTransactions);
        }

        List<ContainerInfo> list = new ArrayList();
        if (containerSystemInfo.entityContainers != null) list.addAll(Arrays.asList(containerSystemInfo.entityContainers));
        if (containerSystemInfo.statefulContainers != null) list.addAll(Arrays.asList(containerSystemInfo.statefulContainers));
        if (containerSystemInfo.statelessContainers != null) list.addAll(Arrays.asList(containerSystemInfo.statelessContainers));

        for (ContainerInfo container : list) {
            for (EnterpriseBeanInfo beanInfo : container.ejbeans) {
                CoreDeploymentInfo deployment = (CoreDeploymentInfo) containerSystem.getDeploymentInfo(beanInfo.ejbDeploymentId);
                applySecurityRoleReference(deployment, beanInfo, roleMapping);
            }
        }
        /*[4]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        if (configInfo.facilities.remoteJndiContexts != null) {
            for (JndiContextInfo contextInfo : configInfo.facilities.remoteJndiContexts) {
                javax.naming.InitialContext cntx = assembleRemoteJndiContext(contextInfo);
                containerSystem.getJNDIContext().bind("java:openejb/remote_jndi_contexts/" + contextInfo.jndiContextId, cntx);
            }
        }

        return containerSystem;
    }

    private URL toUrl(String jarPath) throws OpenEJBException {
        try {
            return new File(jarPath).toURL();
        } catch (MalformedURLException e) {
            throw new OpenEJBException(messages.format("cl0001", jarPath, e.getMessage()));
        }
    }

    private void createSecurityService(OpenEjbConfiguration configInfo) throws Exception {
        SecurityServiceInfo service = configInfo.facilities.securityService;
        ObjectRecipe securityServiceRecipe = new ObjectRecipe(service.factoryClassName, service.properties);
        securityService = (SecurityService) securityServiceRecipe.create();

        props.put(SecurityService.class.getName(), securityService);
        containerSystem.getJNDIContext().bind("java:openejb/SecurityService", securityService);
    }

    private void createTransactionManager(OpenEjbConfiguration configInfo) throws Exception {
        TransactionServiceInfo service = configInfo.facilities.transactionService;

        ObjectRecipe txServiceRecipe = new ObjectRecipe(service.factoryClassName, service.properties);
        ObjectRecipe txManagerWrapperRecipe = new ObjectRecipe(TransactionManagerWrapper.class, new String[]{"transactionManager"},new Class[]{TransactionManager.class});

        txManagerWrapperRecipe.setProperty("transactionManager", new StaticRecipe(txServiceRecipe.create()));

        transactionManager = (TransactionManager) txManagerWrapperRecipe.create();

        props.put(TransactionManager.class.getName(), transactionManager);
        getContext().put(TransactionManager.class.getName(), transactionManager);
        SystemInstance.get().setComponent(TransactionManager.class, transactionManager);
        containerSystem.getJNDIContext().bind("java:openejb/TransactionManager", transactionManager);
    }
}
