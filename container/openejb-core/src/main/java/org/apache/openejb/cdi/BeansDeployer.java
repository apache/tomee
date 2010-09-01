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

import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.AbstractProducerBean;
import org.apache.webbeans.component.EnterpriseBeanMarker;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.component.InjectionTargetWrapper;
import org.apache.webbeans.component.InterceptedMarker;
import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.component.NewBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.component.creation.BeanCreator;
import org.apache.webbeans.component.creation.ManagedBeanCreatorImpl;
import org.apache.webbeans.config.DefinitionUtil;
import org.apache.webbeans.config.ManagedBeanConfigurator;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.decorator.DecoratorsManager;
import org.apache.webbeans.decorator.WebBeansDecorator;
import org.apache.webbeans.deployment.StereoTypeManager;
import org.apache.webbeans.deployment.StereoTypeModel;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansDeploymentException;
import org.apache.webbeans.exception.inject.InconsistentSpecializationException;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.intercept.InterceptorsManager;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;
import org.apache.webbeans.logger.WebBeansLogger;
import org.apache.webbeans.plugins.PluginLoader;
import org.apache.webbeans.portable.events.ProcessAnnotatedTypeImpl;
import org.apache.webbeans.portable.events.ProcessInjectionTargetImpl;
import org.apache.webbeans.portable.events.discovery.AfterBeanDiscoveryImpl;
import org.apache.webbeans.portable.events.discovery.AfterDeploymentValidationImpl;
import org.apache.webbeans.portable.events.discovery.BeforeBeanDiscoveryImpl;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansWebPlugin;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.WebBeansAnnotatedTypeUtil;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;
import org.apache.webbeans.xml.XMLAnnotationTypeManager;
import org.apache.webbeans.xml.XMLSpecializesManager;

import javax.enterprise.inject.Model;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.interceptor.Interceptor;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * @version $Rev$ $Date$
 */

/**
 * Deploys the all beans that are defined in the {@link org.apache.webbeans.spi.ScannerService} at
 * the scanner phase.
 */
@SuppressWarnings("unchecked")
//This class written as single threaded.
public class BeansDeployer {
    //Logger instance
    private static final WebBeansLogger logger = WebBeansLogger.getLogger(BeansDeployer.class);

    /**
     * Configure Default Beans.
     */
    public static void configureDefaultBeans() {
        BeanManagerImpl beanManager = BeanManagerImpl.getManager();

        // Register Conversation built-in component
        beanManager.addBean(WebBeansUtil.getConversationBean());

        // Register InjectionPoint bean
        beanManager.addBean(WebBeansUtil.getInjectionPointBean());

        //Register Instance Bean
        beanManager.addBean(WebBeansUtil.getInstanceBean());

        //Register Event Bean
        beanManager.addBean(WebBeansUtil.getEventBean());

        //REgister Provider Beans
        OpenWebBeansJavaEEPlugin beanEeProvider = PluginLoader.getInstance().getJavaEEPlugin();
        OpenWebBeansWebPlugin beanWebProvider = PluginLoader.getInstance().getWebPlugin();

        if (beanEeProvider != null) {
            addDefaultBean(beanManager, "org.apache.webbeans.ee.common.beans.PrinicipalBean");
            addDefaultBean(beanManager, "org.apache.webbeans.ee.beans.ValidatorBean");
            addDefaultBean(beanManager, "org.apache.webbeans.ee.beans.ValidatorFactoryBean");
            addDefaultBean(beanManager, "org.apache.webbeans.ee.beans.UserTransactionBean");
        } else if (beanWebProvider != null) {
            addDefaultBean(beanManager, "org.apache.webbeans.ee.common.beans.PrinicipalBean");
        }

    }

    public static void addDefaultBean(BeanManagerImpl manager, String className) {
        Bean<?> bean = null;

        Class<?> beanClass = ClassUtil.getClassFromName(className);
        if (beanClass != null) {
            bean = (Bean) ClassUtil.newInstance(beanClass);
        }

        if (bean != null) {
            manager.addBean(bean);
        }
    }

