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

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.InjectionPoint;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

/**
 * Abstract implementation of the {@link OwbBean} contract. 
 * 
 * @version $Rev: 1368981 $ $Date: 2012-08-03 16:31:17 +0200 (ven., 03 août 2012) $
 * 
 * @see javax.enterprise.inject.spi.Bean
 * 
 */
public abstract class AbstractOwbBean<T> implements OwbBean<T>
{
    /**Logger instance*/
    protected Logger logger = null;
    
    /** Name of the bean */
    protected String name;

    /** Cached scope type of the bean */
    protected Class<? extends Annotation> scopeClass;

    /** Qualifiers of the bean */
    protected Set<Annotation> implQualifiers = new HashSet<Annotation>();

    /** Api types of the bean */
    protected Set<Type> apiTypes = new HashSet<Type>();

    /** Web Beans type */
    protected WebBeansType webBeansType;

    /** Return type of the bean */
    protected Class<T> returnType;

    /** Stereotypes of the bean */
    protected Set<Annotation> stereoTypes = new HashSet<Annotation>();

    /** this is only for public access and will be built from {@link #stereoTypes} on demand */
    protected Set<Class<? extends Annotation>> stereoTypeClasses = null;

    /**This bean is specialized or not*/
    protected boolean specializedBean;

    /**This bean is enabled or disabled*/
    protected boolean enabled = true;
    
    /** The bean is serializable or not */
    protected boolean serializable;

    /** The bean allows nullable object */
    protected boolean nullable = true;
    
    /**Beans injection points*/
    protected Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();

    /**
     * We gonna cache the hashCode since it is used millions of times per second.
     * Beans are pretty much static once they got constructed. So it's easy to
     * cache it and reset the cache to the default of 0 on a change
     * which means it should get recalculated.
     */
    private int cachedHashCode = 0;


    /**
     * This string will be used for passivating the Bean.
     * It will be created on the first use.
     * @see #getId()
     */
    protected String passivatingId = null;
    
    protected final WebBeansContext webBeansContext;

    /**
     * Constructor definiton. Each subclass redefines its own constructor with
     * calling this.
     * 
     * @param returnType of the bean
     * @param webBeansContext
     * @param webBeansType web beans type
     */
    protected AbstractOwbBean(WebBeansType webBeansType, Class<T> returnType, WebBeansContext webBeansContext)
    {
        this.webBeansType = webBeansType;
        this.returnType = returnType;
        this.webBeansContext = webBeansContext;
    }

    /**
     * Creates a new instance.
     * 
     * @param webBeanType beans type
     * @param webBeansContext
     */
    protected AbstractOwbBean(WebBeansType webBeanType, WebBeansContext webBeansContext)
    {
        this(webBeanType, null, webBeansContext);
    }

    /**
     * Get the web beans context this bean is associated with
     *
     * @return WebBeansContext this bean is associated with
     */
    public WebBeansContext getWebBeansContext()
    {
        return webBeansContext;
    }
    
