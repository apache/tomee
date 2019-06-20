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

import org.apache.openejb.util.Saxs;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public final class ScanUtil {
    private ScanUtil() {
        // no-op
    }

    public static ScanHandler read(final URL scanXml) throws IOException {
        try {
            final SAXParser parser = Saxs.factory().newSAXParser();
            final ScanHandler handler = new ScanHandler();
            parser.parse(new BufferedInputStream(scanXml.openStream()), handler);
            return handler;
        } catch (final Exception e) {
            throw new IOException("can't parse " + scanXml.toExternalForm());
        }
    }

    public static final class ScanHandler extends DefaultHandler {
        private final Set<String> classes = new HashSet<>();
        private final Set<String> packages = new HashSet<>();
        private Set<String> current;
        private boolean optimized = true;

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            switch (qName) {
                case "class":
                    current = classes;
                    break;
                case "package":
                    current = packages;
                    break;
                case "scan":
                    final String optimized = attributes.getValue("optimized");
                    this.optimized = optimized == null || Boolean.parseBoolean(optimized);
                    break;
            }
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            if (current != null) {
                current.add(new String(ch, start, length));
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            current = null;
        }

        public boolean isOptimized() {
            return optimized;
        }

        public Set<String> getPackages() {
            return packages;
        }

        public Set<String> getClasses() {
            return classes;
        }
    }
}
