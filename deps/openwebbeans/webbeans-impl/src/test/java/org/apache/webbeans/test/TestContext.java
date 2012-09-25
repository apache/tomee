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
package org.apache.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.decorator.Decorator;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.interceptor.Interceptor;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.DependentContext;
import org.apache.webbeans.decorator.DecoratorUtil;
import org.apache.webbeans.decorator.WebBeansDecoratorConfig;
import org.apache.webbeans.deployment.StereoTypeModel;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.portable.events.generics.GProcessAnnotatedType;
import org.apache.webbeans.test.component.decorator.broken.DelegateAttributeIsnotInterface;
import org.apache.webbeans.test.component.decorator.broken.DelegateAttributeMustImplementAllDecoratedTypes;
import org.apache.webbeans.test.component.decorator.broken.MoreThanOneDelegateAttribute;
import org.apache.webbeans.test.component.decorator.broken.PaymentDecorator;
import org.apache.webbeans.test.component.decorator.clean.LargeTransactionDecorator;
import org.apache.webbeans.test.component.decorator.clean.ServiceDecorator;
import org.apache.webbeans.test.component.intercept.webbeans.WebBeansInterceptor;
import org.apache.webbeans.test.component.intercept.webbeans.WebBeanswithMetaInterceptor;
import org.apache.webbeans.test.containertests.ComponentResolutionByTypeTest;
import org.apache.webbeans.test.mock.MockManager;
import org.apache.webbeans.test.servlet.ITestContext;
import org.apache.webbeans.test.servlet.TestListener;
import org.apache.webbeans.test.sterotype.StereoWithNonScope;
import org.apache.webbeans.test.sterotype.StereoWithRequestScope;
import org.apache.webbeans.test.sterotype.StereoWithSessionScope;
import org.apache.webbeans.test.sterotype.StereoWithSessionScope2;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;

/**
 * Superclass of all the unit test classes. It defines some methods for
 * subclasses and also do some initializtions for running the tests succesfully.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 * @deprecated
 * Please do not use this class anymore while writing tests. Instead
 * use {@link AbstractUnitTest} class. Also do not add new tests into 
 * the package org.apache.webbeans.test folder. Add your new tests into
 * the org.apache.webbeans.newtests folder.
 * 
 */
public abstract class TestContext implements ITestContext
{
    private Logger logger = WebBeansLoggerFacade.getLogger(TestContext.class);

    /**
     * All unit test classes. It is defined for starting the tests from the
     * ServletContextListener methods
     */
    private static Set<ITestContext> testContexts = new HashSet<ITestContext>();

    /** Test class name */
    private String clazzName;

    /** MockManager is the mock implementation of the {@link BeanManager} */
    private MockManager manager;

    /** Use for XML tests */
    protected WebBeansXMLConfigurator xmlConfigurator = null;

    private WebBeansContext webBeansContext;

    /**
     * Creates new test class.
     * 
     * @param clazzName class name of the test class
     */
    protected TestContext(String clazzName)
    {
        this.clazzName = clazzName;
        TestContext.testContexts.add(this);
        this.manager = new MockManager();
        this.xmlConfigurator = new WebBeansXMLConfigurator();
        this.webBeansContext = WebBeansContext.getInstance();
        webBeansContext.getPluginLoader().startUp();
    }
    

    /**
     * Initialize the tests. NOTE : Actually this has to be defined for each
     * test classes. But for the time being, this super-class globally defines
     * some deployment types, interceptor types, decorator types and
     * stereotypes. If you would like to override default initialization,
     * override these methods in your test classes.
     */
    public void init()
    {
        manager.clear();
        WebBeansContext.getInstance().getPluginLoader().startUp();
        
        initInterceptors();
        initDecorators();
        initStereoTypes();
        initDependentContext();            
    }

    protected void initDependentContext()
    {
        DependentContext dependentContext = (DependentContext) WebBeansContext.getInstance().getContextFactory().getStandardContext(Dependent.class);
        dependentContext.setActive(true);
    }

    /**
     * Initialize some predefined interceptors.
     */
    protected void initInterceptors()
    {
        initializeInterceptorType(WebBeansInterceptor.class);
        initializeInterceptorType(WebBeanswithMetaInterceptor.class);

    }

