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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.cdi.CdiScanner;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.cdi.ScopeHelper;
import org.apache.openejb.cdi.ThreadSingletonService;
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
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ParentClassLoaderFinder;
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
import org.apache.openejb.rest.RESTResourceFinder;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.rest.ContextProvider;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.NetworkUtil;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.openejb.util.ServiceManagerProxy;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.reflection.Reflections;
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
import org.apache.xbean.finder.archive.FileArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.archive.JarArchive;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;
import org.xml.sax.InputSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static java.util.Arrays.asList;
import static org.apache.openejb.config.DeploymentFilterable.DEPLOYMENTS_CLASSPATH_PROPERTY;
import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.apache.openejb.util.Classes.ancestors;

// TODO: surely explode it and base it on refactored ContainerRule and DeployApplicationRule
@SuppressWarnings("deprecation")
public class ApplicationComposers {

    public static final String OPENEJB_APPLICATION_COMPOSER_CONTEXT = "openejb.application.composer.context";
    private static final Class[] MODULE_TYPES = {IAnnotationFinder.class, ClassesArchive.class,
            AppModule.class, WebModule.class, EjbModule.class,
            Application.class,
            WebApp.class, EjbJar.class, EnterpriseBean.class,
            Persistence.class, PersistenceUnit.class,
            Connector.class, Beans.class,
            Class[].class, Class.class,
            Resources.class
    };

    static {
        ApplicationComposers.linkageErrorProtection();
    }

    private final Map<Object, ClassFinder> testClassFinders;
    private final Class<?> testClass;
    private ServiceManagerProxy serviceManager;

    // invocation context
    private ClassLoader originalLoader;
    private AppInfo appInfo;
    private Assembler assembler;
    private AppContext appContext;
    private ThreadContext previous;
    private MockHttpSession session;
    private MockServletContext servletContext;
    private final Collection<String> globalJndiEntries = new ArrayList<>();
    private final Collection<Runnable> beforeDestroyAfterRunnables = new ArrayList<>();
    private final Collection<Runnable> afterRunnables = new ArrayList<>();
    private Properties originalProperties;

    public ApplicationComposers(final Object... modules) {
        this(modules[0].getClass(), modules);
    }

    public ApplicationComposers(final Class<?> klass, final Object... additionalModules) {
        testClass = klass;

        testClassFinders = new HashMap<>();
        testClassFinders.put(this, new ClassFinder(ancestors(klass))); // using this temporary since we don't have yet the instance
        if (additionalModules != null) {
            for (final Object o : additionalModules) {
                final Class<?> aClass = o.getClass();
                if (aClass != klass) {
                    testClassFinders.put(o, new ClassFinder(ancestors(aClass)));
                }
            }
        }

        validate();
        assembler = SystemInstance.get().getComponent(Assembler.class); // for DeployApplicationRule we need it
    }

    protected boolean isContainer() {
        return true;
    }

    protected boolean isApplication() {
        return true;
    }

