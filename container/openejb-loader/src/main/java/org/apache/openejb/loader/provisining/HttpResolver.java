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
package org.apache.openejb.loader.provisining;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class HttpResolver implements ArchiveResolver {
    private static final int CONNECT_TIMEOUT = 10000;

    @Override
    public String prefix() {
        return "http";
    }

    @Override
    public InputStream resolve(final String location) {
        try {
            final URL url = new URL(location);
            for (final Proxy proxy : ProxySelector.getDefault().select(url.toURI())) {
                try {
                    final URLConnection urlConnection = url.openConnection(proxy);
                    urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                    return urlConnection.getInputStream();
                } catch (final IOException e) {
                    // ignored
                }
            }
        } catch (final MalformedURLException | URISyntaxException e) {
            // no-op
        }
        return null;
    }

    @Override
    public String name(final String rawLocation) {
        return lastPart(rawLocation.replace(':', '/'));
    }

    public static String lastPart(final String location) {
        int idx = location.lastIndexOf('/');
        if (idx <= 0) {
            idx = location.lastIndexOf(':');
            if (idx <= 0) {
                return location;
            }
        }
        return location.substring(idx + 1, location.length());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[timeout=" + CONNECT_TIMEOUT + "ms]";
    }
}
