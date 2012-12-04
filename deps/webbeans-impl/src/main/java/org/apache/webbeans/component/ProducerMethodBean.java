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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.context.creational.DependentCreationalContext;
import org.apache.webbeans.context.creational.DependentCreationalContext.DependentType;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.inject.InjectableMethods;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Concrete implementation of the {@link AbstractOwbBean}.
 * <p>
 * It is defined as producer method component.
 * </p>
 * 
 * @version $Rev: 1363131 $ $Date: 2012-07-18 23:39:39 +0200 (mer., 18 juil. 2012) $
 */
public class ProducerMethodBean<T> extends AbstractProducerBean<T>
{
    /** Creator method of the parent component */
    protected Method creatorMethod;

    /** Disposal method */
    protected Method disposalMethod;

    /**
     * Creates a new instance.
     * 
     * @param parent parent bean
     * @param returnType producer method return type
     */
    public ProducerMethodBean(InjectionTargetBean<?> parent, Class<T> returnType)
    {
        super(WebBeansType.PRODUCERMETHOD, returnType, parent);
    }

    /**
     * Gets the creator method.
     * 
     * @return producer method
     */
    public Method getCreatorMethod()
    {
        return creatorMethod;
    }

    /**
     * Sets the method.
     * 
     * @param creatorMethod producer method
     */
    public void setCreatorMethod(Method creatorMethod)
    {
        this.creatorMethod = creatorMethod;
    }

    /**
     * Sets the disposal method.
     * 
     * @param disposalMethod disposal method of this producer method component
     */
    public void setDisposalMethod(Method disposalMethod)
    {
        if (this.disposalMethod != null)
        {
            throw new WebBeansConfigurationException("There are multiple disposal method for producer method " +
                    "component with name : " + getName() + " with implementation class " +
                    getParent().getReturnType().getName() + " with disposal method name : " +
                    disposalMethod.getName());
        }
        this.disposalMethod = disposalMethod;
    }

