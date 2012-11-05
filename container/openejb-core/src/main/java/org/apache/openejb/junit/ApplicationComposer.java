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
package org.apache.openejb.junit;

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
import org.apache.openejb.web.LightweightWebAppBuilder;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.web.lifecycle.test.MockHttpSession;
import org.apache.webbeans.web.lifecycle.test.MockServletContext;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import javax.naming.Context;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.openejb.config.DeploymentsResolver.DEPLOYMENTS_CLASSPATH_PROPERTY;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationComposer extends BlockJUnit4ClassRunner {

    public static final String OPENEJB_APPLICATION_COMPOSER_CONTEXT = "openejb.application.composer.context";
    private static final Class[] MODULE_TYPES = { IAnnotationFinder.class, ClassesArchive.class,
            AppModule.class, WebModule.class, EjbModule.class,
            Application.class,
            WebApp.class, EjbJar.class, EnterpriseBean.class,
            Persistence.class, PersistenceUnit.class,
            Connector.class, Beans.class,
            Class[].class
    };

    private final TestClass testClass;
    private ServiceManagerProxy serviceManager = null;

    public ApplicationComposer(Class<?> klass) throws InitializationError {
        super(klass);
        testClass = new TestClass(klass);
        validate();
        linkageErrorProtection();
    }

    private void linkageErrorProtection() {
        final ClassLoader loader = getClass().getClassLoader();
        try {
            Class.forName("sun.security.pkcs11.SunPKCS11", true, loader);
            Class.forName("sun.security.pkcs11.SunPKCS11$Descriptor", true, loader);
            Class.forName("sun.security.pkcs11.wrapper.PKCS11Exception", true, loader);
        } catch (Throwable e) {
            // no-op: not an issue
        }
    }

    private void validate() throws InitializationError {
        List<Throwable> errors = new ArrayList<Throwable>();

        final List<FrameworkMethod> configs = testClass.getAnnotatedMethods(Configuration.class);
        if (configs.size() > 1) {
            final String gripe = "Test class should have no more than one @Configuration method";
            errors.add(new Exception(gripe));
        }

        final List<FrameworkMethod> mockInjector = testClass.getAnnotatedMethods(MockInjector.class);
        if (mockInjector.size() > 1) {
            errors.add(new Exception("Test class should have no more than one @MockInjector method"));
        }

        for (FrameworkMethod method : configs) {
            final Class<?> type = method.getMethod().getReturnType();
            if (!Properties.class.isAssignableFrom(type)) {
                final String gripe = "@Configuration method must return " + Properties.class.getName();
                errors.add(new Exception(gripe));
            }
        }

        for (FrameworkMethod method : testClass.getAnnotatedMethods(Component.class)) {
            if (method.getMethod().getParameterTypes().length > 0) {
                errors.add(new Exception("@Component methods shouldn't take any parameters"));
            }
        }

        for (FrameworkMethod method : testClass.getAnnotatedMethods(Descriptors.class)) {
            final Class<?> returnType = method.getMethod().getReturnType();
            if (!returnType.equals(WebModule.class) && !returnType.equals(EjbModule.class)
                    && !returnType.equals(WebApp.class) && !returnType.equals(EjbJar.class)
                    && !returnType.equals(AppModule.class)) {
                errors.add(new Exception("@Descriptors can't be used on " + returnType.getName()));
            }
        }

        for (FrameworkMethod method : testClass.getAnnotatedMethods(Classes.class)) {
            final Class<?> returnType = method.getMethod().getReturnType();
            if (!returnType.equals(WebModule.class) && !returnType.equals(EjbModule.class)
                    && !returnType.equals(WebApp.class) && !returnType.equals(EjbJar.class)) {
                errors.add(new Exception("@Classes can't be used on a method returning " + returnType));
            }
        }

        int appModules = 0;
        int modules = 0;

        for (FrameworkMethod method : testClass.getAnnotatedMethods(Module.class)) {

            modules++;

            final Class<?> type = method.getMethod().getReturnType();

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
            throw new InitializationError(errors);
        }
    }

    private boolean isValidModuleType(Class<?> type, Class[] moduleTypes) {
        for (Class moduleType : moduleTypes) {
            if (moduleType.isAssignableFrom(type)) return true;
        }
        return false;
    }

    @Override
    protected List<MethodRule> rules(Object test) {
        final List<MethodRule> rules = super.rules(test);
        rules.add(new MethodRule(){
            public Statement apply(Statement base, FrameworkMethod method, Object target) {
                return new DeployApplication(target, base);
            }
        });
        return rules;
    }

    public class DeployApplication extends Statement {

        // The TestCase instance
        private final Object testInstance;

        private final Statement next;

        public DeployApplication(Object testInstance, Statement next) {
            this.testInstance = testInstance;
            this.next = next;
        }

        @Override
        public void evaluate() throws Throwable {
            final Class<?> javaClass = testClass.getJavaClass();
            final ClassLoader loader = javaClass.getClassLoader();
            AppModule appModule = new AppModule(loader, javaClass.getSimpleName());

            // Add the test case as an @ManagedBean
            {
                final EjbJar ejbJar = new EjbJar();
                final OpenejbJar openejbJar = new OpenejbJar();
                final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(javaClass.getSimpleName(), javaClass.getName(), true));
                bean.setTransactionType(TransactionType.BEAN);
                final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);
                ejbDeployment.setDeploymentId(javaClass.getName());

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

            final List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Configuration.class);
            for (FrameworkMethod method : methods) {
                final Object o = method.invokeExplosively(testInstance);
                if (o instanceof Properties) {
                    Properties properties = (Properties) o;
                    configuration.putAll(properties);
                }
                break;
            }

            if (SystemInstance.isInitialized()) SystemInstance.reset();

            SystemInstance.init(configuration);

            // save the test under test to be able to retrieve it from extensions
            // /!\ has to be done before all other init
            SystemInstance.get().setComponent(TestInstance.class, new TestInstance(testClass.getJavaClass(), testInstance));

            // call the mock injector before module method to be able to use mocked classes
            // it will often use the TestInstance so
            final List<FrameworkMethod> mockInjectors = testClass.getAnnotatedMethods(MockInjector.class);
            for (FrameworkMethod method : mockInjectors) { // max == 1 so no need to break
                Object o = method.invokeExplosively(testInstance);
                if (o instanceof Class<?>) {
                    o = ((Class<?>) o).newInstance();
                }
                if (o instanceof FallbackPropertyInjector) {
                    SystemInstance.get().setComponent(FallbackPropertyInjector.class, (FallbackPropertyInjector) o);
                }
            }

            for (FrameworkMethod method : testClass.getAnnotatedMethods(Component.class)) {
                Object value = method.invokeExplosively(testInstance);
                if (value instanceof Class<?>) {
                    value = ((Class<?>) value).newInstance();
                }

                Class<?> key = method.getMethod().getReturnType();

                if (!key.isInstance(value)) { // we can't do it in validate to avoid to instantiate the value twice
                    throw new OpenEJBRuntimeException(value + " is not an instance of " + key.getName());
                }

                SystemInstance.get().setComponent((Class<Object>) key, value);
            }

            final Map<String, URL> additionalDescriptors = descriptorsToMap(testClass.getJavaClass().getAnnotation(Descriptors.class));

            Application application = null;

            int webModulesNb = 0;

            // Invoke the @Module producer methods to build out the AppModule
            for (FrameworkMethod method : testClass.getAnnotatedMethods(Module.class)) {

                final Object obj = method.invokeExplosively(testInstance);
                final Classes classesAnnotation = method.getAnnotation(Classes.class);

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

                    if (classesAnnotation != null) {
                        webModule.setFinder(finderFromClasses(classesAnnotation.value()));
                    }
                    DeploymentLoader.addWebModule(webModule, appModule);
                } else if (obj instanceof WebModule) { // will add the ejbmodule too
                    webModulesNb++;

                    final WebModule webModule = (WebModule) obj;

                    webModule.getAltDDs().putAll(additionalDescriptors);
                    webModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    if (classesAnnotation != null) {
                        webModule.setFinder(finderFromClasses(classesAnnotation.value()));
                    }
                    DeploymentLoader.addWebModule(webModule, appModule);
                } else if (obj instanceof EjbModule) {
                    final EjbModule ejbModule = (EjbModule) obj;

                    ejbModule.getAltDDs().putAll(additionalDescriptors);
                    ejbModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    if (classesAnnotation != null) {
                        ejbModule.setFinder(finderFromClasses(classesAnnotation.value()));
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
                    if (classesAnnotation != null) {
                        ejbModule.setFinder(finderFromClasses(classesAnnotation.value()));
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
                    appModule.addPersistenceModule(new PersistenceModule("", persistence));

                } else if (obj instanceof PersistenceUnit) {

                    final PersistenceUnit unit = (PersistenceUnit) obj;
                    appModule.addPersistenceModule(new PersistenceModule("", new Persistence(unit)));

                } else if (obj instanceof Beans) {

                    final Beans beans = (Beans) obj;
                    final EjbModule ejbModule = new EjbModule(new EjbJar(method.getName()));
                    ejbModule.setBeans(beans);
                    if (classesAnnotation != null) {
                        ejbModule.setFinder(finderFromClasses(classesAnnotation.value()));
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

            try {
                ConfigurationFactory config = new ConfigurationFactory();
                config.init(SystemInstance.get().getProperties());

                Assembler assembler = new Assembler();
                SystemInstance.get().setComponent(Assembler.class, assembler);

                assembler.buildContainerSystem(config.getOpenEjbConfiguration());

                if ("true".equals(configuration.getProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "false"))
                        || testClass.getJavaClass().getAnnotation(EnableServices.class) != null) {
                    try {
                        serviceManager = new ServiceManagerProxy();
                        serviceManager.start();
                    } catch (ServiceManagerProxy.AlreadyStartedException e) {
                        throw new OpenEJBRuntimeException(e);
                    }
                }

                final MockServletContext servletContext = new MockServletContext();
                final MockHttpSession session = new MockHttpSession();

                final AppInfo appInfo = config.configureApplication(appModule);

                final AppContext appContext = assembler.createApplication(appInfo);

                ScopeHelper.startContexts(appContext.getWebBeansContext().getContextsService(), servletContext, session);

                try {
                    final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                    final BeanContext context = containerSystem.getBeanContext(javaClass.getName());

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
                    final List<FrameworkField> fields = testClass.getAnnotatedFields(AppResource.class);
                    for (FrameworkField field : fields) {
                        final Class<?> type = field.getType();
                        if (AppModule.class.isAssignableFrom(type)) {
                            final Field jField = field.getField();
                            jField.setAccessible(true);
                            jField.set(testInstance, appModule);
                        } else if (Context.class.isAssignableFrom(type)) {
                            final Field jField = field.getField();
                            jField.setAccessible(true);
                            jField.set(testInstance, appContext.getGlobalJndiContext());
                        } else {
                            throw new IllegalArgumentException("can't find value for type " + type.getName());
                        }
                    }

                    final ThreadContext previous = ThreadContext.enter(new ThreadContext(context, null, Operation.BUSINESS));
                    try {
                        next.evaluate();
                    } finally {
                        ThreadContext.exit(previous);
                    }

                } finally {
                    ScopeHelper.stopContexts(appContext.getWebBeansContext().getContextsService(), servletContext, session);
                    assembler.destroyApplication(appInfo.path);
                }
            } finally {
                if (serviceManager != null) {
                    try {
                        serviceManager.stop();
                    } catch (RuntimeException ignored) {
                        // no-op
                    }
                }
                OpenEJB.destroy();
                SystemInstance.reset();
            }
        }

        private void load(String className) {
//            className = className.replace('/', '.');
            try {
                this.getClass().getClassLoader().loadClass(className);
            } catch (Throwable t1) {
                try {
                    this.getClass().getClassLoader().loadClass(className);
                } catch (Throwable t2) {
                }
            }
        }

        private <Module extends NamedModule> Module setId(Module module, FrameworkMethod method) {
            return setId(module, method.getName());
        }

        private <Module extends NamedModule> Module setId(Module module, String name) {
            if (module.getModuleName() != null) return module;
            if (module.getId() != null) return module;
            module.setId(name);
            return module;
        }
    }

    private static Map<String, URL> descriptorsToMap(final Descriptors descriptors) {
        if (descriptors != null) {
            final Map<String, URL> dds = new HashMap<String, URL>();
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            for (Descriptor descriptor : descriptors.value()) {
                dds.put(descriptor.name(), loader.getResource(descriptor.path()));
            }
            return dds;
        }
        return Collections.emptyMap();
    }

    private static IAnnotationFinder finderFromClasses(final Class<?>[] value) {
        return new AnnotationFinder(new ClassesArchive(value)).link();
    }
}
