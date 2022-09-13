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
import java.util.Collections;
import java.util.Map;

/**
 * The public key and expected issuer needed to validate a token.
 */
public class JWTAuthConfiguration {
    public static final String DEFAULT_KEY = "DEFAULT";

    private Map<String, Key> publicKeys;
    private Map<String, Key> decryptKeys;
    private String[] audiences;
    private String issuer;
    private int expGracePeriodSecs = 60;
    private String headerName = "Authorization";
    private String headerScheme = "Bearer";
    private boolean allowNoExpiryClaim = false;
    private String cookieName = "Bearer";

    public JWTAuthConfiguration(final Map<String, Key> publicKeys, final String issuer, final boolean allowNoExpiryClaim, final String[] audiences, final Map<String, Key> decryptKeys, final String header, final String cookie) {
        if (publicKeys == null) {
            this.publicKeys = Collections.EMPTY_MAP;
        } else if (publicKeys.size() == 1) {
            final Key singleKey = publicKeys.values().iterator().next();
            this.publicKeys = Collections.singletonMap(DEFAULT_KEY, singleKey);
        } else {
            this.publicKeys = Collections.unmodifiableMap(publicKeys);
        }

        if (decryptKeys == null) {
            this.decryptKeys = Collections.EMPTY_MAP;
        } else {
            this.decryptKeys = Collections.unmodifiableMap(decryptKeys);
        }

        this.issuer = issuer;
        this.allowNoExpiryClaim = allowNoExpiryClaim;
        this.audiences = audiences;
        this.headerName = header;
        this.cookieName = cookie;
    }

    public String getCookieName() {
        return cookieName;
    }

    public String[] getAudiences() {
        return audiences;
    }

    public Key getPublicKey() {
        return publicKeys.get(DEFAULT_KEY);
    }

    public Map<String, Key> getPublicKeys() {
        return publicKeys;
    }

    public Map<String, Key> getDecryptKeys() {
        return decryptKeys;
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

}
