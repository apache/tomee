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
package org.apache.webbeans.intercept;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.AroundInvoke;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.context.creational.EjbInterceptorContext;
import org.apache.webbeans.decorator.WebBeansDecoratorInterceptor;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.plugins.OpenWebBeansEjbLCAPlugin;

/**
 * Abstract implementation of the {@link InterceptorData} api contract.
 * 
 * @version $Rev: 1366701 $ $Date: 2012-07-28 18:07:31 +0200 (sam., 28 juil. 2012) $
 */
public class InterceptorDataImpl implements InterceptorData
{
    // Logger instance
    private final static Logger logger = WebBeansLoggerFacade.getLogger(InterceptorDataImpl.class);

    /** Around invokes method */
    private Method aroundInvoke = null;
    
    /** Around timeout method */
    private Method aroundTimeout = null;

    /** Post construct methods */
    private Method postConstruct = null;

    /** Post activate method */
    private Method postActivate = null;
    
    /** Predestroy Method */
    private Method preDestroy = null;

    /** Prepassivate Method */
    private Method prePassivate = null;
    
    private Interceptor<?> webBeansInterceptor;

    /** Defined in the interceptor or bean */
    private boolean definedInInterceptorClass;

    /** Whether the interceptor class is defined in the method */
    private boolean definedInMethod;

    /**
     * If defined in method true, then this method holds interceptor binding
     * annotated method
     */
    private Method annotatedMethod;

    /** Defined with webbeans specific interceptor */
    private boolean isDefinedWithWebBeansInterceptor;

    private Class<?> interceptorClass = null;

    private WebBeansDecoratorInterceptor decoratorInterceptor = null;

    private final WebBeansContext webBeansContext;

    public InterceptorDataImpl(boolean isDefinedWithWebBeansInterceptor, WebBeansContext webBeansContext)
    {
        this(isDefinedWithWebBeansInterceptor, null, webBeansContext);
    }

    public InterceptorDataImpl(boolean isDefinedWithWebBeansInterceptor,
                               WebBeansDecoratorInterceptor decoratorInterceptor, WebBeansContext webBeansContext)
    {
        this.isDefinedWithWebBeansInterceptor = isDefinedWithWebBeansInterceptor;
        this.decoratorInterceptor = decoratorInterceptor;
        this.webBeansContext = webBeansContext;
    }

    public Class<?> getInterceptorClass()
    {
        return interceptorClass;
    }

