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
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.microprofile.jwt.config.Names.AUDIENCES;
import static org.eclipse.microprofile.jwt.config.Names.DECRYPTOR_KEY_LOCATION;
import static org.eclipse.microprofile.jwt.config.Names.ISSUER;
import static org.eclipse.microprofile.jwt.config.Names.TOKEN_COOKIE;
import static org.eclipse.microprofile.jwt.config.Names.TOKEN_HEADER;
import static org.eclipse.microprofile.jwt.config.Names.VERIFIER_PUBLIC_KEY;
import static org.eclipse.microprofile.jwt.config.Names.VERIFIER_PUBLIC_KEY_LOCATION;

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

        final Optional<String> publicKeyContents = getVerifierPublicKey();
        final Optional<String> publicKeyLocation = getPublicKeyLocation();
        final List<String> audiences = getAudiences();

        final Optional<String> decryptorKeyLocation = config.getOptionalValue(DECRYPTOR_KEY_LOCATION, String.class);

        final KeyResolver resolver = new KeyResolver();
        final Map<String, Key> publicKeys = resolver.resolvePublicKey(publicKeyContents, publicKeyLocation).orElse(null);
        final Map<String, Key> decryptkeys = resolver.resolveDecryptKey(Optional.empty(), decryptorKeyLocation).orElse(null);

        final Boolean allowNoExp = config.getOptionalValue("mp.jwt.tomee.allow.no-exp", Boolean.class).orElse(false);

        return new JWTAuthConfiguration(
                publicKeys,
                getIssuer().orElse(null),
                allowNoExp,
                audiences.toArray(new String[0]),
                decryptkeys,
                config.getOptionalValue(TOKEN_HEADER, String.class).map(String::toLowerCase).orElse("authorization"),
                config.getOptionalValue(TOKEN_COOKIE, String.class).map(String::toLowerCase).orElse("bearer"),
                config.getOptionalValue("mp.jwt.decrypt.key.algorithm", String.class).orElse( null),
                config.getOptionalValue("mp.jwt.verify.publickey.algorithm", String.class).orElse( null));
    }

}
