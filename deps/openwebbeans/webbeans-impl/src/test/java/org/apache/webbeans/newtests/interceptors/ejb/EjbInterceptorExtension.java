/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.interceptors.ejb;

import org.apache.webbeans.newtests.interceptors.lifecycle.LifecycleBinding;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.Interceptors;


public class EjbInterceptorExtension implements Extension
{
    /**
     * we add the InterceptorBinding via Extension to test OWB-593
     * @param event
     */
    public void registerInterceptorBinding(@Observes BeforeBeanDiscovery event)
    {
        event.addInterceptorBinding(LifecycleBinding.class);
    }

    public static class InterceptorsLit extends  AnnotationLiteral<Interceptors> implements Interceptors
    {
        public Class[] value()
        {
            return new Class[]{EjbInterceptor.class};
        }
    }

    public void observeNotInterceptedBean(@Observes ProcessAnnotatedType<ManagedBeanWithoutInterceptor> process)
    {
        AnnotationLiteral<Interceptors> intAnnot = new InterceptorsLit();

        process.getAnnotatedType().getAnnotations().add(intAnnot);
        process.setAnnotatedType(process.getAnnotatedType());
    }

}
