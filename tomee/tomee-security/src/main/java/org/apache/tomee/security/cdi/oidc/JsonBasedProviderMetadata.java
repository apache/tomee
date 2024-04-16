package org.apache.tomee.security.cdi.oidc;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdProviderMetadata;

import java.lang.annotation.Annotation;
import java.util.stream.Collectors;

public class JsonBasedProviderMetadata implements OpenIdProviderMetadata {
    private final JsonObject document;

    protected JsonBasedProviderMetadata(final JsonObject document) {
        this.document = document;
    }

    @Override
    public String authorizationEndpoint() {
        return document.getString(OpenIdConstant.AUTHORIZATION_ENDPOINT);
    }

    @Override
    public String tokenEndpoint() {
        return document.getString(OpenIdConstant.TOKEN_ENDPOINT);
    }

    @Override
    public String userinfoEndpoint() {
        return document.getString(OpenIdConstant.USERINFO_ENDPOINT);
    }

    @Override
    public String endSessionEndpoint() {
        return document.getString(OpenIdConstant.END_SESSION_ENDPOINT);
    }

    @Override
    public String jwksURI() {
        return document.getString(OpenIdConstant.JWKS_URI);
    }

    @Override
    public String issuer() {
        return document.getString(OpenIdConstant.ISSUER);
    }

    @Override
    public String subjectTypeSupported() {
        return joinWithCommas(document.getJsonArray(OpenIdConstant.SUBJECT_TYPES_SUPPORTED));
    }

    @Override
    public String idTokenSigningAlgorithmsSupported() {
        return joinWithCommas(document.getJsonArray(OpenIdConstant.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED));
    }

    @Override
    public String responseTypeSupported() {
        return joinWithCommas(document.getJsonArray(OpenIdConstant.RESPONSE_TYPES_SUPPORTED));
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return OpenIdProviderMetadata.class;
    }

    protected String joinWithCommas(JsonArray jsonValues)
    {
        return jsonValues.stream()
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .collect(Collectors.joining(","));
    }
}
