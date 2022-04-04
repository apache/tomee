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

package org.apache.openejb.assembler.classic;

import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.outbound.AbstractConnectionManager;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.connector.work.HintsContextHandler;
import org.apache.geronimo.connector.work.TransactionContextHandler;
import org.apache.geronimo.connector.work.WorkContextHandler;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.Container;
import org.apache.openejb.DeploymentContext;
import org.apache.openejb.DuplicateDeploymentIdException;
import org.apache.openejb.Extensions;
import org.apache.openejb.Injection;
import org.apache.openejb.JndiConstants;
import org.apache.openejb.MethodContext;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.api.jmx.MBean;
import org.apache.openejb.api.resource.DestroyableResource;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.assembler.classic.event.AssemblerCreated;
import org.apache.openejb.assembler.classic.event.AssemblerDestroyed;
import org.apache.openejb.assembler.classic.event.BeforeStartEjbs;
import org.apache.openejb.assembler.classic.event.ContainerSystemPostCreate;
import org.apache.openejb.assembler.classic.event.ContainerSystemPreDestroy;
import org.apache.openejb.assembler.classic.event.ResourceBeforeDestroyed;
import org.apache.openejb.assembler.classic.event.ResourceCreated;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.assembler.monitoring.JMXContainer;
import org.apache.openejb.async.AsynchronousPool;
import org.apache.openejb.batchee.BatchEEServiceManager;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.cdi.CdiPlugin;
import org.apache.openejb.cdi.CdiResourceInjectionService;
import org.apache.openejb.cdi.CdiScanner;
import org.apache.openejb.cdi.CustomELAdapter;
import org.apache.openejb.cdi.ManagedSecurityService;
import org.apache.openejb.cdi.OpenEJBBeanInfoService;
import org.apache.openejb.cdi.OpenEJBJndiService;
import org.apache.openejb.cdi.OpenEJBTransactionService;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.cdi.ThreadSingletonService;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.classloader.CompositeClassLoaderConfigurer;
import org.apache.openejb.component.ClassLoaderEnricher;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.Module;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.QuickJarsTxtParser;
import org.apache.openejb.config.TldScanner;
import org.apache.openejb.core.ConnectorReference;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.JndiFactory;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.SimpleTransactionSynchronizationRegistry;
import org.apache.openejb.core.TransactionSynchronizationRegistryWrapper;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.ContextHandler;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.core.ivm.naming.ContextualJndiReference;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.IvmJndiFactory;
import org.apache.openejb.core.ivm.naming.JndiUrlReference;
import org.apache.openejb.core.ivm.naming.LazyObjectReference;
import org.apache.openejb.core.ivm.naming.Reference;
import org.apache.openejb.core.security.SecurityContextHandler;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.timer.MemoryTimerStore;
import org.apache.openejb.core.timer.NullEjbTimerServiceImpl;
import org.apache.openejb.core.timer.ScheduleData;
import org.apache.openejb.core.timer.TimerStore;
import org.apache.openejb.core.transaction.JtaTransactionPolicyFactory;
import org.apache.openejb.core.transaction.SimpleBootstrapContext;
import org.apache.openejb.core.transaction.SimpleWorkManager;
import org.apache.openejb.core.transaction.TransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.javaagent.Agent;
import org.apache.openejb.jpa.integration.MakeTxLookup;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.MBeanPojoWrapper;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.remote.RemoteResourceMonitor;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.quartz.Scheduler;
import org.apache.openejb.resource.GeronimoConnectionManagerFactory;
import org.apache.openejb.resource.PropertiesFactory;
import org.apache.openejb.resource.jdbc.DataSourceFactory;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.threads.impl.ManagedExecutorServiceImpl;
import org.apache.openejb.util.Contexts;
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.ExecutorBuilder;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEJBErrorHandler;
import org.apache.openejb.util.PropertiesHelper;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.openejb.util.References;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.SetAccessible;
import org.apache.openejb.util.SuperProperties;
import org.apache.openejb.util.URISupport;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.classloader.ClassLoaderAwareHandler;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.openejb.util.proxy.ClassDefiner;
import org.apache.openejb.util.proxy.ProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.webbeans.component.ResourceBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.logger.JULLoggerFactory;
import org.apache.webbeans.service.ClassLoaderProxyService;
import org.apache.webbeans.spi.BeanArchiveService;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.DefiningClassService;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.spi.api.ResourceReference;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ClassLoaders;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.UnsetPropertiesRecipe;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.DefinitionException;
import javax.enterprise.inject.spi.DeploymentException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.openejb.util.Classes.ancestors;

@SuppressWarnings({"UnusedDeclaration", "UnqualifiedFieldAccess", "UnqualifiedMethodAccess"})
public class Assembler extends AssemblerTool implements org.apache.openejb.spi.Assembler, JndiConstants {

    static {
        // avoid linkage error on mac
        // adding just in case others run into in their tests
        JULLoggerFactory.class.getName();
    }

    public static final String OPENEJB_URL_PKG_PREFIX = IvmContext.class.getPackage().getName();
    public static final String OPENEJB_JPA_DEPLOY_TIME_ENHANCEMENT_PROP = "openejb.jpa.deploy-time-enhancement";
    public static final String PROPAGATE_APPLICATION_EXCEPTIONS = "openejb.propagate.application-exceptions";
    private static final String GLOBAL_UNIQUE_ID = "global";
    public static final String TIMER_STORE_CLASS = "timerStore.class";
    private static final ReentrantLock lock = new ReentrantLock(true);
    public static final String OPENEJB_TIMERS_ON = "openejb.timers.on";
    static final String FORCE_READ_ONLY_APP_NAMING = "openejb.forceReadOnlyAppNamingContext";

    public static final Class<?>[] VALIDATOR_FACTORY_INTERFACES = new Class<?>[]{ValidatorFactory.class, Serializable.class};
    public static final Class<?>[] VALIDATOR_INTERFACES = new Class<?>[]{Validator.class};
    private final boolean skipLoaderIfPossible;

    Messages messages = new Messages(Assembler.class.getPackage().getName());
    public final Logger logger;
    public final String resourceDestroyTimeout;
    public final boolean threadStackOnTimeout;
    private final CoreContainerSystem containerSystem;
    private final PersistenceClassLoaderHandler persistenceClassLoaderHandler;
    private final JndiBuilder jndiBuilder;
    private TransactionManager transactionManager;
    private SecurityService securityService;
    protected OpenEjbConfigurationFactory configFactory;
    private final Map<String, AppInfo> deployedApplications = new HashMap<>();
    private final Map<ObjectName, CreationalContext> creationalContextForAppMbeans = new HashMap<>();
    private final Set<ObjectName> containerObjectNames = new HashSet<>();
    private final RemoteResourceMonitor remoteResourceMonitor = new RemoteResourceMonitor();

    @Override
    public ContainerSystem getContainerSystem() {
        return containerSystem;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public SecurityService getSecurityService() {
        return securityService;
    }

    public void addDeploymentListener(final DeploymentListener deploymentListener) {

        final ReentrantLock l = lock;
        l.lock();

        try {
            logger.warning("DeploymentListener API is replaced by @Observes event");
            SystemInstance.get().addObserver(new DeploymentListenerObserver(deploymentListener));
        } finally {
            l.unlock();
        }
    }

    public void removeDeploymentListener(final DeploymentListener deploymentListener) {

        final ReentrantLock l = lock;
        l.lock();

        try {
            // the wrapping is done here to get the correct equals/hashcode methods
            SystemInstance.get().removeObserver(new DeploymentListenerObserver(deploymentListener));
        } finally {
            l.unlock();
        }
    }

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("Assembler");
    protected OpenEjbConfiguration config;

    public Assembler() {
        this(new IvmJndiFactory());
    }

    public Assembler(final JndiFactory jndiFactory) {
        logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, Assembler.class);
        skipLoaderIfPossible = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.classloader.skip-app-loader-if-possible", "true"));
        resourceDestroyTimeout = SystemInstance.get().getProperty("openejb.resources.destroy.timeout");
        threadStackOnTimeout = "true".equals(SystemInstance.get().getProperty("openejb.resources.destroy.stack-on-timeout", "false"));
        persistenceClassLoaderHandler = new PersistenceClassLoaderHandlerImpl();

        installNaming();

        final SystemInstance system = SystemInstance.get();

        system.setComponent(org.apache.openejb.spi.Assembler.class, this);
        system.setComponent(Assembler.class, this);

        containerSystem = new CoreContainerSystem(jndiFactory);
        system.setComponent(ContainerSystem.class, containerSystem);

        jndiBuilder = new JndiBuilder(containerSystem.getJNDIContext());

        setConfiguration(new OpenEjbConfiguration());

        final ApplicationServer appServer = system.getComponent(ApplicationServer.class);
        if (appServer == null) {
            system.setComponent(ApplicationServer.class, new ServerFederation());
        }

        system.setComponent(EjbResolver.class, new EjbResolver(null, EjbResolver.Scope.GLOBAL));

        installExtensions();

        system.fireEvent(new AssemblerCreated());

        initBValFiltering();
    }

    private void initBValFiltering() {
        if ("true".equals(SystemInstance.get().getProperty("openejb.cdi.bval.filter", "true"))) {
            try { // bval jars are optional so do it by reflection
                final ClassLoader loader = ParentClassLoaderFinder.Helper.get();
                final Object filter = loader.loadClass("org.apache.openejb.bval.BValCdiFilter").newInstance();
                loader.loadClass("org.apache.bval.cdi.BValExtension")
                        .getMethod(
                            "setAnnotatedTypeFilter",
                            loader.loadClass("org.apache.bval.cdi.BValExtension$AnnotatedTypeFilter"))
                        .invoke(null, filter);
            } catch (final Throwable th) {
                logger.warning("Can't setup BVal filtering, this can impact negatively performances: " + th.getMessage());
            }
        }
    }

    private void installExtensions() {
        try {
            final Collection<URL> urls = NewLoaderLogic.applyBuiltinExcludes(new UrlSet(Assembler.class.getClassLoader()).excludeJvm()).getUrls();
            Extensions.installExtensions(new Extensions.Finder("META-INF", false, urls.toArray(new URL[urls.size()])));
            return;
        } catch (final IOException e) {
            // no-op
        }

        // if an error occurred do it brutely
        Extensions.installExtensions(new Extensions.Finder("META-INF", true));
    }

    private void setConfiguration(final OpenEjbConfiguration config) {
        this.config = config;
        if (config.containerSystem == null) {
            config.containerSystem = new ContainerSystemInfo();
        }

        if (config.facilities == null) {
            config.facilities = new FacilitiesInfo();
        }

        SystemInstance.get().setComponent(OpenEjbConfiguration.class, this.config);
    }

    @Override
    public void init(final Properties props) throws OpenEJBException {
        this.props = new Properties(props);
        final Options options = new Options(props, SystemInstance.get().getOptions());
        final String className = options.get("openejb.configurator", "org.apache.openejb.config.ConfigurationFactory");

        if ("org.apache.openejb.config.ConfigurationFactory".equals(className)) {
            configFactory = new ConfigurationFactory(); // no need to use reflection
        } else {
            configFactory = (OpenEjbConfigurationFactory) toolkit.newInstance(className);
        }
        configFactory.init(props);
        SystemInstance.get().setComponent(OpenEjbConfigurationFactory.class, configFactory);
    }

    public static void installNaming() {
        if (SystemInstance.get().hasProperty("openejb.geronimo")) {
            return;
        }

        /* Add IntraVM JNDI service /////////////////////*/
        installNaming(OPENEJB_URL_PKG_PREFIX);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
    }

    public static void installNaming(final String prefix) {
        installNaming(prefix, false);
    }

    public static void installNaming(final String prefix, final boolean clean) {

        final ReentrantLock l = lock;
        l.lock();

        try {
            final Properties systemProperties = JavaSecurityManagers.getSystemProperties();

            String str = systemProperties.getProperty(Context.URL_PKG_PREFIXES);
            if (str == null || clean) {
                str = prefix;
            } else if (!str.contains(prefix)) {
                str = str + ":" + prefix;
            }
            systemProperties.setProperty(Context.URL_PKG_PREFIXES, str);
        } finally {
            l.unlock();
        }
    }

    private static final ThreadLocal<Map<String, Object>> context = new ThreadLocal<Map<String, Object>>();

    public static void setContext(final Map<String, Object> map) {
        context.set(map);
    }

    public static Map<String, Object> getContext() {
        Map<String, Object> map = context.get();
        if (map == null) {
            map = new HashMap<>();
            context.set(map);
        }
        return map;
    }

