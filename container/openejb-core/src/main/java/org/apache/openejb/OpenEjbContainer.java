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

import javax.validation.ValidationException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.cdi.OWBInjector;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ConnectorModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Exceptions;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OptionsLog;
import org.apache.openejb.util.ServiceManagerProxy;
import org.apache.openejb.util.StringUtilities;
import org.apache.xbean.naming.context.ContextFlyweight;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjbContainer extends EJBContainer {

    public static final String OPENEJB_EMBEDDED_REMOTABLE = "openejb.embedded.remotable";
    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, OpenEjbContainer.class);

    private static OpenEjbContainer instance;

    private final AppContext appContext;

    private ServiceManagerProxy serviceManager;
    private Options options;
    private OpenEjbContainer.GlobalContext globalJndiContext;

    private OpenEjbContainer(Map<?, ?> map, AppContext appContext) {
        this.appContext = appContext;
        this.globalJndiContext = new GlobalContext(appContext.getGlobalJndiContext());

        final Properties properties = new Properties();
        properties.putAll(map);

        options = new Options(properties);

        startNetworkServices();
    }

    @Override
    public void close() {
        if (serviceManager != null) {
            serviceManager.stop();
        }
        try {
            globalJndiContext.close();
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
        OpenEJB.destroy();
        instance = null;
    }

    @Override
    public Context getContext() {
        return globalJndiContext;
    }

    public <T> T inject(T object) {

        assert object != null;

        final Class<?> clazz = object.getClass();

        final BeanContext context = resolve(clazz);

        if (context == null) throw new NoInjectionMetaDataException(clazz.getName());

        final InjectionProcessor processor = new InjectionProcessor(object, context.getInjections(), context.getJndiContext());

        try {
            OWBInjector beanInjector = new OWBInjector(appContext.getWebBeansContext());
            beanInjector.inject(object);
        } catch (Throwable t) {
            // TODO handle this differently
            // this is temporary till the injector can be rewritten
        }

        try {
            return (T) processor.createInstance();
        } catch (OpenEJBException e) {
            throw new InjectionException(clazz.getName(), e);
        }
    }

    private <T> BeanContext resolve(Class<?> clazz) {

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        while (clazz != null && clazz != Object.class) {

            {
                final BeanContext context = containerSystem.getBeanContext(clazz.getName());

                if (context != null) return context;
            }

            for (BeanContext context : containerSystem.deployments()) {

                if (clazz == context.getBeanClass()) return context;

            }

            clazz = clazz.getSuperclass();
        }

        return null;
    }


    private void startNetworkServices() {
        if (!options.get(OPENEJB_EMBEDDED_REMOTABLE, false)) {
            return;
        }

        try {
            serviceManager = new ServiceManagerProxy();
            serviceManager.start();
        } catch (ServiceManagerProxy.AlreadyStartedException e) {
            logger.debug("Network services already started.  Ignoring option " + OPENEJB_EMBEDDED_REMOTABLE);
        }
    }


    public static class NoInjectionMetaDataException extends IllegalStateException {
        public NoInjectionMetaDataException(String s) {
            super(s);
        }
    }

    public static class InjectionException extends IllegalStateException {
        public InjectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class Provider implements EJBContainerProvider {

        @Override
        public EJBContainer createEJBContainer(Map<?, ?> map) {
            if (isOtherProvider(map)) return null;

            if (instance != null || OpenEJB.isInitialized()) {
                logger.info("EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization");
                return instance;
            }


            try {
                Properties properties = new Properties();
                properties.putAll(map);

                SystemInstance.reset();
                SystemInstance.init(properties);
                SystemInstance.get().setProperty("openejb.embedded", "true");
                SystemInstance.get().setProperty(EJBContainer.class.getName(), "true");

                OptionsLog.install();

                OpenEJB.init(properties);


                final ConfigurationFactory configurationFactory = new ConfigurationFactory();


                final AppModule appModule = load(map, configurationFactory);

                final Set<String> callers = NewLoaderLogic.callers();
                final EjbJar ejbJar = new EjbJar();
                final OpenejbJar openejbJar = new OpenejbJar();

                for (String caller : callers) {

                    if (!isValid(caller)) continue;
                    
                    final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(caller, caller));

                    // set it to bean so it can get UserTransaction injection
                    bean.setTransactionType(TransactionType.BEAN);

                    final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);

                    // important in case any other deploment id formats are specified
                    ejbDeployment.setDeploymentId(caller);
                }

                appModule.getEjbModules().add(new EjbModule(ejbJar, openejbJar));


                final AppInfo appInfo;
                try {

                    appInfo = configurationFactory.configureApplication(appModule);

                } catch (ValidationFailedException e) {

                    logger.warning("configureApplication.loadFailed", appModule.getModuleId(), e.getMessage()); // DO not include the stacktrace in the message

                    throw new InvalidApplicationException(e);

                } catch (OpenEJBException e) {
                    // DO NOT REMOVE THE EXCEPTION FROM THIS LOG MESSAGE
                    // removing this message causes NO messages to be printed when embedded
                    logger.warning("configureApplication.loadFailed", e, appModule.getModuleId(), e.getMessage());

                    throw new ConfigureApplicationException(e);
                }

                final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);

                final AppContext appContext;

                try {
                    appContext = assembler.createApplication(appInfo, appModule.getClassLoader());
                } catch (ValidationException ve) {
                    throw ve;
                } catch (Exception e) {
                    throw new AssembleApplicationException(e);
                }


                return instance = new OpenEjbContainer(map, appContext);

            } catch (OpenEJBException e) {

                throw new EJBException(e);

            } catch (MalformedURLException e) {

                throw new EJBException(e);

            } catch (ValidationException ve) {
                throw ve;
            } catch (Exception e) {

                if (e instanceof EJBException) {

                    throw (EJBException) e;
                }

                throw new InitializationException(e);
            } finally {
                if (instance == null && OpenEJB.isInitialized()) {
                    try {
                        OpenEJB.destroy();
                    } catch (Exception e) {
                    }
                }
            }
        }

        private boolean isValid(String caller) {
            try {
                final ClassLoader loader = Thread.currentThread().getContextClassLoader();

                final Class<?> clazz = loader.loadClass(caller);

                if (clazz.isEnum()) return false;
                if (clazz.isInterface()) return false;
                if (Modifier.isAbstract(clazz.getModifiers())) return false;

                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        private AppModule load(Map<?, ?> map, ConfigurationFactory configurationFactory) throws MalformedURLException, OpenEJBException {
            final List<File> moduleLocations;

            String appId = (String) map.get(EJBContainer.APP_NAME);


            final Object modules = map.get(EJBContainer.MODULES);

            // will be updated if needed

            ClassLoader classLoader = getClass().getClassLoader();

            if (modules instanceof String) {

                moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);
                for (Iterator<File> i = moduleLocations.iterator(); i.hasNext();) {
                    File file = i.next();
                    if (!match((String) modules, file)) {
                        i.remove();
                    }
                }

            } else if (modules instanceof String[]) {

                // TODO Optimize this so we look specifically for modules by name
                moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);

                int matched = 0;

                for (Iterator<File> i = moduleLocations.iterator(); i.hasNext();) {
                    File file = i.next();
                    boolean remove = true;
                    for (String s : (String[]) modules) {
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

                URL url = ((File) modules).toURI().toURL();
                classLoader = new URLClassLoader(new URL[]{url}, classLoader);
                moduleLocations = Collections.singletonList((File) modules);

            } else if (modules instanceof File[]) {

                File[] files = (File[]) modules;
                URL[] urls = new URL[files.length];
                for (int i = 0; i < urls.length; i++) {
                    urls[i] = files[i].toURI().toURL();
                }
                classLoader = new URLClassLoader(urls, classLoader);
                moduleLocations = Arrays.asList((File[]) modules);

            } else if (modules == null) {

                moduleLocations = configurationFactory.getModulesFromClassPath(null, classLoader);

            } else {

                AppModule appModule = load(map);

                if (appModule != null) return appModule;

                throw invalidModulesValue(modules);

            }


            if (moduleLocations.isEmpty()) {
            	throw Exceptions.newNoModulesFoundException();

            }

            // TODO With this load method we can finally do some checking on module ids to really implement EJBContainer.MODULES String/String[]
            return configurationFactory.loadApplication(classLoader, appId, moduleLocations);
        }

        private AppModule load(Map<?, ?> map) {

            String appId = (String) map.get(EJBContainer.APP_NAME);


            final Object modules = map.get(EJBContainer.MODULES);

            map.size();
            AppModule m;

            {
                final Object obj = modules;
                Application application = null;
                AppModule appModule = new AppModule(this.getClass().getClassLoader(), appId);

                {
                    if (obj instanceof EjbJar) {

                        final EjbJar ejbJar = (EjbJar) obj;
                        appModule.getEjbModules().add(new EjbModule(ejbJar));

                    } else if (obj instanceof EnterpriseBean) {

                        final EnterpriseBean bean = (EnterpriseBean) obj;
                        final EjbJar ejbJar = new EjbJar();
                        ejbJar.addEnterpriseBean(bean);
                        appModule.getEjbModules().add(new EjbModule(ejbJar));

                    } else if (obj instanceof Application) {

                        application = (Application) obj;

                    } else if (obj instanceof Connector) {

                        final Connector connector = (Connector) obj;
                        appModule.getConnectorModules().add(new ConnectorModule(connector));

                    } else if (obj instanceof Persistence) {

                        final Persistence persistence = (Persistence) obj;
                        appModule.getPersistenceModules().add(new PersistenceModule("", persistence));

                    } else if (obj instanceof PersistenceUnit) {

                        final PersistenceUnit unit = (PersistenceUnit) obj;
                        appModule.getPersistenceModules().add(new PersistenceModule("", new Persistence(unit)));

                    } else if (obj instanceof Beans) {

                        final Beans beans = (Beans) obj;
                        final EjbModule ejbModule = new EjbModule(new EjbJar());
                        ejbModule.setBeans(beans);
                        appModule.getEjbModules().add(ejbModule);
                    }
                }

                // Application is final in AppModule, which is fine, so we'll create a new one and move everything
                if (application != null) {
                    final AppModule newModule = new AppModule(appModule.getClassLoader(), appModule.getModuleId(), application, false);
                    newModule.getClientModules().addAll(appModule.getClientModules());
                    newModule.getPersistenceModules().addAll(appModule.getPersistenceModules());
                    newModule.getEjbModules().addAll(appModule.getEjbModules());
                    newModule.getConnectorModules().addAll(appModule.getConnectorModules());
                    appModule = newModule;
                }

                m = appModule;
            }
            return m;
        }

        // TODO, report some information

        private EJBException specifiedModulesNotFound() {
            return new NoSuchModuleException("some modules not matched on classpath");
        }

        private InvalidModulesPropertyException invalidModulesValue(Object value) {
            String[] spec = {"java.lang.String", "java.lang.String[]", "java.io.File", "java.io.File[]"};
//            TODO
//            String[] vendor = {"java.lang.Class","java.lang.Class[]", "java.net.URL", "java.io.URL[]"};
            String type = (value == null) ? null : value.getClass().getName();
            return new InvalidModulesPropertyException(String.format("Invalid '%s' value '%s'. Valid values are: %s", EJBContainer.MODULES, type, Join.join(", ", spec)));
        }

        private static boolean isOtherProvider(Map<?, ?> properties) {
            Object provider = properties.get(EJBContainer.PROVIDER);

            if (provider != null && !provider.equals(OpenEjbContainer.class) && !provider.equals(OpenEjbContainer.class.getName())) {

                return true;

            }
            return false;
        }

        private boolean match(String s, File file) {
            String s2 = file.getName();
            boolean matches;
            if (file.isDirectory()) {
                matches = s2.equals(s) || s2.equals(s + ".jar");
            } else {
                matches = s2.equals(s + ".jar");
            }
            if (!matches) {
                //TODO look for ejb-jar.xml with matching module name
            }
            return matches;
        }
    }

    private class GlobalContext extends ContextFlyweight {
        private final Context globalJndiContext;

        public GlobalContext(Context globalJndiContext) {
            this.globalJndiContext = globalJndiContext;
        }

        @Override
        protected Context getContext() throws NamingException {
            return globalJndiContext;
        }

        @Override
        protected Name getName(Name name) throws NamingException {
            String first = name.get(0);
            if (!first.startsWith("java:")) throw new NameNotFoundException("Name must be in java: namespace");
            first = first.substring("java:".length());
            name = name.getSuffix(1);
            return name.add(0, first);
        }

        @Override
        protected String getName(String name) throws NamingException {
            if ("inject".equals(name)) return name;
            if (!name.startsWith("java:")) throw new NameNotFoundException("Name must be in java: namespace");
            return name.substring("java:".length());
        }

        @Override
        public void bind(Name name, Object obj) throws NamingException {
            if (name.size() == 1 && "inject".equals(name.get(0))) inject(obj);
            else super.bind(name, obj);
        }

        @Override
        public void bind(String name, Object obj) throws NamingException {
            if (name != null && "inject".equals(name)) inject(obj);
            else super.bind(name, obj);
        }
    }


    public static class InitializationException extends EJBException {
        public InitializationException(String s) {
            super(s);
        }

        public InitializationException(Exception cause) {
            super(cause);
        }
    }

    public static class InvalidModulesPropertyException extends InitializationException {
        public InvalidModulesPropertyException(String s) {
            super(s);
        }
    }

    public static class NoSuchModuleException extends InitializationException {
        public NoSuchModuleException(String s) {
            super(s);
        }
    }

    public static class NoModulesFoundException extends InitializationException {
        public NoModulesFoundException(String s) {
            super(s);
        }
    }

    public static class ConfigureApplicationException extends InitializationException {
        public ConfigureApplicationException(Exception cause) {
            super(cause);
        }
    }

    public static class AssembleApplicationException extends InitializationException {
        public AssembleApplicationException(Exception cause) {
            super(cause);
        }
    }

    public static class InvalidApplicationException extends InitializationException {
        public InvalidApplicationException(Exception cause) {
            super(cause);
        }
    }
}
