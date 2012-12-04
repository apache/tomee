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
package org.apache.webbeans.inject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.component.ProducerMethodBean;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.exception.WebBeansException;

@SuppressWarnings("unchecked")
public class InjectableMethods<T> extends AbstractInjectable
{
    /** Injectable method */
    protected Method method;

    /** Bean parent instance that owns the method */
    protected Object instance;
    
    /**If this method is dispose method*/
    private boolean disposable;
    
    /**Used in dispose method, represents produces method parameter instance*/
    private Object producerMethodInstance = null;
    
    private Map<Bean<?>,Object> dependentParameters = new HashMap<Bean<?>, Object>();

    /**
     * Constructs new instance.
     * 
     * @param m injectable method
     * @param instance component instance
     */
    public InjectableMethods(Method m, Object instance, OwbBean<?> owner,CreationalContext<?> creationalContext)
    {
        super(owner,creationalContext);
        method = m;
        this.instance = instance;
        injectionMember = m;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.webbeans.inject.Injectable#doInjection()
     */
    public T doInjection()
    {
        List<InjectionPoint> injectedPoints = getInjectedPoints(method);
        List<Object> list = new ArrayList<Object>();
                
        
        for(int i=0;i<injectedPoints.size();i++)
        {
            for(InjectionPoint point : injectedPoints)
            {                
                AnnotatedParameter<?> parameter = (AnnotatedParameter<?>)point.getAnnotated();
                if(parameter.getPosition() == i)
                {
                    boolean injectionPoint = false;
                    if(injectionOwnerBean instanceof ProducerMethodBean)
                    {
                        if(parameter.getBaseType().equals(InjectionPoint.class))
                        {
                            BeanManager manager = injectionOwnerBean.getWebBeansContext().getBeanManagerImpl();
                            Bean<?> injectionPointBean = manager.getBeans(InjectionPoint.class, new DefaultLiteral()).iterator().next();
                            Object reference = manager.getReference(injectionPointBean, InjectionPoint.class, manager.createCreationalContext(injectionPointBean));

                            dependentParameters.put(injectionPointBean, reference);
                            list.add(reference);
                            
                            injectionPoint = true;
                        }
   
                    }
                    
                    if(!injectionPoint)
                    {
                        if(isDisposable() && parameter.getAnnotation(Disposes.class) != null)
                        {
                            list.add(producerMethodInstance);
                        }
                        else
                        {
                            Object instance = inject(point);
                            InjectionResolver injectionResolver = injectionOwnerBean.getWebBeansContext().getBeanManagerImpl().getInjectionResolver();

                            Bean<?> injectedBean = (Bean<?>) injectionResolver.getInjectionPointBean(point);
                            if(injectedBean.getScope() == Dependent.class)
                            {
                                dependentParameters.put(injectedBean, instance);
                            }

                            list.add(instance);    
                        }                        
                    }
                                        
                    break;
                }
            }
        }        
        
        try
        {
            if (!method.isAccessible())
            {
                injectionOwnerBean.getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(method, true);
            }

            return (T) method.invoke(instance, list.toArray(new Object[list.size()]));

        }
        catch (Exception e)
        {
            throw new WebBeansException(e);
        }
    }
    
    public Map<Bean<?>,Object> getDependentBeanParameters()
    {
        return dependentParameters;
    }

    /**
     * @return the disposable
     */
    private boolean isDisposable()
    {
        return disposable;
    }

    /**
     * @param disposable the disposable to set
     */
    public void setDisposable(boolean disposable)
    {
        this.disposable = disposable;
    }
    
    public void setProducerMethodInstance(Object instance)
    {
        producerMethodInstance = instance;
    }
}
