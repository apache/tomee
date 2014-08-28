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

package org.apache.openejb.core.webservices;

import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class WsdlResolver implements WSDLLocator {
    private String baseUri;
    private String importedUri;
    private InputSource inputSource;

    public WsdlResolver(final String baseURI, final InputSource is) {
        this.baseUri = baseURI;
        inputSource = is;
    }

    public InputSource getBaseInputSource() {
        return inputSource;
    }

    public String getBaseURI() {
        return baseUri;
    }

    public String getLatestImportURI() {
        return importedUri;
    }

    public InputSource getImportInputSource(final String parent, final String importLocation) {
        this.baseUri = parent;
        final URL parentUrl;
        try {
            parentUrl = new URL(parent);
            final URL importUrl = new URL(parentUrl, importLocation);
            if (importUrl != null && !importUrl.getProtocol().startsWith("file")) {
                final URLConnection con = importUrl.openConnection();
                con.setUseCaches(false);
                inputSource = new InputSource(con.getInputStream());
            } else {
                final File file = new File(importUrl.toURI());
                if (file.exists()) {
                    final UriResolver resolver = new UriResolver(parent.toString(), importLocation);
                    inputSource = new InputSource(resolver.getInputStream());
                } else {
                    final UriResolver resolver = new UriResolver(importLocation);
                    if (resolver.isResolved()) {
                        inputSource = new InputSource(resolver.getInputStream());
                    }
                }
            }
            importedUri = importUrl.toURI().toString();

        } catch (final URISyntaxException | IOException e) {
            // no-op
        }
        return inputSource;

    }

    public void close() {
        if (inputSource.getByteStream() != null) {
            try {
                inputSource.getByteStream().close();
            } catch (final IOException e) {
                //
            }
        }

    }
}
