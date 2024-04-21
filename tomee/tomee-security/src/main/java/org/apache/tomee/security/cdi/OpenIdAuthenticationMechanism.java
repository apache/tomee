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
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.tomee.security.http.openid.OpenIdStorageHandler;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * see <a href="https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth">OIDC</a>
 * and <a href="https://datatracker.ietf.org/doc/html/rfc6749">OAuth 2.0</a>
 */
@ApplicationScoped
public class OpenIdAuthenticationMechanism implements HttpAuthenticationMechanism {
    @Inject private Supplier<OpenIdAuthenticationMechanismDefinition> definition;

    @Inject private Instance<IdentityStoreHandler> identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        OpenIdStorageHandler storageHandler = OpenIdStorageHandler.get(definition.get().useSession());
        if (request.getUserPrincipal() == null && httpMessageContext.isProtected()) {
            String state = request.getParameter(OpenIdConstant.STATE);
            if (state == null) {
                return httpMessageContext.redirect(buildAuthorizationUri(storageHandler, request, response).toString());
            }

            // state != null -> callback from openid provider (3)

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

            URI tokenUri = UriBuilder.fromUri(definition.get().providerMetadata().tokenEndpoint())
                    .queryParam(OpenIdConstant.CLIENT_ID, definition.get().clientId())
                    .queryParam(OpenIdConstant.CLIENT_SECRET, definition.get().clientSecret())
                    .queryParam(OpenIdConstant.GRANT_TYPE, "authorization_code")
                    .queryParam(OpenIdConstant.REDIRECT_URI, definition.get().redirectURI())
                    .queryParam(OpenIdConstant.CODE, request.getParameter(OpenIdConstant.CODE))
                    .build();
        }


        return httpMessageContext.redirect(buildAuthorizationUri(storageHandler, request, response).toString());
    }

    protected URI buildAuthorizationUri(OpenIdStorageHandler storageHandler, HttpServletRequest request, HttpServletResponse response) {
        UriBuilder uriBuilder = UriBuilder.fromUri(definition.get().providerMetadata().authorizationEndpoint())
                .queryParam(OpenIdConstant.CLIENT_ID, definition.get().clientId())
                .queryParam(OpenIdConstant.SCOPE, String.join(",", definition.get().scope()))
                .queryParam(OpenIdConstant.RESPONSE_TYPE, definition.get().responseType())
                .queryParam(OpenIdConstant.STATE, storageHandler.createNewState(request, response))
                .queryParam(OpenIdConstant.REDIRECT_URI, request.getRequestURI());

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
