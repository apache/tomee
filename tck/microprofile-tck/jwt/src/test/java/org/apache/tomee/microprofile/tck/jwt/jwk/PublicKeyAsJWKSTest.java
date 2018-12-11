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
package org.apache.tomee.microprofile.tck.jwt.jwk;

import org.apache.tomee.microprofile.jwt.config.ConfigurableJWTAuthContextInfo;
import org.apache.tomee.microprofile.jwt.config.JWTAuthContextInfo;
import org.eclipse.microprofile.jwt.config.Names;
import org.eclipse.microprofile.jwt.tck.TCKConstants;
import org.eclipse.microprofile.jwt.tck.util.TokenUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.HashMap;

import static org.jose4j.jwa.AlgorithmConstraints.ConstraintType.WHITELIST;
import static org.jose4j.jws.AlgorithmIdentifiers.RSA_USING_SHA256;

public class PublicKeyAsJWKSTest {
    @Test
    public void validateJWKS() throws Exception {
        System.setProperty(Names.VERIFIER_PUBLIC_KEY, "");
        System.setProperty(Names.VERIFIER_PUBLIC_KEY_LOCATION, "file://" +
                                                               Paths.get("").toAbsolutePath().toString() +
                                                               "/src/test/resources/signer-keyset4k.jwk");
        System.setProperty(Names.ISSUER, TCKConstants.TEST_ISSUER);

        final PrivateKey privateKey = TokenUtils.readPrivateKey("/privateKey4k.pem");
        final String kid = "publicKey4k";
        final String token = TokenUtils.generateTokenString(privateKey, kid, "/Token1.json", null, new HashMap<>());
        System.out.println("token = " + token);

        final ConfigurableJWTAuthContextInfo configurableJWTAuthContextInfo = new ConfigurableJWTAuthContextInfo();
        configurableJWTAuthContextInfo.init(null);

        final JWTAuthContextInfo jwtAuthContextInfo =
                configurableJWTAuthContextInfo.getJWTAuthContextInfo().orElseThrow(IllegalArgumentException::new);

        final JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setRequireSubject()
                .setSkipDefaultAudienceValidation()
                .setExpectedIssuer(jwtAuthContextInfo.getIssuedBy())
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(WHITELIST, RSA_USING_SHA256))
                .setSkipDefaultAudienceValidation()
                .setVerificationKey(jwtAuthContextInfo.getSignerKey());

        if (jwtAuthContextInfo.getExpGracePeriodSecs() > 0) {
            jwtConsumerBuilder.setAllowedClockSkewInSeconds(jwtAuthContextInfo.getExpGracePeriodSecs());
        } else {
            jwtConsumerBuilder.setEvaluationTime(NumericDate.fromSeconds(0));
        }

        if (jwtAuthContextInfo.isSingleKey()) {
            jwtConsumerBuilder.setVerificationKey(jwtAuthContextInfo.getSignerKey());
        } else {
            jwtConsumerBuilder.setVerificationKeyResolver(new JwksVerificationKeyResolver(jwtAuthContextInfo.getSignerKeys()));
        }

        final JwtConsumer jwtConsumer = jwtConsumerBuilder.build();
        final JwtContext jwtContext = jwtConsumer.process(token);
        Assert.assertEquals(jwtContext.getJwtClaims().getStringClaimValue("upn"), "jdoe@example.com");
    }
}
