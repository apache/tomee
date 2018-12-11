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

import org.apache.openejb.util.JavaSecurityManagers;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class QuickServerXmlParser extends DefaultHandler {
    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
    static {
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    private static final String STOP_KEY = "STOP";
    private static final String HTTP_KEY = "HTTP";
    private static final String SECURED_SUFFIX = "S";
    private static final String AJP_KEY = "AJP";
    private static final String HOST_KEY = "host";
    private static final String APP_BASE_KEY = "app-base";
    private static final String DEFAULT_CONNECTOR_KEY = HTTP_KEY;
    private static final String KEYSTORE_KEY = "keystoreFile";

    public static final String DEFAULT_HTTP_PORT = "8080";
    public static final String DEFAULT_HTTPS_PORT = "8443";
    public static final String DEFAULT_STOP_PORT = "8005";
    public static final String DEFAULT_AJP_PORT = "8009";
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_APP_BASE = "webapps";
    public static final String DEFAULT_KEYSTORE = new File(JavaSecurityManagers.getSystemProperty("user.home"), ".keystore").getAbsolutePath();

    private final Map<String, String> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public QuickServerXmlParser() { // ensure defaults are present
        values.put(STOP_KEY, DEFAULT_STOP_PORT);
        values.put(HTTP_KEY, DEFAULT_HTTP_PORT);
        values.put(AJP_KEY, DEFAULT_AJP_PORT);
        values.put(HOST_KEY, DEFAULT_HOST);
        values.put(APP_BASE_KEY, DEFAULT_APP_BASE);
        values.put(KEYSTORE_KEY, DEFAULT_KEYSTORE);
    }

    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes) throws SAXException {
        if ("Server".equalsIgnoreCase(localName)) {
            final String port = attributes.getValue("port");
            if (port != null) {
                values.put(STOP_KEY, port);
            } else {
                values.put(STOP_KEY, DEFAULT_STOP_PORT);
            }
        } else if ("Connector".equalsIgnoreCase(localName)) {
            String protocol = attributes.getValue("protocol");
            if (protocol == null) {
                protocol = DEFAULT_CONNECTOR_KEY;
            } else if (protocol.contains("/")) {
                protocol = protocol.substring(0, protocol.indexOf('/'));
            }
            final String port = attributes.getValue("port");
            final String ssl = attributes.getValue("secure");

            if (ssl == null || "false".equalsIgnoreCase(ssl)) {
                values.put(protocol.toUpperCase(), port);
            } else {
                values.put(protocol.toUpperCase() + SECURED_SUFFIX, port);
            }

            final String keystore = attributes.getValue("keystoreFile");
            if (null != keystore) {
                values.put(KEYSTORE_KEY, keystore);
            }
        } else if ("Host".equalsIgnoreCase(localName)) {
            final String host = attributes.getValue("name");
            if (host != null) {
                values.put(HOST_KEY, host);
            }

            final String appBase = attributes.getValue("appBase");
            if (appBase != null) {
                values.put(APP_BASE_KEY, appBase);
            }
        }
    }

    public static QuickServerXmlParser parse(final File serverXml) {
        final QuickServerXmlParser handler = new QuickServerXmlParser();
        try {
            final SAXParser parser = FACTORY.newSAXParser();
            parser.parse(serverXml, handler);
        } catch (final Exception e) {
            // no-op: using defaults
        }
        return handler;
    }

    public static QuickServerXmlParser parse(final String serverXmlContents) {
        final QuickServerXmlParser handler = new QuickServerXmlParser();
        try {
            final SAXParser parser = FACTORY.newSAXParser();
            parser.parse(new ByteArrayInputStream(serverXmlContents.getBytes()), handler);
        } catch (final Exception e) {
            // no-op: using defaults
        }
        return handler;
    }

    public String http() {
        return value(HTTP_KEY, DEFAULT_HTTP_PORT);
    }

    public String https() { // enough common to be exposed as method
        return securedValue(HTTP_KEY, DEFAULT_HTTPS_PORT);
    }

    public String ajp() {
        return value(AJP_KEY, DEFAULT_AJP_PORT);
    }

    public String stop() {
        return value(STOP_KEY, DEFAULT_STOP_PORT);
    }

    public String appBase() {
        return value(APP_BASE_KEY, DEFAULT_APP_BASE);
    }

    public String host() {
        return value(HOST_KEY, DEFAULT_HOST);
    }

    public String keystore() {
        return value(KEYSTORE_KEY, DEFAULT_KEYSTORE);
    }

    public String value(final String key, final String defaultValue) {
        final String val = values.get(key);
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    public String securedValue(final String key, final String defaultValue) {
        return value(key + SECURED_SUFFIX, defaultValue);
    }

    @Override
    public String toString() {
        return "QuickServerXmlParser: " + values;
    }
}
