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

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import org.apache.tomee.security.TomEEELInvocationHandler;
import org.apache.tomee.security.TomEEPbkdf2PasswordHash;
import org.apache.tomee.security.TomEEPlaintextPasswordHash;
import org.apache.tomee.security.TomEESecurityContext;
import org.apache.tomee.security.cdi.openid.BaseUrlProducer;
import org.apache.tomee.security.cdi.openid.OpenIdIdentityStore;
import org.apache.tomee.security.cdi.openid.TomEEOpenIdContext;
import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;
import org.apache.tomee.security.cdi.openid.storage.impl.CookieBasedOpenIdStorageHandler;
import org.apache.tomee.security.cdi.openid.storage.impl.SessionBasedOpenIdStorageHandler;
import org.apache.tomee.security.http.openid.OpenIdAuthenticationMechanismDefinitionDelegate;
import org.apache.tomee.security.identitystore.TomEEDatabaseIdentityStore;
import org.apache.tomee.security.identitystore.TomEEDefaultIdentityStore;
import org.apache.tomee.security.identitystore.TomEEIdentityStoreHandler;
import org.apache.tomee.security.identitystore.TomEEInMemoryIdentityStore;
import org.apache.tomee.security.identitystore.TomEELDAPIdentityStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.DeploymentException;
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
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TomEESecurityExtension implements Extension {
    // LinkedHashSet: ProcessBean fires for each bean *and* any derived observer/interceptor beans on the
    // same class, so List.addAll would otherwise duplicate identical annotation instances and cause
    // AmbiguousResolutionException when multiple beans are registered with the same qualifier set.
    private final Set<BasicAuthenticationMechanismDefinition> basicMechanismDefinitions = new LinkedHashSet<>();
    private final Set<FormAuthenticationMechanismDefinition> formMechanismDefinitions = new LinkedHashSet<>();
    private final Set<CustomFormAuthenticationMechanismDefinition> customMechanismDefinitions = new LinkedHashSet<>();
    private final Set<OpenIdAuthenticationMechanismDefinition> oidcMechanismDefinitions = new LinkedHashSet<>();

    private final AtomicReference<Annotated> tomcatUserStore = new AtomicReference<>();
    private final AtomicReference<Annotated> databaseStore = new AtomicReference<>();
    private final AtomicReference<Annotated> ldapStore = new AtomicReference<>();
    private final AtomicReference<Annotated> inMemoryStore = new AtomicReference<>();

    private boolean applicationAuthenticationMechanisms = false;

    void observeBeforeBeanDiscovery(
        @Observes final BeforeBeanDiscovery beforeBeanDiscovery,
        final BeanManager beanManager) {

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(DefaultAuthenticationMechanism.class), "DefaultAuthenticationMechanism");
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(DefaultAuthenticationMechanismHandler.class), "DefaultAuthenticationMechanismHandler");
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEESecurityServletAuthenticationMechanismMapper.class), "TomEESecurityServletAuthenticationMechanismMapper");
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEIdentityStoreHandler.class), "TomEEIdentityStoreHandler");

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEPbkdf2PasswordHash.class), "TomEEPbkdf2PasswordHash");
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEEPlaintextPasswordHash.class), "TomEEPlaintextPasswordHash");

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(AutoApplySessionInterceptor.class), "AutoApplySessionInterceptor");
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(RememberMeInterceptor.class), "RememberMeInterceptor");
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(LoginToContinueInterceptor.class), "LoginToContinueInterceptor");

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(TomEESecurityContext.class), "TomEESecurityContext");

        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(BaseUrlProducer.class), "TomEEBaseUrlProducer");
        beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(CallerPrincipalProducer.class), "TomEECallerPrincipalProducer");
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

        if (inMemoryStore.get() == null && annotatedType.isAnnotationPresent(InMemoryIdentityStoreDefinition.class)) {
            inMemoryStore.set(annotatedType);
        }

        basicMechanismDefinitions.addAll(annotatedType.getAnnotations(BasicAuthenticationMechanismDefinition.class));
        formMechanismDefinitions.addAll(annotatedType.getAnnotations(FormAuthenticationMechanismDefinition.class));
        customMechanismDefinitions.addAll(annotatedType.getAnnotations(CustomFormAuthenticationMechanismDefinition.class));
        oidcMechanismDefinitions.addAll(annotatedType.getAnnotations(OpenIdAuthenticationMechanismDefinition.class));

        if (eventIn.getBean().getTypes().contains(HttpAuthenticationMechanism.class)) {
            applicationAuthenticationMechanisms = true;
        }
    }

    void registerAuthenticationMechanism(
        @Observes final AfterBeanDiscovery afterBeanDiscovery,
        final BeanManager beanManager) {

        // Snapshot definition sets as ordered lists so the registration loops can use index-based IDs.
        final List<BasicAuthenticationMechanismDefinition> basicMechanismDefinitions =
                new ArrayList<>(this.basicMechanismDefinitions);
        final List<FormAuthenticationMechanismDefinition> formMechanismDefinitions =
                new ArrayList<>(this.formMechanismDefinitions);
        final List<CustomFormAuthenticationMechanismDefinition> customMechanismDefinitions =
                new ArrayList<>(this.customMechanismDefinitions);
        final List<OpenIdAuthenticationMechanismDefinition> oidcMechanismDefinitions =
                new ArrayList<>(this.oidcMechanismDefinitions);

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

        if (inMemoryStore.get() != null) {
            afterBeanDiscovery
                .addBean()
                .id(TomEEInMemoryIdentityStore.class.getName() + "#" + InMemoryIdentityStoreDefinition.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<InMemoryIdentityStoreDefinition>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createInMemoryIdentityStoreDefinitionSupplier(beanManager));

            afterBeanDiscovery
                .addBean()
                .id(TomEEInMemoryIdentityStore.class.getName())
                .beanClass(TomEEInMemoryIdentityStore.class)
                .types(Object.class, IdentityStore.class, TomEEInMemoryIdentityStore.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith((CreationalContext<TomEEInMemoryIdentityStore> creationalContext) -> {
                    final AnnotatedType<TomEEInMemoryIdentityStore> annotatedType =
                        beanManager.createAnnotatedType(TomEEInMemoryIdentityStore.class);
                    final BeanAttributes<TomEEInMemoryIdentityStore> beanAttributes =
                        beanManager.createBeanAttributes(annotatedType);
                    return beanManager.createBean(beanAttributes, TomEEInMemoryIdentityStore.class,
                                                  beanManager.getInjectionTargetFactory(annotatedType))
                                      .create(creationalContext);
                });
        }

        if (!basicMechanismDefinitions.isEmpty()) {
            final BasicAuthenticationMechanismDefinition defaultBasicDefinition = basicMechanismDefinitions.get(0);
            afterBeanDiscovery
                .addBean()
                .id(BasicAuthenticationMechanism.class.getName() + "#" + BasicAuthenticationMechanismDefinition.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<BasicAuthenticationMechanismDefinition>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createBasicAuthenticationMechanismDefinitionSupplier(defaultBasicDefinition, beanManager));

            for (int i = 0; i < basicMechanismDefinitions.size(); i++) {
                final BasicAuthenticationMechanismDefinition definition = basicMechanismDefinitions.get(i);
                final Supplier<BasicAuthenticationMechanismDefinition> definitionSupplier =
                        createBasicAuthenticationMechanismDefinitionSupplier(definition, beanManager);
                final Annotation[] qualifiers = QualifierInstances.beanQualifiers(definition.qualifiers());

                afterBeanDiscovery
                    .addBean()
                    .id(BasicAuthenticationMechanism.class.getName() + "#" + i)
                    .beanClass(BasicAuthenticationMechanism.class)
                    .types(Object.class, HttpAuthenticationMechanism.class, BasicAuthenticationMechanism.class)
                    .qualifiers(qualifiers)
                    .scope(ApplicationScoped.class)
                    .createWith((CreationalContext<BasicAuthenticationMechanism> creationalContext) -> {
                        final AnnotatedType<BasicAuthenticationMechanism> annotatedType =
                            beanManager.createAnnotatedType(BasicAuthenticationMechanism.class);
                        final BeanAttributes<BasicAuthenticationMechanism> beanAttributes =
                            beanManager.createBeanAttributes(annotatedType);
                        final BasicAuthenticationMechanism mechanism =
                                beanManager.createBean(beanAttributes, BasicAuthenticationMechanism.class,
                                                       beanManager.getInjectionTargetFactory(annotatedType))
                                           .create(creationalContext);
                        mechanism.setDefinitionSupplier(definitionSupplier);
                        return mechanism;
                    });
            }
        }

        if (!formMechanismDefinitions.isEmpty()) {
            final FormAuthenticationMechanismDefinition defaultFormDefinition = formMechanismDefinitions.get(0);
            afterBeanDiscovery
                .addBean()
                .id(FormAuthenticationMechanism.class.getName() + "#" + LoginToContinue.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<LoginToContinue>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createFormLoginToContinueSupplier(defaultFormDefinition, beanManager));

            for (int i = 0; i < formMechanismDefinitions.size(); i++) {
                final FormAuthenticationMechanismDefinition definition = formMechanismDefinitions.get(i);
                final Supplier<LoginToContinue> loginToContinueSupplier =
                        createFormLoginToContinueSupplier(definition, beanManager);
                final Annotation[] qualifiers = QualifierInstances.beanQualifiers(definition.qualifiers());

                afterBeanDiscovery
                    .addBean()
                    .id(FormAuthenticationMechanism.class.getName() + "#" + i)
                    .beanClass(FormAuthenticationMechanism.class)
                    .types(Object.class, HttpAuthenticationMechanism.class, FormAuthenticationMechanism.class)
                    .qualifiers(qualifiers)
                    .scope(ApplicationScoped.class)
                    .createWith((CreationalContext<FormAuthenticationMechanism> creationalContext) -> {
                        final AnnotatedType<FormAuthenticationMechanism> annotatedType =
                            beanManager.createAnnotatedType(FormAuthenticationMechanism.class);
                        final BeanAttributes<FormAuthenticationMechanism> beanAttributes =
                            beanManager.createBeanAttributes(annotatedType);
                        final FormAuthenticationMechanism mechanism =
                                beanManager.createBean(beanAttributes, FormAuthenticationMechanism.class,
                                                       beanManager.getInjectionTargetFactory(annotatedType))
                                           .create(creationalContext);
                        mechanism.setLoginToContinueSupplier(loginToContinueSupplier);
                        return mechanism;
                    });
            }
        }

        if (!customMechanismDefinitions.isEmpty()) {
            final CustomFormAuthenticationMechanismDefinition defaultCustomDefinition = customMechanismDefinitions.get(0);
            afterBeanDiscovery
                .addBean()
                .id(CustomFormAuthenticationMechanism.class.getName() + "#" + LoginToContinue.class.getName())
                .beanClass(Supplier.class)
                .addType(Object.class)
                .addType(new TypeLiteral<Supplier<LoginToContinue>>() {})
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .createWith(creationalContext -> createCustomFormLoginToContinueSupplier(defaultCustomDefinition, beanManager));

            for (int i = 0; i < customMechanismDefinitions.size(); i++) {
                final CustomFormAuthenticationMechanismDefinition definition = customMechanismDefinitions.get(i);
                final Supplier<LoginToContinue> loginToContinueSupplier =
                        createCustomFormLoginToContinueSupplier(definition, beanManager);
                final Annotation[] qualifiers = QualifierInstances.beanQualifiers(definition.qualifiers());

                afterBeanDiscovery
                    .addBean()
                    .id(CustomFormAuthenticationMechanism.class.getName() + "#" + i)
                    .beanClass(CustomFormAuthenticationMechanism.class)
                    .types(Object.class, HttpAuthenticationMechanism.class, CustomFormAuthenticationMechanism.class)
                    .qualifiers(qualifiers)
                    .scope(ApplicationScoped.class)
                    .createWith((CreationalContext<CustomFormAuthenticationMechanism> creationalContext) -> {
                        final AnnotatedType<CustomFormAuthenticationMechanism> annotatedType =
                            beanManager.createAnnotatedType(CustomFormAuthenticationMechanism.class);
                        final BeanAttributes<CustomFormAuthenticationMechanism> beanAttributes =
                            beanManager.createBeanAttributes(annotatedType);
                        final CustomFormAuthenticationMechanism mechanism =
                                beanManager.createBean(beanAttributes, CustomFormAuthenticationMechanism.class,
                                                       beanManager.getInjectionTargetFactory(annotatedType))
                                           .create(creationalContext);
                        mechanism.setLoginToContinueSupplier(loginToContinueSupplier);
                        return mechanism;
                    });
            }
        }

        if (!oidcMechanismDefinitions.isEmpty()) {
            final OpenIdAuthenticationMechanismDefinition defaultOidcDefinition = oidcMechanismDefinitions.get(0);
            afterBeanDiscovery
                    .addBean()
                    .id(OpenIdAuthenticationMechanism.class.getName() + "#" + OpenIdAuthenticationMechanismDefinition.class.getName())
                    .beanClass(OpenIdAuthenticationMechanismDefinition.class)
                    .types(Object.class, OpenIdAuthenticationMechanismDefinition.class)
                    .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                    .scope(RequestScoped.class)
                    .createWith(creationalContext -> createOpenIdAuthenticationMechanismDefinition(defaultOidcDefinition, beanManager));

            afterBeanDiscovery.addBean()
                    .id(OpenIdStorageHandler.class.getName())
                    .beanClass(OpenIdStorageHandler.class)
                    .types(Object.class, OpenIdStorageHandler.class)
                    .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                    .scope(ApplicationScoped.class)
                    .createWith(creationalContext -> {
                        Bean<OpenIdAuthenticationMechanismDefinition> definitionBean = (Bean<OpenIdAuthenticationMechanismDefinition>)
                                beanManager.resolve(beanManager.getBeans(OpenIdAuthenticationMechanismDefinition.class));

                        OpenIdAuthenticationMechanismDefinition definition = (OpenIdAuthenticationMechanismDefinition)
                                beanManager.getReference(definitionBean, OpenIdAuthenticationMechanismDefinition.class, creationalContext);

                        return definition.useSession()
                                ? new SessionBasedOpenIdStorageHandler()
                                : new CookieBasedOpenIdStorageHandler();
                    });

            afterBeanDiscovery.addBean(createBean(TomEEOpenIdContext.class, beanManager));
            afterBeanDiscovery.addBean(createBean(OpenIdIdentityStore.class, beanManager));

            for (int i = 0; i < oidcMechanismDefinitions.size(); i++) {
                final OpenIdAuthenticationMechanismDefinition definition = oidcMechanismDefinitions.get(i);
                final Supplier<OpenIdAuthenticationMechanismDefinition> definitionSupplier =
                        createOpenIdAuthenticationMechanismDefinitionSupplier(definition, beanManager);
                final Annotation[] qualifiers = QualifierInstances.beanQualifiers(definition.qualifiers());

                afterBeanDiscovery
                        .addBean()
                        .id(OpenIdAuthenticationMechanism.class.getName() + "#" + i)
                        .beanClass(OpenIdAuthenticationMechanism.class)
                        .types(Object.class, HttpAuthenticationMechanism.class, OpenIdAuthenticationMechanism.class)
                        .qualifiers(qualifiers)
                        .scope(ApplicationScoped.class)
                        .createWith((CreationalContext<OpenIdAuthenticationMechanism> creationalContext) -> {
                            final AnnotatedType<OpenIdAuthenticationMechanism> annotatedType =
                                    beanManager.createAnnotatedType(OpenIdAuthenticationMechanism.class);
                            final BeanAttributes<OpenIdAuthenticationMechanism> beanAttributes =
                                    beanManager.createBeanAttributes(annotatedType);
                            final OpenIdAuthenticationMechanism mechanism =
                                    beanManager.createBean(beanAttributes, OpenIdAuthenticationMechanism.class,
                                                           beanManager.getInjectionTargetFactory(annotatedType))
                                               .create(creationalContext);
                            mechanism.setDefinitionSupplier(definitionSupplier);
                            return mechanism;
                        });
            }
        }
    }

    public boolean hasAuthenticationMechanisms() {
        return !basicMechanismDefinitions.isEmpty() || !formMechanismDefinitions.isEmpty() ||
                !customMechanismDefinitions.isEmpty() || !oidcMechanismDefinitions.isEmpty() ||
                applicationAuthenticationMechanisms;
    }

    // Fail deployment early when two *AuthenticationMechanismDefinition annotations would register
    // beans with the same qualifier set but different content (e.g. two @BasicAuthenticationMechanismDefinition
    // with identical default qualifiers but different realmName). Without this the clash only surfaces
    // at first injection as an AmbiguousResolutionException, which is hard to diagnose.
    void validateMechanismDefinitionUniqueness(@Observes final AfterDeploymentValidation afterDeploymentValidation) {
        final List<String> problems = new ArrayList<>();

        problems.addAll(validateQualifierUniqueness(basicMechanismDefinitions,
                BasicAuthenticationMechanismDefinition.class,
                BasicAuthenticationMechanismDefinition::qualifiers));
        problems.addAll(validateQualifierUniqueness(formMechanismDefinitions,
                FormAuthenticationMechanismDefinition.class,
                FormAuthenticationMechanismDefinition::qualifiers));
        problems.addAll(validateQualifierUniqueness(customMechanismDefinitions,
                CustomFormAuthenticationMechanismDefinition.class,
                CustomFormAuthenticationMechanismDefinition::qualifiers));
        problems.addAll(validateQualifierUniqueness(oidcMechanismDefinitions,
                OpenIdAuthenticationMechanismDefinition.class,
                OpenIdAuthenticationMechanismDefinition::qualifiers));

        if (!problems.isEmpty()) {
            final String message = "Ambiguous authentication mechanism definitions detected:\n    "
                    + String.join("\n    ", problems);
            afterDeploymentValidation.addDeploymentProblem(new DeploymentException(message));
        }
    }

    // Package-private for focused unit tests. Groups the annotations by the effective qualifier set
    // (order-insensitive, duplicates collapsed) and returns a problem message for each set that has
    // more than one distinct definition. Returns an empty list when the definitions are unambiguous.
    static <T extends Annotation> List<String> validateQualifierUniqueness(
            final Collection<T> definitions,
            final Class<T> annotationType,
            final Function<T, Class<?>[]> qualifiersAccessor) {

        if (definitions == null || definitions.size() < 2) {
            return List.of();
        }

        // LinkedHashMap preserves discovery order so error messages are deterministic.
        final Map<Set<Class<?>>, List<T>> byQualifierSet = new LinkedHashMap<>();
        for (final T definition : definitions) {
            final Class<?>[] qualifiers = qualifiersAccessor.apply(definition);
            final Set<Class<?>> key = new LinkedHashSet<>(Arrays.asList(qualifiers));
            byQualifierSet.computeIfAbsent(key, k -> new ArrayList<>()).add(definition);
        }

        final List<String> problems = new ArrayList<>();
        for (final Map.Entry<Set<Class<?>>, List<T>> entry : byQualifierSet.entrySet()) {
            if (entry.getValue().size() <= 1) {
                continue;
            }
            final String qualifierList = entry.getKey().isEmpty()
                    ? "{}"
                    : entry.getKey().stream()
                            .map(Class::getSimpleName)
                            .collect(Collectors.joining(", ", "{", "}"));
            final String conflicting = entry.getValue().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("; "));
            problems.add("@" + annotationType.getSimpleName()
                    + " declared " + entry.getValue().size() + " times with qualifier set " + qualifierList
                    + " but different content [" + conflicting + "]; "
                    + "declare distinct qualifiers via qualifiers={...} or pick a single definition");
        }
        return problems;
    }

    private Supplier<LoginToContinue> createFormLoginToContinueSupplier(final FormAuthenticationMechanismDefinition definition,
                                                                        final BeanManager beanManager) {
        return () -> {
            final LoginToContinue loginToContinue = definition.loginToContinue();

            return TomEEELInvocationHandler.of(LoginToContinue.class, loginToContinue, beanManager);
        };
    }

    private Supplier<BasicAuthenticationMechanismDefinition> createBasicAuthenticationMechanismDefinitionSupplier(
            final BasicAuthenticationMechanismDefinition definition, final BeanManager beanManager) {
        return () -> {
            return TomEEELInvocationHandler.of(BasicAuthenticationMechanismDefinition.class, definition, beanManager);
        };
    }

    private Supplier<LoginToContinue> createCustomFormLoginToContinueSupplier(
            final CustomFormAuthenticationMechanismDefinition definition, final BeanManager beanManager) {
        return () -> {
            final LoginToContinue annotation = definition.loginToContinue();

            return TomEEELInvocationHandler.of(LoginToContinue.class, annotation, beanManager);
        };
    }

    private Supplier<TomcatUserIdentityStoreDefinition> createTomcatUserIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final TomcatUserIdentityStoreDefinition annotation = tomcatUserStore.get().getAnnotation(TomcatUserIdentityStoreDefinition.class);
            return TomEEELInvocationHandler.of(TomcatUserIdentityStoreDefinition.class, annotation, beanManager);
        };
    }

    private Supplier<DatabaseIdentityStoreDefinition> createDatabaseIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final DatabaseIdentityStoreDefinition annotation = databaseStore.get().getAnnotation(DatabaseIdentityStoreDefinition.class);
            return TomEEELInvocationHandler.of(DatabaseIdentityStoreDefinition.class, annotation, beanManager);
        };
    }

    private Supplier<LdapIdentityStoreDefinition> createLdapIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final LdapIdentityStoreDefinition annotation = ldapStore.get().getAnnotation(LdapIdentityStoreDefinition.class);
            return TomEEELInvocationHandler.of(LdapIdentityStoreDefinition.class, annotation, beanManager);
        };
    }

    private Supplier<InMemoryIdentityStoreDefinition> createInMemoryIdentityStoreDefinitionSupplier(final BeanManager beanManager) {
        return () -> {
            final InMemoryIdentityStoreDefinition annotation = inMemoryStore.get().getAnnotation(InMemoryIdentityStoreDefinition.class);
            return TomEEELInvocationHandler.of(InMemoryIdentityStoreDefinition.class, annotation, beanManager);
        };
    }

    private Supplier<OpenIdAuthenticationMechanismDefinition> createOpenIdAuthenticationMechanismDefinitionSupplier(
            final OpenIdAuthenticationMechanismDefinition definition, final BeanManager bm) {
        return () -> createOpenIdAuthenticationMechanismDefinition(definition, bm);
    }

    private OpenIdAuthenticationMechanismDefinition createOpenIdAuthenticationMechanismDefinition(
            final OpenIdAuthenticationMechanismDefinition annotation, final BeanManager bm) {
        return new OpenIdAuthenticationMechanismDefinitionDelegate.AutoResolvingProviderMetadata(
                TomEEELInvocationHandler.of(OpenIdAuthenticationMechanismDefinition.class, annotation, bm));
    }

    private <T> Bean<T> createBean(final Class<T> beanType, BeanManager bm) {
        AnnotatedType<T> annotatedType = bm.createAnnotatedType(beanType);
        return bm.createBean(
                bm.createBeanAttributes(annotatedType),
                beanType,
                bm.getInjectionTargetFactory(annotatedType));
    }
}
