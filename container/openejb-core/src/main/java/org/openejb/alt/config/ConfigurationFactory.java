package org.openejb.alt.config;

import org.openejb.OpenEJBException;
import org.openejb.alt.config.ejb.EjbDeployment;
import org.openejb.alt.config.ejb.OpenejbJar;
import org.openejb.alt.config.ejb.Query;
import org.openejb.alt.config.ejb.QueryMethod;
import org.openejb.alt.config.ejb.ResourceLink;
import org.openejb.alt.config.sys.ConnectionManager;
import org.openejb.alt.config.sys.Connector;
import org.openejb.alt.config.sys.Container;
import org.openejb.alt.config.sys.Deployments;
import org.openejb.alt.config.sys.JndiProvider;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.alt.config.sys.ProxyFactory;
import org.openejb.alt.config.sys.SecurityService;
import org.openejb.alt.config.sys.ServiceProvider;
import org.openejb.alt.config.sys.ServicesJar;
import org.openejb.alt.config.sys.TransactionService;
import org.openejb.assembler.classic.ConnectionManagerInfo;
import org.openejb.assembler.classic.ConnectorInfo;
import org.openejb.assembler.classic.ContainerInfo;
import org.openejb.assembler.classic.ContainerSystemInfo;
import org.openejb.assembler.classic.EjbJarInfo;
import org.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.openejb.assembler.classic.EjbReferenceInfo;
import org.openejb.assembler.classic.EjbReferenceLocationInfo;
import org.openejb.assembler.classic.EnterpriseBeanInfo;
import org.openejb.assembler.classic.EntityBeanInfo;
import org.openejb.assembler.classic.EntityContainerInfo;
import org.openejb.assembler.classic.EnvEntryInfo;
import org.openejb.assembler.classic.FacilitiesInfo;
import org.openejb.assembler.classic.IntraVmServerInfo;
import org.openejb.assembler.classic.JndiContextInfo;
import org.openejb.assembler.classic.JndiEncInfo;
import org.openejb.assembler.classic.ManagedConnectionFactoryInfo;
import org.openejb.assembler.classic.MethodInfo;
import org.openejb.assembler.classic.MethodPermissionInfo;
import org.openejb.assembler.classic.MethodTransactionInfo;
import org.openejb.assembler.classic.OpenEjbConfiguration;
import org.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.openejb.assembler.classic.QueryInfo;
import org.openejb.assembler.classic.ResourceReferenceInfo;
import org.openejb.assembler.classic.RoleMappingInfo;
import org.openejb.assembler.classic.SecurityRoleInfo;
import org.openejb.assembler.classic.SecurityRoleReferenceInfo;
import org.openejb.assembler.classic.SecurityServiceInfo;
import org.openejb.assembler.classic.StatefulBeanInfo;
import org.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.openejb.assembler.classic.StatelessBeanInfo;
import org.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.openejb.assembler.classic.TransactionServiceInfo;
import org.openejb.jee.CmpField;
import org.openejb.jee.ContainerTransaction;
import org.openejb.jee.EjbJar;
import org.openejb.jee.EjbRef;
import org.openejb.jee.EnterpriseBean;
import org.openejb.jee.EntityBean;
import org.openejb.jee.EnvEntry;
import org.openejb.jee.Icon;
import org.openejb.jee.Method;
import org.openejb.jee.MethodParams;
import org.openejb.jee.MethodPermission;
import org.openejb.jee.RemoteBean;
import org.openejb.jee.ResourceRef;
import org.openejb.jee.SecurityRole;
import org.openejb.jee.SecurityRoleRef;
import org.openejb.jee.SessionBean;
import org.openejb.jee.SessionType;
import org.openejb.jee.EjbLocalRef;
import org.openejb.loader.FileUtils;
import org.openejb.loader.SystemInstance;
import org.openejb.util.Logger;
import org.openejb.util.Messages;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class ConfigurationFactory implements OpenEjbConfigurationFactory, ProviderDefaults {

    public static final String DEFAULT_SECURITY_ROLE = "openejb.default.security.role";
    protected static final Logger logger = Logger.getInstance("OpenEJB.startup", "org.openejb.alt.config.rules");
    protected static final Messages messages = new Messages("org.openejb.alt.config.rules");

    private AutoDeployer deployer;
    private Openejb openejb;
    private DeployedJar[] jars;
    private ServicesJar openejbDefaults = null;

    private String configLocation = "";

    private Vector deploymentIds = new Vector();
    private Vector securityRoles = new Vector();
    private Vector containerIds = new Vector();

    private Vector mthdPermInfos = new Vector();
    private Vector mthdTranInfos = new Vector();
    private Vector sRoleInfos = new Vector();

    public static OpenEjbConfiguration sys;

    private ContainerInfo[] containers;
    private EntityContainerInfo[] entityContainers;
    private StatefulSessionContainerInfo[] sfsbContainers;
    private StatelessSessionContainerInfo[] slsbContainers;

    private HashMap containerTable = new HashMap();

    private Properties props;

    public void init(Properties props) throws OpenEJBException {
        this.props = props;

        configLocation = props.getProperty("openejb.conf.file");

        if (configLocation == null) {
            configLocation = props.getProperty("openejb.configuration");
        }

        configLocation = ConfigUtils.searchForConfiguration(configLocation, props);
        this.props.setProperty("openejb.configuration", configLocation);

    }

    public static void main(String[] args) {
        try {
            ConfigurationFactory conf = new ConfigurationFactory();
            conf.configLocation = args[0];
            conf.init(null);
            OpenEjbConfiguration openejb = conf.getOpenEjbConfiguration();

            conf.printConf(openejb);
        } catch (Exception e) {
            System.out.println("[OpenEJB] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {

        openejb = ConfigUtils.readConfig(configLocation);

        deployer = new AutoDeployer(openejb);

        resolveDependencies(openejb);

        jars = loadDeployments(openejb);

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
        for (int i = 0; i < jars.length; i++) {
            try {
                EjbJarInfo ejbJarInfo = initEjbJarInfo(jars[i]);
                if (ejbJarInfo == null) {
                    continue;
                }
                ejbJars.add(ejbJarInfo);
                ejbs.addAll(Arrays.asList(ejbJarInfo.enterpriseBeans));
            } catch (Exception e) {
                e.printStackTrace();
                ConfigUtils.logger.i18n.warning("conf.0004", jars[i].jarURI, e.getMessage());
            }
        }
        sys.containerSystem.enterpriseBeans = (EnterpriseBeanInfo[]) ejbs.toArray(new EnterpriseBeanInfo[]{});
        sys.containerSystem.ejbJars = (EjbJarInfo[]) ejbJars.toArray(new EjbJarInfo[]{});

        SecurityRoleInfo defaultRole = new SecurityRoleInfo();
        defaultRole.description = "The role applied to recurity references that are not linked.";
        defaultRole.roleName = DEFAULT_SECURITY_ROLE;
        sRoleInfos.add(defaultRole);

        sys.containerSystem.securityRoles = new SecurityRoleInfo[sRoleInfos.size()];
        sys.containerSystem.methodPermissions = new MethodPermissionInfo[mthdPermInfos.size()];
        sys.containerSystem.methodTransactions = new MethodTransactionInfo[mthdTranInfos.size()];

        sRoleInfos.copyInto(sys.containerSystem.securityRoles);
        mthdPermInfos.copyInto(sys.containerSystem.methodPermissions);
        mthdTranInfos.copyInto(sys.containerSystem.methodTransactions);

        initSecutityService(openejb, sys.facilities);
        return sys;
    }

    Vector jndiProviderIds = new Vector();

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

    Vector connectorIds = new Vector();

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

    private void initProxyFactory(Openejb openejb, FacilitiesInfo facilities)
            throws OpenEJBException {
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

    private Map<String,EjbDeployment> getDeployments(OpenejbJar j) throws OpenEJBException {
        HashMap<String,EjbDeployment> map = new HashMap(j.getEjbDeploymentCount());
        List<EjbDeployment> ejbDeployments = j.getEjbDeployment();
        for (EjbDeployment d : ejbDeployments) {
            map.put(d.getEjbName(), d);
        }
        return map;
    }

    private EjbJarInfo initEjbJarInfo(DeployedJar jar) throws OpenEJBException {

        int beansDeployed = jar.openejbJar.getEjbDeploymentCount();
        int beansInEjbJar = jar.ejbJar.getEnterpriseBeans().length;

        if (beansInEjbJar != beansDeployed) {
            ConfigUtils.logger.i18n.warning("conf.0008", jar.jarURI, "" + beansInEjbJar, "" + beansDeployed);

            return null;
        }

        Map<String,EjbDeployment> ejbds = getDeployments(jar.openejbJar);
        Map<String,EnterpriseBeanInfo> infos = new HashMap();
        Map<String, EnterpriseBean> items = new HashMap();

        EjbJarInfo ejbJar = new EjbJarInfo();
        ejbJar.jarPath = jar.jarURI;


        List<EnterpriseBeanInfo> beanInfos = new ArrayList<EnterpriseBeanInfo>(ejbds.size());
        for (EnterpriseBean bean : jar.ejbJar.getEnterpriseBeans()) {
            EnterpriseBeanInfo beanInfo;
            if (bean instanceof SessionBean) {
                beanInfo = initSessionBean((SessionBean)bean, ejbds);
            } else {
                beanInfo = initEntityBean((EntityBean)bean, ejbds);
            }
            beanInfos.add(beanInfo);

            if (deploymentIds.contains(beanInfo.ejbDeploymentId)) {
                ConfigUtils.logger.i18n.warning("conf.0100", beanInfo.ejbDeploymentId, jar.jarURI, beanInfo.ejbName);

                return null;
            }

            deploymentIds.add(beanInfo.ejbDeploymentId);

            beanInfo.codebase = jar.jarURI;
            infos.put(beanInfo.ejbName, beanInfo);
            items.put(beanInfo.ejbName, bean);


        }

        EnterpriseBeanInfo[] beans = beanInfos.toArray(new EnterpriseBeanInfo[]{});
        ejbJar.enterpriseBeans = beans;

        initJndiReferences(ejbds, infos, items);

        if (jar.ejbJar.getAssemblyDescriptor() != null) {
            initSecurityRoles(jar, ejbds, infos, items);
            initMethodPermissions(jar, ejbds, infos, items);
            initMethodTransactions(jar, ejbds, infos, items);

            for (int x = 0; x < beans.length; x++) {
                resolveRoleLinks(jar, beans[x], (EnterpriseBean) items.get(beans[x].ejbName));
            }
        }

        assignBeansToContainers(beans, ejbds);

        if (!"tomcat-webapp".equals(SystemInstance.get().getProperty("openejb.loader"))) {
            try {

                File base = SystemInstance.get().getBase().getDirectory();
                File jarFile = new File(jar.jarURI);

                SystemInstance.get().getClassPath().addJarToPath(jarFile.toURL());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ejbJar;
    }

    private void initJndiReferences(Map<String,EjbDeployment> ejbds, Map<String,EnterpriseBeanInfo> infos, Map<String,EnterpriseBean> items) throws OpenEJBException {

        Iterator i = infos.values().iterator();
        while (i.hasNext()) {
            JndiEncInfo jndi = new JndiEncInfo();

            EnterpriseBeanInfo bean = (EnterpriseBeanInfo) i.next();
            EnterpriseBean item = (EnterpriseBean) items.get(bean.ejbName);
            EjbDeployment dep = (EjbDeployment) ejbds.get(bean.ejbName);

            /* Build Environment entries *****************/
            jndi.envEntries = buildEnvEntryInfos(item);

            /* Build Resource References *****************/
            jndi.resourceRefs = buildResourceRefInfos(item, dep);

            jndi.ejbReferences = buildEjbRefInfos(item, dep, infos, bean);

            jndi.ejbLocalReferences = buildEjbLocalRefInfos(item, dep, infos, bean);

            bean.jndiEnc = jndi;

        }

    }

    private EjbLocalReferenceInfo[] buildEjbLocalRefInfos(EnterpriseBean item, EjbDeployment dep, Map<String, EnterpriseBeanInfo> beanInfos, EnterpriseBeanInfo bean) throws OpenEJBException {
        List<EjbLocalReferenceInfo> infos = new ArrayList();
        for (EjbLocalRef ejb : item.getEjbLocalRef()) {
            EjbLocalReferenceInfo info = new EjbLocalReferenceInfo();

            info.homeType = ejb.getLocalHome();
            info.referenceName = ejb.getEjbRefName();
            info.location = new EjbReferenceLocationInfo();

            String ejbLink;
            if (ejb.getEjbLink() == null) {
                ejbLink = null;
            } else {
                ejbLink = ejb.getEjbLink();
            }

            EnterpriseBeanInfo otherBean1 = (EnterpriseBeanInfo) beanInfos.get(ejbLink);
            if (otherBean1 == null) {
                String msg = messages.format("config.noBeanFound", ejb.getEjbRefName(), bean.ejbName);

                logger.fatal(msg);
                throw new OpenEJBException(msg);
            }
            EnterpriseBeanInfo otherBean = otherBean1;
            info.location.ejbDeploymentId = otherBean.ejbDeploymentId;
            infos.add(info);
        }
        EjbLocalReferenceInfo[] ejbReferenceInfos = infos.toArray(new EjbLocalReferenceInfo[]{});
        return ejbReferenceInfos;
    }

    private EjbReferenceInfo[] buildEjbRefInfos(EnterpriseBean item, EjbDeployment dep, Map<String, EnterpriseBeanInfo> beanInfos, EnterpriseBeanInfo bean) throws OpenEJBException {
        List<EjbReferenceInfo> infos = new ArrayList();
        for (EjbRef ejb : item.getEjbRef()) {
            EjbReferenceInfo info = new EjbReferenceInfo();

            info.homeType = ejb.getHome();
            info.referenceName = ejb.getEjbRefName();
            info.location = new EjbReferenceLocationInfo();

            String ejbLink;
            if (ejb.getEjbLink() == null) {
                ejbLink = ((ResourceLink) dep.getResourceLink(ejb.getEjbRefName())).getResId();
            } else {
                ejbLink = ejb.getEjbLink();
            }

            EnterpriseBeanInfo otherBean1 = (EnterpriseBeanInfo) beanInfos.get(ejbLink);
            if (otherBean1 == null) {
                String msg = messages.format("config.noBeanFound", ejb.getEjbRefName(), bean.ejbName);

                logger.fatal(msg);
                throw new OpenEJBException(msg);
            }
            EnterpriseBeanInfo otherBean = otherBean1;
            info.location.ejbDeploymentId = otherBean.ejbDeploymentId;
            infos.add(info);
        }
        EjbReferenceInfo[] ejbReferenceInfos = infos.toArray(new EjbReferenceInfo[]{});
        return ejbReferenceInfos;
    }

    private ResourceReferenceInfo[] buildResourceRefInfos(EnterpriseBean item, EjbDeployment dep) {
        List<ResourceReferenceInfo> infos = new ArrayList();
        for (ResourceRef res : item.getResourceRef()) {
            ResourceReferenceInfo info = new ResourceReferenceInfo();

            info.referenceAuth = res.getResAuth().toString();
            info.referenceName = res.getResRefName();
            info.referenceType = res.getResType();
            info.resourceID = dep.getResourceLink(res.getResRefName()).getResId();
            infos.add(info);
        }
        ResourceReferenceInfo[] resourceReferenceInfos = infos.toArray(new ResourceReferenceInfo[]{});
        return resourceReferenceInfos;
    }

    private EnvEntryInfo[] buildEnvEntryInfos(EnterpriseBean item) {
        List<EnvEntryInfo> infos = new ArrayList();
        for (EnvEntry env : item.getEnvEntry()) {
            EnvEntryInfo info = new EnvEntryInfo();

            info.name = env.getEnvEntryName();
            info.type = env.getEnvEntryType();
            info.value = env.getEnvEntryValue();

            infos.add(info);
        }
        EnvEntryInfo[] envEntryInfos = infos.toArray(new EnvEntryInfo[]{});
        return envEntryInfos;
    }

    private void initMethodTransactions(DeployedJar jar, Map ejbds, Map beanInfos, Map items)
            throws OpenEJBException {

        List<ContainerTransaction> containerTransactions = jar.ejbJar.getAssemblyDescriptor().getContainerTransaction();
        List<MethodTransactionInfo> infos = new ArrayList();
        for (ContainerTransaction cTx : containerTransactions) {
            MethodTransactionInfo info = new MethodTransactionInfo();

            info.description = cTx.getDescription();
            info.transAttribute = cTx.getTransAttribute().toString();
            info.methods = getMethodInfos(cTx.getMethod(), ejbds);
            infos.add(info);
        }
        this.mthdTranInfos.addAll(infos);
    }

    private void initSecurityRoles(DeployedJar jar, Map ejbds, Map beaninfos, Map items) throws OpenEJBException {

        List<SecurityRole> roles = jar.ejbJar.getAssemblyDescriptor().getSecurityRole();
        List<SecurityRoleInfo> infos = new ArrayList();

        for (SecurityRole sr : roles) {
            SecurityRoleInfo info = new SecurityRoleInfo();

            info.description = sr.getDescription();
            info.roleName = sr.getRoleName();

            if (securityRoles.contains(sr.getRoleName())) {
                ConfigUtils.logger.i18n.warning("conf.0102", jar.jarURI, sr.getRoleName());
            } else {
                securityRoles.add(sr.getRoleName());
            }
            infos.add(info);
        }
        this.sRoleInfos.addAll(infos);
    }

    private void initMethodPermissions(DeployedJar jar, Map ejbds, Map beanInfos, Map items) throws OpenEJBException {

        List<MethodPermission> methodPermissions = jar.ejbJar.getAssemblyDescriptor().getMethodPermission();
        List<MethodPermissionInfo> infos = new ArrayList();

        for (MethodPermission mp : methodPermissions) {
            MethodPermissionInfo info = new MethodPermissionInfo();

            info.description = mp.getDescription();
            info.roleNames = mp.getRoleName().toArray(new String[]{});
            info.methods = getMethodInfos(mp.getMethod(), ejbds);

            infos.add(info);
        }

        this.mthdPermInfos.addAll(infos);
    }

    private void resolveRoleLinks(DeployedJar jar, EnterpriseBeanInfo bean, EnterpriseBean item)
    throws OpenEJBException {
        if (!(item instanceof RemoteBean)) {
            return;
        }

        RemoteBean rb = (RemoteBean) item;

        List<SecurityRoleRef> refs = rb.getSecurityRoleRef();
        List<SecurityRoleReferenceInfo> infos = new ArrayList();
        for (SecurityRoleRef ref : refs) {
            SecurityRoleReferenceInfo info = new SecurityRoleReferenceInfo();

            info.description = ref.getDescription();
            info.roleLink = ref.getRoleLink();
            info.roleName = ref.getRoleName();

            if (info.roleLink == null) {
                ConfigUtils.logger.i18n.warning("conf.0009", info.roleName, bean.ejbName, jar.jarURI);
                info.roleLink = DEFAULT_SECURITY_ROLE;
            }
            infos.add(info);
        }
        bean.securityRoleReferences = infos.toArray(new SecurityRoleReferenceInfo[]{});
    }

    private MethodInfo[] getMethodInfos(List<Method> ms, Map ejbds) {
        if (ms == null) return null;

        MethodInfo[] mi = new MethodInfo[ms.size()];
        for (int i = 0; i < mi.length; i++) {

            mi[i] = new MethodInfo();

            Method method = ms.get(i);
            EjbDeployment d = (EjbDeployment) ejbds.get(method.getEjbName());

            mi[i].description = method.getDescription();
            mi[i].ejbDeploymentId = d.getDeploymentId();
            mi[i].methodIntf = (method.getMethodIntf() == null)?null: method.getMethodIntf().toString();
            mi[i].methodName = method.getMethodName();

            MethodParams mp = method.getMethodParams();
            if (mp != null) {
                mi[i].methodParams = mp.getMethodParam().toArray(new String[]{});
            }
        }

        return mi;
    }

    private EnterpriseBeanInfo initSessionBean(SessionBean s, Map m) throws OpenEJBException {
        EnterpriseBeanInfo bean = null;

        if (s.getSessionType() == SessionType.STATEFUL) {
            bean = new StatefulBeanInfo();
        } else {
            bean = new StatelessBeanInfo();
        }

        EjbDeployment d = (EjbDeployment) m.get(s.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + s.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();

        Icon icon = s.getIcon();
        bean.largeIcon = (icon ==null)?null: icon.getLargeIcon();
        bean.smallIcon = (icon ==null)?null: icon.getSmallIcon();
        bean.description = s.getDescription();
        bean.displayName = s.getDisplayName();
        bean.ejbClass = s.getEjbClass();
        bean.ejbName = s.getEjbName();
        bean.home = s.getHome();
        bean.remote = s.getRemote();
        bean.localHome = s.getLocalHome();
        bean.local = s.getLocal();
        bean.transactionType = s.getTransactionType().toString();

        return bean;
    }

    private EnterpriseBeanInfo initEntityBean(EntityBean e, Map m) throws OpenEJBException {
        EntityBeanInfo bean = new EntityBeanInfo();

        EjbDeployment d = (EjbDeployment) m.get(e.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + e.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();

        Icon icon = e.getIcon();
        bean.largeIcon = (icon ==null)?null: icon.getLargeIcon();
        bean.smallIcon = (icon ==null)?null: icon.getSmallIcon();
        bean.description = e.getDescription();
        bean.displayName = e.getDisplayName();
        bean.ejbClass = e.getEjbClass();
        bean.ejbName = e.getEjbName();
        bean.home = e.getHome();
        bean.remote = e.getRemote();
        bean.localHome = e.getLocalHome();
        bean.local = e.getLocal();
        bean.transactionType = "Container";

        bean.primKeyClass = e.getPrimKeyClass();
        bean.primKeyField = e.getPrimkeyField();
        bean.persistenceType = e.getPersistenceType().toString();
        bean.reentrant = e.getReentrant() + "";

        List<CmpField> cmpFields = e.getCmpField();
        bean.cmpFieldNames = new String[cmpFields.size()];

        for (int i = 0; i < bean.cmpFieldNames.length; i++) {
            bean.cmpFieldNames[i] = cmpFields.get(i).getFieldName();
        }

        if (bean.persistenceType.equalsIgnoreCase("Container")) {
            List<QueryInfo> qi = new ArrayList<QueryInfo>();
            for (Query q : d.getQuery()) {
                QueryInfo query = new QueryInfo();
                query.description = q.getDescription();
                query.queryStatement = q.getObjectQl().trim();

                MethodInfo method = new MethodInfo();
                QueryMethod qm = q.getQueryMethod();
                method.methodName = qm.getMethodName();
                method.methodParams = qm.getMethodParams().getMethodParam().toArray(new String[]{});
                query.method = method;
                qi.add(query);
            }
            bean.queries = qi.toArray(new QueryInfo[]{});
        }
        return bean;
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

    private void resolveDependencies(Openejb openejb) {
    }

    private void resolveDependencies(EjbJar[] jars) {
    }

    private String[] resolveJarLocations(Deployments[] deploy) {

        FileUtils base = SystemInstance.get().getBase();
        FileUtils home = SystemInstance.get().getHome();

        List jarList = new ArrayList(deploy.length);

        boolean loadFromBoth = getOption("openejb.loadFromBaseAndHome") && !base.equals(home);

        try {
            for (int i = 0; i < deploy.length; i++) {

                Deployments d = deploy[i];

                loadFrom(d, base, jarList);
                if (loadFromBoth) {

                    loadFrom(d, home, jarList);
                }
            }

            boolean searchClassPath = getOption("openejb.deployments.classpath");

            if (searchClassPath) {
                try {
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    Enumeration resources = contextClassLoader.getResources("META-INF/ejb-jar.xml");
                    while (resources.hasMoreElements()) {
                        URL ejbJar = (URL) resources.nextElement();

                        String path = null;
                        Deployments deployment = new Deployments();
                        if (ejbJar.getProtocol().equals("jar")){
                            ejbJar = new URL(ejbJar.getFile().replaceFirst("!.*$", ""));
                            File file = new File(ejbJar.getFile());
                            path = file.getAbsolutePath();
                            deployment.setJar(path);
                        } else if (ejbJar.getProtocol().equals("file")) {
                            File file = new File(ejbJar.getFile());
                            File metainf = file.getParentFile();
                            File ejbPackage = metainf.getParentFile();
                            path = ejbPackage.getAbsolutePath();
                            deployment.setDir(path);
                        } else {
                            logger.warning("Not loading ejbs.  Unknown protocol "+ejbJar.getProtocol());
                            continue;
                        }

                        logger.info("Found ejb in classpath: "+path);
                        loadFrom(deployment, base, jarList);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.warning("Unable to search classpath for ejbs: Received Exception: "+e.getClass().getName()+" "+e.getMessage(),e);
                }

            }
        } catch (SecurityException se) {

        }

        return (String[]) jarList.toArray(new String[]{});

    }

    private boolean getOption(String option) {
        String flag = this.props.getProperty(option, "false").toLowerCase();
        boolean b = flag.equals("true");
        return b;
    }

    private void loadFrom(Deployments d, FileUtils path, List jarList) {

        if (d.getDir() == null && d.getJar() != null) {
            try {
                File jar = path.getFile(d.getJar(), false);
                if (!jarList.contains(jar.getAbsolutePath())) {
                    jarList.add(jar.getAbsolutePath());
                }
            } catch (Exception ignored) {
            }
            return;
        }

        File dir = null;
        try {
            dir = path.getFile(d.getDir(), false);
        } catch (Exception ignored) {
        }

        if (dir == null || !dir.isDirectory()) return;

        File ejbJarXml = new File(dir, "META-INF" + File.separator + "ejb-jar.xml");
        if (ejbJarXml.exists()) {
            if (!jarList.contains(dir.getAbsolutePath())) {
                jarList.add(dir.getAbsolutePath());
            }
            return;
        }

        String[] jarFiles = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (jarFiles == null) {
            return;
        }

        for (int x = 0; x < jarFiles.length; x++) {
            String f = jarFiles[x];
            File jar = new File(dir, f);

            if (jarList.contains(jar.getAbsolutePath())) continue;
            jarList.add(jar.getAbsolutePath());
        }
    }

    private DeployedJar[] loadDeployments(Openejb openejb) throws OpenEJBException {

        EjbValidator validator = new EjbValidator();

        Vector jarsVect = new Vector();

        String[] jarsToLoad = resolveJarLocations(openejb.getDeployments());

        /*[1]  Put all EjbJar & OpenejbJar objects in a vector ***************/
        for (int i = 0; i < jarsToLoad.length; i++) {

            String jarLocation = jarsToLoad[i];
            try {
                EjbJarUtils ejbJarUtils = new EjbJarUtils(jarLocation);
                EjbJar ejbJar = ejbJarUtils.getEjbJar();

                ClassLoader classLoader;

                File jarFile = new File(jarLocation);
                if (jarFile.isDirectory()) {
                    try {
                        URL[] urls = new URL[]{jarFile.toURL()};
                        classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
                    } catch (MalformedURLException e) {
                        throw new OpenEJBException(messages.format("cl0001", jarLocation, e.getMessage()));
                    }
                } else {
                    TempCodebase tempCodebase = new TempCodebase(jarLocation);
                    classLoader = tempCodebase.getClassLoader();
                }

                /* If there is no openejb-jar.xml attempt to auto deploy it.
                 */
                OpenejbJar openejbJar = ejbJarUtils.getOpenejbJar();
                if (openejbJar == null) {
                    openejbJar = deployer.deploy(ejbJarUtils, jarLocation, classLoader);
                }

                EjbSet set = validator.validateJar(ejbJarUtils, classLoader);
                if (set.hasErrors() || set.hasFailures()) {
                    ValidationError[] errors = set.getErrors();
                    for (int j = 0; j < errors.length; j++) {
                        ValidationError e = errors[j];
                        String ejbName = (e.getBean() != null)? e.getBean().getEjbName(): "null";
                        logger.error(e.getPrefix() + " ... " + ejbName + ":\t" + e.getMessage(2));
                    }
                    ValidationFailure[] failures = set.getFailures();
                    for (int j = 0; j < failures.length; j++) {
                        ValidationFailure e = failures[j];
                        logger.info(e.getPrefix() + " ... " + e.getBean().getEjbName() + ":\t" + e.getMessage(2));
                    }

                    throw new OpenEJBException("Jar failed validation.  Use the validation tool for more details");
                }

                /* Add it to the Vector ***************/
                logger.info("Loaded EJBs from " + jarLocation);
                jarsVect.add(new DeployedJar(jarLocation, ejbJar, openejbJar));
            } catch (OpenEJBException e) {
                ConfigUtils.logger.i18n.warning("conf.0004", jarLocation, e.getMessage());
            }
        }

        /*[2]  Get a DeployedJar array from the vector ***************/
        DeployedJar[] jars = new DeployedJar[jarsVect.size()];
        jarsVect.copyInto(jars);
        return jars;
    }

    public Service initService(Service service, String defaultName) throws OpenEJBException {
        return initService(service, defaultName, null);
    }

    public Service initService(Service service, String defaultName, Class type)
            throws OpenEJBException {

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

    String[] tabs = {"", " ", "    ", "      ", "        ", "          "};

    private void printConf(OpenEjbConfiguration conf) {
        out(0, "CONFIGURATION");

        out(1, conf.containerSystem.containers.length);
        for (int i = 0; i < conf.containerSystem.containers.length; i++) {
            out(1, "className    ", conf.containerSystem.containers[i].className);
            out(1, "codebase     ", conf.containerSystem.containers[i].codebase);
            out(1, "containerName", conf.containerSystem.containers[i].containerName);
            out(1, "containerType", conf.containerSystem.containers[i].containerType);
            out(1, "description  ", conf.containerSystem.containers[i].description);
            out(1, "displayName  ", conf.containerSystem.containers[i].displayName);
            out(1, "properties   ");
            conf.containerSystem.containers[i].properties.list(System.out);
            out(1, "ejbeans      ", conf.containerSystem.containers[i].ejbeans.length);
            for (int j = 0; j < conf.containerSystem.containers[i].ejbeans.length; j++) {
                EnterpriseBeanInfo bean = conf.containerSystem.containers[i].ejbeans[j];
                out(2, "codebase       ", bean.codebase);
                out(2, "description    ", bean.description);
                out(2, "displayName    ", bean.displayName);
                out(2, "ejbClass       ", bean.ejbClass);
                out(2, "ejbDeploymentId", bean.ejbDeploymentId);
                out(2, "ejbName        ", bean.ejbName);
                out(2, "home           ", bean.home);
                out(2, "largeIcon      ", bean.largeIcon);
                out(2, "remote         ", bean.remote);
                out(2, "smallIcon      ", bean.smallIcon);
                out(2, "transactionType", bean.transactionType);
                out(2, "type           ", bean.type);
                out(2, "jndiEnc        ", bean.jndiEnc);
                out(2, "envEntries     ", bean.jndiEnc.envEntries.length);
                for (int n = 0; n < bean.jndiEnc.envEntries.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "name  ", bean.jndiEnc.envEntries[n].name);
                    out(3, "type  ", bean.jndiEnc.envEntries[n].type);
                    out(3, "value ", bean.jndiEnc.envEntries[n].value);
                }
                out(2, "ejbReferences  ", bean.jndiEnc.ejbReferences.length);
                for (int n = 0; n < bean.jndiEnc.ejbReferences.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "homeType        ", bean.jndiEnc.ejbReferences[n].homeType);
                    out(3, "referenceName   ", bean.jndiEnc.ejbReferences[n].referenceName);
                    out(3, "location        ", bean.jndiEnc.ejbReferences[n].location);
                    out(3, "ejbDeploymentId ", bean.jndiEnc.ejbReferences[n].location.ejbDeploymentId);
                    out(3, "jndiContextId   ", bean.jndiEnc.ejbReferences[n].location.jndiContextId);
                    out(3, "remote          ", bean.jndiEnc.ejbReferences[n].location.remote);
                    out(3, "remoteRefName   ", bean.jndiEnc.ejbReferences[n].location.remoteRefName);
                }
                out(2, "resourceRefs   ", bean.jndiEnc.resourceRefs.length);
                for (int n = 0; n < bean.jndiEnc.resourceRefs.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "referenceAuth   ", bean.jndiEnc.resourceRefs[n].referenceAuth);
                    out(3, "referenceName   ", bean.jndiEnc.resourceRefs[n].referenceName);
                    out(3, "referenceType   ", bean.jndiEnc.resourceRefs[n].referenceType);
                    if (bean.jndiEnc.resourceRefs[n].location != null) {
                        out(3, "location        ", bean.jndiEnc.resourceRefs[n].location);
                        out(3, "jndiContextId   ", bean.jndiEnc.resourceRefs[n].location.jndiContextId);
                        out(3, "remote          ", bean.jndiEnc.resourceRefs[n].location.remote);
                        out(3, "remoteRefName   ", bean.jndiEnc.resourceRefs[n].location.remoteRefName);
                    }
                }
            }
        }

        if (conf.containerSystem.securityRoles != null) {
            out(0, "--Security Roles------------");
            for (int i = 0; i < sys.containerSystem.securityRoles.length; i++) {
                out(1, "--[" + i + "]----------------------");
                out(1, "            ", sys.containerSystem.securityRoles[i]);
                out(1, "description ", sys.containerSystem.securityRoles[i].description);
                out(1, "roleName    ", sys.containerSystem.securityRoles[i].roleName);
            }
        }

        if (conf.containerSystem.methodPermissions != null) {
            out(0, "--Method Permissions--------");
            for (int i = 0; i < sys.containerSystem.methodPermissions.length; i++) {
                out(1, "--[" + i + "]----------------------");
                out(1, "            ", sys.containerSystem.methodPermissions[i]);
                out(1, "description ", sys.containerSystem.methodPermissions[i].description);
                out(1, "roleNames   ", sys.containerSystem.methodPermissions[i].roleNames);
                if (sys.containerSystem.methodPermissions[i].roleNames != null) {
                    String[] roleNames = sys.containerSystem.methodPermissions[i].roleNames;
                    for (int r = 0; r < roleNames.length; r++) {
                        out(1, "roleName[" + r + "]   ", roleNames[r]);
                    }
                }
                out(1, "methods     ", conf.containerSystem.methodPermissions[i].methods);
                if (conf.containerSystem.methodPermissions[i].methods != null) {
                    MethodInfo[] mthds = conf.containerSystem.methodPermissions[i].methods;
                    for (int j = 0; j < mthds.length; j++) {
                        out(2, "description    ", mthds[j].description);
                        out(2, "ejbDeploymentId", mthds[j].ejbDeploymentId);
                        out(2, "methodIntf     ", mthds[j].methodIntf);
                        out(2, "methodName     ", mthds[j].methodName);
                        out(2, "methodParams   ", mthds[j].methodParams);
                        if (mthds[j].methodParams != null) {
                            for (int n = 0; n < mthds[j].methodParams.length; n++) {
                                out(3, "param[" + n + "]", mthds[j].methodParams[n]);
                            }
                        }

                    }
                }
            }
        }

        if (conf.containerSystem.methodTransactions != null) {
            out(0, "--Method Transactions-------");
            for (int i = 0; i < conf.containerSystem.methodTransactions.length; i++) {

                out(1, "--[" + i + "]----------------------");
                out(1, "               ", conf.containerSystem.methodTransactions[i]);
                out(1, "description    ", conf.containerSystem.methodTransactions[i].description);
                out(1, "transAttribute ", conf.containerSystem.methodTransactions[i].transAttribute);
                out(1, "methods        ", conf.containerSystem.methodTransactions[i].methods);
                if (conf.containerSystem.methodTransactions[i].methods != null) {
                    MethodInfo[] mthds = conf.containerSystem.methodTransactions[i].methods;
                    for (int j = 0; j < mthds.length; j++) {
                        out(2, "description    ", mthds[j].description);
                        out(2, "ejbDeploymentId", mthds[j].ejbDeploymentId);
                        out(2, "methodIntf     ", mthds[j].methodIntf);
                        out(2, "methodName     ", mthds[j].methodName);
                        out(2, "methodParams   ", mthds[j].methodParams);
                        if (mthds[j].methodParams != null) {
                            for (int n = 0; n < mthds[j].methodParams.length; n++) {
                                out(3, "param[" + n + "]", mthds[j].methodParams[n]);
                            }
                        }

                    }
                }
            }
        }
    }

    private void out(int t, String m) {
        System.out.println(tabs[t] + m);
    }

    private void out(int t, String m, String n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private void out(int t, String m, boolean n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private void out(int t, String m, int n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private void out(int t, String m, Object n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private void out(int t, int m) {
        System.out.println(tabs[t] + m);
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

class DeployedJar {

    EjbJar ejbJar;
    OpenejbJar openejbJar;
    String jarURI;

    public DeployedJar(String jar, EjbJar ejbJar, OpenejbJar openejbJar) {
        this.ejbJar = ejbJar;
        this.openejbJar = openejbJar;
        this.jarURI = jar;
    }
}
