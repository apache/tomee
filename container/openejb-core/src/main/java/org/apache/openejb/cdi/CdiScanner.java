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


package org.apache.openejb.cdi;

import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.BeansInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.cdi.transactional.MandatoryInterceptor;
import org.apache.openejb.cdi.transactional.NeverInterceptor;
import org.apache.openejb.cdi.transactional.NotSupportedInterceptor;
import org.apache.openejb.cdi.transactional.RequiredInterceptor;
import org.apache.openejb.cdi.transactional.RequiredNewInterceptor;
import org.apache.openejb.cdi.transactional.SupportsInterceptor;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.openejb.util.classloader.ClassLoaderComparator;
import org.apache.openejb.util.classloader.DefaultClassLoaderComparator;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.decorator.DecoratorsManager;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.inject.AlternativesManager;
import org.apache.webbeans.intercept.InterceptorsManager;
import org.apache.webbeans.spi.BDABeansXmlScanner;
import org.apache.webbeans.spi.BeanArchiveService;
import org.apache.webbeans.spi.ScannerService;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @version $Rev:$ $Date:$
 */
public class CdiScanner implements ScannerService {
    public static final String OPENEJB_CDI_FILTER_CLASSLOADER = "openejb.cdi.filter.classloader";

    private static final Class<?>[] TRANSACTIONAL_INTERCEPTORS = new Class<?>[]{
        MandatoryInterceptor.class, NeverInterceptor.class, NotSupportedInterceptor.class,
        RequiredInterceptor.class, RequiredNewInterceptor.class, SupportsInterceptor.class
    };

    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Class<?>> startupClasses = new HashSet<>();

    private WebBeansContext webBeansContext;

    public void setContext(final WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
    }

