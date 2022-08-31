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
package org.apache.tomee.microprofile.jwt.config;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The public key and expected issuer needed to validate a token.
 */
public class JWTAuthConfiguration {
    private static final Logger logger = Logger.getLogger(JWTAuthConfiguration.class.getName());
    public static final String DEFAULT_KEY = "DEFAULT";

    private Map<String, Key> publicKeys;
    private String[] audiences;
    private String issuer;
    private int expGracePeriodSecs = 60;
    private String headerName = "Authorization";
    private String headerScheme = "Bearer";
    private boolean allowNoExpiryClaim = false;

    private JWTAuthConfiguration(final Key publicKey, final String issuer, final boolean allowNoExpiryClaim, final String[] audiences) {
        this.publicKeys = Collections.singletonMap(DEFAULT_KEY, publicKey);
        this.issuer = issuer;
        this.allowNoExpiryClaim = allowNoExpiryClaim;
        this.audiences = audiences;
    }

    private JWTAuthConfiguration(final Map<String, Key> publicKeys, final String issuer, final boolean allowNoExpiryClaim, final String[] audiences) {
        if (publicKeys == null) {
            this.publicKeys = Collections.EMPTY_MAP;
        } else if (publicKeys.size() == 1) {
            final Key singleKey = publicKeys.values().iterator().next();
            this.publicKeys = Collections.singletonMap(DEFAULT_KEY, singleKey);
        } else {
            this.publicKeys = Collections.unmodifiableMap(publicKeys);
        }
        this.issuer = issuer;
        this.allowNoExpiryClaim = allowNoExpiryClaim;
        this.audiences = audiences;
    }

    public static JWTAuthConfiguration authConfiguration(final Key publicKey, final String issuer, final boolean allowNoExpiryClaim) {
        return new JWTAuthConfiguration(publicKey, issuer, allowNoExpiryClaim, new String[0]);
    }

    public static JWTAuthConfiguration authConfiguration(final Map<String, Key> publicKeys, final String issuer, final boolean allowNoExpiryClaim) {
        return authConfiguration(publicKeys, issuer, allowNoExpiryClaim, new String[0]);
    }

    public static JWTAuthConfiguration authConfiguration(final Map<String, Key> publicKeys, final String issuer, final boolean allowNoExpiryClaim, final String[] audiences) {
        return new JWTAuthConfiguration(publicKeys, issuer, allowNoExpiryClaim, audiences);
    }

    public String[] getAudiences() {
        return audiences;
    }

    public boolean isSingleKey() {
        return publicKeys.size() == 1;
    }

    public Key getPublicKey() {
        return publicKeys.get(DEFAULT_KEY);
    }

    public List<JsonWebKey> getPublicKeys() {
        return publicKeys.entrySet().stream().map(key -> {
            try {
                final JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(key.getValue());
                jsonWebKey.setKeyId(key.getKey());
                return jsonWebKey;
            } catch (final JoseException e) {
                logger.warning(e.getMessage());
                return null;
            }
        }).collect(Collectors.toList());
    }

    public String getIssuer() {
        return issuer;
    }

    public int getExpGracePeriodSecs() {
        return expGracePeriodSecs;
    }

    public void setExpGracePeriodSecs(final int expGracePeriodSecs) {
        this.expGracePeriodSecs = expGracePeriodSecs;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(final String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderScheme() {
        return headerScheme;
    }

    public void setHeaderScheme(final String headerScheme) {
        this.headerScheme = headerScheme;
    }

    public boolean isAllowNoExpiryClaim() {
        return allowNoExpiryClaim;
    }

    public void setAllowNoExpiryClaim(boolean allowNoExpiryClaim) {
        this.allowNoExpiryClaim = allowNoExpiryClaim;
    }
}
