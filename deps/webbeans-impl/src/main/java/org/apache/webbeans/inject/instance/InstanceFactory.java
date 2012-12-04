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
package org.apache.webbeans.inject.instance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;

import org.apache.webbeans.config.WebBeansContext;

public final class InstanceFactory
{
    private InstanceFactory()
    {
        
    }
    
    /**
     *
     * @param injectedType injection class type
     * @param injectionPointClass null or the class of the injection point
     * @param webBeansContext
     * @param creationalContext will get used for creating &#064;Dependent beans
     * @param ownerInstance the object the current Instance got injected into
     * @param annotations qualifier annotations
     * @return the {@link Instance<T>} for the given type.
     */
    public static <T> Instance<T> getInstance(Type injectedType, Class<?> injectionPointClass,
                                              WebBeansContext webBeansContext, CreationalContext<?> creationalContext,
                                              Object ownerInstance, Annotation... annotations)
    {
        InstanceImpl<T> instance = new InstanceImpl<T>(injectedType,injectionPointClass, webBeansContext,
                                                       creationalContext, ownerInstance, annotations);

        return instance;
    }

}
