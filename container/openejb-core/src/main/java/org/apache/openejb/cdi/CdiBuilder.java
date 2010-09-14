/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.BeansInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.decorator.DecoratorsManager;
import org.apache.webbeans.ejb.common.util.EjbUtility;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.inject.AlternativesManager;
import org.apache.webbeans.intercept.InterceptorData;
import org.apache.webbeans.intercept.InterceptorsManager;
import org.apache.webbeans.plugins.PluginLoader;
import org.apache.webbeans.portable.AnnotatedElementFactory;
import org.apache.webbeans.portable.events.ExtensionLoader;
import org.apache.webbeans.portable.events.ProcessAnnotatedTypeImpl;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.WebBeansUtil;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.interceptor.Interceptor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class CdiBuilder {

    private final AppInfo appInfo;
    private final AppContext appContext;
    private ClassLoader classLoader;

    public CdiBuilder(AppInfo appInfo, AppContext appContext) {
        this.appInfo = appInfo;
        this.appContext = appContext;
        classLoader = appContext.getClassLoader();
    }

    private static void deployManagedBeans(Set<Class<?>> beanClasses) {

        // Start from the class
        for (Class<?> implClass : beanClasses) {
            //Define annotation type
            AnnotatedType<?> annotatedType = AnnotatedElementFactory.getInstance().newAnnotatedType(implClass);

            //Fires ProcessAnnotatedType
            ProcessAnnotatedTypeImpl<?> processAnnotatedEvent = WebBeansUtil.fireProcessAnnotatedTypeEvent(annotatedType);

            //if veto() is called
            if (processAnnotatedEvent.isVeto()) {
                continue;
            }

            BeansDeployer.defineManagedBean((Class<Object>) implClass, (ProcessAnnotatedTypeImpl<Object>) processAnnotatedEvent);
        }
    }

    public void build(List<BeanContext> ejbDeployments) throws OpenEJBException {

        long startTime = System.currentTimeMillis();
        final ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            //Set classloader
            Thread.currentThread().setContextClassLoader(classLoader);

            WebBeansFinder.clearInstances(classLoader);

            BeanManagerImpl beanManager = (BeanManagerImpl) WebBeansFinder.getSingletonInstance(BeanManagerImpl.class.getName(), classLoader);
            OpenWebBeansConfiguration owb = OpenWebBeansConfiguration.getInstance();
            owb.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
            appContext.setBeanManager(beanManager);

            //Configure our scanner service

            //Start our plugins
            PluginLoader.getInstance().startUp();

            //Get Plugin
            CdiPlugin cdiPlugin = (CdiPlugin) PluginLoader.getInstance().getEjbPlugin();

            cdiPlugin.setAppContext(appContext);

            cdiPlugin.startup();

            //Configure EJB Deployments
            cdiPlugin.configureDeployments(ejbDeployments);

            //Resournce Injection Service
            CdiResourceInjectionService injectionService = (CdiResourceInjectionService) WebBeansFinder.getSingletonInstance(CdiResourceInjectionService.class.getName(), classLoader);
            injectionService.setAppModule(appInfo);
            injectionService.setClassLoader(classLoader);

            //Deploy the beans
            try {
                //Load Extensions
                ExtensionLoader.getInstance().loadExtensionServices();

                // Register Manager built-in component
                final BeanManagerImpl manager = BeanManagerImpl.getManager();

                manager.addBean(WebBeansUtil.getManagerBean());

                //Fire Event
                BeansDeployer.fireBeforeBeanDiscoveryEvent(manager);

                //Deploy bean from XML. Also configures deployments, interceptors, decorators.

                final CdiScanner cdiScanner = buildScanner();

                //Build injections for managed beans
                // TODO Maybe we should build injections after the bean discovery
                injectionService.buildInjections(cdiScanner.getBeanClasses());

                //Checking stereotype conditions
                BeansDeployer.checkStereoTypes(cdiScanner);

                //Configure Default Beans
                BeansDeployer.configureDefaultBeans();

                //Discover classpath classes
                deployManagedBeans(cdiScanner.getBeanClasses());

                for (BeanContext beanContext : ejbDeployments) {
                    if (!beanContext.getComponentType().isSession()) continue;

                    final Class implClass = beanContext.getBeanClass();

                    //Define annotation type
                    AnnotatedType<?> annotatedType = AnnotatedElementFactory.getInstance().newAnnotatedType(implClass);

                    //Fires ProcessAnnotatedType
                    ProcessAnnotatedTypeImpl<?> processAnnotatedEvent = WebBeansUtil.fireProcessAnnotatedTypeEvent(annotatedType);

                    // TODO Can you really veto an EJB?
                    //if veto() is called
                    if (processAnnotatedEvent.isVeto()) {
                        continue;
                    }

                    CdiEjbBean<Object> bean = new CdiEjbBean<Object>(beanContext);

                    beanContext.set(CdiEjbBean.class, bean);
                    
                    beanContext.addSystemInterceptor(new CdiInterceptor(bean, manager, cdiPlugin.getContexsServices()));

                    EjbUtility.fireEvents((Class<Object>) implClass, bean, (ProcessAnnotatedTypeImpl<Object>) processAnnotatedEvent);

                    WebBeansUtil.setInjectionTargetBeanEnableFlag(bean);
                }

                //Check Specialization
                BeansDeployer.checkSpecializations(cdiScanner);

                //Fire Event
                BeansDeployer.fireAfterBeanDiscoveryEvent(manager);

                //Validate injection Points
                BeansDeployer.validateInjectionPoints(manager);

                for (BeanContext beanContext : ejbDeployments) {
                    if (!beanContext.getComponentType().isSession()) continue;
                    final CdiEjbBean bean = beanContext.get(CdiEjbBean.class);

                    // The interceptor stack is empty until validateInjectionPoints is called as it does more than validate. 
                    final List<InterceptorData> datas = bean.getInterceptorStack();

                    final List<org.apache.openejb.core.interceptor.InterceptorData> converted = new ArrayList<org.apache.openejb.core.interceptor.InterceptorData>();
                    for (InterceptorData data : datas) {
                        // todo this needs to use the code in InterceptorBindingBuilder that respects override rules and private methods
                        converted.add(org.apache.openejb.core.interceptor.InterceptorData.scan(data.getInterceptorClass()));
                    }

                    beanContext.setCdiInterceptors(converted);
                }

                //Fire Event
                BeansDeployer.fireAfterDeploymentValidationEvent(manager);
            } catch (Exception e1) {
                Assembler.logger.error("CDI Beans module deployment failed", e1);
                throw new RuntimeException(e1);
            }

            if (Assembler.logger.isInfoEnabled()) {
                Assembler.logger.info("CDI Beans module deployed in " + (System.currentTimeMillis() - startTime) + " ms");
            }

        } catch (Exception e) {
            String errorMessage = "Error is occurred while starting the CDI container, looks error log for further investigation";
            throw new OpenEJBException(errorMessage, e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    private CdiScanner buildScanner() throws OpenEJBException {

        final Set<Class<?>> classes = new HashSet<Class<?>>();

        final AlternativesManager alternativesManager = AlternativesManager.getInstance();
        final DecoratorsManager decoratorsManager = DecoratorsManager.getInstance();
        final InterceptorsManager interceptorsManager = InterceptorsManager.getInstance();

        final HashSet<String> ejbClasses = new HashSet<String>();

        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            for (EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                ejbClasses.add(bean.ejbClass);
            }
        }

        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            final BeansInfo beans = ejbJar.beans;

            if (beans == null) continue;

            for (String className : beans.interceptors) {
                Class<?> clazz = load(className, "interceptor");

                // TODO: Move check to validation phase
                if (AnnotationUtil.hasAnnotation(clazz.getDeclaredAnnotations(), Interceptor.class) && !AnnotationUtil.hasInterceptorBindingMetaAnnotation(clazz.getDeclaredAnnotations())) {
                    throw new WebBeansConfigurationException("Interceptor class : " + clazz.getName() + " must have at least one @InterceptorBindingType");
                }

                if (interceptorsManager.isInterceptorEnabled(clazz)) {
                    throw new WebBeansConfigurationException("Interceptor class : " + clazz.getName() + " is already defined");
                }

                interceptorsManager.addNewInterceptor(clazz);
                classes.add(clazz);
            }

            for (String className : beans.decorators) {
                Class<?> clazz = load(className, "decorator");

                if (decoratorsManager.isDecoratorEnabled(clazz)) {
                    throw new WebBeansConfigurationException("Decorator class : " + clazz.getName() + " is already defined");
                }

                decoratorsManager.addNewDecorator(clazz);
                classes.add(clazz);
            }


            for (String className : beans.alternativeStereotypes) {
                Class<?> clazz = load(className, "alternative-stereotype");
                alternativesManager.addStereoTypeAlternative(clazz);
                classes.add(clazz);
            }

            for (String className : beans.alternativeClasses) {
                Class<?> clazz = load(className, "alternative-class");
                alternativesManager.addClazzAlternative(clazz);
                classes.add(clazz);
            }

            for (String className : beans.managedClasses) {
                if (ejbClasses.contains(className)) continue;
                final Class clazz = load(className, "managed");
                classes.add(clazz);
            }
        }

        return new CdiScanner(classes);
    }

    private Class load(String className, String type) throws OpenEJBException {
        System.out.println("cdi.load = " + className);
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new OpenEJBException("Unable to load " + type + " class", e);
        }
    }

    public static class CdiScanner implements ScannerService {

        // TODO add all annotated class
        private final Set<Class<?>> classes = new HashSet<Class<?>>();

        public CdiScanner(Collection<Class<?>> classes) {
            this.classes.addAll(classes);
        }

        @Override
        public void init(Object object) {
            // Unused
        }

        @Override
        public void scan() {
            // Unused
        }

        @Override
        public Set<URL> getBeanXmls() {
            return Collections.EMPTY_SET; // Unused
        }

        @Override
        public Set<Class<?>> getBeanClasses() {
            return classes;
        }
    }
}
