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

package org.apache.openejb.config;

import org.apache.openejb.Extensions;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.Vendor;
import org.apache.openejb.api.Proxy;
import org.apache.openejb.api.resource.PropertiesResourceProvider;
import org.apache.openejb.api.resource.Template;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.BmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.CmpEntityContainerInfo;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.DeploymentExceptionManager;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.HandlerChainInfo;
import org.apache.openejb.assembler.classic.HandlerInfo;
import org.apache.openejb.assembler.classic.JndiContextInfo;
import org.apache.openejb.assembler.classic.ManagedContainerInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.component.ClassLoaderEnricher;
import org.apache.openejb.config.sys.AbstractService;
import org.apache.openejb.config.sys.AdditionalDeployments;
import org.apache.openejb.config.sys.ConnectionManager;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.JndiProvider;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.ProxyFactory;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.SecurityService;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.TransactionManager;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.HandlerChain;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.resource.jdbc.DataSourceFactory;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.openejb.resource.jdbc.pool.DefaultDataSourceCreator;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.openejb.util.References;
import org.apache.openejb.util.SuperProperties;
import org.apache.openejb.util.URISupport;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.openejb.util.proxy.QueryProxy;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.MetaAnnotatedClass;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import jakarta.ejb.embeddable.EJBContainer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.apache.openejb.config.DeploymentsResolver.DEPLOYMENTS_CLASSPATH_PROPERTY;
import static org.apache.openejb.config.ServiceUtils.implies;

@SuppressWarnings("UnusedDeclaration")
public class ConfigurationFactory implements OpenEjbConfigurationFactory {

    public static final String OPENEJB_JDBC_DATASOURCE_CREATOR = "openejb.jdbc.datasource-creator";

