/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openejb.server.axis2.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class SimpleUriResolver {
    private URI uri;
    private URL url;
    private InputStream is;

    public SimpleUriResolver() {
    }

    public SimpleUriResolver(String path) throws IOException {
        this("", path);
    }

    public SimpleUriResolver(String baseUriStr, String uriStr) throws IOException {
        if (baseUriStr != null && baseUriStr.startsWith("jar:")) {
            tryJar(baseUriStr, uriStr);
        } else if (uriStr.startsWith("jar:")) {
            tryJar(uriStr);
        } else {
            tryFileSystem(baseUriStr, uriStr);
        }
    }

    public void resolve(String baseUriStr, String uriStr) throws IOException {
        this.uri = null;
        this.url = null;
        this.is = null;

        if (baseUriStr != null && baseUriStr.startsWith("jar:")) {
            tryJar(baseUriStr, uriStr);
        } else if (uriStr.startsWith("jar:")) {
            tryJar(uriStr);
        } else {
            tryFileSystem(baseUriStr, uriStr);
        }
    }

    private void tryFileSystem(String baseUriStr, String uriStr) throws IOException {
        try {
            URI relative;
            File uriFile = new File(uriStr);
            uriFile = new File(uriFile.getAbsolutePath());

            if (uriFile.exists()) {
                relative = uriFile.toURI();
            } else {
                relative = new URI(uriStr.replaceAll(" ", "%20"));
            }

            if (relative.isAbsolute()) {
                uri = relative;
                url = relative.toURL();
                is = url.openStream();
            } else if (baseUriStr != null) {
                URI base;
                File baseFile = new File(baseUriStr);

                if (!baseFile.exists() && baseUriStr.startsWith("file:/")) {
                    baseFile = new File(baseUriStr.substring(6));
                }

                if (baseFile.exists()) {
                    base = baseFile.toURI();
                } else {
                    base = new URI(baseUriStr);
                }

                base = base.resolve(relative);
                if (base.isAbsolute()) {
                    uri = base;
                    url = base.toURL();
                    is = url.openStream();
                }
            }
        } catch (URISyntaxException e) {
            // do nothing
        }
    }

    private void tryJar(String baseStr, String uriStr) throws IOException {
        int i = baseStr.indexOf('!');
        if (i == -1) {
            tryFileSystem(baseStr, uriStr);
        }

        String jarBase = baseStr.substring(0, i + 1);
        String jarEntry = baseStr.substring(i + 1);
        try {
            URI u = new URI(jarEntry).resolve(uriStr);

            tryJar(jarBase + u.toString());

            if (is != null) {
                if (u.isAbsolute()) {
                    url = u.toURL();
                }
                return;
            }
        } catch (URISyntaxException e) {
            // do nothing
        }

        tryFileSystem("", uriStr);
    }

    private void tryJar(String uriStr) throws IOException {
        int i = uriStr.indexOf('!');
        if (i == -1) {
            return;
        }

        url = new URL(uriStr);
        try {
            is = url.openStream();
            try {
                uri = url.toURI();
            } catch (URISyntaxException ex) {
                // ignore
            }
        } catch (IOException e) {
            // do nothing
        }
    }

    public URI getURI() {
        return uri;
    }

    public URL getURL() {
        return url;
    }

    public InputStream getInputStream() {
        return is;
    }

    public boolean isResolved() {
        return is != null;
    }
}
	