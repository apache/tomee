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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.jose4j.http.Get;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Helper for OpenID outbound HTTP calls.
 */
public final class OpenIdHttpClientSupport {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, OpenIdHttpClientSupport.class);
    private static final String RELAXED_LOCALHOST_SSL_PROPERTY = "tomee.security.openid.relaxed-localhost-ssl";

    private static final SSLContext TRUST_ALL_SSL_CONTEXT = createTrustAllSslContext();
    private static final SSLSocketFactory TRUST_ALL_SOCKET_FACTORY = TRUST_ALL_SSL_CONTEXT.getSocketFactory();
    private static final HostnameVerifier LOOPBACK_HOSTNAME_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(final String host, final SSLSession session) {
            return isLoopbackHost(host);
        }
    };

    private OpenIdHttpClientSupport() {
        // no-op
    }

    public static Client newClient(final String endpoint) {
        final ClientBuilder builder = ClientBuilder.newBuilder();
        if (shouldRelaxSsl(endpoint)) {
            LOGGER.debug("Using relaxed localhost TLS for OpenID endpoint: " + endpoint);
            builder.sslContext(TRUST_ALL_SSL_CONTEXT);
            builder.hostnameVerifier(LOOPBACK_HOSTNAME_VERIFIER);
        }
        return builder.build();
    }

    public static void configureHttpsGet(final Get get, final String endpoint) {
        if (!shouldRelaxSsl(endpoint)) {
            return;
        }

        LOGGER.debug("Using relaxed localhost TLS for OpenID JWKS endpoint: " + endpoint);
        get.setHostnameVerifier(LOOPBACK_HOSTNAME_VERIFIER);
        get.setSslSocketFactory(TRUST_ALL_SOCKET_FACTORY);
    }

    public static boolean shouldRelaxSsl(final String endpoint) {
        final URI uri = toUri(endpoint);
        if (uri == null) {
            return false;
        }

        return isRelaxedLocalhostSslEnabled() && "https".equalsIgnoreCase(uri.getScheme()) && isLoopbackHost(uri.getHost());
    }

    public static boolean isLoopbackHost(final String host) {
        if (host == null || host.isEmpty()) {
            return false;
        }

        if ("localhost".equalsIgnoreCase(host)) {
            return true;
        }

        try {
            return InetAddress.getByName(host).isLoopbackAddress();
        } catch (final UnknownHostException e) {
            return false;
        }
    }

    public static int explicitOrDefaultPort(final URI uri) {
        if (uri == null) {
            return -1;
        }

        if (uri.getPort() > 0) {
            return uri.getPort();
        }

        final String scheme = uri.getScheme();
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        }
        return -1;
    }

    public static URI replaceAuthority(final URI source, final URI authoritySource) {
        if (source == null || authoritySource == null || authoritySource.getHost() == null) {
            return source;
        }

        final int port = explicitOrDefaultPort(authoritySource);
        try {
            return new URI(
                    authoritySource.getScheme() == null ? source.getScheme() : authoritySource.getScheme(),
                    source.getUserInfo(),
                    authoritySource.getHost(),
                    port,
                    source.getRawPath(),
                    source.getRawQuery(),
                    source.getRawFragment());
        } catch (final URISyntaxException e) {
            return source;
        }
    }

    public static boolean hasCause(final Throwable throwable, final Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public static URI toUri(final String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return URI.create(value);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean isRelaxedLocalhostSslEnabled() {
        return Boolean.parseBoolean(System.getProperty(RELAXED_LOCALHOST_SSL_PROPERTY, "true"));
    }

    private static SSLContext createTrustAllSslContext() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {new TrustAllX509TrustManager()}, new SecureRandom());
            return sslContext;
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Unable to initialize relaxed localhost TLS support for OpenID", e);
        }
    }

    private static final class TrustAllX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            // no-op
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            // no-op
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
