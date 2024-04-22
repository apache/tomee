package org.apache.tomee.security.http.openid.model;

import jakarta.json.JsonObject;
import jakarta.security.enterprise.identitystore.openid.JwtClaims;

public class TomEEJwtClaims extends TomEEJsonClaims implements JwtClaims {
    public TomEEJwtClaims(JsonObject claims) {
        super(claims);
    }
}
