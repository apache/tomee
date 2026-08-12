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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.passkey;

import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AttestationConveyancePreference;
import com.webauthn4j.data.AuthenticatorAttachment;
import com.webauthn4j.data.AuthenticatorSelectionCriteria;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.webauthn4j.data.PublicKeyCredentialRpEntity;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.PublicKeyCredentialUserEntity;
import com.webauthn4j.data.UserVerificationRequirement;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientInputs;
import com.webauthn4j.test.EmulatorUtil;
import com.webauthn4j.test.authenticator.webauthn.WebAuthnAuthenticatorAdaptor;
import com.webauthn4j.test.client.ClientPlatform;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.apache.tomee.bootstrap.Archive;
import org.apache.tomee.bootstrap.Server;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringReader;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class PasskeyFlowTest {

    private static String baseUrl;

    private static String origin;

    private static boolean external;

    private final ObjectConverter objectConverter = new ObjectConverter();

    @BeforeClass
    public static void setup() {
        final String override = System.getProperty("passkey.baseUri");
        if (override != null && !override.isBlank()) {
            external = true;
            baseUrl = stripTrailingSlash(override.trim());
        } else {
            external = false;
            baseUrl = stripTrailingSlash(bootEmbeddedTomEE().toString());
        }
        origin = originOf(baseUrl);
    }

    private static URI bootEmbeddedTomEE() {
        final Archive classes = Archive.archive()
                .add(PasskeyAuthenticationMechanism.class)
                .add(PasskeyCredential.class)
                .add(PasskeyLoginServlet.class)
                .add(PasskeyRegistrationServlet.class)
                .add(ProtectedServlet.class)
                .add(WebAuthnService.class)
                .add(CredentialStore.class)
                .add(UserRepository.class)
                .add(Http.class);

        final Server server = Server.builder()
                .add("webapps/ROOT/WEB-INF/classes", classes)
                .add("webapps/ROOT/WEB-INF/beans.xml", "")
                .build();

        return server.getURI();
    }

    @Test
    public void protectedResourceRejectsAnonymous() throws Exception {
        final HttpClient client = newClient();
        final HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(uri("/app")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        Assert.assertTrue("expected the protected page to reject anonymous access, got " + response.statusCode(),
                response.statusCode() == 401 || response.statusCode() == 403);
    }

    @Test
    public void firstFactorRejectsBadPassword() throws Exception {
        final HttpClient client = newClient();
        Assert.assertEquals(401,
                postJson(client, "/api/login/password", "{\"username\":\"jon\",\"password\":\"wrong\"}").statusCode());
    }

    @Test
    public void secondFactorRequiresFirstFactor() throws Exception {
        final HttpClient client = newClient();
        final HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(uri("/api/login/assertion-options")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(401, response.statusCode());
    }

    @Test
    public void passwordThenPasskeyAuthenticates() throws Exception {
        final HttpClient client = newClient();
        final ClientPlatform authenticator = softwareAuthenticator();

        firstFactor(client);
        registerPasskey(client, authenticator, "jon");

        final HttpResponse<String> assertion = login(client, authenticator);
        Assert.assertEquals(200, assertion.statusCode());
        Assert.assertTrue(assertion.body().contains("\"authenticated\":true"));
    }

    @Test
    public void loginPersistsToNextRequest() throws Exception {
        final HttpClient client = newClient();
        final ClientPlatform authenticator = softwareAuthenticator();

        firstFactor(client);
        registerPasskey(client, authenticator, "jon");
        Assert.assertEquals(200, login(client, authenticator).statusCode());

        // A brand new request on the same session - this is where persistence matters.
        final HttpResponse<String> protectedResp = client.send(
                HttpRequest.newBuilder(uri("/app")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals("follow-up request to /app was not authenticated - "
                        + "the login did not persist to the session on " + baseUrl,
                200, protectedResp.statusCode());
        Assert.assertTrue(protectedResp.body().contains("caller: <b>jon</b>"));
    }

    private void firstFactor(final HttpClient client) throws Exception {
        Assert.assertEquals(200,
                postJson(client, "/api/login/password", "{\"username\":\"jon\",\"password\":\"doe\"}").statusCode());
    }

    private void registerPasskey(final HttpClient client, final ClientPlatform authenticator, final String username)
            throws Exception {

        final JsonObject options = getJson(client, "/api/register/options");

        final PublicKeyCredentialCreationOptions creationOptions = new PublicKeyCredentialCreationOptions(
                new PublicKeyCredentialRpEntity(options.getJsonObject("rp").getString("id"),
                                                options.getJsonObject("rp").getString("name")),
                new PublicKeyCredentialUserEntity(decode(options.getJsonObject("user").getString("id")),
                                                  username, username),
                new DefaultChallenge(decode(options.getString("challenge"))),
                List.of(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                                          COSEAlgorithmIdentifier.ES256)),
                null,
                Collections.emptyList(),
                new AuthenticatorSelectionCriteria(AuthenticatorAttachment.CROSS_PLATFORM, true,
                                                   UserVerificationRequirement.PREFERRED),
                AttestationConveyancePreference.NONE,
                new AuthenticationExtensionsClientInputs<>());

        final String responseJson = objectConverter.getJsonConverter()
                .writeValueAsString(authenticator.create(creationOptions));

        Assert.assertEquals(200, postJson(client, "/api/register", responseJson).statusCode());
    }

    private HttpResponse<String> login(final HttpClient client, final ClientPlatform authenticator) throws Exception {
        final JsonObject options = getJson(client, "/api/login/assertion-options");

        final PublicKeyCredentialRequestOptions requestOptions = new PublicKeyCredentialRequestOptions(
                new DefaultChallenge(decode(options.getString("challenge"))),
                0L,
                options.getString("rpId"),
                null,
                UserVerificationRequirement.PREFERRED,
                null);

        final String responseJson = objectConverter.getJsonConverter()
                .writeValueAsString(authenticator.get(requestOptions));

        return postJson(client, "/api/login/assertion", responseJson);
    }

    private static ClientPlatform softwareAuthenticator() {
        return new ClientPlatform(new Origin(origin), new WebAuthnAuthenticatorAdaptor(EmulatorUtil.PACKED_AUTHENTICATOR));
    }

    private static HttpClient newClient() {
        // a cookie manager so the JSESSIONID is carried between requests
        return HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    private static URI uri(final String path) {
        return URI.create(baseUrl + path);
    }

    private static HttpResponse<String> postJson(final HttpClient client, final String path, final String json)
            throws Exception {
        return client.send(
                HttpRequest.newBuilder(uri(path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static JsonObject getJson(final HttpClient client, final String path) throws Exception {
        final HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("GET " + path + " -> " + response.statusCode(), 200, response.statusCode());
        return Json.createReader(new StringReader(response.body())).readObject();
    }

    private static byte[] decode(final String base64Url) {
        return Base64.getUrlDecoder().decode(base64Url);
    }

    private static String stripTrailingSlash(final String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String originOf(final String url) {
        final URI u = URI.create(url);
        final String authority = u.getPort() == -1 ? u.getHost() : u.getHost() + ":" + u.getPort();
        return u.getScheme() + "://" + authority;
    }
}
