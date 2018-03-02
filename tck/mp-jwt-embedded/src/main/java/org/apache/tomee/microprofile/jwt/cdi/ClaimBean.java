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

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class ClaimBean<T> implements Bean<T>, PassivationCapable {

    private static Logger logger = Logger.getLogger(MPJWTCDIExtension.class.getName());

    private final static Set<Annotation> QUALIFIERS = new HashSet<>();

    private final BeanManager bm;
    private final Class rawType;
    private final Set<Type> types;
    private final String id;

    public ClaimBean(final BeanManager bm, final Type type) {
        this.bm = bm;
        types = new HashSet<>();
        types.add(type);
        rawType = getRawType(type);
        this.id = "ClaimBean_" + types;
    }

    private Class getRawType(final Type type) {
        if (type instanceof Class) {
            return (Class) type;

        } else if (type instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) type;

            return (Class) paramType.getRawType();
        }

        // todo deal with Optional here? aka check type again

        throw new UnsupportedOperationException("Unsupported type " + type);
    }


    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Class<?> getBeanClass() {
        return rawType;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> context) {

    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return QUALIFIERS;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public T create(final CreationalContext<T> context) {
        final InjectionPoint ip = (InjectionPoint) bm.getInjectableReference(new ClaimInjectionPoint(this), context);
        if (ip == null) {
            throw new IllegalStateException("Could not retrieve InjectionPoint");
        }

        final Annotated annotated = ip.getAnnotated();
        final Claim claim = annotated.getAnnotation(Claim.class);
        final String key = getClaimKey(claim);

        System.out.println(String.format("Found Claim injection with name=%s and for InjectionPoint=%s", key, ip.toString()));
        logger.finest(String.format("Found Claim injection with name=%s and for InjectionPoint=%s", key, ip.toString()));

        if (annotated.getBaseType() instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) annotated.getBaseType();
            final Type rawType = paramType.getRawType();
            if (rawType instanceof Class && paramType.getActualTypeArguments().length == 1) {

                final Class<?> rawTypeClass = ((Class<?>) rawType);

                // handle Provider<T>
                if (rawTypeClass.isAssignableFrom(Provider.class)) {
                    final Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    return (T) getClaimValue(key, clazz);
                }

                // handle ClaimValue<T>
                if (rawTypeClass.isAssignableFrom(ClaimValue.class)) {
                    final Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    return (T) getClaimValue(key, clazz);
                }

                // handle Optional<T>
                if (rawTypeClass.isAssignableFrom(Optional.class)) {
                    final Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    return (T) getClaimValue(key, clazz);
                }

                // handle Set<T>
                if (rawTypeClass.isAssignableFrom(Set.class)) {
                    Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    return getClaimValue(key, clazz);
                }

                // handle List<T>
                if (rawTypeClass.isAssignableFrom(List.class)) {
                    final Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    return getClaimValue(key, clazz);
                }
            }

        } else {
            // handle Raw types
            final Class clazz = (Class) annotated.getBaseType();
            return getClaimValue(key, clazz);
        }

        throw new IllegalStateException("Unhandled ClaimValue type");
    }

    public static String getClaimKey(final Claim claim) {
        return claim.standard() == Claims.UNKNOWN ? claim.value() : claim.standard().name();
    }

    private T getClaimValue(final String name, final Class clazz) {
        final JsonWebToken jwt = MPJWTProducer.getJWTPrincpal();
        if (jwt == null) {
            logger.warning(String.format("Can't retrieve claim %s. No active principal.", name));
            return null;
        }

        final Optional<T> claimValue = jwt.claim(name);
        logger.finest(String.format("Found ClaimValue=%s for name=%s", claimValue, name));
        return claimValue.orElse(null); // todo more to do?
    }

}
