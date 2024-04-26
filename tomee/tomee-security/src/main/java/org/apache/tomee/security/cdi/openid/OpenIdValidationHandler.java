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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
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
import org.apache.tomee.security.http.openid.model.TomEEAccesToken;
import org.apache.tomee.security.http.openid.model.TomEEIdentityToken;
import org.apache.tomee.security.http.openid.model.TomEEOpenIdCredential;
import org.jose4j.http.Get;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.consumer.ErrorCodeValidator;
import org.jose4j.jwt.consumer.ErrorCodes;
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
import java.util.function.Supplier;

@ApplicationScoped
public class OpenIdValidationHandler implements IdentityStore {
    private final static Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, OpenIdValidationHandler.class);

    @Inject
    private Instance<Supplier<OpenIdAuthenticationMechanismDefinition>> definition;
    @Inject
    private TomEEOpenIdContext openIdContext;

    @PostConstruct
    public void init() {
        if (definition.isUnsatisfied()) {
            throw new IllegalStateException("OpenIdContext is not available if no @OpenIdAuthenticationMechanismDefinition is defined");
        }
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (!(credential instanceof TomEEOpenIdCredential openIdCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        JwtConsumer jwtConsumer = buildJwtConsumer();
        openIdContext.setAccessToken(createAccessToken(jwtConsumer, openIdCredential));
        openIdContext.setIdentityToken(createIdentityToken(jwtConsumer, openIdCredential));
        if (openIdContext.getIdentityToken() == null) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        openIdContext.setUserInfoClaims(fetchUserinfoClaims(jwtConsumer, openIdContext.getAccessToken().getToken()));

        String callerNameClaim = definition.get().get().claimsDefinition().callerNameClaim();
        String groupsClaim = definition.get().get().claimsDefinition().callerGroupsClaim();

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

    private AccessToken createAccessToken(JwtConsumer jwtConsumer, TomEEOpenIdCredential credential) {
        boolean valitJwt = false;
        try {
            jwtConsumer.process(credential.getAccesToken());
            valitJwt = true;
        } catch (InvalidJwtException e) {
            LOGGER.warning("Could not decode " + OpenIdConstant.ACCESS_TOKEN, e);
        }

        return new TomEEAccesToken(
                valitJwt, credential.getAccesToken(),
                "Bearer".equals(credential.getTokenType()) ? AccessToken.Type.BEARER : AccessToken.Type.MAC,
                credential.getScope(),
                credential.getExpiresIn(),
                definition.get().get().tokenMinValidity());
    }

    private IdentityToken createIdentityToken(JwtConsumer jwtConsumer, TomEEOpenIdCredential credential) {
        try {
            JwtContext idToken = jwtConsumer.process(credential.getIdToken());
            return new TomEEIdentityToken(idToken.getJwt(), definition.get().get().tokenMinValidity());
        } catch (InvalidJwtException e) {
            LOGGER.warning(OpenIdConstant.IDENTITY_TOKEN + " is invalid", e);

            return null;
        }
    }

    private JsonObject fetchUserinfoClaims(JwtConsumer jwtConsumer, String accessToken) {
        try (Client client = ClientBuilder.newClient()) {
            Response response = client.target(definition.get().get().providerMetadata().userinfoEndpoint())
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                LOGGER.warning("Could not fetch userinfo, response was "
                        + response.getStatus() + "\n" + response.readEntity(String.class));
                return null;
            }

            String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            if (contentType == null || contentType.equals(MediaType.APPLICATION_JSON)) {
                return response.readEntity(JsonObject.class);
            }

            if ("application/jwt".equals(contentType)) {
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

    protected JwtConsumer buildJwtConsumer() {
        HttpsJwks jwks = new HttpsJwks(definition.get().get().providerMetadata().jwksURI());
        Get get = new Get();
        get.setConnectTimeout(definition.get().get().jwksConnectTimeout());
        get.setReadTimeout(definition.get().get().jwksReadTimeout());
        jwks.setSimpleHttpGet(get);

        HttpsJwksVerificationKeyResolver keyResolver = new HttpsJwksVerificationKeyResolver(jwks);

        ErrorCodeValidator azpValidator = context -> {
            List<String> aud = context.getJwtClaims().getAudience();
            if (aud.size() > 1) {
                String azp = context.getJwtClaims().getStringClaimValue("azp");
                if (azp == null) {
                    return new ErrorCodeValidator.Error(ErrorCodes.MISCELLANEOUS, OpenIdConstant.AUDIENCE + " has " + aud.size() + " entries (" + String.join(", " + aud) + ") but no " + OpenIdConstant.AUTHORIZED_PARTY + " claim is present");
                }

                String clientId = definition.get().get().clientId();
                if (!clientId.equals(azp)) {
                    return new ErrorCodeValidator.Error(ErrorCodes.MISCELLANEOUS, OpenIdConstant.AUTHORIZED_PARTY + " is not equal to configured clientId (got " + azp + " but expected " + clientId + ")");
                }
            }

            return null;
        };

        JwtConsumerBuilder builder = new JwtConsumerBuilder()
                .setRequireSubject()
                .setRequireIssuedAt()
                .setRequireExpirationTime()
                .setVerificationKeyResolver(keyResolver)
                .setExpectedIssuer(definition.get().get().providerMetadata().issuer())
                .setExpectedAudience(definition.get().get().clientId())
                .registerValidator(azpValidator);

        return builder.build();
    }
}
