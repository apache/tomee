/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.security.http.openid;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdProviderMetadata;
import java.lang.annotation.Annotation;
import java.util.stream.Collectors;

/**
 * A merged view of provider metadata using the json document returned from OP
 * and the user provided @OpenIdProviderMetadata overrides
 */
public class CompositeOpenIdProviderMetadata implements OpenIdProviderMetadata {
    private final JsonObject openidProviderMetadata;
    private final OpenIdProviderMetadata openIdProviderMetadataOverride;

    public CompositeOpenIdProviderMetadata(JsonObject openidProviderMetadata, OpenIdProviderMetadata openIdProviderMetadataOverride) {
        this.openidProviderMetadata = openidProviderMetadata;
        this.openIdProviderMetadataOverride = openIdProviderMetadataOverride;
    }

    @Override
    public String authorizationEndpoint() {
        if (!openIdProviderMetadataOverride.authorizationEndpoint().isEmpty()) {
            return openIdProviderMetadataOverride.authorizationEndpoint();
        }

        return openidProviderMetadata.getString(OpenIdConstant.AUTHORIZATION_ENDPOINT);
    }

    @Override
    public String tokenEndpoint() {
        if (!openIdProviderMetadataOverride.tokenEndpoint().isEmpty()) {
            return openIdProviderMetadataOverride.tokenEndpoint();
        }

        return openidProviderMetadata.getString(OpenIdConstant.TOKEN_ENDPOINT);
    }

    @Override
    public String userinfoEndpoint() {
        if (!openIdProviderMetadataOverride.userinfoEndpoint().isEmpty()) {
            return openIdProviderMetadataOverride.userinfoEndpoint();
        }

        return openidProviderMetadata.getString(OpenIdConstant.USERINFO_ENDPOINT);
    }

    @Override
    public String endSessionEndpoint() {
        if (!openIdProviderMetadataOverride.endSessionEndpoint().isEmpty()) {
            return openIdProviderMetadataOverride.endSessionEndpoint();
        }

        return openidProviderMetadata.getString(OpenIdConstant.END_SESSION_ENDPOINT);
    }

    @Override
    public String jwksURI() {
        if (!openIdProviderMetadataOverride.jwksURI().isEmpty()) {
            return openIdProviderMetadataOverride.jwksURI();
        }

        return openidProviderMetadata.getString(OpenIdConstant.JWKS_URI);
    }

    @Override
    public String issuer() {
        if (!openIdProviderMetadataOverride.issuer().isEmpty()) {
            return openIdProviderMetadataOverride.issuer();
        }

        return openidProviderMetadata.getString(OpenIdConstant.ISSUER);
    }

    //TODO subjectTypeSupported, idTokenSigningAlgorithmsSupported and responseTypeSupported default to a non-empty string.
    // Probably needs to be clarified in spec what should happen here, for now we prioritize the json response from the OP.
    // This is counter intuitive, but it matches with how existing impls (=soteria) work and is the least disruptive way of doing things for now
    @Override
    public String subjectTypeSupported() {
        if (openidProviderMetadata.containsKey(OpenIdConstant.SUBJECT_TYPES_SUPPORTED)) {
            return openidProviderMetadata.getJsonArray(OpenIdConstant.SUBJECT_TYPES_SUPPORTED).stream()
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .collect(Collectors.joining(", "));
        }

        return openIdProviderMetadataOverride.subjectTypeSupported();
    }

    @Override
    public String idTokenSigningAlgorithmsSupported() {
        if (openidProviderMetadata.containsKey(OpenIdConstant.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED)) {
            return openidProviderMetadata.getJsonArray(OpenIdConstant.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED).stream()
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .collect(Collectors.joining(", "));
        }

        return openIdProviderMetadataOverride.idTokenSigningAlgorithmsSupported();

    }

    @Override
    public String responseTypeSupported() {
        if (openidProviderMetadata.containsKey(OpenIdConstant.RESPONSE_TYPES_SUPPORTED)) {
            return openidProviderMetadata.getJsonArray(OpenIdConstant.RESPONSE_TYPES_SUPPORTED).stream()
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .collect(Collectors.joining(", "));
        }

        return openIdProviderMetadataOverride.responseTypeSupported();

    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return OpenIdProviderMetadata.class;
    }
}
