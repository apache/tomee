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
package org.apache.tomee.security.cdi.openid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.openid.AccessToken;
import jakarta.security.enterprise.identitystore.openid.IdentityToken;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;
import org.apache.tomee.security.http.openid.JwtValidators;
import org.apache.tomee.security.http.openid.model.TokenResponse;
import org.apache.tomee.security.http.openid.model.TomEEAccesToken;
import org.apache.tomee.security.http.openid.model.TomEEIdentityToken;
import org.apache.tomee.security.http.openid.model.TomEEOpenIdCredential;
import org.apache.tomee.security.http.openid.model.TomEERefreshToken;
import org.jose4j.http.Get;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@ApplicationScoped
public class OpenIdIdentityStore implements IdentityStore {
    private final static Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, OpenIdIdentityStore.class);

    @Inject
    private OpenIdAuthenticationMechanismDefinition definition;

    @Inject
    private TomEEOpenIdContext openIdContext;

    @Inject
    private OpenIdStorageHandler storageHandler;
    

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (!(credential instanceof TomEEOpenIdCredential openIdCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        JwtConsumer defaultJwtConsumer = buildJwtConsumer(null);
        JwtConsumer idTokenJwtConsumer = buildJwtConsumer(builder -> {
            if (!definition.useNonce()) {
                return;
            }

            HttpMessageContext msgContext = openIdCredential.getMessageContext();
            String expectedNonce = storageHandler.getStoredNonce(msgContext.getRequest(), msgContext.getResponse());

            builder.registerValidator(JwtValidators.nonce(expectedNonce));
        });


        openIdContext.setAccessToken(createAccessToken(defaultJwtConsumer, openIdCredential.getTokenResponse()));
        openIdContext.setIdentityToken(createIdentityToken(idTokenJwtConsumer, openIdCredential.getTokenResponse()));
        openIdContext.setRefreshToken(openIdCredential.getTokenResponse().getRefreshToken().map(TomEERefreshToken::new));
        if (openIdContext.getIdentityToken() == null) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        openIdContext.setUserInfoClaims(fetchUserinfoClaims(defaultJwtConsumer, openIdContext.getAccessToken().getToken()));

        String callerNameClaim = definition.claimsDefinition().callerNameClaim();
        String groupsClaim = definition.claimsDefinition().callerGroupsClaim();

        String callerName = null;
        List<String> groups = Collections.emptyList();

        if (openIdContext.getAccessToken().isJWT()) {
            callerName = openIdContext.getAccessToken().getJwtClaims().getStringClaim(callerNameClaim).orElse(null);
            groups = openIdContext.getAccessToken().getJwtClaims().getArrayStringClaim(groupsClaim);
        }

        if (callerName == null) {
            callerName = openIdContext.getIdentityToken().getJwtClaims().getStringClaim(callerNameClaim).orElse(null);
        }

        if (groups.isEmpty()) {
            groups = openIdContext.getIdentityToken().getJwtClaims().getArrayStringClaim(groupsClaim);
        }

        if (callerName == null) {
            callerName = openIdContext.getClaims().getStringClaim(callerNameClaim).orElse(null);
        }

        if (groups.isEmpty()) {
            groups = openIdContext.getClaims().getArrayStringClaim(groupsClaim);
        }

        if (callerName == null) {
            callerName = openIdContext.getSubject();
        }

        return new CredentialValidationResult(callerName, new HashSet<>(groups));
    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        return validationResult.getCallerGroups();
    }

    private AccessToken createAccessToken(JwtConsumer jwtConsumer, TokenResponse tokenResponse) {
        boolean validJwt = false;
        try {
            jwtConsumer.process(tokenResponse.getAccesToken());
            validJwt = true;
        } catch (InvalidJwtException e) {
            LOGGER.warning(OpenIdConstant.ACCESS_TOKEN + " is invalid: " + e.getMessage());
        }

        return new TomEEAccesToken(
                validJwt, tokenResponse.getAccesToken(),
                "Bearer".equals(tokenResponse.getTokenType()) ? AccessToken.Type.BEARER : AccessToken.Type.MAC,
                tokenResponse.getScope(),
                tokenResponse.getExpiresIn(),
                definition.tokenMinValidity());
    }

    private IdentityToken createIdentityToken(JwtConsumer jwtConsumer, TokenResponse tokenResponse) {
        try {
            JwtContext idToken = jwtConsumer.process(tokenResponse.getIdToken());
            return new TomEEIdentityToken(idToken.getJwt(), definition.tokenMinValidity());
        } catch (InvalidJwtException e) {
            LOGGER.warning(OpenIdConstant.IDENTITY_TOKEN + " is invalid: " + e.getMessage());

            return null;
        }
    }

    private JsonObject fetchUserinfoClaims(JwtConsumer jwtConsumer, String accessToken) {
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(definition.providerMetadata().userinfoEndpoint())
                    .request(MediaType.APPLICATION_JSON, "application/jwt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                LOGGER.warning("Could not fetch userinfo, response was "
                        + response.getStatus() + "\n" + response.readEntity(String.class));
                return null;
            }

            String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            if (contentType == null || contentType.startsWith(MediaType.APPLICATION_JSON)) {
                return response.readEntity(JsonObject.class);
            }

            if ("application/jwt".startsWith(contentType)) {
                try {
                    JwtContext context = jwtConsumer.process(response.readEntity(String.class));

                    try (JsonReader reader = Json.createReader(new StringReader(context.getJwtClaims().getRawJson()))) {
                        return reader.readObject();
                    }
                } catch (InvalidJwtException e) {
                    LOGGER.warning("userinfo endpoint response was of type application/jwt but jwt could not be verified", e);

                    return null;
                }
            }

            throw new IllegalStateException("Illegal response from userinfo endpoint received with " + HttpHeaders.CONTENT_TYPE + " " + contentType
                    + ", supported values are " + MediaType.APPLICATION_JSON + " and application/jwt");
        }
    }

    protected JwtConsumer buildJwtConsumer(Consumer<JwtConsumerBuilder> enhancer) {
        HttpsJwks jwks = new HttpsJwks(definition.providerMetadata().jwksURI());
        Get get = new Get();
        get.setConnectTimeout(definition.jwksConnectTimeout());
        get.setReadTimeout(definition.jwksReadTimeout());
        jwks.setSimpleHttpGet(get);

        HttpsJwksVerificationKeyResolver keyResolver = new HttpsJwksVerificationKeyResolver(jwks);
        JwtConsumerBuilder builder = new JwtConsumerBuilder()
                .setRequireSubject()
                .setRequireIssuedAt()
                .setRequireExpirationTime()
                .setVerificationKeyResolver(keyResolver)
                .setExpectedIssuer(definition.providerMetadata().issuer())
                .setExpectedAudience(definition.clientId())
                .registerValidator(JwtValidators.azp(definition.clientId()))
                .registerValidator(JwtValidators.EXPIRATION)
                .registerValidator(JwtValidators.ISSUED_AT)
                .registerValidator(JwtValidators.NOT_BEOFRE);

        if (enhancer != null) {
            enhancer.accept(builder);
        }

        return builder.build();
    }
}
