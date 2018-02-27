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

import org.apache.tomee.microprofile.jwt.config.JWTAuthContextInfoProvider;
import org.apache.tomee.microprofile.jwt.MPJWTFilter;
import org.apache.tomee.microprofile.jwt.MPJWTInitializer;
import org.apache.tomee.microprofile.jwt.TCKTokenParser;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A CDI extension that provides a producer for the current authenticated JsonWebToken based on a thread
 * local value that is managed by the {@link JWTAuthMechanism} request
 * authentication handler.
 * <p>
 * This also installs the producer methods for the discovered:
 * <ul>
 * <li>@Claim ClaimValue<T> injection sites.</li>
 * <li>@Claim raw type<T> injection sites.</li>
 * <li>@Claim JsonValue injection sites.</li>
 * </ul>
 *
 * @see JWTAuthMechanism
 */
public class MPJWTCDIExtension implements Extension {
    private static Logger log = Logger.getLogger(MPJWTCDIExtension.class.getName());

    /**
     * Register the MPJWTProducer JsonWebToken producer bean
     *
     * @param bbd         before discovery event
     * @param beanManager cdi bean manager
     */
    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager beanManager) {
        log.fine("MPJWTExtension(), added JWTPrincipalProducer");
        bbd.addAnnotatedType(beanManager.createAnnotatedType(TCKTokenParser.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MPJWTFilter.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MPJWTInitializer.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(JWTAuthContextInfoProvider.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(MPJWTProducer.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(RawClaimTypeProducer.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(ClaimValueProducer.class));
        bbd.addAnnotatedType(beanManager.createAnnotatedType(JsonValueProducer.class));
    }

    /**
     * Replace the general producer method BeanAttributes with one bound to the collected injection site
     * types to properly reflect all of the type locations the producer method applies to.
     *
     * @param pba the ProcessBeanAttributes
     * @see ClaimProviderBeanAttributes
     */
    public void addTypeToClaimProducer(@Observes ProcessBeanAttributes pba) {
        if (pba.getAnnotated().isAnnotationPresent(Claim.class)) {
            Claim claim = pba.getAnnotated().getAnnotation(Claim.class);
            if (claim.value().length() == 0 && claim.standard() == Claims.UNKNOWN) {
                log.fine(String.format("addTypeToClaimProducer: %s\n", pba.getAnnotated()));
                BeanAttributes delegate = pba.getBeanAttributes();
                String name = delegate.getName();
                if (delegate.getTypes().contains(Optional.class)) {
                    if (providerOptionalTypes.size() == 0) {
                        providerOptionalTypes.add(Optional.class);
                    }
                    pba.setBeanAttributes(new ClaimProviderBeanAttributes(delegate, providerOptionalTypes, providerQualifiers));
                    // This is
                } else if (name != null && name.startsWith("RawClaimTypeProducer#")) {
                    if (rawTypes.size() == 0) {
                        rawTypes.add(Object.class);
                    }
                    pba.setBeanAttributes(new ClaimProviderBeanAttributes(delegate, rawTypes, rawTypeQualifiers));
                    log.fine(String.format("Setup RawClaimTypeProducer BeanAttributes"));
                }
            }
        }
    }

    public void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
    }

    void doProcessProducers(@Observes ProcessProducer pp) {
    }

    /**
     * Handle the non-{@linkplain Provider}, {@linkplain org.eclipse.microprofile.jwt.ClaimValue}, and
     * {@linkplain javax.json.JsonValue} claim injection types.
     *
     * @param pip - the injection point event information
     * @see RawClaimTypeProducer
     */
    void processClaimInjections(@Observes ProcessInjectionPoint pip) {
        log.fine(String.format("pipRaw: %s", pip.getInjectionPoint()));
        InjectionPoint ip = pip.getInjectionPoint();
        if (ip.getAnnotated().isAnnotationPresent(Claim.class)) {
            Claim claim = ip.getAnnotated().getAnnotation(Claim.class);
            if (ip.getType() instanceof Class) {
                Class rawClass = (Class) ip.getType();
                // Primative types
                if (Modifier.isFinal(rawClass.getModifiers())) {
                    rawTypes.add(ip.getType());
                    rawTypeQualifiers.add(claim);
                    log.fine(String.format("+++ Added Claim raw type: %s", ip.getType()));
                    Class declaringClass = ip.getMember().getDeclaringClass();
                    Annotation[] appScoped = declaringClass.getAnnotationsByType(ApplicationScoped.class);
                    Annotation[] sessionScoped = declaringClass.getAnnotationsByType(SessionScoped.class);
                    if ((appScoped != null && appScoped.length > 0) || (sessionScoped != null && sessionScoped.length > 0)) {
                        String err = String.format("A raw type cannot be injected into application/session scope: IP=%s", ip);
                        pip.addDefinitionError(new DeploymentException(err));
                    }
                }
                // This handles collections of primative types
            } else if (isRawParameterizedType(ip.getType())) {
                log.fine(String.format("+++ Added Claim ParameterizedType: %s", ip.getType()));
                rawTypes.add(ip.getType());
                rawTypeQualifiers.add(claim);
            }
        } else {
            log.fine(String.format("Skipping pip: %s, type: %s/%s", ip, ip.getType(), ip.getType().getClass()));
        }
    }

    /**
     * Collect the types of all {@linkplain Provider} injection points annotated with {@linkplain Claim}.
     *
     * @param pip - the injection point event information
     */
    void processClaimProviderInjections(@Observes ProcessInjectionPoint<?, ? extends Provider> pip) {
        log.fine(String.format("pip: %s", pip.getInjectionPoint()));
        final InjectionPoint ip = pip.getInjectionPoint();
        if (ip.getAnnotated().isAnnotationPresent(Claim.class)) {
            Claim claim = ip.getAnnotated().getAnnotation(Claim.class);
            if (claim.value().length() == 0 && claim.standard() == Claims.UNKNOWN) {
                pip.addDefinitionError(new DeploymentException("@Claim at: " + ip + " has no name or valid standard enum setting"));
            }
            boolean usesEnum = claim.standard() != Claims.UNKNOWN;
            final String claimName = usesEnum ? claim.standard().name() : claim.value();
            log.fine(String.format("Checking Provider Claim(%s), ip: %s", claimName, ip));
            ClaimIP claimIP = claims.get(claimName);
            Type matchType = ip.getType();
            // The T from the Provider<T> injection site
            Type actualType = ((ParameterizedType) matchType).getActualTypeArguments()[0];
            // Don't add Optional or JsonValue as this is handled specially
            if (!optionalOrJsonValue(actualType)) {
                rawTypes.add(actualType);
            } else if (!actualType.getTypeName().startsWith("javax.json.Json")) {
                // Validate that this is not an Optional<JsonValue>
                Type innerType = ((ParameterizedType) actualType).getActualTypeArguments()[0];
                if (!innerType.getTypeName().startsWith("javax.json.Json")) {
                    providerOptionalTypes.add(actualType);
                    providerQualifiers.add(claim);
                }
            }
            rawTypeQualifiers.add(claim);
            ClaimIPType key = new ClaimIPType(claimName, actualType);
            if (claimIP == null) {
                claimIP = new ClaimIP(actualType, actualType, false, claim);
                claimIP.setProviderSite(true);
                claims.put(key, claimIP);
            }
            claimIP.getInjectionPoints().add(ip);
            log.fine(String.format("+++ Added Provider Claim(%s) ip: %s", claimName, ip));

        }
    }

    /**
     * Create producer methods for each ClaimValue injection site
     *
     * @param event       - AfterBeanDiscovery
     * @param beanManager - CDI bean manager
     */
    void observesAfterBeanDiscovery(@Observes final AfterBeanDiscovery event, final BeanManager beanManager) {
        log.fine(String.format("observesAfterBeanDiscovery, %s", claims));
        installClaimValueProducerMethodsViaSyntheticBeans(event, beanManager);

        //installClaimValueProducesViaTemplateType(event, beanManager);
    }

    /**
     * Create a synthetic bean with a custom Producer for the non-Provider injection sites.
     *
     * @param event       - AfterBeanDiscovery
     * @param beanManager - CDI bean manager
     */
    private void installClaimValueProducerMethodsViaSyntheticBeans(final AfterBeanDiscovery event, final BeanManager beanManager) {

    }

    private boolean optionalOrJsonValue(Type type) {
        boolean isOptionOrJson = type.getTypeName().startsWith(Optional.class.getTypeName())
                | type.getTypeName().startsWith("javax.json.Json");
        return isOptionOrJson;
    }

    private boolean isRawParameterizedType(Type type) {
        boolean isRawParameterizedType = false;
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = ParameterizedType.class.cast(type);
            Type rawType = ptype.getRawType();
            String rawTypeName = rawType.getTypeName();
            isRawParameterizedType = !rawTypeName.startsWith("org.eclipse.microprofile.jwt");
        }
        return isRawParameterizedType;
    }

    /**
     * A map of claim,type pairs to the injection site information
     */
    private HashMap<ClaimIPType, ClaimIP> claims = new HashMap<>();

    private Set<Type> providerOptionalTypes = new HashSet<>();

    private Set<Type> providerTypes = new HashSet<>();

    private Set<Type> rawTypes = new HashSet<>();

    private Set<Annotation> rawTypeQualifiers = new HashSet<>();

    private Set<Annotation> providerQualifiers = new HashSet<>();

    /**
     * A key for a claim,injection site type pair
     */
    public static class ClaimIPType implements Comparable<ClaimIPType> {
        public ClaimIPType(String claimName, Type ipType) {
            this.claimName = claimName;
            this.ipType = ipType;
        }

        /**
         * Order the @Claim ClaimValue<T> on the @Claim.value and then T type name
         *
         * @param o - ClaimIP to compare to
         * @return the ordering of this claim relative to o
         */
        @Override
        public int compareTo(ClaimIPType o) {
            int compareTo = claimName.compareTo(o.claimName);
            if (compareTo == 0) {
                compareTo = ipType.getTypeName().compareTo(o.ipType.getTypeName());
            }
            return compareTo;
        }

        private String claimName;

        private Type ipType;
    }

    /**
     * The representation of an @Claim annotated injection site
     */
    public static class ClaimIP {
        /**
         * Create a ClaimIP from the injection site information
         *
         * @param matchType  - the outer type of the injection site
         * @param valueType  - the parameterized type of the injection site
         * @param isOptional - is the injection site an Optional
         * @param claim      - the Claim qualifier
         */
        public ClaimIP(Type matchType, Type valueType, boolean isOptional, Claim claim) {
            this.matchType = matchType;
            this.valueType = valueType;
            this.claim = claim;
        }

        public Type getMatchType() {
            return matchType;
        }

        public String getClaimName() {
            return claim.standard() == Claims.UNKNOWN ? claim.value() : claim.standard().name();
        }

        public Claim getClaim() {
            return claim;
        }

        public Type getValueType() {
            return valueType;
        }

        public boolean isOptional() {
            return isOptional;
        }

        public boolean isProviderSite() {
            return isProviderSite;
        }

        public void setProviderSite(boolean providerSite) {
            this.isProviderSite = providerSite;
        }

        public boolean isNonStandard() {
            return isNonStandard;
        }

        public void setNonStandard(boolean nonStandard) {
            isNonStandard = nonStandard;
        }

        public boolean isJsonValue() {
            return isJsonValue;
        }

        public void setJsonValue(boolean jsonValue) {
            isJsonValue = jsonValue;
        }

        public Set<InjectionPoint> getInjectionPoints() {
            return injectionPoints;
        }

        @Override
        public String toString() {
            return "ClaimIP{" +
                    "type=" + matchType +
                    ", claim=" + claim +
                    ", ips=" + injectionPoints +
                    '}';
        }

        /**
         * The injection site value type
         */
        private Type matchType;

        /**
         * The actual type of of the ParameterizedType matchType
         */
        private Type valueType;

        /**
         * Is valueType actually wrapped in an Optional
         */
        private boolean isOptional;

        private boolean isProviderSite;

        private boolean isNonStandard;

        private boolean isJsonValue;

        /**
         * The injection site @Claim annotation value
         */
        private Claim claim;

        /**
         * The location that share the @Claim/type combination
         */
        private HashSet<InjectionPoint> injectionPoints = new HashSet<>();
    }
}