    public void setInterceptorClass(Class<?> clazz)
    {
        interceptorClass = clazz;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#setInterceptor(java.lang
     * .reflect.Method, java.lang.Class)
     */
    public void setInterceptorMethod(Method m, Class<? extends Annotation> annotation)
    {
        OpenWebBeansEjbLCAPlugin ejbPlugin = webBeansContext.getPluginLoader().getEjbLCAPlugin();
        Class <? extends Annotation> prePassivateClass = null;
        Class <? extends Annotation> postActivateClass = null;
        Class <? extends Annotation> aroundTimeoutClass = null;
        if (null != ejbPlugin)
        {
            prePassivateClass = ejbPlugin.getPrePassivateClass();
            postActivateClass = ejbPlugin.getPostActivateClass();
            aroundTimeoutClass = ejbPlugin.getAroundTimeoutClass();
        }
        
        if (annotation.equals(AroundInvoke.class))
        {
            setAroundInvoke(m);
        }
        else if (annotation.equals(PostConstruct.class))
        {
            setPostConstruct(m);
        }
        else if (annotation.equals(PreDestroy.class))
        {
            setPreDestroy(m);
        } 
        else if (null != ejbPlugin && annotation.equals(prePassivateClass))
        {
            setPrePassivate(m);
        } 
        else if (null != ejbPlugin && annotation.equals(postActivateClass))
        {
            setPostActivate(m);
        } 
        else if (null != ejbPlugin && annotation.equals(aroundTimeoutClass))
        {
            setAroundTimeout(m);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#addAroundInvoke(java.lang
     * .reflect.Method)
     */
    public void setAroundInvoke(Method m)
    {
        aroundInvoke = m;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#addAroundTimeout(java.lang
     * .reflect.Method)
     */
    public void setAroundTimeout(Method m)
    {
        aroundTimeout = m;
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#addPostConstruct(java.lang
     * .reflect.Method)
     */
    protected void setPostConstruct(Method m)
    {
        postConstruct = m;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#addPostActivate(java.lang
     * .reflect.Method)
     */
    protected void setPostActivate(Method m)
    {
        postActivate = m;
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#addPreDestroy(java.lang
     * .reflect.Method)
     */
    protected void setPreDestroy(Method m)
    {
        preDestroy = m;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#addPrePassivate(java.lang
     * .reflect.Method)
     */
    protected void setPrePassivate(Method m)
    {
        prePassivate = m;
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.intercept.InterceptorData#getPostConstruct()
     */
    public Method getPostConstruct()
    {
        return postConstruct;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.intercept.InterceptorData#getPostActivate()
     */
    public Method getPostActivate()
    {
        return postActivate;
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.intercept.InterceptorData#getPreDestroy()
     */
    public Method getPreDestroy()
    {
        return preDestroy;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.intercept.InterceptorData#getPrePassivate()
     */
    public Method getPrePassivate()
    {
        return prePassivate;
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.intercept.InterceptorData#getAroundInvoke()
     */
    public Method getAroundInvoke()
    {
        return aroundInvoke;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.intercept.InterceptorData#getAroundTimeout()
     */
    public Method getAroundTimeout()
    {
        return aroundTimeout;
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#isDefinedInInterceptorClass
     * ()
     */
    public boolean isDefinedInInterceptorClass()
    {
        return definedInInterceptorClass;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#setDefinedInInterceptorClass
     * (boolean)
     */
    public void setDefinedInInterceptorClass(boolean definedInInterceptorClass)
    {
        this.definedInInterceptorClass = definedInInterceptorClass;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.intercept.InterceptorData#isDefinedInMethod()
     */
    public boolean isDefinedInMethod()
    {
        return definedInMethod;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#setDefinedInMethod(boolean)
     */
    public void setDefinedInMethod(boolean definedInMethod)
    {
        this.definedInMethod = definedInMethod;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.intercept.InterceptorData#getAnnotatedMethod()
     */
    public Method getInterceptorBindingMethod()
    {
        return annotatedMethod;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.webbeans.intercept.InterceptorData#setAnnotatedMethod(java
     * .lang.reflect.Method)
     */
    public void setInterceptorBindingMethod(Method annotatedMethod)
    {
        this.annotatedMethod = annotatedMethod;
    }

    /*
     * (non-Javadoc)
     * @seeorg.apache.webbeans.intercept.InterceptorData#
     * isDefinedWithWebBeansInterceptor()
     */
    public boolean isDefinedWithWebBeansInterceptor()
    {
        return isDefinedWithWebBeansInterceptor;
    }

    /**
     * @return the webBeansInterceptor
     */
    public Interceptor<?> getWebBeansInterceptor()
    {
        return webBeansInterceptor;
    }

    /**
     * @param webBeansInterceptor the webBeansInterceptor to set
     */
    public void setWebBeansInterceptor(Interceptor<?> webBeansInterceptor)
    {
        this.webBeansInterceptor = webBeansInterceptor;
    }

    public Method getInterceptorMethod()
    {
        if (aroundInvoke != null)
        {
            return aroundInvoke;
        }
        else if (aroundTimeout != null)
        {
            return aroundTimeout;
        }
        else if (postConstruct != null)
        {
            return postConstruct;
        }
        else if (postActivate != null)
        {
            return postActivate;
        }
        else if (preDestroy != null)
        {
            return preDestroy;
        }
        else if (prePassivate != null)
        {
            return prePassivate;
        }

        return null;
    }

    public boolean isLifecycleInterceptor()
    {
        if (preDestroy != null || postConstruct != null || prePassivate != null || postActivate != null)
        {
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public Object createNewInstance(Object ownerInstance, CreationalContextImpl<?> ownerCreationalContext)
    {
        // check for this InterceptorData is defined by interceptor class
        if (isDefinedWithWebBeansInterceptor && definedInInterceptorClass)
        {
            Object interceptor;

            // Means that it is the last interceptor added by InterceptorHandler
            if (webBeansInterceptor == null)
            {
                return decoratorInterceptor;
            }

            interceptor = ownerCreationalContext.getDependentInterceptor(ownerInstance, webBeansInterceptor);
            // There is no define interceptor, define and add it into dependent
            if (interceptor == null)
            {
                BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();

                WebBeansInterceptor<Object> actualInterceptor = (WebBeansInterceptor<Object>) webBeansInterceptor;
                CreationalContext<Object> creationalContext = manager.createCreationalContext(actualInterceptor);
                interceptor = manager.getReference(actualInterceptor, actualInterceptor.getBeanClass(), creationalContext);

                actualInterceptor.setInjections(interceptor, creationalContext);

                ownerCreationalContext.addDependent(ownerInstance, (WebBeansInterceptor<Object>) webBeansInterceptor, interceptor);
            }
            return interceptor;
        }

        EjbInterceptorContext ejbInterceptorContext ;
        Object interceptor = null;
        // control for this InterceptorData is defined by interceptor class
        if (definedInInterceptorClass)
        {
            ejbInterceptorContext = ownerCreationalContext.getEjbInterceptor(ownerInstance, interceptorClass);
            if (ejbInterceptorContext == null)
            {
                interceptor = webBeansContext.getWebBeansUtil().newInstanceForced(interceptorClass);
                try
                {
                    OWBInjector.inject(webBeansContext.getBeanManagerImpl(), interceptor, ownerCreationalContext);

                    ejbInterceptorContext = new EjbInterceptorContext();
                    ejbInterceptorContext.setInterceptorInstance(interceptor);
                    ejbInterceptorContext.setInterceptorClass(interceptorClass);
                }
                catch (Exception e)
                {
                    logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0022, interceptorClass), e);
                }

                ownerCreationalContext.addEjbInterceptor(ownerInstance, ejbInterceptorContext);
            }
            else
            {
                interceptor = ejbInterceptorContext.getInterceptorInstance();
            }
        }
        return interceptor;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (null != webBeansInterceptor) 
        {
            sb.append("webBeansInterceptor: [").append(webBeansInterceptor.getBeanClass()).append("]");
        }
        sb.append(" aroundInvoke  [").append(aroundInvoke).append("]");
        sb.append(" aroundTimeout [").append(aroundTimeout).append("]");
        
        sb.append(" postConstruct [").append(postConstruct).append("]");
        sb.append(" preDestroy    [").append(preDestroy).append("]");
        
        sb.append(" postActivate  [").append(postActivate).append("]");
        sb.append(" prePassivate  [").append(prePassivate).append("]");
        

        return sb.toString();
    }

}
