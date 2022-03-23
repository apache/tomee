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

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ConnectorModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.ProvidedClassLoaderFinder;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Exceptions;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OptionsLog;
import org.apache.openejb.util.ServiceManagerProxy;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.web.lifecycle.test.MockHttpSession;
import org.apache.webbeans.web.lifecycle.test.MockServletContext;
import org.apache.xbean.naming.context.ContextFlyweight;

import jakarta.ejb.EJBException;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ValidationException;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;

import static org.apache.openejb.cdi.ScopeHelper.startContexts;
import static org.apache.openejb.cdi.ScopeHelper.stopContexts;

/**
 * Embeddable {@link EJBContainer} implementation based
 * on OpenEJB container.
 */
public final class OpenEjbContainer extends EJBContainer {

    static {
        // if tomee embedded was ran we'll lost log otherwise
        final String logManager = JavaSecurityManagers.getSystemProperty("java.util.logging.manager");
        if (logManager != null) {
            try {
                Thread.currentThread().getContextClassLoader().loadClass(logManager);
            } catch (final Exception ignored) {
                Field field = null;
                boolean accessible = false;
                try {
                    field = LogManager.class.getDeclaredField("manager");
                    if(field != null){
                        accessible = field.isAccessible();
                        field.setAccessible(true);
                        field.set(null, new JuliLogStreamFactory.OpenEJBLogManager());
                    }

                } catch (final Exception ignore) {
                    // ignore
                }finally{
                    if(field != null){
                        field.setAccessible(accessible);
                    }
                }
            }
        }
    }

    public static final String OPENEJB_EMBEDDED_REMOTABLE = "openejb.embedded.remotable";

    public static final String OPENEJB_EJBCONTAINER_CLOSE = "openejb.ejbcontainer.close";

    public static final String OPENEJB_EJBCONTAINER_CLOSE_SINGLE = "single-jvm";

    private static OpenEjbContainer instance;

    // initialized lazily to get the logging config from properties
    private static Logger logger;

    private ServiceManagerProxy serviceManager;

    private final Options options;

    private final OpenEjbContainer.GlobalContext globalJndiContext;

    private final WebBeansContext webBeanContext;

    private volatile ServletContext servletContext;

    private volatile HttpSession session;

    /**
     * Creates an embedded open ejb container.
     * @param map configuration map
     * @param appContext {@link AppContext} instance
     */
    private OpenEjbContainer(final Map<?, ?> map, final AppContext appContext) {
        webBeanContext = appContext.getWebBeansContext();
        globalJndiContext = new GlobalContext(appContext.getGlobalJndiContext());

        final Properties properties = new Properties();
        properties.putAll(map);

        options = new Options(properties);

        startNetworkServices();

        if (webBeanContext != null) {
            servletContext = new MockServletContext();
            session = new MockHttpSession();
            try {
                startContexts(webBeanContext.getContextsService(), servletContext, session);
            } catch (final Exception e) {
                logger().warning("can't start all CDI contexts", e);
            }
        }
    }

    @Override
    public void close() {
        if (isSingleClose()) {
            return;
        }
        doClose();
    }

    private static boolean isSingleClose() {
        return OPENEJB_EJBCONTAINER_CLOSE_SINGLE.equals(
                SystemInstance.get().getProperty(OPENEJB_EJBCONTAINER_CLOSE, "by-invocation"));
    }

    private synchronized void doClose() {
        if (instance == null) {
            return;
        }

        if (serviceManager != null) {
            serviceManager.stop();
        }
        try {
            globalJndiContext.close();
        } catch (final NamingException e) {
            throw new IllegalStateException(e);
        }

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        if (assembler != null) {
            for (final AppInfo info : assembler.getDeployedApplications()) {
                try {
                    assembler.destroyApplication(info);
                } catch (final UndeployException e) {
                    logger().error(e.getMessage(), e);
                }
            }
        }

        if (webBeanContext != null) {
            try {
                stopContexts(webBeanContext.getContextsService(), servletContext, session);
            } catch (final Exception e) {
                logger().warning("can't stop all CDI contexts", e);
            }
        }

        logger().info("Destroying OpenEJB container");
        OpenEJB.destroy();
        instance = null;
    }

