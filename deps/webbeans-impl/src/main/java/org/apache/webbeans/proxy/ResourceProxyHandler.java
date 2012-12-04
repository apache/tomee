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
package org.apache.webbeans.proxy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.webbeans.component.ResourceBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ResourceInjectionService;

public class ResourceProxyHandler implements InvocationHandler, MethodHandler, Serializable, Externalizable
{
    /**
     * 
     */
    private static final long serialVersionUID = 2608686651845218158L;

    private transient Object actualResource;

    private transient ResourceBean bean;

    public ResourceProxyHandler()
    {
        //DO NOT REMOVE, used by failover and passivation.
    }
    
    public ResourceProxyHandler(ResourceBean bean, Object actualResource)
    {
        this.bean = bean;
        this.actualResource = actualResource;
    }
    
    public Object invoke(Object instance, Method method, Method proceed, Object[] arguments) throws Throwable
    {
        return invoke(instance, method, arguments);
    }

    public Object invoke(Object instance, Method method, Object[] arguments) throws Throwable
    {
        try
        {
            return method.invoke(actualResource, arguments);
        }
        catch (InvocationTargetException e)
        {
            throw e.getTargetException();
        }
    }

    /**
     * When serialized, first try container provided failover service. If the failover service 
     * does not handle the actual instance, the default behavior is:
     * 1. If actual object is serializable, then serialize it directly.
     * 2. If not, serialize the DUMMY_STRING.
     */
    public void writeExternal(ObjectOutput out) throws IOException 
    {        
        // write bean id first
        out.writeObject(bean.getId());
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        ResourceInjectionService resourceInjectionService = webBeansContext.getService(ResourceInjectionService.class);
        resourceInjectionService.writeExternal(bean, actualResource, out);
    }

    /**
     * When deserialized, first try container provided failover service. If the failover service does not 
     * handle the actual instance, the default behavior is:
     * 1. Read the object from the stream,
     * 2. If the object is renote ejb stub, reconnect it.
     * 3. if the object is DUMMY_STRING, invoke ResourceBean.getActualInstance to get a new instance of the resource.
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException 
    {
        String id = (String)in.readObject();
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        bean = (ResourceBean) webBeansContext.getBeanManagerImpl().getPassivationCapableBean(id);
        ResourceInjectionService resourceInjectionService = webBeansContext.getService(ResourceInjectionService.class);
        actualResource = resourceInjectionService.readExternal(bean, in);
    }
}
