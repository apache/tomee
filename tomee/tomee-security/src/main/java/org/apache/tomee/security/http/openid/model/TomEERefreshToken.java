package org.apache.tomee.security.http.openid.model;

import jakarta.security.enterprise.identitystore.openid.RefreshToken;

public class TomEERefreshToken implements RefreshToken {
    private final String token;

    public TomEERefreshToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }
}
