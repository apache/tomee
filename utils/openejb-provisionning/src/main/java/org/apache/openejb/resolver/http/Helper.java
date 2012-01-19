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
package org.apache.openejb.resolver.http;

import org.apache.openejb.loader.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class Helper {
    private static final int CONNECT_TIMEOUT = 10000;

    private Helper() {
        // no-op
    }

    public static void copyTryingProxies(final URI source, final File destination) throws Exception {
        final List<Proxy> proxies = ProxySelector.getDefault().select(source);
        final URL url = source.toURL();
        for (Proxy proxy : ProxySelector.getDefault().select(source)) {
            InputStream is;

            // try to connect
            try {
                URLConnection urlConnection = url.openConnection(proxy);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                is = urlConnection.getInputStream();
            } catch (IOException e) {
                continue;
            }

            // parse
            FileUtils.copy(new FileOutputStream(destination), is);
            break;
        }
    }
}
