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
package org.apache.webbeans.component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.decorator.Delegate;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.config.inheritance.BeanInheritedMetaData;
import org.apache.webbeans.config.inheritance.IBeanInheritedMetaData;
import org.apache.webbeans.decorator.WebBeansDecorator;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.inject.InjectableField;
import org.apache.webbeans.inject.InjectableMethods;
import org.apache.webbeans.intercept.InterceptorData;
import javax.enterprise.inject.spi.InterceptionType;
import org.apache.webbeans.intercept.InvocationContextImpl;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.proxy.ProxyFactory;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Abstract class for injection target beans.
 * 
 * @version $Rev: 1385030 $ $Date: 2012-09-15 10:41:29 +0200 (sam., 15 sept. 2012) $
 * @param <T> bean class
 */
public abstract class AbstractInjectionTargetBean<T> extends AbstractOwbBean<T> implements InjectionTargetBean<T>
{
    /** Bean observable method */
    private Set<Method> observableMethods = new HashSet<Method>();

    /** Injected fields of the bean */
    private Set<Field> injectedFields = new HashSet<Field>();

    /** Injected methods of the bean */
    private Set<Method> injectedMethods = new HashSet<Method>();
    
    /** Injected fields of the bean */
    private Set<Field> injectedFromSuperFields = new HashSet<Field>();

    /** Injected methods of the bean */
    private Set<Method> injectedFromSuperMethods = new HashSet<Method>();
    
    /**Annotated type for bean*/
    private AnnotatedType<T> annotatedType;
    
    /**
     * Holds the all of the interceptor related data, contains around-invoke,
     * post-construct and pre-destroy
     */
    protected List<InterceptorData> interceptorStack = new ArrayList<InterceptorData>();

    /**Decorators*/
    protected List<Decorator<?>> decorators = new ArrayList<Decorator<?>>();
    
    /**Bean inherited meta data*/
    protected IBeanInheritedMetaData inheritedMetaData;    
    
    /**
     * Creates a new observer owner component.
     * 
     * @param webBeansType webbean type
     * @param returnType bean class type
     * @param webBeansContext
     */
    protected AbstractInjectionTargetBean(WebBeansType webBeansType, Class<T> returnType, WebBeansContext webBeansContext)
    {
        super(webBeansType, returnType, webBeansContext);
    }

    /**
     * {@inheritDoc}
     */
    protected T createInstance(CreationalContext<T> creationalContext)
    {
        T instance;

        //Default creation phases
        instance = createDefaultInstance(creationalContext);

        return instance;
    }

