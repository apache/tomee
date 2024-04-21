package org.apache.tomee.security.http.openid.model;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.security.enterprise.identitystore.openid.AccessToken;
import jakarta.security.enterprise.identitystore.openid.JwtClaims;
import jakarta.security.enterprise.identitystore.openid.Scope;
import java.io.StringReader;
import java.util.Map;

public class TomEEAccesToken implements AccessToken {
    private final String token;
    private final Type type;
    private final Scope scope;
    private final Long expiresIn;

    public TomEEAccesToken(String token, Type type, String rawScope, Long expiresIn) {
        this.token = token;
        this.type = type;
        this.scope = Scope.parse(rawScope);
        this.expiresIn = expiresIn;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public boolean isJWT() {
        return getType() == Type.BEARER;
    }

    @Override
    public JwtClaims getJwtClaims() {
        if (!isJWT()) {
            return null;
        }

        try (JsonReader reader = Json.createReader(new StringReader(token))) {
            return new TomEEJwtClaims(reader.readObject());
        }
    }

    @Override
    public Map<String, Object> getClaims() {
        if (!isJWT()) {
            return null;
        }

        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(jsonb.toJson(getJwtClaims()), Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object getClaim(String key) {
        if (!isJWT()) {
            return null;
        }

        return getClaims().get(key);
    }

    @Override
    public Long getExpirationTime() {
        return expiresIn;
    }

    @Override
    public boolean isExpired() {
        return false; // TODO
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public Type getType() {
        return type;
    }
}