    /**
     * Fires event before bean discovery.
     * @param manager
     */
    public static void fireBeforeBeanDiscoveryEvent(BeanManager manager) {
        manager.fireEvent(new BeforeBeanDiscoveryImpl(), new Annotation[0]);
    }

    /**
     * Fires event after bean discovery.
     * @param manager
     */
    public static void fireAfterBeanDiscoveryEvent(BeanManagerImpl manager) {
        manager.fireEvent(new AfterBeanDiscoveryImpl(), new Annotation[0]);

        WebBeansUtil.inspectErrorStack("There are errors that are added by AfterBeanDiscovery event observers. Look at logs for further details");
    }

    /**
     * Fires event after deployment valdiation.
     * @param manager
     */
    public static void fireAfterDeploymentValidationEvent(BeanManagerImpl manager) {
        manager.fireEvent(new AfterDeploymentValidationImpl(), new Annotation[0]);

        WebBeansUtil.inspectErrorStack("There are errors that are added by AfterDeploymentValidation event observers. Look at logs for further details");
    }

    /**
     * Validate all injection points.
     * @param manager
     */
    public static void validateInjectionPoints(BeanManagerImpl manager) {
        logger.debug("Validation of injection points has started.");

        DecoratorsManager.getInstance().validateDecoratorClasses();
        InterceptorsManager.getInstance().validateInterceptorClasses();

        Set<Bean<?>> beans = new HashSet<Bean<?>>();

        //Adding decorators to validate
        Set<Decorator<?>> decorators = manager.getDecorators();
        for (Decorator decorator : decorators) {
            WebBeansDecorator wbDec = (WebBeansDecorator) decorator;
            beans.add(wbDec);
        }


        logger.debug("Validation of the decorator's injection points has started.");

        //Validate Decorators
        validate(beans);

        beans.clear();

        //Adding interceptors to validate
        Set<javax.enterprise.inject.spi.Interceptor<?>> interceptors = manager.getInterceptors();
        for (javax.enterprise.inject.spi.Interceptor interceptor : interceptors) {
            WebBeansInterceptor wbInt = (WebBeansInterceptor) interceptor;
            beans.add(wbInt);
        }

        logger.debug("Validation of the interceptor's injection points has started.");

        //Validate Interceptors
        validate(beans);

        beans.clear();

        beans = manager.getBeans();

        //Validate Others
        validate(beans);

        logger.info(OWBLogConst.INFO_0003);
    }

    /**
     * Validates beans.
     *
     * @param beans deployed beans
     */
    public static void validate(Set<Bean<?>> beans) {
        BeanManagerImpl manager = BeanManagerImpl.getManager();

        if (beans != null && beans.size() > 0) {
            Stack<String> beanNames = new Stack<String>();
            for (Bean<?> bean : beans) {
                String beanName = bean.getName();
                if (beanName != null) {
                    beanNames.push(beanName);
                }


                if (bean instanceof InjectionTargetBean) {
                    //Decorators not applied to interceptors/decorators/@NewBean
                    if (!(bean instanceof Decorator) &&
                            !(bean instanceof javax.enterprise.inject.spi.Interceptor) &&
                            !(bean instanceof NewBean)) {
                        DefinitionUtil.defineDecoratorStack((AbstractInjectionTargetBean<Object>) bean);
                    }

                    //If intercepted marker
                    if (bean instanceof InterceptedMarker) {
                        DefinitionUtil.defineBeanInterceptorStack((AbstractInjectionTargetBean<Object>) bean);
                    }
                }

                //Check passivation scope
                checkPassivationScope(bean);

                //Bean injection points
                Set<InjectionPoint> injectionPoints = bean.getInjectionPoints();

                //Check injection points
                if (injectionPoints != null) {
                    for (InjectionPoint injectionPoint : injectionPoints) {
                        if (!injectionPoint.isDelegate()) {
                            manager.validate(injectionPoint);
                        } else {
                            if (!bean.getBeanClass().isAnnotationPresent(javax.decorator.Decorator.class)
                                    && !BeanManagerImpl.getManager().containsCustomDecoratorClass(bean.getBeanClass())) {
                                throw new WebBeansConfigurationException(
                                        "Delegate injection points can not defined by beans that are not decorator. Injection point : "
                                                + injectionPoint);
                            }
                        }
                    }
                }
            }

            //Validate Bean names
            validateBeanNames(beanNames);

            //Clear Names
            beanNames.clear();
        }

    }

