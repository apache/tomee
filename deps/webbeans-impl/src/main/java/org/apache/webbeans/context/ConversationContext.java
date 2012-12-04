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
package org.apache.webbeans.context;

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.spi.Contextual;

import org.apache.webbeans.context.creational.BeanInstanceBag;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Conversation context implementation. 
 */
public class ConversationContext extends AbstractContext implements Externalizable
{
    private static final long serialVersionUID = -576054696008715282L;

    /*
    * Constructor
    */
    public ConversationContext()
    {
        super(ConversationScoped.class);
    }

    @Override
    public void setComponentInstanceMap()
    {
        componentInstanceMap = new ConcurrentHashMap<Contextual<?>, BeanInstanceBag<?>>();
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException 
    {
        scopeType = (Class<? extends Annotation>) in.readObject();
        Map<String, BeanInstanceBag<?>> map = (Map<String, BeanInstanceBag<?>>)in.readObject();
        setComponentInstanceMap();
        Iterator<String> it = map.keySet().iterator();
        Contextual<?> contextual = null;
        while(it.hasNext()) 
        {
            String id = it.next();
            if (id != null)
            {
                contextual = org.apache.webbeans.config.WebBeansContext.currentInstance().getBeanManagerImpl().getPassivationCapableBean(id);
            }
            if (contextual != null) 
            {
                componentInstanceMap.put(contextual, map.get(id));
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException 
    {
            out.writeObject(scopeType);
            Iterator<Contextual<?>> it = componentInstanceMap.keySet().iterator();
            Map<String, BeanInstanceBag<?>> map = new HashMap<String, BeanInstanceBag<?>>();
            while(it.hasNext()) 
            {
                Contextual<?>contextual = it.next();
                String id = WebBeansUtil.isPassivationCapable(contextual);
                if (id == null) 
                {
                    throw new NotSerializableException("cannot serialize " + contextual.toString());
                }
                map.put(id, componentInstanceMap.get(contextual));
            }
            out.writeObject(map);
    }
    
}
