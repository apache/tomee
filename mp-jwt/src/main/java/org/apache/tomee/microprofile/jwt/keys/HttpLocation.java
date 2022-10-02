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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class HttpLocation implements Supplier<byte[]> {
    private static final Logger LOG = Logger.getInstance(JWTLogCategories.KEYS.createChild("http"), HttpLocation.class);

    private final URI location;

    public HttpLocation(final URI location) {
        if (!location.getScheme().startsWith("http")) {
            throw new IllegalArgumentException("Expected HTTP URI, found " + location);
        }
        this.location = location;
    }

    @Override
    public byte[] get() {
        final long start = System.nanoTime();
        Integer responseCode = null;
        Integer length = null;
        String contentType = null;
        try {
            LOG.debug(String.format("Connecting to Key Server: %s", location));

            final HttpURLConnection httpUrlConnection = (HttpURLConnection) location.toURL().openConnection();
            responseCode = httpUrlConnection.getResponseCode();
            contentType = httpUrlConnection.getHeaderField("content-type");

            if (responseCode != 200) {
                final String responseMessage = httpUrlConnection.getResponseMessage();
                throw new UnexpectedHttpResponseException(responseCode, responseMessage);
            }

            try (final InputStream in = httpUrlConnection.getInputStream()) {
                final byte[] bytes = IO.readBytes(in);
                length = bytes.length;
                return bytes;
            }
        } catch (UnexpectedHttpResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ReadFailureException(e);
        } finally {
            final long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            if (responseCode == null) {
                final String message = String.format("Key Server connection failed: %s, %s ms", location, elapsed);
                LOG.error(message);
            } else if (responseCode != 200) {
                final String message = String.format("Key Server returned HTTP %s: %s, %s ms", responseCode, location, elapsed);
                LOG.error(message);
            } else {
                final String message = String.format("Key Server returned HTTP %s: %s, %s, %s bytes, %s ms",
                        responseCode, location, contentType, length, elapsed);
                LOG.info(message);
            }
        }
    }

    public static class UnexpectedHttpResponseException extends RuntimeException {
        public UnexpectedHttpResponseException(final int responseCode, final String responseMessage) {
            super(String.format("Unexpected HTTP response: %s %s", responseCode, responseMessage));
        }
    }

    public static class ReadFailureException extends RuntimeException {
        public ReadFailureException(final Exception cause) {
            super(String.format("Read failed: %s: %s", cause.getClass().getSimpleName(), cause.getMessage()), cause);
        }
    }
}
