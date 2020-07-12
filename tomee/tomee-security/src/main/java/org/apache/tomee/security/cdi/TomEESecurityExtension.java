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

import org.apache.tomee.security.TomEEELInvocationHandler;
import org.apache.tomee.security.TomEEPbkdf2PasswordHash;
import org.apache.tomee.security.TomEEPlaintextPasswordHash;
import org.apache.tomee.security.TomEESecurityContext;
import org.apache.tomee.security.identitystore.TomEEDatabaseIdentityStore;
import org.apache.tomee.security.identitystore.TomEEIdentityStoreHandler;
import org.apache.tomee.security.identitystore.TomEELDAPIdentityStore;

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
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.TypeLiteral;
import javax.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class TomEESecurityExtension implements Extension {

    private final Set<AnnotatedType> basicAuthentication = new HashSet<>();
    private final Set<AnnotatedType> formAuthentication = new HashSet<>();
    private final Set<AnnotatedType> customAuthentication = new HashSet<>();

    private final Set<AnnotatedType> databaseIdentityStore = new HashSet<>();
    private final Set<AnnotatedType> ldapIdentityStore = new HashSet<>();

    void observeBeforeBeanDiscovery(
        @Observes final BeforeBeanDiscovery beforeBeanDiscovery,
        final BeanManager beanManager) {

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(DefaultAuthenticationMechanism.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEESecurityServletAuthenticationMechanismMapper.class));
        // beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEDefaultIdentityStore.class)); // only if at least idstore was found?
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEIdentityStoreHandler.class));

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEPbkdf2PasswordHash.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEPlaintextPasswordHash.class));

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(AutoApplySessionInterceptor.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(RememberMeInterceptor.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(LoginToContinueInterceptor.class));

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEESecurityContext.class));
    }

    void processIdentityStores(
        @Observes
        @WithAnnotations({
                             DatabaseIdentityStoreDefinition.class,
                             LdapIdentityStoreDefinition.class
                         }) final ProcessAnnotatedType<?> processAnnotatedType) {

        final AnnotatedType<?> annotatedType = processAnnotatedType.getAnnotatedType();

        if (annotatedType.isAnnotationPresent(DatabaseIdentityStoreDefinition.class)) {
            databaseIdentityStore.add(annotatedType);
        }

        if (annotatedType.isAnnotationPresent(LdapIdentityStoreDefinition.class)) {
            ldapIdentityStore.add(annotatedType);
        }

    }

    void processAuthenticationMechanismDefinitions(
        @Observes
        @WithAnnotations({
                             BasicAuthenticationMechanismDefinition.class,
                             FormAuthenticationMechanismDefinition.class,
                             CustomFormAuthenticationMechanismDefinition.class
                         }) final ProcessAnnotatedType<?> processAnnotatedType) {

        final AnnotatedType<?> annotatedType = processAnnotatedType.getAnnotatedType();

        if (annotatedType.isAnnotationPresent(BasicAuthenticationMechanismDefinition.class)) {
            basicAuthentication.add(annotatedType);
        }

        if (annotatedType.isAnnotationPresent(FormAuthenticationMechanismDefinition.class)) {
            formAuthentication.add(annotatedType);
        }

        if (annotatedType.isAnnotationPresent(CustomFormAuthenticationMechanismDefinition.class)) {
            customAuthentication.add(annotatedType);
        }
    }

    void registerAuthenticationMechanism(
        @Observes final AfterBeanDiscovery afterBeanDiscovery,
        final BeanManager beanManager) {

        if (!databaseIdentityStore.isEmpty()) {
            afterBeanDiscovery
                .addBean()
                .id(TomEEDatabaseIdentityStore.class.getName() + "#" + DatabaseIdentityStoreDefinition.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<DatabaseIdentityStoreDefinition>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createDatabaseIdentityStoreDefinitionSupplier(beanManager));

            afterBeanDiscovery
                .addBean()
                .id(TomEEDatabaseIdentityStore.class.getName())
                .beanClass(TomEEDatabaseIdentityStore.class)
                .types(Object.class, IdentityStore.class, TomEEDatabaseIdentityStore.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith((CreationalContext<TomEEDatabaseIdentityStore> creationalContext) -> {
                    final AnnotatedType<TomEEDatabaseIdentityStore> annotatedType =
                        beanManager.createAnnotatedType(TomEEDatabaseIdentityStore.class);
                    final BeanAttributes<TomEEDatabaseIdentityStore> beanAttributes =
                        beanManager.createBeanAttributes(annotatedType);
                    return beanManager.createBean(beanAttributes, TomEEDatabaseIdentityStore.class,
                                                  beanManager.getInjectionTargetFactory(annotatedType))
                                      .create(creationalContext);
                });
        }

        if (!ldapIdentityStore.isEmpty()) {
            afterBeanDiscovery
                .addBean()
                .id(TomEELDAPIdentityStore.class.getName() + "#" + LdapIdentityStoreDefinition.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<LdapIdentityStoreDefinition>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createLdapIdentityStoreDefinitionSupplier(beanManager));

            afterBeanDiscovery
                .addBean()
                .id(TomEELDAPIdentityStore.class.getName())
                .beanClass(TomEELDAPIdentityStore.class)
                .types(Object.class, IdentityStore.class, TomEELDAPIdentityStore.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith((CreationalContext<TomEELDAPIdentityStore> creationalContext) -> {
                    final AnnotatedType<TomEELDAPIdentityStore> annotatedType =
                        beanManager.createAnnotatedType(TomEELDAPIdentityStore.class);
                    final BeanAttributes<TomEELDAPIdentityStore> beanAttributes =
                        beanManager.createBeanAttributes(annotatedType);
                    return beanManager.createBean(beanAttributes, TomEELDAPIdentityStore.class,
                                                  beanManager.getInjectionTargetFactory(annotatedType))
                                      .create(creationalContext);
                });
        }

        if (!basicAuthentication.isEmpty()) {
            afterBeanDiscovery
                .addBean()
                .id(BasicAuthenticationMechanism.class.getName())
                .beanClass(BasicAuthenticationMechanism.class)
                .types(Object.class, HttpAuthenticationMechanism.class, BasicAuthenticationMechanism.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith((CreationalContext<BasicAuthenticationMechanism> creationalContext) -> {
                    final AnnotatedType<BasicAuthenticationMechanism> annotatedType =
                        beanManager.createAnnotatedType(BasicAuthenticationMechanism.class);
                    final BeanAttributes<BasicAuthenticationMechanism> beanAttributes =
                        beanManager.createBeanAttributes(annotatedType);
                    return beanManager.createBean(beanAttributes, BasicAuthenticationMechanism.class,
                                                  beanManager.getInjectionTargetFactory(annotatedType))
                                      .create(creationalContext);
                });

        }

        if (!formAuthentication.isEmpty()) {
            afterBeanDiscovery
                .addBean()
                .id(FormAuthenticationMechanism.class.getName() + "#" + LoginToContinue.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<LoginToContinue>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createFormLoginToContinueSupplier(beanManager));

            afterBeanDiscovery
                .addBean()
                .id(FormAuthenticationMechanism.class.getName())
                .beanClass(FormAuthenticationMechanism.class)
                .types(Object.class, HttpAuthenticationMechanism.class, FormAuthenticationMechanism.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith((CreationalContext<FormAuthenticationMechanism> creationalContext) -> {
                    final AnnotatedType<FormAuthenticationMechanism> annotatedType =
                        beanManager.createAnnotatedType(FormAuthenticationMechanism.class);
                    final BeanAttributes<FormAuthenticationMechanism> beanAttributes =
                        beanManager.createBeanAttributes(annotatedType);
                    return beanManager.createBean(beanAttributes, FormAuthenticationMechanism.class,
                                                  beanManager.getInjectionTargetFactory(annotatedType))
                                      .create(creationalContext);
                });


        }

        if (!customAuthentication.isEmpty()) {
            afterBeanDiscovery
                .addBean()
                .id(CustomFormAuthenticationMechanism.class.getName() + "#" + LoginToContinue.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<LoginToContinue>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createCustomFormLoginToContinueSupplier(beanManager));

            afterBeanDiscovery
                .addBean()
                .id(CustomFormAuthenticationMechanism.class.getName())
                .beanClass(CustomFormAuthenticationMechanism.class)
                .types(Object.class, HttpAuthenticationMechanism.class, CustomFormAuthenticationMechanism.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith((CreationalContext<CustomFormAuthenticationMechanism> creationalContext) -> {
                    final AnnotatedType<CustomFormAuthenticationMechanism> annotatedType =
                        beanManager.createAnnotatedType(CustomFormAuthenticationMechanism.class);
                    final BeanAttributes<CustomFormAuthenticationMechanism> beanAttributes =
                        beanManager.createBeanAttributes(annotatedType);
                    return beanManager.createBean(beanAttributes, CustomFormAuthenticationMechanism.class,
                                                  beanManager.getInjectionTargetFactory(annotatedType))
                                      .create(creationalContext);
                });
        }

    }

    public boolean hasAuthenticationMechanisms() {
        return (basicAuthentication.size() + formAuthentication.size() + customAuthentication.size()) > 0;
    }

    private Supplier<LoginToContinue> createFormLoginToContinueSupplier(final BeanManager beanManager) {
        return () -> {
            final LoginToContinue loginToContinue = formAuthentication.iterator()
                                                                      .next()
                                                                      .getAnnotation(
                                                                          FormAuthenticationMechanismDefinition.class)
                                                                      .loginToContinue();

            return TomEEELInvocationHandler.of(LoginToContinue.class, loginToContinue, beanManager);
        };
    }

    private Supplier<LoginToContinue> createCustomFormLoginToContinueSupplier(final BeanManager beanManager) {
        return () -> {
            final LoginToContinue loginToContinue = customAuthentication.iterator()
                                                                        .next()
                                                                        .getAnnotation(
                                                                            CustomFormAuthenticationMechanismDefinition.class)
                                                                        .loginToContinue();

            return TomEEELInvocationHandler.of(LoginToContinue.class, loginToContinue, beanManager);
        };
    }

    private Supplier<DatabaseIdentityStoreDefinition> createDatabaseIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final DatabaseIdentityStoreDefinition annotation = databaseIdentityStore.iterator()
                                                                                    .next()
                                                                                    .getAnnotation(
                                                                                        DatabaseIdentityStoreDefinition.class);

            return TomEEELInvocationHandler.of(DatabaseIdentityStoreDefinition.class, annotation, beanManager);
        };
    }

    private Supplier<LdapIdentityStoreDefinition> createLdapIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final LdapIdentityStoreDefinition annotation = ldapIdentityStore.iterator()
                                                                                    .next()
                                                                                    .getAnnotation(
                                                                                        LdapIdentityStoreDefinition.class);

            return TomEEELInvocationHandler.of(LdapIdentityStoreDefinition.class, annotation, beanManager);
        };
    }
}
