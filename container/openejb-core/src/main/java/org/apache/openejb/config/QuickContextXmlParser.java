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
package org.apache.openejb.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

public class QuickContextXmlParser extends DefaultHandler {
    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
    static {
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    private String virtualClasspath = "";

    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes) throws SAXException {
        if ("Loader".equalsIgnoreCase(localName)) {
            final String className = attributes.getValue("className");
            if ("org.apache.catalina.loader.VirtualWebappLoader".equals(className)
                    || "org.apache.tomee.catalina.ProvisioningWebappLoader".equals(className)) {
                virtualClasspath = attributes.getValue("virtualClasspath");
            } // else ?
        }
    }

    public Collection<URL> getAdditionalURLs() {
        final StringTokenizer tkn = new StringTokenizer(virtualClasspath, ";");
        final Set<URL> set = new LinkedHashSet<URL>();
        while (tkn.hasMoreTokens()) {
            String token = tkn.nextToken().trim();
            if (token.isEmpty()) {
                continue;
            }

            if (token.endsWith("*.jar")) {
                token = token.substring(0, token.length() - "*.jar".length());

                final File directory = new File(token);
                if (!directory.isDirectory()) {
                    continue;
                }

                final String filenames[] = directory.list();
                Arrays.sort(filenames);

                for (final String rawFilename : filenames) {
                    final String filename = rawFilename.toLowerCase(Locale.ENGLISH);
                    if (!filename.endsWith(".jar")) {
                        continue;
                    }

                    final File file = new File(directory, rawFilename);
                    if (!file.isFile()) {
                        continue;
                    }

                    try {
                        set.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        // no-op
                    }
                }
            } else {
                // single file or directory
                final File file = new File(token);
                if (!file.exists()) {
                    continue;
                }

                try {
                    set.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    // no-op
                }
            }
        }
        return set;
    }

    public static QuickContextXmlParser parse(final File contextXml) {
        final QuickContextXmlParser handler = new QuickContextXmlParser();
        try {
            final SAXParser parser = FACTORY.newSAXParser();
            parser.parse(contextXml, handler);
        } catch (final Exception e) {
            // no-op: not parseable so ignoring
        }
        return handler;
    }
}
