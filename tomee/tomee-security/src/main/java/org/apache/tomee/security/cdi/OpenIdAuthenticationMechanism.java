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
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
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
import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;
import org.apache.tomee.security.http.SavedRequest;
import org.apache.tomee.security.http.openid.model.TokenResponse;
import org.apache.tomee.security.http.openid.model.TomEEOpenIdCredential;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.function.Supplier;

/**
 * see <a href="https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth">OIDC</a>
 * and <a href="https://datatracker.ietf.org/doc/html/rfc6749">OAuth 2.0</a>
 */
@ApplicationScoped
public class OpenIdAuthenticationMechanism implements HttpAuthenticationMechanism {
    private static final Logger LOGGER = Logger.getInstance(
            LogCategory.TOMEE_SECURITY, OpenIdAuthenticationMechanism.class);

    @Inject
    private OpenIdAuthenticationMechanismDefinition openIdAuthenticationMechanismDefinition;
    private Supplier<OpenIdAuthenticationMechanismDefinition> resolvedDefinition;

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Inject
    private TomEEOpenIdContext openIdContext;

    @Inject
    private OpenIdStorageHandler storageHandler;

    void setDefinitionSupplier(final Supplier<OpenIdAuthenticationMechanismDefinition> definitionSupplier) {
        this.resolvedDefinition = definitionSupplier;
    }

    private OpenIdAuthenticationMechanismDefinition getDefinition() {
        return resolvedDefinition != null ? resolvedDefinition.get() : openIdAuthenticationMechanismDefinition;
    }

    private volatile String cachedRedirectPath;
    private volatile String cachedRedirectUriSource;

    /**
     * Returns the path component of the configured {@code redirectURI}, memoised so we don't
     * re-parse on every request. The comparison in {@link #performAuthentication} is deliberately
     * path-based instead of comparing against {@link HttpServletRequest#getRequestURL()} which is
     * derived from the client-supplied {@code Host} header (CWE-350).
     */
    private String redirectPath() {
        String configured = getDefinition().redirectURI();
        String cachedSource = cachedRedirectUriSource;
        if (configured != null && configured.equals(cachedSource)) {
            return cachedRedirectPath;
        }

        String path = configured == null ? null : URI.create(configured).getPath();
        cachedRedirectPath = path;
        cachedRedirectUriSource = configured;
        return path;
    }

    @Override
    public void cleanSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        String redirectTarget = buildRedirectUri();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        if (redirectTarget != null) {
            httpMessageContext.redirect(redirectTarget);
            return;
        }