    /**
     * Initialize some predefined decorators.
     */
    protected void initDecorators()
    {
        initializeDecoratorType(DelegateAttributeIsnotInterface.class);
        initializeDecoratorType(MoreThanOneDelegateAttribute.class);
        initializeDecoratorType(PaymentDecorator.class);
        initializeDecoratorType(DelegateAttributeMustImplementAllDecoratedTypes.class);
        initializeDecoratorType(ServiceDecorator.class);
        initializeDecoratorType(LargeTransactionDecorator.class);

    }

    /**
     * Initialize some predefined stereotypes.
     */
    protected void initStereoTypes()
    {
        initDefaultStereoTypes();
        initializeStereoType(StereoWithNonScope.class);
        initializeStereoType(StereoWithRequestScope.class);
        initializeStereoType(StereoWithSessionScope.class);
        initializeStereoType(StereoWithSessionScope2.class);

    }

    /**
     * Default stereo types
     */
    protected void initDefaultStereoTypes()
    {
        initializeStereoType(Interceptor.class);
        initializeStereoType(Decorator.class);
    }


    /**
     * This will be called whenever the test is failed. NOT : This method is
     * used for running the tests from the ServletContextListener. It is
     * not used for normal unit tests.
     * 
     * @see TestListener
     * @see ComponentResolutionByTypeTest
     * @param methodName failed method name
     */
    public void fail(String methodName)
    {
        logger.severe("Test Class: " + clazzName + ",Method Name: " + methodName + " is FAILED");
    }

    /**
     * Initialize all tests. NOT : This method is used for initializing the all
     * tests classes from the ServletContextListener. It is not used for
     * normal unit tests.
     * 
     * @see TestListener
     * @see ComponentResolutionByTypeTest
     */
    public static void initTests()
    {
        Iterator<ITestContext> it = testContexts.iterator();
        while (it.hasNext())
        {
            it.next().init();
        }

    }

    /**
     * Start all tests. NOT : This method is used for starting the all tests
     * classes from the ServletContextListener. It is not used for
     * normal unit tests.
     * 
     * @see TestListener
     * @see ComponentResolutionByTypeTest
     */
    public static void startAllTests(Object object)
    {
        Iterator<ITestContext> it = testContexts.iterator();
        while (it.hasNext())
        {
            it.next().startTests(object);
        }

    }

    /**
     * Ending all tests. NOT : This method is used for ending the all tests
     * classes from the ServletContextListener. It is not used for
     * normal unit tests.
     * 
     * @see TestListener
     * @see ComponentResolutionByTypeTest
     */
    public static void endAllTests(Object object)
    {
        Iterator<ITestContext> it = testContexts.iterator();
        while (it.hasNext())
        {
            it.next().endTests(object);
        }

    }

    /**
     * Defines simple webbeans from the given class.
     * 
     * @param clazz simple webbeans class
     * @return simple webbean
     */
    protected <T> AbstractInjectionTargetBean<T> defineManagedBean(Class<T> clazz)
    {
        ManagedBean<T> bean;

        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        bean = webBeansContext.getManagedBeanConfigurator().define(clazz, WebBeansType.MANAGED);
        if (bean != null)
        {
            DecoratorUtil.checkManagedBeanDecoratorConditions(bean,null);
            WebBeansDecoratorConfig.configureDecorators(bean);
            webBeansContext.getDefinitionUtil().defineBeanInterceptorStack(bean);

            getComponents().add((AbstractOwbBean<?>) bean);
            manager.addBean(bean);

            GProcessAnnotatedType type = new GProcessAnnotatedType(webBeansContext.getAnnotatedElementFactory().newAnnotatedType(clazz));
            manager.fireEvent(type, new Annotation[0]);            
        }

        return bean;
    }

    /**
     * Defines simple webbeans interceptor.
     * 
     * @param clazz interceptor class
     * @return the new interceptor
     */
    @SuppressWarnings("unchecked")
    protected <T> AbstractOwbBean<T> defineInterceptor(Class<T> clazz)
    {
        ManagedBean<T> component = null;

        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        webBeansContext.getManagedBeanConfigurator().checkManagedBeanCondition(clazz);

        webBeansContext.getInterceptorsManager().addNewInterceptor(clazz);
        AnnotatedType annotatedType = webBeansContext.getAnnotatedElementFactory().newAnnotatedType(clazz);
        webBeansContext.getInterceptorUtil().checkInterceptorConditions(annotatedType);
        component = webBeansContext.getManagedBeanConfigurator().define(clazz, WebBeansType.INTERCEPTOR);
        webBeansContext.getWebBeansInterceptorConfig().configureInterceptorClass((ManagedBean<Object>) component,
                                                            webBeansContext.getAnnotationManager().getInterceptorBindingMetaAnnotations(
                                                                clazz.getDeclaredAnnotations()));


        return component;
    }