    @Override
    public void build() throws OpenEJBException {
        setContext(new HashMap<>());
        try {
            final OpenEjbConfiguration config = getOpenEjbConfiguration();
            buildContainerSystem(config);
        } catch (final OpenEJBException ae) {
            /* OpenEJBExceptions contain useful information and are debbugable.
             * Let the exception pass through to the top and be logged.
             */
            throw ae;
        } catch (final Exception e) {
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

    protected OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {
        return configFactory.getOpenEjbConfiguration();
    }

    /////////////////////////////////////////////////////////////////////
    ////
    ////    Public Methods Used for Assembly
    ////
    /////////////////////////////////////////////////////////////////////

    /**
     * When given a complete OpenEjbConfiguration graph this method
     * will construct an entire container system and return a reference to that
     * container system, as ContainerSystem instance.
     *
     * This method leverage the other assemble and apply methods which
     * can be used independently.
     *
     * Assembles and returns the {@link CoreContainerSystem} using the
     * information from the {@link OpenEjbConfiguration} object passed in.
     * <pre>
     * This method performs the following actions(in order):
     *
     * 1  Assembles ProxyFactory
     * 2  Assembles External JNDI Contexts
     * 3  Assembles TransactionService
     * 4  Assembles SecurityService
     * 5  Assembles ConnectionManagers
     * 6  Assembles Connectors
     * 7  Assembles Containers
     * 8  Assembles Applications
     * </pre>
     *
     * @param configInfo OpenEjbConfiguration
     * @throws Exception if there was a problem constructing the ContainerSystem.
     * @see OpenEjbConfiguration
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void buildContainerSystem(final OpenEjbConfiguration configInfo) throws Exception {
        final SystemInstance systemInstance = SystemInstance.get();
        if (systemInstance.getOptions().get(OPENEJB_JPA_DEPLOY_TIME_ENHANCEMENT_PROP, false)) {
            systemInstance.addObserver(new DeployTimeEnhancer());
        }
        if (hasBatchEE()) {
            systemInstance.addObserver(new BatchEEServiceManager());
        }

        for (final ServiceInfo serviceInfo : configInfo.facilities.services) {
            createService(serviceInfo);
        }

        final ContainerSystemInfo containerSystemInfo = configInfo.containerSystem;

        if (configInfo.facilities.intraVmServer != null) {
            createProxyFactory(configInfo.facilities.intraVmServer);
        }

        for (final JndiContextInfo contextInfo : configInfo.facilities.remoteJndiContexts) {
            createExternalContext(contextInfo);
        }

        createTransactionManager(configInfo.facilities.transactionService);

        createSecurityService(configInfo.facilities.securityService);

        final Set<String> reservedResourceIds = new HashSet<>(configInfo.facilities.resources.size());
        for (final AppInfo appInfo : containerSystemInfo.applications) {
            reservedResourceIds.addAll(appInfo.resourceIds);
        }

        final Map<AppInfo, ClassLoader> appInfoClassLoaders = new HashMap<>();
        final Map<String, ClassLoader> appClassLoaders = new HashMap<>();

        for (final AppInfo appInfo : containerSystemInfo.applications) {
            appInfoClassLoaders.put(appInfo, createAppClassLoader(appInfo));
            appClassLoaders.put(appInfo.appId, createAppClassLoader(appInfo));
        }

        final Set<String> rIds = new HashSet<>(configInfo.facilities.resources.size());
        for (final ResourceInfo resourceInfo : configInfo.facilities.resources) {
            createResource(configInfo.facilities.services, resourceInfo);
            rIds.add(resourceInfo.id);
        }
        rIds.removeAll(reservedResourceIds);
        final ContainerSystem component = systemInstance.getComponent(ContainerSystem.class);
        if (component != null) {
            postConstructResources(rIds, ParentClassLoaderFinder.Helper.get(), component.getJNDIContext(), null);
        }else {
            throw new RuntimeException("ContainerSystem has not been initialzed");
        }

        // Containers - create containers using the application's classloader
        final Map<String, List<ContainerInfo>> appContainers = new HashMap<>();

        for (final ContainerInfo serviceInfo : containerSystemInfo.containers) {
            List<ContainerInfo> containerInfos = appContainers.computeIfAbsent(serviceInfo.originAppName, k -> new ArrayList<>());

            containerInfos.add(serviceInfo);
        }

        for (final Entry<String, List<ContainerInfo>> stringListEntry : appContainers.entrySet()) {
            final List<ContainerInfo> containerInfos = stringListEntry.getValue();
            final ClassLoader classLoader = appClassLoaders.get(stringListEntry.getKey());

            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

            try {
                if (classLoader != null) {
                    Thread.currentThread().setContextClassLoader(classLoader);
                }

                for (final ContainerInfo containerInfo : containerInfos) {
                    createContainer(containerInfo);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }

        createJavaGlobal(); // before any deployment bind global to be able to share the same context

        for (final AppInfo appInfo : containerSystemInfo.applications) {

            try {
                createApplication(appInfo, appInfoClassLoaders.get(appInfo)); // use the classloader from the map above
            } catch (final DuplicateDeploymentIdException e) {
                // already logged.
            } catch (final Throwable e) {
                logger.error("appNotDeployed", e, appInfo.path);

                final DeploymentExceptionManager exceptionManager = systemInstance.getComponent(DeploymentExceptionManager.class);
                if (exceptionManager != null && e instanceof Exception) {
                    exceptionManager.saveDeploymentException(appInfo, (Exception) e);
                }
            }
        }

        systemInstance.fireEvent(new ContainerSystemPostCreate());
    }

    private static boolean hasBatchEE() {
        try {
            Class.forName("org.apache.batchee.container.services.ServicesManager", true, Assembler.class.getClassLoader());
            return true;
        } catch (final Throwable e) {
            return false;
        }
    }

    private void createJavaGlobal() {
        try {
            containerSystem.getJNDIContext().createSubcontext("global");
        } catch (final NamingException e) {
            // no-op
        }
    }

    public AppInfo getAppInfo(final String path) {
        return deployedApplications.get(ProvisioningUtil.realLocation(path).iterator().next());
    }

    public boolean isDeployed(final String path) {
        return deployedApplications.containsKey(ProvisioningUtil.realLocation(path).iterator().next());
    }

    public Collection<AppInfo> getDeployedApplications() {
        return new ArrayList<>(deployedApplications.values());
    }

    public AppContext createApplication(final EjbJarInfo ejbJar) throws NamingException, IOException, OpenEJBException {
        return createEjbJar(ejbJar);
    }

    public AppContext createEjbJar(final EjbJarInfo ejbJar) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = ejbJar.path;
        appInfo.appId = ejbJar.moduleName;
        appInfo.ejbJars.add(ejbJar);
        return createApplication(appInfo);
    }

    public AppContext createApplication(final EjbJarInfo ejbJar, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        return createEjbJar(ejbJar, classLoader);
    }

    public AppContext createEjbJar(final EjbJarInfo ejbJar, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = ejbJar.path;
        appInfo.appId = ejbJar.moduleName;
        appInfo.ejbJars.add(ejbJar);
        return createApplication(appInfo, classLoader);
    }

    public AppContext createClient(final ClientInfo clientInfo) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = clientInfo.path;
        appInfo.appId = clientInfo.moduleId;
        appInfo.clients.add(clientInfo);
        return createApplication(appInfo);
    }

    public AppContext createClient(final ClientInfo clientInfo, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = clientInfo.path;
        appInfo.appId = clientInfo.moduleId;
        appInfo.clients.add(clientInfo);
        return createApplication(appInfo, classLoader);
    }

    public AppContext createConnector(final ConnectorInfo connectorInfo) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = connectorInfo.path;
        appInfo.appId = connectorInfo.moduleId;
        appInfo.connectors.add(connectorInfo);
        return createApplication(appInfo);
    }

    public AppContext createConnector(final ConnectorInfo connectorInfo, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = connectorInfo.path;
        appInfo.appId = connectorInfo.moduleId;
        appInfo.connectors.add(connectorInfo);
        return createApplication(appInfo, classLoader);
    }

    public AppContext createWebApp(final WebAppInfo webAppInfo) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = webAppInfo.path;
        appInfo.appId = webAppInfo.moduleId;
        appInfo.webApps.add(webAppInfo);
        return createApplication(appInfo);
    }

    public AppContext createWebApp(final WebAppInfo webAppInfo, final ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        final AppInfo appInfo = new AppInfo();
        appInfo.path = webAppInfo.path;
        appInfo.appId = webAppInfo.moduleId;
        appInfo.webApps.add(webAppInfo);
        return createApplication(appInfo, classLoader);
    }

    public AppContext createApplication(final AppInfo appInfo) throws OpenEJBException, IOException, NamingException {
        return createApplication(appInfo, createAppClassLoader(appInfo));
    }

    public AppContext createApplication(final AppInfo appInfo, final ClassLoader classLoader) throws OpenEJBException, IOException, NamingException {
        return createApplication(appInfo, classLoader, true);
    }

    private AppContext createApplication(final AppInfo appInfo, ClassLoader classLoader, final boolean start) throws OpenEJBException, IOException, NamingException {
        try {
            try {
                mergeServices(appInfo);
            } catch (final URISyntaxException e) {
                logger.info("Can't merge resources.xml services and appInfo.properties");
            }

            // The path is used in the UrlCache, command line deployer, JNDI name templates, tomcat integration and a few other places
            if (appInfo.appId == null) {
                throw new IllegalArgumentException("AppInfo.appId cannot be null");
            }
            if (appInfo.path == null) {
                appInfo.path = appInfo.appId;
            }

            Extensions.addExtensions(classLoader, appInfo.eventClassesNeedingAppClassloader);
            logger.info("createApplication.start", appInfo.path);
            final Context containerSystemContext = containerSystem.getJNDIContext();

            // To start out, ensure we don't already have any beans deployed with duplicate IDs.  This
            // is a conflict we can't handle.
            final List<String> used = getDuplicates(appInfo);

            if (used.size() > 0) {
                StringBuilder message = new StringBuilder(logger.error("createApplication.appFailedDuplicateIds"
                        , appInfo.path));
                for (final String id : used) {
                    logger.error("createApplication.deploymentIdInUse", id);
                    message.append("\n    ").append(id);
                }
                throw new DuplicateDeploymentIdException(message.toString());
            }

            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                for (final ContainerInfo container : appInfo.containers) {
                    createContainer(container);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }

            //Construct the global and app jndi contexts for this app
            final InjectionBuilder injectionBuilder = new InjectionBuilder(classLoader);

            final Set<Injection> injections = new HashSet<>();
            injections.addAll(injectionBuilder.buildInjections(appInfo.globalJndiEnc));
            injections.addAll(injectionBuilder.buildInjections(appInfo.appJndiEnc));

            final JndiEncBuilder globalBuilder = new JndiEncBuilder(appInfo.globalJndiEnc, injections, appInfo.appId, null, GLOBAL_UNIQUE_ID, classLoader, appInfo.properties);
            final Map<String, Object> globalBindings = globalBuilder.buildBindings(JndiEncBuilder.JndiScope.global);
            final Context globalJndiContext = globalBuilder.build(globalBindings);

            final JndiEncBuilder appBuilder = new JndiEncBuilder(appInfo.appJndiEnc, injections, appInfo.appId, null, appInfo.appId, classLoader, appInfo.properties);
            final Map<String, Object> appBindings = appBuilder.buildBindings(JndiEncBuilder.JndiScope.app);
            final Context appJndiContext = appBuilder.build(appBindings);

            final boolean cdiActive = shouldStartCdi(appInfo);

            try {
                // Generate the cmp2/cmp1 concrete subclasses
                final CmpJarBuilder cmpJarBuilder = new CmpJarBuilder(appInfo, classLoader);
                final File generatedJar = cmpJarBuilder.getJarFile();
                if (generatedJar != null) {
                    classLoader = ClassLoaderUtil.createClassLoader(appInfo.path, new URL[]{generatedJar.toURI().toURL()}, classLoader);
                }

                final AppContext appContext = new AppContext(appInfo.appId, SystemInstance.get(), classLoader, globalJndiContext, appJndiContext, appInfo.standaloneModule);
                for (final Entry<Object, Object> entry : appInfo.properties.entrySet()) {
                    if (! Module.class.isInstance(entry.getValue())) {
                        appContext.getProperties().put(entry.getKey(), entry.getValue());
                    }
                }

                appContext.getInjections().addAll(injections);
                appContext.getBindings().putAll(globalBindings);
                appContext.getBindings().putAll(appBindings);

                containerSystem.addAppContext(appContext);

                appContext.set(AsynchronousPool.class, AsynchronousPool.create(appContext));

                final Map<String, LazyValidatorFactory> lazyValidatorFactories = new HashMap<>();
                final Map<String, LazyValidator> lazyValidators = new HashMap<>();
                final boolean isGeronimo = SystemInstance.get().hasProperty("openejb.geronimo");

                // try to not create N times the same validator for a single app
                final Map<ComparableValidationConfig, ValidatorFactory> validatorFactoriesByConfig = new HashMap<>();
                if (!isGeronimo) {
                    // Bean Validation
                    // ValidatorFactory needs to be put in the map sent to the entity manager factory
                    // so it has to be constructed before
                    final List<CommonInfoObject> vfs = listCommonInfoObjectsForAppInfo(appInfo);
                    final Map<String, ValidatorFactory> validatorFactories = new HashMap<>();

                    for (final CommonInfoObject info : vfs) {
                        if (info.validationInfo == null) {
                            continue;
                        }

                        final ComparableValidationConfig conf = new ComparableValidationConfig(
                                info.validationInfo.providerClassName, info.validationInfo.messageInterpolatorClass,
                                info.validationInfo.traversableResolverClass, info.validationInfo.constraintFactoryClass,
                                info.validationInfo.parameterNameProviderClass, info.validationInfo.version,
                                info.validationInfo.propertyTypes, info.validationInfo.constraintMappings,
                                info.validationInfo.executableValidationEnabled, info.validationInfo.validatedTypes
                        );
                        ValidatorFactory factory = validatorFactoriesByConfig.get(conf);
                        if (factory == null) {
                            try { // lazy cause of CDI :(
                                final LazyValidatorFactory handler = new LazyValidatorFactory(classLoader, info.validationInfo);
                                factory = (ValidatorFactory) Proxy.newProxyInstance(
                                        appContext.getClassLoader(), VALIDATOR_FACTORY_INTERFACES, handler);
                                lazyValidatorFactories.put(info.uniqueId, handler);
                            } catch (final ValidationException ve) {
                                logger.warning("can't build the validation factory for module " + info.uniqueId, ve);
                                continue;
                            }
                            validatorFactoriesByConfig.put(conf, factory);
                        } else {
                            lazyValidatorFactories.put(info.uniqueId, LazyValidatorFactory.class.cast(Proxy.getInvocationHandler(factory)));
                        }
                        validatorFactories.put(info.uniqueId, factory);
                    }

                    // validators bindings
                    for (final Entry<String, ValidatorFactory> validatorFactory : validatorFactories.entrySet()) {
                        final String id = validatorFactory.getKey();
                        final ValidatorFactory factory = validatorFactory.getValue();
                        try {
                            containerSystemContext.bind(VALIDATOR_FACTORY_NAMING_CONTEXT + id, factory);

                            final Validator validator;
                            try {
                                final LazyValidator lazyValidator = new LazyValidator(factory);
                                validator = (Validator) Proxy.newProxyInstance(appContext.getClassLoader(), VALIDATOR_INTERFACES, lazyValidator);
                                lazyValidators.put(id, lazyValidator);
                            } catch (final Exception e) {
                                logger.error(e.getMessage(), e);
                                continue;
                            }

                            containerSystemContext.bind(VALIDATOR_NAMING_CONTEXT + id, validator);
                        } catch (final NameAlreadyBoundException e) {
                            throw new OpenEJBException("ValidatorFactory already exists for module " + id, e);
                        } catch (final Exception e) {
                            throw new OpenEJBException(e);
                        }
                    }

                    validatorFactories.clear();
                }

                // JPA - Persistence Units MUST be processed first since they will add ClassFileTransformers
                // to the class loader which must be added before any classes are loaded
                final Map<String, String> units = new HashMap<>();
                final PersistenceBuilder persistenceBuilder = new PersistenceBuilder(persistenceClassLoaderHandler);
                for (final PersistenceUnitInfo info : appInfo.persistenceUnits) {
                    final ReloadableEntityManagerFactory factory;
                    try {
                        factory = persistenceBuilder.createEntityManagerFactory(info, classLoader, validatorFactoriesByConfig, cdiActive);
                        containerSystem.getJNDIContext().bind(PERSISTENCE_UNIT_NAMING_CONTEXT + info.id, factory);
                        units.put(info.name, PERSISTENCE_UNIT_NAMING_CONTEXT + info.id);
                    } catch (final NameAlreadyBoundException e) {
                        throw new OpenEJBException("PersistenceUnit already deployed: " + info.persistenceUnitRootUrl);
                    } catch (final Exception e) {
                        throw new OpenEJBException(e);
                    }

                    factory.register();
                }

                logger.debug("Loaded persistence units: " + units);

                // Connectors
                for (final ConnectorInfo connector : appInfo.connectors) {
                    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(classLoader);
                    try {
                        // todo add undeployment code for these
                        if (connector.resourceAdapter != null) {
                            createResource(null, connector.resourceAdapter);
                        }
                        for (final ResourceInfo outbound : connector.outbound) {
                            createResource(null, outbound);
                            outbound.properties.setProperty("openejb.connector", "true"); // set it after as a marker but not as an attribute (no getOpenejb().setConnector(...))
                        }
                        for (final MdbContainerInfo inbound : connector.inbound) {
                            createContainer(inbound);
                        }
                        for (final ResourceInfo adminObject : connector.adminObject) {
                            createResource(null, adminObject);
                        }
                    } finally {
                        Thread.currentThread().setContextClassLoader(oldClassLoader);
                    }
                }

                final List<BeanContext> allDeployments = initEjbs(classLoader, appInfo, appContext, injections, new ArrayList<>(), null);

                if ("true".equalsIgnoreCase(SystemInstance.get()
                        .getProperty(PROPAGATE_APPLICATION_EXCEPTIONS,
                                appInfo.properties.getProperty(PROPAGATE_APPLICATION_EXCEPTIONS, "false")))) {
                    propagateApplicationExceptions(appInfo, classLoader, allDeployments);
                }

                if (cdiActive) {
                    new CdiBuilder().build(appInfo, appContext, allDeployments);
                    ensureWebBeansContext(appContext);
                    appJndiContext.bind("app/BeanManager", appContext.getBeanManager());
                    appContext.getBindings().put("app/BeanManager", appContext.getBeanManager());
                } else { // ensure we can reuse it in tomcat to remove OWB filters
                    appInfo.properties.setProperty("openejb.cdi.activated", "false");
                }

                // now cdi is started we can try to bind real validator factory and validator
                if (!isGeronimo) {
                    for (final Entry<String, LazyValidator> lazyValidator : lazyValidators.entrySet()) {
                        final String id = lazyValidator.getKey();
                        final ValidatorFactory factory = lazyValidatorFactories.get(lazyValidator.getKey()).getFactory();
                        try {
                            final String factoryName = VALIDATOR_FACTORY_NAMING_CONTEXT + id;
                            containerSystemContext.unbind(factoryName);
                            containerSystemContext.bind(factoryName, factory);

                            final String validatoryName = VALIDATOR_NAMING_CONTEXT + id;
                            try { // do it after factory cause of TCKs which expects validator to be created later
                                final Validator val = lazyValidator.getValue().getValidator();
                                containerSystemContext.unbind(validatoryName);
                                containerSystemContext.bind(validatoryName, val);
                            } catch (final Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        } catch (final NameAlreadyBoundException e) {
                            throw new OpenEJBException("ValidatorFactory already exists for module " + id, e);
                        } catch (final Exception e) {
                            throw new OpenEJBException(e);
                        }
                    }
                }

                startEjbs(start, allDeployments);

                // App Client
                for (final ClientInfo clientInfo : appInfo.clients) {
                    // determine the injections
                    final List<Injection> clientInjections = injectionBuilder.buildInjections(clientInfo.jndiEnc);

                    // build the enc
                    final JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(clientInfo.jndiEnc, clientInjections, "Bean", clientInfo.moduleId, null, clientInfo.uniqueId, classLoader, new Properties());
                    // if there is at least a remote client classes
                    // or if there is no local client classes
                    // then, we can set the client flag
                    if (clientInfo.remoteClients.size() > 0 || clientInfo.localClients.size() == 0) {
                        jndiEncBuilder.setClient(true);

                    }
                    jndiEncBuilder.setUseCrossClassLoaderRef(false);
                    final Context context = jndiEncBuilder.build(JndiEncBuilder.JndiScope.comp);

                    //                Debug.printContext(context);

                    containerSystemContext.bind("openejb/client/" + clientInfo.moduleId, context);

                    if (clientInfo.path != null) {
                        context.bind("info/path", clientInfo.path);
                    }
                    if (clientInfo.mainClass != null) {
                        context.bind("info/mainClass", clientInfo.mainClass);
                    }
                    if (clientInfo.callbackHandler != null) {
                        context.bind("info/callbackHandler", clientInfo.callbackHandler);
                    }
                    context.bind("info/injections", clientInjections);

                    for (final String clientClassName : clientInfo.remoteClients) {
                        containerSystemContext.bind("openejb/client/" + clientClassName, clientInfo.moduleId);
                    }

                    for (final String clientClassName : clientInfo.localClients) {
                        containerSystemContext.bind("openejb/client/" + clientClassName, clientInfo.moduleId);
                        logger.getChildLogger("client").info("createApplication.createLocalClient", clientClassName, clientInfo.moduleId);
                    }
                }

                // WebApp
                final SystemInstance systemInstance = SystemInstance.get();

                final WebAppBuilder webAppBuilder = systemInstance.getComponent(WebAppBuilder.class);
                if (webAppBuilder != null) {
                    webAppBuilder.deployWebApps(appInfo, classLoader);
                }

                if (start) {
                    final EjbResolver globalEjbResolver = systemInstance.getComponent(EjbResolver.class);
                    globalEjbResolver.addAll(appInfo.ejbJars);
                }

                // bind all global values on global context
                bindGlobals(appContext.getBindings());

                validateCdiResourceProducers(appContext, appInfo);

                // deploy MBeans
                for (final String mbean : appInfo.mbeans) {
                    deployMBean(appContext.getWebBeansContext(), classLoader, mbean, appInfo.jmx, appInfo.appId);
                }
                for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
                    for (final String mbean : ejbJarInfo.mbeans) {
                        deployMBean(appContext.getWebBeansContext(), classLoader, mbean, appInfo.jmx, ejbJarInfo.moduleName);
                    }
                }
                for (final ConnectorInfo connectorInfo : appInfo.connectors) {
                    for (final String mbean : connectorInfo.mbeans) {
                        deployMBean(appContext.getWebBeansContext(), classLoader, mbean, appInfo.jmx, appInfo.appId + ".add-lib");
                    }
                }

                postConstructResources(appInfo.resourceIds, classLoader, containerSystemContext, appContext);

                deployedApplications.put(appInfo.path, appInfo);
                resumePersistentSchedulers(appContext);

                systemInstance.fireEvent(new AssemblerAfterApplicationCreated(appInfo, appContext, allDeployments));
                logger.info("createApplication.success", appInfo.path);

                //required by spec EE.5.3.4
                if(setAppNamingContextReadOnly(allDeployments)) {
                    logger.info("createApplication.naming", appInfo.path);
                }

                return appContext;
            } catch (final ValidationException | DeploymentException ve) {
                throw ve;
            } catch (final Throwable t) {
                try {
                    destroyApplication(appInfo);
                } catch (final Exception e1) {
                    logger.debug("createApplication.undeployFailed", e1, appInfo.path);
                }
                throw new OpenEJBException(messages.format("createApplication.failed", appInfo.path), t);
            }
        } finally {
            // cleanup there as well by safety cause we have multiple deployment mode (embedded, tomcat...)
            for (final WebAppInfo webApp : appInfo.webApps) {
                appInfo.properties.remove(webApp);
            }
        }
    }

    boolean setAppNamingContextReadOnly(final List<BeanContext> allDeployments) {
        if("true".equals(SystemInstance.get().getProperty(FORCE_READ_ONLY_APP_NAMING, "false"))) {
            for(BeanContext beanContext : allDeployments) {
                Context ctx = beanContext.getJndiContext();
             
                if(IvmContext.class.isInstance(ctx)) {
                    IvmContext.class.cast(ctx).setReadOnly(true);
                } else if(ContextHandler.class.isInstance(ctx)) {
                    ContextHandler.class.cast(ctx).setReadOnly();
                }
            }
            return true;
        }
        return false;
    }

    private List<String> getDuplicates(final AppInfo appInfo) {
        final List<String> used = new ArrayList<>();
        for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            for (final EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
                if (containerSystem.getBeanContext(beanInfo.ejbDeploymentId) != null) {
                    used.add(beanInfo.ejbDeploymentId);
                }
            }
        }
        return used;
    }

    private boolean shouldStartCdi(final AppInfo appInfo) {
        if (!"true".equalsIgnoreCase(appInfo.properties.getProperty("openejb.cdi.activated", "true"))) {
            return false;
        }
        for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            if (ejbJarInfo.beans != null
                    && (!ejbJarInfo.beans.bdas.isEmpty() || !ejbJarInfo.beans.noDescriptorBdas.isEmpty())) {
                return true;
            }
        }
        return false;
    }

    private void validateCdiResourceProducers(final AppContext appContext, final AppInfo info) {
        if (appContext.getWebBeansContext() == null) {
            return;
        }
        // validate @Produces @Resource/@PersistenceX/@EJB once all is bound to JNDI - best case - or with our model
        if (appContext.isStandaloneModule() && !appContext.getProperties().containsKey("openejb.cdi.skip-resource-validation")) {
            final Map<String, Object> bindings =
                    appContext.getWebContexts().isEmpty() ? appContext.getBindings() : appContext.getWebContexts().iterator().next().getBindings();
            if (bindings != null && appContext.getWebBeansContext() != null && appContext.getWebBeansContext().getBeanManagerImpl().isInUse()) {
                for (final Bean<?> bean : appContext.getWebBeansContext().getBeanManagerImpl().getBeans()) {
                    if (ResourceBean.class.isInstance(bean)) {
                        final ResourceReference reference = ResourceBean.class.cast(bean).getReference();
                        String jndi = reference.getJndiName().replace("java:", "");
                        if (reference.getJndiName().startsWith("java:/")) {
                            jndi = jndi.substring(1);
                        }
                        Object lookup = bindings.get(jndi);
                        if (lookup == null && reference.getAnnotation(EJB.class) != null) {
                            final CdiPlugin plugin = CdiPlugin.class.cast(appContext.getWebBeansContext().getPluginLoader().getEjbPlugin());
                            if (!plugin.isSessionBean(reference.getResourceType())) { // local beans are here and access is O(1) instead of O(n)
                                boolean ok = false;
                                for (final BeanContext bc : appContext.getBeanContexts()) {
                                    if (bc.getBusinessLocalInterfaces().contains(reference.getResourceType())
                                            || bc.getBusinessRemoteInterfaces().contains(reference.getResourceType())) {
                                        ok = true;
                                        break;
                                    }
                                }
                                if (!ok) {
                                    throw new DefinitionException(
                                            "EJB " + reference.getJndiName() + " in " + reference.getOwnerClass() + " can't be cast to " + reference.getResourceType());
                                }
                            }
                        }
                        if (Reference.class.isInstance(lookup)) {
                            try {
                                lookup = Reference.class.cast(lookup).getContent();
                            } catch (final Exception e) { // surely too early, let's try some known locations
                                if (JndiUrlReference.class.isInstance(lookup)) {
                                    checkBuiltInResourceTypes(reference, JndiUrlReference.class.cast(lookup).getJndiName());
                                }
                                continue;
                            }
                        } else if (lookup == null) { // TODO: better validation with lookups in tomee, should be in TWAB surely but would split current code
                            final Resource r = Resource.class.cast(reference.getAnnotation(Resource.class));
                            if (r != null) {
                                if (!r.lookup().isEmpty()) {
                                    checkBuiltInResourceTypes(reference, r.lookup());
                                } else if (!r.name().isEmpty()) {
                                    final String name = "comp/env/" + r.name();
                                    boolean done = false;
                                    for (final WebAppInfo w : info.webApps) {
                                        for (final EnvEntryInfo e : w.jndiEnc.envEntries) {
                                            if (name.equals(e.referenceName)) {
                                                if (e.type != null && !reference.getResourceType().getName().equals(e.type)) {
                                                    throw new DefinitionException(
                                                            "Env Entry " + reference.getJndiName() + " in " + reference.getOwnerClass() + " can't be cast to " + reference.getResourceType());
                                                }
                                                done = true;
                                                break;
                                            }
                                        }
                                        if (done) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (lookup != null && !reference.getResourceType().isInstance(lookup)) {
                            throw new DefinitionException(
                                    "Resource " + reference.getJndiName() + " in " + reference.getOwnerClass() + " can't be cast, instance is " + lookup);
                        }
                    }
                }
            }
        }
    }

    private void checkBuiltInResourceTypes(final ResourceReference reference, final String jndi) {
        final Class<?> resourceType = reference.getResourceType();
        if ("java:comp/BeanManager".equals(jndi) && resourceType != BeanManager.class) {
            throw new DefinitionException(
                    "Resource " + reference.getJndiName() + " in " + reference.getOwnerClass() + " can't be cast to a BeanManager");
        } else if ("java:comp/TransactionSynchronizationRegistry".equals(jndi) && resourceType != TransactionSynchronizationRegistry.class) {
            throw new DefinitionException(
                    "Resource " + reference.getJndiName() + " in " + reference.getOwnerClass() + " can't be cast to a TransactionSynchronizationRegistry");
        } else if ("java:comp/TransactionManager".equals(jndi) && resourceType != TransactionManager.class) {
            throw new DefinitionException(
                    "Resource " + reference.getJndiName() + " in " + reference.getOwnerClass() + " can't be cast to a TransactionManager");
        } else if ("java:comp/ValidatorFactory".equals(jndi) && resourceType != ValidatorFactory.class) {
            throw new DefinitionException(
                    "Resource " + reference.getJndiName() + " in " + reference.getOwnerClass() + " can't be cast to a ValidatorFactory");
        } else if ("java:comp/Validator".equals(jndi) && resourceType != Validator.class) {
            throw new DefinitionException(
                    "Resource " + reference.getJndiName() + " in " + reference.getOwnerClass() + " can't be cast to a Validator");
        }
    }

    private void postConstructResources(
            final Set<String> resourceIds, final ClassLoader classLoader,
            final Context containerSystemContext, final AppContext appContext) throws NamingException, OpenEJBException {
        final Thread thread = Thread.currentThread();
        final ClassLoader oldCl = thread.getContextClassLoader();

        try {
            thread.setContextClassLoader(classLoader);

            final List<ResourceInfo> resourceList = config.facilities.resources;

            for (final ResourceInfo resourceInfo : resourceList) {
                if (!resourceIds.contains(resourceInfo.id)) {
                    continue;
                }
                if (isTemplatizedResource(resourceInfo)) {
                    continue;
                }

                try {
                    Class<?> clazz;
                    try {
                        clazz = classLoader.loadClass(resourceInfo.className);
                    } catch (final ClassNotFoundException cnfe) { // custom classpath
                        clazz = containerSystemContext.lookup(OPENEJB_RESOURCE_JNDI_PREFIX + resourceInfo.id).getClass();
                    }

                    final boolean initialize = "true".equalsIgnoreCase(String.valueOf(resourceInfo.properties.remove("InitializeAfterDeployment")));
                    final AnnotationFinder finder = Proxy.isProxyClass(clazz) ?
                            null : new AnnotationFinder(new ClassesArchive(ancestors(clazz)));
                    final List<Method> postConstructs = finder == null ?
                            Collections.<Method>emptyList() : finder.findAnnotatedMethods(PostConstruct.class);
                    final List<Method> preDestroys = finder == null ?
                            Collections.<Method>emptyList() : finder.findAnnotatedMethods(PreDestroy.class);

                    resourceInfo.postConstructMethods = new ArrayList<>();
                    resourceInfo.preDestroyMethods = new ArrayList<>();

                    addMethodsToResourceInfo(resourceInfo.postConstructMethods, PostConstruct.class, postConstructs);
                    addMethodsToResourceInfo(resourceInfo.preDestroyMethods, PreDestroy.class, preDestroys);

                    CreationalContext<?> creationalContext = null;
                    Object originalResource = null;
                    if (!postConstructs.isEmpty() || initialize) {
                        originalResource = containerSystemContext.lookup(OPENEJB_RESOURCE_JNDI_PREFIX + resourceInfo.id);
                        Object resource = originalResource;
                        if (resource instanceof Reference) {
                            resource = unwrapReference(resource);
                            this.bindResource(resourceInfo.id, resource, true);
                        }

                        try {
                            // wire up CDI
                            if (appContext != null && appContext.getWebBeansContext() != null) {
                                final BeanManagerImpl beanManager = appContext.getWebBeansContext().getBeanManagerImpl();
                                if (beanManager.isInUse()) {
                                    creationalContext = beanManager.createCreationalContext(null);
                                    OWBInjector.inject(beanManager, resource, creationalContext);
                                }
                            }

                            if (!"none".equals(resourceInfo.postConstruct)) {
                                if (resourceInfo.postConstruct != null) {
                                    final Method p = clazz.getDeclaredMethod(resourceInfo.postConstruct);
                                    if (!p.isAccessible()) {
                                        SetAccessible.on(p);
                                    }
                                    p.invoke(resource);
                                }

                                for (final Method m : postConstructs) {
                                    if (!m.isAccessible()) {
                                        SetAccessible.on(m);
                                    }
                                    m.invoke(resource);
                                }
                            }
                        } catch (final Exception e) {
                            logger.fatal("Error calling @PostConstruct method on " + resource.getClass().getName());
                            throw new OpenEJBException(e);
                        }
                    }

                    if (!"none".equals(resourceInfo.preDestroy)) {
                        if (resourceInfo.preDestroy != null) {
                            final Method p = clazz.getDeclaredMethod(resourceInfo.preDestroy);
                            if (!p.isAccessible()) {
                                SetAccessible.on(p);
                            }
                            preDestroys.add(p);
                        }

                        if (!preDestroys.isEmpty() || creationalContext != null) {
                            final String name = OPENEJB_RESOURCE_JNDI_PREFIX + resourceInfo.id;
                            if (originalResource == null) {
                                originalResource = containerSystemContext.lookup(name);
                            }

                            this.bindResource(resourceInfo.id, new ResourceInstance(name, originalResource, preDestroys, creationalContext), true);
                        }
                    }

                    // log unused now for these resources now we built the resource completely and @PostConstruct can have used injected properties
                    if (resourceInfo.unsetProperties != null && !isPassthroughType(resourceInfo)) {
                        final Set<String> unsetKeys = resourceInfo.unsetProperties.stringPropertyNames();
                        for (final String key : unsetKeys) { // don't use keySet to auto filter txMgr for instance and not real properties!
                            unusedProperty(resourceInfo.id, logger, key);
                        }
                    }
                } catch (final Exception e) {
                    logger.fatal("Error calling PostConstruct method on " + resourceInfo.id);
                    logger.fatal("Resource " + resourceInfo.id + " could not be initialized. Application will be undeployed.");
                    throw new OpenEJBException(e);
                }
            }
        } finally {
            thread.setContextClassLoader(oldCl);
        }
    }

    private void addMethodsToResourceInfo(final List<String> list, final Class type, final List<Method> methodList) throws OpenEJBException {
        for (final Method method : methodList) {
            if (method.getParameterTypes().length > 0) {
                throw new OpenEJBException(type.getSimpleName() + " method " +
                        method.getDeclaringClass().getName() + "."
                        + method.getName() + " should have zero arguments");
            }

            list.add(method.getName());
        }
    }

    private static boolean isTemplatizedResource(final ResourceInfo resourceInfo) { // ~ container resource even if not 100% right
        return resourceInfo.className == null || resourceInfo.className.isEmpty();
    }

    public static void mergeServices(final AppInfo appInfo) throws URISyntaxException {
        // used lazily by JaxWsServiceObjectFactory so merge both to keep same config
        // note: we could do the same for resources
        for (final ServiceInfo si : appInfo.services) {
            if (!appInfo.properties.containsKey(si.id)) {
                final Map<String, String> query = new HashMap<>();
                if (si.types != null && !si.types.isEmpty()) {
                    query.put("type", si.types.iterator().next());
                }
                if (si.className != null) {
                    query.put("class-name", si.className);
                }
                if (si.factoryMethod != null) {
                    query.put("factory-name", si.factoryMethod);
                }
                if (si.constructorArgs != null && !si.constructorArgs.isEmpty()) {
                    query.put("constructor", Join.join(",", si.constructorArgs));
                }
                appInfo.properties.put(si.id, "new://Service?" + URISupport.createQueryString(query));
                if (si.properties != null) {
                    for (final String k : si.properties.stringPropertyNames()) {
                        appInfo.properties.setProperty(si.id + "." + k, si.properties.getProperty(k));
                    }
                }
            }
        }
    }

    private static List<CommonInfoObject> listCommonInfoObjectsForAppInfo(final AppInfo appInfo) {
        final List<CommonInfoObject> vfs = new ArrayList<>(
            appInfo.clients.size() + appInfo.connectors.size() +
                appInfo.ejbJars.size() + appInfo.webApps.size());
        vfs.addAll(appInfo.clients);
        vfs.addAll(appInfo.connectors);
        vfs.addAll(appInfo.ejbJars);
        vfs.addAll(appInfo.webApps);
        return vfs;
    }

    public void bindGlobals(final Map<String, Object> bindings) throws NamingException {
        final Context containerSystemContext = containerSystem.getJNDIContext();
        for (final Entry<String, Object> value : bindings.entrySet()) {
            final String path = value.getKey();
            // keep only global bindings
            if (path.startsWith("module/") || path.startsWith("app/") || path.startsWith("comp/") || path.equalsIgnoreCase("global/dummy")) {
                continue;
            }

            // a bit weird but just to be consistent if user doesn't lookup directly the resource
            final Context lastContext = Contexts.createSubcontexts(containerSystemContext, path);
            try {
                lastContext.rebind(path.substring(path.lastIndexOf('/') + 1, path.length()), value.getValue());
            } catch (final NameAlreadyBoundException nabe) {
                nabe.printStackTrace();
            }
            containerSystemContext.rebind(path, value.getValue());
        }
    }

    private void propagateApplicationExceptions(final AppInfo appInfo, final ClassLoader classLoader, final List<BeanContext> allDeployments) {
        for (final BeanContext context : allDeployments) {
            if (BeanContext.Comp.class.equals(context.getBeanClass())) {
                continue;
            }

            for (final EjbJarInfo jar : appInfo.ejbJars) {
                for (final ApplicationExceptionInfo exception : jar.applicationException) {
                    try {
                        final Class<?> exceptionClass = classLoader.loadClass(exception.exceptionClass);
                        context.addApplicationException(exceptionClass, exception.rollback, exception.inherited);
                    } catch (final Exception e) {
                        // no-op: not a big deal since by jar config is respected, mainly means propagation didn't work because of classloader constraints
                    }
                }
            }
        }
    }

    private void resumePersistentSchedulers(final AppContext appContext) {
        try { // if quartz is missing
            final Scheduler globalScheduler = SystemInstance.get().getComponent(Scheduler.class);
            final Collection<Scheduler> schedulers = new ArrayList<>();
            for (final BeanContext ejb : appContext.getBeanContexts()) {
                final Scheduler scheduler = ejb.get(Scheduler.class);
                if (scheduler == null || scheduler == globalScheduler || schedulers.contains(scheduler)) {
                    continue;
                }

                schedulers.add(scheduler);
                try {
                    scheduler.resumeAll();
                } catch (final Exception e) {
                    logger.warning("Can't resume scheduler for " + ejb.getEjbName(), e);
                }
            }
        } catch (final NoClassDefFoundError ncdfe) {
            // no-op
        }
    }

    public List<BeanContext> initEjbs(final ClassLoader classLoader, final AppInfo appInfo, final AppContext appContext,
                                      final Set<Injection> injections, final List<BeanContext> allDeployments, final String webappId) throws OpenEJBException {
        final String globalTimersOn = SystemInstance.get().getProperty(OPENEJB_TIMERS_ON, "true");

        final EjbJarBuilder ejbJarBuilder = new EjbJarBuilder(props, appContext);
        for (final EjbJarInfo ejbJar : appInfo.ejbJars) {

            if (isSkip(appInfo, webappId, ejbJar)) {
                continue;
            }

            final HashMap<String, BeanContext> deployments = ejbJarBuilder.build(ejbJar, injections, classLoader);

            final JaccPermissionsBuilder jaccPermissionsBuilder = new JaccPermissionsBuilder();
            final PolicyContext policyContext = jaccPermissionsBuilder.build(ejbJar, deployments);
            jaccPermissionsBuilder.install(policyContext);

            final TransactionPolicyFactory transactionPolicyFactory = createTransactionPolicyFactory(ejbJar, classLoader);
            for (final BeanContext beanContext : deployments.values()) {
                beanContext.setTransactionPolicyFactory(transactionPolicyFactory);
            }

            final MethodTransactionBuilder methodTransactionBuilder = new MethodTransactionBuilder();
            methodTransactionBuilder.build(deployments, ejbJar.methodTransactions);

            final MethodConcurrencyBuilder methodConcurrencyBuilder = new MethodConcurrencyBuilder();
            methodConcurrencyBuilder.build(deployments, ejbJar.methodConcurrency);

            for (final BeanContext beanContext : deployments.values()) {
                containerSystem.addDeployment(beanContext);
            }

            //bind ejbs into global jndi
            jndiBuilder.build(ejbJar, deployments);

            // setup timers/asynchronous methods - must be after transaction attributes are set
            for (final BeanContext beanContext : deployments.values()) {
                if (beanContext.getComponentType() != BeanType.STATEFUL) {
                    final Method ejbTimeout = beanContext.getEjbTimeout();
                    boolean timerServiceRequired = false;
                    if (ejbTimeout != null) {
                        // If user set the tx attribute to RequiresNew change it to Required so a new transaction is not started
                        if (beanContext.getTransactionType(ejbTimeout) == TransactionType.RequiresNew) {
                            beanContext.setMethodTransactionAttribute(ejbTimeout, TransactionType.Required);
                        }
                        timerServiceRequired = true;
                    }
                    for (final Iterator<Map.Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext(); ) {
                        final Map.Entry<Method, MethodContext> entry = it.next();
                        final MethodContext methodContext = entry.getValue();
                        if (methodContext.getSchedules().size() > 0) {
                            timerServiceRequired = true;
                            final Method method = entry.getKey();
                            //TODO Need ?
                            if (beanContext.getTransactionType(method) == TransactionType.RequiresNew) {
                                beanContext.setMethodTransactionAttribute(method, TransactionType.Required);
                            }
                        }
                    }

                    if (timerServiceRequired && "true".equalsIgnoreCase(appInfo.properties.getProperty(OPENEJB_TIMERS_ON, globalTimersOn))) {
                        // Create the timer
                        final EjbTimerServiceImpl timerService = new EjbTimerServiceImpl(beanContext, newTimerStore(beanContext));
                        //Load auto-start timers
                        final TimerStore timerStore = timerService.getTimerStore();
                        for (final Iterator<Map.Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext(); ) {
                            final Map.Entry<Method, MethodContext> entry = it.next();
                            final MethodContext methodContext = entry.getValue();
                            for (final ScheduleData scheduleData : methodContext.getSchedules()) {
                                timerStore.createCalendarTimer(timerService,
                                    (String) beanContext.getDeploymentID(),
                                    null,
                                    entry.getKey(),
                                    scheduleData.getExpression(),
                                    scheduleData.getConfig(),
                                    true);
                            }
                        }
                        beanContext.setEjbTimerService(timerService);
                    } else {
                        beanContext.setEjbTimerService(new NullEjbTimerServiceImpl());
                    }
                }
                //set asynchronous methods transaction
                //TODO ???
                for (final Iterator<Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext(); ) {
                    final Entry<Method, MethodContext> entry = it.next();
                    if (entry.getValue().isAsynchronous() && beanContext.getTransactionType(entry.getKey()) == TransactionType.RequiresNew) {
                        beanContext.setMethodTransactionAttribute(entry.getKey(), TransactionType.Required);
                    }
                }

                // if local bean or mdb generate proxy class now to avoid bottleneck on classloader later
                if (beanContext.isLocalbean() && !beanContext.getComponentType().isMessageDriven() && !beanContext.isDynamicallyImplemented()) {
                    final List<Class> interfaces = new ArrayList<>(3);
                    interfaces.add(Serializable.class);
                    interfaces.add(IntraVmProxy.class);
                    final BeanType type = beanContext.getComponentType();
                    if (BeanType.STATEFUL.equals(type) || BeanType.MANAGED.equals(type)) {
                        interfaces.add(BeanContext.Removable.class);
                    }

                    beanContext.set(
                        BeanContext.ProxyClass.class,
                        new BeanContext.ProxyClass(
                            beanContext,
                            interfaces.toArray(new Class<?>[interfaces.size()])
                        ));
                }
            }
            // process application exceptions
            for (final ApplicationExceptionInfo exceptionInfo : ejbJar.applicationException) {
                try {
                    final Class exceptionClass = classLoader.loadClass(exceptionInfo.exceptionClass);
                    for (final BeanContext beanContext : deployments.values()) {
                        beanContext.addApplicationException(exceptionClass, exceptionInfo.rollback, exceptionInfo.inherited);
                    }
                } catch (final ClassNotFoundException e) {
                    logger.error("createApplication.invalidClass", e, exceptionInfo.exceptionClass, e.getMessage());
                }
            }

            allDeployments.addAll(deployments.values());
        }

        final List<BeanContext> ejbs = sort(allDeployments);
        for (final BeanContext b : ejbs) { // otherwise for ears we have duplicated beans
            if (appContext.getBeanContexts().contains(b)) {
                continue;
            }
            appContext.getBeanContexts().add(b);
        }
        return ejbs;
    }

    private boolean isSkip(final AppInfo appInfo, final String webappId, final EjbJarInfo ejbJar) {
        boolean skip = false;
        if (!appInfo.webAppAlone) {
            if (webappId == null) {
                skip = ejbJar.webapp; // we look for the lib part of the ear so deploy only if not a webapp
            } else if (!ejbJar.webapp
                    || (!ejbJar.moduleId.equals(webappId) && !ejbJar.properties.getProperty("openejb.ejbmodule.webappId", "-").equals(webappId))) {
                skip = true; // we look for a particular webapp deployment so deploy only if this webapp
            }
        }
        return skip;
    }

    private TimerStore newTimerStore(final BeanContext beanContext) {
        for (final DeploymentContext context : Arrays.asList(beanContext, beanContext.getModuleContext(), beanContext.getModuleContext().getAppContext())) {
            final String timerStoreClass = context.getProperties().getProperty(TIMER_STORE_CLASS);
            if (timerStoreClass != null) {
                logger.info("Found timer class: " + timerStoreClass);

                try {
                    final Class<?> clazz = beanContext.getClassLoader().loadClass(timerStoreClass);
                    try {
                        final Constructor<?> constructor = clazz.getConstructor(TransactionManager.class);
                        return TimerStore.class.cast(constructor.newInstance(EjbTimerServiceImpl.getDefaultTransactionManager()));
                    } catch (final Exception ignored) {
                        return TimerStore.class.cast(clazz.newInstance());
                    }
                } catch (final Exception e) {
                    logger.error("Can't instantiate " + timerStoreClass + ", using default memory timer store");
                }
            }
        }

        return new MemoryTimerStore(EjbTimerServiceImpl.getDefaultTransactionManager());
    }

    public void startEjbs(final boolean start, final List<BeanContext> allDeployments) throws OpenEJBException {
        // now that everything is configured, deploy to the container
        if (start) {
            SystemInstance.get().fireEvent(new BeforeStartEjbs(allDeployments));

            final Collection<BeanContext> toStart = new ArrayList<>();

            // deploy
            for (final BeanContext deployment : allDeployments) {
                try {
                    final Container container = deployment.getContainer();
                    if (container.getBeanContext(deployment.getDeploymentID()) == null) {
                        container.deploy(deployment);
                        if (!((String) deployment.getDeploymentID()).endsWith(".Comp")
                            && !deployment.isHidden()) {
                            logger.info("createApplication.createdEjb", deployment.getDeploymentID(), deployment.getEjbName(), container.getContainerID());
                        }
                        if (logger.isDebugEnabled()) {
                            for (final Map.Entry<Object, Object> entry : deployment.getProperties().entrySet()) {
                                logger.info("createApplication.createdEjb.property", deployment.getEjbName(), entry.getKey(), entry.getValue());
                            }
                        }
                        toStart.add(deployment);
                    }
                } catch (final Throwable t) {
                    throw new OpenEJBException("Error deploying '" + deployment.getEjbName() + "'.  Exception: " + t.getClass() + ": " + t.getMessage(), t);
                }
            }

            // start
            for (final BeanContext deployment : toStart) {
                try {
                    final Container container = deployment.getContainer();
                    container.start(deployment);
                    if (!((String) deployment.getDeploymentID()).endsWith(".Comp")
                        && !deployment.isHidden()) {
                        logger.info("createApplication.startedEjb", deployment.getDeploymentID(), deployment.getEjbName(), container.getContainerID());
                    }
                } catch (final Throwable t) {
                    throw new OpenEJBException("Error starting '" + deployment.getEjbName() + "'.  Exception: " + t.getClass() + ": " + t.getMessage(), t);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void deployMBean(final WebBeansContext wc, final ClassLoader cl, final String mbeanClass, final Properties appMbeans, final String id) {
        if (LocalMBeanServer.isJMXActive()) {
            final Class<?> clazz;
            try {
                clazz = cl.loadClass(mbeanClass);
            } catch (final ClassNotFoundException e) {
                throw new OpenEJBRuntimeException(e);
            }

            // cdi can be off so init with null bean in this case
            final Bean<?> bean;
            final BeanManager bm;
            if (wc == null) {
                bm = null;
                bean = null;
            } else {
                bm = wc.getBeanManagerImpl();
                final Set<Bean<?>> beans = bm.getBeans(clazz);
                bean = bm.resolve(beans);
            }

            // create the MBean instance with cdi if possible or manually otherwise
            final Object instance;
            final CreationalContext creationalContext;
            if (bean == null) {
                try {
                    instance = clazz.newInstance();
                } catch (final InstantiationException e) {
                    logger.error("the mbean " + mbeanClass + " can't be registered because it can't be instantiated", e);
                    return;
                } catch (final IllegalAccessException e) {
                    logger.error("the mbean " + mbeanClass + " can't be registered because it can't be accessed", e);
                    return;
                }
                creationalContext = null;
            } else {
                creationalContext = bm.createCreationalContext(bean);
                instance = bm.getReference(bean, clazz, creationalContext);
            }

            final MBeanServer server = LocalMBeanServer.get();
            try {
                final MBean annotation = clazz.getAnnotation(MBean.class);
                final ObjectName leaf = annotation == null || annotation.objectName().isEmpty() ? new ObjectNameBuilder("openejb.user.mbeans")
                    .set("application", id)
                    .set("group", clazz.getPackage().getName())
                    .set("name", clazz.getSimpleName())
                    .build() : new ObjectName(annotation.objectName());

                server.registerMBean(new DynamicMBeanWrapper(wc, instance), leaf);
                appMbeans.put(mbeanClass, leaf.getCanonicalName());
                if (creationalContext != null && (bean.getScope() == null || Dependent.class.equals(bean.getScope()))) {
                    creationalContextForAppMbeans.put(leaf, creationalContext);
                }
                logger.info("Deployed MBean(" + leaf.getCanonicalName() + ")");
            } catch (final Exception e) {
                logger.error("the mbean " + mbeanClass + " can't be registered", e);
            }
        }
    }

    private void ensureWebBeansContext(final AppContext appContext) {
        WebBeansContext webBeansContext = appContext.get(WebBeansContext.class);
        if (webBeansContext == null) {
            webBeansContext = appContext.getWebBeansContext();
        }else{
            if (null == appContext.getWebBeansContext()){
                appContext.setWebBeansContext(webBeansContext);
            }
            return;
        }

        if (webBeansContext == null) {

            final Map<Class<?>, Object> services = new HashMap<>();

            services.put(Executor.class, new ManagedExecutorServiceImpl(ForkJoinPool.commonPool()));
            services.put(JNDIService.class, new OpenEJBJndiService());
            services.put(AppContext.class, appContext);
            services.put(ScannerService.class, new CdiScanner());
            services.put(BeanArchiveService.class, new OpenEJBBeanInfoService());
            services.put(ELAdaptor.class, new CustomELAdapter(appContext));
            services.put(LoaderService.class, new OptimizedLoaderService(appContext.getProperties()));

            final Properties properties = new Properties();
            properties.setProperty(org.apache.webbeans.spi.SecurityService.class.getName(), ManagedSecurityService.class.getName());
            properties.setProperty(ContextsService.class.getName(), CdiAppContextsService.class.getName());
            properties.setProperty(ResourceInjectionService.class.getName(), CdiResourceInjectionService.class.getName());
            properties.setProperty(TransactionService.class.getName(), OpenEJBTransactionService.class.getName());

            // for Java 9 and above, using Unsafe does not work well to create proxies
            // OWB has support for classloader defineClass but this isn't done automagically
            // like in ClassDefiner. We need to explicitly set the Proxy service
            if (ClassDefiner.isClassLoaderDefineClass()) {
                properties.setProperty(DefiningClassService.class.getName(), ClassDefiner.class.getName());
            }

            webBeansContext = new WebBeansContext(services, properties);

            appContext.setCdiEnabled(false);
            appContext.set(WebBeansContext.class, webBeansContext);
            appContext.setWebBeansContext(webBeansContext);
        }
    }

    private TransactionPolicyFactory createTransactionPolicyFactory(final EjbJarInfo ejbJar, final ClassLoader classLoader) {
        TransactionPolicyFactory factory = null;

        final Object value = ejbJar.properties.get(TransactionPolicyFactory.class.getName());
        if (value instanceof TransactionPolicyFactory) {
            factory = (TransactionPolicyFactory) value;
        } else if (value instanceof String) {
            try {
                final String[] parts = ((String) value).split(":", 2);

                final ResourceFinder finder = new ResourceFinder("META-INF", classLoader);
                final Map<String, Class<? extends TransactionPolicyFactory>> plugins = finder.mapAvailableImplementations(TransactionPolicyFactory.class);
                final Class<? extends TransactionPolicyFactory> clazz = plugins.get(parts[0]);
                if (clazz != null) {
                    if (parts.length == 1) {
                        factory = clazz.getConstructor(String.class).newInstance(parts[1]);
                    } else {
                        factory = clazz.newInstance();
                    }
                }
            } catch (final Exception ignored) {
                // couldn't determine the plugins, which isn't fatal
            }
        }

        if (factory == null) {
            factory = new JtaTransactionPolicyFactory(transactionManager);
        }
        return factory;
    }

    private static List<BeanContext> sort(List<BeanContext> deployments) {
        // Sort all the singletons to the back of the list.  We want to make sure
        // all non-singletons are created first so that if a singleton refers to them
        // they are available.
        deployments.sort(new Comparator<BeanContext>() {
            @Override
            public int compare(final BeanContext a, final BeanContext b) {
                final int aa = a.getComponentType() == BeanType.SINGLETON ? 1 : 0;
                final int bb = b.getComponentType() == BeanType.SINGLETON ? 1 : 0;
                return aa - bb;
            }
        });

        // Sort all the beans with references to the back of the list.  Beans
        // without references to ther beans will be deployed first.
        deployments = References.sort(deployments, new References.Visitor<BeanContext>() {
            @Override
            public String getName(final BeanContext t) {
                return (String) t.getDeploymentID();
            }

            @Override
            public Set<String> getReferences(final BeanContext t) {
                return t.getDependsOn();
            }
        });

        // Now Sort all the MDBs to the back of the list.  The Resource Adapter
        // may attempt to use the MDB on endpointActivation and the MDB may have
        // references to other ejbs that would need to be available first.
        deployments.sort(new Comparator<BeanContext>() {
            @Override
            public int compare(final BeanContext a, final BeanContext b) {
                final int aa = a.getComponentType() == BeanType.MESSAGE_DRIVEN ? 1 : 0;
                final int bb = b.getComponentType() == BeanType.MESSAGE_DRIVEN ? 1 : 0;
                return aa - bb;
            }
        });

        return deployments;
    }

    @Override
    public void destroy() {

        final ReentrantLock l = lock;
        l.lock();

        try {
            final SystemInstance systemInstance = SystemInstance.get();
            systemInstance.fireEvent(new ContainerSystemPreDestroy());

            try {
                EjbTimerServiceImpl.shutdown();
            } catch (final Exception e) {
                logger.warning("Unable to shutdown scheduler", e);
            } catch (final NoClassDefFoundError ncdfe) {
                // no-op
            }

            logger.debug("Undeploying Applications");
            final Assembler assembler = this;
            final List<AppInfo> deployedApps = new ArrayList<>(assembler.getDeployedApplications());
            Collections.reverse(deployedApps); // if an app relies on the previous one it surely relies on it too at undeploy time
            for (final AppInfo appInfo : deployedApps) {
                try {
                    assembler.destroyApplication(appInfo.path);
                } catch (final UndeployException e) {
                    logger.error("Undeployment failed: " + appInfo.path, e);
                } catch (final NoSuchApplicationException e) {
                    //Ignore
                }
            }

            final Iterator<ObjectName> it = containerObjectNames.iterator();
            final MBeanServer server = LocalMBeanServer.get();
            while (it.hasNext()) {
                try {
                    server.unregisterMBean(it.next());
                } catch (final Exception ignored) {
                    // no-op
                }
                it.remove();
            }
            try {
                remoteResourceMonitor.unregister();
            } catch (final Exception ignored) {
                // no-op
            }

            NamingEnumeration<Binding> namingEnumeration = null;
            try {
                namingEnumeration = containerSystem.getJNDIContext().listBindings("openejb/Resource");
            } catch (final NamingException ignored) {
                // no resource adapters were created
            }
            destroyResourceTree("", namingEnumeration);

            try {
                containerSystem.getJNDIContext().unbind("java:global");
            } catch (final NamingException ignored) {
                // no-op
            }

            systemInstance.removeComponent(OpenEjbConfiguration.class);
            systemInstance.removeComponent(JtaEntityManagerRegistry.class);
            systemInstance.removeComponent(TransactionSynchronizationRegistry.class);
            systemInstance.removeComponent(EjbResolver.class);
            systemInstance.removeComponent(ThreadSingletonService.class);
            systemInstance.fireEvent(new AssemblerDestroyed());
            systemInstance.removeObservers();

            if (DestroyableResource.class.isInstance(this.securityService)) {
                DestroyableResource.class.cast(this.securityService).destroyResource();
            }
            if (DestroyableResource.class.isInstance(this.transactionManager)) {
                DestroyableResource.class.cast(this.transactionManager).destroyResource();
            }

            for (final Container c : this.containerSystem.containers()) {
                if (DestroyableResource.class.isInstance(c)) { // TODO: should we use auto closeable there?
                    DestroyableResource.class.cast(c).destroyResource();
                }
            }

            SystemInstance.reset();
        } finally {
            l.unlock();
        }
    }

    private Collection<DestroyingResource> destroyResourceTree(final String base, final NamingEnumeration<Binding> namingEnumeration) {
        final List<DestroyingResource> resources = new LinkedList<>();
        while (namingEnumeration != null && namingEnumeration.hasMoreElements()) {
            final Binding binding = namingEnumeration.nextElement();
            final Object object = binding.getObject();
            if (Context.class.isInstance(object)) {
                try {
                    resources.addAll(destroyResourceTree(
                            IvmContext.class.isInstance(object) ? IvmContext.class.cast(object).mynode.getAtomicName() : "",
                            Context.class.cast(object).listBindings("")));
                } catch (final Exception ignored) {
                    // no-op
                }
            } else {
                resources.add(new DestroyingResource((base == null || base.isEmpty() ? "" : (base + '/')) + binding.getName(), binding.getClassName(), object));
            }
        }

        resources.sort(new Comparator<DestroyingResource>() { // end by destroying RA after having closed CF pool (for jms for instance)
            @Override
            public int compare(final DestroyingResource o1, final DestroyingResource o2) {
                final boolean ra1 = isRa(o1.instance);
                final boolean ra2 = isRa(o2.instance);
                if (ra2 && !ra1) {
                    return -1;
                }
                if (ra1 && !ra2) {
                    return 1;
                }
                // TODO: handle dependencies there too
                return o1.name.compareTo(o2.name);
            }

            private boolean isRa(final Object instance) {
                return ResourceAdapter.class.isInstance(instance) || ResourceAdapterReference.class.isInstance(instance);
            }
        });

        for (final DestroyingResource resource : resources) {
            try {
                destroyResource(resource.name, resource.clazz, resource.instance);
            } catch (final Throwable th) {
                logger.debug(th.getMessage(), th);
            }
        }

        return resources;
    }

    private void destroyResource(final String name, final String className, final Object inObject) {
        Object object;
        try {
            object = LazyResource.class.isInstance(inObject) && LazyResource.class.cast(inObject).isInitialized() ?
                LazyResource.class.cast(inObject).getObject() : inObject;
        } catch (final NamingException e) {
            object = inObject; // in case it impl DestroyableResource
        }

        final Collection<Method> preDestroy = null;

        if (resourceDestroyTimeout != null) {
            final Duration d = new Duration(resourceDestroyTimeout);
            final ExecutorService es = Executors.newSingleThreadExecutor(new DaemonThreadFactory("openejb-resource-destruction-" + name));
            final Object o = object;
            try {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        doResourceDestruction(name, className, o);
                    }
                }).get(d.getTime(), d.getUnit());
            } catch (final InterruptedException e) {
                Thread.interrupted();
            } catch (final ExecutionException e) {
                throw RuntimeException.class.cast(e.getCause());
            } catch (final TimeoutException e) {
                logger.error("Can't destroy " + name + " in " + resourceDestroyTimeout + ", giving up.", e);
                if (threadStackOnTimeout) {
                    final ThreadInfo[] dump = ManagementFactory.getThreadMXBean().dumpAllThreads(false, false);
                    final ByteArrayOutputStream writer = new ByteArrayOutputStream();
                    final PrintStream stream = new PrintStream(writer);
                    for (final ThreadInfo info : dump) {
                        stream.println('"' + info.getThreadName() + "\" suspended=" + info.isSuspended() + " state=" + info.getThreadState());
                        for (final StackTraceElement traceElement : info.getStackTrace()) {
                            stream.println("\tat " + traceElement);
                        }
                    }
                    logger.info("Dump on " + name + " destruction timeout:\n" + new String(writer.toByteArray()));
                }
            }
        } else {
            doResourceDestruction(name, className, object);
        }

        callPreDestroy(name, object);
        removeResourceInfo(name);
    }

    private void callPreDestroy(final String name, final Object object) {
        if (object == null) {
            return;
        }

        if (ResourceInstance.class.isInstance(object)) {
            ResourceInstance.class.cast(object).destroyResource();
            return;
        }

        final Class<?> objectClass = object.getClass();

        final ResourceInfo ri = findResourceInfo(name);
        if (ri == null) {
            return;
        }

        final Set<String> destroyMethods = new HashSet<>();
        if (ri.preDestroy != null) {
            destroyMethods.add(ri.preDestroy);
        }

        if (ri.preDestroyMethods != null && ri.preDestroyMethods.size() > 0) {
            destroyMethods.addAll(ri.preDestroyMethods);
        }

        for (final String destroyMethod : destroyMethods) {
            try {
                final Method p = objectClass.getDeclaredMethod(destroyMethod);
                if (!p.isAccessible()) {
                    SetAccessible.on(p);
                }
                p.invoke(object);
            } catch (Exception e) {
                logger.error("Unable to call pre destroy method " + destroyMethod + " on "
                        + objectClass.getName() + ". Continuing with resource destruction.", e);
            }
        }
    }

    private ResourceInfo findResourceInfo(String name) {
        List<ResourceInfo> resourceInfos = config.facilities.resources;
        for (final ResourceInfo resourceInfo : resourceInfos) {
            if (resourceInfo.id.equals(name)) {
                return resourceInfo;
            }
        }

        return null;
    }

    private void doResourceDestruction(final String name, final String className, final Object jndiObject) {
        final ResourceBeforeDestroyed event = new ResourceBeforeDestroyed(jndiObject, name);
        SystemInstance.get().fireEvent(event);
        final Object object = event.getReplacement() == null ? jndiObject : event.getReplacement();
        if (object instanceof ResourceAdapterReference) {
            final ResourceAdapterReference resourceAdapter = (ResourceAdapterReference) object;
            try {
                logger.info("Stopping ResourceAdapter: " + name);

                if (logger.isDebugEnabled()) {
                    logger.debug("Stopping ResourceAdapter: " + className);
                }

                if (resourceAdapter.pool != null && ExecutorService.class.isInstance(resourceAdapter.pool)) {
                    ExecutorService.class.cast(resourceAdapter.pool).shutdownNow();
                }
                resourceAdapter.ra.stop();

                // remove associated JMX object
            } catch (final Throwable t) {
                logger.fatal("ResourceAdapter Shutdown Failed: " + name, t);
            }

            removeResourceMBean(name, "ResourceAdapter");

        } else if (object instanceof ResourceAdapter) {
            final ResourceAdapter resourceAdapter = (ResourceAdapter) object;
            try {
                logger.info("Stopping ResourceAdapter: " + name);

                if (logger.isDebugEnabled()) {
                    logger.debug("Stopping ResourceAdapter: " + className);
                }

                resourceAdapter.stop();
            } catch (final Throwable t) {
                logger.fatal("ResourceAdapter Shutdown Failed: " + name, t);
            }

            removeResourceMBean(name, "ResourceAdapter");

        } else if (DataSourceFactory.knows(object)) {
            logger.info("Closing DataSource: " + name);

            try {
                DataSourceFactory.destroy(object);
            } catch (final Throwable t) {
                //Ignore
            }
        } else if (object instanceof ConnectorReference) {
            final ConnectorReference cr = (ConnectorReference) object;
            try {
                final ConnectionManager cm = cr.getConnectionManager();
                if (cm != null && cm instanceof AbstractConnectionManager) {
                    ((AbstractConnectionManager) cm).doStop();
                }
            } catch (final Exception e) {
                logger.debug("Not processing resource on destroy: " + className, e);
            }

            removeResourceMBean(name, "ConnectionFactory");

        } else if (DestroyableResource.class.isInstance(object)) {
            try {
                DestroyableResource.class.cast(object).destroyResource();
            } catch (final RuntimeException e) {
                logger.error(e.getMessage(), e);
            }

            removeResourceMBean(name, "Resource");
        } else if (!DataSource.class.isInstance(object)) {
            removeResourceMBean(name, "Resource");

            if (logger.isDebugEnabled()) {
                logger.debug("Not processing resource on destroy: " + className);
            }
        }
    }

    private void removeResourceMBean(String name, String type) {
        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("j2eeType", "");
        jmxName.set("name",name);

        final MBeanServer server = LocalMBeanServer.get();
        try {
            final ObjectName objectName = jmxName.set("j2eeType", type).build();
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }
        } catch (final Exception e) {
            logger.error("Unable to unregister MBean ", e);
        }
    }

    public ResourceInfo removeResourceInfo(final String name) {
        try {
            //Ensure ResourceInfo for this resource is removed
            final OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
            final Iterator<ResourceInfo> iterator;
            if (configuration != null) {
                iterator = configuration.facilities.resources.iterator();
            }else{
                throw new Exception("OpenEjbConfiguration has not been initialized");
            }
            while (iterator.hasNext()) {
                final ResourceInfo info = iterator.next();
                if (name.equals(info.id)) {
                    iterator.remove();
                    return info;
                }
            }
        } catch (final Exception e) {
            logger.debug("Failed to purge resource on destroy: " + e.getMessage());
        }

        return null;
    }

    private static Object unwrapReference(final Object object) {
        Object o = object;
        while (o != null && Reference.class.isInstance(o)) {
            try {
                o = Reference.class.cast(o).getObject();
            } catch (final NamingException e) {
                // break
            }
        }
        if (o == null) {
            o = object;
        }
        return o;
    }

    public void destroyApplication(final String filePath) throws UndeployException, NoSuchApplicationException {

        final ReentrantLock l = lock;
        l.lock();

        try {
            final AppInfo appInfo = deployedApplications.remove(filePath);
            if (appInfo == null) {
                throw new NoSuchApplicationException(filePath);
            }
            destroyApplication(appInfo);
        } finally {
            l.unlock();
        }
    }

    // @todo Remove this method in next release
    @Deprecated
    public void destroyApplication(final AppContext appContext) throws UndeployException {

        final ReentrantLock l = lock;
        l.lock();

        try {
            final AppInfo appInfo = deployedApplications.remove(appContext.getId());
            if (appInfo == null) {
                throw new IllegalStateException(String.format("Cannot find AppInfo for app: %s", appContext.getId()));
            }
            destroyApplication(appInfo);
        } finally {
            l.unlock();
        }
    }

    public void destroyApplication(final AppInfo appInfo) throws UndeployException {

        final ReentrantLock l = lock;
        l.lock();

        try {
            deployedApplications.remove(appInfo.path);
            logger.info("destroyApplication.start", appInfo.path);

            final Context globalContext = containerSystem.getJNDIContext();
            final AppContext appContext = containerSystem.getAppContext(appInfo.appId);

            if (null == appContext) {
                logger.warning("Application id '" + appInfo.appId + "' not found in: " + Arrays.toString(containerSystem.getAppContextKeys()));
                return;
            }

            final ClassLoader classLoader = appContext.getClassLoader();

            SystemInstance.get().fireEvent(new AssemblerBeforeApplicationDestroyed(appInfo, appContext));

            //noinspection ConstantConditions

            final WebBeansContext webBeansContext = appContext.getWebBeansContext();
            if (webBeansContext != null) {
                final ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(classLoader);
                try {
                    final ServletContext context = appContext.isStandaloneModule() && appContext.getWebContexts().iterator().hasNext() ?
                            appContext.getWebContexts().iterator().next().getServletContext() : null;
                    webBeansContext.getService(ContainerLifecycle.class).stopApplication(context);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
            final Map<String, Object> cb = appContext.getBindings();
            for (final Entry<String, Object> value : cb.entrySet()) {
                String path = value.getKey();
                if (path.startsWith("global")) {
                    path = "java:" + path;
                }
                if (!path.startsWith("java:global")) {
                    continue;
                }

                if(IvmContext.class.isInstance(globalContext)) {
                    IvmContext.class.cast(globalContext).setReadOnly(false);
                }

                unbind(globalContext, path);
                unbind(globalContext, "openejb/global/" + path.substring("java:".length()));
                unbind(globalContext, path.substring("java:global".length()));
            }
            if (appInfo.appId != null && !appInfo.appId.isEmpty() && !"openejb".equals(appInfo.appId)) {
                unbind(globalContext, "global/" + appInfo.appId);
                unbind(globalContext, appInfo.appId);
            }

            final EjbResolver globalResolver = new EjbResolver(null, EjbResolver.Scope.GLOBAL);
            for (final AppInfo info : deployedApplications.values()) {
                globalResolver.addAll(info.ejbJars);
            }
            SystemInstance.get().setComponent(EjbResolver.class, globalResolver);

            final UndeployException undeployException = new UndeployException(messages.format("destroyApplication.failed", appInfo.path));

            final WebAppBuilder webAppBuilder = SystemInstance.get().getComponent(WebAppBuilder.class);
            if (webAppBuilder != null && !appInfo.webAppAlone) {
                try {
                    webAppBuilder.undeployWebApps(appInfo);
                } catch (final Exception e) {
                    undeployException.getCauses().add(new Exception("App: " + appInfo.path + ": " + e.getMessage(), e));
                }
            }

            // get all of the ejb deployments
            List<BeanContext> deployments = new ArrayList<>();
            for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
                for (final EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
                    final String deploymentId = beanInfo.ejbDeploymentId;
                    final BeanContext beanContext = containerSystem.getBeanContext(deploymentId);
                    if (beanContext == null) {
                        undeployException.getCauses().add(new Exception("deployment not found: " + deploymentId));
                    } else {
                        deployments.add(beanContext);
                    }
                }
            }

            // Just as with startup we need to get things in an
            // order that respects the singleton @DependsOn information
            // Theoreticlly if a Singleton depends on something in its
            // @PostConstruct, it can depend on it in its @PreDestroy.
            // Therefore we want to make sure that if A dependsOn B,
            // that we destroy A first then B so that B will still be
            // usable in the @PreDestroy method of A.

            // Sort them into the original starting order
            deployments = sort(deployments);
            // reverse that to get the stopping order
            Collections.reverse(deployments);

            // stop
            for (final BeanContext deployment : deployments) {
                final String deploymentID = String.valueOf(deployment.getDeploymentID());
                try {
                    final Container container = deployment.getContainer();
                    container.stop(deployment);
                } catch (final Throwable t) {
                    undeployException.getCauses().add(new Exception("bean: " + deploymentID + ": " + t.getMessage(), t));
                }
            }

            // undeploy
            for (final BeanContext bean : deployments) {
                final String deploymentID = String.valueOf(bean.getDeploymentID());
                try {
                    final Container container = bean.getContainer();
                    container.undeploy(bean);
                    bean.setContainer(null);
                } catch (final Throwable t) {
                    undeployException.getCauses().add(new Exception("bean: " + deploymentID + ": " + t.getMessage(), t));
                } finally {
                    bean.setDestroyed(true);
                }
            }

            if (webAppBuilder != null && appInfo.webAppAlone) { // now that EJB are stopped we can undeploy webapps
                try {
                    webAppBuilder.undeployWebApps(appInfo);
                } catch (final Exception e) {
                    undeployException.getCauses().add(new Exception("App: " + appInfo.path + ": " + e.getMessage(), e));
                }
            }

            // get the client ids
            final List<String> clientIds = new ArrayList<>();
            for (final ClientInfo clientInfo : appInfo.clients) {
                clientIds.add(clientInfo.moduleId);
                clientIds.addAll(clientInfo.localClients);
                clientIds.addAll(clientInfo.remoteClients);
            }

            for (final WebContext webContext : appContext.getWebContexts()) {
                containerSystem.removeWebContext(webContext);
            }
            TldScanner.forceCompleteClean(classLoader);

            // Clear out naming for all components first
            for (final BeanContext deployment : deployments) {
                final String deploymentID = String.valueOf(deployment.getDeploymentID());
                try {
                    containerSystem.removeBeanContext(deployment);
                } catch (final Throwable t) {
                    undeployException.getCauses().add(new Exception(deploymentID, t));
                }

                final JndiBuilder.Bindings bindings = deployment.get(JndiBuilder.Bindings.class);
                if (bindings != null) {
                    for (final String name : bindings.getBindings()) {
                        try {
                            globalContext.unbind(name);
                        } catch (final Throwable t) {
                            undeployException.getCauses().add(new Exception("bean: " + deploymentID + ": " + t.getMessage(), t));
                        }
                    }
                }
            }

            // stop this executor only now since @PreDestroy can trigger some stop events
            final AsynchronousPool pool = appContext.get(AsynchronousPool.class);
            if (pool != null) {
                pool.stop();
            }

            for (final CommonInfoObject jar : listCommonInfoObjectsForAppInfo(appInfo)) {
                try {
                    globalContext.unbind(VALIDATOR_FACTORY_NAMING_CONTEXT + jar.uniqueId);
                    globalContext.unbind(VALIDATOR_NAMING_CONTEXT + jar.uniqueId);
                } catch (final NamingException e) {
                    if (EjbJarInfo.class.isInstance(jar)) {
                        undeployException.getCauses().add(new Exception("validator: " + jar.uniqueId + ": " + e.getMessage(), e));
                    } // else an error but not that important
                }
            }
            try {
                if (globalContext instanceof IvmContext) {
                    final IvmContext ivmContext = (IvmContext) globalContext;
                    ivmContext.prune("openejb/Deployment");
                    ivmContext.prune("openejb/local");
                    ivmContext.prune("openejb/remote");
                    ivmContext.prune("openejb/global");
                }
            } catch (final NamingException e) {
                undeployException.getCauses().add(new Exception("Unable to prune openejb/Deployments and openejb/local namespaces, this could cause future deployments to fail.",
                    e));
            }

            deployments.clear();

            for (final String clientId : clientIds) {
                try {
                    globalContext.unbind("/openejb/client/" + clientId);
                } catch (final Throwable t) {
                    undeployException.getCauses().add(new Exception("client: " + clientId + ": " + t.getMessage(), t));
                }
            }

            // mbeans
            final MBeanServer server = LocalMBeanServer.get();
            for (final Object objectName : appInfo.jmx.values()) {
                try {
                    final ObjectName on = new ObjectName((String) objectName);
                    if (server.isRegistered(on)) {
                        server.unregisterMBean(on);
                    }
                    final CreationalContext cc = creationalContextForAppMbeans.remove(on);
                    if (cc != null) {
                        cc.release();
                    }
                } catch (final InstanceNotFoundException e) {
                    logger.warning("can't unregister " + objectName + " because the mbean was not found", e);
                } catch (final MBeanRegistrationException e) {
                    logger.warning("can't unregister " + objectName, e);
                } catch (final MalformedObjectNameException mone) {
                    logger.warning("can't unregister because the ObjectName is malformed: " + objectName, mone);
                }
            }

            // destroy PUs before resources since the JPA provider can use datasources
            for (final PersistenceUnitInfo unitInfo : appInfo.persistenceUnits) {
                try {
                    final Object object = globalContext.lookup(PERSISTENCE_UNIT_NAMING_CONTEXT + unitInfo.id);
                    globalContext.unbind(PERSISTENCE_UNIT_NAMING_CONTEXT + unitInfo.id);

                    // close EMF so all resources are released
                    final ReloadableEntityManagerFactory remf = (ReloadableEntityManagerFactory) object;
                    remf.close();
                    persistenceClassLoaderHandler.destroy(unitInfo.id);
                    remf.unregister();
                } catch (final Throwable t) {
                    undeployException.getCauses().add(new Exception("persistence-unit: " + unitInfo.id + ": " + t.getMessage(), t));
                }
            }

            for (final String id : appInfo.resourceAliases) {
                final String name = OPENEJB_RESOURCE_JNDI_PREFIX + id;
                ContextualJndiReference.followReference.set(false);
                try {
                    final Object object;
                    try {
                        object = globalContext.lookup(name);
                    } finally {
                        ContextualJndiReference.followReference.remove();
                    }
                    if (object instanceof ContextualJndiReference) {
                        final ContextualJndiReference contextualJndiReference = ContextualJndiReference.class.cast(object);
                        contextualJndiReference.removePrefix(appContext.getId());
                        if (contextualJndiReference.hasNoMorePrefix()) {
                            globalContext.unbind(name);
                        } // else not the last deployed application to use this resource so keep it
                    } else {
                        globalContext.unbind(name);
                    }
                } catch (final NamingException e) {
                    logger.warning("can't unbind resource '{0}'", id);
                }
            }
            for (final String id : appInfo.resourceIds) {
                final String name = OPENEJB_RESOURCE_JNDI_PREFIX + id;
                try {
                    destroyLookedUpResource(globalContext, id, name);
                } catch (final NamingException e) {
                    logger.warning("can't unbind resource '{0}'", id);
                }
            }
            for (final ConnectorInfo connector : appInfo.connectors) {
                if (connector.resourceAdapter == null || connector.resourceAdapter.id == null) {
                    continue;
                }

                final String name = OPENEJB_RESOURCE_JNDI_PREFIX + connector.resourceAdapter.id;
                try {
                    destroyLookedUpResource(globalContext, connector.resourceAdapter.id, name);
                } catch (final NamingException e) {
                    logger.warning("can't unbind resource '{0}'", connector);
                }

                for (final ResourceInfo outbound : connector.outbound) {
                    try {
                        destroyLookedUpResource(globalContext, outbound.id, OPENEJB_RESOURCE_JNDI_PREFIX + outbound.id);
                    } catch (final Exception e) {
                        // no-op
                    }
                }
                for (final ResourceInfo outbound : connector.adminObject) {
                    try {
                        destroyLookedUpResource(globalContext, outbound.id, OPENEJB_RESOURCE_JNDI_PREFIX + outbound.id);
                    } catch (final Exception e) {
                        // no-op
                    }
                }
                for (final MdbContainerInfo container : connector.inbound) {
                    try {
                        containerSystem.removeContainer(container.id);
                        config.containerSystem.containers.remove(container);
                        this.containerSystem.getJNDIContext().unbind(JAVA_OPENEJB_NAMING_CONTEXT + container.service + "/" + container.id);
                    } catch (final Exception e) {
                        // no-op
                    }
                }
            }

            for (final ContainerInfo containerInfo : appInfo.containers) {
                if (! containerInfo.applicationWide) {
                    removeContainer(containerInfo.id);
                }
            }

            containerSystem.removeAppContext(appInfo.appId);

            if (!appInfo.properties.containsKey("tomee.destroying")) { // destroy tomee classloader after resources cleanup
                try {
                    final Method m = classLoader.getClass().getMethod("internalStop");
                    m.invoke(classLoader);
                } catch (final NoSuchMethodException nsme) {
                    // no-op
                } catch (final Exception e) {
                    logger.error("error stopping classloader of webapp " + appInfo.appId, e);
                }
                ClassLoaderUtil.cleanOpenJPACache(classLoader);
            }
            ClassLoaderUtil.destroyClassLoader(appInfo.appId, appInfo.path);

            if (undeployException.getCauses().size() > 0) {
                // logging causes here otherwise it will be eaten in later logs.
                for (Throwable cause : undeployException.getCauses()) {
                    logger.error("undeployException original cause", cause);
                }
                throw undeployException;
            }

            logger.debug("destroyApplication.success", appInfo.path);
        } finally {
            l.unlock();
        }
    }

    private void destroyLookedUpResource(final Context globalContext, final String id, final String name) throws NamingException {

        final Object object;

        try {
            object = globalContext.lookup(name);
        } catch (final NamingException e) {
            // if we catch a NamingException, check to see if the resource is a LaztObjectReference that has not been initialized correctly
            final String ctx = name.substring(0, name.lastIndexOf('/'));
            final String objName = name.substring(ctx.length() + 1);
            final NamingEnumeration<Binding> bindings = globalContext.listBindings(ctx);
            while (bindings.hasMoreElements()) {
                final Binding binding = bindings.nextElement();
                if (!binding.getName().equals(objName)) {
                    continue;
                }

                if (!LazyObjectReference.class.isInstance(binding.getObject())) {
                    continue;
                }

                final LazyObjectReference<?> ref = LazyObjectReference.class.cast(binding.getObject());
                if (! ref.isInitialized()) {
                    globalContext.unbind(name);
                    removeResourceInfo(name);
                    return;
                }
            }

            throw e;
        }

        final String clazz;
        if (object == null) { // should it be possible?
            clazz = "?";
        } else {
            clazz = object.getClass().getName();
        }
        destroyResource(id, clazz, object);
        globalContext.unbind(name);
    }

    private void unbind(final Context context, final String name) {
        try {
            context.unbind(name);
        } catch (final NamingException e) {
            // no-op
        }
    }

    public ClassLoader createAppClassLoader(final AppInfo appInfo) throws OpenEJBException, IOException {
        if ("openejb".equals(appInfo.appId)) {
            return ParentClassLoaderFinder.Helper.get();
        }

        final Set<URL> jars = new HashSet<>();
        for (final EjbJarInfo info : appInfo.ejbJars) {
            if (info.path != null) {
                jars.add(toUrl(info.path));
            }
        }
        for (final ClientInfo info : appInfo.clients) {
            if (info.path != null) {
                jars.add(toUrl(info.path));
            }
        }
        for (final ConnectorInfo info : appInfo.connectors) {
            for (final String jarPath : info.libs) {
                jars.add(toUrl(jarPath));
            }
        }
        for (final String jarPath : appInfo.libs) {
            jars.add(toUrl(jarPath));
        }

        // add openejb-jpa-integration if the jpa provider is in lib/
        if (appInfo.libs.size() > 0) { // the test could be enhanced
            try {
                final File jpaIntegrationFile = JarLocation.jarLocation(MakeTxLookup.class);
                final URL url = jpaIntegrationFile.toURI().toURL();
                if (!jars.contains(url)) { // could have been done before (webapp enrichment or manually for instance)
                    jars.add(url);
                }
            } catch (final RuntimeException re) {
                logger.warning("Unable to find the open-jpa-integration jar");
            }
        }
        final ClassLoaderEnricher component = SystemInstance.get().getComponent(ClassLoaderEnricher.class);
        if (component != null) {
            jars.addAll(Arrays.asList(component.applicationEnrichment()));
        }else {
            logger.warning("Unable to find open-jpa-integration jar");
        }

        // Create the class loader
        final ClassLoader parent = ParentClassLoaderFinder.Helper.get();

        final String prefix;
        if (appInfo.webAppAlone) {
            prefix = "WEB-INF/";
        } else {
            prefix = "META-INF/";
        }
        final ClassLoaderConfigurer configurer1 = QuickJarsTxtParser.parse(new File(appInfo.path, prefix + QuickJarsTxtParser.FILE_NAME));
        final ClassLoaderConfigurer configurer2 = ClassLoaderUtil.configurer(appInfo.appId);

        if (configurer1 != null || configurer2 != null) {
            final ClassLoaderConfigurer configurer = new CompositeClassLoaderConfigurer(configurer1, configurer2);
            ClassLoaderConfigurer.Helper.configure(jars, configurer);
        }

        final URL[] filtered = jars.toArray(new URL[jars.size()]);

        // some lib (DS for instance) rely on AppClassLoader for CDI bean manager usage (common for tests cases where you
        // try to get the app BM from the AppClassLoader having stored it in a map).
        // since we don't really need to create a classloader here when starting from classpath just let skip this step
        if (skipLoaderIfPossible) { // TODO: maybe use a boolean to know if all urls comes from the classpath to avoid this validation
            if ("classpath.ear".equals(appInfo.appId)) {
                return parent;
            }

            final Collection<File> urls = new HashSet<>();
            for (final URL url : ClassLoaders.findUrls(parent)) { // need to convert it to file since urls can be file:/xxx or jar:file:///xxx
                try {
                    urls.add(URLs.toFile(url).getCanonicalFile());
                } catch (final Exception error) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Can't determine url for: " + url.toExternalForm(), error);
                    }
                }
            }

            boolean allIsIntheClasspath = true;
            for (final URL url : filtered) {
                try {
                    if (!urls.contains(URLs.toFile(url).getCanonicalFile())) {
                        allIsIntheClasspath = false;
                        if (logger.isDebugEnabled()) {
                            logger.debug(url.toExternalForm() + " (" + URLs.toFile(url)
                                + ") is not in the classloader so we'll create a dedicated classloader for this app");
                        }
                        break;
                    }
                } catch (final Exception ignored) {
                    allIsIntheClasspath = false;
                    if (logger.isDebugEnabled()) {
                        logger.debug(url.toExternalForm() + " (" + URLs.toFile(url) + ") is not in the classloader", ignored);
                    }
                    break;
                }
            }

            if (allIsIntheClasspath) {
                logger.info("Not creating another application classloader for " + appInfo.appId);
                return parent;
            } else if (logger.isDebugEnabled()) {
                logger.debug("Logging all urls from the app since we don't skip the app classloader creation:");
                for (final URL url : filtered) {
                    logger.debug(" -> " + url.toExternalForm());
                }
                logger.debug("Logging all urls from the classloader since we don't skip the app classloader creation:");
                for (final File url : urls) {
                    logger.debug(" -> " + url.getAbsolutePath());
                }
            }
        }

        logger.info("Creating dedicated application classloader for " + appInfo.appId);

        if (!appInfo.delegateFirst) {
            return ClassLoaderUtil.createClassLoader(appInfo.path, filtered, parent);
        }
        return ClassLoaderUtil.createClassLoaderFirst(appInfo.path, filtered, parent);
    }

    public void createExternalContext(final JndiContextInfo contextInfo) throws OpenEJBException {
        logger.getChildLogger("service").info("createService", contextInfo.service, contextInfo.id, contextInfo.className);

        final InitialContext initialContext;
        try {
            initialContext = new InitialContext(contextInfo.properties);
        } catch (final NamingException ne) {
            throw new OpenEJBException(String.format("JndiProvider(id=\"%s\") could not be created.  Failed to create the InitialContext using the supplied properties",
                contextInfo.id), ne);
        }

        try {
            containerSystem.getJNDIContext().bind("openejb/remote_jndi_contexts/" + contextInfo.id, initialContext);
        } catch (final NamingException e) {
            throw new OpenEJBException("Cannot bind " + contextInfo.service + " with id " + contextInfo.id, e);
        }

        // Update the config tree
        config.facilities.remoteJndiContexts.add(contextInfo);

        logger.getChildLogger("service").debug("createService.success", contextInfo.service, contextInfo.id, contextInfo.className);
    }

    public void createContainer(final ContainerInfo serviceInfo) throws OpenEJBException {

        final ObjectRecipe serviceRecipe = createRecipe(Collections.<ServiceInfo>emptyList(), serviceInfo);

        serviceRecipe.setProperty("id", serviceInfo.id);
        serviceRecipe.setProperty("transactionManager", props.get(TransactionManager.class.getName()));
        serviceRecipe.setProperty("securityService", props.get(SecurityService.class.getName()));
        serviceRecipe.setProperty("properties", new UnsetPropertiesRecipe());

        // MDB container has a resource adapter string name that
        // must be replaced with the real resource adapter instance
        replaceResourceAdapterProperty(serviceRecipe);

        final Object service = serviceRecipe.create();

        serviceRecipe.getUnsetProperties().remove("id"); // we forced it
        serviceRecipe.getUnsetProperties().remove("securityService"); // we forced it
        logUnusedProperties(serviceRecipe, serviceInfo);

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        bindService(serviceInfo, service);

        setSystemInstanceComponent(interfce, service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        containerSystem.addContainer(serviceInfo.id, (Container) service);

        // Update the config tree
        config.containerSystem.containers.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);

        if (Container.class.isInstance(service) && LocalMBeanServer.isJMXActive()) {
            final ObjectName objectName = ObjectNameBuilder.uniqueName("containers", serviceInfo.id, service);
            try {
                LocalMBeanServer.get().registerMBean(new DynamicMBeanWrapper(new JMXContainer(serviceInfo, (Container) service)), objectName);
                containerObjectNames.add(objectName);
            } catch (final Exception | NoClassDefFoundError e) {
                // no-op
            }
        }
    }

    private void bindService(final ServiceInfo serviceInfo, final Object service) throws OpenEJBException {
        try {
            this.containerSystem.getJNDIContext().bind(JAVA_OPENEJB_NAMING_CONTEXT + serviceInfo.service + "/" + serviceInfo.id, service);
        } catch (final NamingException e) {
            throw new OpenEJBException(messages.format("assembler.cannotBindServiceWithId", serviceInfo.service, serviceInfo.id), e);
        }
    }

    public void removeContainer(final String containerId) {
        containerSystem.removeContainer(containerId);

        // Update the config tree
        for (final Iterator<ContainerInfo> iterator = config.containerSystem.containers.iterator(); iterator.hasNext(); ) {
            final ContainerInfo containerInfo = iterator.next();
            if (containerInfo.id.equals(containerId)) {
                iterator.remove();
                try {
                    this.containerSystem.getJNDIContext().unbind(JAVA_OPENEJB_NAMING_CONTEXT + containerInfo.service + "/" + containerInfo.id);
                } catch (final Exception e) {
                    logger.error("removeContainer.unbindFailed", containerId);
                }
            }
        }
    }

    public void createService(final ServiceInfo serviceInfo) throws OpenEJBException {
        final ObjectRecipe serviceRecipe = createRecipe(Collections.<ServiceInfo>emptyList(), serviceInfo);
        serviceRecipe.setProperty("properties", new UnsetPropertiesRecipe());

        final Object service = serviceRecipe.create();
        SystemInstance.get().addObserver(service);

        logUnusedProperties(serviceRecipe, serviceInfo);

        final Class<?> serviceClass = service.getClass();

        getContext().put(serviceClass.getName(), service);

        props.put(serviceClass.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        config.facilities.services.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void createProxyFactory(final ProxyFactoryInfo serviceInfo) throws OpenEJBException {

        final ObjectRecipe serviceRecipe = createRecipe(Collections.<ServiceInfo>emptyList(), serviceInfo);

        final Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        ProxyManager.registerFactory(serviceInfo.id, (ProxyFactory) service);
        ProxyManager.setDefaultFactory(serviceInfo.id);

        bindService(serviceInfo, service);

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        // Update the config tree
        config.facilities.intraVmServer = serviceInfo;

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    private void replaceResourceAdapterProperty(final ObjectRecipe serviceRecipe) throws OpenEJBException {
        final Object resourceAdapterId = serviceRecipe.getProperty("ResourceAdapter");
        if (resourceAdapterId instanceof String) {
            String id = (String) resourceAdapterId;
            id = id.trim();

            Object resourceAdapter = null;
            try {
                resourceAdapter = containerSystem.getJNDIContext().lookup("openejb/Resource/" + id);
            } catch (final NamingException e) {
                // handled below
            }

            if (Reference.class.isInstance(resourceAdapter)) {
                try {
                    resourceAdapter = Reference.class.cast(resourceAdapter).getContent();
                } catch (final NamingException e) {
                    // no-op: will fail after
                }
            }

            if (resourceAdapter == null) {
                throw new OpenEJBException("No existing resource adapter defined with id '" + id + "'.");
            }

            // if the resource adapter looked up is wrapped in a ResourceAdapterReference, unwrap it
            if (ResourceAdapterReference.class.isInstance(resourceAdapter)) {
                resourceAdapter = ResourceAdapterReference.class.cast(resourceAdapter).getRa();
            }

            if (!(resourceAdapter instanceof ResourceAdapter)) {
                throw new OpenEJBException(messages.format("assembler.resourceAdapterNotResourceAdapter", id, resourceAdapter.getClass()));
            }
            serviceRecipe.setProperty("ResourceAdapter", resourceAdapter);
        }
    }

    @Deprecated
    public void createResource(final ResourceInfo serviceInfo) throws OpenEJBException {
        createResource(null, serviceInfo);
    }

    public void createResource(final Collection<ServiceInfo> infos, final ResourceInfo serviceInfo) throws OpenEJBException {
        final boolean usesCdiPwdCipher = usesCdiPwdCipher(serviceInfo);
        final Object service = "true".equalsIgnoreCase(String.valueOf(serviceInfo.properties.remove("Lazy"))) || usesCdiPwdCipher ?
            newLazyResource(infos, serviceInfo) :
                doCreateResource(infos, serviceInfo);
        if (usesCdiPwdCipher && !serviceInfo.properties.contains("InitializeAfterDeployment")) {
            serviceInfo.properties.put("InitializeAfterDeployment", "true");
        }

        bindResource(serviceInfo.id, service, false);
        for (final String alias : serviceInfo.aliases) {
            bindResource(alias, service, false);
        }
        if (serviceInfo.originAppName != null && !serviceInfo.originAppName.isEmpty() && !"/".equals(serviceInfo.originAppName)
            && !serviceInfo.id.startsWith("global")) {
            final String baseJndiName = serviceInfo.id.substring(serviceInfo.originAppName.length() + 1);
            serviceInfo.aliases.add(baseJndiName);
            final ContextualJndiReference ref = new ContextualJndiReference(baseJndiName);
            ref.addPrefix(serviceInfo.originAppName);
            bindResource(baseJndiName, ref, false);
        }

        // Update the config tree
        config.facilities.resources.add(serviceInfo);

        if (logger.isDebugEnabled()) { // weird to check parent logger but save time and it is almost never activated
            logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
        }
    }

    private boolean usesCdiPwdCipher(final ResourceInfo serviceInfo) {
        for (final Object val : serviceInfo.properties.values()) {
            if (String.valueOf(val).startsWith("cipher:cdi:")) {
                return true;
            }
        }
        return false;
    }

    private LazyResource newLazyResource(final Collection<ServiceInfo> infos, final ResourceInfo serviceInfo) {
        return new LazyResource(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final boolean appClassLoader = "true".equals(serviceInfo.properties.remove("UseAppClassLoader"))
                        || serviceInfo.originAppName != null;

                final Thread thread = Thread.currentThread();
                final ClassLoader old = thread.getContextClassLoader();
                if (!appClassLoader) {
                    final ClassLoader classLoader = Assembler.class.getClassLoader();
                    thread.setContextClassLoader(classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader);
                } // else contextually we should have the app loader

                try {
                    return doCreateResource(infos, serviceInfo);
                } finally {
                    thread.setContextClassLoader(old);
                }
            }
        });
    }

    private Object doCreateResource(final Collection<ServiceInfo> infos, final ResourceInfo serviceInfo) throws OpenEJBException {
        final String skipPropertiesFallback = (String) serviceInfo.properties.remove("SkipPropertiesFallback"); // do it early otherwise we can loose it
        final ObjectRecipe serviceRecipe = createRecipe(infos, serviceInfo);
        final boolean properties = PropertiesFactory.class.getName().equals(serviceInfo.className);
        if ("false".equalsIgnoreCase(serviceInfo.properties.getProperty("SkipImplicitAttributes", "false")) && !properties) {
            serviceRecipe.setProperty("transactionManager", transactionManager);
            serviceRecipe.setProperty("ServiceId", serviceInfo.id);
        }
        serviceInfo.properties.remove("SkipImplicitAttributes");

        // if custom instance allow to skip properties fallback to avoid to set unexpectedly it - connectionProps of DBs
        final AtomicReference<Properties> injectedProperties = new AtomicReference<>();
        if (!"true".equalsIgnoreCase(skipPropertiesFallback)) {
            serviceRecipe.setProperty("properties", new UnsetPropertiesRecipe() {
                @Override
                protected Object internalCreate(final Type expectedType, final boolean lazyRefAllowed) throws ConstructionException {
                    final Map<String, Object> original = serviceRecipe.getUnsetProperties();
                    final Properties properties = new SuperProperties() {
                        @Override
                        public Object remove(final Object key) { // avoid to log them then
                            original.remove(key);
                            return super.remove(key);
                        }
                    }.caseInsensitive(true); // keep our nice case insensitive feature
                    for (final Map.Entry<String, Object> entry : original.entrySet()) {
                        properties.put(entry.getKey(), entry.getValue());
                    }
                    injectedProperties.set(properties);
                    return properties;
                }
            });
        } else { // this is not the best fallback we have but since it is super limited it is acceptable
            final Map<String, Object> unsetProperties = serviceRecipe.getUnsetProperties();
            injectedProperties.set(new Properties() {
                @Override
                public String getProperty(final String key) {
                    final Object obj = unsetProperties.get(key);
                    return String.class.isInstance(obj) ? String.valueOf(obj) : null;
                }

                @Override
                public Set<String> stringPropertyNames() {
                    return unsetProperties.keySet();
                }

                @Override
                public Set<Object> keySet() {
                    //noinspection unchecked
                    return Set.class.cast(unsetProperties.keySet());
                }

                @Override
                public synchronized boolean containsKey(final Object key) {
                    return getProperty(String.valueOf(key)) != null;
                }
            });
        }

        if (serviceInfo.types.contains("DataSource") || serviceInfo.types.contains(DataSource.class.getName())) {
            final Properties props = PropertyPlaceHolderHelper.simpleHolds(serviceInfo.properties);
            if (serviceInfo.properties.containsKey("Definition")) {
                final Object encoding = serviceInfo.properties.remove("DefinitionEncoding");
                try { // we catch classcast etc..., if it fails it is not important
                    final InputStream is = new ByteArrayInputStream(serviceInfo.properties.getProperty("Definition")
                        .getBytes(encoding != null ? encoding.toString() : "ISO-8859-1"));
                    final Properties p = new SuperProperties();
                    IO.readProperties(is, p);
                    for (final Entry<Object, Object> entry : p.entrySet()) {
                        final String key = entry.getKey().toString();
                        if (!props.containsKey(key)
                            // never override from Definition, just use it to complete the properties set
                            &&
                            !(key.equalsIgnoreCase("url") &&
                                props.containsKey("JdbcUrl"))) { // with @DataSource we can get both, see org.apache.openejb.config.ConvertDataSourceDefinitions.rawDefinition()
                            props.put(key, entry.getValue());
                        }
                    }
                } catch (final Exception e) {
                    // ignored
                }
            }
            serviceRecipe.setProperty("Definition", PropertiesHelper.propertiesToString(props));
        } // else: any other kind of resource relying on it? shouldnt be

        replaceResourceAdapterProperty(serviceRecipe);

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        boolean customLoader = false;
        try {
            if (serviceInfo.classpath != null && serviceInfo.classpath.length > 0) {
                final URL[] urls = new URL[serviceInfo.classpath.length];
                for (int i = 0; i < serviceInfo.classpath.length; i++) {
                    urls[i] = serviceInfo.classpath[i].toURL();
                }
                loader = new URLClassLoaderFirst(urls, loader);
                customLoader = true;
                serviceRecipe.setProperty("OpenEJBResourceClasspath", "true");
            }
        } catch (final MalformedURLException e) {
            throw new OpenEJBException("Unable to create a classloader for " + serviceInfo.id, e);
        }

        if (!customLoader && serviceInfo.classpathAPI != null) {
            throw new IllegalArgumentException("custom-api provided but not classpath used for " + serviceInfo.id);
        }

        Object service = serviceRecipe.create(loader);
        if (customLoader) {
            final Collection<Class<?>> apis;
            if (serviceInfo.classpathAPI == null) {
                apis = new ArrayList<>(Arrays.asList(service.getClass().getInterfaces()));
            } else {
                final String[] split = serviceInfo.classpathAPI.split(" *, *");
                apis = new ArrayList<>(split.length);
                final ClassLoader apiLoader = Thread.currentThread().getContextClassLoader();
                for (final String fqn : split) {
                    try {
                        apis.add(apiLoader.loadClass(fqn));
                    } catch (final ClassNotFoundException e) {
                        throw new IllegalArgumentException(fqn + " not usable as API for " + serviceInfo.id, e);
                    }
                }
            }

            if (apis.size() - (apis.contains(Serializable.class) ? 1 : 0) - (apis.contains(Externalizable.class) ? 1 : 0) > 0) {
                service = Proxy.newProxyInstance(loader, apis.toArray(new Class<?>[apis.size()]), new ClassLoaderAwareHandler(null, service, loader));
            } // else proxy would be useless
        }

        serviceInfo.unsetProperties = injectedProperties.get();

        // Java Connector spec ResourceAdapters and ManagedConnectionFactories need special activation
        if (service instanceof ResourceAdapter) {
            final ResourceAdapter resourceAdapter = (ResourceAdapter) service;

            // Create a thead pool for work manager
            final int threadPoolSize = getIntProperty(serviceInfo.properties, "threadPoolSize", 30);
            final Executor threadPool;
            if (threadPoolSize <= 0) {
                logger.warning("Thread pool for '" + serviceInfo.id + "' is (unbounded), consider setting a size using: " + serviceInfo.id + ".QueueSize=[size]");
                threadPool = Executors.newCachedThreadPool(new DaemonThreadFactory(serviceInfo.id + "-worker-"));
            } else {
                threadPool = new ExecutorBuilder()
                    .size(threadPoolSize)
                    .prefix(serviceInfo.id)
                    .threadFactory(new DaemonThreadFactory(serviceInfo.id + "-worker-"))
                    .build(new Options(serviceInfo.properties, SystemInstance.get().getOptions()));
                logger.info("Thread pool size for '" + serviceInfo.id + "' is (" + threadPoolSize + ")");
            }

            // WorkManager: the resource adapter can use this to dispatch messages or perform tasks
            final WorkManager workManager;
            if (GeronimoTransactionManager.class.isInstance(transactionManager)) {
                final GeronimoTransactionManager geronimoTransactionManager = (GeronimoTransactionManager) transactionManager;
                final TransactionContextHandler txWorkContextHandler = new TransactionContextHandler(geronimoTransactionManager);

                // use id as default realm name if realm is not specified in service properties
                final String securityRealmName = getStringProperty(serviceInfo.properties, "realm", serviceInfo.id);

                final SecurityContextHandler securityContextHandler = new SecurityContextHandler(securityRealmName);
                final HintsContextHandler hintsContextHandler = new HintsContextHandler();

                final Collection<WorkContextHandler> workContextHandlers = new ArrayList<>();
                workContextHandlers.add(txWorkContextHandler);
                workContextHandlers.add(securityContextHandler);
                workContextHandlers.add(hintsContextHandler);

                workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, workContextHandlers);
            } else {
                workManager = new SimpleWorkManager(threadPool);
            }

            // BootstrapContext: wraps the WorkMananger and XATerminator
            final BootstrapContext bootstrapContext;
            if (transactionManager instanceof GeronimoTransactionManager) {
                bootstrapContext = new GeronimoBootstrapContext(GeronimoWorkManager.class.cast(workManager),
                    (GeronimoTransactionManager) transactionManager,
                    (GeronimoTransactionManager) transactionManager);
            } else if (transactionManager instanceof XATerminator) {
                bootstrapContext = new SimpleBootstrapContext(workManager, (XATerminator) transactionManager);
            } else {
                bootstrapContext = new SimpleBootstrapContext(workManager);
            }

            // start the resource adapter
            try {
                logger.debug("createResource.startingResourceAdapter", serviceInfo.id, service.getClass().getName());
                resourceAdapter.start(bootstrapContext);
            } catch (final ResourceAdapterInternalException e) {
                throw new OpenEJBException(e);
            }

            final Map<String, Object> unset = serviceRecipe.getUnsetProperties();
            unset.remove("threadPoolSize");
            logUnusedProperties(unset, serviceInfo);

            registerAsMBean(serviceInfo.id, "ResourceAdapter", resourceAdapter);
            service = new ResourceAdapterReference(resourceAdapter, threadPool, OPENEJB_RESOURCE_JNDI_PREFIX + serviceInfo.id);
        } else if (service instanceof ManagedConnectionFactory) {
            final ManagedConnectionFactory managedConnectionFactory = (ManagedConnectionFactory) service;

            // connection manager is constructed via a recipe so we automatically expose all cmf properties
            final ObjectRecipe connectionManagerRecipe = new ObjectRecipe(GeronimoConnectionManagerFactory.class, "create");
            connectionManagerRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
            connectionManagerRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            connectionManagerRecipe.setAllProperties(serviceInfo.properties);
            connectionManagerRecipe.setProperty("name", serviceInfo.id);
            connectionManagerRecipe.setProperty("mcf", managedConnectionFactory);

            // standard properties
            connectionManagerRecipe.setProperty("transactionManager", transactionManager);
            ClassLoader classLoader = loader;
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            connectionManagerRecipe.setProperty("classLoader", classLoader);

            logger.getChildLogger("service").info("createResource.createConnectionManager", serviceInfo.id, service.getClass().getName());

            // create the connection manager
            final ConnectionManager connectionManager = (ConnectionManager) connectionManagerRecipe.create();


            String txSupport = "xa";
            try {
                txSupport = (String) connectionManagerRecipe.getProperty("transactionSupport");
            } catch (Exception e) {
                // ignore
            }

            if (txSupport == null || txSupport.trim().length() == 0) {
                txSupport = "xa";
            }

            if (connectionManager == null) {
                throw new OpenEJBRuntimeException(messages.format("assembler.invalidConnectionManager", serviceInfo.id));
            }

            final Map<String, Object> unsetA = serviceRecipe.getUnsetProperties();
            final Map<String, Object> unsetB = connectionManagerRecipe.getUnsetProperties();
            final Map<String, Object> unset = new HashMap<>();
            for (final Entry<String, Object> entry : unsetA.entrySet()) {
                if (unsetB.containsKey(entry.getKey())) {
                    unset.put(entry.getKey(), entry.getValue());
                }
            }

            // service becomes a ConnectorReference which merges connection manager and mcf
            service = new ConnectorReference(connectionManager, managedConnectionFactory);

            // init cm if needed
            final Object eagerInit = unset.remove("eagerInit");
            if (eagerInit != null && eagerInit instanceof String && "true".equalsIgnoreCase((String) eagerInit)
                && connectionManager instanceof AbstractConnectionManager) {
                try {
                    ((AbstractConnectionManager) connectionManager).doStart();
                    try {
                        final Object cf = managedConnectionFactory.createConnectionFactory(connectionManager);
                        if (cf instanceof ConnectionFactory) {
                            final Connection connection = ((ConnectionFactory) cf).getConnection();
                            connection.getMetaData();
                            connection.close();
                        }
                    } catch (final Exception e) {
                        // no-op: just to force eager init of pool
                    }
                } catch (final Exception e) {
                    logger.warning("Can't start connection manager", e);
                }
            }

            logUnusedProperties(unset, serviceInfo);
        } else if (service instanceof DataSource) {
            ClassLoader classLoader = loader;
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            final ImportSql importer = new ImportSql(classLoader, serviceInfo.id, (DataSource) service);
            if (importer.hasSomethingToImport()) {
                importer.doImport();
            }

            final ObjectRecipe recipe = DataSourceFactory.forgetRecipe(service, serviceRecipe);
            if (recipe != serviceRecipe || !serviceInfo.properties.containsKey("XaDataSource")) {
                logUnusedProperties(recipe, serviceInfo);
            } // else logged on xadatasource itself

            final Properties prop = serviceInfo.properties;
            String url = prop.getProperty("JdbcUrl", prop.getProperty("url"));
            if (url == null) {
                url = prop.getProperty("jdbcUrl");
            }
            if (url == null) {
                logger.debug("Unable to find url for " + serviceInfo.id + " will not monitor it");
            } else {
                final String host = extractHost(url);
                if (host != null) {
                    remoteResourceMonitor.addHost(host);
                    remoteResourceMonitor.registerIfNot();
                }
            }
        } else if (!Properties.class.isInstance(service)) {
            if (serviceInfo.unsetProperties == null || isTemplatizedResource(serviceInfo)) {
                logUnusedProperties(serviceRecipe, serviceInfo);
            } // else wait post construct

            registerAsMBean(serviceInfo.id, "Resource", service);
        }

        final ResourceCreated event = new ResourceCreated(service, serviceInfo.id);
        SystemInstance.get().fireEvent(event);
        return event.getReplacement() == null ? service : event.getReplacement();
    }

    private void registerAsMBean(final String name, final String type, Object resource) {
        final MBeanServer server = LocalMBeanServer.get();

        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("j2eeType", "");
        jmxName.set("name", name);

        try {
            final ObjectName objectName = jmxName.set("j2eeType", type).build();
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }

            if (DynamicMBean.class.isInstance(resource)) {
                server.registerMBean(resource, objectName);
                logger.debug("Registered JMX name: " + objectName.toString());
            } else {
                server.registerMBean(new MBeanPojoWrapper(name, resource), objectName);
                logger.debug("Registered JMX name: " + objectName.toString());
            }
        } catch (final Exception e) {
            logger.error("Unable to register MBean ", e);
        }
    }

    private void bindResource(final String id, final Object service, final boolean canReplace) throws OpenEJBException {
        final String name = OPENEJB_RESOURCE_JNDI_PREFIX + id;
        final Context jndiContext = containerSystem.getJNDIContext();
        Object existing = null;
        try {
            ContextualJndiReference.followReference.set(false);
            existing = jndiContext.lookup(name);
        } catch (final Exception ignored) {
            // no-op
        } finally {
            ContextualJndiReference.followReference.remove(); // if the lookup fails the remove is not done
        }

        boolean rebind = false;
        if (existing != null) {
            final boolean existingIsContextual = ContextualJndiReference.class.isInstance(existing);
            final boolean serviceIsExisting = ContextualJndiReference.class.isInstance(service);
            if (!existingIsContextual && serviceIsExisting) {
                ContextualJndiReference.class.cast(service).setDefaultValue(existing);
                rebind = true;
            } else if (existingIsContextual && !serviceIsExisting) {
                ContextualJndiReference.class.cast(existing).setDefaultValue(service);
            } else if (existingIsContextual) { // && serviceIsExisting is always true here
                final ContextualJndiReference contextual = ContextualJndiReference.class.cast(existing);
                if (canReplace && contextual.prefixesSize() == 1) { // replace!
                    contextual.removePrefix(contextual.lastPrefix());
                    contextual.setDefaultValue(service);
                } else {
                    contextual.addPrefix(ContextualJndiReference.class.cast(service).lastPrefix());
                }
                return;
            }
        }

        try {
            if (canReplace && existing != null) {
                jndiContext.unbind(name);
            }
            if (rebind) {
                jndiContext.rebind(name, service);
            } else {
                jndiContext.bind(name, service);
            }
        } catch (final NameAlreadyBoundException nabe) {
            logger.warning("unbounding resource " + name + " can happen because of a redeployment or because of a duplicated id");
            try {
                jndiContext.unbind(name);
                jndiContext.bind(name, service);
            } catch (final NamingException e) {
                throw new OpenEJBException("Cannot bind resource adapter with id " + id, e);
            }
        } catch (final NamingException e) {
            throw new OpenEJBException("Cannot bind resource adapter with id " + id, e);
        }
    }

    private static String extractHost(final String url) { // can be enhanced
        if (url == null || !url.contains("://")) {
            return null;
        }

        final int idx = url.indexOf("://");
        final String subUrl = url.substring(idx + 3);
        final int port = subUrl.indexOf(':');
        final int slash = subUrl.indexOf('/');

        int end = port;
        if (end < 0 || slash > 0 && slash < end) {
            end = slash;
        }
        if (end > 0) {
            return subUrl.substring(0, end);
        }

        return subUrl;
    }

    private int getIntProperty(final Properties properties, final String propertyName, final int defaultValue) {
        final String propertyValue = getStringProperty(properties, propertyName, Integer.toString(defaultValue));
        if (propertyValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(propertyValue);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(propertyName + " is not an integer " + propertyValue, e);
        }
    }

    private String getStringProperty(final Properties properties, final String propertyName, final String defaultValue) {
        final String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }

        return propertyValue;
    }

    public void createConnectionManager(final ConnectionManagerInfo serviceInfo) throws OpenEJBException {

        final ObjectRecipe serviceRecipe = createRecipe(Collections.<ServiceInfo>emptyList(), serviceInfo);

        final Object object = props.get("TransactionManager");
        serviceRecipe.setProperty("transactionManager", object);

        final Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        bindService(serviceInfo, service);

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        // Update the config tree
        config.facilities.connectionManagers.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void createSecurityService(final SecurityServiceInfo serviceInfo) throws OpenEJBException {

        Object service = SystemInstance.get().getComponent(SecurityService.class);
        if (service == null) {
            final ObjectRecipe serviceRecipe = createRecipe(Collections.<ServiceInfo>emptyList(), serviceInfo);
            service = serviceRecipe.create();
            logUnusedProperties(serviceRecipe, serviceInfo);
        }

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind(JAVA_OPENEJB_NAMING_CONTEXT + serviceInfo.service, service);
        } catch (final NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        this.securityService = (SecurityService) service;

        // Update the config tree
        config.facilities.securityService = serviceInfo;

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void createTransactionManager(final TransactionServiceInfo serviceInfo) throws OpenEJBException {

        Object service = SystemInstance.get().getComponent(TransactionManager.class);
        if (service == null) {
            final ObjectRecipe serviceRecipe = createRecipe(Collections.<ServiceInfo>emptyList(), serviceInfo);
            service = serviceRecipe.create();
            logUnusedProperties(serviceRecipe, serviceInfo);
        } else {
            logger.info("Reusing provided TransactionManager " + service);
        }

        final Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind(JAVA_OPENEJB_NAMING_CONTEXT + serviceInfo.service, service);
            this.containerSystem.getJNDIContext().bind("comp/UserTransaction", new CoreUserTransaction((TransactionManager) service));
            this.containerSystem.getJNDIContext().bind("comp/TransactionManager", service);
        } catch (final NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        this.transactionManager = (TransactionManager) service;

        // Update the config tree
        config.facilities.transactionService = serviceInfo;

        // todo find a better place for this

        // TransactionSynchronizationRegistry
        final TransactionSynchronizationRegistry synchronizationRegistry;
        if (transactionManager instanceof TransactionSynchronizationRegistry) {
            synchronizationRegistry = (TransactionSynchronizationRegistry) transactionManager;
        } else {
            // todo this should be built
            synchronizationRegistry = new SimpleTransactionSynchronizationRegistry(transactionManager);
        }

        Assembler.getContext().put(TransactionSynchronizationRegistry.class.getName(), synchronizationRegistry);
        SystemInstance.get().setComponent(TransactionSynchronizationRegistry.class, synchronizationRegistry);

        try {
            this.containerSystem.getJNDIContext().bind("comp/TransactionSynchronizationRegistry", new TransactionSynchronizationRegistryWrapper());
        } catch (final NamingException e) {
            throw new OpenEJBException("Cannot bind java:comp/TransactionSynchronizationRegistry", e);
        }

        // JtaEntityManagerRegistry
        // todo this should be built
        final JtaEntityManagerRegistry jtaEntityManagerRegistry = new JtaEntityManagerRegistry(synchronizationRegistry);
        Assembler.getContext().put(JtaEntityManagerRegistry.class.getName(), jtaEntityManagerRegistry);
        SystemInstance.get().setComponent(JtaEntityManagerRegistry.class, jtaEntityManagerRegistry);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public static void logUnusedProperties(final ObjectRecipe serviceRecipe, final ServiceInfo info) {
        final Map<String, Object> unsetProperties = serviceRecipe.getUnsetProperties();
        logUnusedProperties(unsetProperties, info);
    }

    private static void logUnusedProperties(final Map<String, ?> unsetProperties, final ServiceInfo info) {
        if (isPassthroughType(info)) {
            return;
        }

        final boolean ignoreJdbcDefault = "Annotation".equalsIgnoreCase(info.properties.getProperty("Origin"));
        Logger logger = null;
        for (final String property : unsetProperties.keySet()) {
            //TODO: DMB: Make more robust later
            if (ignoreJdbcDefault && ("JdbcUrl".equals(property) || "UserName".equals(property) || "Password".equals(property) || "PasswordCipher".equals(property))) {
                continue;
            }
            if (property.equalsIgnoreCase("Definition")) {
                continue;
            }
            if (property.equalsIgnoreCase("SkipImplicitAttributes")) {
                continue;
            }
            if (property.equalsIgnoreCase("JndiName")) {
                continue;
            }
            if (property.equalsIgnoreCase("Origin")) {
                continue;
            }
            if (property.equalsIgnoreCase("DatabaseName")) {
                continue;
            }
            if (property.equalsIgnoreCase("connectionAttributes")) {
                return;
            }

            if (property.equalsIgnoreCase("properties")) {
                return;
            }
            if (property.equalsIgnoreCase("ApplicationWide")) {
                continue;
            }
            if (property.equalsIgnoreCase("OpenEJBResourceClasspath")) {
                continue;
            }
            if (isInternalProperty(property)) {
                continue;
            }
            if (info.types.isEmpty() && "class".equalsIgnoreCase(property)) {
                continue; // inline service (no sp)
            }
            if ("destination".equalsIgnoreCase(property) && info.id.equals(unsetProperties.get("destination"))) {
                continue;
            }

            if (logger == null) {
                final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                if (assembler != null) {
                    logger = assembler.logger;
                }else {
                    System.err.println("Assembler has not been initialized");
                }
            }
            unusedProperty(info.id, logger, property);
        }
    }

    private static boolean isPassthroughType(final ServiceInfo info) {
        return info.types.contains("javax.mail.Session");
    }

    private static void unusedProperty(final String id, final Logger parentLogger, final String property) {
        if (isInternalProperty(property)) {
            return;
        }
        final String msg = "unused property '" + property + "' for resource '" + id + "'";
        if (null != parentLogger) {
            parentLogger.getChildLogger("service").warning(msg);
        } else { // note: we should throw an exception if this is called, shouldnt be possible in our lifecycle
            System.out.println(msg);
        }
    }

    private static boolean isInternalProperty(final String property) {
        return property.equalsIgnoreCase("ServiceId") || property.equalsIgnoreCase("transactionManager");
    }

    private static void unusedProperty(final String id, final String property) {
        final Assembler component = SystemInstance.get().getComponent(Assembler.class);
        final Logger logger = component != null ? component.logger : null;
        unusedProperty(id, logger, property);
    }

    public static ObjectRecipe prepareRecipe(final ServiceInfo info) {
        final String[] constructorArgs = info.constructorArgs.toArray(new String[info.constructorArgs.size()]);
        final ObjectRecipe serviceRecipe = new ObjectRecipe(info.className, info.factoryMethod, constructorArgs, null);
        serviceRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        serviceRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        serviceRecipe.allow(Option.PRIVATE_PROPERTIES);
        return serviceRecipe;
    }

    private ObjectRecipe createRecipe(final Collection<ServiceInfo> services, final ServiceInfo info) {
        final Logger serviceLogger = logger.getChildLogger("service");

        if (info instanceof ResourceInfo) {
            final List<String> aliasesList = ((ResourceInfo) info).aliases;
            if (!aliasesList.isEmpty()) {
                final String aliases = Join.join(", ", aliasesList);
                serviceLogger.info("createServiceWithAliases", info.service, info.id, aliases);
            } else {
                serviceLogger.info("createService", info.service, info.id);
            }
        } else {
            serviceLogger.info("createService", info.service, info.id);
        }

        final ObjectRecipe serviceRecipe = prepareRecipe(info);
        final Object value = info.properties.remove("SkipImplicitAttributes"); // we don't want this one to go in recipe
        final Properties allProperties = PropertyPlaceHolderHelper.simpleHolds(info.properties);
        allProperties.remove("SkipPropertiesFallback");
        if (services == null) { // small optim for internal resources
            serviceRecipe.setAllProperties(allProperties);
        } else {
            info.properties = allProperties;
            ServiceInfos.setProperties(services, info, serviceRecipe);
        }
        if (value != null) {
            info.properties.put("SkipImplicitAttributes", value);
        }

        if (serviceLogger.isDebugEnabled()) {
            for (final Map.Entry<String, Object> entry : serviceRecipe.getProperties().entrySet()) {
                serviceLogger.debug("createService.props", entry.getKey(), entry.getValue());
            }
        }
        return serviceRecipe;
    }

    @SuppressWarnings({"unchecked"})
    private void setSystemInstanceComponent(final Class interfce, final Object service) {
        SystemInstance.get().setComponent(interfce, service);
    }

    private URL toUrl(final String jarPath) throws OpenEJBException {
        try {
            return new File(jarPath).toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new OpenEJBException(messages.format("cl0001", jarPath, e.getMessage()), e);
        }
    }

    private static class PersistenceClassLoaderHandlerImpl implements PersistenceClassLoaderHandler {
        private static final AtomicBoolean logged = new AtomicBoolean(false);

        private final Map<String, List<ClassFileTransformer>> transformers = new TreeMap<>();

        @Override
        public void addTransformer(final String unitId, final ClassLoader classLoader, final ClassFileTransformer classFileTransformer) {
            final Instrumentation instrumentation = Agent.getInstrumentation();
            if (instrumentation != null) {
                instrumentation.addTransformer(classFileTransformer);

                if (unitId != null) {
                    List<ClassFileTransformer> transformers = this.transformers.computeIfAbsent(unitId, k -> new ArrayList<>(1));
                    transformers.add(classFileTransformer);
                }
            } else if (!logged.getAndSet(true)) {
                final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                if (assembler != null) {
                    assembler.logger.info("assembler.noAgent");
                } else {
                    System.err.println("addTransformer: Assembler not initialized: JAVA AGENT NOT INSTALLED");
                }
            }
        }

        @Override
        public void destroy(final String unitId) {
            final List<ClassFileTransformer> transformers = this.transformers.remove(unitId);
            if (transformers != null) {
                final Instrumentation instrumentation = Agent.getInstrumentation();
                if (instrumentation != null) {
                    for (final ClassFileTransformer transformer : transformers) {
                        instrumentation.removeTransformer(transformer);
                    }
                } else {
                    final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                    if (assembler != null) {
                        assembler.logger.info("assembler.noAgent");
                    }else {
                        System.err.println("destroy: Assembler not initialized: JAVA AGENT NOT INSTALLED");
                    }
                }
            }
        }

        @Override
        public ClassLoader getNewTempClassLoader(final ClassLoader classLoader) {
            return ClassLoaderUtil.createTempClassLoader(classLoader);
        }
    }

    public static class DeploymentListenerObserver {

        private final DeploymentListener delegate;

        public DeploymentListenerObserver(final DeploymentListener deploymentListener) {
            delegate = deploymentListener;
        }

        public void afterApplicationCreated(@Observes final AssemblerAfterApplicationCreated event) {
            delegate.afterApplicationCreated(event.getApp());
        }

        public void beforeApplicationDestroyed(@Observes final AssemblerBeforeApplicationDestroyed event) {
            delegate.beforeApplicationDestroyed(event.getApp());
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DeploymentListenerObserver)) {
                return false;
            }

            final DeploymentListenerObserver that = (DeploymentListenerObserver) o;

            return !(!Objects.equals(delegate, that.delegate));
        }

        @Override
        public int hashCode() {
            return delegate != null ? delegate.hashCode() : 0;
        }
    }

    private static final class DestroyingResource {
        private final String name;
        private final String clazz;
        private final Object instance;

        private DestroyingResource(final String name, final String clazz, final Object instance) {
            this.name = name;
            this.clazz = clazz;
            this.instance = instance;
        }
    }

    public static final class ResourceAdapterReference extends Reference {
        private final transient ResourceAdapter ra;
        private final transient Executor pool;
        private final String jndi;

        public ResourceAdapterReference(final ResourceAdapter ra, final Executor pool, final String jndi) {
            this.ra = ra;
            this.pool = pool;
            this.jndi = jndi;
        }

        public Executor getPool() {
            return pool;
        }

        public ResourceAdapter getRa() {
            return ra;
        }

        public String getJndi() {
            return jndi;
        }

        @Override
        public Object getObject() throws NamingException {
            return ra;
        }

        protected Object readResolve() throws ObjectStreamException {
            try {
                final ContainerSystem component = SystemInstance.get().getComponent(ContainerSystem.class);
                if (component != null) {
                    return component.getJNDIContext().lookup(jndi);
                } else {
                    throw new NamingException("ContainerSystem has not been initialized");
                }
            } catch (final NamingException e) {
                final InvalidObjectException objectException = new InvalidObjectException("name not found: " + jndi);
                objectException.initCause(e);
                throw objectException;
            }
        }
    }

    public static class LazyResource extends LazyObjectReference<Object> {
        public LazyResource(final Callable<Object> creator) {
            super(creator);
        }

        Object writeReplace() throws ObjectStreamException {
            try {
                return getObject();
            } catch (final NamingException e) {
                return null;
            }
        }
    }

    public static class ResourceInstance extends Reference implements Serializable, DestroyableResource {
        private final String name;
        private final Object delegate;
        private final transient Collection<Method> preDestroys;
        private final transient CreationalContext<?> context;
        private volatile boolean destroyed = false;

        public ResourceInstance(final String name, final Object delegate, final Collection<Method> preDestroys, final CreationalContext<?> context) {
            this.name = name;
            this.delegate = delegate;
            this.preDestroys = preDestroys;
            this.context = context;
        }

        @Override
        public Object getObject() throws NamingException {
            return delegate;
        }

        @Override
        public synchronized void destroyResource() {
            if (destroyed) {
                return;
            }
            final Object o = unwrapReference(delegate);
            for (final Method m : preDestroys) {
                try {
                    if (!m.isAccessible()) {
                        SetAccessible.on(m);
                    }
                    m.invoke(o);
                } catch (final Exception e) {
                    final Assembler component = SystemInstance.get().getComponent(Assembler.class);
                    if (component != null) {
                        component.logger.error(e.getMessage(), e);
                    }else {
                        System.err.println("" + e.getMessage());
                    }
                }
            }
            try {
                if (context != null) {
                    context.release();
                }
            } catch (final Exception e) {
                // no-op
            }
            destroyed = true;
        }

        // we don't care unwrapping the resource here since we want to keep ResourceInstance data for destruction
        // which is never serialized (IvmContext)
        Object readResolve() throws ObjectStreamException {
            try {
                final ContainerSystem component = SystemInstance.get().getComponent(ContainerSystem.class);
                if (component != null) {
                    return component.getJNDIContext().lookup(name);
                }else {
                    throw new Exception("ContainerSystem is not initialized");
                }
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
