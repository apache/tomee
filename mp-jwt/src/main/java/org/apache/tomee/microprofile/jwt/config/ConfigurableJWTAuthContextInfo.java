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
import org.eclipse.microprofile.jwt.config.Names;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@RequestScoped
public class ConfigurableJWTAuthContextInfo {
    @Inject
    private Config config;

    public Optional<JWTAuthContextInfo> getJWTAuthContextInfo() {
        final Optional<String> publicKey = config.getOptionalValue(Names.VERIFIER_PUBLIC_KEY, String.class);
        final Optional<String> issuer = config.getOptionalValue(Names.ISSUER, String.class);

        if (publicKey.isPresent()) {
            final Optional<RSAPublicKey> rsaPublicKey = parsePCKS8(publicKey.get());
            if (rsaPublicKey.isPresent()) {
                return Optional.of(new JWTAuthContextInfo(rsaPublicKey.get(), issuer.orElse("")));
            }
        }

        return Optional.empty();
    }

    private Optional<RSAPublicKey> parsePCKS8(final String publicKey) {
        isPrivatePCKS8(publicKey);
        try {
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(normalizeAndDecodePCKS8(publicKey));
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            return Optional.of((RSAPublicKey) kf.generatePublic(spec));
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            return Optional.empty();
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
