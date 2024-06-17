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

import jakarta.json.JsonObject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.ClaimsDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.DisplayType;
import jakarta.security.enterprise.authentication.mechanism.http.openid.LogoutDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdProviderMetadata;
import jakarta.security.enterprise.authentication.mechanism.http.openid.PromptType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;

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
    public Class<? extends Annotation> annotationType() {
        return delegate.annotationType();
    }

    public static class AutoResolvingProviderMetadata extends OpenIdAuthenticationMechanismDefinitionDelegate {
        private static final Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, AutoResolvingProviderMetadata.class);

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

            // Try to fetch from remote and build a merged view of OP response + @OpenIdProviderMetadata
            try (Client client = ClientBuilder.newClient()) {
                String providerUri = StringUtils.removeEnd(providerURI(), "/");
                if (!providerUri.endsWith("/.well-known/openid-configuration")) {
                    providerUri += "/.well-known/openid-configuration";
                }

                LOGGER.debug("Fetching provider metadata from " + providerUri);

                JsonObject response = client.target(providerUri)
                        .request(MediaType.APPLICATION_JSON)
                        .get(JsonObject.class);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Fetched provider metadata from " + providerUri + ": " + response.toString());
                }

                cached = new CompositeOpenIdProviderMetadata(response, super.providerMetadata());
            }

            return cached;
        }
    }
}
