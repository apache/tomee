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

import org.apache.commons.lang3.StringUtils;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.ClaimsDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.DisplayType;
import jakarta.security.enterprise.authentication.mechanism.http.openid.LogoutDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdProviderMetadata;
import jakarta.security.enterprise.authentication.mechanism.http.openid.PromptType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.net.ConnectException;
import java.net.URI;

public class OpenIdAuthenticationMechanismDefinitionDelegate implements OpenIdAuthenticationMechanismDefinition {
    private final OpenIdAuthenticationMechanismDefinition delegate;

    public OpenIdAuthenticationMechanismDefinitionDelegate(final OpenIdAuthenticationMechanismDefinition delegate) {
        this.delegate = delegate;
    }

    @Override
    public String providerURI() {
        return delegate.providerURI();
    }

    @Override
    public OpenIdProviderMetadata providerMetadata() {
        return delegate.providerMetadata();
    }

    @Override
    public String clientId() {
        return delegate.clientId();
    }

    @Override
    public String clientSecret() {
        return delegate.clientSecret();
    }

    @Override
    public ClaimsDefinition claimsDefinition() {
        return delegate.claimsDefinition();
    }

    @Override
    public LogoutDefinition logout() {
        return delegate.logout();
    }

    @Override
    public String redirectURI() {
        return delegate.redirectURI();
    }

    @Override
    public boolean redirectToOriginalResource() {
        return delegate.redirectToOriginalResource();
    }

    @Override
    public String redirectToOriginalResourceExpression() {
        return delegate.redirectToOriginalResourceExpression();
    }

    @Override
    public String[] scope() {
        return delegate.scope();
    }

    @Override
    public String scopeExpression() {
        return delegate.scopeExpression();
    }

    @Override
    public String responseType() {
        return delegate.responseType();
    }

    @Override
    public String responseMode() {
        return delegate.responseMode();
    }

    @Override
    public PromptType[] prompt() {
        return delegate.prompt();
    }

    @Override
    public String promptExpression() {
        return delegate.promptExpression();
    }

    @Override
    public DisplayType display() {
        return delegate.display();
    }

    @Override
    public String displayExpression() {
        return delegate.displayExpression();
    }

    @Override
    public boolean useNonce() {
        return delegate.useNonce();
    }

    @Override
    public String useNonceExpression() {
        return delegate.useNonceExpression();
    }

    @Override
    public boolean useSession() {
        return delegate.useSession();
    }

    @Override
    public String useSessionExpression() {
        return delegate.useSessionExpression();
    }

    @Override
    public String[] extraParameters() {
        return delegate.extraParameters();
    }

    @Override
    public String extraParametersExpression() {
        return delegate.extraParametersExpression();
    }

    @Override
    public int jwksConnectTimeout() {
        return delegate.jwksConnectTimeout();
    }

    @Override
    public String jwksConnectTimeoutExpression() {
        return delegate.jwksConnectTimeoutExpression();
    }

    @Override
    public int jwksReadTimeout() {
        return delegate.jwksReadTimeout();
    }

    @Override
    public String jwksReadTimeoutExpression() {
        return delegate.jwksReadTimeoutExpression();
    }

    @Override
    public boolean tokenAutoRefresh() {
        return delegate.tokenAutoRefresh();
    }

    @Override
    public String tokenAutoRefreshExpression() {
        return delegate.tokenAutoRefreshExpression();
    }

    @Override
    public int tokenMinValidity() {
        return delegate.tokenMinValidity();
    }

    @Override
    public String tokenMinValidityExpression() {
        return delegate.tokenMinValidityExpression();
    }

    @Override
    public Class<?>[] qualifiers() {
        return delegate.qualifiers();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return delegate.annotationType();
    }

    public static class AutoResolvingProviderMetadata extends OpenIdAuthenticationMechanismDefinitionDelegate {
        private static final Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, AutoResolvingProviderMetadata.class);

        private static final String WELL_KNOWN_CONFIGURATION_PATH = "/.well-known/openid-configuration";
        private static final String[] LOCALHOST_REWRITE_ENDPOINTS = {
                OpenIdConstant.AUTHORIZATION_ENDPOINT,
                OpenIdConstant.TOKEN_ENDPOINT,
                OpenIdConstant.USERINFO_ENDPOINT,
                OpenIdConstant.END_SESSION_ENDPOINT,
                OpenIdConstant.JWKS_URI
        };
        private static final String[] LOCAL_HTTP_PORT_PROPERTIES = {
                "tomee.httpPort",
                "openejb.httpPort"
        };

        private OpenIdProviderMetadata cached = null;

        public AutoResolvingProviderMetadata(OpenIdAuthenticationMechanismDefinition delegate) {
            super(delegate);
        }

