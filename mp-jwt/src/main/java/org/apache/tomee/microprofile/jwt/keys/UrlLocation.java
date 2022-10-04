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

import org.apache.openejb.loader.IO;
import org.apache.openejb.util.Logger;
import org.apache.tomee.microprofile.jwt.JWTLogCategories;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class UrlLocation implements Supplier<byte[]> {

    static final Logger LOG = Logger.getInstance(JWTLogCategories.KEYS, UrlLocation.class);
    private final URI location;

    public UrlLocation(final URI location) {
        this.location = location;
    }

    @Override
    public byte[] get() {
        final long start = System.nanoTime();

        try {
            LOG.debug(String.format("Opening connection to Key Location %s", location));

            final URL locationURL = location.toURL();

            final byte[] bytes = IO.readBytes(locationURL);

            {
                final long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                final String message = String.format("Key Location %s returned %s bytes in %s ms", location, bytes.length, elapsed);
                LOG.info(message);
            }
            return bytes;
        } catch (IOException e) {
            final long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            LOG.error(String.format("Key Location %s read failed in %s ms", location, elapsed), e);
            throw new ReadFailureException(e);
        }
    }

    public static class ReadFailureException extends RuntimeException {
        public ReadFailureException(final Exception cause) {
            super(String.format("Read failed: %s: %s", cause.getClass().getSimpleName(), cause.getMessage()), cause);
        }
    }
}
