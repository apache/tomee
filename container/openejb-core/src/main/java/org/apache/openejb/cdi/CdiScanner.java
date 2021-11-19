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
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.classloader.ClassLoaderComparator;
import org.apache.openejb.util.classloader.DefaultClassLoaderComparator;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.intercept.InterceptorsManager;
import org.apache.webbeans.spi.BDABeansXmlScanner;
import org.apache.webbeans.spi.BdaScannerService;
import org.apache.webbeans.spi.BeanArchiveService;
import org.apache.webbeans.xml.DefaultBeanArchiveInformation;

import javax.decorator.Decorator;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @version $Rev:$ $Date:$
 */
public class CdiScanner implements BdaScannerService {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CDI, OpenEJBLifecycle.class);
    public static final String OPENEJB_CDI_FILTER_CLASSLOADER = "openejb.cdi.filter.classloader";

    private static final Class<?>[] TRANSACTIONAL_INTERCEPTORS = new Class<?>[]{
        MandatoryInterceptor.class, NeverInterceptor.class, NotSupportedInterceptor.class,
        RequiredInterceptor.class, RequiredNewInterceptor.class, SupportsInterceptor.class
    };

    private final Set<Class<?>> startupClasses = new HashSet<>();
    private final Set<URL> beansXml = new HashSet<>();
    private final boolean logDebug;

    private WebBeansContext webBeansContext;
    private ClassLoader containerLoader;

    /**
     * This BdaInfo is used for all manually added beans in this scanner.
     */
    private final DefaultBeanArchiveInformation tomeeBeanArchiveInformation;


    /**
     * for having proper scan mode 'SCOPED'/trim support we need to know which bean class
     * has which beans.xml.
     */
    private Map<BeanArchiveService.BeanArchiveInformation, Set<Class<?>>> beanClassesPerBda = new HashMap<>();


    public CdiScanner() {
        logDebug = "true".equals(SystemInstance.get().getProperty("openejb.cdi.noclassdeffound.log", "false"));

        tomeeBeanArchiveInformation = new DefaultBeanArchiveInformation("tomee");
        tomeeBeanArchiveInformation.setBeanDiscoveryMode(BeanArchiveService.BeanDiscoveryMode.ALL);

    }

    public void setContext(final WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
    }

    @Override
    public void init(final Object object) {
        if (!StartupObject.class.isInstance (object)) {
            return;
        }
        containerLoader = ParentClassLoaderFinder.Helper.get();

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
        final InterceptorsManager interceptorsManager = webBeansContext.getInterceptorsManager();

        // app beans
        for (final EjbJarInfo ejbJar : appInfo.ejbJars) {
            Set<Class<?>> classes = new HashSet<>();
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

            if (appInfo.webAppAlone || !ejbJar.webapp) {
                // "manual" extension to avoid to add it through SPI mecanism
                classes.addAll(asList(TRANSACTIONAL_INTERCEPTORS));
                for (final Class<?> interceptor : TRANSACTIONAL_INTERCEPTORS) {
                    interceptorsManager.addEnabledInterceptorClass(interceptor);
                }
            }

            // here for ears we need to skip classes in the parent classloader
            final ClassLoader scl = ClassLoader.getSystemClassLoader();
            final boolean filterByClassLoader = "true".equals(
                    ejbJar.properties.getProperty(OPENEJB_CDI_FILTER_CLASSLOADER,
                            SystemInstance.get().getProperty(OPENEJB_CDI_FILTER_CLASSLOADER, "true")));

            final BeanArchiveService beanArchiveService = webBeansContext.getBeanArchiveService();
            final boolean openejb = OpenEJBBeanInfoService.class.isInstance(beanArchiveService);

            final Map<BeansInfo.BDAInfo, BeanArchiveService.BeanArchiveInformation> infoByBda = new HashMap<>();
            for (final BeansInfo.BDAInfo bda : beans.bdas) {
/*                if (!startupObject.isFromWebApp() &&
                    ejbJar.webapp &&
                    !appInfo.webAppAlone &&
                    ejbJar.path != null &&
                    bda.uri.toString().contains(ejbJar.path)) {
                    continue;
                }*/

                if (bda.uri != null) {
                    try {
                        beansXml.add(bda.uri.toURL());
                    } catch (final MalformedURLException e) {
                        // no-op
                    }
                }
                infoByBda.put(bda, handleBda(startupObject, classLoader, comparator, ejbJar, scl, filterByClassLoader, beanArchiveService, openejb, bda));
            }
/*
            if (!startupObject.isFromWebApp() && ejbJar.webapp && !appInfo.webAppAlone) {
                continue;
            }*/
            
            for (final BeansInfo.BDAInfo bda : beans.noDescriptorBdas) {
                // infoByBda.put() not needed since we know it means annotated
                handleBda(startupObject, classLoader, comparator, ejbJar, scl, filterByClassLoader, beanArchiveService, openejb, bda);
            }

            if (startupObject.getBeanContexts() != null) {
                for (final BeanContext bc : startupObject.getBeanContexts()) {
                    final String name = bc.getBeanClass().getName();
                    if (BeanContext.Comp.class.getName().equals(name)) {
                        continue;
                    }

                    boolean cdi = false;
                    for (final BeansInfo.BDAInfo bda : beans.bdas) {
                        final BeanArchiveService.BeanArchiveInformation info = infoByBda.get(bda);
                        if (info.getBeanDiscoveryMode() == BeanArchiveService.BeanDiscoveryMode.NONE) {
                            continue;
                        }
                        if (bda.managedClasses.contains(name)) {
                            classes.add(load(name, classLoader));
                            cdi = true;
                            break;
                        }
                    }
                    if (!cdi) {
                        for (final BeansInfo.BDAInfo bda : beans.noDescriptorBdas) {
                            if (bda.managedClasses.contains(name)) {
                                classes.add(load(name, classLoader));
                                break;
                            }
                        }
                    }
                }
            }

            if ("true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.cdi.debug", "false"))) {
                final Logger logger =  Logger.getInstance(LogCategory.OPENEJB, CdiScanner.class.getName());
                logger.info("CDI beans for " + startupObject.getAppInfo().appId + (startupObject.getWebContext() != null ? " webcontext = " + startupObject.getWebContext().getContextRoot() : ""));
                final List<String> names = new ArrayList<>(classes.size());
                for (final Class<?> c : classes) {
                    names.add(c.getName());
                }
                Collections.sort(names);
                for (final String c : names) {
                    logger.info("    " + c);
                }
            }

           if (!classes.isEmpty()) {
                addClasses(tomeeBeanArchiveInformation, classes);
           }
        }
    }

    private void addClasses(BeanArchiveService.BeanArchiveInformation bdaInfo, final Collection<String> list, final ClassLoader loader) {
        Set<Class<?>> classes = beanClassesPerBda.computeIfAbsent(bdaInfo, k -> new HashSet<>());

        for (final String s : list) {
            final Class<?> load = load(s, loader);
            if (load != null) {
                classes.add(load);
            }
        }
    }
    private void addClasses(BeanArchiveService.BeanArchiveInformation bdaInfo, final Collection<Class<?>> list) {
        Set<Class<?>> classes = beanClassesPerBda.computeIfAbsent(bdaInfo, k -> new HashSet<>());

        classes.addAll(list);
    }

    @Override
    public Map<BeanArchiveService.BeanArchiveInformation, Set<Class<?>>> getBeanClassesPerBda() {
        return beanClassesPerBda;
    }

    private BeanArchiveService.BeanArchiveInformation handleBda(final StartupObject startupObject, final ClassLoader classLoader, final ClassLoaderComparator comparator,
                                                                final EjbJarInfo ejbJarInfo, final ClassLoader scl, final boolean filterByClassLoader,
                                                                final BeanArchiveService beanArchiveService, final boolean openejb,
                                                                final BeansInfo.BDAInfo bda) {
        BeanArchiveService.BeanArchiveInformation information;
        if (openejb) {
            final OpenEJBBeanInfoService beanInfoService = OpenEJBBeanInfoService.class.cast(beanArchiveService);
            information = beanInfoService.createBeanArchiveInformation(bda, ejbJarInfo.beans, classLoader);
            // TODO: log a warn is discoveryModes.get(key) == null
            try {
                beanInfoService.getBeanArchiveInfo().put(bda.uri == null ? null : bda.uri.toURL(), information);
            } catch (final MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        } else {
            try {
                information = beanArchiveService.getBeanArchiveInformation(bda.uri.toURL());
            } catch (final MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }
        addClasses(information, information.getAlternativeClasses(), classLoader);
        addClasses(information, information.getDecorators(), classLoader);
        addClasses(information, information.getInterceptors(), classLoader);
        addClasses(information, information.getAlternativeStereotypes(), classLoader);

        final boolean scanModeAnnotated = BeanArchiveService.BeanDiscoveryMode.ANNOTATED.equals(information.getBeanDiscoveryMode());
        final boolean noScan = BeanArchiveService.BeanDiscoveryMode.NONE.equals(information.getBeanDiscoveryMode());
        final boolean isNotEarWebApp = startupObject.getWebContext() == null;

        if (!noScan) {
            if (scanModeAnnotated) {
                try {
                    Logger.getInstance(LogCategory.OPENEJB, CdiScanner.class.getName())
                            .info("Using annotated mode for " + bda.uri.toASCIIString()
                                    + " looking all classes to find CDI beans, maybe think to add a beans.xml if not there or "
                                    + "add the jar to exclusions.list");
                } catch (final Exception ex) {
                    // no-op: not a big deal
                }
            }

            Set<Class<?>> classes = new HashSet<>(bda.managedClasses.size());
            for (final String name : bda.managedClasses) {
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
                        if (ejbJarInfo.beans.startupClasses.contains(name)) {
                            logger.debug("Adding class " + clazz.getName()
                                    + " from " + getLocation(clazz) + ", in module " + ejbJarInfo.moduleId
                                    + " to startup list. Scan mode="
                                    + information.getBeanDiscoveryMode().toString() + ". EAR webapp=" + !isNotEarWebApp);

                            startupClasses.add(clazz);
                        }
                    }
                } else {
                    final ClassLoader loader = clazz.getClassLoader();
                    // main case it tries to filter is ear one ie lib classes shouldn't be in webapp classes
                    // but embedded case should still work
                    if (!filterByClassLoader
                            || comparator.isSame(loader)
                            || ((loader.equals(scl) || loader == containerLoader) && isNotEarWebApp)) {
                        classes.add(clazz);
                        if (ejbJarInfo.beans.startupClasses.contains(name)) {
                            logger.debug("Adding class " + clazz.getName()
                                    + " from " + getLocation(clazz) + ", in module " + ejbJarInfo.moduleId
                                    + " to startup list. Scan mode=" + information.getBeanDiscoveryMode().toString()
                                    + ". EAR webapp=" + !isNotEarWebApp);
                            
                            startupClasses.add(clazz);
                        }
                    }
                }
            }

            addClasses(information, classes);
        }

        return information;
    }

    // TODO: reusing our finder would be a good idea to avoid reflection we already did!
    private boolean isBean(final Class clazz) {
        try {
            for (final Annotation a : clazz.getAnnotations()) {
                final Class<? extends Annotation> annotationType = a.annotationType();
                final BeanManagerImpl beanManager = webBeansContext.getBeanManagerImpl();
                if (beanManager.isScope(annotationType)
                        || beanManager.isStereotype(annotationType)
                        || beanManager.isInterceptorBinding(annotationType)
                        || Decorator.class == a.annotationType()) {
                    return true;
                }
            }
        }
        catch (final Throwable e) {
            // no-op
        }
        return false;
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
            final Class<?> loadClass = classLoader.loadClass(className);
            tryToMakeItFail(loadClass);
            return loadClass;
        } catch (final ClassNotFoundException e) {
            return null;
        } catch (final NoClassDefFoundError e) {
            if (logDebug) {
                Logger.getInstance(LogCategory.OPENEJB_CDI, CdiScanner.class).warning(className + " -> " + e);
            }
            return null;
        }
    }

    private void tryToMakeItFail(final Class<?> loadClass) { // we try to avoid later NoClassDefFoundError
        loadClass.getDeclaredFields();
        loadClass.getDeclaredMethods();
    }

    @Override
    public void scan() {
        // Unused
    }

    @Override
    public Set<URL> getBeanXmls() {
        return beansXml;
    }

    @Override
    public Set<Class<?>> getBeanClasses() {
        return Collections.EMPTY_SET;
    }

    @Override
    public void release() {
        beanClassesPerBda.clear();
    }

    public Set<Class<?>> getStartupClasses() {
        return startupClasses;
    }

    public static String getLocation(final Class<?> clazz) {

        if (clazz != null
                && clazz.getProtectionDomain() != null
                && clazz.getProtectionDomain().getCodeSource() != null
                && clazz.getProtectionDomain().getCodeSource().getLocation() != null) {

            return clazz.getProtectionDomain().getCodeSource().getLocation().toString();
        }

        return "<not available>";
    }
}
