/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Model;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.interceptor.Interceptor;
import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.AbstractProducerBean;
import org.apache.webbeans.component.EnterpriseBeanMarker;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.component.InjectionTargetWrapper;
import org.apache.webbeans.component.InterceptedMarker;
import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.component.NewBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.component.creation.BeanCreator.MetaDataProvider;
import org.apache.webbeans.component.creation.ManagedBeanCreatorImpl;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectableBeanManager;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.corespi.se.DefaultJndiService;
import org.apache.webbeans.decorator.WebBeansDecorator;
import org.apache.webbeans.deployment.StereoTypeModel;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansDeploymentException;
import org.apache.webbeans.exception.inject.InconsistentSpecializationException;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.portable.AnnotatedElementFactory;
import org.apache.webbeans.portable.events.ProcessAnnotatedTypeImpl;
import org.apache.webbeans.portable.events.ProcessInjectionTargetImpl;
import org.apache.webbeans.portable.events.discovery.AfterBeanDiscoveryImpl;
import org.apache.webbeans.portable.events.discovery.AfterDeploymentValidationImpl;
import org.apache.webbeans.portable.events.discovery.BeforeBeanDiscoveryImpl;
import org.apache.webbeans.portable.events.generics.GProcessInjectionTarget;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansWebPlugin;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.InjectionExceptionUtils;
import org.apache.webbeans.util.WebBeansConstants;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;

/**
 * Deploys the all beans that are defined in the {@link org.apache.webbeans.spi.ScannerService} at
 * the scanner phase.
 */
@SuppressWarnings("unchecked")
//This class written as single threaded.
public class BeansDeployer
{
    //Logger instance
    private final static Logger logger = WebBeansLoggerFacade.getLogger(BeansDeployer.class);

    /**Deployment is started or not*/
    protected boolean deployed = false;

    /**XML Configurator*/
    protected WebBeansXMLConfigurator xmlConfigurator = null;
    
    /**Discover ejb or not*/
    protected boolean discoverEjb = false;
    private final WebBeansContext webBeansContext;

    /**
     * Creates a new deployer with given xml configurator.
     * 
     * @param xmlConfigurator xml configurator
     * @param webBeansContext
     */
    public BeansDeployer(WebBeansXMLConfigurator xmlConfigurator, WebBeansContext webBeansContext)
    {
        this.xmlConfigurator = xmlConfigurator;
        this.webBeansContext = webBeansContext;
        String usage = this.webBeansContext.getOpenWebBeansConfiguration().getProperty(OpenWebBeansConfiguration.USE_EJB_DISCOVERY);
        discoverEjb = Boolean.parseBoolean(usage);
    }

    /**
     * Deploys all the defined web beans components in the container startup.
     * <p>
     * It deploys from the web-beans.xml files and from the class files. It uses
     * the {@link org.apache.webbeans.spi.ScannerService} to get classes.
     * </p>
     * 
     * @throws WebBeansDeploymentException if any deployment exception occurs
     */
    public void deploy(ScannerService scanner)
    {
        try
        {
            if (!deployed)
            {                
                //Load Extensions
                webBeansContext.getExtensionLoader().loadExtensionServices();

                // Bind manager
                JNDIService service = webBeansContext.getService(JNDIService.class);
                
                //Default jndi is just a map
                if(service instanceof DefaultJndiService)
                {
                    service.bind(WebBeansConstants.WEB_BEANS_MANAGER_JNDI_NAME, new InjectableBeanManager(webBeansContext.getBeanManagerImpl()));
                }
                //Assume, actual JNDI implementation
                else
                {
                    service.bind(WebBeansConstants.WEB_BEANS_MANAGER_JNDI_NAME, webBeansContext.getBeanManagerImpl().getReference());
                }

                // Register Manager built-in component
                webBeansContext.getBeanManagerImpl().addInternalBean(webBeansContext.getWebBeansUtil().getManagerBean());

                //Fire Event
                fireBeforeBeanDiscoveryEvent();
                
                //Deploy bean from XML. Also configures deployments, interceptors, decorators.
                deployFromXML(scanner);
                
                //Checking stereotype conditions
                checkStereoTypes(scanner);
                
                //Configure Default Beans
                configureDefaultBeans();
                                
                //Discover classpath classes
                deployFromClassPath(scanner);
                
                //Deploy additional Annotated Types
                deployAdditionalAnnotatedTypes();

                //Check Specialization
                checkSpecializations(scanner);
                
                //Fire Event
                fireAfterBeanDiscoveryEvent();
                
                //Validate injection Points
                validateInjectionPoints();
                
                //Fire Event
                fireAfterDeploymentValidationEvent();


                // do some cleanup after the deployment
                scanner.release();
                webBeansContext.getAnnotatedElementFactory().clear();

                deployed = true;
            }

        }
        catch(Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            WebBeansUtil.throwRuntimeExceptions(e);
        }
    }


