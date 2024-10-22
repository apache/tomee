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

import org.junit.Test;

import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.spi.JsonProvider;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdProviderMetadata;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class CompositeOpenIdProviderMetadataTest {
    private final JsonObject providerResponse = readJsonObject("""
            {
             "issuer":
               "https://server.example.com",
             "authorization_endpoint":
               "https://server.example.com/connect/authorize",
             "token_endpoint":
               "https://server.example.com/connect/token",
             "token_endpoint_auth_methods_supported":
               ["client_secret_basic", "private_key_jwt"],
             "token_endpoint_auth_signing_alg_values_supported":
               ["RS256", "ES256"],
             "userinfo_endpoint":
               "https://server.example.com/connect/userinfo",
             "check_session_iframe":
               "https://server.example.com/connect/check_session",
             "end_session_endpoint":
               "https://server.example.com/connect/end_session",
             "jwks_uri":
               "https://server.example.com/jwks.json",
             "registration_endpoint":
               "https://server.example.com/connect/register",
             "scopes_supported":
               ["openid", "profile", "email", "address",
                "phone", "offline_access"],
             "response_types_supported":
               ["code", "code id_token", "id_token", "id_token token"],
             "acr_values_supported":
               ["urn:mace:incommon:iap:silver",
                "urn:mace:incommon:iap:bronze"],
             "subject_types_supported":
               ["public", "pairwise"],
             "userinfo_signing_alg_values_supported":
               ["RS256", "ES256", "HS256"],
             "userinfo_encryption_alg_values_supported":
               ["RSA-OAEP-256", "A128KW"],
             "userinfo_encryption_enc_values_supported":
               ["A128CBC-HS256", "A128GCM"],
             "id_token_signing_alg_values_supported":
               ["RS256", "ES256", "HS256"],
             "id_token_encryption_alg_values_supported":
               ["RSA-OAEP-256", "A128KW"],
             "id_token_encryption_enc_values_supported":
               ["A128CBC-HS256", "A128GCM"],
             "request_object_signing_alg_values_supported":
               ["none", "RS256", "ES256"],
             "display_values_supported":
               ["page", "popup"],
             "claim_types_supported":
               ["normal", "distributed"],
             "claims_supported":
               ["sub", "iss", "auth_time", "acr",
                "name", "given_name", "family_name", "nickname",
                "profile", "picture", "website",
                "email", "email_verified", "locale", "zoneinfo",
                "http://example.info/claims/groups"],
             "claims_parameter_supported":
               true,
             "service_documentation":
               "http://server.example.com/connect/service_documentation.html",
             "ui_locales_supported":
               ["en-US", "en-GB", "en-CA", "fr-FR", "fr-CA"]
            }
            """);

    @Test
    public void jsonValuesOverwritten() {
        OpenIdProviderMetadata annotation = AnnotationHolder.class.getAnnotation(OpenIdProviderMetadata.class);

        OpenIdProviderMetadata composite = new CompositeOpenIdProviderMetadata(providerResponse, annotation);
        assertEquals("https://override.exmaple.com/authorize", composite.authorizationEndpoint());
        assertEquals("https://override.exmaple.com/token", composite.tokenEndpoint());
        assertEquals("https://override.exmaple.com/userinfo", composite.userinfoEndpoint());
        assertEquals("https://override.exmaple.com/end_session", composite.endSessionEndpoint());
        assertEquals("https://override.exmaple.com/jwks.json", composite.jwksURI());
    }

    @Test
    public void jsonValueNotOverwritten() throws Exception {
        OpenIdProviderMetadata annotation = AnnotationHolder.class.getAnnotation(OpenIdProviderMetadata.class);

        OpenIdProviderMetadata composite = new CompositeOpenIdProviderMetadata(providerResponse, annotation);
        assertEquals("https://server.example.com", composite.issuer());
    }
    
    @Test
    public void annotationValuesIgnored() {
        OpenIdProviderMetadata annotation = AnnotationHolder.class.getAnnotation(OpenIdProviderMetadata.class);

        OpenIdProviderMetadata composite = new CompositeOpenIdProviderMetadata(providerResponse, annotation);
        assertEquals("public, pairwise", composite.subjectTypeSupported());
        assertEquals("RS256, ES256, HS256", composite.idTokenSigningAlgorithmsSupported());
        assertEquals("code, code id_token, id_token, id_token token", composite.responseTypeSupported());
    }

    @OpenIdProviderMetadata(
            authorizationEndpoint = "https://override.exmaple.com/authorize",
            tokenEndpoint = "https://override.exmaple.com/token",
            userinfoEndpoint = "https://override.exmaple.com/userinfo",
            endSessionEndpoint = "https://override.exmaple.com/end_session",
            jwksURI = "https://override.exmaple.com/jwks.json",
            // issuer = "https://override.exmaple.com/", not overwritten to test fallback to OP providerinfo json
            subjectTypeSupported = "pairwise",
            idTokenSigningAlgorithmsSupported = "HS256",
            responseTypeSupported = "code")
    private static class AnnotationHolder {

    }

    private JsonObject readJsonObject(String json) {
        try (JsonReader reader = JsonProvider.provider().createReader(new StringReader(json))) {
            return reader.readObject();
        }
    }
}