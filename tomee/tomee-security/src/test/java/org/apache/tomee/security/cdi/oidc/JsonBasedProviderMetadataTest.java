package org.apache.tomee.security.cdi.oidc;

import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdProviderMetadata;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class JsonBasedProviderMetadataTest {
    @Test
    public void testWithRealProviderMetadata() {
        // Example from https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationResponse
        String json = """
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
        """;

        JsonParser jsonParser = JsonProvider.provider().createParser(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        JsonObject jsonObject = jsonParser.getObject();
        jsonParser.close();

        OpenIdProviderMetadata providerMetadata = new JsonBasedProviderMetadata(jsonObject);
        assertEquals("https://server.example.com/connect/authorize", providerMetadata.authorizationEndpoint());
        assertEquals("https://server.example.com/connect/token", providerMetadata.tokenEndpoint());
        assertEquals("https://server.example.com/connect/userinfo", providerMetadata.userinfoEndpoint());
        assertEquals("https://server.example.com/connect/end_session", providerMetadata.endSessionEndpoint());
        assertEquals("https://server.example.com/jwks.json", providerMetadata.jwksURI());
        assertEquals("https://server.example.com", providerMetadata.issuer());
        assertEquals("public,pairwise", providerMetadata.subjectTypeSupported());
        assertEquals("RS256,ES256,HS256", providerMetadata.idTokenSigningAlgorithmsSupported());
        assertEquals("code,code id_token,id_token,id_token token", providerMetadata.responseTypeSupported());
    }
}