    public static void validateBeanNames(Stack<String> beanNames) {
        if (beanNames.size() > 0) {
            for (String beanName : beanNames) {
                for (String other : beanNames) {
                    String part = null;
                    int i = beanName.lastIndexOf('.');
                    if (i != -1) {
                        part = beanName.substring(0, i);
                    }

                    if (beanName.equals(other)) {
                        InjectionResolver resolver = InjectionResolver.getInstance();
                        Set<Bean<?>> beans = resolver.implResolveByName(beanName);
                        if (beans.size() > 1) {
                            beans = resolver.findByAlternatives(beans);
                            if (beans.size() > 1) {
                                throw new WebBeansConfigurationException("There are two different beans with name : " + beanName + " in the deployment archieve");
                            }
                        }
                    } else {
                        if (part != null) {
                            if (part.equals(other)) {
                                throw new WebBeansConfigurationException("EL name of one bean is of the form x.y, where y is a valid bean EL name, and " +
                                        "x is the EL name of the other bean for the bean name : " + beanName);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks specialization.
     *
     * @param scanner scanner instance
     */
    protected static void checkSpecializations(ScannerService scanner) {
        logger.debug("Checking Specialization constraints has started.");

        try {
            Set<Class<?>> beanClasses = scanner.getBeanClasses();
            if (beanClasses != null && beanClasses.size() > 0) {
                //superClassList is used to handle the case: Car, CarToyota, Bus, SchoolBus, CarFord
                //for which case, the owb should throw exception that both CarToyota and CarFord are
                //specialize Car.
                Class<?> superClass = null;
                ArrayList<Class<?>> superClassList = new ArrayList<Class<?>>();
                ArrayList<Class<?>> specialClassList = new ArrayList<Class<?>>();
                for (Class<?> specialClass : beanClasses) {
                    if (AnnotationUtil.hasClassAnnotation(specialClass, Specializes.class)) {
                        superClass = specialClass.getSuperclass();
                        if (superClass.equals(Object.class)) {
                            throw new WebBeansConfigurationException(logger.getTokenString(OWBLogConst.EXCEPT_0003) + specialClass.getName()
                                    + logger.getTokenString(OWBLogConst.EXCEPT_0004));
                        }
                        if (superClassList.contains(superClass)) {
                            throw new InconsistentSpecializationException(logger.getTokenString(OWBLogConst.EXCEPT_0005) + superClass.getName());
                        }
                        superClassList.add(superClass);
                        specialClassList.add(specialClass);
                    }
                }
                WebBeansUtil.configureSpecializations(specialClassList);
            }

            // XML Defined Specializations
            checkXMLSpecializations();

            //configure specialized producer beans.
            WebBeansUtil.configureProducerMethodSpecializations();
        }
        catch (Exception e) {
            throw new WebBeansDeploymentException(e);
        }


        logger.debug("Checking Specialization constraints has ended.");
    }


    /**
     * Check xml specializations.
     * NOTE : Currently XML is not used in configuration.
     */
    protected static void checkXMLSpecializations() {
        // Check XML specializations
        Set<Class<?>> clazzes = XMLSpecializesManager.getInstance().getXMLSpecializationClasses();
        Iterator<Class<?>> it = clazzes.iterator();
        Class<?> superClass = null;
        Class<?> specialClass = null;
        ArrayList<Class<?>> specialClassList = new ArrayList<Class<?>>();
        while (it.hasNext()) {
            specialClass = it.next();

            if (superClass == null) {
                superClass = specialClass.getSuperclass();
            } else {
                if (superClass.equals(specialClass.getSuperclass())) {
                    throw new InconsistentSpecializationException(logger.getTokenString(OWBLogConst.EXCEPT_XML)
                            + logger.getTokenString(OWBLogConst.EXCEPT_0005)
                            + superClass.getName());
                }
            }
            specialClassList.add(specialClass);
        }
        WebBeansUtil.configureSpecializations(specialClassList);
    }

    /**
     * Check passivations.
     */
    protected static void checkPassivationScope(Bean<?> beanObj) {
        boolean validate = false;

        if (EnterpriseBeanMarker.class.isAssignableFrom(beanObj.getClass())) {
            EnterpriseBeanMarker marker = (EnterpriseBeanMarker) beanObj;
            if (marker.isPassivationCapable()) {
                validate = true;
            }
        } else if (BeanManagerImpl.getManager().isPassivatingScope(beanObj.getScope())) {
            if (WebBeansUtil.isPassivationCapable(beanObj) == null) {
                if (!(beanObj instanceof AbstractProducerBean)) {
                    throw new WebBeansConfigurationException("Passivation scoped defined bean must be passivation capable, " +
                            "but bean : " + beanObj.toString() + " is not passivation capable");
                } else {
                    validate = true;
                }
            }

            validate = true;
        }

        if (validate) {
            ((OwbBean<?>) beanObj).validatePassivationDependencies();
        }
    }

    /**
     * Check steretypes.
     *
     * @param scanner scanner instance
     */
    protected static void checkStereoTypes(ScannerService scanner) {
        logger.debug("Checking StereoType constraints has started.");

        addDefaultStereoTypes();

        Set<Class<?>> beanClasses = scanner.getBeanClasses();
        if (beanClasses != null && beanClasses.size() > 0) {

            for (Class<?> beanClass : beanClasses) {
                if (beanClass.isAnnotation()) {
                    Class<? extends Annotation> stereoClass = (Class<? extends Annotation>) beanClass;
                    if (AnnotationUtil.isStereoTypeAnnotation(stereoClass)) {
                        if (!XMLAnnotationTypeManager.getInstance().hasStereoType(stereoClass)) {
                            WebBeansUtil.checkStereoTypeClass(stereoClass);
                            StereoTypeModel model = new StereoTypeModel(stereoClass);
                            StereoTypeManager.getInstance().addStereoTypeModel(model);
                        }
                    }
                }
            }
        }

        logger.debug("Checking StereoType constraints has ended.");
    }

    /**
     * Adds default stereotypes.
     */
    protected static void addDefaultStereoTypes() {
        StereoTypeModel model = new StereoTypeModel(Model.class);
        StereoTypeManager.getInstance().addStereoTypeModel(model);

        model = new StereoTypeModel(javax.decorator.Decorator.class);
        StereoTypeManager.getInstance().addStereoTypeModel(model);

        model = new StereoTypeModel(Interceptor.class);
        StereoTypeManager.getInstance().addStereoTypeModel(model);
    }

    /**
     * Defines and configures managed bean.
     *
     * @param <T>   type info
     * @param clazz bean class
     * @return true if given class is configured as a managed bean
     */
    protected static <T> boolean defineManagedBean(Class<T> clazz, ProcessAnnotatedTypeImpl<T> processAnnotatedEvent) {
        //Bean manager
        BeanManagerImpl manager = BeanManagerImpl.getManager();

        //Create an annotated type
        AnnotatedType<T> annotatedType = processAnnotatedEvent.getAnnotatedType();

        //Fires ProcessInjectionTarget event for Java EE components instances
        //That supports injections but not managed beans
        ProcessInjectionTargetImpl<T> processInjectionTargetEvent = null;
        if (WebBeansUtil.supportsJavaEeComponentInjections(clazz)) {
            //Fires ProcessInjectionTarget
            processInjectionTargetEvent = WebBeansUtil.fireProcessInjectionTargetEventForJavaEeComponents(clazz);
            WebBeansUtil.inspectErrorStack("There are errors that are added by ProcessInjectionTarget event observers. Look at logs for further details");

            //Sets custom InjectionTarget instance
            if (processInjectionTargetEvent.isSet()) {
                //Adding injection target
                manager.putInjectionTargetWrapperForJavaEeComponents(clazz, new InjectionTargetWrapper<T>(processInjectionTargetEvent.getInjectionTarget()));
            }

            //Checks that not contains @Inject InjectionPoint
            OWBInjector.checkInjectionPointForInjectInjectionPoint(clazz);
        }

        //Check for whether this class is candidate for Managed Bean
        if (ManagedBeanConfigurator.isManagedBean(clazz)) {
            //Check conditions
            ManagedBeanConfigurator.checkManagedBeanCondition(clazz);

            //Temporary managed bean instance creationa
            ManagedBean<T> managedBean = new ManagedBean<T>(clazz, WebBeansType.MANAGED);
            ManagedBeanCreatorImpl<T> managedBeanCreator = new ManagedBeanCreatorImpl<T>(managedBean);

            boolean annotationTypeSet = false;
            if (processAnnotatedEvent.isSet()) {
                annotationTypeSet = true;
                managedBean.setAnnotatedType(annotatedType);
                annotatedType = processAnnotatedEvent.getAnnotatedType();
                managedBeanCreator.setAnnotatedType(annotatedType);
                managedBeanCreator.setMetaDataProvider(BeanCreator.MetaDataProvider.THIRDPARTY);
            }

            //If ProcessInjectionTargetEvent is not set, set it
            if (processInjectionTargetEvent == null) {
                processInjectionTargetEvent = WebBeansUtil.fireProcessInjectionTargetEvent(managedBean);
            }

            //Decorator
            if (WebBeansAnnotatedTypeUtil.isAnnotatedTypeDecorator(annotatedType)) {
                if (logger.wblWillLogDebug()) {
                    logger.debug("Found Managed Bean Decorator with class name : [{0}]", annotatedType.getJavaClass().getName());
                }
                if (annotationTypeSet) {
                    WebBeansAnnotatedTypeUtil.defineDecorator(annotatedType);
                } else {
                    WebBeansUtil.defineDecorator(managedBeanCreator, processInjectionTargetEvent);
                }
            }
            //Interceptor
            else if (WebBeansAnnotatedTypeUtil.isAnnotatedTypeInterceptor(annotatedType)) {
                if (logger.wblWillLogDebug()) {
                    logger.debug("Found Managed Bean Interceptor with class name : [{0}]", annotatedType.getJavaClass().getName());
                }
                if (annotationTypeSet) {
                    WebBeansAnnotatedTypeUtil.defineInterceptor(annotatedType);
                } else {
                    WebBeansUtil.defineInterceptor(managedBeanCreator, processInjectionTargetEvent);
                }
            } else {
                if (BeanManagerImpl.getManager().containsCustomDecoratorClass(annotatedType.getJavaClass()) ||
                        BeanManagerImpl.getManager().containsCustomInterceptorClass(annotatedType.getJavaClass())) {
                    return false;
                }

                if (logger.wblWillLogDebug()) {
                    logger.debug("Found Managed Bean with class name : [{0}]", annotatedType.getJavaClass().getName());
                }
                WebBeansUtil.defineManagedBean(managedBeanCreator, processInjectionTargetEvent);
            }

            return true;
        }
        //Not a managed bean
        else {
            return false;
        }
    }
}
