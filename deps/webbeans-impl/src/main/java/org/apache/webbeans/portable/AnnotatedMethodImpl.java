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

import org.apache.webbeans.config.WebBeansContext;

import java.lang.reflect.Method;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * Implementation of {@link AnnotatedMethod} interface.
 * 
 * @version $Rev: 1182780 $ $Date: 2011-10-13 13:11:03 +0200 (jeu., 13 oct. 2011) $
 *
 * @param <X> class info
 */
class AnnotatedMethodImpl<X> extends AbstractAnnotatedCallable<X> implements AnnotatedMethod<X>
{

    /**
     * Create a ew instance.
     * 
     * @param declaringType declaring type
     * @param javaMember method
     */
    AnnotatedMethodImpl(WebBeansContext webBeansContext, Method javaMember,AnnotatedType<X> declaringType)
    {        
        super(webBeansContext, javaMember.getGenericReturnType(), javaMember,declaringType);
        setAnnotations(javaMember.getDeclaredAnnotations());
        setAnnotatedParameters(javaMember.getGenericParameterTypes(), javaMember.getParameterAnnotations());
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Method getJavaMember()
    {
        return Method.class.cast(javaMember);
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Annotated Method,");
        builder.append(super.toString());
        
        return builder.toString();
    }
    
}
