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
package org.apache.tomee.security.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.security.cdi.openid.TomEEOpenIdContext;
import org.apache.tomee.security.http.openid.OpenIdStorageHandler;
import org.apache.tomee.security.http.openid.model.TomEEOpenIdCredential;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * see <a href="https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth">OIDC</a>
 * and <a href="https://datatracker.ietf.org/doc/html/rfc6749">OAuth 2.0</a>
 */
@ApplicationScoped
@AutoApplySession // TODO probably interferes with token auto refreshing
public class OpenIdAuthenticationMechanism implements HttpAuthenticationMechanism {
    private static final Logger LOGGER = Logger.getInstance(
            LogCategory.TOMEE_SECURITY, OpenIdAuthenticationMechanism.class);

    @Inject
    private Supplier<OpenIdAuthenticationMechanismDefinition> definition;

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Inject
    private TomEEOpenIdContext openIdContext;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        OpenIdStorageHandler storageHandler = OpenIdStorageHandler.get(definition.get().useSession());

        String state = request.getParameter(OpenIdConstant.STATE);
        if (state == null && request.getUserPrincipal() == null && httpMessageContext.isProtected()) {
            return httpMessageContext.redirect(buildAuthorizationUri(storageHandler, request, response).toString());
        }

        if (state != null) { // -> callback from openid provider (3)
            // TODO validate url matches redirectURI/original URL

            if (storageHandler.getStoredState(request, response) == null) {
                return httpMessageContext.notifyContainerAboutLogin(CredentialValidationResult.NOT_VALIDATED_RESULT);
            }

            if (!state.equals(storageHandler.getStoredState(request, response))) {
                return httpMessageContext.notifyContainerAboutLogin(CredentialValidationResult.INVALID_RESULT);
            }

            if (request.getParameter(OpenIdConstant.ERROR_PARAM) != null) {
                return httpMessageContext.notifyContainerAboutLogin(CredentialValidationResult.INVALID_RESULT);
            }

            // Callback is okay, continue with (4)
            storageHandler.set(request, response, OpenIdStorageHandler.STATE_KEY, null);

            try (Client client = ClientBuilder.newClient()) {
                Form form = new Form()
                        .param(OpenIdConstant.CLIENT_ID, definition.get().clientId())
                        .param(OpenIdConstant.CLIENT_SECRET, definition.get().clientSecret())
                        .param(OpenIdConstant.GRANT_TYPE, "authorization_code")
                        .param(OpenIdConstant.REDIRECT_URI, definition.get().redirectURI())
                        .param(OpenIdConstant.CODE, request.getParameter(OpenIdConstant.CODE));

                TomEEOpenIdCredential credential = client.target(definition.get().providerMetadata().tokenEndpoint()).request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.form(form), TomEEOpenIdCredential.class);

                openIdContext.setExpiresIn(credential.getExpiresIn());
                openIdContext.setTokenType(credential.getTokenType());

                CredentialValidationResult validationResult = identityStoreHandler.validate(credential);
                httpMessageContext.setRegisterSession(validationResult.getCallerPrincipal().getName(), validationResult.getCallerGroups());

                return httpMessageContext.notifyContainerAboutLogin(validationResult);
            }
        }


        return httpMessageContext.doNothing();
    }

    protected URI buildAuthorizationUri(OpenIdStorageHandler storageHandler, HttpServletRequest request, HttpServletResponse response) {
        UriBuilder uriBuilder = UriBuilder.fromUri(definition.get().providerMetadata().authorizationEndpoint())
                .queryParam(OpenIdConstant.CLIENT_ID, definition.get().clientId())
                .queryParam(OpenIdConstant.SCOPE, String.join(",", definition.get().scope()))
                .queryParam(OpenIdConstant.RESPONSE_TYPE, definition.get().responseType())
                .queryParam(OpenIdConstant.STATE, storageHandler.createNewState(request, response))
                .queryParam(OpenIdConstant.REDIRECT_URI, definition.get().redirectURI());

        if (definition.get().useNonce()) {
            uriBuilder.queryParam(OpenIdConstant.NONCE, storageHandler.createNewNonce(request, response));
        }

        if (!definition.get().responseMode().isEmpty()) {
            uriBuilder.queryParam(OpenIdConstant.RESPONSE_MODE, definition.get().responseMode());
        }

        if (definition.get().display() != null) {
            uriBuilder.queryParam(OpenIdConstant.DISPLAY, definition.get().display().name().toLowerCase());
        }

        if (definition.get().prompt().length > 0) {
            String stringifiedPrompt = Arrays.stream(definition.get().prompt())
                    .map(Enum::toString).map(String::toLowerCase)
                    .collect(Collectors.joining(" "));

            uriBuilder.queryParam(OpenIdConstant.PROMPT, stringifiedPrompt);
        }

        for (String extraParam : definition.get().extraParameters()) {
            String[] paramParts = extraParam.split("=");

            if (paramParts.length != 2) {
                throw new IllegalStateException("extra parameter in invalid format, expected \"key=value\": " + extraParam);
            }

            uriBuilder.queryParam(paramParts[0], paramParts[1]);
        }

        return uriBuilder.build();
    }
}
