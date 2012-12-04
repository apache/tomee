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
package org.apache.webbeans.portable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.apache.webbeans.config.WebBeansContext;

/**
 * Implementation of {@link AnnotatedMember} interface.
 * 
 * @version $Rev: 1182847 $ $Date: 2011-10-13 15:31:37 +0200 (jeu., 13 oct. 2011) $
 *
 * @param <X> declaring class
 */
abstract class AbstractAnnotatedMember<X> extends AbstractAnnotated implements AnnotatedMember<X>
{
    /**Annotated type that owns this member*/
    private final AnnotatedType<X> declaringType;
    
    /**Member type*/
    protected final Member javaMember;
    
    @SuppressWarnings("unchecked")
    AbstractAnnotatedMember(WebBeansContext webBeansContext, Type baseType, Member javaMember, AnnotatedType<X> declaringType)
    {
        super(webBeansContext, baseType);
        
        this.javaMember = javaMember;
        
        if(declaringType == null)
        {
            this.declaringType = (AnnotatedType<X>) getWebBeansContext().getAnnotatedElementFactory().newAnnotatedType(this.javaMember.getDeclaringClass());
            
            AnnotatedTypeImpl<X> impl = (AnnotatedTypeImpl<X>)this.declaringType;
            
            if(this.javaMember instanceof Constructor)
            {
                impl.addAnnotatedConstructor((AnnotatedConstructor<X>)this);
            }
            
            else if(this.javaMember instanceof Method)
            {
                impl.addAnnotatedMethod((AnnotatedMethod<X>)this);
            }
            
            else if(this.javaMember instanceof Field)
            {
                impl.addAnnotatedField((AnnotatedField<X>)this);
            }                
        }
        else
        {
            this.declaringType = declaringType;
        }
    }
   /**
     * {@inheritDoc}
     */    
    public AnnotatedType<X> getDeclaringType()
    {
        return declaringType;
    }

    /**
     * {@inheritDoc}
     */    
    public Member getJavaMember()
    {
        return javaMember;
    }

    /**
     * {@inheritDoc}
     */    
    public boolean isStatic()
    {
        return Modifier.isStatic(javaMember.getModifiers());
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append(",");
        builder.append("Java Member Name : " + javaMember.getName());
        
        return builder.toString();
    }
}
