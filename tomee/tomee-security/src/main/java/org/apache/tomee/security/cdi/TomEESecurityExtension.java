/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.security.cdi;

import org.apache.tomee.security.identitystore.TomEEDefaultIdentityStore;
import org.apache.tomee.security.identitystore.TomEEIdentityStoreHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import java.util.HashSet;
import java.util.Set;

public class TomEESecurityExtension implements Extension {
    private final Set<AnnotatedType> basicAuthentication = new HashSet<>();

    void processAuthenticationMechanismDefinitions(@Observes final ProcessAnnotatedType<?> processAnnotatedType) {
        final AnnotatedType<?> annotatedType = processAnnotatedType.getAnnotatedType();
        if (annotatedType.isAnnotationPresent(BasicAuthenticationMechanismDefinition.class)) {
            basicAuthentication.add(annotatedType);
        }
    }

    void observeBeforeBeanDiscovery(@Observes final BeforeBeanDiscovery beforeBeanDiscovery,
                                    final BeanManager beanManager) {
        if (basicAuthentication.isEmpty()) {
            beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(DefaultAuthenticationMechanism.class));
            beforeBeanDiscovery.addAnnotatedType(
                    beanManager.createAnnotatedType(TomEESecurityServletAuthenticationMechanismMapper.class));
            beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEDefaultIdentityStore.class));
            beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEIdentityStoreHandler.class));
        }
    }

    void registerAuthenticationMechanism(@Observes final AfterBeanDiscovery afterBeanDiscovery,
                                         final BeanManager beanManager) {
        if (!basicAuthentication.isEmpty()) {
            afterBeanDiscovery.addBean()
               .id(BasicAuthenticationMechanism.class.getName())
               .beanClass(BasicAuthenticationMechanism.class)
               .types(Object.class, HttpAuthenticationMechanism.class, BasicAuthenticationMechanism.class)
               .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
               .scope(ApplicationScoped.class)
               .createWith((CreationalContext<BasicAuthenticationMechanism> creationalContext) -> {
                   AnnotatedType<BasicAuthenticationMechanism> annotatedType =
                           beanManager.createAnnotatedType(BasicAuthenticationMechanism.class);
                   BeanAttributes<BasicAuthenticationMechanism> beanAttributes =
                           beanManager.createBeanAttributes(annotatedType);
                   return beanManager.createBean(beanAttributes, BasicAuthenticationMechanism.class,
                                                 beanManager.getInjectionTargetFactory(annotatedType))
                                     .create(creationalContext);
               });
        }
    }

    public boolean hasAuthenticationMechanisms() {
        return !basicAuthentication.isEmpty();
    }
}
