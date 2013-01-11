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

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.BeansInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.classloader.ClassLoaderComparator;
import org.apache.openejb.util.classloader.DefaultClassLoaderComparator;
import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.decorator.DecoratorsManager;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.inject.AlternativesManager;
import org.apache.webbeans.intercept.InterceptorsManager;
import org.apache.webbeans.spi.BDABeansXmlScanner;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.util.AnnotationUtil;

import javax.interceptor.Interceptor;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev:$ $Date:$
 */
public class CdiScanner implements ScannerService {
    public static final String OPENEJB_CDI_FILTER_CLASSLOADER = "openejb.cdi.filter.classloader";
    public static final ThreadLocal<Collection<String>> ADDITIONAL_CLASSES = new ThreadLocal<Collection<String>>();

    // TODO add all annotated class
    private final Set<Class<?>> classes = new HashSet<Class<?>>();

    @Override
    public Set<String> getAllAnnotations(String className) {
        return Collections.emptySet();
    }

    @Override
    public void init(Object object) {
        if (!(object instanceof StartupObject)) {
            return;
        }
        StartupObject startupObject = (StartupObject) object;
        AppInfo appInfo = startupObject.getAppInfo();
        ClassLoader classLoader = startupObject.getClassLoader();
        final ClassLoaderComparator comparator;
        if (classLoader instanceof  ClassLoaderComparator) {
            comparator = (ClassLoaderComparator) classLoader;
        } else {
            comparator = new DefaultClassLoaderComparator(classLoader);
        }

        final WebBeansContext webBeansContext = startupObject.getWebBeansContext();
        final AlternativesManager alternativesManager = webBeansContext.getAlternativesManager();
        final DecoratorsManager decoratorsManager = webBeansContext.getDecoratorsManager();
        final InterceptorsManager interceptorsManager = webBeansContext.getInterceptorsManager();

        final HashSet<String> ejbClasses = new HashSet<String>();

        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            for (EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                ejbClasses.add(bean.ejbClass);
            }
        }

        final AnnotationManager annotationManager = webBeansContext.getAnnotationManager();

        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            final BeansInfo beans = ejbJar.beans;

            if (beans == null) continue;

            if (startupObject.isFromWebApp()) { // deploy only the related ejbmodule
                if (!ejbJar.moduleId.equals(startupObject.getWebContext().getId())) {
                    continue;
                }
            } else if (ejbJar.webapp && !appInfo.webAppAlone) {
                continue;
            }

            // fail fast
            final StringBuilder errors = new StringBuilder("You can't define multiple times the same class in beans.xml: ");
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

            for (String className : beans.interceptors) {
                Class<?> clazz = load(className, classLoader);

                if (clazz != null) {
// TODO: Move check to validation phase
                    if (AnnotationUtil.hasAnnotation(clazz.getDeclaredAnnotations(), Interceptor.class) && !annotationManager.hasInterceptorBindingMetaAnnotation(
                        clazz.getDeclaredAnnotations())) {
                        throw new WebBeansConfigurationException("Interceptor class : " + clazz.getName() + " must have at least one @InterceptorBindingType");
                    }

                    if (!interceptorsManager.isInterceptorEnabled(clazz)) {
                        interceptorsManager.addNewInterceptor(clazz);
                        classes.add(clazz);
                    } /* else { don't do it, check is done when we know the beans.xml path --> org.apache.openejb.config.DeploymentLoader.addBeansXmls
                        throw new WebBeansConfigurationException("Interceptor class : " + clazz.getName() + " is already defined");
                    }*/
                } else if (shouldThrowCouldNotLoadException(startupObject)) {
                    throw new WebBeansConfigurationException("Could not load interceptor class: " + className);
                }
            }

            for (String className : beans.decorators) {
                Class<?> clazz = load(className, classLoader);

                if (clazz != null) {
                    if (!decoratorsManager.isDecoratorEnabled(clazz)) {
                        decoratorsManager.addNewDecorator(clazz);
                        classes.add(clazz);
                    } // same than interceptors regarding throw new WebBeansConfigurationException("Decorator class : " + clazz.getName() + " is already defined");
                } else if (shouldThrowCouldNotLoadException(startupObject)) {
                    throw new WebBeansConfigurationException("Could not load decorator class: " + className);
                }
            }


            for (String className : beans.alternativeStereotypes) {
                Class<?> clazz = load(className, classLoader);
                if (clazz != null) {
                    alternativesManager.addStereoTypeAlternative(clazz, null, null);
                    classes.add(clazz);
                } else if (shouldThrowCouldNotLoadException(startupObject)) {
                    throw new WebBeansConfigurationException("Could not load alternativeStereotype class: " + className);
                }
            }

            for (String className : beans.alternativeClasses) {
                Class<?> clazz = load(className, classLoader);
                if (clazz != null) {
                    alternativesManager.addClazzAlternative(clazz, null, null);
                    classes.add(clazz);
                } else if (shouldThrowCouldNotLoadException(startupObject)) {
                    throw new WebBeansConfigurationException("Could not load alternative class: " + className);
                }
            }

            // here for ears we need to skip classes in the parent classloader
            final ClassLoader scl = ClassLoader.getSystemClassLoader();
            final boolean filterByClassLoader = "true".equals(SystemInstance.get().getProperty(OPENEJB_CDI_FILTER_CLASSLOADER, "true"));

            final Iterator<String> it = beans.managedClasses.iterator();
            while (it.hasNext()) {
                process(classLoader, ejbClasses, it, startupObject, comparator, scl, filterByClassLoader);
            }

            final Collection<String> otherClasses = ADDITIONAL_CLASSES.get();
            if (otherClasses != null) {
                final Iterator<String> it2 = otherClasses.iterator();
                while (it2.hasNext()) {
                    process(classLoader, ejbClasses, it2, startupObject, comparator, scl, filterByClassLoader);
                }
            }
        }

    }

    private static boolean shouldThrowCouldNotLoadException(final StartupObject startupObject) {
        final AppInfo appInfo = startupObject.getAppInfo();
        return appInfo.webAppAlone || appInfo.webApps.size() == 0 || startupObject.isFromWebApp();
    }

    private void process(final ClassLoader classLoader, final Set<String> ejbClasses, final Iterator<String> it, final StartupObject startupObject, final ClassLoaderComparator comparator, final ClassLoader scl, final boolean filterByClassLoader) {
        final String className = it.next();
        if (ejbClasses.contains(className)) it.remove();
        final Class clazz = load(className, classLoader);
        if (clazz == null) {
            return;
        }

        final ClassLoader cl = clazz.getClassLoader();
        // 1. this classloader is the good one
        // 2. the classloader is the appclassloader one and we are in the ear parent
        if (!filterByClassLoader
                || comparator.isSame(cl) || (cl.equals(scl) && startupObject.getWebContext() == null)) {
            classes.add(clazz);
        } else {
            it.remove();
        }
    }

    private boolean addErrors(final StringBuilder errors, final String msg, final List<String> list) {
        if (!list.isEmpty()) {
            errors.append("[ ").append(msg).append(" --> ");
            for (String s : list) {
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
     *
     * @param className  name of class to load
     * @param classLoader classloader to (try to) load it from
     * @return the loaded class if possible, or null if loading fails.
     */
    private Class load(String className, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoClassDefFoundError e) {
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
}
