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
public class JWTAuthContextInfo {
    private static final Logger logger = Logger.getLogger(JWTAuthContextInfo.class.getName());
    public static final String DEFAULT_KEY = "DEFAULT";

    private Map<String, Key> signerKeys;
    private String issuedBy;
    private int expGracePeriodSecs = 60;

    private JWTAuthContextInfo(final Key signerKey, final String issuedBy) {
        this.signerKeys = Collections.singletonMap(DEFAULT_KEY, signerKey);
        this.issuedBy = issuedBy;
    }

    private JWTAuthContextInfo(final Map<String, Key> signerKeys, final String issuedBy) {
        if (signerKeys.size() == 1) {
            final Key singleKey = signerKeys.values().iterator().next();
            this.signerKeys = Collections.singletonMap(DEFAULT_KEY, singleKey);
        } else {
            this.signerKeys = Collections.unmodifiableMap(signerKeys);
        }
        this.issuedBy = issuedBy;
    }

    public static JWTAuthContextInfo authContextInfo(final Key signerKey, final String issuedBy) {
        return new JWTAuthContextInfo(signerKey, issuedBy);
    }

    public static JWTAuthContextInfo authContextInfo(final Map<String, Key> signerKeys, final String issuedBy) {
        return new JWTAuthContextInfo(signerKeys, issuedBy);
    }

    public boolean isSingleKey() {
        return signerKeys.size() == 1;
    }

    public Key getSignerKey() {
        return signerKeys.get(DEFAULT_KEY);
    }

    public List<JsonWebKey> getSignerKeys() {
        return signerKeys.entrySet().stream().map(key -> {
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

    public String getIssuedBy() {
        return issuedBy;
    }

    public int getExpGracePeriodSecs() {
        return expGracePeriodSecs;
    }
}