        // Restart authorization by redirecting to openid provider
        redirectToAuthorization(request, response, httpMessageContext);
    }

    private String buildRedirectUri()
    {
        if (getDefinition().logout().notifyProvider()) {
            if (!getDefinition().providerMetadata().endSessionEndpoint().isEmpty()) {
                UriBuilder endSession = UriBuilder.fromUri(getDefinition().providerMetadata().endSessionEndpoint())
                        .queryParam(OpenIdConstant.ID_TOKEN_HINT, openIdContext.getIdentityToken().getToken());

                if (!getDefinition().logout().redirectURI().isEmpty()) {
                    endSession.queryParam(OpenIdConstant.POST_LOGOUT_REDIRECT_URI, getDefinition().logout().redirectURI());
                }

                return endSession.build().toString();
            }
        } else {
            if (!getDefinition().logout().redirectURI().isEmpty()) {
                return getDefinition().logout().redirectURI();
            }
        }

        return null;
    }

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        if (request.getUserPrincipal() != null) {
            AuthenticationStatus result = handleExpiredTokens(request, response, httpMessageContext);
            if (result != null) {
                return result;
            }

            final CallerPrincipalCallback callerPrincipalCallback = new CallerPrincipalCallback(
                    httpMessageContext.getClientSubject(), request.getUserPrincipal());
            try {
                httpMessageContext.getHandler().handle(new Callback[]{callerPrincipalCallback});
            } catch (IOException | UnsupportedCallbackException e) {
                LOGGER.error("Could not handle CallerPrincipalCallback", e);
            }

            return AuthenticationStatus.SUCCESS;
        }

        AuthenticationStatus result = performAuthentication(request, response, httpMessageContext);
        if (result != null) {
            return result;
        }

        return httpMessageContext.doNothing();
    }

    protected AuthenticationStatus handleExpiredTokens(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        if (openIdContext.getAccessToken() != null && openIdContext.getAccessToken().isExpired()) {
            LOGGER.debug("access token did expire");

            if (getDefinition().tokenAutoRefresh()) {
                LOGGER.debug("Attempting to refresh tokens after access token expiry");
                return refreshTokens(request, response, httpMessageContext);
            }

            if (getDefinition().logout().accessTokenExpiry()) {
                LOGGER.debug("access token expired and accessTokenExpiry=true, performing logout");
                cleanSubject(request, response, httpMessageContext);
                return AuthenticationStatus.SEND_FAILURE;
            }
        }

        if (openIdContext.getIdentityToken() != null && openIdContext.getIdentityToken().isExpired()) {
            LOGGER.debug("identity token did expire");
            if (getDefinition().tokenAutoRefresh()) {
                LOGGER.debug("Attempting to refresh tokens after identity token expiry");
                return refreshTokens(request, response, httpMessageContext);
            }

            if (getDefinition().logout().identityTokenExpiry()) {
                LOGGER.debug("identity token expired and identityTokenExpiry=true, performing logout");
                cleanSubject(request, response, httpMessageContext);
                return AuthenticationStatus.SEND_FAILURE;
            }
        }

        return null;
    }

    protected AuthenticationStatus refreshTokens(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        final String tokenEndpoint = getDefinition().providerMetadata().tokenEndpoint();
        try (Client client = ClientBuilder.newClient()) {
            RefreshToken refreshToken = openIdContext.getRefreshToken()
                    .orElse(null);

            if (refreshToken == null) {
                throw new IllegalStateException("Cannot refresh tokens, no refresh_token received");
            }

            Form form = new Form()
                    .param(OpenIdConstant.CLIENT_ID, getDefinition().clientId())
                    .param(OpenIdConstant.CLIENT_SECRET, getDefinition().clientSecret())
                    .param(OpenIdConstant.GRANT_TYPE, OpenIdConstant.REFRESH_TOKEN)
                    .param(OpenIdConstant.REFRESH_TOKEN, refreshToken.getToken());

            TokenResponse tokenResponse = client.target(tokenEndpoint).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.form(form), TokenResponse.class);

            return handleTokenResponse(tokenResponse, httpMessageContext);

        } catch (Exception e) {
            LOGGER.warning("Token refresh failed, logging out the current subject", e);
            cleanSubject(request, response, httpMessageContext);
            return AuthenticationStatus.SEND_FAILURE;
        }
    }

    protected AuthenticationStatus redirectToAuthorization(HttpServletRequest request, HttpServletResponse response, HttpMessageContext messageContext) {
        String fullRequestUrl = request.getRequestURL().toString();
        if (request.getQueryString() != null) {
            fullRequestUrl += "?" + request.getQueryString();
        }

        storageHandler.set(request, response, OpenIdConstant.ORIGINAL_REQUEST, fullRequestUrl);
        storageHandler.set(request, response, OpenIdStorageHandler.REQUEST_KEY, SavedRequest.fromRequest(request).toJson());

        return messageContext.redirect(buildAuthorizationUri(request, response).toString());
    }

    protected AuthenticationStatus performAuthentication(HttpServletRequest request, HttpServletResponse response, HttpMessageContext messageContext) {
        String state = request.getParameter(OpenIdConstant.STATE);
        if (state == null && request.getUserPrincipal() == null && messageContext.isProtected()) {
            // unauthenticated user tries to access protected resource, begin authorization dialog
            return redirectToAuthorization(request, response, messageContext);
        }

        if (state != null) {
            String originalRequest = storageHandler.get(request, response, OpenIdConstant.ORIGINAL_REQUEST);
            // Compare by path only; the full URL from getRequestURL() is composed from the
            // client-supplied Host header and therefore untrusted (CWE-350).
            String requestPath = request.getRequestURI();
            String originalRequestPath = null;
            if (originalRequest != null) {
                try {
                    originalRequestPath = URI.create(originalRequest).getPath();
                } catch (IllegalArgumentException e) {
                    originalRequestPath = null;
                }
            }
            boolean matchesOriginalRequest = originalRequestPath != null && originalRequestPath.equals(requestPath);
            boolean matchesRedirectUri = requestPath != null && requestPath.equals(redirectPath());

            // callback from openid provider (3)
            // Per Jakarta Security 4.0 §2.4.4.2, if the callback URL matches neither the configured
            // redirectURI nor the stored original request URL, the request must be rejected regardless
            // of the redirectToOriginalResource setting.
            if (!matchesRedirectUri && !matchesOriginalRequest) {
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

            if (getDefinition().redirectToOriginalResource() && !matchesOriginalRequest) {
                AuthenticationStatus redirectStatus = redirectToStoredOriginalRequest(request, messageContext, originalRequest);
                if (redirectStatus != null) {
                    return redirectStatus;
                }
            }

            // Callback is okay, continue with (4)
            storageHandler.delete(request, response, OpenIdStorageHandler.STATE_KEY);

            final String tokenEndpoint = getDefinition().providerMetadata().tokenEndpoint();
            try (Client client = ClientBuilder.newClient()) {
                Form form = new Form()
                        .param(OpenIdConstant.CLIENT_ID, getDefinition().clientId())
                        .param(OpenIdConstant.CLIENT_SECRET, getDefinition().clientSecret())
                        .param(OpenIdConstant.GRANT_TYPE, "authorization_code")
                        .param(OpenIdConstant.REDIRECT_URI, getDefinition().redirectURI())
                        .param(OpenIdConstant.CODE, request.getParameter(OpenIdConstant.CODE));

                TokenResponse tokenResponse = client.target(tokenEndpoint).request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.form(form), TokenResponse.class);

                AuthenticationStatus result = handleTokenResponse(tokenResponse, messageContext);

                // We're finished, restore original request now and clean up
                if (getDefinition().redirectToOriginalResource()) {
                    restoreOriginalRequest(request, response, messageContext);
                }

                storageHandler.delete(request, response, OpenIdStorageHandler.NONCE_KEY);
                storageHandler.delete(request, response, OpenIdStorageHandler.REQUEST_KEY);
                storageHandler.delete(request, response, OpenIdConstant.ORIGINAL_REQUEST);

                return result;
            }
        }

        return null;
    }

    protected AuthenticationStatus redirectToStoredOriginalRequest(HttpServletRequest request, HttpMessageContext messageContext,
                                                                   String originalRequest) {
        if (originalRequest == null) {
            LOGGER.warning("redirectToOriginalResource=true is configured but no original request has been stored before; continuing without redirecting to the original resource");
            return null;
        }

        return messageContext.redirect(appendQueryString(originalRequest, request.getQueryString()));
    }

    protected void restoreOriginalRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext messageContext) {
        String originalRequestJson = storageHandler.get(request, response, OpenIdStorageHandler.REQUEST_KEY);
        if (originalRequestJson == null) {
            return;
        }

        SavedRequest savedRequest = SavedRequest.fromJson(originalRequestJson);
        if (savedRequest != null) {
            messageContext.withRequest(savedRequest.mask(request));
        }
    }

    protected AuthenticationStatus handleTokenResponse(TokenResponse tokenResponse, HttpMessageContext httpMessageContext) {
        openIdContext.setExpiresIn(tokenResponse.getExpiresIn());
        openIdContext.setTokenType(tokenResponse.getTokenType());

        TomEEOpenIdCredential credential = new TomEEOpenIdCredential(tokenResponse, httpMessageContext);
        CredentialValidationResult validationResult = identityStoreHandler.validate(credential);

        httpMessageContext.setRegisterSession(validationResult.getCallerPrincipal().getName(), validationResult.getCallerGroups());

        return httpMessageContext.notifyContainerAboutLogin(validationResult);
    }

    protected URI buildAuthorizationUri(HttpServletRequest request, HttpServletResponse response) {
        UriBuilder uriBuilder = UriBuilder.fromUri(getDefinition().providerMetadata().authorizationEndpoint())
                .queryParam(OpenIdConstant.CLIENT_ID, getDefinition().clientId())
                .queryParam(OpenIdConstant.SCOPE, String.join(" ", getDefinition().scope()))
                .queryParam(OpenIdConstant.RESPONSE_TYPE, getDefinition().responseType())
                .queryParam(OpenIdConstant.STATE, storageHandler.createNewState(request, response))
                .queryParam(OpenIdConstant.REDIRECT_URI, getDefinition().redirectURI());

        if (getDefinition().useNonce()) {
            uriBuilder.queryParam(OpenIdConstant.NONCE, storageHandler.createNewNonce(request, response));
        }

        if (!getDefinition().responseMode().isEmpty()) {
            uriBuilder.queryParam(OpenIdConstant.RESPONSE_MODE, getDefinition().responseMode());
        }

        if (getDefinition().display() != null) {
            uriBuilder.queryParam(OpenIdConstant.DISPLAY, getDefinition().display().name().toLowerCase());
        }

        if (getDefinition().prompt().length > 0) {
            String stringifiedPrompt = Arrays.stream(getDefinition().prompt())
                    .map(Enum::toString).map(String::toLowerCase)
                    .collect(Collectors.joining(" "));

            uriBuilder.queryParam(OpenIdConstant.PROMPT, stringifiedPrompt);
        }

        for (String extraParam : getDefinition().extraParameters()) {
            String[] paramParts = extraParam.split("=");

            if (paramParts.length != 2) {
                throw new IllegalArgumentException("extra parameter in invalid format, expected \"key=value\": " + extraParam);
            }

            uriBuilder.queryParam(paramParts[0], paramParts[1]);
        }

        return uriBuilder.build();
    }

    protected String appendQueryString(String url, String query) {
        if (url.contains("?")) {
            return url + "&" + query;
        }

        return url + "?" + query;
    }
}
