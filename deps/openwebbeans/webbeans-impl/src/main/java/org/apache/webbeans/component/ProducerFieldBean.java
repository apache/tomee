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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;

import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Defines the producer field component implementation.
 * 
 * @param <T> Type of the field decleration
 */
public class ProducerFieldBean<T> extends AbstractProducerBean<T> implements IBeanHasParent<T>
{
    /** Producer field that defines the component */
    private Field producerField = null;

    /**
     * Defines the new producer field component.
     * 
     * @param returnType type of the field decleration
     */
    public ProducerFieldBean(InjectionTargetBean<?> ownerComponent, Class<T> returnType)
    {
        super(WebBeansType.PRODUCERFIELD, returnType, ownerComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected T createInstance(CreationalContext<T> creationalContext)
    {
        T instance = null;
        
        instance = createDefaultInstance(creationalContext);
        checkNullInstance(instance);
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
        try
        {
            parentCreational = getManager().createCreationalContext(ownerComponent);
            
            if (!producerField.isAccessible())
            {
                getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(producerField, true);
            }

            if (Modifier.isStatic(producerField.getModifiers()))
            {
                instance = (T) producerField.get(null);
            }
            else
            { 
                parentInstance = getParentInstanceFromContext(parentCreational);
                
                instance = (T) producerField.get(parentInstance);
            }
        }
        catch(Exception e)
        {
            throw new WebBeansException(e);
        }
        finally
        {
            if (ownerComponent.getScope().equals(Dependent.class))
            {
                destroyBean(ownerComponent, parentInstance, parentCreational);
            }
        }

        return instance;

    }

    /**
     * Gets creator field.
     * 
     * @return creator field
     */
    public Field getCreatorField()
    {
        return producerField;
    }

    /**
     * Set producer field.
     * 
     * @param field producer field
     */
    public void setProducerField(Field field)
    {
        producerField = field;
    }

    /**
     * Check null instance.
     * 
     * @param instance bean instance
     */
    protected void checkNullInstance(Object instance)
    {
        String errorMessage = "WebBeans producer field : %s" +
                              " return type in the component implementation class : %s" +
                              " scope must be @Dependent to create null instance";
        WebBeansUtil.checkNullInstance(instance, getScope(), errorMessage, producerField.getName(),
                ownerComponent.getReturnType().getName());
    }

    /**
     * Check scope type passivation controls.
     */
    protected void checkScopeType()
    {
        String errorMessage = "WebBeans producer method : %s" + 
                              " return type in the component implementation class : %s" +
                              " with passivating scope @%s" +
                              " must be Serializable";
        getWebBeansContext().getWebBeansUtil().checkSerializableScopeType(getScope(),
                isSerializable(), errorMessage, producerField.getName(), 
                ownerComponent.getReturnType().getName(), getScope().getName());
    }
    
    @Override
    public boolean isPassivationCapable()
    {
        return isPassivationCapable(producerField.getType(), producerField.getModifiers());
    }
    
    @Override
    public String getId()
    {
        if (passivatingId == null)
        {
            String id = super.getId();
            
            passivatingId = id + "#" + producerField.toGenericString();
        }
        return passivatingId;
    }
}
