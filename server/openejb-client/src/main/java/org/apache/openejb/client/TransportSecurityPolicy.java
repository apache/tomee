/**
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
package org.apache.openejb.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps server-pushed locations (cluster updates, failover lists,
 * authentication redirects) on a transport at least as protected as the one
 * the client was configured with: a client using a TLS scheme
 * (ejbds/zejbds/https) only follows pushed locations with an equally
 * protected scheme unless {@link #ALLOW_DOWNGRADE} is set.
 */
final class TransportSecurityPolicy {

    public static final String ALLOW_DOWNGRADE = "openejb.client.allowTransportDowngrade";

    private static final Logger LOGGER = Logger.getLogger("OpenEJB.client");

    private TransportSecurityPolicy() {
        // utility class
    }

    static boolean isSecure(final URI uri) {
        if (uri == null) {
            return false;
        }
        final String scheme = uri.getScheme();
        return "ejbds".equalsIgnoreCase(scheme) || "zejbds".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    static boolean accepts(final URI baseline, final URI pushed) {
        return !isSecure(baseline) || isSecure(pushed) || allowDowngrade();
    }

    /**
     * @return the pushed array untouched when no filtering applies, else a new
     * array retaining only the locations at least as protected as the baseline
     */
    static URI[] filter(final URI baseline, final URI[] pushed) {
        if (pushed == null || !isSecure(baseline) || allowDowngrade()) {
            return pushed;
        }
        final List<URI> accepted = new ArrayList<>(pushed.length);
        for (final URI uri : pushed) {
            if (isSecure(uri)) {
                accepted.add(uri);
            } else {
                LOGGER.log(Level.WARNING, "Ignoring server-pushed location " + uri
                    + ": it would downgrade the transport security of " + baseline
                    + ". Set -D" + ALLOW_DOWNGRADE + "=true to allow it.");
            }
        }
        if (accepted.size() == pushed.length) {
            return pushed;
        }
        return accepted.toArray(new URI[0]);
    }

    private static boolean allowDowngrade() {
        return Boolean.getBoolean(ALLOW_DOWNGRADE);
    }
}
