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
package org.apache.tomee.microprofile.jwt.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.servlet.ServletContext;
import org.apache.openejb.util.CachedSupplier;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Logger;
import org.apache.tomee.microprofile.jwt.JWTLogCategories;
import org.apache.tomee.microprofile.jwt.keys.DecryptKeys;
import org.apache.tomee.microprofile.jwt.keys.FixedKeys;
import org.apache.tomee.microprofile.jwt.keys.PublicKeys;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.eclipse.microprofile.jwt.config.Names.AUDIENCES;
import static org.eclipse.microprofile.jwt.config.Names.ISSUER;
import static org.eclipse.microprofile.jwt.config.Names.TOKEN_COOKIE;
import static org.eclipse.microprofile.jwt.config.Names.TOKEN_HEADER;
import static org.eclipse.microprofile.jwt.config.Names.VERIFIER_PUBLIC_KEY;
import static org.eclipse.microprofile.jwt.config.Names.VERIFIER_PUBLIC_KEY_LOCATION;
import static org.eclipse.microprofile.jwt.config.Names.TOKEN_AGE;
import static org.eclipse.microprofile.jwt.config.Names.CLOCK_SKEW;

/**
 * The purpose of this class is to create an instance of JWTAuthConfiguration using
 * the MicroProfile JWT 1.1 standard configuration properties.  These are supplied
 * through the MicroProfile Config specification.
 *
 * If the user @Produces an instance of JWTAuthConfiguration via a CDI bean they create,
 * then this class will never be called.
 */
@ApplicationScoped
public class JWTAuthConfigurationProperties {
    public static final String PUBLIC_KEY_ERROR = "Could not read MicroProfile Public Key";
    public static final String PUBLIC_KEY_ERROR_LOCATION = PUBLIC_KEY_ERROR + " from Location: ";
    private static final Logger CONFIGURATION = Logger.getInstance(JWTLogCategories.CONFIG, JWTAuthConfigurationProperties.class);

    private Config config;
    private JWTAuthConfiguration jwtAuthConfiguration;

