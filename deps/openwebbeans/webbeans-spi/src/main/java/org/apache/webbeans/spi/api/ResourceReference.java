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
package org.apache.webbeans.spi.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


public class ResourceReference<X,T extends Annotation>
{
    private final T annotation;
    
    private final Class<X> resourceType;
    
    private final Class<?> ownerClass;
    
    private final String name;
    
    private String jndiName;
    
    public ResourceReference(Class<?> ownerClass, String name, Class<X> resourceType, T annotation)
    {
        this.resourceType = resourceType;
        this.annotation = annotation;
        this.ownerClass = ownerClass;
        this.name = name;
    }
    
    public boolean supports(Class<?> resourceAnnoation)
    {
        if(this.annotation.annotationType().equals(resourceAnnoation))
        {
            return true;
        }
        
        return false;
    }
    
    public <Y extends Annotation> Y getAnnotation(Class<Y> resourceAnnoation)
    {
        if(this.annotation.annotationType().equals(resourceAnnoation))
        {
            return resourceAnnoation.cast(this.annotation);
        }
        
        return null;
    }    
    
    public Class<X> getResourceType()
    {
        return this.resourceType;
    }

    /**
     * @return the ownerClass
     */
    public Class<?> getOwnerClass()
    {
        return ownerClass;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getJndiName()
    {
        if(this.jndiName == null)
        {
            this.jndiName = getResourceName();
        }
        
        return normalize(this.jndiName);
    }
    
    private Method getNameMethod(Class cls) 
    {
        try 
        {
            return cls.getMethod("name", null);
        } 
        catch (NoSuchMethodException e) 
        {
            return null;
        }
    }

    private String getResourceName() 
    {
        String value = null;
        Method nameMethod = getNameMethod(this.annotation.getClass());
        if (nameMethod != null) 
        {
            try 
            {
                value = (String) nameMethod.invoke(this.annotation, null);
            } 
            catch (Exception e) 
            {
                // ignore
            }
        }
        
        if(value == null || value.equals(""))
        {
            value = this.ownerClass.getName() + "/" + this.name;
        }
        
        return value;
    }
    
    private String normalize(String refName) 
    {
        if (refName.startsWith("java:")) 
        {
            return refName;
        }
        
        return "java:comp/env/" + refName;
    }
    
    
}
