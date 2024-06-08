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
package org.apache.tomee.security.http.openid;

import org.jose4j.jwt.consumer.Validator;

import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import java.time.Instant;
import java.util.List;

/**
 * Various jose.4.j validators validating aspects as defined by the security 3 spec
 */
public class JwtValidators {

    // The expiration claim must be present and must be 'in the future' (a clock skew might be considered or configured in an implementation specific way)
    public static final Validator EXPIRATION = context -> {
        long exp = context.getJwtClaims().getClaimValue(OpenIdConstant.EXPIRATION_IDENTIFIER, Long.class);
        long now = Instant.now().getEpochSecond();

        if (exp < now) {
            return OpenIdConstant.EXPIRATION_IDENTIFIER + " is not in the future (exp=" + exp + ", current time is " + now + ")";
        }

        return null;
    };

    // The issued at claim must be present and must be 'in the past' (a clock skew might be considered or configured in an implementation specific way)
    public static final Validator ISSUED_AT = context -> {
        // iat is somehow not in OpenIdConstants?
        long iat = context.getJwtClaims().getClaimValue("iat", Long.class);
        long now = Instant.now().getEpochSecond();

        if (iat > now) {
            return "iat is in the future (iat=" + iat + ", current time is " + now + ")";
        }

        return null;
    };

    // The not before claim can be present and if defined, must be 'in the past' (a clock skew might be considered or configured in an implementation specific way)
    public static final Validator NOT_BEOFRE = context -> {
        // nbf is somehow not in OpenIdConstants?
        Long nbf = context.getJwtClaims().getClaimValue("nbf", Long.class);
        if (nbf == null) {
            return null;
        }

        long now = Instant.now().getEpochSecond();

        if (nbf > now) {
            return "iat is in the future (nbf=" + nbf + ", current time is " + now + ")";
        }

        return null;
    };

    // When nonce usage is configured, verify if the nonce value within the Identity Token is identical to the one that was specified in the authentication request.
    public static Validator nonce(String nonce) {
        return context -> {
            String nonceClaim = context.getJwtClaims().getStringClaimValue("nonce");
            if (!nonce.equals(nonceClaim)) {
                return "Nonce value does not match the stored value (expected " + nonce + " but got " + nonceClaim + ")";
            }

            return null;
        };
    }

    /* - If multiple audience values are returned by the OpenID Connect Provider, an authorized party claim (azp) must be present.
       - If an authorized party claim (azp) is present, it must match the OpenIdAuthenticationMechanismDefinition.clientId */
    public static Validator azp(String clientId) {
        return context -> {
            List<String> aud = context.getJwtClaims().getAudience();
            if (aud.size() > 1) {
                String azp = context.getJwtClaims().getStringClaimValue("azp");
                if (azp == null) {
                    return OpenIdConstant.AUDIENCE + " has " + aud.size() + " entries (" + String.join(", " + aud) + ") but no " + OpenIdConstant.AUTHORIZED_PARTY + " claim is present";
                }

                if (!clientId.equals(azp)) {
                    return OpenIdConstant.AUTHORIZED_PARTY + " is not equal to configured clientId (got " + azp + " but expected " + clientId + ")";
                }
            }

            return null;
        };
    }
}
