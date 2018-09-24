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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.eclipse.microprofile.jwt.config.Names.ISSUER;
import static org.eclipse.microprofile.jwt.config.Names.VERIFIER_PUBLIC_KEY;
import static org.eclipse.microprofile.jwt.config.Names.VERIFIER_PUBLIC_KEY_LOCATION;

@ApplicationScoped
public class ConfigurableJWTAuthContextInfo {
    private static final Logger log = Logger.getLogger(ConfigurableJWTAuthContextInfo.class.getName());

    @Inject
    private Config config;

    private JWTAuthContextInfo jwtAuthContextInfo;

    public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
        this.jwtAuthContextInfo = createJWTAuthContextInfo();
    }

    public Optional<JWTAuthContextInfo> getJWTAuthContextInfo() {
        return Optional.ofNullable(jwtAuthContextInfo);
    }

    private Optional<String> getVerifierPublicKey() {
        return config.getOptionalValue(VERIFIER_PUBLIC_KEY, String.class);
    }

    private Optional<String> getPublicKeyLocation() {
        return config.getOptionalValue(VERIFIER_PUBLIC_KEY_LOCATION, String.class);
    }

    private Optional<String> getIssuer() {
        return config.getOptionalValue(ISSUER, String.class);
    }

    private JWTAuthContextInfo createJWTAuthContextInfo() {
        final Stream<Supplier<Optional<RSAPublicKey>>> possiblePublicKeys =
                Stream.of(() -> getVerifierPublicKey().map(this::readPublicKey),
                          () -> getPublicKeyLocation().map(this::readPublicKeyFromLocation));

        return possiblePublicKeys
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(key -> new JWTAuthContextInfo(key, getIssuer().orElse(null)))
                .orElse(null);
    }

    private RSAPublicKey readPublicKey(final String publicKey) {
        return parsePCKS8(publicKey);
    }

    private RSAPublicKey readPublicKeyFromLocation(final String publicKeyLocation) {
        final Stream<Supplier<Optional<String>>> possiblePublicKeysLocations =
                Stream.of(() -> readPublicKeyFromClasspath(publicKeyLocation),
                          () -> readPublicKeyFromFile(publicKeyLocation),
                          () -> readPublicKeyFromHttp(publicKeyLocation),
                          () -> readPublicKeyFromUrl(publicKeyLocation));

        return possiblePublicKeysLocations
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(this::readPublicKey)
                .orElseThrow(() -> new DeploymentException("Could not read MicroProfile Public Key from Location: " +
                                                           publicKeyLocation));
    }

    private Optional<String> readPublicKeyFromClasspath(final String publicKeyLocation) {
        try {
            final InputStream is =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(publicKeyLocation);
            if (is == null) {
                return Optional.empty();
            }
            return Optional.of(readPublicKeyFromInputStream(is));
        } catch (final IOException e) {
            throw new DeploymentException(
                    "Could not read MicroProfile Public Key from Location: " + publicKeyLocation, e);
        }
    }

    private Optional<String> readPublicKeyFromFile(final String publicKeyLocation) {
        if (!publicKeyLocation.startsWith("file")) {
            return Optional.empty();
        }

        try {
            final URL locationURL = new URL(publicKeyLocation);

            final File publicKeyFile = new File(locationURL.toURI());
            if (!publicKeyFile.exists() || publicKeyFile.isDirectory()) {
                throw new DeploymentException(
                        "Could not read MicroProfile Public Key from Location: " +
                        publicKeyLocation +
                        ". File does not exist or it is a directory.");
            }
            return Optional.of(readPublicKeyFromInputStream(locationURL.openStream()));
        } catch (final IOException | URISyntaxException e) {
            throw new DeploymentException(
                    "Could not read MicroProfile Public Key from Location: " + publicKeyLocation, e);
        }
    }

    private Optional<String> readPublicKeyFromHttp(final String publicKeyLocation) {
        if (!publicKeyLocation.startsWith("http")) {
            return Optional.empty();
        }

        try {
            final URL locationURL = new URL(publicKeyLocation);
            return Optional.of(readPublicKeyFromInputStream(locationURL.openStream()));
        } catch (final IOException e) {
            throw new DeploymentException(
                    "Could not read MicroProfile Public Key from Location: " + publicKeyLocation, e);
        }
    }

    private Optional<String> readPublicKeyFromUrl(final String publicKeyLocation) {
        return Optional.empty();
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

    private RSAPublicKey parsePCKS8(final String publicKey) {
        isPrivatePCKS8(publicKey);
        try {
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(normalizeAndDecodePCKS8(publicKey));
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new DeploymentException("Could not read MicroProfile Public Key: " + publicKey, e);
        }
    }

    private void isPrivatePCKS8(final String publicKey) {
        if (publicKey.contains("PRIVATE KEY")) {
            throw new DeploymentException("MicroProfile JWT Public Key is Private.");
        }
    }

    private byte[] normalizeAndDecodePCKS8(final String publicKey) {
        final String normalizedKey =
                publicKey.replaceAll("-----BEGIN (.*)-----", "")
                         .replaceAll("-----END (.*)----", "")
                         .replaceAll("\r\n", "")
                         .replaceAll("\n", "");

        return Base64.getDecoder().decode(normalizedKey);
    }
}
