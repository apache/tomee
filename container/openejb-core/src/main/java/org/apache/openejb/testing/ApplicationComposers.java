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
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.cdi.ScopeHelper;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ConnectorModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.sys.JSonConfigReader;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.NamedModule;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.ServiceManagerProxy;
import org.apache.openejb.util.URLs;
import org.apache.openejb.web.LightweightWebAppBuilder;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.web.lifecycle.test.MockHttpSession;
import org.apache.webbeans.web.lifecycle.test.MockServletContext;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.JarArchive;
import org.xml.sax.InputSource;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static org.apache.openejb.config.DeploymentFilterable.DEPLOYMENTS_CLASSPATH_PROPERTY;
import static org.apache.openejb.util.Classes.ancestors;

@SuppressWarnings("deprecation")
public final class ApplicationComposers {

    public static final String OPENEJB_APPLICATION_COMPOSER_CONTEXT = "openejb.application.composer.context";
    private static final Class[] MODULE_TYPES = {IAnnotationFinder.class, ClassesArchive.class,
            AppModule.class, WebModule.class, EjbModule.class,
            Application.class,
            WebApp.class, EjbJar.class, EnterpriseBean.class,
            Persistence.class, PersistenceUnit.class,
            Connector.class, Beans.class,
            Class[].class, Class.class
    };

    static {
        ApplicationComposers.linkageErrorProtection();
    }

    private final Map<Object, ClassFinder> testClassFinders;
    private final Class<?> testClass;
    private ServiceManagerProxy serviceManager;

    // invocation context
    private AppInfo appInfo;
    private Assembler assembler;
    private AppContext appContext;
    private ThreadContext previous;
    private MockHttpSession session;
    private MockServletContext servletContext;
    private final Collection<String> globalJndiEntries = new ArrayList<String>();

    public ApplicationComposers(final Class<?> klass, final Object... additionalModules) {
        testClass = klass;

        testClassFinders = new HashMap<Object, ClassFinder>();
        testClassFinders.put(this, new ClassFinder(ancestors(klass))); // using this temporary since we don't have yet the instance
        if (additionalModules != null) {
            for (final Object o : additionalModules) {
                testClassFinders.put(o, new ClassFinder(ancestors(o.getClass())));
            }
        }

        validate();
    }

