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
package org.apache.openejb.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Client side of the secret the standalone admin daemon expects after the
 * stop request code: the stop code byte, the secret and a NUL.
 *
 * <p>Only depends on the JDK since it is also bundled in the executable
 * jars built by the TomEE maven plugin.</p>
 */
public final class AdminShutdownSecret {

    /**
     * File written in the conf directory at server startup, readable by the owner only.
     */
    public static final String FILE_NAME = "admin-shutdown.secret";

    /**
     * Fixed secret, used instead of a random one generated at every start.
     */
    public static final String PROPERTY = "openejb.admin.shutdown.secret";

    /**
     * Set it to false on the server to accept stop requests without a secret.
     */
    public static final String REQUIRED_PROPERTY = "openejb.admin.shutdown.secret.required";

    private AdminShutdownSecret() {
        // no-op
    }

    /**
     * @param home the server home (or base) directory
     * @return the {@link #PROPERTY} system property if set, else the content of
     * {@code <home>/conf/admin-shutdown.secret}, or null if none can be read
     */
    public static String find(final File home) {
        return find(System.getProperty(PROPERTY), home);
    }

    /**
     * @param configured the configured secret, used when not blank
     * @param home       the server home (or base) directory, may be null
     * @return the secret or null if none can be found
     */
    public static String find(final String configured, final File home) {
        return findInConf(configured, home == null ? null : new File(home, "conf"));
    }

    /**
     * @param configured the configured secret, used when not blank
     * @param conf       the server conf directory, may be null
     * @return the secret or null if none can be found
     */
    public static String findInConf(final String configured, final File conf) {
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }
        if (conf == null) {
            return null;
        }
        return read(new File(conf, FILE_NAME));
    }

    /**
     * @return the trimmed content of the file, or null if it is missing, unreadable or blank
     */
    public static String read(final File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            final String secret = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            return secret.isEmpty() ? null : secret;
        } catch (final IOException | SecurityException e) {
            return null;
        }
    }

    /**
     * @param code   the stop request code byte
     * @param secret the secret, may be null when the server does not require one
     * @return the stop code byte followed by the secret and a NUL
     */
    public static byte[] stopMessage(final int code, final String secret) {
        final ByteArrayOutputStream message = new ByteArrayOutputStream();
        message.write(code);
        if (secret != null) {
            final byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            message.write(bytes, 0, bytes.length);
        }
        message.write(0);
        return message.toByteArray();
    }
}
