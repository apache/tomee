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
package org.apache.tomee.microprofile.jwt.keys;

import io.churchkey.Keys;
import jakarta.enterprise.inject.spi.DeploymentException;
import org.apache.openejb.loader.IO;
import org.apache.tomee.microprofile.jwt.config.JWTAuthConfigurationProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.churchkey.Key.Type.PRIVATE;
import static io.churchkey.Key.Type.PUBLIC;
import static io.churchkey.Key.Type.SECRET;

public class KeyResolver {

    public Optional<Map<String, Key>> resolvePublicKey(final Optional<String> keyContents, final Optional<String> keyLocation) {
        return resolve(keyContents, keyLocation, this::validatePublicKeys);
    }

    public Optional<Map<String, Key>> resolveDecryptKey(final Optional<String> keyContents, final Optional<String> keyLocation) {
        return resolve(keyContents, keyLocation, this::validateDecryptKeys);
    }

    private Optional<Map<String, Key>> resolve(final Optional<String> publicKeyContents, final Optional<String> publicKeyLocation, final Consumer<List<io.churchkey.Key>> validation) {
        final Stream<Supplier<Optional<Map<String, Key>>>> possiblePublicKeys =
                Stream.of(() -> publicKeyContents.map(publicKey -> readPublicKeys(publicKey, validation)),
                        () -> publicKeyLocation.map(publicKeyLocation1 -> readPublicKeysFromLocation(publicKeyLocation1, validation)));

        return (Optional<Map<String, Key>>) possiblePublicKeys
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Map<String, Key> readPublicKeys(final String publicKey, final Consumer<List<io.churchkey.Key>> validation) {
        final List<io.churchkey.Key> keys;
        try {
            keys = Keys.decodeSet(publicKey);
        } catch (Exception e) {
            throw new DeploymentException("Unable to decode key contents: " + publicKey, e);
        }

        if (keys.size() == 0) {
            throw new DeploymentException("No keys found in key contents: " + publicKey);
        }

        validation.accept(keys);

        int unknown = 0;

        final Map<String, Key> map = new HashMap<>();
        for (final io.churchkey.Key key : keys) {
            final String id;
            if (defined(key, "kid")) {
                id = key.getAttribute("kid");
            } else if (defined(key, "Comment")) {
                id = key.getAttribute("Comment");
            } else {
                id = "Unknown " + (unknown++);
            }

            map.put(id, key.getKey());
        }

        return map;
    }

    private void validatePublicKeys(final List<io.churchkey.Key> keys) {
        checkInvalidTypes(keys, PRIVATE);
        checkInvalidTypes(keys, SECRET);
    }

    private void validateDecryptKeys(final List<io.churchkey.Key> keys) {
        checkInvalidTypes(keys, PUBLIC);
    }

    private boolean defined(final io.churchkey.Key key, final String kid) {
        final String attribute = key.getAttribute(kid);
        return attribute != null && attribute.length() > 0;
    }

    private void checkInvalidTypes(final List<io.churchkey.Key> keys, final io.churchkey.Key.Type type) {
        final long invalid = keys.stream()
                .map(io.churchkey.Key::getType)
                .filter(type::equals)
                .count();

        if (invalid > 0) {
            throw new DeploymentException("Found " + invalid + " " + type.name().toLowerCase() +
                    " keys in MP JWT key configuration.  Only Public Keys must be configured for JWT validation");
        }
    }

    private Map<String, Key> readPublicKeysFromLocation(final String publicKeyLocation, final Consumer<List<io.churchkey.Key>> validatePublicKeys) {
        final Stream<Supplier<Optional<String>>> possiblePublicKeysLocations =
                Stream.of(() -> readPublicKeysFromClasspath(publicKeyLocation),
                        () -> readPublicKeysFromFile(publicKeyLocation),
                        () -> readPublicKeysFromUrl(publicKeyLocation));

        return possiblePublicKeysLocations
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(publicKey -> readPublicKeys(publicKey, validatePublicKeys))
                .orElseThrow(() -> new DeploymentException(JWTAuthConfigurationProperties.PUBLIC_KEY_ERROR_LOCATION +
                        publicKeyLocation));
    }

    private Optional<String> readPublicKeysFromClasspath(final String publicKeyLocation) {
        try {
            final InputStream is =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(publicKeyLocation);
            if (is == null) {
                return Optional.empty();
            }
            return Optional.of(IO.slurp(is));
        } catch (final IOException e) {
            throw new DeploymentException(
                    JWTAuthConfigurationProperties.PUBLIC_KEY_ERROR_LOCATION + publicKeyLocation, e);
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
                        JWTAuthConfigurationProperties.PUBLIC_KEY_ERROR_LOCATION +
                                publicKeyLocation +
                                ". File does not exist or it is a directory.");
            }
            return Optional.of(IO.slurp(locationURL));
        } catch (final IOException | URISyntaxException e) {
            throw new DeploymentException(
                    JWTAuthConfigurationProperties.PUBLIC_KEY_ERROR_LOCATION + publicKeyLocation, e);
        }
    }

    private Optional<String> readPublicKeysFromUrl(final String publicKeyLocation) {
        final URI uri = URI.create(publicKeyLocation);

        if (uri.getScheme().startsWith("http")) {
            final byte[] bytes = new HttpLocation(uri).get();
            return Optional.of(new String(bytes, StandardCharsets.UTF_8));
        }

        final byte[] bytes = new UrlLocation(uri).get();
        return Optional.of(new String(bytes, StandardCharsets.UTF_8));
    }
}
