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

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtContext;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JwtValidatorsTest {
    @Test
    public void expirationValid() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("exp", Instant.now().getEpochSecond() + 100));

        String result = JwtValidators.EXPIRATION.validate(jwt);
        assertNull(result);
    }

    @Test
    public void expirationInvalid() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("exp", Instant.now().getEpochSecond() - 100));

        String result = JwtValidators.EXPIRATION.validate(jwt);
        assertNotNull(result);
        assertTrue(result.startsWith("exp is not in the future"));
    }
    @Test
    public void issuedAtValid() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("iat", Instant.now().getEpochSecond() - 100));

        String result = JwtValidators.ISSUED_AT.validate(jwt);
        assertNull(result);
    }

    @Test
    public void issuedAtInvalid() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("iat", Instant.now().getEpochSecond() + 100));

        String result = JwtValidators.ISSUED_AT.validate(jwt);
        assertNotNull(result);
        assertTrue(result.startsWith("iat is in the future"));
    }

    @Test
    public void notBeforeNotPresent() throws Exception {
        JwtContext jwt = createJwtContext(Map.of());

        String result = JwtValidators.NOT_BEOFRE.validate(jwt);
        assertNull(result);
    }

    @Test
    public void notBeforeValid() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("nbf", Instant.now().getEpochSecond() - 100));

        String result = JwtValidators.NOT_BEOFRE.validate(jwt);
        assertNull(result);
    }

    @Test
    public void notBeforeInvalid() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("nbf", Instant.now().getEpochSecond() + 100));

        String result = JwtValidators.NOT_BEOFRE.validate(jwt);
        assertNotNull(result);
        assertTrue(result.startsWith("nbf is in the future"));
    }

    @Test
    public void nonceValid() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("nonce", "foo"));

        String result = JwtValidators.nonce("foo").validate(jwt);
        assertNull(result);
    }

    @Test
    public void nonceInvalid() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("nonce", "bar"));

        String result = JwtValidators.nonce("foo").validate(jwt);
        assertEquals("nonce value does not match the stored value (expected foo but got bar)", result);
    }

    @Test
    public void nonceNotPresent() throws Exception {
        JwtContext jwt = createJwtContext(Map.of());

        String result = JwtValidators.nonce("foo").validate(jwt);
        assertEquals("nonce value does not match the stored value (expected foo but got null)", result);
    }

    @Test
    public void singleAudience() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("aud", "tomee"));

        String result = JwtValidators.azp("tomee").validate(jwt);
        assertNull(result);
    }

    @Test
    public void singleAudienceInList() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("aud", List.of("tomee")));

        String result = JwtValidators.azp("tomee").validate(jwt);
        assertNull(result);
    }

    @Test
    public void multipleAudienceMissingAuthorizedParty() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("aud", List.of("tomee1", "tomee2")));

        String result = JwtValidators.azp("tomee").validate(jwt);
        assertEquals("aud has 2 entries (tomee1, tomee2) but no azp claim is present", result);
    }

    @Test
    public void invalidAuthorizedParty() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("aud", List.of("tomee1", "tomee2"), "azp", "tomee2"));

        String result = JwtValidators.azp("tomee").validate(jwt);
        assertEquals("azp is not equal to configured clientId (got tomee2 but expected tomee)", result);
    }

    @Test
    public void invalidAuthorizedSingleAudience() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("aud", List.of("tomee"), "azp", "tomee2"));

        String result = JwtValidators.azp("tomee").validate(jwt);
        assertEquals("azp is not equal to configured clientId (got tomee2 but expected tomee)", result);
    }


    @Test
    public void validAuthorizedParty() throws Exception {
        JwtContext jwt = createJwtContext(Map.of("aud", List.of("tomee1", "tomee2"), "azp", "tomee"));

        String result = JwtValidators.azp("tomee").validate(jwt);
        assertNull(result);
    }

    private JwtContext createJwtContext(Map<String, Object> claims) {
        JwtClaims jwtClaims = new JwtClaims();
        for (String key : claims.keySet()) {
            jwtClaims.setClaim(key, claims.get(key));
        }

        return new JwtContext(jwtClaims, null);
    }
}