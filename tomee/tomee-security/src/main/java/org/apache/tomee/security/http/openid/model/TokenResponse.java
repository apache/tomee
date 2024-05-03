package org.apache.tomee.security.http.openid.model;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.identitystore.openid.Scope;
import java.util.Optional;

public class TokenResponse {
    @JsonbProperty(OpenIdConstant.TOKEN_TYPE) private String tokenType;

    @JsonbProperty(OpenIdConstant.ACCESS_TOKEN) private String accesToken;
    @JsonbProperty(OpenIdConstant.IDENTITY_TOKEN) private String idToken;
    @JsonbProperty(OpenIdConstant.REFRESH_TOKEN) private Optional<String> refreshToken;

    @JsonbProperty(OpenIdConstant.EXPIRES_IN) private long expiresIn;

    @JsonbProperty(OpenIdConstant.SCOPE)
    @JsonbTypeAdapter(JsonbScopeAdapter.class)
    private Scope scope;

    public String getTokenType() {
        return tokenType;
    }

    public String getAccesToken() {
        return accesToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public Optional<String> getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public Scope getScope() {
        return scope;
    }

    public static class JsonbScopeAdapter implements JsonbAdapter<Scope, String> {
        @Override
        public String adaptToJson(Scope obj) throws Exception {
            return obj == null ? null : obj.toString();
        }

        @Override
        public Scope adaptFromJson(String obj) throws Exception {
            return Scope.parse(obj);
        }
    }
}
