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

import java.security.Key;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The public key and expected issuer needed to validate a token.
 */
public class JWTAuthConfiguration {
    public static final String DEFAULT_KEY = "DEFAULT";

    private final Supplier<Map<String, Key>> publicKeys;
    private final Supplier<Map<String, Key>> decryptKeys;
    private final String[] audiences;
    private final String issuer;
    private final int expGracePeriodSecs = 60;
    private final String headerName;
    private final String headerScheme = "Bearer";
    private final boolean allowNoExpiryClaim;
    private final String cookieName;
    private final Integer tokenAge;
    private final Integer clockSkew;

    /**
     * mp.jwt.verify.publickey.algorithm
     *
     * The mp.jwt.verify.publickey.algorithm configuration property allows for
     * specifying which Public Key Signature Algorithm is supported by the MP JWT endpoint.
     */
    private String signatureAlgorithm;

    /**
     * mp.jwt.decrypt.key.algorithm
     *
     * The mp.jwt.decrypt.key.algorithm configuration property allows for specifying which key
     * management key algorithm is supported by the MP JWT endpoint. Algorithms which must be
     * supported are either RSA-OAEP or RSA-OAEP-256.
     */
    private String decryptAlgorithm;

    public JWTAuthConfiguration(final Supplier<Map<String, Key>> publicKeys, final String issuer, final boolean allowNoExpiryClaim, final String[] audiences, final Supplier<Map<String, Key>> decryptKeys, final String header, final String cookie, final String decryptAlgorithm, final String signatureAlgorithm, final Integer tokenAge, final Integer clockSkew) {
        this.publicKeys = publicKeys;
        this.decryptKeys = decryptKeys;
        this.issuer = issuer;
        this.allowNoExpiryClaim = allowNoExpiryClaim;
        this.audiences = audiences;
        this.headerName = header;
        this.cookieName = cookie;
        this.decryptAlgorithm = decryptAlgorithm;
        this.signatureAlgorithm = signatureAlgorithm;
        this.tokenAge = tokenAge;
        this.clockSkew = clockSkew;
    }

    public String getCookieName() {
        return cookieName;
    }

    public String[] getAudiences() {
        return audiences;
    }

    public Map<String, Key> getPublicKeys() {
        return publicKeys.get();
    }

    public Map<String, Key> getDecryptKeys() {
        return decryptKeys.get();
    }

    public String getIssuer() {
        return issuer;
    }

    public int getExpGracePeriodSecs() {
        return expGracePeriodSecs;
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getHeaderScheme() {
        return headerScheme;
    }

    public boolean isAllowNoExpiryClaim() {
        return allowNoExpiryClaim;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public String getDecryptAlgorithm() {
        return decryptAlgorithm;
    }
    
    public Integer getTokenAge() {
        return tokenAge;
    }
    
    public Integer getClockSkew() {
        return clockSkew;
    }
}
