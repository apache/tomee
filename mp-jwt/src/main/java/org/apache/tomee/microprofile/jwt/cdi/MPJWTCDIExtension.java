/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.cdi;

import org.apache.bval.cdi.BValInterceptor;
import org.apache.tomee.microprofile.jwt.MPJWTFilter;
import org.apache.tomee.microprofile.jwt.MPJWTInitializer;
import org.apache.tomee.microprofile.jwt.config.JWTAuthConfigurationProperties;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.inject.Provider;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MPJWTCDIExtension implements Extension {

    private static final Predicate<InjectionPoint> NOT_PROVIDERS = ip -> (Class.class.isInstance(ip.getType())) || (ParameterizedType.class.isInstance(ip.getType()) && ((ParameterizedType) ip.getType()).getRawType() != Provider.class);
    private static final Predicate<InjectionPoint> NOT_INSTANCES = ip -> (Class.class.isInstance(ip.getType())) || (ParameterizedType.class.isInstance(ip.getType()) && ((ParameterizedType) ip.getType()).getRawType() != Instance.class);
    private static final Map<Type, Type> REPLACED_TYPES = new HashMap<>();

    static {
        REPLACED_TYPES.put(double.class, Double.class);
        REPLACED_TYPES.put(int.class, Integer.class);
        REPLACED_TYPES.put(float.class, Float.class);
        REPLACED_TYPES.put(long.class, Long.class);
        REPLACED_TYPES.put(boolean.class, Boolean.class);
    }

    private Set<InjectionPoint> injectionPoints = new HashSet<>();

    public void collectConfigProducer(@Observes final ProcessInjectionPoint<?, ?> pip) {
        final Claim claim = pip.getInjectionPoint().getAnnotated().getAnnotation(Claim.class);
        if (claim != null) {
            injectionPoints.add(pip.getInjectionPoint());
        }
    }

    void pat(@Observes final ProcessAnnotatedType<BValInterceptor> stockBvalInterceptor) {
        stockBvalInterceptor.veto();
    }


    public void registerClaimProducer(@Observes final AfterBeanDiscovery abd, final BeanManager bm) {

        final Set<Type> types = injectionPoints.stream()
                .filter(NOT_PROVIDERS)
                .filter(NOT_INSTANCES)
                .map(ip -> REPLACED_TYPES.getOrDefault(ip.getType(), ip.getType()))
                .collect(Collectors.<Type>toSet());

        final Set<Type> providerTypes = injectionPoints.stream()
                .filter(NOT_PROVIDERS.negate())
                .map(ip -> ((ParameterizedType) ip.getType()).getActualTypeArguments()[0])
                .collect(Collectors.<Type>toSet());

        final Set<Type> instanceTypes = injectionPoints.stream()
                .filter(NOT_INSTANCES.negate())
                .map(ip -> ((ParameterizedType) ip.getType()).getActualTypeArguments()[0])
                .collect(Collectors.<Type>toSet());

        types.addAll(providerTypes);
        types.addAll(instanceTypes);

        types.stream()
                .map(type -> new ClaimBean<>(bm, type))
                .forEach((Consumer<ClaimBean>) abd::addBean);

        abd.addBean()
                .id(MPJWTCDIExtension.class.getName() + "#" + JsonWebToken.class.getName())
                .beanClass(JsonWebToken.class)
                .types(JsonWebToken.class, Object.class)
                .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                .scope(Dependent.class)
                .createWith(ctx -> {
                    final Principal principal = getContextualReference(Principal.class, bm);
                    if (JsonWebToken.class.isInstance(principal)) {
                        return JsonWebToken.class.cast(principal);
                    }

                    return null;
                });
    }

    public void observeBeforeBeanDiscovery(@Observes final BeforeBeanDiscovery bbd, final BeanManager beanManager) {
        bbd.addAnnotatedType(beanManager.createAnnotatedType(JWTAuthConfigurationProperties.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(JsonbProducer.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MPJWTFilter.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MPJWTInitializer.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(org.apache.tomee.microprofile.jwt.bval.BValInterceptor.class));
    }

    public static <T> T getContextualReference(Class<T> type, final BeanManager beanManager) {
        final Set<Bean<?>> beans = beanManager.getBeans(type);

        if (beans == null || beans.isEmpty()) {
            throw new IllegalStateException("Could not find beans for Type=" + type);
        }

        final Bean<?> bean = beanManager.resolve(beans);
        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
        return (T) beanManager.getReference(bean, type, creationalContext);
    }

//** Scanning now happens automatically
//    static {
//        SystemInstance.get().addObserver(new MPJWPProviderRegistration());
//    }
}