    public String getId()
    {
        if (passivatingId == null)
        {
            String id = super.getId();
            
            passivatingId = id + "#" + creatorMethod.toGenericString();
        }
        return passivatingId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected T createInstance(CreationalContext<T> creationalContext)
    {
        T instance = null;
        instance = createDefaultInstance(creationalContext);
        // Check null instance
        checkNullInstance(instance);

        // Check scope type
        checkScopeType();
        return instance;
    }

    /**
     * Default producer method creation.
     * 
     * @param creationalContext creational context
     * @return producer method instance
     */
    @SuppressWarnings("unchecked")
    protected T createDefaultInstance(CreationalContext<T> creationalContext)
    {
        T instance = null;
        Object parentInstance = null;
        CreationalContext<?> parentCreational = null;
        InjectableMethods<T> m = null;
        List<DependentCreationalContext<Object>> oldDependents =
                AbstractInjectable.dependentInstanceOfProducerMethods.get();

        try
        {
            //X TODO dependentInstanceOfProducerMethods MUST NOT be public! 
            AbstractInjectable.dependentInstanceOfProducerMethods.set(new ArrayList<DependentCreationalContext<Object>>());
            parentCreational = getManager().createCreationalContext(ownerComponent);
            
            if (!Modifier.isStatic(creatorMethod.getModifiers()))
            {
                parentInstance = getParentInstance(parentCreational);
            }

            m = new InjectableMethods<T>(creatorMethod, parentInstance, this, creationalContext);
            //Injection of parameters
            instance = m.doInjection();
            
            boolean isInjectionToAnotherBean = false;
            Contextual<?> contextual = null; 
            if(creationalContext instanceof CreationalContextImpl)
            {
                contextual =  ((CreationalContextImpl)creationalContext).getBean();
                isInjectionToAnotherBean = contextual == this ? false : true;
            }
            
            ThreadLocal<Object> injectionTargetInstance = AbstractInjectable.instanceUnderInjection;
            if(isInjectionToAnotherBean)
            {
                if(oldDependents != null)
                {
                    DependentCreationalContext<Object> dependentCreational =
                            new DependentCreationalContext<Object>((Contextual<Object>)this);
                    dependentCreational.setInstance(instance);
                    dependentCreational.setDependentType(DependentType.BEAN);

                    oldDependents.add(dependentCreational);
                }
            }
            else
            {
                List<DependentCreationalContext<Object>> dependents =
                        AbstractInjectable.dependentInstanceOfProducerMethods.get();
                if(dependents != null)
                {
                    for(int i = 0, size = dependents.size(); i < size; i++)
                    {
                        DependentCreationalContext<Object> dependent = dependents.get(i);
                        ((CreationalContextImpl)creationalContext).
                                addDependent(instance, dependent.getContextual(), dependent.getInstance());
                    }
                }
            }
            
            //Adding dependents of producers
            Map<Bean<?>, Object> dependents = m.getDependentBeanParameters();
            if(dependents != null)
            {
                Set<Bean<?>> beans = dependents.keySet();
                for(Bean<?> bean : beans)
                {
                    if(creationalContext instanceof CreationalContextImpl)
                    {
                        if(isInjectionToAnotherBean)
                        {
                            if(oldDependents == null)
                            {
                                if (injectionTargetInstance != null && injectionTargetInstance.get() != null)
                                {
                                    ((CreationalContextImpl)creationalContext)
                                            .addDependent(injectionTargetInstance.get(), this , instance);
                                }
                            }
                            else
                            {
                                DependentCreationalContext<Object> dependentCreational =
                                        new DependentCreationalContext<Object>((Contextual<Object>)bean);
                                dependentCreational.setInstance(dependents.get(bean));
                                dependentCreational.setDependentType(DependentType.BEAN);
                                oldDependents.add(dependentCreational);                                                            
                            }
                        }
                        else
                        {
                            ((CreationalContextImpl)creationalContext).addDependent(instance, bean ,
                                                                                    dependents.get(bean));   
                        }
                    }
                }
            }
        }
        finally
        {
            if (getParent().getScope().equals(Dependent.class))
            {
                // this handles dependent scoped producer methods and fields
                destroyBean(getParent(), parentInstance, parentCreational);
            }
            
            if(oldDependents != null)
            {
                AbstractInjectable.dependentInstanceOfProducerMethods.set(oldDependents);   
            }
            else
            {
                AbstractInjectable.dependentInstanceOfProducerMethods.set(null);
                AbstractInjectable.dependentInstanceOfProducerMethods.remove();
            }
        }

        return instance;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void destroyInstance(T instance, CreationalContext<T> creationalContext)
    {
        dispose(instance,creationalContext);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose(T instance, CreationalContext<T> creationalContext)
    {
        disposeDefault(instance, creationalContext);
    }

    /**
     * Default dispose method used.
     * 
     * @param instance bean instance
     */
    @SuppressWarnings("unchecked")
    protected void disposeDefault(T instance, CreationalContext<T> creationalContext)
    {
        if (disposalMethod != null)
        {
            Object parentInstance = null;
            CreationalContext<?> parentCreational = null;
            InjectableMethods<T> m = null;
            try
            {
                parentCreational = getManager().createCreationalContext(ownerComponent);
                
                if (!Modifier.isStatic(disposalMethod.getModifiers()))
                {
                    parentInstance = getParentInstance(parentCreational);
                }

                m = new InjectableMethods<T>(disposalMethod, parentInstance, ownerComponent, creationalContext);
                m.setDisposable(true);
                m.setProducerMethodInstance(instance);

                m.doInjection();

            }
            finally
            {
                if (getParent().getScope().equals(Dependent.class))
                {
                    destroyBean(getParent(), parentInstance, parentCreational);
                }

                //Destroy dependent parameters
                Map<Bean<?>, Object> dependents = m.getDependentBeanParameters();
                if(dependents != null)
                {
                    Set<Bean<?>> beans = dependents.keySet();
                    for(Bean<?> bean : beans)
                    {
                        Bean<Object> beanTt = (Bean<Object>)bean;
                        if(creationalContext instanceof CreationalContextImpl)
                        {
                            beanTt.destroy(dependents.get(beanTt), (CreationalContext<Object>)creationalContext);
                        }
                    }
                }
                
            }
        }
    }
    
    /**
     * Check null control.
     * 
     * @param instance bean instance
     */
    protected void checkNullInstance(Object instance)
    {
        String errorMessage = "WebBeans producer method : %s" +
                              " return type in the component implementation class : %s" +
                              " scope type must be @Dependent to create null instance";
        WebBeansUtil.checkNullInstance(instance, getScope(), errorMessage, creatorMethod.getName(),
                ownerComponent.getReturnType().getName());
    }

    /**
     * Check passivation check.
     */
    protected void checkScopeType()
    {
        String errorMessage = "WebBeans producer method : %s" +
                              " return type in the component implementation class : %s" +
                              " with passivating scope @%s" +
                              " must be Serializable";
        getWebBeansContext().getWebBeansUtil().checkSerializableScopeType(getScope(),
                isSerializable(), errorMessage, creatorMethod.getName(), ownerComponent.getReturnType().getName(),
                getScope().getName());

    }

    @Override
    public boolean isPassivationCapable()
    {
        return isPassivationCapable(creatorMethod.getReturnType(), creatorMethod.getModifiers());
    }
}