    public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
        this.config = ConfigProvider.getConfig();
        this.jwtAuthConfiguration = createJWTAuthConfiguration();
    }

    public Optional<JWTAuthConfiguration> getJWTAuthConfiguration() {
        return Optional.ofNullable(jwtAuthConfiguration);
    }

    private Optional<String> getVerifierPublicKey() {
        return config.getOptionalValue(VERIFIER_PUBLIC_KEY, String.class).map(s -> s.isEmpty() ? null : s);
    }

    private Optional<String> getPublicKeyLocation() {
        return config.getOptionalValue(VERIFIER_PUBLIC_KEY_LOCATION, String.class).map(s -> s.isEmpty() ? null : s);
    }

    private Optional<String> getIssuer() {
        return config.getOptionalValue(ISSUER, String.class);
    }

    private List<String> getAudiences() {
        final String audiences = config.getOptionalValue(AUDIENCES, String.class).orElse(null);
        if (audiences == null) return Collections.EMPTY_LIST;
        return Arrays.asList(audiences.split(" *, *"));
    }

    private JWTAuthConfiguration createJWTAuthConfiguration() {
        if (getVerifierPublicKey().isPresent() && getPublicKeyLocation().isPresent()) {
            throw new DeploymentException("Both " +
                    VERIFIER_PUBLIC_KEY +
                    " and " +
                    VERIFIER_PUBLIC_KEY_LOCATION +
                    " are being supplied. You must use only one.");
        }

        final List<String> audiences = getAudiences();

        final Supplier<Map<String, Key>> publicKeys = Keys.VERIFY.configure(config);
        final Supplier<Map<String, Key>> decryptKeys = Keys.DECRYPT.configure(config);

        final Boolean allowNoExp = queryAllowExp();
        
        return new JWTAuthConfiguration(
                publicKeys,
                getIssuer().orElse(null),
                allowNoExp,
                audiences.toArray(new String[0]),
                decryptKeys,
                config.getOptionalValue(TOKEN_HEADER, String.class).map(String::toLowerCase).orElse("authorization"),
                config.getOptionalValue(TOKEN_COOKIE, String.class).map(String::toLowerCase).orElse("bearer"),
                config.getOptionalValue("mp.jwt.decrypt.key.algorithm", String.class).orElse(null),
                config.getOptionalValue("mp.jwt.verify.publickey.algorithm", String.class).orElse(null),
                config.getOptionalValue(TOKEN_AGE, Integer.class).orElse(null),
                config.getOptionalValue(CLOCK_SKEW, Integer.class).orElse(0));
    }
  
    private Boolean queryAllowExp(){
        final Optional<Boolean> allowExp = config.getOptionalValue("tomee.mp.jwt.allow.no-exp", Boolean.class);
        final Optional<Boolean> allowExpDeprecatedValue = config.getOptionalValue("mp.jwt.tomee.allow.no-exp", Boolean.class);

        if (allowExpDeprecatedValue.isPresent()) {
            CONFIGURATION.warning("mp.jwt.tomee.allow.no-exp property is deprecated, use tomee.mp.jwt.allow.no-exp property instead.");
        }

        return allowExp
                .or(() -> allowExpDeprecatedValue)
                .orElse(false);
    }
    
    enum Keys {
        VERIFY("mp.jwt.verify.publickey", "tomee.jwt.verify.publickey"),
        DECRYPT("mp.jwt.decrypt.key", "tomee.jwt.decrypt.key");

        private final String mpPrefix;
        private final String tomeePrefix;

        Keys(final String mpPrefix, final String tomeePrefix) {
            this.mpPrefix = mpPrefix;
            this.tomeePrefix = tomeePrefix;
        }

        public Supplier<Map<String, Key>> configure(final Config config) {
            final Options options = new Options(config);
            final Optional<String> contents = options.contents();
            final Optional<String> location = options.location();

            if (contents.isEmpty() && location.isEmpty()) return new Unset();

            final Supplier<Map<String, Key>> supplier;

            switch (this) {
                case VERIFY: supplier = new PublicKeys(contents, location);
                    break;
                case DECRYPT: supplier = new DecryptKeys(contents, location);
                    break;
                default: throw new IllegalArgumentException("Unsupported enum value: " + this);
            }

            if (options.cached()) {
                return CachedSupplier.builder(supplier)
                        .refreshInterval(options.refreshInterval())
                        .initialRetryDelay(options.initialRetryDelay())
                        .maxRetryDelay(options.maxRetryDelay())
                        .accessTimeout(options.accessTimeout())
                        .logger(Logger.getInstance(JWTLogCategories.KEYS, supplier.getClass()))
                        .build();
            }

            return new FixedKeys(supplier.get());
        }

        class Options {
            private final Config config;

            public Options(final Config config) {
                this.config = config;
            }

            Optional<String> contents() {
                return config.getOptionalValue(mpPrefix, String.class);
            }

            Optional<String> location() {
                return config.getOptionalValue(mpPrefix + ".location", String.class);
            }

            boolean cached() {
                final boolean cacheByDefault = location().filter(s -> s.startsWith("http")).isPresent();
                return config.getOptionalValue(tomeePrefix + ".cache", Boolean.class).orElse(cacheByDefault);
            }

            Duration initialRetryDelay() {
                return config.getOptionalValue(tomeePrefix + ".cache.initialRetryDelay", Duration.class)
                        .orElse(new Duration(2, TimeUnit.SECONDS));
            }

            Duration maxRetryDelay() {
                return config.getOptionalValue(tomeePrefix + ".cache.maxRetryDelay", Duration.class)
                        .orElse(new Duration(1, TimeUnit.HOURS));
            }

            Duration accessTimeout() {
                return config.getOptionalValue(tomeePrefix + ".cache.accessTimeout", Duration.class)
                        .orElse(new Duration(30, TimeUnit.SECONDS));
            }

            Duration refreshInterval() {
                return config.getOptionalValue(tomeePrefix + ".cache.refreshInterval", Duration.class)
                        .orElse(new Duration(1, TimeUnit.DAYS));
            }
        }
    }

    public static class Unset implements Supplier<Map<String, Key>> {
        @Override
        public Map<String, Key> get() {
            return Collections.EMPTY_MAP;
        }
    }

}
