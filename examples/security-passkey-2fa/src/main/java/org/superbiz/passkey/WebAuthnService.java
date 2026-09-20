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
package org.superbiz.passkey;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Base64;
import java.util.List;

@ApplicationScoped
public class WebAuthnService {

    private static final String RP_NAME = "TomEE Passkey Demo";
    private static final long TIMEOUT_MS = 60_000L;

    static final String REG_CHALLENGE = "passkey.registration.challenge";
    static final String AUTH_CHALLENGE = "passkey.assertion.challenge";

    private final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();

    @Inject
    private CredentialStore credentialStore;

    public String registrationOptions(final HttpServletRequest request, final String username) {
        final Challenge challenge = new DefaultChallenge();
        request.getSession().setAttribute(REG_CHALLENGE, encode(challenge.getValue()));

        final var pubKeyCredParams = Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("type", "public-key").add("alg", -7))    // ES256
                .add(Json.createObjectBuilder().add("type", "public-key").add("alg", -257));  // RS256

        final var excludeCredentials = Json.createArrayBuilder();
        for (final byte[] id : credentialStore.credentialIds(username)) {
            excludeCredentials.add(Json.createObjectBuilder()
                    .add("type", "public-key")
                    .add("id", encode(id)));
        }

        return Json.createObjectBuilder()
                .add("challenge", encode(challenge.getValue()))
                .add("rp", Json.createObjectBuilder().add("id", rpId(request)).add("name", RP_NAME))
                .add("user", Json.createObjectBuilder()
                        .add("id", encode(username.getBytes()))
                        .add("name", username)
                        .add("displayName", username))
                .add("pubKeyCredParams", pubKeyCredParams)
                .add("timeout", TIMEOUT_MS)
                .add("attestation", "none")
                .add("authenticatorSelection", Json.createObjectBuilder()
                        .add("residentKey", "preferred")
                        .add("userVerification", "preferred"))
                .add("excludeCredentials", excludeCredentials)
                .build()
                .toString();
    }

    public void finishRegistration(final HttpServletRequest request,
                                   final String username,
                                   final String responseJson) {

        final ServerProperty serverProperty = serverProperty(request, REG_CHALLENGE);

        final RegistrationData registrationData = webAuthnManager.parseRegistrationResponseJSON(responseJson);

        // pubKeyCredParams = null (accept what we offered), UV not required, UP required
        final RegistrationParameters parameters =
                new RegistrationParameters(serverProperty, null, false, true);

        webAuthnManager.verify(registrationData, parameters);

        final CredentialRecord record = new CredentialRecordImpl(
                registrationData.getAttestationObject(),
                registrationData.getCollectedClientData(),
                registrationData.getClientExtensions(),
                registrationData.getTransports());

        final byte[] credentialId = registrationData.getAttestationObject()
                .getAuthenticatorData()
                .getAttestedCredentialData()
                .getCredentialId();

        credentialStore.save(username, credentialId, record);
        request.getSession().removeAttribute(REG_CHALLENGE);
    }

    public String assertionOptions(final HttpServletRequest request, final String username) {
        final Challenge challenge = new DefaultChallenge();
        request.getSession().setAttribute(AUTH_CHALLENGE, encode(challenge.getValue()));

        final var allowCredentials = Json.createArrayBuilder();
        for (final byte[] id : credentialStore.credentialIds(username)) {
            allowCredentials.add(Json.createObjectBuilder()
                    .add("type", "public-key")
                    .add("id", encode(id)));
        }

        return Json.createObjectBuilder()
                .add("challenge", encode(challenge.getValue()))
                .add("rpId", rpId(request))
                .add("timeout", TIMEOUT_MS)
                .add("userVerification", "preferred")
                .add("allowCredentials", allowCredentials)
                .build()
                .toString();
    }

    public String finishAssertion(final HttpServletRequest request, final String responseJson) {
        final ServerProperty serverProperty = serverProperty(request, AUTH_CHALLENGE);

        final AuthenticationData authenticationData = webAuthnManager.parseAuthenticationResponseJSON(responseJson);

        final CredentialStore.Entry entry = credentialStore.find(authenticationData.getCredentialId())
                .orElseThrow(() -> new IllegalStateException("Unknown credential"));

        final List<byte[]> allowCredentials = null;
        final AuthenticationParameters parameters =
                new AuthenticationParameters(serverProperty, entry.getRecord(), allowCredentials, false, true);

        webAuthnManager.verify(authenticationData, parameters);

        entry.getRecord().setCounter(authenticationData.getAuthenticatorData().getSignCount());
        credentialStore.updateRecord(authenticationData.getCredentialId(), entry.getRecord());

        request.getSession().removeAttribute(AUTH_CHALLENGE);
        return entry.getUsername();
    }

    private ServerProperty serverProperty(final HttpServletRequest request, final String challengeAttr) {
        final String stored = (String) request.getSession().getAttribute(challengeAttr);
        if (stored == null) {
            throw new IllegalStateException("No challenge in session - call the options endpoint first");
        }
        final Challenge challenge = new DefaultChallenge(decode(stored));
        return new ServerProperty(origin(request), rpId(request), challenge, null);
    }

    private static String rpId(final HttpServletRequest request) {
        return request.getServerName();
    }

    private static Origin origin(final HttpServletRequest request) {
        final String scheme = request.getScheme();
        final int port = request.getServerPort();
        final boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        final String authority = defaultPort
                ? request.getServerName()
                : request.getServerName() + ":" + port;
        return new Origin(scheme + "://" + authority);
    }

    private static String encode(final byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] decode(final String base64Url) {
        return Base64.getUrlDecoder().decode(base64Url);
    }

    static JsonObject parse(final String json) {
        return Json.createReader(new java.io.StringReader(json)).readObject();
    }
}
