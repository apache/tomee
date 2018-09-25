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
package org.apache.tomee.microprofile.jwt.principal;

import org.apache.tomee.microprofile.jwt.ParseException;
import org.apache.tomee.microprofile.jwt.config.JWTAuthContextInfo;
import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;

/**
 * A default implementation of the abstract JWTCallerPrincipalFactory that uses the Keycloak token parsing classes.
 */
public class DefaultJWTCallerPrincipalFactory extends JWTCallerPrincipalFactory {

    /**
     * Tries to load the JWTAuthContextInfo from CDI if the class level authContextInfo has not been set.
     */
    public DefaultJWTCallerPrincipalFactory() {
    }

    @Override
    public JWTCallerPrincipal parse(final String token, final JWTAuthContextInfo authContextInfo) throws ParseException {
        JWTCallerPrincipal principal;

        try {
            final JwtConsumerBuilder builder = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setRequireSubject()
                    .setSkipDefaultAudienceValidation()
                    .setExpectedIssuer(authContextInfo.getIssuedBy())
                    .setJwsAlgorithmConstraints(
                            new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                    AlgorithmIdentifiers.RSA_USING_SHA256));

            if (authContextInfo.getExpGracePeriodSecs() > 0) {
                builder.setAllowedClockSkewInSeconds(authContextInfo.getExpGracePeriodSecs());
            } else {
                builder.setEvaluationTime(NumericDate.fromSeconds(0));
            }

            if (authContextInfo.isSingleKey()) {
                builder.setVerificationKey(authContextInfo.getSignerKey());
            } else {
                builder.setVerificationKeyResolver(new JwksVerificationKeyResolver(authContextInfo.getSignerKeys()));
            }

            final JwtConsumer jwtConsumer = builder.build();
            final JwtContext jwtContext = jwtConsumer.process(token);
            final String type = jwtContext.getJoseObjects().get(0).getHeader("typ");
            //  Validate the JWT and process it to the Claims
            jwtConsumer.processContext(jwtContext);
            JwtClaims claimsSet = jwtContext.getJwtClaims();

            // We have to determine the unique name to use as the principal name. It comes from upn, preferred_username, sub in that order
            String principalName = claimsSet.getClaimValue("upn", String.class);
            if (principalName == null) {
                principalName = claimsSet.getClaimValue("preferred_username", String.class);
                if (principalName == null) {
                    principalName = claimsSet.getSubject();
                }
            }
            claimsSet.setClaim(Claims.raw_token.name(), token);
            principal = new DefaultJWTCallerPrincipal(token, type, claimsSet, principalName);

        } catch (final InvalidJwtException e) {
            throw new ParseException("Failed to verify token", e);

        } catch (final MalformedClaimException e) {
            throw new ParseException("Failed to verify token claims", e);
        }

        return principal;
    }
}
