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
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Vetoed
public class ClaimBean<T> implements Bean<T>, PassivationCapable {

    private static final Logger logger = Logger.getLogger(MPJWTCDIExtension.class.getName());

    private static final Set<Annotation> QUALIFIERS = new HashSet<>();

    static {
        QUALIFIERS.add(new ClaimLiteral());
    }

    @Inject
    private Jsonb jsonb;

    private final BeanManager bm;
    private final Class rawType;
    private final Set<Type> types;
    private final String id;
    private final Class<? extends Annotation> scope;

    public ClaimBean(final BeanManager bm, final Type type) {
        this.bm = bm;
        types = new HashSet<>();
        types.add(type);
        rawType = getRawType(type);
        this.id = "ClaimBean_" + types;
        scope = Dependent.class;
    }

    private Class getRawType(final Type type) {
        if (Class.class.isInstance(type)) {
            return Class.class.cast(type);

        } else if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType paramType = ParameterizedType.class.cast(type);
            return Class.class.cast(paramType.getRawType());
        }

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
    public void destroy(final T instance, final CreationalContext<T> context) {
        logger.finest("Destroying CDI Bean for type " + types.iterator().next());
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
        return scope;
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
        logger.finest("Creating CDI Bean for type " + types.iterator().next());
        final InjectionPoint ip = (InjectionPoint) bm.getInjectableReference(new ClaimInjectionPoint(this), context);
        if (ip == null) {
            throw new IllegalStateException("Could not retrieve InjectionPoint for type " + types.iterator().next());
        }

        final Annotated annotated = ip.getAnnotated();
        final Claim claim = annotated.getAnnotation(Claim.class);
        final String key = getClaimKey(claim);

        logger.finest(String.format("Found Claim injection with name=%s and for %s", key, ip.toString()));

        if (ParameterizedType.class.isInstance(annotated.getBaseType())) {
            final ParameterizedType paramType = ParameterizedType.class.cast(annotated.getBaseType());
            final Type rawType = paramType.getRawType();
            if (Class.class.isInstance(rawType) && paramType.getActualTypeArguments().length == 1) {

                final Class<?> rawTypeClass = ((Class<?>) rawType);

                // handle Provider<T>
                if (rawTypeClass.isAssignableFrom(Provider.class)) {
                    final Type providerType = paramType.getActualTypeArguments()[0];
                    if (ParameterizedType.class.isInstance(providerType) && isOptional(ParameterizedType.class.cast(providerType))) {
                        return (T) Optional.ofNullable(getClaimValue(key));
                    }
                    return getClaimValue(key);
                }

                // handle Instance<T>
                if (rawTypeClass.isAssignableFrom(Instance.class)) {
                    final Type instanceType = paramType.getActualTypeArguments()[0];
                    if (ParameterizedType.class.isInstance(instanceType) && isOptional(ParameterizedType.class.cast(instanceType))) {
                        return (T) Optional.ofNullable(getClaimValue(key));
                    }
                    return getClaimValue(key);
                }

                // handle ClaimValue<T>
                if (rawTypeClass.isAssignableFrom(ClaimValue.class)) {
                    final Type claimValueType = paramType.getActualTypeArguments()[0];

                    final ClaimValueWrapper claimValueWrapper = new ClaimValueWrapper(key);
                    if (ParameterizedType.class.isInstance(claimValueType) && isOptional(ParameterizedType.class.cast(claimValueType))) {
                        claimValueWrapper.setValue(new Supplier() {
                            @Override
                            public Object get() {
                                final T claimValue = ClaimBean.this.getClaimValue(key);
                                return Optional.ofNullable(claimValue);
                            }
                        });

                    } else if (ParameterizedType.class.isInstance(claimValueType) && isSet(ParameterizedType.class.cast(claimValueType))) {
                        claimValueWrapper.setValue(new Supplier() {
                            @Override
                            public Object get() {
                                final T claimValue = ClaimBean.this.getClaimValue(key);
                                return claimValue;
                            }
                        });

                    } else if (ParameterizedType.class.isInstance(claimValueType) && isList(ParameterizedType.class.cast(claimValueType))) {
                        claimValueWrapper.setValue(new Supplier() {
                            @Override
                            public Object get() {
                                final T claimValue = ClaimBean.this.getClaimValue(key);
                                return claimValue;
                            }
                        });

                    } else if (Class.class.isInstance(claimValueType)) {
                        claimValueWrapper.setValue(new Supplier() {
                            @Override
                            public Object get() {
                                final T claimValue = ClaimBean.this.getClaimValue(key);
                                return claimValue;
                            }
                        });

                    } else {
                        throw new IllegalArgumentException("Unsupported ClaimValue type " + claimValueType.toString());
                    }

                    return (T) claimValueWrapper;
                }

                // handle Optional<T>
                if (rawTypeClass.isAssignableFrom(Optional.class)) {
                    return getClaimValue(key);
                }

                // handle Set<T>
                if (rawTypeClass.isAssignableFrom(Set.class)) {
                    return getClaimValue(key);
                }

                // handle List<T>
                if (rawTypeClass.isAssignableFrom(List.class)) {
                    return getClaimValue(key);
                }
            }

        } else if (annotated.getBaseType().getTypeName().startsWith("javax.json.Json")) {
            // handle JsonValue<T> (number, string, etc)
            return (T) toJson(key);

        } else if (((Class<?>) ip.getType()).isEnum()) {
            try {
                return (T) ((Class<?>) ip.getType()).getMethod("valueOf", String.class).invoke(null, getClaimValue(key).toString());
            } catch (Exception e) {
            }
        } else {
            // handle Raw types
            return getClaimValue(key);
        }

        throw new IllegalStateException("Unhandled Claim type " + annotated.getBaseType());
    }