    @Override
    public void init(final Object object) {
        if (!StartupObject.class.isInstance (object)) {
            return;
        }

        final StartupObject startupObject = StartupObject.class.cast(object);
        final AppInfo appInfo = startupObject.getAppInfo();
        final ClassLoader classLoader = startupObject.getClassLoader();
        final ClassLoaderComparator comparator;
        if (classLoader instanceof ClassLoaderComparator) {
            comparator = (ClassLoaderComparator) classLoader;
        } else {
            comparator = new DefaultClassLoaderComparator(classLoader);
        }

        final WebBeansContext webBeansContext = startupObject.getWebBeansContext();
        final AlternativesManager alternativesManager = webBeansContext.getAlternativesManager();
        final DecoratorsManager decoratorsManager = webBeansContext.getDecoratorsManager();
        final InterceptorsManager interceptorsManager = webBeansContext.getInterceptorsManager();

        // "manual" extension to avoid to add it through SPI mecanism
        classes.addAll(asList(TRANSACTIONAL_INTERCEPTORS));
        for (final Class<?> interceptor : TRANSACTIONAL_INTERCEPTORS) {
            interceptorsManager.addEnabledInterceptorClass(interceptor);
        }

        // app beans
        for (final EjbJarInfo ejbJar : appInfo.ejbJars) {
            final BeansInfo beans = ejbJar.beans;

            if (beans == null || "false".equalsIgnoreCase(ejbJar.properties.getProperty("openejb.cdi.activated"))) {
                continue;
            }

            if (startupObject.isFromWebApp()) { // deploy only the related ejbmodule
                if (!ejbJar.moduleId.equals(startupObject.getWebContext().getId())) {
                    continue;
                }
            } else if (ejbJar.webapp && !appInfo.webAppAlone) {
                continue;
            }

            // fail fast
            final StringBuilder errors = new StringBuilder("You must not declare the same class multiple times in the beans.xml: ");
            if (addErrors(errors, "alternative classes", beans.duplicatedAlternativeClasses)
                || addErrors(errors, "alternative stereotypes", beans.duplicatedAlternativeStereotypes)
                || addErrors(errors, "decorators", beans.duplicatedDecorators)
                || addErrors(errors, "interceptors", beans.duplicatedInterceptors)) {
                throw new WebBeansConfigurationException(errors.toString());
            }
            // no more need of errors so clear them
            beans.duplicatedAlternativeStereotypes.clear();
            beans.duplicatedAlternativeClasses.clear();
            beans.duplicatedDecorators.clear();
            beans.duplicatedInterceptors.clear();

            for (final String className : beans.interceptors) {
                final Class<?> clazz = load(PropertyPlaceHolderHelper.simpleValue(className), classLoader);

                if (clazz != null) {
                    if (!interceptorsManager.isInterceptorClassEnabled(clazz)) {
                        interceptorsManager.addEnabledInterceptorClass(clazz);
                        classes.add(clazz);
                    } /* else { don't do it, check is done when we know the beans.xml path --> org.apache.openejb.config.DeploymentLoader.addBeansXmls
                        throw new WebBeansConfigurationException("Interceptor class : " + clazz.getName() + " is already defined");
                    }*/
                } else if (shouldThrowCouldNotLoadException(startupObject)) {
                    throw new WebBeansConfigurationException("Could not load interceptor class: " + className);
                }
            }

            for (final String className : beans.decorators) {
                final Class<?> clazz = load(PropertyPlaceHolderHelper.simpleValue(className), classLoader);
                if (clazz != null) {
                    if (!decoratorsManager.isDecoratorEnabled(clazz)) {
                        decoratorsManager.addEnabledDecorator(clazz);
                        classes.add(clazz);
                    } // same than interceptors regarding throw new WebBeansConfigurationException("Decorator class : " + clazz.getName() + " is already defined");
                } else if (shouldThrowCouldNotLoadException(startupObject)) {
                    throw new WebBeansConfigurationException("Could not load decorator class: " + className);
                }
            }


            for (final String className : beans.alternativeStereotypes) {
                final Class<?> clazz = load(PropertyPlaceHolderHelper.simpleValue(className), classLoader);
                if (clazz != null) {
                    alternativesManager.addXmlStereoTypeAlternative(clazz);
                    classes.add(clazz);
                } else if (shouldThrowCouldNotLoadException(startupObject)) {
                    throw new WebBeansConfigurationException("Could not load alternativeStereotype class: " + className);
                }
            }

            for (final String className : beans.alternativeClasses) {
                final Class<?> clazz = load(PropertyPlaceHolderHelper.simpleValue(className), classLoader);
                if (clazz != null) {
                    alternativesManager.addXmlClazzAlternative(clazz);
                    classes.add(clazz);
                } else if (shouldThrowCouldNotLoadException(startupObject)) {
                    throw new WebBeansConfigurationException("Could not load alternative class: " + className);
                }
            }

            // here for ears we need to skip classes in the parent classloader
            final ClassLoader scl = ClassLoader.getSystemClassLoader();
            final boolean filterByClassLoader = "true".equals(SystemInstance.get().getProperty(OPENEJB_CDI_FILTER_CLASSLOADER, "true"));

            final BeanArchiveService beanArchiveService = webBeansContext.getBeanArchiveService();
            final boolean openejb = OpenEJBBeanInfoService.class.isInstance(beanArchiveService);

            for (final BeansInfo.BDAInfo next : beans.bdas) {
                final BeanArchiveService.BeanArchiveInformation information;
                if (openejb) {
                    final OpenEJBBeanInfoService beanInfoService = OpenEJBBeanInfoService.class.cast(beanArchiveService);
                    information = beanInfoService.createBeanArchiveInformation(beans, classLoader, next.discoveryMode == null? "ALL" : next.discoveryMode); // this fallback is 100% for tests, TODO: get rid of it (AppComposer)
                    // TODO: log a warn is discoveryModes.get(key) == null
                    try {
                        beanInfoService.getBeanArchiveInfo().put(next.uri == null ? null : next.uri.toURL(), information);
                    } catch (final MalformedURLException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    try {
                        information = beanArchiveService.getBeanArchiveInformation(next.uri.toURL());
                    } catch (MalformedURLException e) {
                        throw new IllegalStateException(e);
                    }
                }

                final boolean scanModeAnnotated = BeanArchiveService.BeanDiscoveryMode.ANNOTATED.equals(information.getBeanDiscoveryMode());
                final boolean noScan = BeanArchiveService.BeanDiscoveryMode.NONE.equals(information.getBeanDiscoveryMode());
                final boolean isNotEarWebApp = startupObject.getWebContext() == null;

                if (!noScan) {
                    for (final String name : next.managedClasses) {
                        if (information.isClassExcluded(name)) {
                            continue;
                        }

                        final Class clazz = load(name, classLoader);
                        if (clazz == null) {
                            continue;
                        }

                        if (scanModeAnnotated) {
                            if (isBean(clazz)) {
                                classes.add(clazz);
                                if (beans.startupClasses.contains(name)) {
                                    startupClasses.add(clazz);
                                }
                            }
                        } else {
                            final ClassLoader loader = clazz.getClassLoader();
                            if (!filterByClassLoader || comparator.isSame(loader) || (loader.equals(scl) && isNotEarWebApp)) {
                                classes.add(clazz);
                                if (beans.startupClasses.contains(name)) {
                                    startupClasses.add(clazz);
                                }
                            }
                        }
                    }
                }
            }

            if (startupObject.getBeanContexts() != null) { // ensure ejbs are in managed beans otherwise they will not be deployed in CDI
                for (final BeanContext bc : startupObject.getBeanContexts()) {
                    final String name = bc.getBeanClass().getName();
                    if (BeanContext.Comp.class.getName().equals(name)) {
                        continue;
                    }

                    final Class<?> load = load(name, classLoader);
                    if (load != null && !classes.contains(load)) {
                        classes.add(load);
                    }
                }
            }
        }
    }

    // TODO: reusing our finder would be a good idea to avoid reflection we already did!
    private boolean isBean(final Class clazz) {
        try {
            for (final Annotation a : clazz.getAnnotations()) {
                final Class<? extends Annotation> annotationType = a.annotationType();
                if (webBeansContext.getBeanManagerImpl().isScope(annotationType)
                        || webBeansContext.getBeanManagerImpl().isStereotype(annotationType)) {
                    return true;
                }
            }
        }
        catch (final Throwable e) {
            // no-op
        }
        return false;
    }

    private static boolean shouldThrowCouldNotLoadException(final StartupObject startupObject) {
        final AppInfo appInfo = startupObject.getAppInfo();
        return appInfo.webAppAlone || appInfo.webApps.size() == 0 || startupObject.isFromWebApp();
    }

    private boolean addErrors(final StringBuilder errors, final String msg, final List<String> list) {
        if (!list.isEmpty()) {
            errors.append("[ ").append(msg).append(" --> ");
            for (final String s : list) {
                errors.append(s).append(" ");
            }
            errors.append("]");
            return true;
        } else {
            return false;
        }
    }

    public boolean isBDABeansXmlScanningEnabled() {
        return false;
    }

    public BDABeansXmlScanner getBDABeansXmlScanner() {
        return null;
    }

    /**
     * @param className   name of class to load
     * @param classLoader classloader to (try to) load it from
     * @return the loaded class if possible, or null if loading fails.
     */
    private Class load(final String className, final ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (final ClassNotFoundException e) {
            return null;
        } catch (final NoClassDefFoundError e) {
            return null;
        }
    }

    @Override
    public void scan() {
        // Unused
    }

    @Override
    public Set<URL> getBeanXmls() {
        return Collections.emptySet(); // Unused
    }

    @Override
    public Set<Class<?>> getBeanClasses() {
        return classes;
    }

    @Override
    public void release() {
        classes.clear();
    }

    public Set<Class<?>> getStartupClasses() {
        return startupClasses;
    }
}