    /**
     * Gets manager instance
     * 
     * @return manager instance
     */
    protected BeanManagerImpl getManager()
    {
        return webBeansContext.getBeanManagerImpl();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T create(CreationalContext<T> creationalContext)
    {
        T instance;
        try
        {  
            if(!(creationalContext instanceof CreationalContextImpl))
            {
                creationalContext = webBeansContext.getCreationalContextFactory().wrappedCreationalContext(
                        creationalContext, this); 
            }
           
            InjectionTargetWrapper<T> wrapper = getManager().getInjectionTargetWrapper(this);
            //If wrapper not null
            if(wrapper != null)
            {
                instance = wrapper.produce(creationalContext);
                wrapper.inject(instance, creationalContext);
                wrapper.postConstruct(instance);
            }
            else
            {
                instance = createInstance(creationalContext); 
                if(this instanceof AbstractInjectionTargetBean)
                {
                    ((AbstractInjectionTargetBean<T>)this).afterConstructor(instance, creationalContext);
                }
            }                                    
        }
        catch (Exception re)
        {
            Throwable throwable = getRootException(re);
            
            if(!(throwable instanceof RuntimeException))
            {
                throw new CreationException(throwable);
            }
            throw (RuntimeException) throwable;
        }

        return instance;
    }

    private Throwable getRootException(Throwable throwable)
    {
        if(throwable.getCause() == null || throwable.getCause() == throwable)
        {
            return throwable;
        }
        else
        {
            return getRootException(throwable.getCause());
        }
    }

    /**
     * Creates the instance of the bean that has a specific implementation
     * type. Each subclass must define its own create mechanism.
     *
     * @param creationalContext the contextual instance shall be created in
     * @return instance of the bean
     */
    protected abstract T createInstance(CreationalContext<T> creationalContext);
    
    /**
     * {@inheritDoc}
     */
    public T createNewInstance(CreationalContext<T> creationalContext)
    {
        return createInstance(creationalContext);
    }

    /*
     * (non-Javadoc)
     * @param creationalContext the contextual instance has been created in
     */
    public void destroy(T instance, CreationalContext<T> creationalContext)
    {
        try
        {
            InjectionTargetWrapper<T> wrapper = getManager().getInjectionTargetWrapper(this);
            if(wrapper != null)
            {
                // instance might be null if we only created a proxy
                // but no actual contextual instance for this bean!
                if (instance != null)
                {
                    wrapper.preDestroy(instance);
                    wrapper.dispose(instance);
                }
            }
            else
            {
                //Destroy instance, call @PreDestroy
                destroyInstance(instance,creationalContext);
            }
            
            //Destory dependent instances
            creationalContext.release();
        }
        catch(Exception e)
        {
            getLogger().log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.FATAL_0001, this), e);
        }
    }

    /**
     * Destroy the instance of the bean. Each subclass must define its own
     * destroy mechanism.
     * 
     * @param instance instance of the bean that is being destroyed
     * @param creationalContext the contextual instance has been created in
     */
    protected void destroyInstance(T instance, CreationalContext<T> creationalContext)
    {
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void destroyCreatedInstance(T instance, CreationalContext<T> creationalContext)
    {
        destroyInstance(instance, creationalContext);
    }
    
    /**
     * get the unique Id of the bean. This will get used as reference on
     * passivation.
     *
     * {@inheritDoc}
     */
    public String getId()
    {
        if (!isEnabled() || returnType.equals(Object.class))
        {
            // if the Bean is disabled, either by rule, or by
            // annotating it @Typed() as Object, then it is not serializable
            return null;
        }
        if (passivatingId == null)
        {
            StringBuilder sb = new StringBuilder(webBeansType.toString()).append('#');
            sb.append(returnType).append('#');
            for (Annotation qualifier : implQualifiers)
            {
                sb.append(qualifier.toString()).append(',');
            }
            
            passivatingId = sb.toString();
        }

        return passivatingId;
    }
    
    public boolean isPassivationCapable()
    {
        return false;
    }

    /**
     * Get return types of the bean.
     * As per section 11.1 it is defined as
     * &quot;returns the bean class of the managed bean or session bean or of the bean
     * that declares the producer method or field.&quot;
     * Which means in case of a producer field or method, we need to return the class
     * where the producer field/method is defined in.
     */
    public Class<?> getBeanClass()
    {
        if(IBeanHasParent.class.isAssignableFrom(getClass()))
        {
            @SuppressWarnings("unchecked")
            IBeanHasParent<T> comp = (IBeanHasParent<T>)this;
            
            return comp.getParent().getBeanClass();
        }
        
        return getReturnType();
    }

    /**
     * Set scope type.
     * 
     * @param scopeType scope type
     */
    public void setImplScopeType(Annotation scopeType)
    {
        scopeClass = scopeType.annotationType();
        cachedHashCode = 0;
    }

    /**
     * Name of the bean.
     * 
     * @return name of the bean
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get web bean type of the bean.
     * 
     * @return web beans type
     */
    public WebBeansType getWebBeansType()
    {
        return webBeansType;
    }

    /**
     * Add new stereotype.
     *
     * @param stereoType new stereotype annotation
     */
    public void addStereoType(Annotation stereoType)
    {
        stereoTypeClasses = null; // will get rebuilt on the next request

        stereoTypes.add(stereoType);
        cachedHashCode = 0;
    }

    /**
     * Add new api type.
     *
     * @param apiType new api type
     */
    public void addApiType(Class<?> apiType)
    {
        apiTypes.add(apiType);
        cachedHashCode = 0;
    }

    /**
     * Gets the stereotypes.
     *
     * @return stereotypes of the bean
     */
    public Set<Annotation> getOwbStereotypes()
    {
        return stereoTypes;
    }

    /**
     * Add new qualifier.
     *
     * @param qualifier new qualifier
     */
    public void addQualifier(Annotation qualifier)
    {
        implQualifiers.add(qualifier);
        cachedHashCode = 0;
    }

    /**
     * Set name.
     * 
     * @param name new name
     */
    public void setName(String name)
    {
        if (this.name == null)
        {
            this.name = name;
            cachedHashCode = 0;
        }
        else
        {
            throw new UnsupportedOperationException("Component name is already set to: " + this.name);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.webbeans.manager.Bean#getQualifiers()
     */
    public Set<Annotation> getQualifiers()
    {
        return implQualifiers;
    }

    /*
     * (non-Javadoc)
     * @see javax.webbeans.manager.Bean#getScope()
     */
    public Class<? extends Annotation> getScope()
    {
        return scopeClass;
    }

    
    public Set<Type> getTypes()
    {        
        return apiTypes;
    }

    /**
     * Gets type of the producer method/field or the bean class if it's not a producer.
     * This basically determines the class which will get created.
     * 
     * @return type of the producer method
     * @see #getBeanClass()
     */
    public Class<T> getReturnType()
    {
        return returnType;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setNullable(boolean nullable)
    {
        this.nullable = nullable;
        cachedHashCode = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setSerializable(boolean serializable)
    {
        this.serializable = serializable;
        cachedHashCode = 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNullable()
    {
        return nullable;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isSerializable()
    {
        return serializable;
    }

    /**
     * {@inheritDoc}
     */    
    public void addInjectionPoint(InjectionPoint injectionPoint)
    {
        injectionPoints.add(injectionPoint);
    }
    
    /**
     * {@inheritDoc}
     */    
    public Set<InjectionPoint> getInjectionPoints()
    {
        return injectionPoints;
    }
    
    /**
     * {@inheritDoc}
     */    
    public void setSpecializedBean(boolean specialized)
    {
        specializedBean = specialized;
        cachedHashCode = 0;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        cachedHashCode = 0;
    }
    
    /**
     * {@inheritDoc}
     */    
    public boolean isSpecializedBean()
    {
        return specializedBean;
    }
    
    /**
     * {@inheritDoc}
     */    
    public List<InjectionPoint> getInjectionPoint(Member member)
    {
        List<InjectionPoint> points = new ArrayList<InjectionPoint>();
        
        for(InjectionPoint ip : injectionPoints)
        {
            if(ip.getMember().equals(member))
            {
                points.add(ip);
            }
        }
        
        return points;
    }
    
    /**
     * {@inheritDoc}
     */    
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        if (stereoTypeClasses == null)
        {
            Set<Class<? extends Annotation>> set = new HashSet<Class<? extends Annotation>>();

            for(Annotation ann : stereoTypes)
            {
                set.add(ann.annotationType());
            }
            stereoTypeClasses = set;
        }

        return stereoTypeClasses;
    }
    
     /**
     * {@inheritDoc}
     */    
    public boolean isAlternative()
    {
        return webBeansContext.getAlternativesManager().isBeanHasAlternative(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled()
    {
        return enabled;
    }
    
        
    /**
     * {@inheritDoc}
     */    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if (returnType != null)
        {
            final String simpleName = returnType.getSimpleName();
            builder.append(simpleName).append(", ");
        }
        builder.append("Name:").append(getName()).append(", WebBeans Type:").append(getWebBeansType());
        builder.append(", API Types:[");
        
        int size = getTypes().size();
        int index = 1;
        for(Type clazz : getTypes())
        {
            if(clazz instanceof Class)
            {
                builder.append(((Class<?>)clazz).getName());    
            }
            else
            {
                Class<?> rawType = (Class<?>)((ParameterizedType)clazz).getRawType();
                builder.append(rawType.getName());
            }
            
            if(index < size)
            {
                builder.append(",");
            }
            
            index++;                        
        }
        
        builder.append("], ");
        builder.append("Qualifiers:[");
        
        size = getQualifiers().size();
        index = 1;
        for(Annotation ann : getQualifiers())
        {
            builder.append(ann.annotationType().getName());
            
            if(index < size)
            {
                builder.append(",");
            }
            
            index++;
        }
        
        builder.append("]");
        
        return builder.toString();
    }

    /**
     * The Logger should really only be used to log errors!
     */
    protected synchronized Logger getLogger()
    {
        if (logger == null)
        {
            logger = WebBeansLoggerFacade.getLogger(getClass());
        }
        return logger;
    }

    public boolean isDependent()
    {
        return getScope().equals(Dependent.class);
    }
    
    public void validatePassivationDependencies()
    {
        if(isPassivationCapable())
        {
            Set<InjectionPoint> beanInjectionPoints = getInjectionPoints();
            for(InjectionPoint injectionPoint : beanInjectionPoints)
            {
                if(!injectionPoint.isTransient())
                {
                    if(!getWebBeansContext().getWebBeansUtil().isPassivationCapableDependency(injectionPoint))
                    {
                        if(injectionPoint.getAnnotated().isAnnotationPresent(Disposes.class))
                        {
                            continue;
                        }
                        throw new WebBeansConfigurationException(
                                "Passivation capable beans must satisfy passivation capable dependencies. " +
                                "Bean : " + toString() + " does not satisfy. Details about the Injection-point: " +
                                        injectionPoint.toString());
                    }
                }
            }            
        }
    }

    @Override
    public int hashCode()
    {
        if (cachedHashCode != 0)
        {
            return cachedHashCode;
        }

        final int prime = 31;
        int result = 1;
        result = prime * result + ((apiTypes == null) ? 0 : apiTypes.hashCode());
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + (isAlternative() ? 1289 : 1273);
        result = prime * result + ((implQualifiers == null) ? 0 : implQualifiers.hashCode());
        result = prime * result + ((injectionPoints == null) ? 0 : injectionPoints.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nullable ? 1231 : 1237);
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
        result = prime * result + ((scopeClass == null) ? 0 : scopeClass.hashCode());
        result = prime * result + (serializable ? 1231 : 1237);
        result = prime * result + (specializedBean ? 1231 : 1237);
        result = prime * result + ((stereoTypeClasses == null) ? 0 : stereoTypeClasses.hashCode());
        result = prime * result + ((stereoTypes == null) ? 0 : stereoTypes.hashCode());
        result = prime * result + ((webBeansType == null) ? 0 : webBeansType.hashCode());
        cachedHashCode = result;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        AbstractOwbBean other = (AbstractOwbBean) obj;
        if (apiTypes == null)
        {
            if (other.apiTypes != null)
            {
                return false;
            }
        }
        else if (!apiTypes.equals(other.apiTypes))
        {
            return false;
        }
        if (enabled != other.enabled)
        {
            return false;
        }
        if (isAlternative() != other.isAlternative())
        {
            return false;
        }
        if (implQualifiers == null)
        {
            if (other.implQualifiers != null)
            {
                return false;
            }
        }
        else if (!implQualifiers.equals(other.implQualifiers))
        {
            return false;
        }
        if (injectionPoints == null)
        {
            if (other.injectionPoints != null)
            {
                return false;
            }
        }
        else if (!injectionPoints.equals(other.injectionPoints))
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        if (nullable != other.nullable)
        {
            return false;
        }
        if (returnType == null)
        {
            if (other.returnType != null)
            {
                return false;
            }
        }
        else if (!returnType.equals(other.returnType))
        {
            return false;
        }
        if (scopeClass == null)
        {
            if (other.scopeClass != null)
            {
                return false;
            }
        }
        else if (!scopeClass.equals(other.scopeClass))
        {
            return false;
        }
        if (serializable != other.serializable)
        {
            return false;
        }
        if (specializedBean != other.specializedBean)
        {
            return false;
        }
        if (stereoTypeClasses == null)
        {
            if (other.stereoTypeClasses != null)
            {
                return false;
            }
        }
        else if (!stereoTypeClasses.equals(other.stereoTypeClasses))
        {
            return false;
        }
        if (stereoTypes == null)
        {
            if (other.stereoTypes != null)
            {
                return false;
            }
        }
        else if (!stereoTypes.equals(other.stereoTypes))
        {
            return false;
        }
        if (webBeansType == null)
        {
            if (other.webBeansType != null)
            {
                return false;
            }
        }
        else if (!webBeansType.equals(other.webBeansType))
        {
            return false;
        }
        return true;
    }

}
