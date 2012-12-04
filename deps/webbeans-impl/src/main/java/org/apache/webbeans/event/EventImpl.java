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
package org.apache.webbeans.event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.util.TypeLiteral;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.OwbCustomObjectInputStream;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Event implementation.
 * 
 * @param <T> event type
 * @see Event
 */
public class EventImpl<T> implements Event<T>, Serializable
{
    private static final long serialVersionUID = -9035218380365451350L;
    
    /**Event binding types*/
    private Annotation[] injectedBindings;

    /**Event types*/
    private Type eventType;

    private transient WebBeansContext webBeansContext;

    /**
     * Creates a new event.
     * 
     * @param injectedBindings event bindings
     * @param eventType event type
     * @param webBeansContext
     */
    public EventImpl(Annotation[] injectedBindings, Type eventType, WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        this.injectedBindings = injectedBindings;
        this.eventType = eventType;
    }

    /**
     * Fires event with given event object.
     */
    public void fire(T event)
    {
        webBeansContext.getBeanManagerImpl().fireEvent(event, injectedBindings);
    }

    /**
     * Returns total binding annotations.
     * 
     * @param annotations new annotations
     * @return total binding annotations
     */
    private Annotation[] getEventBindings(Annotation... annotations)
    {
        webBeansContext.getAnnotationManager().checkQualifierConditions(annotations);

        Set<Annotation> eventBindings = new HashSet<Annotation>();
        
        for(Annotation ann : injectedBindings)
        {
            eventBindings.add(ann);
        }
        
        Annotation[] anns = null;

        for (Annotation binding : annotations)
        {
            eventBindings.add(binding);
        }

        anns = new Annotation[eventBindings.size()];
        anns = eventBindings.toArray(anns);

        return anns;

    }

    /**
     * {@inheritDoc}
     */
    public Event<T> select(Annotation... bindings)
    {
        Event<T> sub = new EventImpl<T>(getEventBindings(bindings), eventType, webBeansContext);
        
        return sub;
    }
    
    /**
     * {@inheritDoc}
     */
    public <U extends T> Event<U> select(Class<U> subtype, Annotation... bindings)
    {
        if(ClassUtil.isDefinitionContainsTypeVariables(subtype))
        {
            throw new IllegalArgumentException("Class : " + subtype + " cannot contain type variable");
        }
        
        Type sub = subtype;
        
        if(sub == null)
        {
            sub = eventType;
        }
        
        Event<U> subEvent = new EventImpl<U>(getEventBindings(bindings),sub, webBeansContext);
        
        return subEvent;
    }
    
    /**
     * {@inheritDoc}
     */
    public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... bindings)
    {
        return select(subtype.getRawType(), bindings);
    }
    
    private void writeObject(java.io.ObjectOutputStream op) throws IOException
    {
        ObjectOutputStream oos = new ObjectOutputStream(op);
        oos.writeObject(eventType);
        oos.writeObject(injectedBindings);
        
        oos.flush();
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        final ObjectInputStream inputStream = new OwbCustomObjectInputStream(in, WebBeansUtil.getCurrentClassLoader());
        eventType = (Type)inputStream.readObject();
        injectedBindings = (Annotation[])inputStream.readObject();

        webBeansContext = WebBeansContext.currentInstance();
    }
}
