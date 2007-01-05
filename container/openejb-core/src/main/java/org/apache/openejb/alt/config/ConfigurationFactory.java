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
package org.apache.openejb.alt.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.alt.config.ejb.EjbDeployment;
import org.apache.openejb.alt.config.sys.ConnectionManager;
import org.apache.openejb.alt.config.sys.Connector;
import org.apache.openejb.alt.config.sys.Container;
import org.apache.openejb.alt.config.sys.Deployments;
import org.apache.openejb.alt.config.sys.JndiProvider;
import org.apache.openejb.alt.config.sys.Openejb;
import org.apache.openejb.alt.config.sys.ProxyFactory;
import org.apache.openejb.alt.config.sys.SecurityService;
import org.apache.openejb.alt.config.sys.ServiceProvider;
import org.apache.openejb.alt.config.sys.TransactionService;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EntityContainerInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.IntraVmServerInfo;
import org.apache.openejb.assembler.classic.JndiContextInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.ManagedConnectionFactoryInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.RoleMappingInfo;
import org.apache.openejb.assembler.classic.SecurityRoleInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigurationFactory implements OpenEjbConfigurationFactory, ProviderDefaults {

    public static final Logger logger = Logger.getInstance("OpenEJB.startup", "org.apache.openejb.alt.config.rules");
    protected static final Messages messages = new Messages("org.apache.openejb.alt.config.rules");

    private String configLocation = "";

    private List<String> containerIds = new ArrayList<String>();
    private List<String> connectorIds = new ArrayList<String>();
    private List<String> jndiProviderIds = new ArrayList<String>();

    public static OpenEjbConfiguration sys;

    private final List<ContainerInfo> containers  = new ArrayList<ContainerInfo>();
    private final List<EntityContainerInfo> entityContainers = new ArrayList<EntityContainerInfo>();
    private final List<StatefulSessionContainerInfo> sfsbContainers = new ArrayList<StatefulSessionContainerInfo>();
    private final List<StatelessSessionContainerInfo> slsbContainers = new ArrayList<StatelessSessionContainerInfo>();
    private final List<MdbContainerInfo> mdbContainers = new ArrayList<MdbContainerInfo>();
    private Map<String,ContainerInfo> containerTable = new HashMap<String,ContainerInfo>();

    private Properties props;
    public EjbJarInfoBuilder ejbJarInfoBuilder = new EjbJarInfoBuilder();

    public void init(Properties props) throws OpenEJBException {
        this.props = props;

        configLocation = props.getProperty("openejb.conf.file");

        if (configLocation == null) {
            configLocation = props.getProperty("openejb.configuration");
        }

        configLocation = ConfigUtils.searchForConfiguration(configLocation, props);
        if (configLocation != null){
            this.props.setProperty("openejb.configuration", configLocation);
        }

    }

    public static void main(String[] args) {
        try {
            ConfigurationFactory conf = new ConfigurationFactory();
            conf.configLocation = args[0];
            conf.init(null);
            OpenEjbConfiguration openejb = conf.getOpenEjbConfiguration();

            ConfigurationPrinter.printConf(openejb);
        } catch (Exception e) {
            System.out.println("[OpenEJB] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {

        Openejb openejb;
        DynamicDeployer deployer;
        if (configLocation != null) {
            openejb = ConfigUtils.readConfig(configLocation);
            deployer = new AutoDeployer(openejb);
        } else {
            openejb = new Openejb();
            deployer = new AutoConfigAndDeploy(openejb);
        }


        List<Deployments> deployments = new ArrayList<Deployments>(Arrays.asList(openejb.getDeployments()));

        //// getOption /////////////////////////////////  BEGIN  ////////////////////
        String flag = props.getProperty("openejb.deployments.classpath", "true").toLowerCase();
        boolean searchClassPath = flag.equals("true");
        //// getOption /////////////////////////////////  END  ////////////////////

        if (searchClassPath) {
            Deployments deployment = new Deployments();
            deployment.setClasspath(this.getClass().getClassLoader());
            deployments.add(deployment);
        }

        List<DeploymentModule> deployedJars = new DeploymentLoader().loadDeploymentsList(deployments, deployer);

        DeploymentModule[] jars = deployedJars.toArray(new DeploymentModule[]{});

        sys = new OpenEjbConfiguration();
        sys.containerSystem = new ContainerSystemInfo();
        sys.facilities = new FacilitiesInfo();

        initJndiProviders(openejb, sys.facilities);
        initTransactionService(openejb, sys.facilities);
        initConnectors(openejb, sys.facilities);
        initConnectionManagers(openejb, sys.facilities);
        initProxyFactory(openejb, sys.facilities);

        initContainerInfos(openejb);

        sys.containerSystem.containers.addAll(containers);
        sys.containerSystem.entityContainers.addAll(entityContainers);
        sys.containerSystem.statefulContainers.addAll(sfsbContainers);
        sys.containerSystem.statelessContainers.addAll(slsbContainers);
        sys.containerSystem.mdbContainers.addAll(mdbContainers);
        
        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        {
            AppInfo appInfo = new AppInfo();
            for (DeploymentModule jar : jars) {
                if (!(jar instanceof EjbModule)) {
                    continue;
                }
                EjbModule ejbModule = (EjbModule) jar;
                try {
                    EjbJarInfo ejbJarInfo = ejbJarInfoBuilder.buildInfo(ejbModule);
                    if (ejbJarInfo == null) {
                        continue;
                    }
                    assignBeansToContainers(ejbJarInfo.enterpriseBeans, ejbModule.getOpenejbJar().getDeploymentsByEjbName());
                    appInfo.ejbJars.add(ejbJarInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    ConfigUtils.logger.i18n.warning("conf.0004", ejbModule.getJarURI(), e.getMessage());
                }
            }

            appInfos.add(appInfo);
        }

        for (DeploymentModule module : jars) {
            if (!(module instanceof AppModule)) {
                continue;
            }
            AppModule appModule = (AppModule) module;

            AppInfo appInfo = new AppInfo();
            for (EjbModule ejbModule : appModule.getEjbModules()) {
                try {
                    EjbJarInfo ejbJarInfo = ejbJarInfoBuilder.buildInfo(ejbModule);
                    if (ejbJarInfo == null) {
                        continue;
                    }
                    assignBeansToContainers(ejbJarInfo.enterpriseBeans, ejbModule.getOpenejbJar().getDeploymentsByEjbName());
                    appInfo.ejbJars.add(ejbJarInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    ConfigUtils.logger.i18n.warning("conf.0004", ejbModule.getJarURI(), e.getMessage());
                }
            }

            for (ClientModule clientModule : appModule.getClientModules()) {

                ApplicationClient applicationClient = clientModule.getApplicationClient();
                ClientInfo clientInfo = new ClientInfo();
                clientInfo.description = applicationClient.getDescription();
                clientInfo.displayName = applicationClient.getDisplayName();
                clientInfo.codebase = clientModule.getJarLocation();
                clientInfo.mainClass = clientModule.getMainClass();
                clientInfo.moduleId = getClientModuleId(clientModule);

                JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo.ejbJars);
                JndiEncInfo jndiEncInfo = jndiEncInfoBuilder.build(applicationClient, clientModule.getJarLocation());
                clientInfo.jndiEnc = jndiEncInfo;
                appInfo.clients.add(clientInfo);
            }

            appInfo.jarPath = appModule.getJarLocation();
            List<URL> additionalLibraries = appModule.getAdditionalLibraries();
            for (URL url : additionalLibraries) {
                File file = new File(url.getPath());
                appInfo.libs.add(file.getAbsolutePath());
            }
            appInfos.add(appInfo);
        }

        sys.containerSystem.applications.addAll(appInfos);

//        SecurityRoleInfo defaultRole = new SecurityRoleInfo();
//        defaultRole.description = "The role applied to recurity references that are not linked.";
//        defaultRole.roleName = EjbJarInfoBuilder.DEFAULT_SECURITY_ROLE;
//        ejbJarInfoBuilder.getSecurityRoleInfos().add(defaultRole);

        initSecutityService(openejb, sys.facilities);

        SystemInstance.get().setComponent(OpenEjbConfiguration.class, sys);
        return sys;
    }

    private static String getClientModuleId(ClientModule clientModule) {
        String jarLocation = clientModule.getJarLocation();
        File file = new File(jarLocation);
        String name = file.getName();
        if (name.endsWith(".jar") || name.endsWith(".zip")){
            name = name.replaceFirst("....$","");
        }
        return name;
    }


    private void initJndiProviders(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        JndiProvider[] provider = openejb.getJndiProvider();

        if (provider == null || provider.length < 1) return;

        for (int i = 0; i < provider.length; i++) {
            provider[i] = (JndiProvider) initService(provider[i], null);
            ServiceProvider service = ServiceUtils.getServiceProvider(provider[i]);
            checkType(service, provider[i], "JndiProvider");

            JndiContextInfo jndiContextInfo = new JndiContextInfo();

            jndiContextInfo.jndiContextId = provider[i].getId();

            if (jndiProviderIds.contains(provider[i].getId())) {
                handleException("conf.0103", configLocation, provider[i].getId());
            }

            jndiProviderIds.add(provider[i].getId());

            jndiContextInfo.properties = ServiceUtils.assemblePropertiesFor("JndiProvider",
                    provider[i].getId(),
                    provider[i].getContent(),
                    configLocation,
                    service);

            facilities.remoteJndiContexts.add(jndiContextInfo);
        }
    }

    private void initSecutityService(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        SecurityService ss = openejb.getSecurityService();

        ss = (SecurityService) initService(ss, DEFAULT_SECURITY_SERVICE, SecurityService.class);
        ServiceProvider ssp = ServiceUtils.getServiceProvider(ss);
        checkType(ssp, ss, "Security");

        SecurityServiceInfo ssi = new SecurityServiceInfo();

        ssi.codebase = ss.getJar();
        ssi.description = ssp.getDescription();
        ssi.displayName = ssp.getDisplayName();
        ssi.factoryClassName = ssp.getClassName();
        ssi.serviceName = ss.getId();
        ssi.properties = ServiceUtils.assemblePropertiesFor("Security",
                ss.getId(),
                ss.getContent(),
                configLocation,
                ssp);

// DMB: commented out 1/4/07
//        for (SecurityRoleInfo role : sys.containerSystem.securityRoles) {
//            RoleMappingInfo roleMappingInfo = new RoleMappingInfo();
//            roleMappingInfo.logicalRoleNames.add(role.roleName);
//            roleMappingInfo.physicalRoleNames.add(role.roleName);
//            ssi.roleMappings.add(roleMappingInfo);
//        }

        facilities.securityService = ssi;
    }

    private void initTransactionService(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        TransactionService ts = openejb.getTransactionService();

        ts = (TransactionService) initService(ts,
                DEFAULT_TRANSACTION_MANAGER,
                TransactionService.class);
        ServiceProvider service = ServiceUtils.getServiceProvider(ts);
        checkType(service, ts, "Transaction");

        TransactionServiceInfo tsi = new TransactionServiceInfo();

        tsi.codebase = ts.getJar();
        tsi.description = service.getDescription();
        tsi.displayName = service.getDisplayName();
        tsi.factoryClassName = service.getClassName();
        tsi.serviceName = ts.getId();
        tsi.properties = ServiceUtils.assemblePropertiesFor("Transaction",
                ts.getId(),
                ts.getContent(),
                configLocation,
                service);
        facilities.transactionService = tsi;
    }

    private void initConnectors(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {

        Connector[] conn = openejb.getConnector();

        if (conn == null || conn.length < 1) return;

        for (int i = 0; i < conn.length; i++) {

            conn[i] = (Connector) initService(conn[i], DEFAULT_JDBC_DATABASE, Connector.class);
            ServiceProvider service = ServiceUtils.getServiceProvider(conn[i]);
            checkType(service, conn[i], "Connector");

            ManagedConnectionFactoryInfo factory = new ManagedConnectionFactoryInfo();

            ConnectorInfo connectorInfo = new ConnectorInfo();
            connectorInfo.connectorId = conn[i].getId();
            connectorInfo.connectionManagerId = DEFAULT_LOCAL_TX_CON_MANAGER;
            connectorInfo.managedConnectionFactory = factory;

            factory.id = conn[i].getId();
            factory.className = service.getClassName();
            factory.codebase = conn[i].getJar();
            factory.properties = ServiceUtils.assemblePropertiesFor("Connector",
                    conn[i].getId(),
                    conn[i].getContent(),
                    configLocation,
                    service);

            if (connectorIds.contains(conn[i].getId())) {
                handleException("conf.0103", configLocation, conn[i].getId());
            }

            connectorIds.add(conn[i].getId());
            facilities.connectors.add(connectorInfo);
        }
    }

    private void initConnectionManagers(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {

        ConnectionManagerInfo manager = new ConnectionManagerInfo();
        ConnectionManager cm = openejb.getConnectionManager();

        cm = (ConnectionManager) initService(cm,
                DEFAULT_LOCAL_TX_CON_MANAGER,
                ConnectionManager.class);

        ServiceProvider service = ServiceUtils.getServiceProvider(cm);

        checkType(service, cm, "ConnectionManager");

        manager.connectionManagerId = cm.getId();
        manager.className = service.getClassName();
        manager.codebase = cm.getJar();
        manager.properties = ServiceUtils.assemblePropertiesFor("ConnectionManager",
                cm.getId(),
                cm.getContent(),
                configLocation,
                service);

        facilities.connectionManagers.add(manager);
    }

    private void initProxyFactory(Openejb openejb, FacilitiesInfo facilities) throws OpenEJBException {
        String defaultFactory = null;
        try {
            String version = System.getProperty("java.vm.version");
            if (version.startsWith("1.1") || version.startsWith("1.2")) {
                defaultFactory = "Default JDK 1.2 ProxyFactory";
            } else {
                defaultFactory = "Default JDK 1.3 ProxyFactory";
            }
        } catch (Exception e) {

            throw new RuntimeException("Unable to determine the version of your VM.  No ProxyFactory Can be installed");
        }

        ProxyFactory pf = openejb.getProxyFactory();

        pf = (ProxyFactory) initService(pf, defaultFactory, ProxyFactory.class);
        ServiceProvider pfp = ServiceUtils.getServiceProvider(pf);
        checkType(pfp, pf, "Proxy");

        IntraVmServerInfo pfi = new IntraVmServerInfo();

        facilities.intraVmServer = pfi;
        pfi.proxyFactoryClassName = pfp.getClassName();

        pfi.factoryName = pf.getId();
        pfi.codebase = pf.getJar();
        pfi.properties = ServiceUtils.assemblePropertiesFor("Proxy",
                pf.getId(),
                pf.getContent(),
                configLocation,
                pfp);
    }

    private void initContainerInfos(Openejb conf) throws OpenEJBException {
//        Vector e = new Vector();
//        Vector sf = new Vector();
//        Vector sl = new Vector();

        Container[] containers = conf.getContainer();

        for (Container c : containers) {
            ContainerInfo ci;
            if (c.getCtype().equals("STATELESS")) {
                c = (Container) initService(c, DEFAULT_STATELESS_CONTAINER);
                ci = new StatelessSessionContainerInfo();
                slsbContainers.add((StatelessSessionContainerInfo) ci);
            } else if (c.getCtype().equals("STATEFUL")) {
                c = (Container) initService(c, DEFAULT_STATEFUL_CONTAINER);
                ci = new StatefulSessionContainerInfo();
                sfsbContainers.add((StatefulSessionContainerInfo) ci);
            } else if (c.getCtype().equals("BMP_ENTITY")) {
                c = (Container) initService(c, DEFAULT_BMP_CONTAINER);
                ci = new EntityContainerInfo();
                entityContainers.add((EntityContainerInfo) ci);
            } else if (c.getCtype().equals("CMP_ENTITY")) {
                c = (Container) initService(c, DEFAULT_CMP_CONTAINER);
                ci = new EntityContainerInfo();
                entityContainers.add((EntityContainerInfo) ci);
            } else if (c.getCtype().equals("CMP2_ENTITY")) {
                c = (Container) initService(c, DEFAULT_CMP2_CONTAINER);
                ci = new EntityContainerInfo();
                entityContainers.add((EntityContainerInfo) ci);
            } else if (c.getCtype().equals("MESSAGE")) {
                c = (Container) initService(c, DEFAULT_MDB_CONTAINER);
                ci = new MdbContainerInfo();
                mdbContainers.add((MdbContainerInfo) ci);
            } else {
                throw new OpenEJBException("Unrecognized contianer type " + c.getCtype());
            }

            ServiceProvider service = ServiceUtils.getServiceProvider(c);
            checkType(service, c, "Container");

            ci.containerName = c.getId();
            ci.className = service.getClassName();
            ci.codebase = c.getJar();
            ci.properties = ServiceUtils.assemblePropertiesFor("Container",
                    c.getId(),
                    c.getContent(),
                    configLocation,
                    service);
            ci.constructorArgs.addAll(parseConstructorArgs(service));
            if (containerIds.contains(c.getId())) {
                handleException("conf.0101", configLocation, c.getId());
            }

            containerIds.add(c.getId());

        }

        this.containers.addAll(sfsbContainers);
        this.containers.addAll(slsbContainers);
        this.containers.addAll(entityContainers);
        this.containers.addAll(mdbContainers);

        for (ContainerInfo containerInfo : this.containers) {
            containerTable.put(containerInfo.containerName, containerInfo);
        }

    }

    private List<String> parseConstructorArgs(ServiceProvider service) {
        String constructor = service.getConstructor();
        if (constructor == null) {
            return null;
        }
        return Arrays.asList(constructor.split("[ ,]+"));
    }

    private void assignBeansToContainers(List<EnterpriseBeanInfo> beans, Map ejbds)
            throws OpenEJBException {

        for (EnterpriseBeanInfo bean : beans) {
            EjbDeployment d = (EjbDeployment) ejbds.get(bean.ejbName);

            ContainerInfo cInfo = containerTable.get(d.getContainerId());
            if (cInfo == null) {

                String msg = messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());

                logger.fatal(msg);
                throw new OpenEJBException(msg);
            }
            cInfo.ejbeans.add(bean);
        }
    }

    public Service initService(Service service, String defaultName) throws OpenEJBException {
        return initService(service, defaultName, null);
    }

    public Service initService(Service service, String defaultName, Class type) throws OpenEJBException {

        if (service == null) {
            try {
                service = (Service) type.newInstance();
                service.setProvider(defaultName);
                service.setId(defaultName);
            } catch (Exception e) {
                throw new OpenEJBException("Cannot instantiate class " + type);
            }
        } else if (service.getProvider() == null) {

            try {
                ServiceUtils.getServiceProvider(service.getId());
                service.setProvider(service.getId());
            } catch (Exception e) {

                service.setProvider(defaultName);
            }
        }

        return service;
    }

    private void checkType(ServiceProvider provider, Service service, String type)
            throws OpenEJBException {
        if (!provider.getProviderType().equals(type)) {
            handleException("conf.4902", service, type);
        }
    }


    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException(String errorCode,
                                       Object arg0,
                                       Object arg1,
                                       Object arg2,
                                       Object arg3)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1));
    }

    public static void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0));
    }

    public static void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException(messages.message(errorCode));
    }
}

