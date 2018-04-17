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

import org.apache.tomee.microprofile.jwt.MPJWTFilter;
import org.apache.tomee.microprofile.jwt.MPJWTInitializer;
import org.apache.tomee.microprofile.jwt.config.JWTAuthContextInfoProvider;
import org.eclipse.microprofile.jwt.Claim;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.inject.Provider;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MPJWTCDIExtension implements Extension {

    private static final Predicate<InjectionPoint> NOT_PROVIDERS = new Predicate<InjectionPoint>() {
        @Override
        public boolean test(final InjectionPoint ip) {
            return (Class.class.isInstance(ip.getType())) || (ParameterizedType.class.isInstance(ip.getType()) && ((ParameterizedType) ip.getType()).getRawType() != Provider.class);
        }
    };
    private static final Predicate<InjectionPoint> NOT_INSTANCES = new Predicate<InjectionPoint>() {
        @Override
        public boolean test(final InjectionPoint ip) {
            return (Class.class.isInstance(ip.getType())) || (ParameterizedType.class.isInstance(ip.getType()) && ((ParameterizedType) ip.getType()).getRawType() != Instance.class);
        }
    };
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

    public void registerClaimProducer(@Observes final AfterBeanDiscovery abd, final BeanManager bm) {

        final Set<Type> types = injectionPoints.stream()
                .filter(NOT_PROVIDERS)
                .filter(NOT_INSTANCES)
                .map(new Function<InjectionPoint, Type>() {
                    @Override
                    public Type apply(final InjectionPoint ip) {
                        return REPLACED_TYPES.getOrDefault(ip.getType(), ip.getType());
                    }
                })
                .collect(Collectors.<Type>toSet());

        final Set<Type> providerTypes = injectionPoints.stream()
                .filter(NOT_PROVIDERS.negate())
                .map(new Function<InjectionPoint, Type>() {
                    @Override
                    public Type apply(final InjectionPoint ip) {
                        return ((ParameterizedType) ip.getType()).getActualTypeArguments()[0];
                    }
                })
                .collect(Collectors.<Type>toSet());

        final Set<Type> instanceTypes = injectionPoints.stream()
                .filter(NOT_INSTANCES.negate())
                .map(new Function<InjectionPoint, Type>() {
                    @Override
                    public Type apply(final InjectionPoint ip) {
                        return ((ParameterizedType) ip.getType()).getActualTypeArguments()[0];
                    }
                })
                .collect(Collectors.<Type>toSet());

        types.addAll(providerTypes);
        types.addAll(instanceTypes);

        types.stream()
                .map(new Function<Type, ClaimBean>() {
                    @Override
                    public ClaimBean apply(final Type type) {
                        return new ClaimBean<>(bm, type);
                    }
                })
                .forEach(new Consumer<ClaimBean>() {
                    @Override
                    public void accept(final ClaimBean claimBean) {
                        abd.addBean(claimBean);
                    }
                });
    }

    public void observeBeforeBeanDiscovery(@Observes final BeforeBeanDiscovery bbd, final BeanManager beanManager) {
        bbd.addAnnotatedType(beanManager.createAnnotatedType(JsonbProducer.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MPJWTFilter.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MPJWTInitializer.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(JWTAuthContextInfoProvider.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MPJWTProducer.class));
    }

}