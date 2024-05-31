package org.superbiz.openid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Named
@ApplicationScoped
public class OpenIdConfig {
    @Inject
    @ConfigProperty(name = "openid.provider-uri")
    private String providerUri;

    @Inject
    @ConfigProperty(name = "openid.client-id")
    private String clientId;

    @Inject
    @ConfigProperty(name = "openid.client-secret")
    private String clientSecret;

    public String getProviderUri() {
        return providerUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
