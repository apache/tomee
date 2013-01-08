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
package org.apache.openejb.testing;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.cdi.ScopeHelper;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ConnectorModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.NamedModule;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.ServiceManagerProxy;
import org.apache.openejb.util.URLs;
import org.apache.openejb.web.LightweightWebAppBuilder;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.web.lifecycle.test.MockHttpSession;
import org.apache.webbeans.web.lifecycle.test.MockServletContext;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;

import javax.naming.Context;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import static org.apache.openejb.config.DeploymentFilterable.DEPLOYMENTS_CLASSPATH_PROPERTY;

public final class ApplicationComposers {
    public static final String OPENEJB_APPLICATION_COMPOSER_CONTEXT = "openejb.application.composer.context";
    private static final Class[] MODULE_TYPES = { IAnnotationFinder.class, ClassesArchive.class,
            AppModule.class, WebModule.class, EjbModule.class,
            Application.class,
            WebApp.class, EjbJar.class, EnterpriseBean.class,
            Persistence.class, PersistenceUnit.class,
            Connector.class, Beans.class,
            Class[].class
    };

    static {
        ApplicationComposers.linkageErrorProtection();
    }

    private final ClassFinder testClassFinder;
    private final Class<?> testClass;
    private ServiceManagerProxy serviceManager = null;

    // invocation context
    private AppInfo appInfo = null;
    private Assembler assembler = null;
    private AppContext appContext = null;
    private ThreadContext previous = null;
    private MockHttpSession session = null;
    private MockServletContext servletContext = null;

    public ApplicationComposers(final Class<?> klass) {
        testClass = klass;
        testClassFinder = new ClassFinder(klass);
        validate();
    }

    private void validate() {
        List<Throwable> errors = new ArrayList<Throwable>();

        final List<Method> configs = new ArrayList<Method>();
        configs.addAll(testClassFinder.findAnnotatedMethods(Configuration.class));
        configs.addAll(testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.Configuration.class));
        if (configs.size() > 1) {
            final String gripe = "Test class should have no more than one @Configuration method";
            errors.add(new Exception(gripe));
        }

