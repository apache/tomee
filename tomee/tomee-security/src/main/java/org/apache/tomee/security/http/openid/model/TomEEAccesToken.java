package org.apache.tomee.security.http.openid.model;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.security.enterprise.identitystore.openid.AccessToken;
import jakarta.security.enterprise.identitystore.openid.JwtClaims;
import jakarta.security.enterprise.identitystore.openid.Scope;
import java.io.StringReader;
import java.util.Base64;
import java.util.Map;

public class TomEEAccesToken implements AccessToken {
    private final boolean jwt;
    private final String token;
    private final Type type;
    private final Scope scope;
    private final Long expiresIn;

    public TomEEAccesToken(boolean jwt, String token, Type type, Scope scope, Long expiresIn) {
        this.jwt = jwt;
        this.token = token;
        this.type = type;
        this.scope = scope;
        this.expiresIn = expiresIn;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public boolean isJWT() {
        return jwt;
    }

    @Override
    public JwtClaims getJwtClaims() {
        if (!isJWT()) {
            return null;
        }

        String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            return new TomEEJwtClaims(reader.readObject());
        }
    }

    @Override
    public Map<String, Object> getClaims() {
        if (!isJWT()) {
            return null;
        }

        String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(json, Map.class);
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
