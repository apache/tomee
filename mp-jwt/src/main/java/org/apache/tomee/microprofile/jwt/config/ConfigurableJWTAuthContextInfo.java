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
package org.apache.tomee.microprofile.jwt.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.DeploymentException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;
import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.microprofile.jwt.config.Names.ISSUER;
import static org.eclipse.microprofile.jwt.config.Names.VERIFIER_PUBLIC_KEY;
import static org.eclipse.microprofile.jwt.config.Names.VERIFIER_PUBLIC_KEY_LOCATION;
import static org.jose4j.jwk.JsonWebKeySet.JWK_SET_MEMBER_NAME;

@ApplicationScoped
public class ConfigurableJWTAuthContextInfo {
    private static final List<String> JWK_SUPPORTED_KEY_TYPES = Arrays.asList("RSA");

    private Config config;
    private JWTAuthContextInfo jwtAuthContextInfo;
    private static final String PUBLIC_KEY_ERROR = "Could not read MicroProfile Public Key";
    private static final String PUBLIC_KEY_ERROR_LOCATION = PUBLIC_KEY_ERROR + " from Location: ";

    public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
        this.config = ConfigProvider.getConfig();
        this.jwtAuthContextInfo = createJWTAuthContextInfo();
    }

    public Optional<JWTAuthContextInfo> getJWTAuthContextInfo() {
        return Optional.ofNullable(jwtAuthContextInfo);
    }

    private Optional<String> getVerifierPublicKey() {
        return config.getOptionalValue(VERIFIER_PUBLIC_KEY, String.class).map(s -> s.isEmpty() ? null : s);
    }

    private Optional<String> getPublicKeyLocation() {
        return config.getOptionalValue(VERIFIER_PUBLIC_KEY_LOCATION, String.class).map(s -> s.isEmpty() ? null : s);
    }

    private Optional<String> getIssuer() {
        return config.getOptionalValue(ISSUER, String.class);
    }

    private JWTAuthContextInfo createJWTAuthContextInfo() {
        if (getVerifierPublicKey().isPresent() && getPublicKeyLocation().isPresent()) {
            throw new DeploymentException("Both " +
                                          VERIFIER_PUBLIC_KEY +
                                          " and " +
                                          VERIFIER_PUBLIC_KEY_LOCATION +
                                          " are being supplied. You must use only one.");
        }

        final Stream<Supplier<Optional<Map<String, Key>>>> possiblePublicKeys =
                Stream.of(() -> getVerifierPublicKey().map(this::readPublicKeys),
                          () -> getPublicKeyLocation().map(this::readPublicKeysFromLocation));

        return possiblePublicKeys
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(keys -> JWTAuthContextInfo.authContextInfo(keys, getIssuer().orElse(null)))
                .orElse(null);
    }

    private Map<String, Key> readPublicKeys(final String publicKey) {
        final Stream<Supplier<Map<String, Key>>> possiblePublicKeysParses =
                Stream.of(() -> parsePCKS8(publicKey),
                          () -> parseJwk(publicKey),
                          () -> parseJwkDecoded(publicKey),
                          () -> parseJwks(publicKey),
                          () -> parseJwksDecoded(publicKey));

        return possiblePublicKeysParses
                .map(Supplier::get)
                .filter(keys -> !keys.isEmpty())
                .findFirst()
                .orElseThrow(() -> new DeploymentException(": " + publicKey));
    }

    private Map<String, Key> readPublicKeysFromLocation(final String publicKeyLocation) {
        final Stream<Supplier<Optional<String>>> possiblePublicKeysLocations =
                Stream.of(() -> readPublicKeysFromClasspath(publicKeyLocation),
                          () -> readPublicKeysFromFile(publicKeyLocation),
                          () -> readPublicKeysFromHttp(publicKeyLocation),
                          () -> readPublicKeysFromUrl(publicKeyLocation));

        return possiblePublicKeysLocations
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(this::readPublicKeys)
                .orElseThrow(() -> new DeploymentException(PUBLIC_KEY_ERROR_LOCATION +
                                                           publicKeyLocation));
    }

    private Optional<String> readPublicKeysFromClasspath(final String publicKeyLocation) {
        try {
            final InputStream is =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(publicKeyLocation);
            if (is == null) {
                return Optional.empty();
            }
            return Optional.of(readPublicKeyFromInputStream(is));
        } catch (final IOException e) {
            throw new DeploymentException(
                    PUBLIC_KEY_ERROR_LOCATION + publicKeyLocation, e);
        }
    }

    private Optional<String> readPublicKeysFromFile(final String publicKeyLocation) {
        if (!publicKeyLocation.startsWith("file")) {
            return Optional.empty();
        }

        try {
            final URL locationURL = new URL(publicKeyLocation);

            final File publicKeyFile = new File(locationURL.toURI());
            if (!publicKeyFile.exists() || publicKeyFile.isDirectory()) {
                throw new DeploymentException(
                        PUBLIC_KEY_ERROR_LOCATION +
                        publicKeyLocation +
                        ". File does not exist or it is a directory.");
            }
            return Optional.of(readPublicKeyFromInputStream(locationURL.openStream()));
        } catch (final IOException | URISyntaxException e) {
            throw new DeploymentException(
                    PUBLIC_KEY_ERROR_LOCATION + publicKeyLocation, e);
        }
    }

    private Optional<String> readPublicKeysFromHttp(final String publicKeyLocation) {
        if (!publicKeyLocation.startsWith("http")) {
            return Optional.empty();
        }

        try {
            final URL locationURL = new URL(publicKeyLocation);
            return Optional.of(readPublicKeyFromInputStream(locationURL.openStream()));
        } catch (final IOException e) {
            throw new DeploymentException(
                    PUBLIC_KEY_ERROR_LOCATION + publicKeyLocation, e);
        }
    }

    private Optional<String> readPublicKeysFromUrl(final String publicKeyLocation) {
        try {
            final URL locationURL = new URL(publicKeyLocation);
            return Optional.of(readPublicKeyFromInputStream(locationURL.openStream()));
        } catch (final IOException e) {
            throw new DeploymentException(
                    PUBLIC_KEY_ERROR_LOCATION + publicKeyLocation, e);
        }
    }

    private String readPublicKeyFromInputStream(final InputStream publicKey) throws IOException {
        final StringWriter content = new StringWriter();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(publicKey))) {
            String line = reader.readLine();
            while (line != null) {
                content.write(line);
                content.write('\n');
                line = reader.readLine();
            }
        }
        return content.toString();
    }

    private Map<String, Key> parsePCKS8(final String publicKey) {
        try {
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(normalizeAndDecodePCKS8(publicKey));
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            return Collections.singletonMap(null, kf.generatePublic(spec));
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            return Collections.emptyMap();
        }
    }

    private Map<String, Key> parseJwk(final String publicKey) {
        final JsonObject jwk;
        try {
            jwk = Json.createReader(new StringReader(publicKey)).readObject();
        } catch (final JsonParsingException e) {
            return Collections.emptyMap();
        }

        if (jwk.containsKey(JWK_SET_MEMBER_NAME)) {
            return Collections.emptyMap();
        }

        validateJwk(jwk);

        try {
            final JsonWebKey key = JsonWebKey.Factory.newJwk(publicKey);
            return Collections.singletonMap(key.getKeyId(), key.getKey());
        } catch (final JoseException e) {
            throw new DeploymentException(PUBLIC_KEY_ERROR + " JWK.", e);
        }
    }

    private Map<String, Key> parseJwkDecoded(final String publicKey) {
        final String publicKeyDecoded;
        try {
            publicKeyDecoded = new String(Base64.getDecoder().decode(publicKey));
        } catch (final Exception e) {
            return Collections.emptyMap();
        }

        return parseJwk(publicKeyDecoded);
    }

    private Map<String, Key> parseJwks(final String publicKey) {
        final JsonObject jwks;
        try {
            jwks = Json.createReader(new StringReader(publicKey)).readObject();
        } catch (final JsonParsingException e) {
            return Collections.emptyMap();
        }

        try {
            final JsonArray keys = jwks.getJsonArray(JWK_SET_MEMBER_NAME);
            for (final JsonValue key : keys) {
                validateJwk(key.asJsonObject());
            }
        } catch (final Exception e) {
            throw new DeploymentException("MicroProfile Public Key JWKS invalid format.");
        }

        try {
            final JsonWebKeySet keySet = new JsonWebKeySet(publicKey);
            final Map<String, Key> keys =
                    keySet.getJsonWebKeys()
                          .stream()
                          .collect(Collectors.toMap(JsonWebKey::getKeyId, JsonWebKey::getKey));
            return Collections.unmodifiableMap(keys);
        } catch (final JoseException e) {
            throw new DeploymentException(PUBLIC_KEY_ERROR + " JWK.", e);
        }
    }

    private Map<String, Key> parseJwksDecoded(final String publicKey) {
        final String publicKeyDecoded;
        try {
            publicKeyDecoded = new String(Base64.getDecoder().decode(publicKey));
        } catch (final Exception e) {
            return Collections.emptyMap();
        }

        return parseJwks(publicKeyDecoded);
    }

    private void validateJwk(final JsonObject jwk) {
        final String keyType = jwk.getString("kty", null);
        if (keyType == null) {
            throw new DeploymentException("MicroProfile Public Key JWK kty field is missing.");
        }

        if (!JWK_SUPPORTED_KEY_TYPES.contains(keyType)) {
            throw new DeploymentException("MicroProfile Public Key JWK kty not supported: " + keyType);
        }
    }

    private byte[] normalizeAndDecodePCKS8(final String publicKey) {
        if (publicKey.contains("PRIVATE KEY")) {
            throw new DeploymentException("MicroProfile Public Key is Private.");
        }

        final String normalizedKey =
                publicKey.replaceAll("-----BEGIN (.*)-----", "")
                         .replaceAll("-----END (.*)----", "")
                         .replaceAll("\r\n", "")
                         .replaceAll("\n", "");

        return Base64.getDecoder().decode(normalizedKey);
    }
}
