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
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.alt.config.ejb.EjbDeployment;
import org.apache.openejb.alt.config.sys.ConnectionManager;
import org.apache.openejb.alt.config.sys.Connector;
import org.apache.openejb.alt.config.sys.Container;
import org.apache.openejb.alt.config.sys.JndiProvider;
import org.apache.openejb.alt.config.sys.Openejb;
import org.apache.openejb.alt.config.sys.ProxyFactory;
import org.apache.openejb.alt.config.sys.SecurityService;
import org.apache.openejb.alt.config.sys.ServiceProvider;
import org.apache.openejb.alt.config.sys.TransactionService;
import org.apache.openejb.alt.config.sys.Deployments;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EntityBeanInfo;
import org.apache.openejb.assembler.classic.EntityContainerInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.IntraVmServerInfo;
import org.apache.openejb.assembler.classic.JndiContextInfo;
import org.apache.openejb.assembler.classic.ManagedConnectionFactoryInfo;
import org.apache.openejb.assembler.classic.MethodPermissionInfo;
import org.apache.openejb.assembler.classic.MethodTransactionInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.RoleMappingInfo;
import org.apache.openejb.assembler.classic.SecurityRoleInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class ConfigurationFactory implements OpenEjbConfigurationFactory, ProviderDefaults {

    public static final Logger logger = Logger.getInstance("OpenEJB.startup", "org.apache.openejb.alt.config.rules");
    protected static final Messages messages = new Messages("org.apache.openejb.alt.config.rules");

    private String configLocation = "";

    private List<String> containerIds = new ArrayList();
    private List<String> connectorIds = new ArrayList();
    private List<String> jndiProviderIds = new ArrayList();

    public static OpenEjbConfiguration sys;

    private ContainerInfo[] containers;
    private EntityContainerInfo[] entityContainers;
    private StatefulSessionContainerInfo[] sfsbContainers;
    private StatelessSessionContainerInfo[] slsbContainers;

    private HashMap containerTable = new HashMap();

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


        List<Deployments> deployments = new ArrayList(Arrays.asList(openejb.getDeployments()));

        //// getOption /////////////////////////////////  BEGIN  ////////////////////
        String flag = props.getProperty("openejb.deployments.classpath", "false").toLowerCase();
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

        sys.containerSystem.containers = containers;
        sys.containerSystem.entityContainers = entityContainers;
        sys.containerSystem.statefulContainers = sfsbContainers;
        sys.containerSystem.statelessContainers = slsbContainers;

        ArrayList ejbs = new ArrayList();
        ArrayList ejbJars = new ArrayList();
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
                ejbJars.add(ejbJarInfo);
                ejbs.addAll(Arrays.asList(ejbJarInfo.enterpriseBeans));
            } catch (Exception e) {
                e.printStackTrace();
                ConfigUtils.logger.i18n.warning("conf.0004", ejbModule.getJarURI(), e.getMessage());
            }
        }
        sys.containerSystem.enterpriseBeans = (EnterpriseBeanInfo[]) ejbs.toArray(new EnterpriseBeanInfo[]{});
        sys.containerSystem.ejbJars = (EjbJarInfo[]) ejbJars.toArray(new EjbJarInfo[]{});

        SecurityRoleInfo defaultRole = new SecurityRoleInfo();
        defaultRole.description = "The role applied to recurity references that are not linked.";
        defaultRole.roleName = EjbJarInfoBuilder.DEFAULT_SECURITY_ROLE;
        ejbJarInfoBuilder.getSecurityRoleInfos().add(defaultRole);

        sys.containerSystem.securityRoles = new SecurityRoleInfo[ejbJarInfoBuilder.getSecurityRoleInfos().size()];
        sys.containerSystem.methodPermissions = new MethodPermissionInfo[ejbJarInfoBuilder.getMethodPermissionInfos().size()];
        sys.containerSystem.methodTransactions = new MethodTransactionInfo[ejbJarInfoBuilder.getMethodTransactionInfos().size()];

        sys.containerSystem.securityRoles = ejbJarInfoBuilder.getSecurityRoleInfos().toArray(new SecurityRoleInfo[]{});
        sys.containerSystem.methodPermissions = ejbJarInfoBuilder.getMethodPermissionInfos().toArray(new MethodPermissionInfo[]{});
        sys.containerSystem.methodTransactions = ejbJarInfoBuilder.getMethodTransactionInfos().toArray(new MethodTransactionInfo[]{});

        initSecutityService(openejb, sys.facilities);

        SystemInstance.get().setComponent(OpenEjbConfiguration.class, sys);
        return sys;
    }


    private void initJndiProviders(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
        JndiProvider[] provider = openejb.getJndiProvider();

        if (provider == null || provider.length < 1) return;

        JndiContextInfo[] ctxInfo = new JndiContextInfo[provider.length];
        facilities.remoteJndiContexts = ctxInfo;

        for (int i = 0; i < provider.length; i++) {
            provider[i] = (JndiProvider) initService(provider[i], null);
            ServiceProvider service = ServiceUtils.getServiceProvider(provider[i]);
            checkType(service, provider[i], "JndiProvider");

            ctxInfo[i] = new JndiContextInfo();

            ctxInfo[i].jndiContextId = provider[i].getId();

            if (jndiProviderIds.contains(provider[i].getId())) {
                handleException("conf.0103", configLocation, provider[i].getId());
            }

            jndiProviderIds.add(provider[i].getId());

            ctxInfo[i].properties = ServiceUtils.assemblePropertiesFor("JndiProvider",
                    provider[i].getId(),
                    provider[i].getContent(),
                    configLocation,
                    service);
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
        SecurityRoleInfo[] roles = sys.containerSystem.securityRoles;
        RoleMappingInfo[] r = new RoleMappingInfo[roles.length];
        ssi.roleMappings = r;

        for (int i = 0; i < r.length; i++) {
            r[i] = new RoleMappingInfo();
            r[i].logicalRoleNames = new String[]{roles[i].roleName};
            r[i].physicalRoleNames = new String[]{roles[i].roleName};
        }

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

        ConnectorInfo[] info = new ConnectorInfo[conn.length];
        facilities.connectors = info;

        for (int i = 0; i < conn.length; i++) {

            conn[i] = (Connector) initService(conn[i], DEFAULT_JDBC_DATABASE, Connector.class);
            ServiceProvider service = ServiceUtils.getServiceProvider(conn[i]);
            checkType(service, conn[i], "Connector");

            ManagedConnectionFactoryInfo factory = new ManagedConnectionFactoryInfo();

            info[i] = new ConnectorInfo();
            info[i].connectorId = conn[i].getId();
            info[i].connectionManagerId = DEFAULT_LOCAL_TX_CON_MANAGER;
            info[i].managedConnectionFactory = factory;

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

        facilities.connectionManagers = new ConnectionManagerInfo[]{manager};
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
        Vector e = new Vector();
        Vector sf = new Vector();
        Vector sl = new Vector();

        Container[] containers = conf.getContainer();

        for (int i = 0; i < containers.length; i++) {

            Container c = containers[i];
            ContainerInfo ci = null;

            if (c.getCtype().equals("STATELESS")) {
                c = (Container) initService(c, DEFAULT_STATELESS_CONTAINER);
                ci = new StatelessSessionContainerInfo();
                sl.add(ci);
            } else if (c.getCtype().equals("STATEFUL")) {
                c = (Container) initService(c, DEFAULT_STATEFUL_CONTAINER);
                ci = new StatefulSessionContainerInfo();
                sf.add(ci);
            } else if (c.getCtype().equals("BMP_ENTITY")) {
                c = (Container) initService(c, DEFAULT_BMP_CONTAINER);
                ci = new EntityContainerInfo();
                e.add(ci);
            } else if (c.getCtype().equals("CMP_ENTITY")) {
                c = (Container) initService(c, DEFAULT_CMP_CONTAINER);
                ci = new EntityContainerInfo();
                e.add(ci);
            } else {
                throw new OpenEJBException("Unrecognized contianer type " + c.getCtype());
            }

            ServiceProvider service = ServiceUtils.getServiceProvider(c);
            checkType(service, c, "Container");

            ci.ejbeans = new EnterpriseBeanInfo[0];
            ci.containerName = c.getId();
            ci.className = service.getClassName();
            ci.codebase = c.getJar();
            ci.properties = ServiceUtils.assemblePropertiesFor("Container",
                    c.getId(),
                    c.getContent(),
                    configLocation,
                    service);
            ci.constructorArgs = parseConstructorArgs(service);
            if (containerIds.contains(c.getId())) {
                handleException("conf.0101", configLocation, c.getId());
            }

            containerIds.add(c.getId());

        }

        entityContainers = new EntityContainerInfo[e.size()];
        e.copyInto(entityContainers);

        sfsbContainers = new StatefulSessionContainerInfo[sf.size()];
        sf.copyInto(sfsbContainers);

        slsbContainers = new StatelessSessionContainerInfo[sl.size()];
        sl.copyInto(slsbContainers);

        e.addAll(sf);
        e.addAll(sl);
        this.containers = new ContainerInfo[e.size()];
        e.copyInto(this.containers);

        for (int i = 0; i < this.containers.length; i++) {
            containerTable.put(this.containers[i].containerName, this.containers[i]);
        }

    }

    private String[] parseConstructorArgs(ServiceProvider service) {
        String constructor = service.getConstructor();
        if (constructor == null) {
            return null;
        }
        return constructor.split("[ ,]+");
    }

    private void assignBeansToContainers(EnterpriseBeanInfo[] beans, Map ejbds)
            throws OpenEJBException {

        for (int i = 0; i < beans.length; i++) {

            EjbDeployment d = (EjbDeployment) ejbds.get(beans[i].ejbName);

            ContainerInfo cInfo = (ContainerInfo) containerTable.get(d.getContainerId());
            if (cInfo == null) {

                String msg = messages.format("config.noContainerFound", d.getContainerId(), d.getEjbName());

                logger.fatal(msg);
                throw new OpenEJBException(msg);
            }

            EnterpriseBeanInfo[] oldList = cInfo.ejbeans;
            EnterpriseBeanInfo[] newList = new EnterpriseBeanInfo[oldList.length + 1];
            System.arraycopy(oldList, 0, newList, 1, oldList.length);
            newList[0] = beans[i];
            cInfo.ejbeans = newList;
        }

        for (int i = 0; i < entityContainers.length; i++) {
            EnterpriseBeanInfo[] b = entityContainers[i].ejbeans;
            EntityBeanInfo[] eb = new EntityBeanInfo[b.length];
            System.arraycopy(b, 0, eb, 0, b.length);
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

