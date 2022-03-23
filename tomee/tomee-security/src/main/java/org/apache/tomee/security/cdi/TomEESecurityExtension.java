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
import org.apache.tomee.security.identitystore.TomEEDefaultIdentityStore;
import org.apache.tomee.security.identitystore.TomEEIdentityStoreHandler;
import org.apache.tomee.security.identitystore.TomEELDAPIdentityStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class TomEESecurityExtension implements Extension {

    final List<Class<? extends Annotation>> annotationsToFind = Arrays.asList(TomcatUserIdentityStoreDefinition.class,
                                                                              DatabaseIdentityStoreDefinition.class,
                                                                              LdapIdentityStoreDefinition.class,
                                                                              BasicAuthenticationMechanismDefinition.class,
                                                                              FormAuthenticationMechanismDefinition.class,
                                                                              CustomFormAuthenticationMechanismDefinition.class);

    private final AtomicReference<Annotated> basicMechanism = new AtomicReference<>();
    private final AtomicReference<Annotated> formMechanism = new AtomicReference<>();
    private final AtomicReference<Annotated> customMechanism = new AtomicReference<>();

    private final AtomicReference<Annotated> tomcatUserStore = new AtomicReference<>();
    private final AtomicReference<Annotated> databaseStore = new AtomicReference<>();
    private final AtomicReference<Annotated> ldapStore = new AtomicReference<>();

    private boolean applicationAuthenticationMechanisms = false;

    void observeBeforeBeanDiscovery(
        @Observes final BeforeBeanDiscovery beforeBeanDiscovery,
        final BeanManager beanManager) {

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(DefaultAuthenticationMechanism.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEESecurityServletAuthenticationMechanismMapper.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEIdentityStoreHandler.class));

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEPbkdf2PasswordHash.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEPlaintextPasswordHash.class));

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(AutoApplySessionInterceptor.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(RememberMeInterceptor.class));
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(LoginToContinueInterceptor.class));

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEESecurityContext.class));
    }

    // using CDI Observes with WithAnnotations seems to trigger loading of the ProcessAnnotatedType
    // and it may fail into a NoClassDefFound pretty hard to pin down
    public <T> void processBean(@Observes final ProcessBean<T> eventIn, final BeanManager beanManager) {

        final Annotated annotatedType = eventIn.getAnnotated();

        if (tomcatUserStore.get() == null && annotatedType.isAnnotationPresent(TomcatUserIdentityStoreDefinition.class)) {
            tomcatUserStore.set(annotatedType);
        }

        if (databaseStore.get() == null && annotatedType.isAnnotationPresent(DatabaseIdentityStoreDefinition.class)) {
            databaseStore.set(annotatedType);
        }

        if (ldapStore.get() == null && annotatedType.isAnnotationPresent(LdapIdentityStoreDefinition.class)) {
            ldapStore.set(annotatedType);
        }

        if (basicMechanism.get() == null && annotatedType.isAnnotationPresent(BasicAuthenticationMechanismDefinition.class)) {
            basicMechanism.set(annotatedType);
        }

        if (formMechanism.get() == null && annotatedType.isAnnotationPresent(FormAuthenticationMechanismDefinition.class)) {
            formMechanism.set(annotatedType);
        }

        if (customMechanism.get() == null && annotatedType.isAnnotationPresent(CustomFormAuthenticationMechanismDefinition.class)) {
            customMechanism.set(annotatedType);
        }

        if (eventIn.getBean().getTypes().contains(HttpAuthenticationMechanism.class)) {
            applicationAuthenticationMechanisms = true;
        }
    }

    void registerAuthenticationMechanism(
        @Observes final AfterBeanDiscovery afterBeanDiscovery,
        final BeanManager beanManager) {

        if (tomcatUserStore.get() != null) {
            afterBeanDiscovery
                .addBean()
                .id(TomEEDefaultIdentityStore.class.getName() + "#" + TomcatUserIdentityStoreDefinition.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<TomcatUserIdentityStoreDefinition>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createTomcatUserIdentityStoreDefinitionSupplier(beanManager));

            afterBeanDiscovery
                .addBean()
                .id(TomEEDefaultIdentityStore.class.getName())
                .beanClass(TomEEDefaultIdentityStore.class)
                .types(Object.class, IdentityStore.class, TomEEDefaultIdentityStore.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith((CreationalContext<TomEEDefaultIdentityStore> creationalContext) -> {
                    final AnnotatedType<TomEEDefaultIdentityStore> annotatedType =
                        beanManager.createAnnotatedType(TomEEDefaultIdentityStore.class);
                    final BeanAttributes<TomEEDefaultIdentityStore> beanAttributes =
                        beanManager.createBeanAttributes(annotatedType);
                    return beanManager.createBean(beanAttributes, TomEEDefaultIdentityStore.class,
                                                  beanManager.getInjectionTargetFactory(annotatedType))
                                      .create(creationalContext);
                });

        }

        if (databaseStore.get() != null) {
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

        if (ldapStore.get() != null) {
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

        if (basicMechanism.get() != null) {
            afterBeanDiscovery
                .addBean()
                .id(BasicAuthenticationMechanism.class.getName() + "#" + BasicAuthenticationMechanismDefinition.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<BasicAuthenticationMechanismDefinition>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createBasicAuthenticationMechanismDefinitionSupplier(beanManager));

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

        if (formMechanism.get() != null) {
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

        if (customMechanism.get() != null) {
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
        return basicMechanism.get() != null || formMechanism.get() != null || customMechanism.get() != null || applicationAuthenticationMechanisms;
    }

    private Supplier<LoginToContinue> createFormLoginToContinueSupplier(final BeanManager beanManager) {
        return () -> {
            final LoginToContinue loginToContinue = formMechanism.get()
                                                                      .getAnnotation(
                                                                          FormAuthenticationMechanismDefinition.class)
                                                                      .loginToContinue();

            return TomEEELInvocationHandler.of(LoginToContinue.class, loginToContinue, beanManager);
        };
    }

    private Supplier<BasicAuthenticationMechanismDefinition> createBasicAuthenticationMechanismDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final BasicAuthenticationMechanismDefinition annotation = basicMechanism.get()
                                                                   .getAnnotation(BasicAuthenticationMechanismDefinition.class);

            return TomEEELInvocationHandler.of(BasicAuthenticationMechanismDefinition.class, annotation, beanManager);
        };
    }

    private Supplier<LoginToContinue> createCustomFormLoginToContinueSupplier(final BeanManager beanManager) {
        return () -> {
            final LoginToContinue annotation = customMechanism.get()
                                                                   .getAnnotation(
                                                                       CustomFormAuthenticationMechanismDefinition.class)
                                                                   .loginToContinue();

            return TomEEELInvocationHandler.of(LoginToContinue.class, annotation, beanManager);
        };
    }

    private Supplier<TomcatUserIdentityStoreDefinition> createTomcatUserIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final TomcatUserIdentityStoreDefinition annotation = tomcatUserStore.get()
                                                                                    .getAnnotation(
                                                                                        TomcatUserIdentityStoreDefinition.class);

            return TomEEELInvocationHandler.of(TomcatUserIdentityStoreDefinition.class, annotation, beanManager);
        };
    }

    private Supplier<DatabaseIdentityStoreDefinition> createDatabaseIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final DatabaseIdentityStoreDefinition annotation = databaseStore.get()
                                                                                    .getAnnotation(
                                                                                        DatabaseIdentityStoreDefinition.class);

            return TomEEELInvocationHandler.of(DatabaseIdentityStoreDefinition.class, annotation, beanManager);
        };
    }

    private Supplier<LdapIdentityStoreDefinition> createLdapIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final LdapIdentityStoreDefinition annotation = ldapStore.get()
                                                                                    .getAnnotation(
                                                                                        LdapIdentityStoreDefinition.class);

            return TomEEELInvocationHandler.of(LdapIdentityStoreDefinition.class, annotation, beanManager);
        };
    }
}