    public static String getClaimKey(final Claim claim) {
        return claim.standard() == Claims.UNKNOWN ? claim.value() : claim.standard().name();
    }

    private T getClaimValue(final String name) {
        final Bean<?> bean = bm.resolve(bm.getBeans(JsonWebToken.class));
        JsonWebToken jsonWebToken = null;
        if (RequestScoped.class.equals(bean.getScope())) {
            jsonWebToken = JsonWebToken.class.cast(bm.getReference(bean, JsonWebToken.class, null));
        }
        if (jsonWebToken == null || !bean.getScope().equals(RequestScoped.class)) {
            logger.warning(String.format("Can't retrieve claim %s. No active principal.", name));
            return null;
        }

        final Optional<T> claimValue = jsonWebToken.claim(name);
        logger.finest(String.format("Found ClaimValue=%s for name=%s", claimValue, name));
        return claimValue.orElse(null);
    }

    private JsonValue toJson(final String name) {
        final T claimValue = getClaimValue(name);
        return wrapValue(claimValue);
    }

    private static final String TMP = "tmp";

    private JsonValue wrapValue(final Object value) {
        JsonValue jsonValue = null;

        if (JsonValue.class.isInstance(value)) {
            // This may already be a JsonValue
            jsonValue = JsonValue.class.cast(value);

        } else if (String.class.isInstance(value)) {
            jsonValue = Json.createObjectBuilder()
                    .add(TMP, value.toString())
                    .build()
                    .getJsonString(TMP);

        } else if (Number.class.isInstance(value)) {
            final Number number = Number.class.cast(value);
            if ((Long.class.isInstance(number)) || (Integer.class.isInstance(number))) {
                jsonValue = Json.createObjectBuilder()
                        .add(TMP, number.longValue())
                        .build()
                        .getJsonNumber(TMP);

            } else {
                jsonValue = Json.createObjectBuilder()
                        .add(TMP, number.doubleValue())
                        .build()
                        .getJsonNumber(TMP);
            }

        } else if (Boolean.class.isInstance(value)) {
            final Boolean flag = Boolean.class.cast(value);
            jsonValue = flag ? JsonValue.TRUE : JsonValue.FALSE;

        } else if (Collection.class.isInstance(value)) {
            final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            final Collection list = Collection.class.cast(value);

            for (Object element : list) {
                if (String.class.isInstance(element)) {
                    arrayBuilder.add(element.toString());

                } else {
                    final JsonValue jvalue = wrapValue(element);
                    arrayBuilder.add(jvalue);
                }
            }
            jsonValue = arrayBuilder.build();

        } else if (Map.class.isInstance(value)) {
            jsonValue = jsonb.fromJson(jsonb.toJson(value), JsonObject.class);

        }
        return jsonValue;
    }

    private boolean isOptional(final ParameterizedType type) {
        return ((Class) type.getRawType()).isAssignableFrom(Optional.class);
    }

    private boolean isSet(final ParameterizedType type) {
        return ((Class) type.getRawType()).isAssignableFrom(Set.class);
    }

    private boolean isList(final ParameterizedType type) {
        return ((Class) type.getRawType()).isAssignableFrom(List.class);
    }

    private static class ClaimLiteral extends AnnotationLiteral<Claim> implements Claim {

        @Override
        public String value() {
            return "";
        }

        @Override
        public Claims standard() {
            return Claims.UNKNOWN;
        }
    }

}