    @Override
    public Context getContext() {
        return globalJndiContext;
    }


    private void startNetworkServices() {
        if (!options.get(OPENEJB_EMBEDDED_REMOTABLE, false)) {
            return;
        }

        try {
            serviceManager = new ServiceManagerProxy();
            serviceManager.start();
        } catch (final ServiceManagerProxy.AlreadyStartedException e) {
            logger().debug("Network services already started.  Ignoring option " + OPENEJB_EMBEDDED_REMOTABLE);
        }
    }

    private static Logger logger() { // don't trigger init too eagerly to be sure to be configured
        if (logger == null) {
            logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, OpenEjbContainer.class);
        }
        return logger;
    }

    public static class Provider implements EJBContainerProvider {

        public static final String OPENEJB_ADDITIONNAL_CALLERS_KEY = "openejb.additionnal.callers";

        @Override
        public EJBContainer createEJBContainer(Map<?, ?> map) {
            if (map == null) {
                map = new HashMap<>();
            }

            if (isOtherProvider(map)) {
                return null;
            }

            if (instance != null || OpenEJB.isInitialized()) {
                if (!isSingleClose()) {
                    logger().info("EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization");
                }
                return instance;
            }

            try {

                //reset URL Packages
                this.resetUrlPackages();

                //Initialize properties
                final Properties properties = new Properties();
                properties.putAll(map);

                //Initialize System Instance
                this.doInitialize(properties);

                //Create app context instance
                AppContext appContext = this.createAppContext(map);

                //Create the embedded container
                final OpenEjbContainer openEjbContainer = instance = new OpenEjbContainer(map, appContext);

                //Single close
                if (isSingleClose()) {
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            if (instance != null) {
                                instance.doClose();
                            }
                        }
                    });
                }

                return openEjbContainer;

            } catch (final OpenEJBException | MalformedURLException e) {
                throw new EJBException(e);
            } catch (final ValidationException ve) {
                throw ve;
            } catch (final Exception e) {
                if (e instanceof EJBException) {
                    throw (EJBException) e;
                }
                throw new InitializationException(e);
            } finally {
                if (instance == null && OpenEJB.isInitialized()) {
                    try {
                        OpenEJB.destroy();
                    } catch (final Exception e) {
                        // no-op
                    }
                }
            }
        }

        /**
         * Reset to be able to run this container then tomee one etc...
         */
        private void resetUrlPackages(){
            if (JavaSecurityManagers.getSystemProperty(Context.URL_PKG_PREFIXES) != null) {
                JavaSecurityManagers.removeSystemProperty(Context.URL_PKG_PREFIXES);
            }
        }

        /**
         * Initialize the {@link SystemInstance}
         * @param properties properties instance
         * @throws Exception if any problem occurs
         */
        private void doInitialize(final Properties properties) throws Exception{
            SystemInstance.reset();
            SystemInstance.init(properties);
            SystemInstance.get().setProperty("openejb.embedded", "true");
            SystemInstance.get().setProperty(EJBContainer.class.getName(), "true");

            if (SystemInstance.get().getComponent(ParentClassLoaderFinder.class) == null) {
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                if (tccl == null) {
                    tccl = OpenEjbContainer.class.getClassLoader();
                }
                SystemInstance.get().setComponent(ParentClassLoaderFinder.class, new ProvidedClassLoaderFinder(tccl));
            }

            //Install option log
            OptionsLog.install();

            //Initialize openEjb
            OpenEJB.init(properties);

            //Warmup class
            // don't do it too eagerly to avoid to not have properties
            Core.warmup();

            //Reload ALTDD
            // otherwise hard to use multiple altdd with several start/stop in the same JVM
            DeploymentLoader.reloadAltDD();

        }


        /**
         * Creates the {@link AppContext} instance.
         * @param map map instance
         * @return the {@link AppContext} instance
         * @throws MalformedURLException
         * @throws OpenEJBException
         */
        private AppContext createAppContext(Map<?, ?> map) throws MalformedURLException, OpenEJBException {
            final ConfigurationFactory configurationFactory = new ConfigurationFactory();
            final AppModule appModule = createAppModule(map, configurationFactory);

            final Set<String> callers;
            if (map.containsKey(OPENEJB_ADDITIONNAL_CALLERS_KEY)) {
                callers = new LinkedHashSet<>();
                callers.addAll(Arrays.asList(((String) map.get(OPENEJB_ADDITIONNAL_CALLERS_KEY)).split(",")));
            } else {
                callers = NewLoaderLogic.callers();
            }

            final EjbJar ejbJar = new EjbJar();
            final OpenejbJar openejbJar = new OpenejbJar();
            for (final String caller : callers) {

                if (!isValidEjbBean(caller)) {
                    continue;
                }

                String name = caller;
                if (name.contains("$")) {
                    name = caller.replace("$", "_");
                }

                final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(name, caller, true));
                bean.localBean();

                // set it to bean so it can get UserTransaction injection
                bean.setTransactionType(TransactionType.BEAN);

                final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);

                // important in case any other deploment id formats are specified
                ejbDeployment.setDeploymentId(name);
            }

            final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);
            ejbModule.getProperties().setProperty("openejb.cdi.activated", "false"); // BeanManagerImpl will likely be empty
            ejbModule.setBeans(new Beans()); // avoid warnings but not effectvely used
            appModule.getEjbModules().add(ejbModule);


            final AppInfo appInfo;
            try {
                appInfo = configurationFactory.configureApplication(appModule);
            } catch (final ValidationFailedException e) {
                // DO not include the stacktrace in the message
                logger().warning("configureApplication.loadFailed", appModule.getModuleId(), e.getMessage());
                throw new InvalidApplicationException(e);

            } catch (final OpenEJBException e) {
                // DO NOT REMOVE THE EXCEPTION FROM THIS LOG MESSAGE
                // removing this message causes NO messages to be printed when embedded
                logger().warning("configureApplication.loadFailed", e, appModule.getModuleId(), e.getMessage());
                throw new ConfigureApplicationException(e);
            }

            final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            final AppContext appContext;
            try {
                appContext = assembler.createApplication(appInfo, appModule.getClassLoader());
            } catch (final ValidationException ve) {
                throw ve;
            } catch (final Exception e) {
                throw new AssembleApplicationException(e);
            }

            return appContext;
        }

        /**
         * Check that caller is the valid EJB Bean
         * @param caller caller
         * @return the true if valid false otherwise.
         */
        private boolean isValidEjbBean(final String caller) {
            try {
                final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                final Class<?> clazz = loader.loadClass(caller);
                final int modifiers = clazz.getModifiers();
                return !clazz.isEnum() && !clazz.isInterface() && !Modifier.isAbstract(modifiers);
            } catch (final ClassNotFoundException e) {
                return false;
            }
        }

        /**
         * Creates and returns the {@link AppModule} instance.
         * @param map map instance
         * @param configurationFactory {@link ConfigurationFactory} instance
         * @return the {@link AppModule}
         * @throws MalformedURLException
         * @throws OpenEJBException
         */
        private AppModule createAppModule(final Map<?, ?> map, final ConfigurationFactory configurationFactory) throws MalformedURLException, OpenEJBException {
            final List<File> moduleLocations;
            final String appId = (String) map.get(EJBContainer.APP_NAME);
            final Object modules = map.get(EJBContainer.MODULES);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            if (modules instanceof String) {
                moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);
                moduleLocations.removeIf(file -> !match((String) modules, file));
            } else if (modules instanceof String[]) {
                // TODO Optimize this so we look specifically for modules by name
                moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);
                int matched = 0;

                for (final Iterator<File> i = moduleLocations.iterator(); i.hasNext(); ) {
                    final File file = i.next();
                    boolean remove = true;
                    for (final String s : (String[]) modules) {
                        if (match(s, file)) {
                            remove = false;
                            matched++;
                            break;
                        }
                    }
                    if (remove) {
                        i.remove();
                    }
                }

                if (matched != ((String[]) modules).length) {
                    throw specifiedModulesNotFound();
                }

            } else if (modules instanceof File) {
                final URL url = ((File) modules).toURI().toURL();
                classLoader = new URLClassLoader(new URL[]{url}, classLoader);
                moduleLocations = Collections.singletonList((File) modules);
            } else if (modules instanceof File[]) {
                final File[] files = (File[]) modules;
                final URL[] urls = new URL[files.length];
                for (int i = 0; i < urls.length; i++) {
                    urls[i] = files[i].toURI().toURL();
                }
                classLoader = new URLClassLoader(urls, classLoader);
                moduleLocations = Arrays.asList((File[]) modules);
            } else if (modules == null) {
                moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);
            } else {
                final AppModule appModule = createAppModule(map);
                if (appModule != null) {
                    return appModule;
                }
                throw invalidModulesValue(modules);
            }

            if (moduleLocations.isEmpty()) {
                throw Exceptions.newNoModulesFoundException();
            }

            // TODO With this createAppModule method we can finally do some checking on module ids to really implement EJBContainer.MODULES String/String[]
            return configurationFactory.loadApplication(classLoader, appId, moduleLocations);
        }

        private AppModule createAppModule(final Map<?, ?> map) {

            final String appId = (String) map.get(EJBContainer.APP_NAME);
            final Object modules = map.get(EJBContainer.MODULES);
            Application application = null;
            AppModule appModule = new AppModule(this.getClass().getClassLoader(), appId);

            if (modules instanceof EjbJar) {
                final EjbJar ejbJar = (EjbJar) modules;
                appModule.getEjbModules().add(new EjbModule(ejbJar));
            } else if (modules instanceof EnterpriseBean) {
                final EnterpriseBean bean = (EnterpriseBean) modules;
                final EjbJar ejbJar = new EjbJar();
                ejbJar.addEnterpriseBean(bean);
                appModule.getEjbModules().add(new EjbModule(ejbJar));

            } else if (modules instanceof Application) {
                application = (Application) modules;
            } else if (modules instanceof Connector) {
                final Connector connector = (Connector) modules;
                appModule.getConnectorModules().add(new ConnectorModule(connector));
            } else if (modules instanceof org.apache.openejb.jee.jpa.unit.Persistence) {
                final org.apache.openejb.jee.jpa.unit.Persistence persistence = (org.apache.openejb.jee.jpa.unit.Persistence) modules;
                appModule.addPersistenceModule(new PersistenceModule(appModule, "", persistence));
            } else if (modules instanceof PersistenceUnit) {
                final PersistenceUnit unit = (PersistenceUnit) modules;
                appModule.addPersistenceModule(new PersistenceModule(appModule, "", new org.apache.openejb.jee.jpa.unit.Persistence(unit)));
            } else if (modules instanceof Beans) {
                final Beans beans = (Beans) modules;
                final EjbModule ejbModule = new EjbModule(new EjbJar());
                ejbModule.setBeans(beans);
                appModule.getEjbModules().add(ejbModule);
            }

            // Application is final in AppModule, which is fine, so we'll create a new one and move everything
            if (application != null) {
                final AppModule newModule = new AppModule(appModule.getClassLoader(), appModule.getModuleId(), application, false);
                newModule.getClientModules().addAll(appModule.getClientModules());
                newModule.addPersistenceModules(appModule.getPersistenceModules());
                newModule.getEjbModules().addAll(appModule.getEjbModules());
                newModule.getConnectorModules().addAll(appModule.getConnectorModules());
                appModule = newModule;
            }

            return appModule;
        }

        // TODO, report some information

        private EJBException specifiedModulesNotFound() {
            return new NoSuchModuleException("some modules not matched on classpath");
        }

        private InvalidModulesPropertyException invalidModulesValue(final Object value) {
            final String[] spec = {"java.lang.String", "java.lang.String[]", "java.io.File", "java.io.File[]"};
//            TODO
//            String[] vendor = {"java.lang.Class","java.lang.Class[]", "java.net.URL", "java.io.URL[]"};
            final String type = value == null ? null : value.getClass().getName();
            return new InvalidModulesPropertyException(String.format("Invalid '%s' value '%s'. Valid values are: %s", EJBContainer.MODULES, type, Join.join(", ", spec)));
        }

        private static boolean isOtherProvider(final Map<?, ?> properties) {
            final Object provider = properties.get(EJBContainer.PROVIDER);
            return provider != null && !provider.equals(OpenEjbContainer.class) &&
                    !provider.equals(OpenEjbContainer.class.getName())
                    && !"openejb".equals(provider);
        }

        private boolean match(final String s, final File file) {
            final String s2 = file.getName();
            final String s3 = file.getAbsolutePath();
            final boolean matches;
            if (file.isDirectory()) {
                matches = s2.equals(s) || s2.equals(s + ".jar") || s3.equals(s);
            } else {
                matches = s2.equals(s + ".jar");
            }
            // TODO if (!matches) { /* look for ejb-jar.xml with matching module name */ }
            return matches;
        }
    }

    private class GlobalContext extends ContextFlyweight {

        private final Context globalJndiContext;

        public GlobalContext(final Context globalJndiContext) {

            this.globalJndiContext = globalJndiContext;
        }

        @Override
        protected Context getContext() throws NamingException {
            return globalJndiContext;
        }

        @Override
        protected Name getName(Name name) throws NamingException {
            String first = name.get(0);
            if (!first.startsWith("java:")) {
                throw new NameNotFoundException("Name must be in java: namespace");
            }
            first = first.substring("java:".length());
            name = name.getSuffix(1);
            return name.add(0, first);
        }

        @Override
        protected String getName(final String name) throws NamingException {
            if ("inject".equals(name)) {
                return name;
            }

            if (!name.startsWith("java:")) {
                throw new NameNotFoundException("Name must be in java: namespace");
            }

            return name.substring("java:".length());
        }

        @Override
        public void bind(final Name name, final Object obj) throws NamingException {
            if (name.size() == 1 && "inject".equals(name.get(0))) {
                Injector.inject(obj);
            } else {
                super.bind(name, obj);
            }
        }

        @Override
        public void bind(final String name, final Object obj) throws NamingException {
            if (name != null && "inject".equals(name)) {
                Injector.inject(obj);
            } else {
                super.bind(name, obj);
            }
        }

        @Override
        public void unbind(final Name name) throws NamingException {
            if (!(name.size() == 1 && "inject".equals(name.get(0)))) {
                super.unbind(name);
            }
        }

        @Override
        public void unbind(final String name) throws NamingException {
            if (!(name != null && "inject".equals(name))) {
                super.unbind(name);
            }
        }
    }

    public static class AssembleApplicationException extends InitializationException {
        public AssembleApplicationException(final Exception cause) {
            super(cause);
        }
    }

    public static class ConfigureApplicationException extends InitializationException {
        public ConfigureApplicationException(final Exception cause) {
            super(cause);
        }
    }

    public static class InitializationException extends EJBException {
        public InitializationException(final String s) {
            super(s);
        }

        public InitializationException(final Exception cause) {
            super(cause);
        }
    }

    public static class InvalidApplicationException extends InitializationException {
        public InvalidApplicationException(final Exception cause) {
            super(cause);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class InvalidModulesPropertyException extends InitializationException {
        public InvalidModulesPropertyException(final String s) {
            super(s);
        }
    }

    public static class NoModulesFoundException extends InitializationException {
        public NoModulesFoundException(final String s) {
            super(s);
        }
    }

    public static class NoSuchModuleException extends InitializationException {
        public NoSuchModuleException(final String s) {
            super(s);
        }
    }

}