        @Override
        public OpenIdProviderMetadata providerMetadata() {
            if (cached != null) {
                return cached;
            }

            if (providerURI().isEmpty()) {
                cached = super.providerMetadata();
                return cached;
            }

            final URI configuredProviderUri = OpenIdHttpClientSupport.toUri(providerURI());
            if (configuredProviderUri == null) {
                throw new IllegalArgumentException("Invalid OpenID providerURI: " + providerURI());
            }

            final URI configuredMetadataUri = toOpenIdConfigurationUri(configuredProviderUri);

            JsonObject response;
            URI responseSource = configuredProviderUri;
            try {
                response = fetchMetadata(configuredMetadataUri);
            } catch (final RuntimeException originalFailure) {
                final URI fallbackProviderUri = resolveLocalhostFallbackProviderUri(configuredProviderUri, originalFailure);
                if (fallbackProviderUri == null) {
                    throw originalFailure;
                }

                final URI fallbackMetadataUri = toOpenIdConfigurationUri(fallbackProviderUri);
                LOGGER.debug("Retrying provider metadata fetch using local authority from redirectURI: " + fallbackMetadataUri);

                response = fetchMetadata(fallbackMetadataUri);
                responseSource = fallbackProviderUri;
            }

            response = rewriteLocalhostMetadataEndpoints(response, configuredProviderUri, responseSource);
            cached = new CompositeOpenIdProviderMetadata(response, super.providerMetadata());
            return cached;
        }

        private JsonObject fetchMetadata(final URI metadataUri) {
            LOGGER.debug("Fetching provider metadata from " + metadataUri);

            try (Client client = OpenIdHttpClientSupport.newClient(metadataUri.toString())) {
                final JsonObject response = client.target(metadataUri.toString())
                        .request(MediaType.APPLICATION_JSON)
                        .get(JsonObject.class);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Fetched provider metadata from " + metadataUri + ": " + response);
                }

                return response;
            }
        }

        private URI toOpenIdConfigurationUri(final URI providerUri) {
            String value = StringUtils.removeEnd(providerUri.toString(), "/");
            if (!value.endsWith(WELL_KNOWN_CONFIGURATION_PATH)) {
                value += WELL_KNOWN_CONFIGURATION_PATH;
            }
            return URI.create(value);
        }

        private URI resolveLocalhostFallbackProviderUri(final URI configuredProviderUri, final RuntimeException failure) {
            if (!OpenIdHttpClientSupport.hasCause(failure, ConnectException.class)) {
                return null;
            }

            if (!OpenIdHttpClientSupport.isLoopbackHost(configuredProviderUri.getHost())) {
                return null;
            }

            final int configuredPort = OpenIdHttpClientSupport.explicitOrDefaultPort(configuredProviderUri);
            if (configuredPort <= 0) {
                return null;
            }

            final URI redirectUri = OpenIdHttpClientSupport.toUri(redirectURI());
            if (redirectUri != null && OpenIdHttpClientSupport.isLoopbackHost(redirectUri.getHost())) {
                final int redirectPort = OpenIdHttpClientSupport.explicitOrDefaultPort(redirectUri);
                if (redirectPort > 0 && configuredPort != redirectPort) {
                    return OpenIdHttpClientSupport.replaceAuthority(configuredProviderUri, redirectUri);
                }
            }

            final int runtimePort = resolveRuntimeHttpPort();
            if (runtimePort <= 0 || configuredPort == runtimePort) {
                return null;
            }

            try {
                return new URI(
                        configuredProviderUri.getScheme(),
                        configuredProviderUri.getUserInfo(),
                        configuredProviderUri.getHost(),
                        runtimePort,
                        configuredProviderUri.getRawPath(),
                        configuredProviderUri.getRawQuery(),
                        configuredProviderUri.getRawFragment());
            } catch (final Exception ignored) {
                return null;
            }
        }

        private int resolveRuntimeHttpPort() {
            for (final String property : LOCAL_HTTP_PORT_PROPERTIES) {
                final String value = System.getProperty(property);
                if (StringUtils.isBlank(value)) {
                    continue;
                }

                try {
                    final int port = Integer.parseInt(value.trim());
                    if (port > 0) {
                        return port;
                    }
                } catch (final NumberFormatException ignored) {
                    // continue
                }
            }
            return -1;
        }

        private JsonObject rewriteLocalhostMetadataEndpoints(final JsonObject metadata,
                                                             final URI configuredProviderUri,
                                                             final URI resolvedProviderUri) {
            if (resolvedProviderUri == null || metadata == null) {
                return metadata;
            }

            final int configuredPort = OpenIdHttpClientSupport.explicitOrDefaultPort(configuredProviderUri);
            final int resolvedPort = OpenIdHttpClientSupport.explicitOrDefaultPort(resolvedProviderUri);
            if (configuredPort <= 0 || configuredPort == resolvedPort) {
                return metadata;
            }

            final JsonObjectBuilder builder = Json.createObjectBuilder();
            metadata.forEach(builder::add);

            for (final String endpointKey : LOCALHOST_REWRITE_ENDPOINTS) {
                if (!metadata.containsKey(endpointKey)) {
                    continue;
                }

                final String endpointValue = metadata.getString(endpointKey, "");
                final URI endpointUri = OpenIdHttpClientSupport.toUri(endpointValue);
                if (endpointUri == null || !OpenIdHttpClientSupport.isLoopbackHost(endpointUri.getHost())) {
                    continue;
                }

                final int endpointPort = OpenIdHttpClientSupport.explicitOrDefaultPort(endpointUri);
                if (endpointPort != configuredPort) {
                    continue;
                }

                final URI rewrittenEndpoint = OpenIdHttpClientSupport.replaceAuthority(endpointUri, resolvedProviderUri);
                builder.add(endpointKey, rewrittenEndpoint.toString());
            }

            return builder.build();
        }
    }
}
