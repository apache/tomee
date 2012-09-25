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
package org.apache.webbeans.ejb.common.component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.SessionBeanType;

import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.EnterpriseBeanMarker;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.util.ClassUtil;

/**
 * Defines bean contract for the session beans.
 * 
 * @version $Rev$ $Date$
 */
public abstract class BaseEjbBean<T> extends AbstractInjectionTargetBean<T> implements EnterpriseBeanMarker
{
    public static final Object[] OBJECT_EMPTY = new Object[0];

    /**Session bean type*/
    protected final SessionBeanType ejbType;
    
    /** Map of proxy instances to the dependent SFSB they've acquired but not yet removed */
    private Map<Object, Object> dependentSFSBToBeRemoved = new ConcurrentHashMap<Object, Object>();

    /**
     * Creates a new instance of the session bean.
     * @param webBeansContext
     * @param ejbClassType ebj class type
     */
    public BaseEjbBean(Class<T> ejbClassType, SessionBeanType type, WebBeansContext webBeansContext)
    {
        super(WebBeansType.ENTERPRISE,ejbClassType, webBeansContext);
        
        //type of the ejb
        this.ejbType = type;
        
        //Setting inherited meta data instance
        setInheritedMetaData();
    }


    /* (non-Javadoc)
     * @see org.apache.webbeans.component.AbstractBean#isPassivationCapable()
     */
    @Override
    public boolean isPassivationCapable()
    {
        if(getEjbType().equals(SessionBeanType.STATEFUL))
        {
            return true;
        }
        
        return false;
    }

    /**
     * Inject session bean injected fields. It is called from
     * interceptor.
     * @param instance bean instance
     * @param creationalContext creational context instance
     */
    @SuppressWarnings("unchecked")
    public void injectFieldInInterceptor(Object instance, CreationalContext<?> creationalContext)
    {
        super.injectFields((T)instance, (CreationalContext<T>)creationalContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected T createComponentInstance(CreationalContext<T> creationalContext)
    {
        return getInstance(creationalContext);
    }
    
    /**
     * Sublclasses must return instance.
     * @param creationalContext creational context
     * @return instance
     */
    protected abstract T getInstance(CreationalContext<T> creationalContext);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void destroyComponentInstance(T instance, CreationalContext<T> creational)
    {
        if (getEjbType().equals(SessionBeanType.STATEFUL))
        {
            if ((this.getScope() == Dependent.class))
            {
                try
                {
                    Object ejbInstance = getDependentSFSBForProxy(instance);
                    if (ejbInstance != null)
                    {
                        destroyStatefulSessionBeanInstance(instance, ejbInstance);
                    }
                }
                finally
                {
                    removeDependentSFSB(instance);
                }
            }
            else // normal scope  
            { 
                // The EjbBeanProxyHandler may not have actually obtained an EJB for this normal-scope Bean.
                Context webbeansContext = WebBeansContext.getInstance().getBeanManagerImpl().getContext(this.getScope());
                Object ejbInstance = webbeansContext.get(this);
                if (ejbInstance != null)
                {
                    destroyStatefulSessionBeanInstance(instance, ejbInstance);
                }
            }
        }
    }

    /**
     * Called when we must ask the container to remove a specific
     * @param proxyInstance The contextual reference 
     * @param ejbInstance The underlying EJB instance to be removed
     */
    protected void destroyStatefulSessionBeanInstance(T proxyInstance, Object ejbInstance)
    {
        Method removeMeth = null;
        for (Method m : getRemoveMethods())
        {   
            // TODO FIXME: This needs to call an API from the EJB
            // container to remove the EJB instance directly,
            // not via a remove method.  For now, just call 1 
            // remove method directly on the EJB
            try 
            { 
                removeMeth = proxyInstance.getClass().getMethod(m.getName(), m.getParameterTypes());
                ClassUtil.callInstanceMethod(removeMeth, proxyInstance, OBJECT_EMPTY);
            }
            catch (NoSuchMethodException e) 
            {
                getLogger().log(Level.SEVERE, "Error calling Stateful Session Bean remove method: ", e);
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * Subclasses can override this.
     * @return remove methods
     */
    public List<Method> getRemoveMethods()
    {
        return null;
    }
    
    /**
     * Subclasses must override this to return local interfaces.
     * @return local business interfaces.
     */
    public List<Class<?>> getBusinessLocalInterfaces()
    {
        return null;
    }
    
    /**
     * Subclasses must override this to return ejb name
     * @return ejb name
     */    
    public String getEjbName()
    {
        return null;
    }
    
    /**
     * Gets ejb session type.
     * @return type of the ejb
     */
    public SessionBeanType getEjbType()
    {
        return this.ejbType;
    }
    
    /**
     * Keep track of which proxies have gotten EJB objects out of a context
     * @param dependentSFSB The dependent SFSB acquired from the EJB container
     * @param proxy The OWB proxy instance whose method handler acquired the dependnet SFSB
     */
    public void addDependentSFSB(Object dependentSFSB, Object proxy) 
    { 
        dependentSFSBToBeRemoved.put(proxy, dependentSFSB);
    }
    
    /**
     * Call after observing an @Remove method on an EJB instance
     * @param proxy the proxy instance the dependent SFSB is associated with
     */
    public void removeDependentSFSB(Object proxy) 
    { 
        dependentSFSBToBeRemoved.remove(proxy);
    }
    
    /**
     * 
     * @param proxy an instance of our own proxy
     * @return the underlying EJB instance associated with the proxy
     */
    public Object getDependentSFSBForProxy(Object proxy) 
    { 
        return dependentSFSBToBeRemoved.get(proxy);
    }
    
    /**
     * 
     * @return true if the bean supports a no-interface (bean-class) local view but 
     * the container does not return it in the list of business local interfaces.
     */
    public boolean needsBeanLocalViewAddedToTypes()
    {
        return false;
    }

}