    private void validate() {
        final List<Throwable> errors = new ArrayList<>();

        if (isContainer()) {
            final Map<Object, List<Method>> annotatedConfigurationMethods = findAnnotatedMethods(new HashMap<>(), Configuration.class);
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
            for (final List<Method> m : findAnnotatedMethods(new HashMap<>(), org.apache.openejb.junit.MockInjector.class).values()) {
                injectorSize += m.size();
            }
            for (final List<Method> m : findAnnotatedMethods(new HashMap<>(), MockInjector.class).values()) {
                injectorSize += m.size();
            }
            if (injectorSize > 1) {
                errors.add(new Exception("Test class should have no more than one @MockInjector method"));
            }

            final List<Method> components = new ArrayList<>();
            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), Component.class).values()) {
                components.addAll(l);
            }
            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), org.apache.openejb.junit.Component.class).values()) {
                components.addAll(l);
            }
            for (final Method method : components) {
                if (method.getParameterTypes().length > 0) {
                    errors.add(new Exception("@Component methods shouldn't take any parameters"));
                }
            }
            for (final ClassFinder finder : testClassFinders.values()) {
                for (final Field field : finder.findAnnotatedFields(RandomPort.class)) {
                    final Class<?> type = field.getType();
                    if (int.class != type && URL.class != type) {
                        throw new IllegalArgumentException("@RandomPort is only supported for int fields");
                    }
                }
            }
        }

        if (isApplication()) {
            final List<Method> descriptors = new ArrayList<>();
            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), Descriptors.class).values()) {
                descriptors.addAll(l);
            }
            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), org.apache.openejb.junit.Descriptors.class).values()) {
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

            final List<Method> classes = new ArrayList<>();
            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), Classes.class).values()) {
                classes.addAll(l);
            }
            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), org.apache.openejb.junit.Classes.class).values()) {
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

            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), Jars.class).values()) {
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

            final List<Method> moduleMethods = new ArrayList<>();
            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), Module.class).values()) {
                moduleMethods.addAll(l);
            }
            for (final List<Method> l : findAnnotatedMethods(new HashMap<>(), org.apache.openejb.junit.Module.class).values()) {
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

            if (modules < 1 && testClass.getAnnotation(Classes.class) == null && testClass.getAnnotation(Default.class) == null) {
                final String gripe = "Test class should have at least one @Module method";
                errors.add(new Exception(gripe));
            }
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
                for (final Method m : newAnnotatedMethods) {
                    if (!annotatedMethods.contains(m)) {
                        annotatedMethods.add(m);
                    }
                }
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
        fixFakeClassFinder(inputTestInstance);

        startContainer(inputTestInstance);

        servletContext = new MockServletContext();
        session = new MockHttpSession();

        deployApp(inputTestInstance);
    }

    public void deployApp(final Object inputTestInstance) throws Exception {
        final ClassFinder testClassFinder = fixFakeClassFinder(inputTestInstance);

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
                    + finder.findMetaAnnotatedMethods(Inject.class).size() > 0) { // "activate" cdi to avoid WARNINGs
                ejbModule.setBeans(new Beans());
            }
            appModule.getEjbModules().add(ejbModule);
        }

        final Map<String, URL> additionalDescriptors = descriptorsToMap(testClass.getAnnotation(org.apache.openejb.junit.Descriptors.class));
        final Map<String, URL> additionalDescriptorsNew = descriptorsToMap(testClass.getAnnotation(Descriptors.class));
        additionalDescriptors.putAll(additionalDescriptorsNew);

        Application application = null;

        int webModulesNb = 0;

        final Jars globalJarsAnnotation = testClass.getAnnotation(Jars.class);

        // Invoke the @Module producer methods to build out the AppModule
        int moduleNumber = 0;
        int notBusinessModuleNumber = 0; // we dont consider resources.xml to set an app as standalone or not
        final Map<Object, List<Method>> moduleMethods = new HashMap<>();
        findAnnotatedMethods(moduleMethods, Module.class);
        findAnnotatedMethods(moduleMethods, org.apache.openejb.junit.Module.class);
        for (final Map.Entry<Object, List<Method>> methods : moduleMethods.entrySet()) {
            moduleNumber += methods.getValue().size();
            for (final Method method : methods.getValue()) {
                final Object obj = method.invoke(methods.getKey());
                final Jars jarsAnnotation = method.getAnnotation(Jars.class);
                final Classes classesAnnotation = method.getAnnotation(Classes.class);
                final org.apache.openejb.junit.Classes classesAnnotationOld = method.getAnnotation(org.apache.openejb.junit.Classes.class);
                final boolean defaultConfig = method.getAnnotation(Default.class) != null;

                Class<?>[] classes = null;
                String[] excludes = null;
                Class<?>[] cdiInterceptors = null;
                Class<?>[] cdiAlternatives = null;
                Class<?>[] cdiStereotypes = null;
                Class<?>[] cdiDecorators = null;
                boolean cdi = false;
                boolean innerClassesAsBean = false;
                if (classesAnnotation != null) {
                    classes = classesAnnotation.value();
                    excludes = classesAnnotation.excludes();
                    innerClassesAsBean = classesAnnotation.innerClassesAsBean();
                    cdiInterceptors = classesAnnotation.cdiInterceptors();
                    cdiDecorators = classesAnnotation.cdiDecorators();
                    cdiAlternatives = classesAnnotation.cdiAlternatives();
                    cdiStereotypes = classesAnnotation.cdiStereotypes();
                    cdi = isCdi(classesAnnotation.cdi(), cdiInterceptors, cdiAlternatives, cdiStereotypes, cdiDecorators);
                } else if (classesAnnotationOld != null) {
                    classes = classesAnnotationOld.value();
                }

                if (obj instanceof WebApp) { // will add the ejbmodule too
                    final WebApp webApp = WebApp.class.cast(obj);
                    if (webApp.getContextRoot() == null && classesAnnotation != null) {
                        webApp.contextRoot(classesAnnotation.context());
                    }
                    webModulesNb++;
                    addWebApp(
                            appModule, testBean, additionalDescriptors,
                            method.getAnnotation(Descriptors.class), method.getAnnotation(JaxrsProviders.class),
                            webApp,
                            globalJarsAnnotation, jarsAnnotation,
                            classes, excludes, cdiInterceptors, cdiAlternatives, cdiDecorators, cdiStereotypes, cdi, innerClassesAsBean,
                            defaultConfig);
                } else if (obj instanceof WebModule) { // will add the ejbmodule too
                    webModulesNb++;

                    final WebModule webModule = (WebModule) obj;

                    webModule.getAltDDs().putAll(additionalDescriptors);
                    webModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    final EjbModule ejbModule = DeploymentLoader.addWebModule(webModule, appModule);
                    ejbModule.getProperties().put(CdiScanner.OPENEJB_CDI_FILTER_CLASSLOADER, "false");
                    if (cdi) {
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives, cdiStereotypes));
                    }

                    Collection<File> files = findFiles(jarsAnnotation);
                    if (defaultConfig) {
                        (files == null ? files = new LinkedList<>() : files).add(jarLocation(testClass));
                    }
                    webModule.setFinder(finderFromClasses(webModule, classes, files, excludes));
                    ejbModule.setFinder(webModule.getFinder());
                } else if (obj instanceof EjbModule) {
                    final EjbModule ejbModule = (EjbModule) obj;

                    ejbModule.getAltDDs().putAll(additionalDescriptors);
                    ejbModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    ejbModule.initAppModule(appModule);
                    appModule.getEjbModules().add(ejbModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives, cdiStereotypes));
                    }

                    Collection<File> files = findFiles(jarsAnnotation);
                    if (defaultConfig) {
                        (files == null ? files = new LinkedList<>() : files).add(jarLocation(testClass));
                    }
                    ejbModule.setFinder(finderFromClasses(ejbModule, classes, files, excludes));
                } else if (obj instanceof EjbJar) {

                    final EjbJar ejbJar = (EjbJar) obj;
                    setId(ejbJar, method);

                    final EjbModule ejbModule = new EjbModule(ejbJar);

                    ejbModule.getAltDDs().putAll(additionalDescriptors);
                    ejbModule.getAltDDs().putAll(descriptorsToMap(method.getAnnotation(Descriptors.class)));

                    appModule.getEjbModules().add(ejbModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives, cdiStereotypes));
                    }

                    Collection<File> files = findFiles(jarsAnnotation);
                    if (defaultConfig) {
                        (files == null ? files = new LinkedList<>() : files).add(jarLocation(testClass));
                    }
                    ejbModule.setFinder(finderFromClasses(ejbModule, classes, files, excludes));
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
                        ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives, cdiStereotypes));
                    }
                    Collection<File> files = findFiles(jarsAnnotation);
                    if (defaultConfig) {
                        (files == null ? files = new LinkedList<>() : files).add(jarLocation(testClass));
                    }
                    ejbModule.setFinder(finderFromClasses(ejbModule, classes, files, excludes));
                } else if (obj instanceof Application) {

                    application = (Application) obj;
                    setId(application, method);

                } else if (obj instanceof Connector) {

                    final Connector connector = (Connector) obj;
                    setId(connector, method);
                    appModule.getConnectorModules().add(new ConnectorModule(connector));

                } else if (obj instanceof Persistence) {

                    final Persistence persistence = (Persistence) obj;
                    appModule.addPersistenceModule(
                            new PersistenceModule(appModule, implicitRootUrl(method.getAnnotation(PersistenceRootUrl.class)), persistence));
                    notBusinessModuleNumber++;
                } else if (obj instanceof PersistenceUnit) {

                    final PersistenceUnit unit = (PersistenceUnit) obj;
                    appModule.addPersistenceModule(
                            new PersistenceModule(appModule, implicitRootUrl(method.getAnnotation(PersistenceRootUrl.class)), new Persistence(unit)));
                    notBusinessModuleNumber++;
                } else if (obj instanceof Beans) {

                    final Beans beans = (Beans) obj;
                    final EjbModule ejbModule = new EjbModule(new EjbJar(method.getName()));
                    ejbModule.setBeans(beans);
                    appModule.getEjbModules().add(ejbModule);
                    if (cdi) {
                        ejbModule.setBeans(beans(beans, cdiDecorators, cdiInterceptors, cdiAlternatives, cdiStereotypes));
                    }
                    Collection<File> files = findFiles(jarsAnnotation);
                    if (defaultConfig) {
                        (files == null ? files = new LinkedList<>() : files).add(jarLocation(testClass));
                    }
                    ejbModule.setFinder(finderFromClasses(ejbModule, classes, files, excludes));
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
                } else if (obj instanceof Resources) {
                    final Resources asResources = Resources.class.cast(obj);
                    appModule.getResources().addAll(asResources.getResource());
                    appModule.getContainers().addAll(asResources.getContainer());
                    notBusinessModuleNumber++;
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
                } else {
                    moduleNumber--;
                }
            }
        }

        final Classes classClasses = testClass.getAnnotation(Classes.class);
        if (classClasses != null) {
            final WebApp webapp = new WebApp();
            webapp.setContextRoot(classClasses.context());
            addWebApp(
                    appModule, testBean, additionalDescriptors,
                    null, null,
                    webapp, globalJarsAnnotation, null, classClasses.value(), classClasses.excludes(),
                    classClasses.cdiInterceptors(), classClasses.cdiAlternatives(), classClasses.cdiDecorators(), classClasses.cdiStereotypes(),
                    classClasses.cdi(), classClasses.innerClassesAsBean(), testClass.getAnnotation(Default.class) != null);
            webModulesNb++;
            moduleNumber++;
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

        // config for the app
        for (final Map.Entry<Object, List<Method>> method : findAnnotatedMethods(new HashMap<>(), ApplicationConfiguration.class).entrySet()) {
            for (final Method m : method.getValue()) {
                final Object o = m.invoke(method.getKey());
                if (Properties.class.isInstance(o)) {
                    appModule.getProperties().putAll(Properties.class.cast(o));
                }
            }
        }

        // copy ejb into beans if cdi is activated and init finder
        for (final EjbModule ejb : appModule.getEjbModules()) {
            final EnterpriseBean[] enterpriseBeans = ejb.getEjbJar().getEnterpriseBeans();

            final Beans beans = ejb.getBeans();
            if (beans != null && ejb.getEjbJar() != null) {
                for (final EnterpriseBean bean : enterpriseBeans) {
                    boolean found = false;
                    for (final List<String> mc : beans.getManagedClasses().values()) {
                        if (mc.contains(bean.getEjbClass())) {
                            found = true;
                            break;
                        }

                    }
                    if (!found) {
                        beans.addManagedClass(bean.getEjbClass());
                    }
                }
            }
        }

        if (moduleNumber - notBusinessModuleNumber == 1 && webModulesNb == 1) {
            appModule.setStandloneWebModule();
        }

        if (webModulesNb > 0 && SystemInstance.get().getComponent(WebAppBuilder.class) == null) {
            SystemInstance.get().setComponent(WebAppBuilder.class, new LightweightWebAppBuilder());
        }

        final Context jndiContext = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
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

        appInfo = SystemInstance.get().getComponent(ConfigurationFactory.class).configureApplication(appModule);
        appContext = assembler.createApplication(appInfo);

        if (mockCdiContexts() && appContext.getWebBeansContext() != null) {
            ScopeHelper.startContexts(appContext.getWebBeansContext().getContextsService(), servletContext, session);
        }

        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext(testClass.getName());

        enrich(inputTestInstance, context);

        JavaSecurityManagers.setSystemProperty(Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        JavaSecurityManagers.setSystemProperty(OPENEJB_APPLICATION_COMPOSER_CONTEXT, appContext.getGlobalJndiContext());

        final List<Field> fields = new ArrayList<>(testClassFinder.findAnnotatedFields(AppResource.class));
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
            } else if (ApplicationComposers.class.isAssignableFrom(type)) {
                field.setAccessible(true);
                field.set(inputTestInstance, this);
            } else if (ContextProvider.class.isAssignableFrom(type)) {
                RESTResourceFinder finder = SystemInstance.get().getComponent(RESTResourceFinder.class);
                if (finder == null || !ContextProvider.class.isInstance(finder)) {
                    finder = new ContextProvider(finder);
                    SystemInstance.get().setComponent(RESTResourceFinder.class, finder);
                }

                field.setAccessible(true);
                field.set(inputTestInstance, finder);
            } else {
                throw new IllegalArgumentException("can't find value for type " + type.getName());
            }
        }

        previous = ThreadContext.enter(new ThreadContext(context, null, Operation.BUSINESS));

        // switch back since next test will use another instance
        testClassFinders.put(this, testClassFinder);
    }

    private ClassFinder fixFakeClassFinder(final Object inputTestInstance) {
        // test injections, we faked the instance before having it so ensuring we use the right finder
        ClassFinder testClassFinder = testClassFinders.get(inputTestInstance);
        if (testClassFinder == null) {
            final ApplicationComposers self = this;
            final ClassFinder remove = testClassFinders.remove(self);
            if (remove != null) {
                testClassFinders.put(inputTestInstance, remove);
                testClassFinder = remove;
                afterRunnables.add(new Runnable() { // reset state for next test
                    @Override
                    public void run() {
                        final ClassFinder classFinder = testClassFinders.remove(inputTestInstance);
                        if (classFinder != null) {
                            testClassFinders.put(self, classFinder);
                        }
                    }
                });
            }
        }
        return testClassFinder;
    }

    private boolean isCdi(final boolean cdi, final Class<?>[] cdiInterceptors,
                          final Class<?>[] cdiAlternatives, final Class<?>[] cdiStereotypes,
                          final Class<?>[] cdiDecorators) {
        return cdi
                || isNotNullOrEmpty(cdiAlternatives)
                || isNotNullOrEmpty(cdiDecorators)
                || isNotNullOrEmpty(cdiInterceptors)
                || isNotNullOrEmpty(cdiStereotypes);
    }

    private boolean isNotNullOrEmpty(final Class<?>[] ca) {
        return null != ca && ca.length > 0;
    }

    protected boolean mockCdiContexts() {
        return "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.testing.start-cdi-contexts", "true"));
    }

    private void addWebApp(final AppModule appModule, final ManagedBean testBean,
                           final Map<String, URL> additionalDescriptors,
                           final Descriptors descriptors,
                           final JaxrsProviders providers,
                           final WebApp webapp, final Jars globalJarsAnnotation,
                           final Jars jarsAnnotation,
                           final Class<?>[] cdiClasses,
                           final String[] excludes,
                           final Class<?>[] cdiInterceptors,
                           final Class<?>[] cdiAlternatives,
                           final Class<?>[] cdiDecorators,
                           final Class<?>[] cdiStereotypes,
                           final boolean cdi,
                           final boolean innerClassesAsBean,
                           final boolean autoConfig) throws OpenEJBException {
        String root = webapp.getContextRoot();
        if (root == null) {
            root = "/openejb";
        }

        testBean.getEnvEntry().addAll(webapp.getEnvEntry());

        final WebModule webModule = new WebModule(webapp, root, Thread.currentThread().getContextClassLoader(), "", root);

        final File thisJar;
        if (autoConfig) {
            thisJar = jarLocation(testClass);
            try {
                webModule.getAltDDs().putAll(DeploymentLoader.mapDescriptors(new ResourceFinder("", webModule.getClassLoader(), thisJar.toURI().toURL())));
                webModule.getAltDDs().putAll(DeploymentLoader.getWebDescriptors(new File(thisJar.getParentFile().getParentFile(), "src/main/webapp")));
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            thisJar = null;
        }

        webModule.getAltDDs().putAll(additionalDescriptors);
        for (final Descriptors d : asList(testClass.getAnnotation(Descriptors.class), descriptors)) {
            if (d != null) {
                webModule.getAltDDs().putAll(descriptorsToMap(d));
            }
        }

        final EjbModule ejbModule = DeploymentLoader.addWebModule(webModule, appModule);
        ejbModule.getProperties().put(CdiScanner.OPENEJB_CDI_FILTER_CLASSLOADER, "false");
        if (isCdi(cdi, cdiInterceptors, cdiAlternatives, cdiStereotypes, cdiDecorators)) {
            ejbModule.setBeans(beans(new Beans(), cdiDecorators, cdiInterceptors, cdiAlternatives, cdiStereotypes));
        }

        Class<?>[] classes = cdiClasses;
        final Class<?>[] providersClasses = providers == null ? null : providers.value();
        for (final JaxrsProviders p : asList(testClass.getAnnotation(JaxrsProviders.class), providers)) {
            if (p != null) {
                if (classes == null) {
                    classes = p.value();
                } else {
                    final Collection<Class<?>> newClasses = new ArrayList<>(asList(classes));
                    newClasses.addAll(asList(p.value()));
                    classes = newClasses.toArray(new Class<?>[newClasses.size()]);
                }
            }
        }
        if (innerClassesAsBean) {
            final Collection<Class<?>> inners = new LinkedList<>();
            for (final Class<?> clazz : testClass.getClasses()) {
                final int modifiers = clazz.getModifiers();
                try {
                    if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && clazz.getConstructor() != null) {
                        inners.add(clazz);
                    }
                } catch (final NoSuchMethodException nsme) {
                    // no-op, skip it
                }
            }

            if (!inners.isEmpty()) {
                final Collection<Class<?>> newClasses = new ArrayList<>(asList(classes));
                newClasses.addAll(inners);
                classes = newClasses.toArray(new Class<?>[newClasses.size()]);
            }
        }

        Collection<File> libs = null;
        for (final Jars jars : asList(jarsAnnotation, globalJarsAnnotation)) {
            final Collection<File> files = findFiles(jars);
            if (files != null) {
                if (libs == null) {
                    libs = new LinkedList<>();
                }
                libs.addAll(files);
            }
        }
        if (autoConfig) {
            if (libs == null) {
                libs = new LinkedList<>();
            }
            libs.add(thisJar);
            if ("test-classes".equals(thisJar.getName()) && "target".equals(thisJar.getParentFile().getName())) { // mvn
                final File mainClasses = new File(thisJar.getParentFile(), "classes");
                if (mainClasses.exists()) {
                    libs.add(mainClasses);
                }
            } else if ("test".equals(thisJar.getName()) && "classes".equals(thisJar.getParentFile().getName())) { // gradle
                final File mainClasses = new File(thisJar.getParentFile(), "main");
                if (mainClasses.exists()) {
                    libs.add(mainClasses);
                }
            }
        }

        final IAnnotationFinder finder = finderFromClasses(webModule, classes, libs, excludes);
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
            // it is specified so skip scanning otherwise we'll get them twice
            pojoDeployment.getProperties().setProperty("cxf.jaxrs.skip-provider-scanning", "true");
            openejbJar.getPojoDeployment().add(pojoDeployment);
        }
    }

    public void enrich(final Object inputTestInstance) throws org.apache.openejb.OpenEJBException {
        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext(inputTestInstance.getClass());
        enrich(inputTestInstance, context);
    }

    private void enrich(final Object inputTestInstance, final BeanContext context) throws org.apache.openejb.OpenEJBException {
        if (context == null) {
            return;
        }

        final ThreadContext callContext = new ThreadContext(context, null, Operation.INJECTION);
        final ThreadContext oldContext = ThreadContext.enter(callContext);
        try {
            final InjectionProcessor processor = new InjectionProcessor(inputTestInstance, context.getInjections(), context.getJndiContext());
            processor.createInstance();

            Throwable error = null;
            try {
                if (appContext.getBeanManager() != null) {
                    OWBInjector.inject(appContext.getBeanManager(), inputTestInstance, null);
                }
            } catch (final Throwable t) {
                error = t;
            }
            for (final WebContext web : appContext.getWebContexts()) {
                if (web.getWebBeansContext() == null) {
                    continue;
                }
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

    public static Collection<File> findFiles(final Jars jarsAnnotation) {
        if (jarsAnnotation == null) {
            return null;
        }

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final List<URL> classpathAppsUrls = new ArrayList<>(8);
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
        final Collection<File> files = new ArrayList<>(value.length);
        for (final String v : value) {
            final int size = files.size();
            for (final URL path : classpathAppsUrls) {
                final File file = URLs.toFile(path);
                if (file.getName().startsWith(v) && file.getName().endsWith(".jar")) {
                    files.add(file);
                } else if ("classes".equals(file.getName()) && "target".equals(file.getParentFile().getName())
                        && file.getParentFile().getParentFile().getName().startsWith(v)) {
                    files.add(file);
                }
            }
            if (size == files.size()) {
                throw new IllegalArgumentException(v + " not found in classpath");
            }
        }
        return files;
    }

    private Beans beans(final Beans beans, final Class<?>[] cdiDecorators, final Class<?>[] cdiInterceptors,
                        final Class<?>[] cdiAlternatives, final Class<?>[] cdiStereotypes) {
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
        if (cdiStereotypes != null) {
            for (final Class<?> clazz : cdiStereotypes) {
                beans.addAlternativeStereotype(clazz);
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

    public <T> T evaluate(final Object testInstance, final Callable<T> next) throws Exception {
        before(testInstance);
        try {
            return next.call();
        } finally {
            ThreadContext.exit(previous);
            after();
        }
    }

    public void evaluate(final Object testInstance, final Runnable next) throws Exception {
        evaluate(testInstance, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                next.run();
                return null;
            }
        });
    }

    public void after() throws Exception {
        try {
            runAll(beforeDestroyAfterRunnables);
            if (assembler != null) {
                stopApplication();
            }

            if (serviceManager != null) {
                try {
                    serviceManager.stop();
                } catch (final RuntimeException ignored) {
                    // no-op
                }
            }

            OpenEJB.destroy();
        } finally {
            runAll(afterRunnables);
            if (originalLoader != null) {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
            if (originalProperties != null) {
                System.setProperties(originalProperties);
            }
        }
    }

    public void stopApplication() throws NamingException {
        if (appContext != null && appContext.getWebBeansContext() != null) {
            final ContextsService contextsService = appContext.getWebBeansContext().getContextsService();
            // No need to stop the ConversationContext manually as it gets stored inside the SessionContext as Bean
            contextsService.endContext(SessionScoped.class, session);
            contextsService.endContext(RequestScoped.class, null);
        }

        if (appInfo != null) {
            try {
                assembler.destroyApplication(appInfo.path);
            } catch (final Exception e) {
                // no-op
            }
        }

        final ContainerSystem component = SystemInstance.get().getComponent(ContainerSystem.class);

        if (null != component) {
            final Context context = component.getJNDIContext();

            for (final String entry : globalJndiEntries) {
                context.unbind(entry);
            }
        }

        globalJndiEntries.clear();

        if (mockCdiContexts() && appContext != null && appContext.getWebBeansContext() != null) {
            try {
                ScopeHelper.stopContexts(appContext.getWebBeansContext().getContextsService(), servletContext, session);
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    private void runAll(final Collection<Runnable> runnables) {
        for (final Runnable r : runnables) {
            try {
                r.run();
            } catch (final Exception e) {
                // no-op
            }
        }
        runnables.clear();
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

    private static String implicitRootUrl(final PersistenceRootUrl annotation) {
        if (annotation != null) {
            return annotation.value();
        }
        final ResourceFinder finder = new ResourceFinder("", Thread.currentThread().getContextClassLoader());
        try {
            final URL url = DeploymentLoader.altDDSources(DeploymentLoader.mapDescriptors(finder), false).get("persistence.xml");
            if (url == null) {
                return "";
            }

            final File file = URLs.toFile(url);
            final String filename = file.getName();
            if (filename.endsWith("persistence.xml")) {
                final String parent = file.getParentFile().getName();
                if (parent.equalsIgnoreCase("META-INF")) {
                    return file.getParentFile().getParentFile().getAbsolutePath();
                }
                return file.getParentFile().getAbsolutePath();
            } else if (filename.endsWith(".jar")) {
                return file.toURI().toURL().toExternalForm();
            }
            return url.toExternalForm();
        } catch (final IOException e) {
            return "";
        }
    }

    private static Map<String, URL> descriptorsToMap(final Object descriptors) {
        if (descriptors != null) {
            final Map<String, URL> dds = new HashMap<>();
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (descriptors instanceof Descriptors) {
                for (final Descriptor descriptor : ((Descriptors) descriptors).value()) {
                    final URL resource = loader.getResource(descriptor.path());
                    try {
                        dds.put(descriptor.name(), resource == null ? new File(descriptor.path()).toURI().toURL() : resource);
                    } catch (final MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            } else {
                if (descriptors instanceof org.apache.openejb.junit.Descriptors) {
                    for (final org.apache.openejb.junit.Descriptor descriptor : ((org.apache.openejb.junit.Descriptors) descriptors).value()) {
                        final URL resource = loader.getResource(descriptor.path());
                        try {
                            dds.put(descriptor.name(), resource == null ? new File(descriptor.path()).toURI().toURL() : resource);
                        } catch (final MalformedURLException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
            }
            return dds;
        }
        return new HashMap<>();
    }

    private static IAnnotationFinder finderFromClasses(final DeploymentModule module, final Class<?>[] value, final Collection<File> others, final String[] excludes) {
        final Collection<Archive> archives = new ArrayList<>(1 + (others == null ? 0 : others.size()));

        final Filter filter = excludes == null || excludes.length == 0 ? null : Filters.invert(Filters.prefixes(excludes));

        final Collection<Class<?>> classes = new ArrayList<>(asList(FinderFactory.ensureMinimalClasses(module)));
        if (value != null) {
            classes.addAll(asList(value));
        }
        final ClassesArchive classesArchive = new ClassesArchive(classes);
        archives.add(filter == null ? classesArchive : new FilteredArchive(classesArchive, filter));

        if (others != null) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (final File f : others) {
                try {
                    final Archive archive = f.isDirectory() ? new FileArchive(classLoader, f) : new JarArchive(classLoader, f.toURI().toURL());
                    archives.add(filter == null ? archive : new FilteredArchive(archive, filter));
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

    public void startContainer(final Object instance) throws Exception {
        originalProperties = (Properties) JavaSecurityManagers.getSystemProperties().clone();
        originalLoader = Thread.currentThread().getContextClassLoader();
        fixFakeClassFinder(instance);

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
        final WebResource webResource = testClass.getAnnotation(WebResource.class);
        if (webResource != null && webResource.value().length > 0) {
            configuration.setProperty("openejb.embedded.http.resources", Join.join(",", webResource.value()));
        }

        Openejb openejb = null;
        final Map<Object, List<Method>> configs = new HashMap<>();
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

        Collection<String> propertiesToSetAgain = null;
        final ContainerProperties configAnnot = testClass.getAnnotation(ContainerProperties.class);
        if (configAnnot != null) {
            for (final ContainerProperties.Property p : configAnnot.value()) {
                final String value = p.value();
                if (ContainerProperties.Property.IGNORED.equals(value)) {
                    System.clearProperty(p.name()); // enforces some clean up since we can't set null in a hash table
                    continue;
                }
                final String name = p.name();
                configuration.put(name, value);
                if (value.contains("${")) {
                    if (propertiesToSetAgain == null) {
                        propertiesToSetAgain = new LinkedList<>();
                    }
                    propertiesToSetAgain.add(name);
                }
            }
        }

        SystemInstance.init(configuration);
        if (SystemInstance.get().getComponent(ThreadSingletonService.class) == null) {
            CdiBuilder.initializeOWB();
        }
        for (final Map.Entry<Object, ClassFinder> finder : testClassFinders.entrySet()) {
            for (final Field field : finder.getValue().findAnnotatedFields(RandomPort.class)) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                final String service = field.getAnnotation(RandomPort.class).value();
                final String key = ("http".equals(service) ? "httpejbd" : service) + ".port";
                final String existing = SystemInstance.get().getProperty(key);
                final int random;
                if (existing == null) {
                    random = NetworkUtil.getNextAvailablePort();
                    SystemInstance.get().setProperty(key, Integer.toString(random));
                } else {
                    random = Integer.parseInt(existing);
                }
                if (int.class == field.getType()) {
                    field.set(finder.getKey(), random);
                } else if (URL.class == field.getType()) {
                    field.set(finder.getKey(), new URL("http://localhost:" + random + "/"));
                }
            }
        }

        for (final Map.Entry<Object, ClassFinder> finder : testClassFinders.entrySet()) {
            if (!finder.getValue().findAnnotatedClasses(SimpleLog.class).isEmpty()) {
                SystemInstance.get().setProperty("openejb.jul.forceReload", "true");
                break;
            }
        }

        final CdiExtensions cdiExtensions = testClass.getAnnotation(CdiExtensions.class);
        if (cdiExtensions != null) {
            SystemInstance.get().setComponent(LoaderService.class, new ExtensionAwareOptimizedLoaderService(cdiExtensions.value()));
        }

        // save the test under test to be able to retrieve it from extensions
        // /!\ has to be done before all other init
        SystemInstance.get().setComponent(TestInstance.class, new TestInstance(testClass, instance));

        // call the mock injector before module method to be able to use mocked classes
        // it will often use the TestInstance so
        final Map<Object, List<Method>> mockInjectors = new HashMap<>();
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

        for (final Map.Entry<Object, List<Method>> method : findAnnotatedMethods(new HashMap<>(), Component.class).entrySet()) {
            for (final Method m : method.getValue()) {
                setComponent(method.getKey(), m);
            }
        }
        for (final Map.Entry<Object, List<Method>> method : findAnnotatedMethods(new HashMap<>(), org.apache.openejb.junit.Component.class).entrySet()) {
            for (final Method m : method.getValue()) {
                setComponent(method.getKey(), m);
            }
        }

        final ConfigurationFactory config = new ConfigurationFactory();
        config.init(SystemInstance.get().getProperties());
        SystemInstance.get().setComponent(ConfigurationFactory.class, config);

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
                    final List<String> value = new ArrayList<>(asList(annotation.value()));
                    if (annotation.jaxrs()) {
                        value.add("jaxrs");
                    }
                    if (annotation.jaxws()) {
                        value.add("jaxws");
                    }
                    initFilteredServiceManager(value.toArray(new String[value.size()]));
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

        if (propertiesToSetAgain != null) {
            for (final String name : propertiesToSetAgain) {
                final String value = PropertyPlaceHolderHelper.simpleValue(SystemInstance.get().getProperty(name));
                configuration.put(name, value);
                JavaSecurityManagers.setSystemProperty(name, value); // done lazily to support placeholders so container will not do it here
            }
            propertiesToSetAgain.clear();
        }
    }

    protected static class ExtensionAwareOptimizedLoaderService extends OptimizedLoaderService {
        private final Class<? extends Extension>[] extensions;

        protected ExtensionAwareOptimizedLoaderService(final Class<? extends Extension>[] extensions) {
            super(new Properties());
            this.extensions = extensions;
        }

        @Override
        protected List<? extends Extension> loadExtensions(final ClassLoader classLoader) {
            final List<Extension> list = new ArrayList<>();
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

    public static void run(final Class<?> type, final String... args) {
        final ApplicationComposers composer = new ApplicationComposers(type);
        try {
            Object instance;
            try {
                final Constructor<?> constructor = type.getConstructor(String[].class);
                instance = constructor.newInstance(new Object[]{args});
            } catch (final Exception e) {
                instance = type.newInstance();
            }
            composer.before(instance);
            composer.testClassFinders.remove(composer); // fix this workaround used for tests but breaking standalone mode

            final CountDownLatch latch = new CountDownLatch(1);

            final Thread hook = new Thread() {
                @Override
                public void run() {
                    try {
                        composer.after();
                    } catch (final Exception e) {
                        // no-op
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(hook);
            composer.afterRunnables.add(new Runnable() {
                @Override
                public void run() {
                    Runtime.getRuntime().removeShutdownHook(hook);
                    latch.countDown();
                }
            });

            // do it after having added the latch countdown hook to avoid to block if start and stop very fast
            composer.handleLifecycle(type, instance);

            latch.await();
        } catch (final InterruptedException ie) {
            Thread.interrupted();
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    public void handleLifecycle(final Class<?> type, final Object appInstance) throws IllegalAccessException, InvocationTargetException {
        beforeDestroyAfterRunnables.add(new Runnable() {
            @Override
            public void run() {
                for (final Map.Entry<Object, ClassFinder> m : testClassFinders.entrySet()) {
                    for (final Method mtd : m.getValue().findAnnotatedMethods(PreDestroy.class)) {
                        if (mtd.getParameterTypes().length == 0) {
                            if (!mtd.isAccessible()) {
                                mtd.setAccessible(true);
                            }
                            try {
                                mtd.invoke(mtd.getDeclaringClass() == type ? appInstance : m.getKey());
                            } catch (final IllegalAccessException | InvocationTargetException e) {
                                // no-op
                            }
                        }
                    }
                }
            }
        });
        if (!appContext.getWebContexts().isEmpty()) {
            beforeDestroyAfterRunnables.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Object sessionManager = SystemInstance.get().getComponent(
                                ParentClassLoaderFinder.Helper.get().loadClass("org.apache.openejb.server.httpd.session.SessionManager")
                        );
                        if (sessionManager != null) {
                            final Class<?>[] paramTypes = {WebContext.class};
                            for (final WebContext web : appContext.getWebContexts()) {
                                Reflections.invokeByReflection(sessionManager, "destroy", paramTypes, new Object[]{web});
                            }
                        }
                    } catch (final Throwable e) {
                        // no-op
                    }
                }
            });
        }
        for (final Map.Entry<Object, ClassFinder> m : testClassFinders.entrySet()) {
            for (final Method mtd : m.getValue().findAnnotatedMethods(PostConstruct.class)) {
                if (mtd.getParameterTypes().length == 0) {
                    if (!mtd.isAccessible()) {
                        mtd.setAccessible(true);
                    }
                    mtd.invoke(mtd.getDeclaringClass() == type ? appInstance : m.getKey());
                }
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("provide at least application class as parameter");
        }

        final Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(args[0]);

        final List<String> a = new ArrayList<>(asList(args));
        a.remove(0);
        run(c, a.toArray(new String[a.size()]));
    }
}
