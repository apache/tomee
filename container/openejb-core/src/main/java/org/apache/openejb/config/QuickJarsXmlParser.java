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

import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

public class QuickJarsXmlParser extends DefaultHandler {
    public static final String FILE_NAME = "jars.xml";

    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
    static {
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    private final Collection<URL> deps = new ArrayList<URL>();

    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes) throws SAXException {
        if ("jar".equalsIgnoreCase(localName)) {
            final String value = attributes.getValue("path");
            if (value != null) {
                try {
                    deps.add(new File(ProvisioningUtil.realLocation(value)).toURI().toURL());
                } catch (final MalformedURLException e) {
                    Logger.getInstance(LogCategory.OPENEJB, QuickJarsXmlParser.class).error("Can't find " + value);
                }
            }
        }
    }

    public Collection<URL> getAdditionalURLs() {
        return deps;
    }

    public static QuickJarsXmlParser parse(final File contextXml) {
        final QuickJarsXmlParser handler = new QuickJarsXmlParser();
        if (!contextXml.exists()) {
            return handler;
        }

        try {
            FACTORY.newSAXParser().parse(contextXml, handler);
        } catch (final Exception e) {
            // no-op: not parseable so ignoring
        }
        return handler;
    }
}
