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
import java.util.List;

/**
 * The public key and expected issuer needed to validate a token.
 */
public class JWTAuthContextInfo {
    private List<Key> signerKeys;
    private String issuedBy;
    private int expGracePeriodSecs = 60;

    private JWTAuthContextInfo(final Key signerKey, final String issuedBy) {
        this.signerKeys = Collections.singletonList(signerKey);
        this.issuedBy = issuedBy;
    }

    private JWTAuthContextInfo(final List<Key> signerKeys, final String issuedBy) {
        this.signerKeys = Collections.unmodifiableList(signerKeys);
        this.issuedBy = issuedBy;
    }

    public static JWTAuthContextInfo authContextInfo(final Key signerKey, final String issuedBy) {
        return new JWTAuthContextInfo(signerKey, issuedBy);
    }

    public static JWTAuthContextInfo authContextInfo(final List<Key> signerKeys, final String issuedBy) {
        return new JWTAuthContextInfo(signerKeys, issuedBy);
    }

    public List<Key> getSignerKeys() {
        return signerKeys;
    }

    public Key getSignerKey(final String kid) {
        return signerKeys.get(0);
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public int getExpGracePeriodSecs() {
        return expGracePeriodSecs;
    }
}
