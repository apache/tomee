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

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.interceptor.Interceptor;

import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.BeansInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.core.WebContext;
import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.decorator.DecoratorsManager;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.inject.AlternativesManager;
import org.apache.webbeans.intercept.InterceptorsManager;
import org.apache.webbeans.spi.BDABeansXmlScanner;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.util.AnnotationUtil;

/**
 * @version $Rev:$ $Date:$
 */
public class CdiScanner implements ScannerService {

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

        WebBeansContext webBeansContext = startupObject.getAppContext().getWebBeansContext();
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

            for (String className : beans.interceptors) {
                Class<?> clazz = load(className, classLoader);

                if (clazz != null) {
// TODO: Move check to validation phase
                    if (AnnotationUtil.hasAnnotation(clazz.getDeclaredAnnotations(), Interceptor.class) && !annotationManager.hasInterceptorBindingMetaAnnotation(
                        clazz.getDeclaredAnnotations())) {
                        throw new WebBeansConfigurationException("Interceptor class : " + clazz.getName() + " must have at least one @InterceptorBindingType");
                    }

                    if (interceptorsManager.isInterceptorEnabled(clazz)) {
                        throw new WebBeansConfigurationException("Interceptor class : " + clazz.getName() + " is already defined");
                    }

                    interceptorsManager.addNewInterceptor(clazz);
                    classes.add(clazz);
                } else {
                    throw new WebBeansConfigurationException("Could not load interceptor class: " + className);
                }
            }

            for (String className : beans.decorators) {
                Class<?> clazz = load(className, classLoader);

                if (clazz != null) {
                    if (decoratorsManager.isDecoratorEnabled(clazz)) {
                        throw new WebBeansConfigurationException("Decorator class : " + clazz.getName() + " is already defined");
                    }

                    decoratorsManager.addNewDecorator(clazz);
                    classes.add(clazz);
                } else {
                    throw new WebBeansConfigurationException("Could not load decorator class: " + className);
                }
            }


            for (String className : beans.alternativeStereotypes) {
                Class<?> clazz = load(className, classLoader);
                if (clazz != null) {
                    alternativesManager.addStereoTypeAlternative(clazz, null, null);
                    classes.add(clazz);
                } else {
                    throw new WebBeansConfigurationException("Could not load alternativeStereotype class: " + className);
                }
            }

            for (String className : beans.alternativeClasses) {
                Class<?> clazz = load(className, classLoader);
                if (clazz != null) {
                    alternativesManager.addClazzAlternative(clazz, null, null);
                    classes.add(clazz);
                } else {
                    throw new WebBeansConfigurationException("Could not load alternative class: " + className);
                }
            }

            for (String className : beans.managedClasses) {
                if (ejbClasses.contains(className)) continue;
                final Class clazz = load(className, classLoader);
                if (clazz != null) { // can happen in standalone
                    classes.add(clazz);
                }
            }
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