    /**
     * Returns bean instance.
     * 
     * @param creationalContext creational context
     * @return bean instance
     */
    @SuppressWarnings("unchecked")
    protected T createDefaultInstance(CreationalContext<T> creationalContext)
    {
        beforeConstructor();
        
        //Create actual bean instance
        T instance = createComponentInstance(creationalContext);
        //For dependent instance checks
        T dependentProxy = null;
        boolean isDependentProxy = false;
        if(getScope() == Dependent.class && !(this instanceof EnterpriseBeanMarker))
        {
            final ProxyFactory proxyFactory = getWebBeansContext().getProxyFactory();
            T result = (T) proxyFactory.createDependentScopedBeanProxy(this, instance, creationalContext);
            //Means that Dependent Bean has interceptor/decorator
            if(proxyFactory.isProxyInstance(result))
            {
                //This is a dependent scoped bean instance,
                //Therefore we inject dependencies of this instance
                //Otherwise we loose injection
                injectResources(instance, creationalContext);
                injectSuperFields(instance, creationalContext);
                injectSuperMethods(instance, creationalContext);
                injectFields(instance, creationalContext);
                injectMethods(instance, creationalContext);            
                
                //Dependent proxy
                dependentProxy = result;
                
                //This is a dependent
                isDependentProxy = true;
            }
        }
                        
        
        //If dependent proxy
        if(isDependentProxy)
        {
            return dependentProxy;
        }
        
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    protected void destroyInstance(T instance, CreationalContext<T> creationalContext)
    {
        destroyComponentInstance(instance,creationalContext);
    }

    /**
     * Sub-classes must override this method to create bean instance.
     * 
     * @param creationalContext creational context
     * @return bean instance
     */
    protected T createComponentInstance(CreationalContext<T> creationalContext)
    {
        return null;
    }

    /**
     * Sub-classes must override this method to destroy bean instance.
     * 
     * @param instance object instance.
     */
    protected void destroyComponentInstance(T instance, CreationalContext<T> creationalContext)
    {
        preDestroy(instance, creationalContext);
    }

    /**
     * Called before constructor.
     */
    protected void beforeConstructor()
    {

    }

    /**
     * Called after bean instance is created.
     * 
     * @param instance bean instance
     * @param creationalContext cretional context object
     */
    protected void afterConstructor(T instance, CreationalContext<T> creationalContext)
    {
        //Inject resources
        injectResources(instance, creationalContext);
        
        injectSuperFields(instance, creationalContext);
        injectSuperMethods(instance, creationalContext);
        
        // Inject fields
        injectFields(instance, creationalContext);

        // Inject methods
        injectMethods(instance, creationalContext);
        
        //Post construct
        postConstruct(instance, creationalContext);
    }

    /**
     * {@inheritDoc}
     */
    public void postConstruct(T instance, CreationalContext<T> cretionalContext)
    {
        postConstructDefault(instance, cretionalContext);
    }

    /**
     * Default post construct.
     * 
     * @param instance bean instance
     */
    protected void postConstructDefault(T instance, CreationalContext<T> ownerCreationalContext)
    {
        if(getWebBeansType().equals(WebBeansType.MANAGED))
        {
            // Call Post Construct
            if (WebBeansUtil.isContainsInterceptorMethod(getInterceptorStack(), InterceptionType.POST_CONSTRUCT))
            {
                InvocationContextImpl impl = new InvocationContextImpl(getWebBeansContext(), null, instance, null, null,
                        getWebBeansContext().getInterceptorUtil().getInterceptorMethods(getInterceptorStack(),
                                                                                        InterceptionType.POST_CONSTRUCT),
                                                                                        InterceptionType.POST_CONSTRUCT);
                impl.setCreationalContext(ownerCreationalContext);
                try
                {
                    impl.proceed();
                }

                catch (Exception e)
                {
                    getLogger().log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0008, "@PostConstruct."), e);
                    throw new WebBeansException(e);
                }
            }            
        }        
    }

    /**
     * {@inheritDoc}
     */
    public void preDestroy(T instance, CreationalContext<T> creationalContext)
    {
        preDestroyDefault(instance, creationalContext);
    }

    /**
     * Default predestroy.
     * 
     * @param instance bean instance
     */
    protected void preDestroyDefault(T instance, CreationalContext<T> creationalContext)
    {
        if(getWebBeansType().equals(WebBeansType.MANAGED) ||
                getWebBeansType().equals(WebBeansType.DECORATOR))                
        {
            if (WebBeansUtil.isContainsInterceptorMethod(getInterceptorStack(), InterceptionType.PRE_DESTROY))
            {                
                InvocationContextImpl impl = new InvocationContextImpl(getWebBeansContext(), null, instance, null, null,
                        getWebBeansContext().getInterceptorUtil().getInterceptorMethods(getInterceptorStack(),
                                                                                        InterceptionType.PRE_DESTROY),
                                                                                        InterceptionType.PRE_DESTROY);
                impl.setCreationalContext(creationalContext);
                try
                {
                    impl.proceed();
                }
                catch (Exception e)
                {
                    getLogger().log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0008, "@PreDestroy."), e);
                    throw new WebBeansException(e);
                }
            }            
        }
    }

    /**
     * Injects fields of the bean after constructing.
     * 
     * @param instance bean instance
     * @param creationalContext creational context
     */
    public void injectFields(T instance, CreationalContext<T> creationalContext)
    {
        Set<Field> fields = getInjectedFields();
        for (Field field : fields)
        {
            if (field.getAnnotation(Delegate.class) == null)
            {
                if(!field.getType().equals(InjectionPoint.class))
                {
                    injectField(field, instance, creationalContext);   
                }
                //InjectionPoint.
                else
                {
                    Bean<?> injectionPointBean = getManager().getBeans(InjectionPoint.class, new DefaultLiteral())
                                                              .iterator().next();
                    Object reference = getManager().getReference(injectionPointBean, InjectionPoint.class,
                                             getManager().createCreationalContext(injectionPointBean));
                    
                    setField(instance, field, reference);
                }
            }
        }                
    }

    private void setField(T instance, Field field, Object value)
    {
        if(!field.isAccessible())
        {
            getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(field, true);
        }

        try
        {
            field.set(instance, value);
        }
        catch (IllegalArgumentException e)
        {
            throw new WebBeansException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new WebBeansException(e);
        }
    }

    public void injectSuperFields(T instance, CreationalContext<T> creationalContext)
    {
        Set<Field> fields = getInjectedFromSuperFields();
        for (Field field : fields)
        {
            if (field.getAnnotation(Delegate.class) == null)
            {
                injectField(field, instance, creationalContext);
            }
        }                        
    }
    
    public void injectSuperMethods(T instance, CreationalContext<T> creationalContext)
    {
        Set<Method> methods = getInjectedFromSuperMethods();

        for (Method method : methods)
        {
            injectMethod(method, instance, creationalContext);
        }        
    }
    
    
    private void injectField(Field field, Object instance, CreationalContext<?> creationalContext)
    {
        InjectableField f = new InjectableField(field, instance, this, creationalContext);
        f.doInjection();        
    }

    /**
     * Injects all {@link javax.inject.Inject} methods of the bean instance.
     * 
     * @param instance bean instance
     * @param creationalContext creational context instance
     */
    public void injectMethods(T instance, CreationalContext<T> creationalContext)
    {
        Set<Method> methods = getInjectedMethods();

        for (Method method : methods)
        {
            injectMethod(method, instance, creationalContext);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void injectMethod(Method method, Object instance, CreationalContext<?> creationalContext)
    {
        InjectableMethods m = new InjectableMethods(method, instance, this, creationalContext);
        m.doInjection();        
    }

    /**
     * {@inheritDoc}
     */
    public void injectResources(T instance, CreationalContext<T> creationalContext)
    {
        if(getWebBeansType().equals(WebBeansType.MANAGED))
        {
            try
            {
                ResourceInjectionService service = null;
                try
                {
                    service = getWebBeansContext().getService(ResourceInjectionService.class);
                    
                }
                catch(Exception e)
                {
                    // When running in tests
                }
                
                if(service != null)
                {
                    service.injectJavaEEResources(instance);   
                }
            }
            catch (Exception e)
            {
                getLogger().log(Level.SEVERE, OWBLogConst.ERROR_0023, instance);
                throw new WebBeansException(MessageFormat.format(
                        WebBeansLoggerFacade.getTokenString(OWBLogConst.ERROR_0023), instance), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addObservableMethod(Method observerMethod)
    {
        observableMethods.add(observerMethod);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Method> getObservableMethods()
    {
        return observableMethods;
    }

    /**
     * Gets injected fields.
     * 
     * @return injected fields
     */
    public Set<Field> getInjectedFields()
    {
        return injectedFields;
    }

    /**
     * Add new injected field.
     * 
     * @param field new injected field
     */
    public void addInjectedField(Field field)
    {
        injectedFields.add(field);
    }
    
    /**
     * Gets injected from super fields.
     * 
     * @return injected fields
     */
    public Set<Field> getInjectedFromSuperFields()
    {
        return injectedFromSuperFields;
    }

    /**
     * Add new injected field.
     * 
     * @param field new injected field
     */
    public void addInjectedFieldToSuper(Field field)
    {
        injectedFromSuperFields.add(field);
    }
    

    /**
     * Gets injected methods.
     * 
     * @return injected methods
     */
    public Set<Method> getInjectedMethods()
    {
        return injectedMethods;
    }

    /**
     * Add new injected method.
     * 
     * @param method new injected method
     */
    public void addInjectedMethod(Method method)
    {
        injectedMethods.add(method);
    }

    /**
     * Gets injected from super methods.
     * 
     * @return injected methods
     */
    public Set<Method> getInjectedFromSuperMethods()
    {
        return injectedFromSuperMethods;
    }

    /**
     * Add new injected method.
     * 
     * @param method new injected method
     */
    public void addInjectedMethodToSuper(Method method)
    {
        injectedFromSuperMethods.add(method);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<InterceptorData> getInterceptorStack()
    {
        return interceptorStack;
    }
    
    public List<Decorator<?>> getDecoratorStack()
    {
        return decorators;
    }

    /**
     * {@inheritDoc}
     */
    public IBeanInheritedMetaData getInheritedMetaData()
    {
        return inheritedMetaData;
    }
    
    /**
     * Sets inherited meta data.
     */
    protected void setInheritedMetaData()
    {
        inheritedMetaData = new BeanInheritedMetaData<T>(this);
    }

    /**
     * {@inheritDoc}
     */
    public AnnotatedType<T> getAnnotatedType()
    {
        return annotatedType;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setAnnotatedType(AnnotatedType<T> annotatedType)
    {
        this.annotatedType = annotatedType;
    }
    
    /* (non-Javadoc)
     * @see org.apache.webbeans.component.AbstractOwbBean#validatePassivationDependencies()
     */
    @Override
    public void validatePassivationDependencies()
    {        
        super.validatePassivationDependencies();
        
        //Check for interceptors and decorators
        for(int i = 0, size = decorators.size(); i < size; i++)
        {
            Decorator<?> dec = decorators.get(i);
            WebBeansDecorator<?> decorator = (WebBeansDecorator<?>)dec;
            if(!decorator.isPassivationCapable())
            {
                throw new WebBeansConfigurationException(MessageFormat.format(
                        WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0015), toString()));
            }
            else
            {
                decorator.validatePassivationDependencies();
            }
        }
        
        for(int i = 0, size = interceptorStack.size(); i < size; i++)
        {
            InterceptorData interceptorData = interceptorStack.get(i);
            if(interceptorData.isDefinedWithWebBeansInterceptor())
            {
                WebBeansInterceptor<?> interceptor = (WebBeansInterceptor<?>)interceptorData.getWebBeansInterceptor();
                if(!interceptor.isPassivationCapable())
                {
                    throw new WebBeansConfigurationException(MessageFormat.format(
                            WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0016), toString()));
                }
                else
                {
                    interceptor.validatePassivationDependencies();
                }
            }
            else
            {
                if(interceptorData.isDefinedInInterceptorClass())
                {
                    Class<?> interceptorClass = interceptorData.getInterceptorClass();
                    if(!Serializable.class.isAssignableFrom(interceptorClass))
                    {
                        throw new WebBeansConfigurationException(MessageFormat.format(
                                WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0016), toString()));
                    }               
                    else
                    {
                        if(!getWebBeansContext().getAnnotationManager().checkInjectionPointForInterceptorPassivation(interceptorClass))
                        {
                            throw new WebBeansConfigurationException(MessageFormat.format(
                                    WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0017), toString(), interceptorClass));
                        }
                    }
                }
            }
        }
    }    
    
    
    
}
