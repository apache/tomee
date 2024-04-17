package org.apache.tomee.security.cdi.oidc;

import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.ClaimsDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.DisplayType;
import jakarta.security.enterprise.authentication.mechanism.http.openid.LogoutDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdProviderMetadata;
import jakarta.security.enterprise.authentication.mechanism.http.openid.PromptType;

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
        private OpenIdProviderMetadata cached = null;

        public AutoResolvingProviderMetadata(OpenIdAuthenticationMechanismDefinition delegate) {
            super(delegate);
        }

        @Override
        public OpenIdProviderMetadata providerMetadata() {
            OpenIdProviderMetadata originalResult = super.providerMetadata();
            if (originalResult != null) {
                return originalResult;
            }

            // Try to fetch from remote
            if (cached == null) {
                // TODO actually fetch and store in OpenIdContext
                JsonObject response = JsonProvider.provider().createObjectBuilder().build();

                cached = new JsonBasedProviderMetadata(response);
            }

            return cached;
        }
    }
}
