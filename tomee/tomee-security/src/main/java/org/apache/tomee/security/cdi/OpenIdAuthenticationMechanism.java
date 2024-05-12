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
import jakarta.security.enterprise.identitystore.openid.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import org.apache.tomee.security.http.openid.model.TokenResponse;
import org.apache.tomee.security.http.openid.model.TomEEOpenIdCredential;

import java.net.URI;
import java.util.Arrays;
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

    @Inject private OpenIdAuthenticationMechanismDefinition definition;

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Inject
    private TomEEOpenIdContext openIdContext;

    @Override
    public void cleanSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        if (definition.logout().notifyProvider()) {
            if (!definition.providerMetadata().endSessionEndpoint().isEmpty()) {
                UriBuilder endSession = UriBuilder.fromUri(definition.providerMetadata().endSessionEndpoint())
                        .queryParam(OpenIdConstant.ID_TOKEN_HINT, openIdContext.getIdentityToken().getToken());

                if (!definition.logout().redirectURI().isEmpty()) {
                    endSession.queryParam(OpenIdConstant.POST_LOGOUT_REDIRECT_URI, definition.logout().redirectURI());
                }

                httpMessageContext.redirect(endSession.toString());
                return;
            }
        } else {
            if (!definition.logout().redirectURI().isEmpty()) {
                httpMessageContext.redirect(definition.logout().redirectURI());
                return;
            }
        }

        performAuthentication(request, response, httpMessageContext);
    }

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        AuthenticationStatus result = performAuthentication(request, response, httpMessageContext);
        if (result != null) {
            return result;
        }

        result = handleExpiredTokens(request, response, httpMessageContext);
        if (result != null) {
            return result;
        }

        return httpMessageContext.doNothing();
    }

    protected AuthenticationStatus handleExpiredTokens(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        if (openIdContext.getAccessToken().isExpired()) {
            LOGGER.debug("access token did expire");

            if (definition.tokenAutoRefresh()) {
                LOGGER.debug("Attempting to refresh tokens after access token expiry");
                return refreshTokens(request, response, httpMessageContext);
            }

            if (definition.logout().accessTokenExpiry()) {
                LOGGER.debug("access token expired and accessTokenExpiry=true, performing logout");
                cleanSubject(request, response, httpMessageContext);
                return AuthenticationStatus.SEND_FAILURE;
            }
        }

        if (openIdContext.getIdentityToken().isExpired()) {
            LOGGER.debug("identity token did expire");
            if (definition.tokenAutoRefresh()) {
                LOGGER.debug("Attempting to refresh tokens after identity token expiry");
                return refreshTokens(request, response, httpMessageContext);
            }

            if (definition.logout().identityTokenExpiry()) {
                LOGGER.debug("identity token expired and identityTokenExpiry=true, performing logout");
                cleanSubject(request, response, httpMessageContext);
                return AuthenticationStatus.SEND_FAILURE;
            }
        }

        return null;
    }

    protected AuthenticationStatus refreshTokens(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        try (Client client = ClientBuilder.newClient()){
            RefreshToken refreshToken = openIdContext.getRefreshToken()
                    .orElseThrow(() -> new IllegalArgumentException("Cannot refresh tokens, no refresh_token received"));

            Form form = new Form()
                    .param(OpenIdConstant.CLIENT_ID, definition.clientId())
                    .param(OpenIdConstant.CLIENT_SECRET, definition.clientSecret())
                    .param(OpenIdConstant.GRANT_TYPE, OpenIdConstant.REFRESH_TOKEN)
                    .param(OpenIdConstant.REFRESH_TOKEN, refreshToken.getToken());

            TokenResponse tokenResponse = client.target(definition.providerMetadata().tokenEndpoint()).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.form(form), TokenResponse.class);

            return handleTokenResponse(tokenResponse, httpMessageContext);

        } catch (Exception e) {
            cleanSubject(request, response, httpMessageContext);

            throw e;
        }
    }

    protected AuthenticationStatus performAuthentication(HttpServletRequest request, HttpServletResponse response, HttpMessageContext messageContext) {
        OpenIdStorageHandler storageHandler = OpenIdStorageHandler.get(definition.useSession());

        String state = request.getParameter(OpenIdConstant.STATE);
        if (state == null && request.getUserPrincipal() == null && messageContext.isProtected()) {
            // unauthenticated user tries to access protected resource, begin authorization dialog
            return messageContext.redirect(buildAuthorizationUri(storageHandler, request, response).toString());
        }

        if (state != null) {
            // callback from openid provider (3)
            if (!request.getRequestURL().toString().equals(definition.redirectURI())) {
                return messageContext.notifyContainerAboutLogin(CredentialValidationResult.NOT_VALIDATED_RESULT);
            }

            if (storageHandler.getStoredState(request, response) == null) {
                return messageContext.notifyContainerAboutLogin(CredentialValidationResult.NOT_VALIDATED_RESULT);
            }

            if (!state.equals(storageHandler.getStoredState(request, response))) {
                return messageContext.notifyContainerAboutLogin(CredentialValidationResult.INVALID_RESULT);
            }

            if (request.getParameter(OpenIdConstant.ERROR_PARAM) != null) {
                return messageContext.notifyContainerAboutLogin(CredentialValidationResult.INVALID_RESULT);
            }

            // Callback is okay, continue with (4)
            storageHandler.set(request, response, OpenIdStorageHandler.STATE_KEY, null);

            try (Client client = ClientBuilder.newClient()) {
                Form form = new Form()
                        .param(OpenIdConstant.CLIENT_ID, definition.clientId())
                        .param(OpenIdConstant.CLIENT_SECRET, definition.clientSecret())
                        .param(OpenIdConstant.GRANT_TYPE, "authorization_code")
                        .param(OpenIdConstant.REDIRECT_URI, definition.redirectURI())
                        .param(OpenIdConstant.CODE, request.getParameter(OpenIdConstant.CODE));

                TokenResponse tokenResponse = client.target(definition.providerMetadata().tokenEndpoint()).request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.form(form), TokenResponse.class);

                return handleTokenResponse(tokenResponse, messageContext);
            }
        }

        return null;
    }

    protected AuthenticationStatus handleTokenResponse(TokenResponse tokenResponse, HttpMessageContext httpMessageContext) {
        openIdContext.setExpiresIn(tokenResponse.getExpiresIn());
        openIdContext.setTokenType(tokenResponse.getTokenType());

        TomEEOpenIdCredential credential = new TomEEOpenIdCredential(tokenResponse, httpMessageContext);
        CredentialValidationResult validationResult = identityStoreHandler.validate(credential);
        httpMessageContext.setRegisterSession(validationResult.getCallerPrincipal().getName(), validationResult.getCallerGroups());

        return httpMessageContext.notifyContainerAboutLogin(validationResult);
    }

    protected URI buildAuthorizationUri(OpenIdStorageHandler storageHandler, HttpServletRequest request, HttpServletResponse response) {
        UriBuilder uriBuilder = UriBuilder.fromUri(definition.providerMetadata().authorizationEndpoint())
                .queryParam(OpenIdConstant.CLIENT_ID, definition.clientId())
                .queryParam(OpenIdConstant.SCOPE, String.join(",", definition.scope()))
                .queryParam(OpenIdConstant.RESPONSE_TYPE, definition.responseType())
                .queryParam(OpenIdConstant.STATE, storageHandler.createNewState(request, response))
                .queryParam(OpenIdConstant.REDIRECT_URI, definition.redirectURI());

        if (definition.useNonce()) {
            uriBuilder.queryParam(OpenIdConstant.NONCE, storageHandler.createNewNonce(request, response));
        }

        if (!definition.responseMode().isEmpty()) {
            uriBuilder.queryParam(OpenIdConstant.RESPONSE_MODE, definition.responseMode());
        }

        if (definition.display() != null) {
            uriBuilder.queryParam(OpenIdConstant.DISPLAY, definition.display().name().toLowerCase());
        }

        if (definition.prompt().length > 0) {
            String stringifiedPrompt = Arrays.stream(definition.prompt())
                    .map(Enum::toString).map(String::toLowerCase)
                    .collect(Collectors.joining(" "));

            uriBuilder.queryParam(OpenIdConstant.PROMPT, stringifiedPrompt);
        }

        for (String extraParam : definition.extraParameters()) {
            String[] paramParts = extraParam.split("=");

            if (paramParts.length != 2) {
                throw new IllegalArgumentException("extra parameter in invalid format, expected \"key=value\": " + extraParam);
            }

            uriBuilder.queryParam(paramParts[0], paramParts[1]);
        }

        return uriBuilder.build();
    }
}