        if (testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.MockInjector.class).size()
                + testClassFinder.findAnnotatedMethods(MockInjector.class).size() > 1) {
            errors.add(new Exception("Test class should have no more than one @MockInjector method"));
        }

        for (Method method : configs) {
            final Class<?> type = method.getReturnType();
            if (!Properties.class.isAssignableFrom(type)) {
                final String gripe = "@Configuration method must return " + Properties.class.getName();
                errors.add(new Exception(gripe));
            }
        }

        final List<Method> components = new ArrayList<Method>();
        components.addAll(testClassFinder.findAnnotatedMethods(Component.class));
        components.addAll(testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.Component.class));
        for (Method method : components) {
            if (method.getParameterTypes().length > 0) {
                errors.add(new Exception("@Component methods shouldn't take any parameters"));
            }
        }

        final List<Method> descriptors = new ArrayList<Method>();
        descriptors.addAll(testClassFinder.findAnnotatedMethods(Descriptors.class));
        descriptors.addAll(testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.Descriptors.class));
        for (Method method : descriptors) {
            final Class<?> returnType = method.getReturnType();
            if (!returnType.equals(WebModule.class) && !returnType.equals(EjbModule.class)
                    && !returnType.equals(WebApp.class) && !returnType.equals(EjbJar.class)
                    && !returnType.equals(AppModule.class)) {
                errors.add(new Exception("@Descriptors can't be used on " + returnType.getName()));
            }
        }

        final List<Method> classes = new ArrayList<Method>();
        classes.addAll(testClassFinder.findAnnotatedMethods(Classes.class));
        classes.addAll(testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.Classes.class));
        for (Method method : classes) {
            final Class<?> returnType = method.getReturnType();
            if (!returnType.equals(WebModule.class) && !returnType.equals(EjbModule.class)
                    && !returnType.equals(WebApp.class) && !returnType.equals(EjbJar.class)) {
                errors.add(new Exception("@Classes can't be used on a method returning " + returnType));
            }
        }

        int appModules = 0;
        int modules = 0;

        final List<Method> moduleMethods = new ArrayList<Method>();
        moduleMethods.addAll(testClassFinder.findAnnotatedMethods(Module.class));
        moduleMethods.addAll(testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.Module.class));
        for (Method method : moduleMethods) {

            modules++;

            final Class<?> type = method.getReturnType();

            if (Application.class.isAssignableFrom(type)) {

                appModules++;

            } else if (!isValidModuleType(type, MODULE_TYPES)) {
                final String gripe = "@Module method must return " + Join.join(" or ", MODULE_TYPES).replaceAll("(class|interface) ", "");
                errors.add(new Exception(gripe));
            }
        }

        if (appModules > 1) {
            final String gripe = "Test class should have no more than one @Module method that returns " + Application.class.getName();
            errors.add(new Exception(gripe));
        }

        if (modules < 1) {
            final String gripe = "Test class should have at least one @Module method";
            errors.add(new Exception(gripe));
        }

        if (!errors.isEmpty()) {
            throw new OpenEJBRuntimeException(errors.toString());
        }
    }

    private boolean isValidModuleType(Class<?> type, Class<?>[] moduleTypes) {
        for (Class<?> moduleType : moduleTypes) {
            if (moduleType.isAssignableFrom(type)) return true;
        }
        return false;
    }

    public void before(final Object testInstance) throws Exception {
        final ClassLoader loader = testClass.getClassLoader();
        AppModule appModule = new AppModule(loader, testClass.getSimpleName());

        // Add the test case as an @ManagedBean
        {
            final EjbJar ejbJar = new EjbJar();
            final OpenejbJar openejbJar = new OpenejbJar();
            final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(testClass.getSimpleName(), testClass.getName(), true));
            bean.setTransactionType(TransactionType.BEAN);
            final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);
            ejbDeployment.setDeploymentId(testClass.getName());

            appModule.getEjbModules().add(new EjbModule(ejbJar, openejbJar));
        }

        // For the moment we just take the first @Configuration method
        // maybe later we can add something fancy to allow multiple configurations using a qualifier
        // as a sort of altDD/altConfig concept.  Say for example the altDD prefix might be "foo",
        // we can then imagine something like this:
        // @Foo @Configuration public Properties alternateConfig(){...}
        // @Foo @Module  public Properties alternateModule(){...}
        // anyway, one thing at a time ....

        final Properties configuration = new Properties();
        configuration.put(DEPLOYMENTS_CLASSPATH_PROPERTY, "false");

        EnableServices annotation = testClass.getAnnotation(EnableServices.class);
        if (annotation != null && annotation.httpDebug()) {
            configuration.setProperty("httpejbd.print", "true");
            configuration.setProperty("httpejbd.indent.xml", "true");
            configuration.setProperty("logging.level.OpenEJB.server.http", "FINE");
        }
        org.apache.openejb.junit.EnableServices annotationOld = testClass.getAnnotation(org.apache.openejb.junit.EnableServices.class);
        if (annotationOld != null && annotationOld.httpDebug()) {
            configuration.setProperty("httpejbd.print", "true");
            configuration.setProperty("httpejbd.indent.xml", "true");
            configuration.setProperty("logging.level.OpenEJB.server.http", "FINE");
        }

        final List<Method> configs = new ArrayList<Method>();
        configs.addAll(testClassFinder.findAnnotatedMethods(Configuration.class));
        configs.addAll(testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.Configuration.class));
        for (Method method : configs) {
            final Object o = method.invoke(testInstance);
            if (o instanceof Properties) {
                Properties properties = (Properties) o;
                configuration.putAll(properties);
            }
        }

        if (SystemInstance.isInitialized()) SystemInstance.reset();

        SystemInstance.init(configuration);

        // save the test under test to be able to retrieve it from extensions
        // /!\ has to be done before all other init
        SystemInstance.get().setComponent(TestInstance.class, new TestInstance(testClass, testInstance));

        // call the mock injector before module method to be able to use mocked classes
        // it will often use the TestInstance so
        final List<Method> mockInjectors = new ArrayList<Method>(testClassFinder.findAnnotatedMethods(MockInjector.class));
        mockInjectors.addAll(testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.MockInjector.class));
        if (!mockInjectors.isEmpty()) {
            Object o = mockInjectors.iterator().next().invoke(testInstance);
            if (o instanceof Class<?>) {
                o = ((Class<?>) o).newInstance();
            }
            if (o instanceof FallbackPropertyInjector) {
                SystemInstance.get().setComponent(FallbackPropertyInjector.class, (FallbackPropertyInjector) o);
            }
        }

        for (Method method : testClassFinder.findAnnotatedMethods(Component.class)) {
            setComponent(testInstance, method);
        }
        for (Method method : testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.Component.class)) {
            setComponent(testInstance, method);
        }

        final Map<String, URL> additionalDescriptors = descriptorsToMap(testClass.getAnnotation(org.apache.openejb.junit.Descriptors.class));
        final Map<String, URL> additionalDescriptorsNew = descriptorsToMap(testClass.getAnnotation(Descriptors.class));
        additionalDescriptors.putAll(additionalDescriptorsNew);

        Application application = null;

        int webModulesNb = 0;

        // Invoke the @Module producer methods to build out the AppModule
        final List<Method> moduleMethods = new ArrayList<Method>();
        moduleMethods.addAll(testClassFinder.findAnnotatedMethods(Module.class));
        moduleMethods.addAll(testClassFinder.findAnnotatedMethods(org.apache.openejb.junit.Module.class));
        for (Method method : moduleMethods) {

            final Object obj = method.invoke(testInstance);
            final Classes classesAnnotation = method.getAnnotation(Classes.class);
            final org.apache.openejb.junit.Classes classesAnnotationOld = method.getAnnotation(org.apache.openejb.junit.Classes.class);

            Class<?>[] classes = null;
            boolean cdi = false;
            if (classesAnnotation != null) {
                classes = classesAnnotation.value();
                cdi = classesAnnotation.cdi();
            } else if (classesAnnotationOld != null) {
                classes = classesAnnotationOld.value();
            }

            if (obj instanceof WebApp) { // will add the ejbmodule too
                webModulesNb++;

                final WebApp webapp = (WebApp) obj;
                String root = webapp.getContextRoot();
                if (root == null) {
                    root = "/openejb";
                }

                final WebModule webModule = new WebModule(webapp, root, Thread.currentThread().getContextClassLoader(), "", root);

                webModule.getAltDDs().putAll(additionalDescriptors);
                webModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                if (classes != null) {
                    webModule.setFinder(finderFromClasses(classes));
                }
                final EjbModule ejbModule = DeploymentLoader.addWebModule(webModule, appModule);
                if (cdi) {
                    ejbModule.setBeans(new Beans());
                }
            } else if (obj instanceof WebModule) { // will add the ejbmodule too
                webModulesNb++;

                final WebModule webModule = (WebModule) obj;

                webModule.getAltDDs().putAll(additionalDescriptors);
                webModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                if (classes != null) {
                    webModule.setFinder(finderFromClasses(classes));
                }
                DeploymentLoader.addWebModule(webModule, appModule);
            } else if (obj instanceof EjbModule) {
                final EjbModule ejbModule = (EjbModule) obj;

                ejbModule.getAltDDs().putAll(additionalDescriptors);
                ejbModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                if (classes != null) {
                    ejbModule.setFinder(finderFromClasses(classes));
                }
                ejbModule.initAppModule(appModule);
                appModule.getEjbModules().add(ejbModule);
            } else if (obj instanceof EjbJar) {

                final EjbJar ejbJar = (EjbJar) obj;
                setId(ejbJar, method);

                final EjbModule ejbModule = new EjbModule(ejbJar);

                ejbModule.getAltDDs().putAll(additionalDescriptors);
                ejbModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                appModule.getEjbModules().add(ejbModule);
                if (classes != null) {
                    ejbModule.setFinder(finderFromClasses(classes));
                }
            } else if (obj instanceof EnterpriseBean) {

                final EnterpriseBean bean = (EnterpriseBean) obj;
                final EjbJar ejbJar = new EjbJar(method.getName());
                ejbJar.addEnterpriseBean(bean);
                EjbModule ejbModule = new EjbModule(ejbJar);
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(bean.getEjbClass());
                ejbModule.setFinder(new AnnotationFinder(new ClassesArchive(clazz)).link());
                appModule.getEjbModules().add(ejbModule);

            } else if (obj instanceof Application) {

                application = (Application) obj;
                setId(application, method);

            } else if (obj instanceof Connector) {

                final Connector connector = (Connector) obj;
                setId(connector, method);
                appModule.getConnectorModules().add(new ConnectorModule(connector));

            } else if (obj instanceof Persistence) {

                final Persistence persistence = (Persistence) obj;
                appModule.addPersistenceModule(new PersistenceModule(appModule, implicitRootUrl(), persistence));

            } else if (obj instanceof PersistenceUnit) {

                final PersistenceUnit unit = (PersistenceUnit) obj;
                appModule.addPersistenceModule(new PersistenceModule(appModule, implicitRootUrl(), new Persistence(unit)));

            } else if (obj instanceof Beans) {

                final Beans beans = (Beans) obj;
                final EjbModule ejbModule = new EjbModule(new EjbJar(method.getName()));
                ejbModule.setBeans(beans);
                if (classes != null) {
                    ejbModule.setFinder(finderFromClasses(classes));
                }
                appModule.getEjbModules().add(ejbModule);

            } else if (obj instanceof Class[]) {

                final Class[] beans = (Class[]) obj;
                final EjbModule ejbModule = new EjbModule(new EjbJar(method.getName()));
                ejbModule.setFinder(new AnnotationFinder(new ClassesArchive(beans)).link());
                ejbModule.setBeans(new Beans());
                appModule.getEjbModules().add(ejbModule);
            } else if (obj instanceof Class) {

                final Class bean = (Class) obj;
                final EjbModule ejbModule = new EjbModule(new EjbJar(method.getName()));
                ejbModule.setFinder(new AnnotationFinder(new ClassesArchive(bean)).link());
                ejbModule.setBeans(new Beans());
                appModule.getEjbModules().add(ejbModule);
            } else if (obj instanceof IAnnotationFinder) {

                final EjbModule ejbModule = new EjbModule(new EjbJar(method.getName()));
                ejbModule.setFinder((IAnnotationFinder) obj);
                ejbModule.setBeans(new Beans());
                appModule.getEjbModules().add(ejbModule);
            } else if (obj instanceof ClassesArchive) {

                final EjbModule ejbModule = new EjbModule(new EjbJar(method.getName()));
                ejbModule.setFinder(new AnnotationFinder((Archive) obj).link());
                ejbModule.setBeans(new Beans());
                appModule.getEjbModules().add(ejbModule);
            } else if (obj instanceof AppModule) {
                // we can probably go further here
                final AppModule module = (AppModule) obj;

                module.getAltDDs().putAll(additionalDescriptors);
                module.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                if (module.getWebModules().size() > 0) {
                    webModulesNb++;
                }

                appModule.getEjbModules().addAll(module.getEjbModules());
                appModule.getPersistenceModules().addAll(module.getPersistenceModules());
                appModule.getAdditionalLibMbeans().addAll(module.getAdditionalLibMbeans());
                appModule.getWebModules().addAll(module.getWebModules());
                appModule.getConnectorModules().addAll(module.getConnectorModules());
                appModule.getResources().addAll(module.getResources());
                appModule.getServices().addAll(module.getServices());
                appModule.getPojoConfigurations().putAll(module.getPojoConfigurations());
                appModule.getAdditionalLibraries().addAll(module.getAdditionalLibraries());
                appModule.getAltDDs().putAll(module.getAltDDs());
                appModule.getProperties().putAll(module.getProperties());
            }
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

        if (webModulesNb > 0 && SystemInstance.get().getComponent(WebAppBuilder.class) == null) {
            SystemInstance.get().setComponent(WebAppBuilder.class, new LightweightWebAppBuilder());
        }

        if (moduleMethods.size() == 1 && webModulesNb == 1) {
            appModule.setStandloneWebModule();
        }

        ConfigurationFactory config = new ConfigurationFactory();
        config.init(SystemInstance.get().getProperties());

        assembler = new Assembler();
        SystemInstance.get().setComponent(Assembler.class, assembler);

        assembler.buildContainerSystem(config.getOpenEjbConfiguration());

        if ("true".equals(configuration.getProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "false"))
                || (annotation != null || annotationOld != null)) {
            try {
                if (annotation != null) {
                    initFilteredServiceManager(annotation.value());
                }
                if (annotationOld != null) {
                    initFilteredServiceManager(annotationOld.value());
                }
                serviceManager = new ServiceManagerProxy(false);
                serviceManager.start();
            } catch (ServiceManagerProxy.AlreadyStartedException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }

        servletContext = new MockServletContext();
        session = new MockHttpSession();

        appInfo = config.configureApplication(appModule);

        appContext = assembler.createApplication(appInfo);

        ScopeHelper.startContexts(appContext.getWebBeansContext().getContextsService(), servletContext, session);

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext context = containerSystem.getBeanContext(testClass.getName());

        ThreadContext callContext = new ThreadContext(context, null, Operation.INJECTION);
        ThreadContext oldContext = ThreadContext.enter(callContext);
        try {
            final InjectionProcessor processor = new InjectionProcessor(testInstance, context.getInjections(), context.getJndiContext());

            processor.createInstance();
            AbstractInjectable.instanceUnderInjection.set(testInstance);
            try {
                OWBInjector.inject(appContext.getBeanManager(), testInstance, null);
            } catch (Throwable t) {
                // TODO handle this differently
                // this is temporary till the injector can be rewritten
                t.printStackTrace();
            } finally {
                AbstractInjectable.instanceUnderInjection.remove();
            }
        } finally {
            ThreadContext.exit(oldContext);
        }

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        System.getProperties().put(OPENEJB_APPLICATION_COMPOSER_CONTEXT, appContext.getGlobalJndiContext());

        // test injections
        final List<Field> fields = new ArrayList<Field>(testClassFinder.findAnnotatedFields(AppResource.class));
        fields.addAll(testClassFinder.findAnnotatedFields(org.apache.openejb.junit.AppResource.class));
        for (Field field : fields) {
            final Class<?> type = field.getType();
            if (AppModule.class.isAssignableFrom(type)) {
                field.setAccessible(true);
                field.set(testInstance, appModule);
            } else if (Context.class.isAssignableFrom(type)) {
                field.setAccessible(true);
                field.set(testInstance, appContext.getGlobalJndiContext());
            } else {
                throw new IllegalArgumentException("can't find value for type " + type.getName());
            }
        }

        previous = ThreadContext.enter(new ThreadContext(context, null, Operation.BUSINESS));
    }

    private void setComponent(Object testInstance, Method method) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object value = method.invoke(testInstance);
        if (value instanceof Class<?>) {
            value = ((Class<?>) value).newInstance();
        }

        Class<?> key = method.getReturnType();

        if (!key.isInstance(value)) { // we can't do it in validate to avoid to instantiate the value twice
            throw new OpenEJBRuntimeException(value + " is not an instance of " + key.getName());
        }

        SystemInstance.get().setComponent((Class<Object>) key, value);
    }

    public void evaluate(final Object testInstance, final Callable<Void> next) throws Exception {
        before(testInstance);
        try {
            next.call();
        } finally {
            if (previous != null) {
                ThreadContext.exit(previous);
            }
            after();
        }

    }

    public void after() throws Exception {
        if (servletContext != null || session != null) {
            try {
                ScopeHelper.stopContexts(appContext.getWebBeansContext().getContextsService(), servletContext, session);
            } catch (Exception e) {
                // no-op
            }
        }
        if (assembler != null) {
            try {
                assembler.destroyApplication(appInfo.path);
            } catch (Exception e) {
                // no-op
            }
        }
        if (serviceManager != null) {
            try {
                serviceManager.stop();
            } catch (RuntimeException ignored) {
                // no-op
            }
        }
        OpenEJB.destroy();
    }

    private <Module extends NamedModule> Module setId(Module module, Method method) {
        return setId(module, method.getName());
    }

    private <Module extends NamedModule> Module setId(Module module, String name) {
        if (module.getModuleName() != null) return module;
        if (module.getId() != null) return module;
        module.setId(name);
        return module;
    }

    private static String implicitRootUrl() {
        final ResourceFinder finder = new ResourceFinder("", Thread.currentThread().getContextClassLoader());
        try {
            final URL url = DeploymentLoader.altDDSources(DeploymentLoader.mapDescriptors(finder), false).get("persistence.xml");
            if (url == null) {
                return "";
            }

            final File file = URLs.toFile(url);
            if (file.getName().endsWith("persistence.xml")) {
                final String parent = file.getParentFile().getName();
                if (parent.equalsIgnoreCase("META-INF")) {
                    return file.getParentFile().getParentFile().getAbsolutePath();
                }
                return file.getParentFile().getAbsolutePath();
            }
            return url.toExternalForm();
        } catch (IOException e) {
            return "";
        }
    }

    private static Map<String, URL> descriptorsToMap(final Object descriptors) {
        if (descriptors != null) {
            final Map<String, URL> dds = new HashMap<String, URL>();
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (descriptors instanceof Descriptors) {
                for (Descriptor descriptor : ((Descriptors) descriptors).value()) {
                    dds.put(descriptor.name(), loader.getResource(descriptor.path()));
                }
            } else {
                if (descriptors instanceof org.apache.openejb.junit.Descriptors) {
                    for (org.apache.openejb.junit.Descriptor descriptor : ((org.apache.openejb.junit.Descriptors) descriptors).value()) {
                        dds.put(descriptor.name(), loader.getResource(descriptor.path()));
                    }
                }
            }
            return dds;
        }
        return Collections.emptyMap();
    }

    private static IAnnotationFinder finderFromClasses(final Class<?>[] value) {
        return new AnnotationFinder(new ClassesArchive(value)).link();
    }

    private void initFilteredServiceManager(String[] services) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class serviceManagerClass;
        try {
            serviceManagerClass = classLoader.loadClass("org.apache.openejb.server.FilteredServiceManager");
        } catch (ClassNotFoundException e) {
            String msg = "Services filtering requires class 'org.apache.openejb.server.FilteredServiceManager' to be available.  " +
                    "Make sure you have the openejb-server-*.jar in your classpath.";
            throw new IllegalStateException(msg, e);
        }

        Method initServiceManager = null;
        try {
            initServiceManager = serviceManagerClass.getMethod("initServiceManager", String[].class);
            initServiceManager.invoke(null, new Object[]{ services });
        } catch (Exception e) {
            throw new IllegalStateException("Failed initializing FilteredServiceManager with services " + services, e);
        }
    }

    private static void linkageErrorProtection() { // mainly for macos jre
        final ClassLoader loader = ApplicationComposers.class.getClassLoader();
        try {
            Class.forName("sun.security.pkcs11.SunPKCS11", true, loader);
            Class.forName("sun.security.pkcs11.SunPKCS11$Descriptor", true, loader);
            Class.forName("sun.security.pkcs11.wrapper.PKCS11Exception", true, loader);
        } catch (Throwable e) {
            // no-op: not an issue
        }
    }
}
