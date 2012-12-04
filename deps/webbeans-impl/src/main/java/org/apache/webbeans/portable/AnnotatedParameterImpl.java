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

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

/**
 * Implementation of {@link AnnotatedParameter} interface.
 * 
 * @version $Rev: 1182847 $ $Date: 2011-10-13 15:31:37 +0200 (jeu., 13 oct. 2011) $
 *
 * @param <X> declaring class info
 */
class AnnotatedParameterImpl<X> extends AbstractAnnotated implements AnnotatedParameter<X>
{
    /**Declaring callable*/
    private final AnnotatedCallable<X> declaringCallable;
    
    /**Parameter position*/
    private final int position;
    
    AnnotatedParameterImpl(WebBeansContext webBeansContext, Type baseType, AnnotatedCallable<X> declaringCallable, int position)
    {
        super(webBeansContext, baseType);
        this.declaringCallable = declaringCallable;
        this.position = position;
    }

    /**
     * {@inheritDoc}
     */
    public AnnotatedCallable<X> getDeclaringCallable()
    {
        return declaringCallable;
    }

    /**
     * {@inheritDoc}
     */
    public int getPosition()
    {
        return position;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Annotated Parameter");
        builder.append(",");
        builder.append(super.toString()+ ",");
        builder.append("Position : " + position);
        
        return builder.toString();
    }
}
