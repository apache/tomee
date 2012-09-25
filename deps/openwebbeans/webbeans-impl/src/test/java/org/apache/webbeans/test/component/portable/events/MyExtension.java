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
package org.apache.webbeans.test.component.portable.events;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;

/**
 * Test extension which only sets a few static members which will be validated in a test case.
 */
public class MyExtension implements Extension
{
    public static ProcessAnnotatedType<?>     processAnnotatedTypeEvent;
    public static BeforeBeanDiscovery         lastBeforeBeanDiscovery;
    public static AfterBeanDiscovery          lastAfterBeanDiscovery;
    public static BeforeShutdown              beforeShutdownEvent;
    public static AfterDeploymentValidation   afterDeploymentValidation;
    public static ProcessInjectionTarget<?>   processInjectionTarget;
    public static ProcessProducer<?,?>        processProducer;
    public static ProcessBean<?>              processBean;
    public static ProcessObserverMethod<?, ?> processObserverMethod;
    
    
    /**
     * Reset all static fields before the test starts
     */
    public static void reset() {
        processAnnotatedTypeEvent = null;
        lastBeforeBeanDiscovery = null;
        lastAfterBeanDiscovery = null;
        beforeShutdownEvent = null;
        afterDeploymentValidation = null;
        processInjectionTarget = null;
        processProducer = null;
        processBean = null;
        processObserverMethod = null;
    }
    
    public MyExtension()
    {
    }

    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBean) 
    {
        lastBeforeBeanDiscovery = beforeBean;
    }
    
    public void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery afterBean, BeanManager beanManager) 
    {
        lastAfterBeanDiscovery = afterBean;
    }

    public void observeAfterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager bm) 
    {
        afterDeploymentValidation = adv;
    }

    public void observeShutdownEvent(@Observes BeforeShutdown bs, BeanManager bm) 
    {
        beforeShutdownEvent = bs;
    }

    public void observeProcessAnnotatedTypeEvent(@Observes ProcessAnnotatedType<?> annotatedType)
    {
        processAnnotatedTypeEvent = annotatedType;
    }
    
    public <T> void observeProcessInjectionTarget(@Observes ProcessInjectionTarget<T> pit)
    {
        processInjectionTarget = pit;
    }
    
    public <T, X> void observeProcessProducer(@Observes ProcessProducer<T, X> pp)
    {
        processProducer = pp;
    }
    
    public <X> void observeProcessBean(@Observes ProcessBean<X> pb)
    {
        processBean = pb;
    }
    
    public <X, T> void processObserverMethod(@Observes ProcessObserverMethod<X, T> pom)
    {
        processObserverMethod = pom;
    }
    
}
