package org.apache.tomee.security.http.openid.model;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.security.enterprise.identitystore.openid.Claims;
import jakarta.security.enterprise.identitystore.openid.JwtClaims;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class TomEEJwtClaims implements JwtClaims {
    private final JsonObject claims;

    public TomEEJwtClaims(JsonObject claims) {
        this.claims = claims;
    }

    @Override
    public Optional<String> getStringClaim(String name) {
        return Optional.ofNullable(claims.getString(name, null));
    }

    @Override
    public Optional<Instant> getNumericDateClaim(String name) {
        OptionalLong rawValue = getLongClaim(name);
        if (rawValue.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Instant.ofEpochSecond(rawValue.getAsLong()));
    }

    @Override
    public List<String> getArrayStringClaim(String name) {
        return claims.getJsonArray(name).stream()
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .toList();
    }

    @Override
    public OptionalInt getIntClaim(String name) {
        if (!claims.containsKey(name)) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(claims.getInt(name));
    }

    @Override
    public OptionalLong getLongClaim(String name) {
        if (!claims.containsKey(name)) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(claims.getJsonNumber(name).longValue());
    }

    @Override
    public OptionalDouble getDoubleClaim(String name) {
        if (!claims.containsKey(name)) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(claims.getJsonNumber(name).doubleValue());
    }

    @Override
    public Optional<Claims> getNested(String name) {
        if (!claims.containsKey(name)) {
            return Optional.empty();
        }

        return Optional.of(new TomEEJwtClaims(claims.getJsonObject(name)));
    }
}
