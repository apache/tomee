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
package org.apache.webbeans.inject;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Injects dependencies of the given Java EE component
 * instance.
 * 
 * @version $Rev$ $Date$
 *
 */
public final class OWBInjector
{
    private OWBInjector()
    {
        //No operation
    }

    /**
     * Inject dependencies of given instance.
     * @param beanManager the BeanManager to use
     * @param instanceUnderInjection instance
     * @param ownerCreationalContext CreationalContext of the owner
     * @return this injector
     * @throws Exception if exception occurs
     */
    @SuppressWarnings("unchecked")
    public static void inject(BeanManager beanManager, Object instanceUnderInjection, CreationalContext<?> ownerCreationalContext)
            throws Exception
    {
        CreationalContext<?> creationalContext = ownerCreationalContext;
        if(creationalContext == null)
        {
            creationalContext = beanManager.createCreationalContext(null);
        }

        AnnotatedType annotatedType = beanManager.createAnnotatedType(instanceUnderInjection.getClass());
        beanManager.createInjectionTarget(annotatedType).inject(instanceUnderInjection, creationalContext);
    }


}