    /**
     * Defines the simple webbeans decorator.
     * 
     * @param clazz decorator class
     * @return the new decorator
     */
    @SuppressWarnings("unchecked")
    protected <T> AbstractOwbBean<T> defineDecorator(Class<T> clazz)
    {
        ManagedBean<T> component = null;

        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        if (webBeansContext.getDecoratorsManager().isDecoratorEnabled(clazz))
        {
            DecoratorUtil.checkDecoratorConditions(clazz);
            component = webBeansContext.getManagedBeanConfigurator().define(clazz, WebBeansType.DECORATOR);

            if (component != null)
            {
                WebBeansDecoratorConfig.configureDecoratorClass((ManagedBean<Object>) component);
            }
        }

        return component;
    }

    /**
     * Clear all components in the {@link MockManager}
     */
    protected void clear()
    {
        this.manager.clear();
        WebBeansContext.getInstance().getPluginLoader().startUp();
    }

    /**
     * Gets all components in the {@link MockManager}
     * 
     * @return all components
     */
    protected List<AbstractOwbBean<?>> getComponents()
    {
        return manager.getComponents();
    }

    /**
     * Return the size of the webbeans in the {@link MockManager}
     * 
     * @return the size of the components in the {@link MockManager}
     */
    protected int getDeployedComponents()
    {
        return manager.getDeployedCompnents();
    }

    /**
     * Gets the webbeans instance.
     * 
     * @param name name of the webbean
     * @return the webbeans instance
     */
    protected Object getInstanceByName(String name)
    {
        Bean<?> bean = manager.resolve(manager.getBeans(name));
        if (bean == null)
        {
            return null;
        }

        return manager.getReference(bean, Object.class, manager.createCreationalContext(bean));
    }

    /**
     * Gets the context with given scope type.
     * 
     * @param scopeType scope type
     * @return the context with given scope type
     */
    protected Context getContext(Class<? extends Annotation> scopeType)
    {
        return manager.getContext(scopeType);
    }

    /**
     * Gets the {@link MockManager} instance.
     * 
     * @return manager instance
     */
    protected MockManager getManager()
    {
        return manager;
    }

    protected WebBeansContext getWebBeansContext()
    {
        return webBeansContext;
    }

    /**
     * Return new MockHttpSession
     * 
     * @return new mock session
     */
    protected Object getSession()
    {
        //X TODO huh? WTF...
        return new Object();
    }

    /**
     * Add new stereotype model.
     * 
     * @param stereoClass stereotype class
     */
    protected void initializeStereoType(Class<? extends Annotation> stereoClass)
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        webBeansContext.getAnnotationManager().checkStereoTypeClass(stereoClass, stereoClass.getDeclaredAnnotations());
        StereoTypeModel model = new StereoTypeModel(webBeansContext, stereoClass);
        webBeansContext.getStereoTypeManager().addStereoTypeModel(model);
    }

    /**
     * Add new interceptor class.
     * 
     * @param interceptorClazz interceptor class
     */
    protected void initializeInterceptorType(Class<?> interceptorClazz)
    {
        WebBeansContext.getInstance().getInterceptorsManager().addNewInterceptor(interceptorClazz);

    }

    /**
     * Add new deocrator class.
     * 
     * @param decoratorClazz decorator class
     */
    protected void initializeDecoratorType(Class<?> decoratorClazz)
    {
        WebBeansContext.getInstance().getDecoratorsManager().addNewDecorator(decoratorClazz);

    }

    /**
     * End tests for sub-class. NOTE : This method is used for ending the all
     * test methods in sub-class from the ServletContextListener. It is
     * not used for normal unit tests.
     * 
     * @see TestListener
     * @see ComponentResolutionByTypeTest
     */
    public void endTests(Object ctx)
    {

    }

    /**
     * Start tests for sub-class. NOTE : This method is used for starting the all
     * test methods in sub-class from the ServletContextListener. It is
     * not used for normal unit tests.
     * 
     * @see TestListener
     * @see ComponentResolutionByTypeTest
     */
    public void startTests(Object ctx)
    {
    }     

}