    /**
     * Configure Default Beans.
     */
    private void configureDefaultBeans()
    {
        BeanManagerImpl beanManager = webBeansContext.getBeanManagerImpl();
        WebBeansUtil webBeansUtil = webBeansContext.getWebBeansUtil();

        // Register Conversation built-in component
        beanManager.addInternalBean(webBeansUtil.getConversationBean());
        
        // Register InjectionPoint bean
        beanManager.addInternalBean(webBeansUtil.getInjectionPointBean());
        
        //Register Instance Bean
        beanManager.addInternalBean(webBeansUtil.getInstanceBean());
        
        //Register Event Bean
        beanManager.addInternalBean(webBeansUtil.getEventBean());
        
        //REgister Provider Beans
        OpenWebBeansJavaEEPlugin beanEeProvider = webBeansContext.getPluginLoader().getJavaEEPlugin();
        OpenWebBeansWebPlugin beanWebProvider = webBeansContext.getPluginLoader().getWebPlugin();
        
        if(beanEeProvider != null)
        {
            addDefaultBean(beanManager, "org.apache.webbeans.ee.common.beans.PrincipalBean");
            addDefaultBean(beanManager, "org.apache.webbeans.ee.beans.ValidatorBean");
            addDefaultBean(beanManager, "org.apache.webbeans.ee.beans.ValidatorFactoryBean");
            addDefaultBean(beanManager, "org.apache.webbeans.ee.beans.UserTransactionBean");
        }
        else if(beanWebProvider != null)
        {
            addDefaultBean(beanManager, "org.apache.webbeans.ee.common.beans.PrincipalBean");
        }
            
    }
    
    private void addDefaultBean(BeanManagerImpl manager,String className)
    {
        Bean<?> bean = null;
        
        Class<?> beanClass = ClassUtil.getClassFromName(className);
        if(beanClass != null)
        {
            bean  = (Bean)ClassUtil.newInstance(webBeansContext, beanClass);
        }
        
        if(bean != null)
        {
            manager.addInternalBean(bean);
        }
    }
    
    /**
     * Fires event before bean discovery.
     */
    private void fireBeforeBeanDiscoveryEvent()
    {
        BeanManager manager = webBeansContext.getBeanManagerImpl();
        manager.fireEvent(new BeforeBeanDiscoveryImpl(webBeansContext));
    }
    
    /**
     * Fires event after bean discovery.
     */
    private void fireAfterBeanDiscoveryEvent()
    {
        BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();
        manager.fireEvent(new AfterBeanDiscoveryImpl(webBeansContext));

        webBeansContext.getWebBeansUtil().inspectErrorStack(
            "There are errors that are added by AfterBeanDiscovery event observers. Look at logs for further details");
    }
    
    /**
     * Fires event after deployment valdiation.
     */
    private void fireAfterDeploymentValidationEvent()
    {
        BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();
        manager.fireEvent(new AfterDeploymentValidationImpl(manager));

        webBeansContext.getWebBeansUtil().inspectErrorStack(
            "There are errors that are added by AfterDeploymentValidation event observers. Look at logs for further details");
    }
    
