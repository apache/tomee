package org.apache.tomee.security.cdi.openid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;

@ApplicationScoped
public class OpenIdProviderMetadataHolder {
    private JsonObject providerMetadataResponse;

    public JsonObject getProviderMetadataResponse() {
        return providerMetadataResponse;
    }

    public void setProviderMetadataResponse(JsonObject providerMetadataResponse) {
        this.providerMetadataResponse = providerMetadataResponse;
    }
}