    private void validate() {
        final List<Throwable> errors = new ArrayList<Throwable>();

        final Map<Object, List<Method>> annotatedConfigurationMethods = findAnnotatedMethods(new HashMap<Object, List<Method>>(), Configuration.class);
        {
            int nbProp = 0;
            int nbOpenejb = 0;
            for (final List<Method> list : annotatedConfigurationMethods.values()) {
                for (final Method m : list) {
                    final Class<?> type = m.getReturnType();
                    if (Openejb.class.isAssignableFrom(type) || String.class.equals(type)) {
                        nbOpenejb++;
                    } else if (Properties.class.isAssignableFrom(type)) {
                        nbProp++;
                    } // else not supported?
                }
            }
            if (nbProp > 1 || nbOpenejb > 1) {
                final String gripe = "Test class should have no more than one @Configuration method by type (Openejb/String or Properties)";
                errors.add(new Exception(gripe));
            }
        }

        int injectorSize = 0;
        for (final List<Method> m : findAnnotatedMethods(new HashMap<Object, List<Method>>(), org.apache.openejb.junit.MockInjector.class).values()) {
            injectorSize += m.size();
        }
        for (final List<Method> m : findAnnotatedMethods(new HashMap<Object, List<Method>>(), MockInjector.class).values()) {
            injectorSize += m.size();
        }
        if (injectorSize > 1) {
            errors.add(new Exception("Test class should have no more than one @MockInjector method"));
        }

        final List<Method> components = new ArrayList<Method>();
        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), Component.class).values()) {
            components.addAll(l);
        }
        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), org.apache.openejb.junit.Component.class).values()) {
            components.addAll(l);
        }
        for (final Method method : components) {
            if (method.getParameterTypes().length > 0) {
                errors.add(new Exception("@Component methods shouldn't take any parameters"));
            }
        }

        final List<Method> descriptors = new ArrayList<Method>();
        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), Descriptors.class).values()) {
            descriptors.addAll(l);
        }
        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), org.apache.openejb.junit.Descriptors.class).values()) {
            descriptors.addAll(l);
        }
        for (final Method method : descriptors) {
            final Class<?> returnType = method.getReturnType();
            if (!returnType.equals(WebModule.class) && !returnType.equals(EjbModule.class)
                    && !returnType.equals(WebApp.class) && !returnType.equals(EjbJar.class)
                    && !returnType.equals(AppModule.class)) {
                errors.add(new Exception("@Descriptors can't be used on " + returnType.getName()));
            }
        }

        final List<Method> classes = new ArrayList<Method>();
        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), Classes.class).values()) {
            classes.addAll(l);
        }
        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), org.apache.openejb.junit.Classes.class).values()) {
            classes.addAll(l);
        }
        for (final Method method : classes) {
            final Class<?> returnType = method.getReturnType();
            if (!returnType.equals(WebModule.class) && !returnType.equals(EjbModule.class)
                    && !returnType.equals(WebApp.class) && !returnType.equals(EjbJar.class)
                    && !EnterpriseBean.class.isAssignableFrom(returnType)) {
                errors.add(new Exception("@Classes can't be used on a method returning " + returnType));
            }
        }

        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), Jars.class).values()) {
            for (final Method method : l) {
                final Class<?> returnType = method.getReturnType();
                if (!returnType.equals(WebModule.class) && !returnType.equals(EjbModule.class)
                        && !returnType.equals(WebApp.class) && !returnType.equals(EjbJar.class)
                        && !EnterpriseBean.class.isAssignableFrom(returnType)) {
                    errors.add(new Exception("@Classes can't be used on a method returning " + returnType));
                }
            }
        }

        int appModules = 0;
        int modules = 0;

        final List<Method> moduleMethods = new ArrayList<Method>();
        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), Module.class).values()) {
            moduleMethods.addAll(l);
        }
        for (final List<Method> l : findAnnotatedMethods(new HashMap<Object, List<Method>>(), org.apache.openejb.junit.Module.class).values()) {
            moduleMethods.addAll(l);
        }
        for (final Method method : moduleMethods) {

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

    private Map<Object, List<Method>> findAnnotatedMethods(final Map<Object, List<Method>> map, final Class<? extends Annotation> annotation) {
        for (final Map.Entry<Object, ClassFinder> finder : testClassFinders.entrySet()) {
            final Object key = finder.getKey();
            final List<Method> newAnnotatedMethods = finder.getValue().findAnnotatedMethods(annotation);
            List<Method> annotatedMethods = map.get(key);
            if (annotatedMethods == null) {
                annotatedMethods = newAnnotatedMethods;
                map.put(key, annotatedMethods);
            } else {
                annotatedMethods.addAll(newAnnotatedMethods);
            }
        }
        return map;
    }

    private boolean isValidModuleType(final Class<?> type, final Class<?>[] moduleTypes) {
        for (final Class<?> moduleType : moduleTypes) {
            if (moduleType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void before(final Object inputTestInstance) throws Exception {
        // we hacked testInstance while we were not aware of it, now we can solve it
        testClassFinders.put(inputTestInstance, testClassFinders.remove(this));

        final ClassLoader loader = testClass.getClassLoader();
        AppModule appModule = new AppModule(loader, testClass.getSimpleName());

        // Add the test case as an @ManagedBean
        final ManagedBean testBean;
        {
            final EjbJar ejbJar = new EjbJar();
            final OpenejbJar openejbJar = new OpenejbJar();
            testBean = ejbJar.addEnterpriseBean(new ManagedBean(testClass.getSimpleName(), testClass.getName(), true));
            testBean.localBean();
            testBean.setTransactionType(TransactionType.BEAN);
            final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(testBean);
            ejbDeployment.setDeploymentId(testClass.getName());

            final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);
            ejbModule.getProperties().setProperty("openejb.cdi.activated", "false");
            final FinderFactory.OpenEJBAnnotationFinder finder = new FinderFactory.OpenEJBAnnotationFinder(new ClassesArchive(ancestors(testClass)));
            ejbModule.setFinder(finder);
            if (finder.findMetaAnnotatedFields(Inject.class).size()
                    + finder.findMetaAnnotatedMethods(Inject.class).size() > 0) { // activate cdi to avoid WARNINGs
                ejbModule.setBeans(new Beans());
            }
            appModule.getEjbModules().add(ejbModule);
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

        final EnableServices annotation = testClass.getAnnotation(EnableServices.class);
        if (annotation != null && annotation.httpDebug()) {
            configuration.setProperty("httpejbd.print", "true");
            configuration.setProperty("httpejbd.indent.xml", "true");
            configuration.setProperty("logging.level.OpenEJB.server.http", "FINE");
        }
        final org.apache.openejb.junit.EnableServices annotationOld = testClass.getAnnotation(org.apache.openejb.junit.EnableServices.class);
        if (annotationOld != null && annotationOld.httpDebug()) {
            configuration.setProperty("httpejbd.print", "true");
            configuration.setProperty("httpejbd.indent.xml", "true");
            configuration.setProperty("logging.level.OpenEJB.server.http", "FINE");
        }

        Openejb openejb = null;
        final Map<Object, List<Method>> configs = new HashMap<Object, List<Method>>();
        findAnnotatedMethods(configs, Configuration.class);
        findAnnotatedMethods(configs, org.apache.openejb.junit.Configuration.class);
        for (final Map.Entry<Object, List<Method>> method : configs.entrySet()) {
            for (final Method m : method.getValue()) {
                final Object o = m.invoke(method.getKey());
                if (o instanceof Properties) {
                    final Properties properties = (Properties) o;
                    configuration.putAll(properties);
                } else if (Openejb.class.isInstance(o)) {
                    openejb = Openejb.class.cast(o);
                } else if (String.class.isInstance(o)) {
                    final String path = String.class.cast(o);
                    final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
                    if (url == null) {
                        throw new IllegalArgumentException(o.toString() + " not found");
                    }
                    final InputStream in = url.openStream();
                    try {
                        if (path.endsWith(".json")) {
                            openejb = JSonConfigReader.read(Openejb.class, in);
                        } else {
                            openejb = JaxbOpenejb.readConfig(new InputSource(in));
                        }
                    } finally {
                        IO.close(in);
                    }
                }
            }
        }

        if (SystemInstance.isInitialized()) {
            SystemInstance.reset();
        }

        SystemInstance.init(configuration);

        final CdiExtensions cdiExtensions = testClass.getAnnotation(CdiExtensions.class);
        if (cdiExtensions != null) {
            SystemInstance.get().setComponent(LoaderService.class, new ExtensionAwareOptimizedLoaderService(cdiExtensions.value()));
        }

        // save the test under test to be able to retrieve it from extensions
        // /!\ has to be done before all other init
        SystemInstance.get().setComponent(TestInstance.class, new TestInstance(testClass, inputTestInstance));

        // call the mock injector before module method to be able to use mocked classes
        // it will often use the TestInstance so
        final Map<Object, List<Method>> mockInjectors = new HashMap<Object, List<Method>>();
        findAnnotatedMethods(mockInjectors, MockInjector.class);
        findAnnotatedMethods(mockInjectors, org.apache.openejb.junit.MockInjector.class);
        if (!mockInjectors.isEmpty() && !mockInjectors.values().iterator().next().isEmpty()) {
            final Map.Entry<Object, List<Method>> methods = mockInjectors.entrySet().iterator().next();
            Object o = methods.getValue().iterator().next().invoke(methods.getKey());
            if (o instanceof Class<?>) {
                o = ((Class<?>) o).newInstance();
            }
            if (o instanceof FallbackPropertyInjector) {
                SystemInstance.get().setComponent(FallbackPropertyInjector.class, (FallbackPropertyInjector) o);
            }
        }

        for (final Map.Entry<Object, List<Method>> method : findAnnotatedMethods(new HashMap<Object, List<Method>>(), Component.class).entrySet()) {
            for (final Method m : method.getValue()) {
                setComponent(method.getKey(), m);
            }
        }
        for (final Map.Entry<Object, List<Method>> method : findAnnotatedMethods(new HashMap<Object, List<Method>>(), org.apache.openejb.junit.Component.class).entrySet()) {
            for (final Method m : method.getValue()) {
                setComponent(method.getKey(), m);
            }
        }

        final Map<String, URL> additionalDescriptors = descriptorsToMap(testClass.getAnnotation(org.apache.openejb.junit.Descriptors.class));
        final Map<String, URL> additionalDescriptorsNew = descriptorsToMap(testClass.getAnnotation(Descriptors.class));
        additionalDescriptors.putAll(additionalDescriptorsNew);

        Application application = null;

        int webModulesNb = 0;

        // Invoke the @Module producer methods to build out the AppModule
        final Map<Object, List<Method>> moduleMethods = new HashMap<Object, List<Method>>();
        findAnnotatedMethods(moduleMethods, Module.class);
        findAnnotatedMethods(moduleMethods, org.apache.openejb.junit.Module.class);
        for (final Map.Entry<Object, List<Method>> methods : moduleMethods.entrySet()) {
            for (final Method method : methods.getValue()) {
                final Object obj = method.invoke(methods.getKey());
                final Jars jarsAnnotation = method.getAnnotation(Jars.class);
                final Classes classesAnnotation = method.getAnnotation(Classes.class);
                final org.apache.openejb.junit.Classes classesAnnotationOld = method.getAnnotation(org.apache.openejb.junit.Classes.class);

                Class<?>[] classes = null;
                Class<?>[] cdiInterceptors = null;
                Class<?>[] cdiAlternatives = null;
                Class<?>[] cdiDecorators = null;
                boolean cdi = false;
                if (classesAnnotation != null) {
                    classes = classesAnnotation.value();
                    cdiInterceptors = classesAnnotation.cdiInterceptors();
                    cdiDecorators = classesAnnotation.cdiDecorators();
                    cdiAlternatives = classesAnnotation.cdiAlternatives();
                    cdi = classesAnnotation.cdi() || cdiAlternatives.length > 0
                            || cdiDecorators.length > 0 || cdiInterceptors.length > 0;
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

                    testBean.getEnvEntry().addAll(webapp.getEnvEntry());

                    final WebModule webModule = new WebModule(webapp, root, Thread.currentThread().getContextClassLoader(), "", root);

                    webModule.getAltDDs().putAll(additionalDescriptors);
                    webModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    final EjbModule ejbModule = DeploymentLoader.addWebModule(webModule, appModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives));
                    }

                    final JaxrsProviders providers = method.getAnnotation(JaxrsProviders.class);
                    final Class<?>[] providersClasses = providers == null ? null : providers.value();
                    if (providers != null) {
                        if (classes == null) {
                            classes = providersClasses;
                        } else {
                            final Collection<Class<?>> newClasses = new ArrayList<Class<?>>(asList(classes));
                            newClasses.addAll(asList(providersClasses));
                            classes = newClasses.toArray(new Class<?>[newClasses.size()]);
                        }
                    }

                    final IAnnotationFinder finder = finderFromClasses(webModule, classes, findFiles(jarsAnnotation));
                    webModule.setFinder(finder);
                    ejbModule.setFinder(webModule.getFinder());

                    if (providersClasses != null) {
                        OpenejbJar openejbJar = ejbModule.getOpenejbJar();
                        if (openejbJar == null) {
                            openejbJar = new OpenejbJar();
                            ejbModule.setOpenejbJar(openejbJar);
                        }
                        final PojoDeployment pojoDeployment = new PojoDeployment();
                        pojoDeployment.setClassName(providers.applicationName());
                        pojoDeployment.getProperties().setProperty("cxf.jaxrs.providers", Join.join(",", providersClasses).replace("class ", ""));
                        openejbJar.getPojoDeployment().add(pojoDeployment);
                    }
                } else if (obj instanceof WebModule) { // will add the ejbmodule too
                    webModulesNb++;

                    final WebModule webModule = (WebModule) obj;

                    webModule.getAltDDs().putAll(additionalDescriptors);
                    webModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    final EjbModule ejbModule = DeploymentLoader.addWebModule(webModule, appModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives));
                    }

                    webModule.setFinder(finderFromClasses(webModule, classes, findFiles(jarsAnnotation)));
                    ejbModule.setFinder(webModule.getFinder());
                } else if (obj instanceof EjbModule) {
                    final EjbModule ejbModule = (EjbModule) obj;

                    ejbModule.getAltDDs().putAll(additionalDescriptors);
                    ejbModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    ejbModule.initAppModule(appModule);
                    appModule.getEjbModules().add(ejbModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives));
                    }

                    ejbModule.setFinder(finderFromClasses(ejbModule, classes, findFiles(jarsAnnotation)));
                } else if (obj instanceof EjbJar) {

                    final EjbJar ejbJar = (EjbJar) obj;
                    setId(ejbJar, method);

                    final EjbModule ejbModule = new EjbModule(ejbJar);

                    ejbModule.getAltDDs().putAll(additionalDescriptors);
                    ejbModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    appModule.getEjbModules().add(ejbModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives));
                    }

                    ejbModule.setFinder(finderFromClasses(ejbModule, classes, findFiles(jarsAnnotation)));
                } else if (obj instanceof EnterpriseBean) {

                    final EnterpriseBean bean = (EnterpriseBean) obj;
                    final EjbJar ejbJar = new EjbJar(method.getName());
                    ejbJar.addEnterpriseBean(bean);
                    final EjbModule ejbModule = new EjbModule(ejbJar);
                    final Beans beans = new Beans();
                    beans.addManagedClass(bean.getEjbClass());
                    ejbModule.setBeans(beans);
                    appModule.getEjbModules().add(ejbModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives));
                    }
                    ejbModule.setFinder(finderFromClasses(ejbModule, classes, findFiles(jarsAnnotation)));
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
                    appModule.getEjbModules().add(ejbModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(beans, cdiDecorators, cdiInterceptors, cdiAlternatives));
                    }
                    ejbModule.setFinder(finderFromClasses(ejbModule, classes, findFiles(jarsAnnotation)));
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

        // copy ejb into beans if cdi is activated and init finder
        for (final EjbModule ejb : appModule.getEjbModules()) {
            final EnterpriseBean[] enterpriseBeans = ejb.getEjbJar().getEnterpriseBeans();

            final Beans beans = ejb.getBeans();
            if (beans != null && ejb.getEjbJar() != null) {
                for (final EnterpriseBean bean : enterpriseBeans) {
                    if (!beans.getManagedClasses().contains(bean.getEjbClass())) {
                        beans.addManagedClass(bean.getEjbClass());
                    }
                }
            }
        }

        if (webModulesNb > 0 && SystemInstance.get().getComponent(WebAppBuilder.class) == null) {
            SystemInstance.get().setComponent(WebAppBuilder.class, new LightweightWebAppBuilder());
        }

        if (moduleMethods.size() == 1 && webModulesNb == 1) {
            appModule.setStandloneWebModule();
        }

        final ConfigurationFactory config = new ConfigurationFactory();
        config.init(SystemInstance.get().getProperties());

        assembler = new Assembler();
        SystemInstance.get().setComponent(Assembler.class, assembler);

        final OpenEjbConfiguration openEjbConfiguration;
        if (openejb != null) {
            openEjbConfiguration = config.getOpenEjbConfiguration(openejb);
        } else {
            openEjbConfiguration = config.getOpenEjbConfiguration();
        }
        assembler.buildContainerSystem(openEjbConfiguration);

        if ("true".equals(configuration.getProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "false"))
                || annotation != null || annotationOld != null) {
            try {
                if (annotation != null) {
                    initFilteredServiceManager(annotation.value());
                }
                if (annotationOld != null) {
                    initFilteredServiceManager(annotationOld.value());
                }
                serviceManager = new ServiceManagerProxy(false);
                serviceManager.start();
            } catch (final ServiceManagerProxy.AlreadyStartedException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }

        servletContext = new MockServletContext();
        session = new MockHttpSession();

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final Context jndiContext = containerSystem.getJNDIContext();

        for (final EnvEntry entry : testBean.getEnvEntry()) { // set it in global jndi context since that's "app" entries and otherwise when we are no more in test bean context lookup fails
            final String name = entry.getName();
            final String jndi;
            if (name.startsWith("java:") || name.startsWith("comp/env")) {
                jndi = name;
            } else {
                jndi = "java:comp/env/" + name;
            }
            jndiContext.bind(jndi, entry.getEnvEntryValue());
        }

        appInfo = config.configureApplication(appModule);

        appContext = assembler.createApplication(appInfo);

        ScopeHelper.startContexts(appContext.getWebBeansContext().getContextsService(), servletContext, session);

        final BeanContext context = containerSystem.getBeanContext(testClass.getName());

        enrich(inputTestInstance, context);

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        System.getProperties().put(OPENEJB_APPLICATION_COMPOSER_CONTEXT, appContext.getGlobalJndiContext());

        // test injections
        final ClassFinder testClassFinder = testClassFinders.remove(inputTestInstance);
        final List<Field> fields = new ArrayList<Field>(testClassFinder.findAnnotatedFields(AppResource.class));
        fields.addAll(testClassFinder.findAnnotatedFields(org.apache.openejb.junit.AppResource.class));
        for (final Field field : fields) {
            final Class<?> type = field.getType();
            if (AppModule.class.isAssignableFrom(type)) {
                field.setAccessible(true);
                field.set(inputTestInstance, appModule);
            } else if (Context.class.isAssignableFrom(type)) {
                field.setAccessible(true);
                field.set(inputTestInstance, new InitialContext(new Properties() {{
                    setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
                }}));
            } else {
                throw new IllegalArgumentException("can't find value for type " + type.getName());
            }
        }

        previous = ThreadContext.enter(new ThreadContext(context, null, Operation.BUSINESS));

        // switch back since next test will use another instance
        testClassFinders.put(this, testClassFinder);
    }

    private void enrich(final Object inputTestInstance, final BeanContext context) throws org.apache.openejb.OpenEJBException {
        final ThreadContext callContext = new ThreadContext(context, null, Operation.INJECTION);
        final ThreadContext oldContext = ThreadContext.enter(callContext);
        try {
            final InjectionProcessor processor = new InjectionProcessor(inputTestInstance, context.getInjections(), context.getJndiContext());
            processor.createInstance();

            Throwable error = null;
            try {
                OWBInjector.inject(appContext.getBeanManager(), inputTestInstance, null);
            } catch (final Throwable t) {
                error = t;
            }
            for (final WebContext web : appContext.getWebContexts()) {
                try {
                    OWBInjector.inject(web.getWebBeansContext().getBeanManagerImpl(), inputTestInstance, null);
                    // hourra, we enriched correctly the test then cleanup error state and quit
                    error = null;
                    break;
                } catch (final Throwable t) {
                    if (error == null) {
                        error = t;
                    } // else keep original one
                }
            }
            if (error != null) {
                error.printStackTrace();
            }
        } finally {
            ThreadContext.exit(oldContext);
        }
    }

    private Collection<File> findFiles(final Jars jarsAnnotation) {
        if (jarsAnnotation == null) {
            return null;
        }

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final List<URL> classpathAppsUrls = new ArrayList<URL>(8);
        if (jarsAnnotation.excludeDefaults()) {
            DeploymentsResolver.loadFromClasspath(null, classpathAppsUrls, classLoader);
        } else {
            UrlSet urlSet;
            try {
                urlSet = new UrlSet(classLoader);
                urlSet = URLs.cullSystemJars(urlSet);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
            classpathAppsUrls.addAll(urlSet.getUrls());
        }

        final String[] value = jarsAnnotation.value();
        final Collection<File> files = new ArrayList<File>(value.length);
        for (final String v : value) {
            final int size = files.size();
            for (final URL path : classpathAppsUrls) {
                final File file = URLs.toFile(path);
                if (file.getName().startsWith(v) && file.getName().endsWith(".jar")) {
                    files.add(file);
                    break;
                }
            }
            if (size == files.size()) {
                throw new IllegalArgumentException(v + " not found in classpath");
            }
        }
        return files;
    }

    private Beans beans(final Beans beans, final Class<?>[] cdiDecorators, final Class<?>[] cdiInterceptors,
                        final Class<?>[] cdiAlternatives) {
        if (cdiDecorators != null) {
            for (final Class<?> clazz : cdiDecorators) {
                beans.addDecorator(clazz);
            }
        }
        if (cdiInterceptors != null) {
            for (final Class<?> clazz : cdiInterceptors) {
                beans.addInterceptor(clazz);
            }
        }
        if (cdiAlternatives != null) {
            for (final Class<?> clazz : cdiAlternatives) {
                beans.addAlternativeClass(clazz);
            }
        }
        return beans;
    }

    @SuppressWarnings("unchecked")
    private void setComponent(final Object testInstance, final Method method) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object value = method.invoke(testInstance);
        if (value instanceof Class<?>) {
            value = ((Class<?>) value).newInstance();
        }

        final Class<?> key = method.getReturnType();

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
            ThreadContext.exit(previous);
            after();
        }

    }

    public void after() throws Exception {
        if (assembler != null) {
            final ContextsService contextsService = appContext.getWebBeansContext().getContextsService();
            contextsService.endContext(SessionScoped.class, session);
            contextsService.endContext(RequestScoped.class, null);
            contextsService.endContext(ConversationScoped.class, null);

            try {
                assembler.destroyApplication(appInfo.path);
            } catch (final Exception e) {
                // no-op
            }

            final ContainerSystem component = SystemInstance.get().getComponent(ContainerSystem.class);

            if (null != component) {
                final Context context = component.getJNDIContext();

                for (final String entry : globalJndiEntries) {
                    context.unbind(entry);
                }
            }

            globalJndiEntries.clear();

            try {
                ScopeHelper.stopContexts(contextsService, servletContext, session);
            } catch (final Exception e) {
                // no-op
            }
        }

        if (serviceManager != null) {

            try {
                serviceManager.stop();
            } catch (final RuntimeException ignored) {
                // no-op
            }
        }

        OpenEJB.destroy();
    }

    private <M extends NamedModule> M setId(final M module, final Method method) {
        return setId(module, method.getName());
    }

    private <M extends NamedModule> M setId(final M module, final String name) {
        if (module.getModuleName() != null) {
            return module;
        }
        if (module.getId() != null) {
            return module;
        }
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
        } catch (final IOException e) {
            return "";
        }
    }

    private static Map<String, URL> descriptorsToMap(final Object descriptors) {
        if (descriptors != null) {
            final Map<String, URL> dds = new HashMap<String, URL>();
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (descriptors instanceof Descriptors) {
                for (final Descriptor descriptor : ((Descriptors) descriptors).value()) {
                    dds.put(descriptor.name(), loader.getResource(descriptor.path()));
                }
            } else {
                if (descriptors instanceof org.apache.openejb.junit.Descriptors) {
                    for (final org.apache.openejb.junit.Descriptor descriptor : ((org.apache.openejb.junit.Descriptors) descriptors).value()) {
                        dds.put(descriptor.name(), loader.getResource(descriptor.path()));
                    }
                }
            }
            return dds;
        }
        return Collections.emptyMap();
    }

    private static IAnnotationFinder finderFromClasses(final DeploymentModule module, final Class<?>[] value, final Collection<File> others) {
        final Collection<Archive> archives = new ArrayList<Archive>(1 + (others == null ? 0 : others.size()));

        final Collection<Class<?>> classes = new ArrayList<Class<?>>(asList(FinderFactory.ensureMinimalClasses(module)));
        if (value != null) {
            classes.addAll(asList(value));
        }
        archives.add(new ClassesArchive(classes));

        if (others != null) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (final File f : others) {
                try {
                    archives.add(new JarArchive(classLoader, f.toURI().toURL()));
                } catch (final MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return new FinderFactory.OpenEJBAnnotationFinder(new CompositeArchive(archives)).link();
    }

    @SuppressWarnings("unchecked")
    private void initFilteredServiceManager(final String[] services) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final Class serviceManagerClass;
        try {
            serviceManagerClass = classLoader.loadClass("org.apache.openejb.server.FilteredServiceManager");
        } catch (final ClassNotFoundException e) {
            final String msg = "Services filtering requires class 'org.apache.openejb.server.FilteredServiceManager' to be available.  " +
                    "Make sure you have the openejb-server-*.jar in your classpath.";
            throw new IllegalStateException(msg, e);
        }

        try {
            final Method initServiceManager = serviceManagerClass.getMethod("initServiceManager", String[].class);
            initServiceManager.invoke(null, new Object[]{services});
        } catch (final Exception e) {
            throw new IllegalStateException("Failed initializing FilteredServiceManager with services " + Arrays.toString(services), e);
        }
    }

    private static void linkageErrorProtection() { // mainly for macos jre
        final ClassLoader loader = ApplicationComposers.class.getClassLoader();
        try {
            Class.forName("sun.security.pkcs11.SunPKCS11", true, loader);
            Class.forName("sun.security.pkcs11.SunPKCS11$Descriptor", true, loader);
            Class.forName("sun.security.pkcs11.wrapper.PKCS11Exception", true, loader);
        } catch (final Throwable e) {
            // no-op: not an issue
        }
    }

    protected static class ExtensionAwareOptimizedLoaderService extends OptimizedLoaderService {
        private final Class<? extends Extension>[] extensions;

        protected ExtensionAwareOptimizedLoaderService(final Class<? extends Extension>[] extensions) {
            this.extensions = extensions;
        }

        @Override
        protected List<? extends Extension> loadExtensions(final ClassLoader classLoader) {
            final List<Extension> list = new ArrayList<Extension>();
            for (final Class<? extends Extension> e : extensions) {
                try {
                    list.add(e.newInstance());
                } catch (final Exception e1) {
                    throw new OpenEJBRuntimeException(e1);
                }
            }
            return list;
        }
    }
}