    /**
     * Validate all injection points.
     */
    private void validateInjectionPoints()
    {
        logger.fine("Validation of injection points has started.");

        webBeansContext.getDecoratorsManager().validateDecoratorClasses();
        webBeansContext.getInterceptorsManager().validateInterceptorClasses();

        BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();
        Set<Bean<?>> beans = new HashSet<Bean<?>>();
        
        //Adding decorators to validate
        Set<Decorator<?>> decorators = manager.getDecorators();
        for(Decorator decorator : decorators)
        {
            WebBeansDecorator wbDec = (WebBeansDecorator)decorator;
            beans.add(wbDec);
        }
        
        
        logger.fine("Validation of the decorator's injection points has started.");
        
        //Validate Decorators
        validate(beans);
        
        beans.clear();
        
        //Adding interceptors to validate
        List<javax.enterprise.inject.spi.Interceptor<?>> interceptors = manager.getInterceptors();
        for(javax.enterprise.inject.spi.Interceptor interceptor : interceptors)
        {
            WebBeansInterceptor wbInt = (WebBeansInterceptor)interceptor;
            beans.add(wbInt);
        }
        
        logger.fine("Validation of the interceptor's injection points has started.");
        
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
    private void validate(Set<Bean<?>> beans)
    {
        BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();
        
        if (beans != null && beans.size() > 0)
        {
           Stack<String> beanNames = new Stack<String>();
            for (Bean<?> bean : beans)
            {
                if (bean instanceof OwbBean && !((OwbBean)bean).isEnabled())
                {
                    // we skip disabled beans
                    continue;
                }

                String beanName = bean.getName();
                if(beanName != null)
                {
                    beanNames.push(beanName);
                }
                
                
                if(bean instanceof InjectionTargetBean)
                {
                    //Decorators not applied to interceptors/decorators/@NewBean
                    if(!(bean instanceof Decorator) && 
                            !(bean instanceof javax.enterprise.inject.spi.Interceptor) &&
                            !(bean instanceof NewBean))
                    {
                        webBeansContext.getDefinitionUtil().defineDecoratorStack((AbstractInjectionTargetBean<Object>)bean);
                    }
                    
                    //If intercepted marker
                    if(bean instanceof InterceptedMarker)
                    {
                        webBeansContext.getDefinitionUtil().defineBeanInterceptorStack((AbstractInjectionTargetBean<Object>)bean);
                    }                                                            
                }                
                
                //Check passivation scope
                checkPassivationScope(bean);
                                
                //Bean injection points
                Set<InjectionPoint> injectionPoints = bean.getInjectionPoints();
                                
                //Check injection points
                if(injectionPoints != null)
                {
                    for (InjectionPoint injectionPoint : injectionPoints)
                    {
                        if(!injectionPoint.isDelegate())
                        {
                            manager.validate(injectionPoint);   
                        }
                        else
                        {
                            if(!bean.getBeanClass().isAnnotationPresent(javax.decorator.Decorator.class)
                                    && !webBeansContext.getBeanManagerImpl().containsCustomDecoratorClass(bean.getBeanClass()))
                            {
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
    
    private void validateBeanNames(Stack<String> beanNames)
    {
        if(beanNames.size() > 0)
        {   
            for(String beanName : beanNames)
            {
                for(String other : beanNames)
                {
                    String part = null;
                    int i = beanName.lastIndexOf('.');
                    if(i != -1)
                    {
                        part = beanName.substring(0,i);                
                    }
                    
                    if(beanName.equals(other))
                    {
                        InjectionResolver resolver = webBeansContext.getBeanManagerImpl().getInjectionResolver();
                        Set<Bean<?>> beans = resolver.implResolveByName(beanName);
                        if(beans.size() > 1)
                        {
                            beans = resolver.findByAlternatives(beans);                            
                            if(beans.size() > 1)
                            {
                                InjectionExceptionUtils.throwAmbiguousResolutionExceptionForBeanName(beans, beanName);
                            }   
                        }
                    }
                    else
                    {
                        if(part != null)
                        {
                            if(part.equals(other))
                            {
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
     * Discovers and deploys classes from class path.
     * 
     * @param scanner discovery scanner
     * @throws ClassNotFoundException if class not found
     */
    protected void deployFromClassPath(ScannerService scanner) throws ClassNotFoundException
    {
        logger.fine("Deploying configurations from class files has started.");

        // Start from the class
        Set<Class<?>> classIndex = scanner.getBeanClasses();
        
        //Iterating over each class
        if (classIndex != null)
        {
            AnnotatedElementFactory annotatedElementFactory = webBeansContext.getAnnotatedElementFactory();
            List<AnnotatedType<?>> additionalAnnotatedTypes = webBeansContext.getBeanManagerImpl()
                                                                             .getAdditionalAnnotatedTypes();

            for(Class<?> implClass : classIndex)
            {
                //Define annotation type
                AnnotatedType<?> annotatedType = annotatedElementFactory.newAnnotatedType(implClass);
                
                if (null != annotatedType)
                {
                    deploySingleAnnotatedType(implClass, annotatedType);

                    if (additionalAnnotatedTypes.contains(annotatedType))
                    {
                        // if the implClass already gets processed as part of the
                        // standard BDA scanning, then we don't need to 'additionally'
                        // deploy it anymore.
                        additionalAnnotatedTypes.remove(annotatedType);
                    }

                } 
                else
                {
                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.fine("Error creating managed bean " + implClass);
                    }

                }

            }
        }

        logger.fine("Deploying configurations from class files has ended.");

    }
    
    /**
     * Deploys any AnnotatedTypes added to the BeanManager during beforeBeanDiscovery.
     */
    private void deployAdditionalAnnotatedTypes()
    {
        BeanManagerImpl beanManager = webBeansContext.getBeanManagerImpl();

        List<AnnotatedType<?>> annotatedTypes = beanManager.getAdditionalAnnotatedTypes();
        
        for(AnnotatedType<?> type : annotatedTypes)
        {
            Class implClass = type.getJavaClass();

            deploySingleAnnotatedType(implClass, type);
        }
    }

    /**
     * Common helper method used to deploy annotated types discovered through
     * scanning or during beforeBeanDiscovery.
     * 
     * @Param Class implClass the class of the bean to be deployed
     * @Param AnnotatedType the AnnotatedType representing the bean to be deployed
     */
    private void deploySingleAnnotatedType(Class implClass, AnnotatedType annotatedType)
    {
        // Fires ProcessAnnotatedType
        ProcessAnnotatedTypeImpl<?> processAnnotatedEvent =
            webBeansContext.getWebBeansUtil().fireProcessAnnotatedTypeEvent(annotatedType);

        // if veto() is called
        if (processAnnotatedEvent.isVeto())
        {
            return;
        }

        // Try class is Managed Bean
        boolean isDefined = defineManagedBean((Class<Object>) implClass, (ProcessAnnotatedTypeImpl<Object>) processAnnotatedEvent);

        // Try class is EJB bean
        if (!isDefined && discoverEjb)
        {
            if (EJBWebBeansConfigurator.isSessionBean(implClass, webBeansContext))
            {
                logger.log(Level.FINE, "Found Enterprise Bean with class name : [{0}]", implClass.getName());
                defineEnterpriseWebBean((Class<Object>) implClass, (ProcessAnnotatedTypeImpl<Object>) processAnnotatedEvent);
            }
        }
    }
    
    /**
     * Discovers and deploys alternatives, interceptors and decorators from XML.
     * 
     * @param scanner discovery scanner
     *
     * @throws WebBeansDeploymentException if a problem occurs
     */
    protected void deployFromXML(ScannerService scanner) throws WebBeansDeploymentException
    {
        logger.fine("Deploying configurations from XML files has started.");

        Set<URL> xmlLocations = scanner.getBeanXmls();
        Iterator<URL> it = xmlLocations.iterator();

        while (it.hasNext())
        {
            URL url = it.next();

            if (logger.isLoggable(Level.FINE))
            {
                logger.fine("OpenWebBeans BeansDeployer configuring: " + url.toExternalForm());
            }

            InputStream fis = null;
            try
            {
                fis = url.openStream();

                xmlConfigurator.configure(fis, url.toExternalForm(), scanner);
            }
            catch (IOException e)
            {
                throw new WebBeansDeploymentException("Error configuring: filename: " + url.toExternalForm() , e);
            }
            finally
            {
                if (fis != null)
                {
                    try
                    {
                        fis.close();
                    }
                    catch (IOException e)
                    {
                        // all ok, ignore this!
                    }
                }
            }
        }

        if(logger.isLoggable(Level.FINE))
        {
            logger.fine("Deploying configurations from XML has ended successfully.");
        }
    }

    /**
     * Checks specialization.
     * @param scanner scanner instance
     */
    protected void checkSpecializations(ScannerService scanner)
    {
        logger.fine("Checking Specialization constraints has started.");
        
        try
        {
            Set<Class<?>> beanClasses = scanner.getBeanClasses();
            if (beanClasses != null && beanClasses.size() > 0)
            {
                //superClassList is used to handle the case: Car, CarToyota, Bus, SchoolBus, CarFord
                //for which case, the owb should throw exception that both CarToyota and CarFord are 
                //specialize Car. 
                Class<?> superClass;
                ArrayList<Class<?>> superClassList = new ArrayList<Class<?>>();
                ArrayList<Class<?>> specialClassList = new ArrayList<Class<?>>();
                for(Class<?> specialClass : beanClasses)
                {
                    if(AnnotationUtil.hasClassAnnotation(specialClass, Specializes.class))
                    {
                        superClass = specialClass.getSuperclass();
                        if(superClass.equals(Object.class))
                        {
                            throw new WebBeansConfigurationException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0003) + specialClass.getName()
                                                                     + WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0004));
                        }
                        if (superClassList.contains(superClass))
                        {
                            throw new InconsistentSpecializationException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0005) + superClass.getName());
                        }
                        superClassList.add(superClass);
                        specialClassList.add(specialClass);
                    }
                }
                webBeansContext.getWebBeansUtil().configureSpecializations(specialClassList);
            }


            //configure specialized producer beans.
            webBeansContext.getWebBeansUtil().configureProducerMethodSpecializations();
        }
        catch(Exception e)
        {
            throw new WebBeansDeploymentException(e);
        }
        

        logger.fine("Checking Specialization constraints has ended.");
    }

    
    /**
     * Check passivations.
     */
    protected void checkPassivationScope(Bean<?> beanObj)
    {
        boolean validate = false;
        
        if(beanObj instanceof EnterpriseBeanMarker)
        {
            EnterpriseBeanMarker marker = (EnterpriseBeanMarker)beanObj;
            if(marker.isPassivationCapable())
            {
                validate = true;   
            }
        }        
        else if(webBeansContext.getBeanManagerImpl().isPassivatingScope(beanObj.getScope()))
        {
            if(WebBeansUtil.isPassivationCapable(beanObj) == null)
            {
                if(!(beanObj instanceof AbstractProducerBean))
                {
                    throw new WebBeansConfigurationException("Passivation scoped defined bean must be passivation capable, " +
                            "but bean : " + beanObj.toString() + " is not passivation capable");
                }
                else
                {
                    validate = true;
                }
            }

            validate = true;
        }
        
        if(validate)
        {
            ((OwbBean<?>)beanObj).validatePassivationDependencies();
        }
    }

    /**
     * Check steretypes.
     * @param scanner scanner instance
     */
    protected void checkStereoTypes(ScannerService scanner)
    {
        logger.fine("Checking StereoType constraints has started.");

        addDefaultStereoTypes();

        final AnnotationManager annotationManager = webBeansContext.getAnnotationManager();
        
        Set<Class<?>> beanClasses = scanner.getBeanClasses();
        if (beanClasses != null && beanClasses.size() > 0)
        {
            for(Class<?> beanClass : beanClasses)
            {                
                if(beanClass.isAnnotation())
                {
                    Class<? extends Annotation> stereoClass = (Class<? extends Annotation>) beanClass;
                    if (annotationManager.isStereoTypeAnnotation(stereoClass))
                    {
                        webBeansContext.getAnnotationManager().checkStereoTypeClass(stereoClass, stereoClass.getDeclaredAnnotations());
                        StereoTypeModel model = new StereoTypeModel(webBeansContext, stereoClass);
                        webBeansContext.getStereoTypeManager().addStereoTypeModel(model);
                    }
                }
            }
        }

        logger.fine("Checking StereoType constraints has ended.");
    }

    /**
     * Adds default stereotypes.
     */
    protected void addDefaultStereoTypes()
    {
        StereoTypeModel model = new StereoTypeModel(webBeansContext, Model.class);
        webBeansContext.getStereoTypeManager().addStereoTypeModel(model);
    }

    /**
     * Defines and configures managed bean.
     * @param <T> type info
     * @param clazz bean class
     * @return true if given class is configured as a managed bean
     */
    protected <T> boolean defineManagedBean(Class<T> clazz, ProcessAnnotatedTypeImpl<T> processAnnotatedEvent)
    {   
        //Bean manager
        BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();
        
        //Create an annotated type
        AnnotatedType<T> annotatedType = processAnnotatedEvent.getAnnotatedType();
                                
        //Fires ProcessInjectionTarget event for Java EE components instances
        //That supports injections but not managed beans
        ProcessInjectionTargetImpl<T> processInjectionTargetEvent = null;
        if(webBeansContext.getWebBeansUtil().supportsJavaEeComponentInjections(clazz))
        {
            //Fires ProcessInjectionTarget
            processInjectionTargetEvent =
                webBeansContext.getWebBeansUtil().fireProcessInjectionTargetEventForJavaEeComponents(clazz);
            webBeansContext.getWebBeansUtil().inspectErrorStack(
                "There are errors that are added by ProcessInjectionTarget event observers. Look at logs for further details");

            //Sets custom InjectionTarget instance
            if(processInjectionTargetEvent.isSet())
            {
                //Adding injection target
                manager.putInjectionTargetWrapperForJavaEeComponents(clazz,
                        new InjectionTargetWrapper<T>(processInjectionTargetEvent.getInjectionTarget()));
            }
            
            //Checks that not contains @Inject InjectionPoint
            webBeansContext.getAnnotationManager().checkInjectionPointForInjectInjectionPoint(clazz);
        }
        
        //Check for whether this class is candidate for Managed Bean
        if (webBeansContext.getManagedBeanConfigurator().isManagedBean(clazz))
        {
            //Check conditions
            webBeansContext.getManagedBeanConfigurator().checkManagedBeanCondition(clazz);

            //Temporary managed bean instance creationa
            ManagedBean<T> managedBean = new ManagedBean<T>(clazz, webBeansContext);
            ManagedBeanCreatorImpl<T> managedBeanCreator = new ManagedBeanCreatorImpl<T>(managedBean);

            boolean annotationTypeSet = false;
            if(processAnnotatedEvent.isModifiedAnnotatedType())
            {
                annotationTypeSet = true;
                managedBean.setAnnotatedType(annotatedType);
                managedBeanCreator.setAnnotatedType(annotatedType);
                managedBeanCreator.setMetaDataProvider(MetaDataProvider.THIRDPARTY);
            }

            GProcessInjectionTarget processInjectionTarget = null;
            if(processInjectionTargetEvent == null)
            {
                processInjectionTarget = webBeansContext.getWebBeansUtil().createProcessInjectionTargetEvent(managedBean);
                processInjectionTargetEvent = processInjectionTarget;
            }


            //Decorator
            if(WebBeansUtil.isAnnotatedTypeDecorator(annotatedType))
            {
                if (logger.isLoggable(Level.FINE))
                {
                    logger.log(Level.FINE, "Found Managed Bean Decorator with class name : [{0}]", annotatedType.getJavaClass().getName());
                }
                if(annotationTypeSet)
                {
                    webBeansContext.getWebBeansUtil().defineDecorator(annotatedType);
                }
                else
                {
                    webBeansContext.getWebBeansUtil().defineDecorator(managedBeanCreator,
                                                                      processInjectionTargetEvent);
                }
            }
            //Interceptor
            else if(WebBeansUtil.isAnnotatedTypeInterceptor(annotatedType))
            {
                if (logger.isLoggable(Level.FINE))
                {
                    logger.log(Level.FINE, "Found Managed Bean Interceptor with class name : [{0}]", annotatedType.getJavaClass().getName());
                }
                if(annotationTypeSet)
                {
                    webBeansContext.getWebBeansUtil().defineInterceptor(annotatedType);
                }
                else
                {
                    webBeansContext.getWebBeansUtil().defineInterceptor(managedBeanCreator,
                                                                        processInjectionTargetEvent);
                }
            }
            else
            {
                if(webBeansContext.getBeanManagerImpl().containsCustomDecoratorClass(annotatedType.getJavaClass()) ||
                        webBeansContext.getBeanManagerImpl().containsCustomInterceptorClass(annotatedType.getJavaClass()))
                {
                    return false;
                }
                
                if (logger.isLoggable(Level.FINE))
                {
                    logger.log(Level.FINE, "Found Managed Bean with class name : [{0}]", annotatedType.getJavaClass().getName());
                }
                webBeansContext.getWebBeansUtil().defineManagedBean(managedBeanCreator,
                                                                    processInjectionTargetEvent, true);
            }

            if(processInjectionTarget != null)
            {
                if(processInjectionTargetEvent != null)
                {
                    final InjectionTarget originalInjectionTarget = processInjectionTargetEvent.getInjectionTarget();
                    final InjectionTarget updatedInjectionTarget = webBeansContext.getWebBeansUtil()
                            .fireProcessInjectionTargetEvent(processInjectionTarget).getInjectionTarget();
                    if (updatedInjectionTarget != originalInjectionTarget)
                    {
                        webBeansContext.getBeanManagerImpl().putInjectionTargetWrapper(managedBean, new InjectionTargetWrapper<T>(updatedInjectionTarget));
                    }
                }
            }

            return true;
        }
        else
        {
            //Not a managed bean
            return false;
        }
                                
    }
    
    /**
     * Defines enterprise bean via plugin.
     * @param <T> bean class type
     * @param clazz bean class
     */
    protected <T> void defineEnterpriseWebBean(Class<T> clazz, ProcessAnnotatedType<T> processAnnotatedTypeEvent)
    {
        InjectionTargetBean<T> bean = (InjectionTargetBean<T>) EJBWebBeansConfigurator.defineEjbBean(clazz, processAnnotatedTypeEvent,
                                                                                                     webBeansContext);
        webBeansContext.getWebBeansUtil().setInjectionTargetBeanEnableFlag(bean);
    }
}
