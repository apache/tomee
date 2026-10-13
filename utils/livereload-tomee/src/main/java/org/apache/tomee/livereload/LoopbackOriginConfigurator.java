/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.livereload;

import jakarta.websocket.server.ServerEndpointConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Only accepts websocket handshakes from loopback pages, browser extensions
 * and non browser clients (no Origin header) unless any origin was explicitly allowed.
 */
public class LoopbackOriginConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public boolean checkOrigin(final String originHeaderValue) {
        return Instances.get().isAllowAnyOrigin() || isLocalOrigin(originHeaderValue);
    }

    static boolean isLocalOrigin(final String origin) {
        if (origin == null || origin.isEmpty()) {
            return true;
        }

        final URI uri;
        try {
            uri = new URI(origin);
        } catch (final URISyntaxException e) {
            return false;
        }

        final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        switch (scheme) {
            case "chrome-extension":
            case "moz-extension":
            case "safari-web-extension":
                return true;
            case "http":
            case "https":
                final String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
                return "localhost".equals(host) || "127.0.0.1".equals(host) || "[::1]".equals(host);
            default:
                return false;
        }
    }
}