    public static final String ADDITIONAL_DEPLOYMENTS = "conf/deployments.xml";
    static final String CONFIGURATION_PROPERTY = "openejb.configuration";
    static final String CONF_FILE_PROPERTY = "openejb.conf.file";
    private static final String DEBUGGABLE_VM_HACKERY_PROPERTY = "openejb.debuggable-vm-hackery";
    protected static final String VALIDATION_SKIP_PROPERTY = "openejb.validation.skip";
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, ConfigurationFactory.class);
    private static final Messages messages = new Messages(ConfigurationFactory.class);

    private static final String IGNORE_DEFAULT_VALUES_PROP = "IgnoreDefaultValues";
    private static final boolean WSDL4J_AVAILABLE = exists("javax.wsdl.xml.WSDLLocator");

    private String configLocation;
    private OpenEjbConfiguration sys;
    private Openejb openejb;
    private final DynamicDeployer deployer;
    private final DeploymentLoader deploymentLoader;
    private final boolean offline;
    private final boolean serviceTypeIsAdjustable; // offline is a bit different from this and offline could be off and this on

    private static final String CLASSPATH_AS_EAR = "openejb.deployments.classpath.ear";
    static final String WEBSERVICES_ENABLED = "openejb.webservices.enabled";
    static final String OFFLINE_PROPERTY = "openejb.offline";

    public ConfigurationFactory() {
        this(!shouldAutoDeploy());
    }

    private static boolean exists(final String s) {
        try {
            ConfigurationFactory.class.getClassLoader().loadClass(s);
            return true;
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    private static boolean shouldAutoDeploy() {
        final Options options = SystemInstance.get().getOptions();
        final boolean b = options.get(ConfigurationFactory.OFFLINE_PROPERTY, false);
        return options.get("tomee.autoconfig", !b);
    }

    public ConfigurationFactory(final boolean offline) {
        this(offline, (DynamicDeployer) null);
    }

    public ConfigurationFactory(final boolean offline, final DynamicDeployer preAutoConfigDeployer) {
        JavaSecurityManagers.setSystemProperty("bval.in-container", JavaSecurityManagers.getSystemProperty("bval.in-container","true"));

        this.offline = offline;
        this.serviceTypeIsAdjustable = SystemInstance.get().getOptions().get("openejb.service-type-adjustement", true);
        this.deploymentLoader = new DeploymentLoader();

        LocalMBeanServer.reset();

        final Options options = SystemInstance.get().getOptions();
        if (SystemInstance.get().getComponent(DataSourceCreator.class) == null) {
            final String creator = options.get(OPENEJB_JDBC_DATASOURCE_CREATOR, (String) null);
            if (creator == null) {
                SystemInstance.get().setComponent(DataSourceCreator.class, new DefaultDataSourceCreator());
            } else {
                try {
                    SystemInstance.get().setComponent(DataSourceCreator.class, DataSourceFactory.creator(creator, false));
                } catch (final Exception e) {
                    logger.error("can't load " + creator + " will use the default creator", e);
                    SystemInstance.get().setComponent(DataSourceCreator.class, new DefaultDataSourceCreator());
                }
            }
        }
        if (SystemInstance.get().getComponent(ClassLoaderEnricher.class) == null) {
            SystemInstance.get().setComponent(ClassLoaderEnricher.class, new ClassLoaderEnricher());
        }
        SystemInstance.get().setComponent(ConfigurationFactory.class, this);

        // annotation deployer encapsulate some logic, to be able to push to it some config
        // we give the ability here to get the internal deployer to push the config values
        final AnnotationDeployer annotationDeployer = new AnnotationDeployer();
        final BeanProperties beanProperties = new BeanProperties();
        final AppContextConfigDeployer appContextConfigDeployer = new AppContextConfigDeployer(annotationDeployer.getEnvEntriesPropertiesDeployer(), beanProperties);

        final Chain chain = new Chain();

        chain.add(new SystemPropertiesOverride());

        chain.add(new GeneratedClientModules.Add());

        chain.add(new ApplicationComposerDeployer()); // before read descriptors and moreover AnnotationDeploer

        chain.add(new ReadDescriptors());

        chain.add(appContextConfigDeployer);

        chain.add(new ApplicationProperties());

        chain.add(new ModuleProperties());

        chain.add(new LegacyProcessor());

        chain.add(annotationDeployer);

        chain.add(beanProperties);

        chain.add(new ConfigurationDeployer()); // after annotation deployer and read descriptors are the only constraints

        chain.add(new ProxyBeanClassUpdate());

        chain.add(new GeneratedClientModules.Prune());

        chain.add(new ClearEmptyMappedName());

        //START SNIPPET: code
        if (!options.get(VALIDATION_SKIP_PROPERTY, false)) {
            chain.add(new ValidateModules());
        } else {
            DeploymentLoader.LOGGER.info("validationDisabled", VALIDATION_SKIP_PROPERTY);
        }
        //END SNIPPET: code
        chain.add(new InitEjbDeployments());

        if (options.get(DEBUGGABLE_VM_HACKERY_PROPERTY, false)) {
            chain.add(new DebuggableVmHackery());
        }

        if (options.get(WEBSERVICES_ENABLED, true) && WSDL4J_AVAILABLE) {
            chain.add(new WsDeployer());
        } else {
            chain.add(new RemoveWebServices());
        }

        chain.add(new CmpJpaConversion());

        // By default all vendor support is enabled
        final Set<Vendor> support = SystemInstance.get().getOptions().getAll("openejb.vendor.config", Vendor.values());

        if (support.contains(Vendor.GERONIMO) || SystemInstance.get().hasProperty("openejb.geronimo")) {
            chain.add(new OpenEjb2Conversion());
        }

        if (support.contains(Vendor.GLASSFISH)) {
            chain.add(new SunConversion());
        }

        if (support.contains(Vendor.WEBLOGIC)) {
            chain.add(new WlsConversion());
        }

        if (SystemInstance.get().hasProperty("openejb.geronimo")) {
            // must be after CmpJpaConversion since it adds new persistence-context-refs
            chain.add(new GeronimoMappedName());
        }

        if (null != preAutoConfigDeployer) {
            chain.add(preAutoConfigDeployer);
        }

        chain.add(new ConvertDataSourceDefinitions());
        chain.add(new ConvertJMSConnectionFactoryDefinitions());
        chain.add(new ConvertJMSDestinationDefinitions());
        chain.add(new CleanEnvEntries());
        chain.add(new LinkBuiltInTypes());


        if (offline) {
            final AutoConfig autoConfig = new AutoConfig(this);
            autoConfig.autoCreateResources(false);
            autoConfig.autoCreateContainers(false);
            chain.add(autoConfig);
        } else {
            chain.add(new AutoConfig(this));
        }

        chain.add(new ActivationConfigPropertyOverride());
        chain.add(new ApplyOpenejbJar());
        chain.add(new MappedNameBuilder());
        chain.add(new OutputGeneratedDescriptors());

        //        chain.add(new MergeWebappJndiContext());
        this.deployer = chain;
    }

    public ConfigurationFactory(final boolean offline, final OpenEjbConfiguration configuration) {
        this(offline, (DynamicDeployer) null, configuration);
    }

    public ConfigurationFactory(final boolean offline,
                                final DynamicDeployer preAutoConfigDeployer,
                                final OpenEjbConfiguration configuration) {
        this(offline, preAutoConfigDeployer);
        sys = configuration;
    }

    public ConfigurationFactory(final boolean offline, final Chain deployerChain, final OpenEjbConfiguration configuration) {
        this.offline = offline;
        this.deploymentLoader = new DeploymentLoader();
        this.deployer = deployerChain;
        this.sys = configuration;
        this.serviceTypeIsAdjustable = true;
    }

    public boolean isOffline() {
        return offline;
    }

    public static List<HandlerChainInfo> toHandlerChainInfo(final HandlerChains chains) {
        final List<HandlerChainInfo> handlerChains = new ArrayList<>();
        if (chains == null) {
            return handlerChains;
        }

        for (final HandlerChain handlerChain : chains.getHandlerChain()) {
            final HandlerChainInfo handlerChainInfo = new HandlerChainInfo();
            handlerChainInfo.serviceNamePattern = handlerChain.getServiceNamePattern();
            handlerChainInfo.portNamePattern = handlerChain.getPortNamePattern();
            handlerChainInfo.protocolBindings.addAll(handlerChain.getProtocolBindings());
            for (final Handler handler : handlerChain.getHandler()) {
                final HandlerInfo handlerInfo = new HandlerInfo();
                handlerInfo.handlerName = handler.getHandlerName();
                handlerInfo.handlerClass = handler.getHandlerClass();
                handlerInfo.soapHeaders.addAll(handler.getSoapHeader());
                handlerInfo.soapRoles.addAll(handler.getSoapRole());
                for (final ParamValue param : handler.getInitParam()) {
                    handlerInfo.initParams.setProperty(param.getParamName(), param.getParamValue());
                }
                handlerChainInfo.handlers.add(handlerInfo);
            }
            handlerChains.add(handlerChainInfo);
        }
        return handlerChains;
    }

    public static class ProxyBeanClassUpdate implements DynamicDeployer {

        @SuppressWarnings("unchecked")
        @Override
        public AppModule deploy(final AppModule appModule) throws OpenEJBException {
            for (final EjbModule module : appModule.getEjbModules()) {
                for (final EnterpriseBean eb : module.getEjbJar().getEnterpriseBeans()) {
                    if (!(eb instanceof SessionBean)) {
                        continue;
                    }

                    final SessionBean bean = (SessionBean) eb;
                    final Class<?> ejbClass;
                    try {
                        ejbClass = module.getClassLoader().loadClass(bean.getEjbClass());
                    } catch (final ClassNotFoundException e) {
                        logger.warning("can't load " + bean.getEjbClass());
                        continue;
                    }

                    final Class<?> proxyClass;
                    if (ejbClass.isInterface()) { // dynamic proxy implementation
                        bean.setLocal(ejbClass.getName());
                        final Proxy proxyAnnotation = (Proxy) new MetaAnnotatedClass(ejbClass).getAnnotation(Proxy.class);
                        if (proxyAnnotation != null) {
                            proxyClass = proxyAnnotation.value();
                        } else {
                            proxyClass = QueryProxy.class;
                        }
                        bean.setProxy(proxyClass.getName());
                    } else {
                        continue;
                    }

                    for (final EnvEntry entry : bean.getEnvEntry()) {
                        if ("java:comp/env/implementingInterfaceClass".equals(entry.getName())) {
                            entry.setEnvEntryValue(ejbClass.getName());
                        }
                    }
                }
            }
            return appModule;
        }
    }

    public static class Chain implements DynamicDeployer {

        private final List<DynamicDeployer> chain = new ArrayList<>();

        public boolean add(final DynamicDeployer o) {
            return chain.add(o);
        }

        @Override
        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            for (final DynamicDeployer deployer : chain) {
                appModule = deployer.deploy(appModule);
            }
            return appModule;
        }
    }

    @Override
    public void init(final Properties props) throws OpenEJBException {
        configLocation = props.getProperty(CONF_FILE_PROPERTY);
        if (configLocation == null) {
            configLocation = props.getProperty(CONFIGURATION_PROPERTY);
        }

        configLocation = ConfigUtils.searchForConfiguration(configLocation);

        if (configLocation != null) {
            logger.info("TomEE configuration file is '" + configLocation + "'");
            props.setProperty(CONFIGURATION_PROPERTY, configLocation);
        }

    }

    protected void install(final ContainerInfo serviceInfo) throws OpenEJBException {
        if (sys != null) {
            sys.containerSystem.containers.add(serviceInfo);
        } else if (!offline) {
            final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            if (assembler != null) {
                assembler.createContainer(serviceInfo);
            }else{
                throw new OpenEJBException("ContainerInfo: Assembler has not been defined");
            }
        }
    }

    protected void install(final ResourceInfo serviceInfo) throws OpenEJBException {
        if (sys != null) {
            sys.facilities.resources.add(serviceInfo);
        } else if (!offline) {
            doInstall(serviceInfo);
        }
    }

    void doInstall(final ResourceInfo serviceInfo) throws OpenEJBException {
        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        if (assembler != null) {
            assembler.createResource(null, serviceInfo);
        } else {
            throw new OpenEJBException("ResourceInfo: Assembler has not been defined");
        }
    }

    public OpenEjbConfiguration getOpenEjbConfiguration(final File configuartionFile) throws OpenEJBException {
        if (configuartionFile != null) {
            return getOpenEjbConfiguration(JaxbOpenejb.readConfig(configuartionFile.getAbsolutePath()));
        } else {
            return getOpenEjbConfiguration((Openejb) null);
        }
    }

    public OpenEjbConfiguration getOpenEjbConfiguration(final Openejb providedConf) throws OpenEJBException {
        if (sys != null) {
            return sys;
        }

        if (providedConf != null) {
            openejb = providedConf;
        } else if (configLocation != null) {
            openejb = JaxbOpenejb.readConfig(configLocation);
        } else {
            openejb = JaxbOpenejb.createOpenejb();
        }

        for (final SystemProperty sp : openejb.getSystemProperties()) {
            final String name = sp.getName();
            final String value = sp.getValue();
            SystemInstance.get().setProperty(name, value);
            JavaSecurityManagers.setSystemProperty(name, value);
        }

        loadPropertiesDeclaredConfiguration(openejb);

        sys = new OpenEjbConfiguration();
        sys.containerSystem = new ContainerSystemInfo();
        sys.facilities = new FacilitiesInfo();

        // listener + some config can be defined as service
        for (final Service service : openejb.getServices()) {
            final ServiceInfo info = configureService(service, ServiceInfo.class);
            sys.facilities.services.add(info);
        }

        for (final JndiProvider provider : openejb.getJndiProvider()) {
            final JndiContextInfo info = configureService(provider, JndiContextInfo.class);
            sys.facilities.remoteJndiContexts.add(info);
        }

        sys.facilities.securityService = configureService(openejb.getSecurityService(), SecurityServiceInfo.class);

        sys.facilities.transactionService = configureService(openejb.getTransactionManager(), TransactionServiceInfo.class);

        List<ResourceInfo> resources = new ArrayList<>();
        for (final Resource resource : openejb.getResource()) {
            final ResourceInfo resourceInfo = configureService(resource, ResourceInfo.class);
            resources.add(resourceInfo);
        }
        resources = sort(resources, null);

        sys.facilities.resources.addAll(resources);

        //        ConnectionManagerInfo service = configureService(openejb.getConnectionManager(), ConnectionManagerInfo.class);
        //        sys.facilities.connectionManagers.add(service);

        if (openejb.getProxyFactory() != null) {
            sys.facilities.intraVmServer = configureService(openejb.getProxyFactory(), ProxyFactoryInfo.class);
        }

        for (final Container declaration : openejb.getContainer()) {
            final ContainerInfo info = createContainerInfo(declaration);
            sys.containerSystem.containers.add(info);
        }

        final List<File> declaredApps = getDeclaredApps();

        for (final File jarFile : declaredApps) {
            try {

                final AppInfo appInfo = configureApplication(jarFile);
                sys.containerSystem.applications.add(appInfo);

            } catch (final OpenEJBException alreadyHandled) {
                final DeploymentExceptionManager exceptionManager = SystemInstance.get().getComponent(DeploymentExceptionManager.class);
                if (exceptionManager != null) {
                    exceptionManager.pushDelpoymentException(alreadyHandled);
                }
            }
        }

        final boolean embedded = SystemInstance.get().hasProperty(EJBContainer.class.getName());
        final Options options = SystemInstance.get().getOptions();

        if (options.get("openejb.system.apps", false)) {
            try {
                final boolean extended = SystemApps.isExtended();
                final AppInfo appInfo;
                if (!extended) { // do it manually, we know what we need and can skip a bunch of processing
                    appInfo = SystemAppInfo.preComputedInfo(this);
                } else {
                    appInfo = configureApplication(new AppModule(SystemApps.getSystemModule()));
                }
                sys.containerSystem.applications.add(appInfo);
            } catch (final OpenEJBException e) {
                logger.error("Unable to load the system applications.", e);
            }
        } else if (options.get(DEPLOYMENTS_CLASSPATH_PROPERTY, !embedded)) {

            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            final ArrayList<File> jarFiles = getModulesFromClassPath(declaredApps, classLoader);
            final String appId = "classpath.ear";

            final boolean classpathAsEar = options.get(CLASSPATH_AS_EAR, true);
            try {
                if (classpathAsEar && !jarFiles.isEmpty()) {

                    final AppInfo appInfo = configureApplication(classLoader, appId, jarFiles);
                    sys.containerSystem.applications.add(appInfo);

                } else {
                    for (final File jarFile : jarFiles) {

                        final AppInfo appInfo = configureApplication(jarFile);
                        sys.containerSystem.applications.add(appInfo);
                    }
                }

                if (jarFiles.size() == 0) {
                    logger.warning("config.noModulesFoundToDeploy");
                }

            } catch (final OpenEJBException alreadyHandled) {
                logger.debug("config.alreadyHandled");
            }
        }

        for (final Deployments deployments : openejb.getDeployments()) {
            if (deployments.isAutoDeploy()) {
                if (deployments.getDir() != null) {
                    sys.containerSystem.autoDeploy.add(deployments.getDir());
                }
            }
        }

        final OpenEjbConfiguration finished = sys;
        sys = null;
        openejb = null;

        return finished;
    }

    /**
     * Main loop that gets executed when OpenEJB starts up Reads config files and produces the basic "AST" the assembler needs to actually build the contianer system
     *
     * This method is called by the Assembler once at startup.
     *
     * @return OpenEjbConfiguration
     * @throws OpenEJBException
     */
    @Override
    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {
        return getOpenEjbConfiguration((Openejb) null);
    }

    private List<File> getDeclaredApps() {
        // make a copy of the list because we update it
        final List<Deployments> deployments = new ArrayList<>();

        if (openejb != null) {
            deployments.addAll(openejb.getDeployments());
        }

        Collection<Deployments> additionalDeploymentsList = Collections.emptyList();
        try {
            final File additionalDeploymentFile = SystemInstance.get().getBase().getFile(ADDITIONAL_DEPLOYMENTS, false);

            if (additionalDeploymentFile.exists()) {
                InputStream fis = null;
                try {
                    fis = IO.read(additionalDeploymentFile);
                    final AdditionalDeployments additionalDeployments = JaxbOpenejb.unmarshal(AdditionalDeployments.class, fis);
                    additionalDeploymentsList = additionalDeployments.getDeployments();
                } catch (final Exception e) {
                    logger.error("can't read " + ADDITIONAL_DEPLOYMENTS, e);
                } finally {
                    IO.close(fis);
                }
            }
        } catch (final Exception e) {
            logger.info("No additional deployments found: " + e);
        }

        // resolve jar locations //////////////////////////////////////  BEGIN  ///////

        final FileUtils base = SystemInstance.get().getBase();

        final List<Deployments> autoDeploy = new ArrayList<>();

        final List<File> declaredAppsUrls = new ArrayList<>();

        for (final Deployments deployment : deployments) {
            try {
                DeploymentsResolver.loadFrom(deployment, base, declaredAppsUrls);
                if (deployment.isAutoDeploy()) {
                    autoDeploy.add(deployment);
                }
            } catch (final SecurityException se) {
                logger.warning("Security check failed on deployment: " + deployment.getFile(), se);
            }
        }
        for (final Deployments additionalDep : additionalDeploymentsList) {
            if (additionalDep.getFile() != null) {
                declaredAppsUrls.add(Files.path(base.getDirectory().getAbsoluteFile(), additionalDep.getFile()));
            } else if (additionalDep.getDir() != null) {
                declaredAppsUrls.add(Files.path(base.getDirectory().getAbsoluteFile(), additionalDep.getDir()));
            }
            if (additionalDep.isAutoDeploy()) {
                autoDeploy.add(additionalDep);
            }
        }

        if (autoDeploy.size() > 0) {
            final AutoDeployer autoDeployer = new AutoDeployer(this, autoDeploy);
            SystemInstance.get().setComponent(AutoDeployer.class, autoDeployer);
            SystemInstance.get().addObserver(autoDeployer);
        }

        return declaredAppsUrls;
    }

    public ArrayList<File> getModulesFromClassPath(final List<File> declaredApps, final ClassLoader classLoader) {

        final List<URL> classpathAppsUrls = DeploymentsResolver.loadFromClasspath(classLoader);

        final ArrayList<File> jarFiles = new ArrayList<>();
        for (final URL path : classpathAppsUrls) {
            final File file = URLs.toFile(path);

            if (declaredApps != null && declaredApps.contains(file)) {
                continue;
            }

            jarFiles.add(file);
        }
        return jarFiles;
    }

    public ContainerInfo createContainerInfo(final Container container) throws OpenEJBException {
        final Class<? extends ContainerInfo> infoClass = getContainerInfoType(container.getType());
        if (infoClass == null) {
            throw new OpenEJBException(messages.format("unrecognizedContainerType", container.getType()));
        }

        return configureService(container, infoClass);
    }

    public static void loadPropertiesDeclaredConfiguration(final Openejb openejb) {

        final Properties sysProps = new Properties(JavaSecurityManagers.getSystemProperties());
        sysProps.putAll(SystemInstance.get().getProperties());
        fillOpenEjb(openejb, sysProps);
    }

    public static void fillOpenEjb(final Openejb openejb, final Properties sysProps) {
        for (final Map.Entry<Object, Object> entry : sysProps.entrySet()) {

            final Object o = entry.getValue();
            if (!(o instanceof String)) {
                continue;
            }
            if (!((String) o).startsWith("new://")) {
                continue;
            }

            final String name = (String) entry.getKey();
            final String value = (String) entry.getValue();

            try {
                final Object service = toConfigDeclaration(name, value);

                openejb.add(service);
            } catch (final URISyntaxException e) {
                logger.error("Error declaring service '" + name + "'. Invalid Service URI '" + value + "'.  java.net.URISyntaxException: " + e.getMessage());
            } catch (final OpenEJBException e) {
                logger.error(e.getMessage());
            }
        }
    }

    protected static Object toConfigDeclaration(final String name, String value) throws URISyntaxException, OpenEJBException {
        //        value = value.replaceFirst("(.)#", "$1%23");
        value = value.replaceFirst("(provider=[^#=&]+)#", "$1%23");

        final URI uri = URLs.uri(value);

        return toConfigDeclaration(name, uri);
    }

    public static Object toConfigDeclaration(final String id, final URI uri) throws OpenEJBException {
        final String serviceType;
        try {
            serviceType = uri.getHost();

            final Object object;
            try {
                object = JaxbOpenejb.create(serviceType);
            } catch (final Exception e) {
                throw new OpenEJBException("Invalid URI '" + uri + "'. " + e.getMessage());
            }

            final Map<String, String> map;
            try {
                map = URISupport.parseParamters(uri);
            } catch (final URISyntaxException e) {
                throw new OpenEJBException("Unable to parse URI parameters '" + uri + "'. URISyntaxException: " + e.getMessage());
            }

            if (object instanceof AbstractService) {
                final AbstractService service = (AbstractService) object;
                service.setId(id);
                service.setType(map.remove("type"));
                service.setProvider(map.remove("provider"));
                service.setClassName(map.remove("class-name"));
                service.setConstructor(map.remove("constructor"));
                service.setFactoryName(map.remove("factory-name"));
                service.setPropertiesProvider(map.remove("properties-provider"));
                service.setTemplate(map.remove("template"));

                final String cp = map.remove("classpath");
                if (null != cp) {
                    service.setClasspath(cp);
                }

                service.setClasspathAPI(map.remove("classpath-api"));

                if (object instanceof Resource) {
                    final Resource resource = Resource.class.cast(object);
                    final String aliases = map.remove("aliases");
                    if (aliases != null) {
                        resource.getAliases().addAll(Arrays.asList(aliases.split(",")));
                    }
                    final String depOn = map.remove("depends-on");
                    if (depOn != null) {
                        resource.getDependsOn().addAll(Arrays.asList(depOn.split(",")));
                    }
                    resource.setPostConstruct(map.remove("post-construct"));
                    resource.setPreDestroy(map.remove("pre-destroy"));
                }

                service.getProperties().putAll(map);
            } else if (object instanceof Deployments) {
                final Deployments deployments = (Deployments) object;
                deployments.setDir(map.remove("dir"));
                deployments.setFile(map.remove("jar"));
                final String cp = map.remove("classpath");
                if (cp != null) {
                    final String[] paths = cp.split(File.pathSeparator);
                    final List<URL> urls = new ArrayList<>();
                    for (final String path : paths) {
                        final Set<String> values = ProvisioningUtil.realLocation(PropertyPlaceHolderHelper.value(path));
                        for (final String v : values) {
                            urls.add(new File(v).toURI().normalize().toURL());
                        }
                    }
                    deployments.setClasspath(new URLClassLoaderFirst(urls.toArray(new URL[urls.size()]), ParentClassLoaderFinder.Helper.get()));
                }
            } else if (SystemProperty.class.isInstance(object)) {
                final SystemProperty sp = SystemProperty.class.cast(object);
                sp.setName(map.remove("name"));
                sp.setValue(map.remove("value"));
            }

            return object;
        } catch (final Exception e) {
            throw new OpenEJBException("Error declaring service '" + id + "'. Unable to create Service definition from URI '" + uri.toString() + "'", e);
        }
    }

    public AppInfo configureApplication(final File jarFile) throws OpenEJBException {
        logger.debug("Beginning load: " + jarFile.getAbsolutePath());

        try {
            final AppModule appModule = deploymentLoader.load(jarFile, null);
            final AppInfo appInfo = configureApplication(appModule);

            // we need the finder for web scanning so push it to what sees TomcatWebAppBuilder, ie the info tree
            // this is clean up in Assembler for safety and TomcatWebAppBuilder when used
            if (!appModule.getWebModules().isEmpty()) {
                for (final WebAppInfo info : appInfo.webApps) {
                    for (final EjbModule ejbModule : appModule.getEjbModules()) {
                        if (ejbModule.getModuleId().equals(info.moduleId) && ejbModule.getFinder() != null) {
                            appInfo.properties.put(info, ejbModule);
                        }
                    }
                }
            }

            // TODO This is temporary -- we need to do this in AppInfoBuilder
            appInfo.paths.add(appInfo.path);
            appInfo.paths.add(jarFile.getAbsolutePath());
            return appInfo;
        } catch (final ValidationFailedException e) {
            logger.warning("configureApplication.loadFailed", jarFile.getAbsolutePath(), e.getMessage()); // DO not include the stacktrace in the message
            throw e;
        } catch (final OpenEJBException e) {
            // DO NOT REMOVE THE EXCEPTION FROM THIS LOG MESSAGE
            // removing this message causes NO messages to be printed when embedded
            logger.warning("configureApplication.loadFailed", e, jarFile.getAbsolutePath(), e.getMessage());
            throw e;
        }
    }

    /**
     * embedded usage
     *
     * @param classLoader classloader
     * @param id          id supplied from embedded properties or null
     * @param jarFiles    list of ejb modules
     * @return configured AppInfo
     * @throws OpenEJBException on error
     */
    public AppInfo configureApplication(final ClassLoader classLoader, final String id, final List<File> jarFiles) throws OpenEJBException {
        final AppModule collection = loadApplication(classLoader, id, jarFiles);

        final AppInfo appInfo;
        try {
            appInfo = configureApplication(collection);
        } catch (final ValidationFailedException e) {
            logger.warning("configureApplication.loadFailed", collection.getModuleId(), e.getMessage()); // DO not include the stacktrace in the message
            throw e;
        } catch (final OpenEJBException e) {
            // DO NOT REMOVE THE EXCEPTION FROM THIS LOG MESSAGE
            // removing this message causes NO messages to be printed when embedded
            logger.warning("configureApplication.loadFailed", e, collection.getModuleId(), e.getMessage());
            throw e;
        }

        return appInfo;
    }

    @SuppressWarnings("unchecked")
    public AppModule loadApplication(final ClassLoader classLoader, String id, final List<File> jarFiles) throws OpenEJBException {
        final boolean standaloneModule = id == null;
        if (standaloneModule) {
            id = "";
        }
        final Application application = new Application();
        application.setApplicationName(id);
        final AppModule collection = new AppModule(classLoader, id, application, standaloneModule);
        final Map<String, Object> altDDs = collection.getAltDDs();

        for (final File jarFile : jarFiles) {
            logger.info("Beginning load: " + jarFile.getAbsolutePath());

            try {
                final AppModule module = deploymentLoader.load(jarFile, null);

                collection.getAdditionalLibraries().addAll(module.getAdditionalLibraries());
                collection.getClientModules().addAll(module.getClientModules());
                collection.getEjbModules().addAll(module.getEjbModules());
                collection.addPersistenceModules(module.getPersistenceModules());
                collection.getConnectorModules().addAll(module.getConnectorModules());
                collection.getWebModules().addAll(module.getWebModules());
                collection.getWatchedResources().addAll(module.getWatchedResources());

                // Merge altDDs
                for (final Map.Entry<String, Object> entry : module.getAltDDs().entrySet()) {
                    final Object existingValue = altDDs.get(entry.getKey());

                    if (existingValue == null) {
                        altDDs.put(entry.getKey(), entry.getValue());
                    } else if (entry.getValue() instanceof Collection) {
                        if (existingValue instanceof Collection) {
                            final Collection values = (Collection) existingValue;
                            values.addAll((Collection) entry.getValue());
                        }
                    } else if (entry.getValue() instanceof Map) {
                        if (existingValue instanceof Map) {
                            final Map values = (Map) existingValue;
                            values.putAll((Map) entry.getValue());
                        }
                    }
                }

            } catch (final ValidationFailedException e) {
                logger.warning("configureApplication.loadFailed", jarFile.getAbsolutePath(), e.getMessage()); // DO not include the stacktrace in the message
                throw e;
            } catch (final OpenEJBException e) {
                // DO NOT REMOVE THE EXCEPTION FROM THIS LOG MESSAGE
                // removing this message causes NO messages to be printed when embedded
                logger.warning("configureApplication.loadFailed", e, jarFile.getAbsolutePath(), e.getMessage());
                throw e;
            }
        }
        return collection;
    }

    public EjbJarInfo configureApplication(final EjbJar ejbJar) throws OpenEJBException {
        final EjbModule ejbModule = new EjbModule(ejbJar);
        return configureApplication(ejbModule);
    }

    public EjbJarInfo configureApplication(final EjbModule ejbModule) throws OpenEJBException {
        final AppInfo appInfo = configureApplication(new AppModule(ejbModule));
        return appInfo.ejbJars.get(0);
    }

    public ClientInfo configureApplication(final ClientModule clientModule) throws OpenEJBException {
        final AppInfo appInfo = configureApplication(new AppModule(clientModule));
        return appInfo.clients.get(0);
    }

    public ConnectorInfo configureApplication(final ConnectorModule connectorModule) throws OpenEJBException {
        final AppInfo appInfo = configureApplication(new AppModule(connectorModule));
        return appInfo.connectors.get(0);
    }

    public WebAppInfo configureApplication(final WebModule webModule) throws OpenEJBException {
        final AppInfo appInfo = configureApplication(new AppModule(webModule));
        return appInfo.webApps.get(0);
    }

    public AppInfo configureApplication(final AppModule appModule) throws OpenEJBException {
        try {
            final Collection<Class<?>> extensions = new HashSet<>();
            final Collection<String> notLoaded = new HashSet<>();

            final List<URL> libs = appModule.getAdditionalLibraries();
            if (libs != null && libs.size() > 0) {
                final Extensions.Finder finder = new Extensions.Finder("META-INF", false, libs.toArray(new URL[libs.size()]));
                extensions.addAll(Extensions.findExtensions(finder));
                notLoaded.addAll(finder.getResourcesNotLoaded());
            }
            for (final EjbModule ejb : appModule.getEjbModules()) {
                try {
                    final URI uri = ejb.getModuleUri();
                    if (uri.isAbsolute()) {
                        final URL url = uri.toURL();
                        if (libs != null && !libs.contains(url)) {
                            final Extensions.Finder finder = new Extensions.Finder("META-INF", false, url);
                            extensions.addAll(Extensions.findExtensions(finder));
                            notLoaded.addAll(finder.getResourcesNotLoaded());
                        }
                    }
                } catch (final IllegalArgumentException | MalformedURLException iae) {
                    logger.debug("can't look for server event listener for module " + ejb.getModuleUri(), iae);
                } catch (final Exception e) {
                    logger.error("can't look for server event listener for module " + ejb.getJarLocation());
                }
            }
            for (final WebModule web : appModule.getWebModules()) {
                final List<URL> webLibs = web.getScannableUrls();
                if (webLibs != null && webLibs.size() > 0) {
                    final Extensions.Finder finder = new Extensions.Finder("META-INF", false, webLibs.toArray(new URL[webLibs.size()]));
                    extensions.addAll(Extensions.findExtensions(finder));
                    notLoaded.addAll(finder.getResourcesNotLoaded());
                }
            }

            // add it as early as possible, the ones needing the app classloader will be added later
            Extensions.addExtensions(extensions);

            final String location = appModule.getJarLocation();
            logger.info("config.configApp", null != location ? location : appModule.getModuleId());
            deployer.deploy(appModule);
            final AppInfoBuilder appInfoBuilder = new AppInfoBuilder(this);

            final AppInfo info = appInfoBuilder.build(appModule);
            info.eventClassesNeedingAppClassloader.addAll(notLoaded);

            return info;
        } finally {
            destroy(appModule.getEarLibFinder());
            for (final EjbModule ejb : appModule.getEjbModules()) {
                destroy(ejb.getFinder());
            }
            for (final WebModule web : appModule.getWebModules()) {
                destroy(web.getFinder());
            }
        }
    }

    private static void destroy(final IAnnotationFinder finder) {
        if (finder == null) {
            return;
        }
        if (AutoCloseable.class.isInstance(finder)) {
            try {
                AutoCloseable.class.cast(finder).close();
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    private static class DefaultService {

        private final Class<? extends org.apache.openejb.config.Service> type;
        private final String id;

        public DefaultService(final String id, final Class<? extends org.apache.openejb.config.Service> type) {
            this.id = id;
            this.type = type;
        }
    }

    private static final Map<Class<? extends ServiceInfo>, DefaultService> defaultProviders = new HashMap<Class<? extends ServiceInfo>, DefaultService>();

    private static final Map<Class<? extends ServiceInfo>, Class<? extends org.apache.openejb.config.Service>> types = new HashMap<Class<? extends ServiceInfo>, Class<? extends org.apache.openejb.config.Service>>();

    /**
     * This is the magic that allows people to be really vague in their openejb.xml and not specify
     * the provider for one of their declared services.  We look here for the default service id
     *
     * We then look in the default provider "namespace" that is setup, which will usually be either one of:
     *
     *   - org.apache.openejb
     *   - org.apache.openejb.embedded
     *   - org.apache.openejb.tomcat
     *   - org.apache.openejb.jetty
     *
     * As in:
     *
     *   - META-INF/<provider>/service-jar.xml
     *
     */
    static {
        defaultProviders.put(MdbContainerInfo.class, new DefaultService("MESSAGE", Container.class));
        defaultProviders.put(ManagedContainerInfo.class, new DefaultService("MANAGED", Container.class));
        defaultProviders.put(StatefulSessionContainerInfo.class, new DefaultService("STATEFUL", Container.class));
        defaultProviders.put(StatelessSessionContainerInfo.class, new DefaultService("STATELESS", Container.class));
        defaultProviders.put(SingletonSessionContainerInfo.class, new DefaultService("SINGLETON", Container.class));
        defaultProviders.put(CmpEntityContainerInfo.class, new DefaultService("CMP_ENTITY", Container.class));
        defaultProviders.put(BmpEntityContainerInfo.class, new DefaultService("BMP_ENTITY", Container.class));
        defaultProviders.put(SecurityServiceInfo.class, new DefaultService("SecurityService", SecurityService.class));
        defaultProviders.put(TransactionServiceInfo.class, new DefaultService("TransactionManager", TransactionManager.class));
        defaultProviders.put(ConnectionManagerInfo.class, new DefaultService("ConnectionManager", ConnectionManager.class));
        defaultProviders.put(ProxyFactoryInfo.class, new DefaultService("ProxyFactory", ProxyFactory.class));

        for (final Map.Entry<Class<? extends ServiceInfo>, DefaultService> entry : defaultProviders.entrySet()) {
            types.put(entry.getKey(), entry.getValue().type);
        }

        types.put(ResourceInfo.class, Resource.class);
    }

    public <T extends ServiceInfo> T configureService(final Class<? extends T> type) throws OpenEJBException {
        return configureService((org.apache.openejb.config.Service) null, type);
    }

    private <T extends ServiceInfo> org.apache.openejb.config.Service getDefaultService(final Class<? extends T> type) throws OpenEJBException {
        final DefaultService defaultService = defaultProviders.get(type);

        if (defaultService == null) {
            return null;
        }

        final org.apache.openejb.config.Service service;
        try {
            service = JaxbOpenejb.create(defaultService.type);
            service.setType(defaultService.id);
        } catch (final Exception e) {
            final String name = defaultService.type == null ? "null" : defaultService.type.getName();
            throw new OpenEJBException("Cannot instantiate class " + name, e);
        }
        return service;
    }

    /**
     * This is the major piece of code that configures services.
     * It merges the data from the <ServiceProvider> declaration
     * with the data from the openejb.xml file (say <Resource>)
     *
     * The end result is a canonical (i.e. flattened) ServiceInfo
     * The ServiceInfo will be of a specific type (ContainerInfo, ResourceInfo, etc)
     *
     * @param service  Service
     * @param infoType Class
     * @param <T>      infoType
     * @return ServiceInfo
     * @throws OpenEJBException On error
     */
    public <T extends ServiceInfo> T configureService(org.apache.openejb.config.Service service, final Class<? extends T> infoType) throws OpenEJBException {
        try {
            if (infoType == null) {
                throw new NullPointerException("type");
            }

            if (service == null) {
                service = getDefaultService(infoType);
                if (service == null) {
                    throw new OpenEJBException(messages.format("configureService.noDefaultService", infoType.getName()));
                }
            }

            {
                String template = service.getTemplate();
                if (template == null) {
                    template = SystemInstance.get().getProperty(Template.class.getName());
                }
                if (template != null) {
                    template = unaliasPropertiesProvider(template);

                    // don't trim them, user wants to handle it himself, let him do it
                    final ObjectRecipe recipe = newObjectRecipe(template);
                    recipe.setProperty("serviceId", service.getId());
                    // note: we can also use reflection if needed to limit the dependency
                    Template.class.cast(recipe.create()).configure(service);
                }
            }

            final ServiceProvider provider = getServiceProvider(service, infoType);
            if (service.getId() == null) {
                service.setId(provider.getId());
            }

            final Properties overrides = trim(getSystemProperties(overrideKey(service), provider.getService()));

            final Properties serviceProperties = service.getProperties();

            trim(serviceProperties);

            trim(provider.getProperties());

            logger.info("configureService.configuring", service.getId(), provider.getService(), provider.getId());

            if (logger.isDebugEnabled()) {
                for (final Map.Entry<Object, Object> entry : serviceProperties.entrySet()) {
                    final Object key = entry.getKey();
                    Object value = entry.getValue();

                    if (key instanceof String && "password".equalsIgnoreCase((String) key)) {
                        value = "<hidden>";
                    }

                    logger.debug("[" + key + "=" + value + "]");
                }

                for (final Map.Entry<Object, Object> entry : overrides.entrySet()) {
                    final Object key = entry.getKey();
                    Object value = entry.getValue();

                    if (key instanceof String && "password".equalsIgnoreCase((String) key)) {
                        value = "<hidden>";
                    }

                    logger.debug("Override [" + key + "=" + value + "]");
                }
            }

            final Properties props = new SuperProperties().caseInsensitive(true);

            // weird hack but sometimes we don't want default values when we want null for instance
            if (serviceProperties == null || "false".equals(serviceProperties.getProperty(IGNORE_DEFAULT_VALUES_PROP, "false"))) {
                props.putAll(provider.getProperties());
            }

            if (serviceProperties != null) {
                props.putAll(serviceProperties);
            }
            props.putAll(overrides);

            {// force user properties last
                String propertiesProvider = service.getPropertiesProvider();
                if (propertiesProvider == null) {
                    propertiesProvider = SystemInstance.get().getProperty(PropertiesResourceProvider.class.getName());
                }
                if (propertiesProvider != null) {
                    propertiesProvider = unaliasPropertiesProvider(propertiesProvider);

                    // don't trim them, user wants to handle it himself, let him do it
                    final ObjectRecipe recipe = newObjectRecipe(propertiesProvider);
                    recipe.setFactoryMethod("provides");
                    recipe.setProperty("serviceId", service.getId());
                    recipe.setProperties(props);
                    recipe.setProperty("properties", props); // let user get all config
                    final Properties p = Properties.class.cast(recipe.create());

                    props.putAll(p);
                }
            }

            props.remove(IGNORE_DEFAULT_VALUES_PROP);

            final T info;
            try {
                info = infoType.newInstance();
            } catch (final Exception e) {
                throw new OpenEJBException(messages.format("configureService.cannotInstantiateClass", infoType.getName()), e);
            }

            // some jndi adjustment
            if (service.getId().startsWith("java:/")) {
                service.setId(service.getId().substring("java:/".length()));
            }

            info.service = provider.getService();
            info.types.addAll(provider.getTypes());
            info.description = provider.getDescription();
            info.displayName = provider.getDisplayName();
            info.className = provider.getClassName();
            info.factoryMethod = provider.getFactoryName();
            info.id = service.getId();
            info.properties = props;
            info.constructorArgs.addAll(parseConstructorArgs(provider));
            if (info instanceof ResourceInfo && service instanceof Resource) {
                final ResourceInfo ri = ResourceInfo.class.cast(info);
                final Resource resource = Resource.class.cast(service);
                ri.jndiName = resource.getJndi();
                ri.postConstruct = resource.getPostConstruct();
                ri.preDestroy = resource.getPreDestroy();
                ri.aliases.addAll(resource.getAliases());
                ri.dependsOn.addAll(resource.getDependsOn());
            }

            if (service.getClasspath() != null && service.getClasspath().length() > 0) {
                info.classpath = resolveClasspath(service.getClasspath());
            }
            info.classpathAPI = service.getClasspathAPI();

            specialProcessing(info);

            return info;
        } catch (final NoSuchProviderException e) {
            final String message = logger.fatal("configureService.failed", e, (null != service ? service.getId() : ""));
            throw new OpenEJBException(message + ": " + e.getMessage());
        } catch (final Throwable e) {
            final String message = logger.fatal("configureService.failed", e, (null != service ? service.getId() : ""));
            throw new OpenEJBException(message, e);
        }
    }

    private <T extends ServiceInfo> ServiceProvider getServiceProvider(
            final org.apache.openejb.config.Service service,
            final Class<? extends T> infoType) throws OpenEJBException {
        final String providerType = getProviderType(service);

        final ServiceProvider provider = resolveServiceProvider(service, infoType);

        if (provider == null) {
            final List<ServiceProvider> providers = ServiceUtils.getServiceProvidersByServiceType(providerType);
            final StringBuilder sb = new StringBuilder();
            final List<String> types = new ArrayList<>();
            for (final ServiceProvider p : providers) {
                for (final String type : p.getTypes()) {
                    if (types.contains(type)) {
                        continue;
                    }
                    types.add(type);
                    sb.append(JavaSecurityManagers.getSystemProperty("line.separator"));
                    sb.append("  <").append(p.getService());
                    sb.append(" id=\"").append(service.getId()).append('"');
                    sb.append(" type=\"").append(type).append("\"/>");
                }
            }
            final String noProviderMessage = messages.format("configureService.noProviderForService", providerType, service.getId(), service.getType(), service.getProvider(), sb.toString());
            throw new NoSuchProviderException(noProviderMessage);
        }

        if (!provider.getService().equals(providerType)) {
            throw new OpenEJBException(messages.format("configureService.wrongProviderType", service.getId(), providerType));
        }
        return provider;
    }

    private ObjectRecipe newObjectRecipe(final String template) {
        final ObjectRecipe recipe = new ObjectRecipe(template);
        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        recipe.allow(Option.PRIVATE_PROPERTIES);
        recipe.allow(Option.FIELD_INJECTION);
        recipe.allow(Option.NAMED_PARAMETERS);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        return recipe;
    }

    private static String unaliasPropertiesProvider(final String propertiesProvider) {
        switch (propertiesProvider.toLowerCase(Locale.ENGLISH)) {
            case "heroku":
                return "org.apache.openejb.resource.heroku.HerokuDatabasePropertiesProvider";
            case "openshift:mysql":
                return "org.apache.openejb.resource.openshift.OpenshiftMySQLPropertiesProvider";
            case "openshift:postgresql":
                return "org.apache.openejb.resource.openshift.OpenshiftPostgreSQLPropertiesProvider";
            default:
                return propertiesProvider;
        }
    }

    private static String unaliasTemplate(final String value) {
        return value;
    }

    /**
     * Takes a raw unparsed string expected to be in jvm classpath syntax
     * and parses it, producing a collection of URIs representing the absolute
     * file paths of the classpath to be created.
     *
     * OS specific delimiters are supported.
     *
     * @param rawstring unparsed string in "classpath" syntax
     * @return URI array
     * @throws IOException if path cannot be resolved or file referenced does not exist
     */
    public static URI[] resolveClasspath(final String rawstring) throws IOException {

        final FileUtils base = SystemInstance.get().getBase();
        final String[] strings = rawstring.contains("mvn:") ? rawstring.split(";") : rawstring.split(File.pathSeparator);
        final Collection<URI> classpath = new LinkedList<>();

        for (final String string : strings) {
            final Set<String> locations = ProvisioningUtil.realLocation(PropertyPlaceHolderHelper.simpleValue(string));
            for (final String location : locations) {
                final File file = base.getFile(location, false);
                classpath.add(file.toURI());
            }
        }

        return classpath.toArray(new URI[classpath.size()]);
    }

    private String overrideKey(final org.apache.openejb.config.Service service) {
        final String origin = String.class.cast(service.getProperties().remove(AutoConfig.ORIGINAL_ID));
        if (origin != null) {
            return origin;
        }
        return service.getId();
    }

    private static String getProviderType(final org.apache.openejb.config.Service service) {

        Class<?> clazz = service.getClass();

        if (AbstractService.class.isAssignableFrom(clazz)) {
            while (!clazz.getSuperclass().equals(AbstractService.class)) {
                clazz = clazz.getSuperclass();
            }
        }

        return clazz.getSimpleName();
    }

    private static Properties trim(final Properties properties) {
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final Object o = entry.getValue();
            if (o instanceof String) {
                final String value = (String) o;
                final String trimmed = value.trim();
                if (value.length() != trimmed.length()) {
                    properties.put(entry.getKey(), trimmed);
                }
            }
        }
        return properties;
    }

    private <T extends ServiceInfo> void specialProcessing(final T info) {
        TopicOrQueueDefaults.process(info);
    }

    @SuppressWarnings({"unchecked"})
    private ServiceProvider resolveServiceProvider(final org.apache.openejb.config.Service service, final Class infoType) throws OpenEJBException {

        if (service.getClassName() != null) {
            if (service.getType() == null) {
                service.setType(service.getClassName());
            }

            final ServiceProvider provider = new ServiceProvider();
            provider.setId(service.getId());
            provider.setService(getProviderType(service));
            provider.getTypes().add(service.getType());
            provider.setClassName(service.getClassName());
            provider.setConstructor(service.getConstructor());
            provider.setFactoryName(service.getFactoryName());
            return provider;
        }

        if (service.getProvider() != null) {
            return ServiceUtils.getServiceProvider(service.getProvider());
        }

        if (service.getType() == null && serviceTypeIsAdjustable) {
            // try to guess quickly for know type
            // -> DataSource
            // the algo is weird but works, don't try to simplify it too much
            // because we just have the service properties, not our defaults
            final Properties properties = service.getProperties();
            if ((properties.containsKey("JdbcDriver") || properties.containsKey("JdbcUrl") || properties.containsKey("url"))
                && (properties.containsKey("JtaManaged") || properties.containsKey("UserName") || properties.containsKey("Password"))) {
                service.setType("javax.sql.DataSource");
            }
        }

        if (service.getType() != null) {
            return ServiceUtils.getServiceProviderByType(getProviderType(service), service.getType());
        }

        if (service.getId() != null) {
            try {
                return ServiceUtils.getServiceProvider(service.getId());
            } catch (final NoSuchProviderException e) {
                logger.debug("resolveServiceProvider", e);
            }
        }

        if (infoType != null) {
            final org.apache.openejb.config.Service defaultService = getDefaultService(infoType);
            if (defaultService != null) {
                return resolveServiceProvider(defaultService, null);
            }
        }

        return null;
    }

    public <T extends ServiceInfo> T configureService(final String id, final Class<? extends T> type) throws OpenEJBException {
        return configureService(type, id, null, id, null);
    }

    /**
     * Resolving the provider for a particular service follows this algorithm:
     *
     * 1.  Attempt to load the provider specified by the 'providerId'.
     * 2.  If this fails, throw NoSuchProviderException
     * 3.  If providerId is null, attempt to load the specified provider using the 'serviceId' as the 'providerId'
     * 4.  If this fails, check the hardcoded defaults for a default providerId using the supplied 'type'
     * 5.  If this fails, throw NoSuchProviderException
     *
     * @param type               Class T
     * @param serviceId          String
     * @param declaredProperties Properties
     * @param providerId         String
     * @param serviceType        String
     * @return ServiceInfo T
     * @throws OpenEJBException
     */
    public <T extends ServiceInfo> T configureService(final Class<? extends T> type, final String serviceId, final Properties declaredProperties, final String providerId, final String serviceType) throws OpenEJBException {
        if (type == null) {
            throw new NullPointerException("type is null");
        }

        final Class<? extends org.apache.openejb.config.Service> serviceClass = types.get(type);
        if (serviceClass == null) {
            throw new OpenEJBException("Unsupported service info type: " + type.getName());
        }
        final org.apache.openejb.config.Service service;
        try {
            service = serviceClass.newInstance();
        } catch (final Exception e) {
            throw new OpenEJBException(messages.format("configureService.cannotInstantiateClass", serviceClass.getName()), e);
        }
        service.setId(serviceId);
        service.setProvider(providerId);

        if (declaredProperties != null) {
            service.getProperties().putAll(declaredProperties);
        }

        return configureService(service, type);
    }

    protected static Properties getSystemProperties(final String serviceId, final String serviceType) {
        // Override with system properties
        final Properties sysProps = new Properties(JavaSecurityManagers.getSystemProperties());
        sysProps.putAll(SystemInstance.get().getProperties());

        return getOverrides(sysProps, serviceId, serviceType);
    }

    protected static Properties getOverrides(final Properties properties, final String serviceId, final String serviceType) {
        final String fullPrefix = serviceType.toUpperCase() + "." + serviceId + ".";
        final String fullPrefix2 = serviceType.toUpperCase() + "." + serviceId + "|";
        final String shortPrefix = serviceId + ".";
        final String shortPrefix2 = serviceId + "|";

        final Properties overrides = new Properties();
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {

            String name = (String) entry.getKey();

            for (final String prefix : Arrays.asList(fullPrefix, fullPrefix2, shortPrefix, shortPrefix2)) {
                if (name.toLowerCase().startsWith(prefix.toLowerCase())) {

                    name = name.substring(prefix.length());

                    // TODO: Maybe use xbean-reflect to get the string value
                    final String value = entry.getValue().toString();

                    overrides.setProperty(name, value);
                    break;
                }
            }

        }
        return overrides;
    }

    private static final Map<String, Class<? extends ContainerInfo>> containerTypes = new HashMap<String, Class<? extends ContainerInfo>>();

    static {
        containerTypes.put(BeanTypes.SINGLETON, SingletonSessionContainerInfo.class);
        containerTypes.put(BeanTypes.MANAGED, ManagedContainerInfo.class);
        containerTypes.put(BeanTypes.STATELESS, StatelessSessionContainerInfo.class);
        containerTypes.put(BeanTypes.STATEFUL, StatefulSessionContainerInfo.class);
        containerTypes.put(BeanTypes.BMP_ENTITY, BmpEntityContainerInfo.class);
        containerTypes.put(BeanTypes.CMP_ENTITY, CmpEntityContainerInfo.class);
        containerTypes.put(BeanTypes.MESSAGE, MdbContainerInfo.class);
    }

    protected static Class<? extends ContainerInfo> getContainerInfoType(final String ctype) {
        return containerTypes.get(ctype);
    }

    private List<String> parseConstructorArgs(final ServiceProvider service) {
        final String constructor = service.getConstructor();
        if (constructor == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(constructor.split("[ ,]+"));
    }

    protected List<String> getResourceIds() {
        return getResourceIds(null);
    }

    protected List<String> getResourceIds(final String type) {
        return getResourceIds(type, null);
    }

    public List<String> getResourceIds(final String type, Properties required) {
        final List<String> resourceIds = new ArrayList<>();

        if (required == null) {
            required = new Properties();
        }

        final OpenEjbConfiguration runningConfig = getRunningConfig();
        if (runningConfig != null) {
            for (final ResourceInfo resourceInfo : runningConfig.facilities.resources) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Trying to match resource type %s with classname %s, service %s.", type, resourceInfo.className, resourceInfo.service));
                }
                if ((type != null && type.equals(resourceInfo.className) || isResourceType(resourceInfo.service, resourceInfo.types, type)) && implies(required, resourceInfo.properties)) {
                    resourceIds.add(resourceInfo.id);
                    resourceIds.addAll(resourceInfo.aliases);
                }
            }
        }

        if (sys != null) {
            for (final ResourceInfo resourceInfo : sys.facilities.resources) {
                if (isResourceType(resourceInfo.service, resourceInfo.types, type) && implies(required, resourceInfo.properties)) {
                    resourceIds.add(resourceInfo.id);
                    resourceIds.addAll(resourceInfo.aliases);
                }
            }

            // The only time we'd have one of these is if we were building
            // the above sys instance
            if (openejb != null) {
                for (final Resource resource : openejb.getResource()) {
                    final ArrayList<String> types = new ArrayList<>();
                    if (resource.getType() != null) {
                        types.add(resource.getType());
                    }
                    if (isResourceType("Resource", types, type) && implies(required, resource.getProperties())) {
                        resourceIds.add(resource.getId());
                        resourceIds.addAll(resource.getAliases());
                    }
                }
            }
        }
        return resourceIds;
    }

    protected ResourceInfo getResourceInfo(final String id) {
        final OpenEjbConfiguration runningConfig = getRunningConfig();
        if (runningConfig != null) {
            for (final ResourceInfo resourceInfo : runningConfig.facilities.resources) {
                if (id.equals(resourceInfo.id)) {
                    return resourceInfo;
                }
                for (final String alias : resourceInfo.aliases) {
                    if (alias.equals(id)) {
                        return resourceInfo;
                    }
                }
            }
        }

        if (sys != null) {
            for (final ResourceInfo resourceInfo : sys.facilities.resources) {
                if (id.equals(resourceInfo.id)) {
                    return resourceInfo;
                }
                for (final String alias : resourceInfo.aliases) {
                    if (alias.equals(id)) {
                        return resourceInfo;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isResourceType(final String service, final List<String> types, final String type) {
        return type == null || service != null && types.contains(type);
    }

    protected List<String> getContainerIds() {
        final List<String> containerIds = new ArrayList<>();

        final OpenEjbConfiguration runningConfig = getRunningConfig();
        if (runningConfig != null) {
            for (final ContainerInfo containerInfo : runningConfig.containerSystem.containers) {
                containerIds.add(containerInfo.id);
            }
        }

        if (sys != null) {
            for (final ContainerInfo containerInfo : sys.containerSystem.containers) {
                containerIds.add(containerInfo.id);
            }

            // The only time we'd have one of these is if we were building
            // the above sys instance
            if (openejb != null) {
                for (final Container container : openejb.getContainer()) {
                    containerIds.add(container.getId());
                }
            }
        }

        return containerIds;
    }

    protected List<ContainerInfo> getContainerInfos() {
        final List<ContainerInfo> containers = new ArrayList<>();

        final OpenEjbConfiguration runningConfig = getRunningConfig();
        if (runningConfig != null) {
            containers.addAll(runningConfig.containerSystem.containers);
        }

        if (sys != null) {
            containers.addAll(sys.containerSystem.containers);
        }
        return containers;
    }

    private OpenEjbConfiguration getRunningConfig() {
        return SystemInstance.get().getComponent(OpenEjbConfiguration.class);
    }

    private static class TopicOrQueueDefaults {

        public static void process(final ServiceInfo provider) {
            if (!provider.service.equals("Resource")) {
                return;
            }
            if (!provider.types.contains("Topic") && !provider.types.contains("Queue")) {
                return;
            }
            if (!provider.className.matches("org.apache.activemq.command.ActiveMQ(Topic|Queue)")) {
                return;
            }

            final String dest = provider.properties.getProperty("destination");
            if (dest == null || dest.length() == 0) {
                provider.properties.setProperty("destination", provider.id);
            }
        }
    }

    public static List<ResourceInfo> sort(final List<ResourceInfo> infos, final String prefix) {
        final Collection<String> ids = new HashSet<>();
        return References.sort(infos, new References.Visitor<ResourceInfo>() {
            @Override // called first so we can rely on it to ensure we have ids full before any getReferences call
            public String getName(final ResourceInfo resourceInfo) {
                final String name = prefix != null && resourceInfo.id.startsWith(prefix) ? resourceInfo.id.substring(prefix.length()) : resourceInfo.id;
                ids.add(name);
                return name;
            }

            @Override
            public Set<String> getReferences(final ResourceInfo resourceInfo) {
                final Set<String> refs = new HashSet<>();
                for (final Object value : resourceInfo.properties.values()) {
                    if (!String.class.isInstance(value)) {
                        continue;
                    }
                    final String string = String.class.cast(value).trim();
                    if (string.isEmpty()) {
                        continue;
                    }
                    if (string.contains(",")) { // multiple references
                        for (final String s : string.split(",")) {
                            final String trim = s.trim();
                            if (ids.contains(trim)) {
                                refs.add(trim);
                            }
                        }
                    } else {
                        final String trim = String.valueOf(value).trim();
                        final String id = (trim.startsWith("@") || trim.startsWith("$")) && trim.length() > 1 ? trim.substring(1) : trim;
                        if (ids.contains(id)) {
                            refs.add(id);
                        }
                    }
                }
                refs.remove(getName(resourceInfo)); // can happen with serviceId for instance, avoid cicular dep issue
                refs.addAll(resourceInfo.dependsOn);
                return refs;
            }
        });
    }
}
