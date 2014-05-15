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

import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.loader.Files;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.xbean.finder.ClassLoaders;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class QuickContextXmlParser extends DefaultHandler {
    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
    static {
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    private String virtualClasspath = "";
    private Collection<URL> urls = null;

    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes) throws SAXException {
        if ("Loader".equalsIgnoreCase(localName)) {
            final String className = attributes.getValue("className");
            if (className != null) {
                if ("org.apache.catalina.loader.VirtualWebappLoader".equals(className)
                        || "org.apache.tomee.catalina.ProvisioningWebappLoader".equals(className)) {
                    virtualClasspath = attributes.getValue("virtualClasspath");
                }
            }

            final String loaderClass = attributes.getValue("loaderClass");
            if (loaderClass != null) {
                try {
                    final ClassLoader parent = ParentClassLoaderFinder.Helper.get();

                    // create a fake loader
                    final Object loader = parent.loadClass("org.apache.catalina.loader.WebappLoader").newInstance();
                    Reflections.set(loader, "loaderClass", loaderClass);
                    Reflections.set(loader, "parentClassLoader", parent);

                    // get the loader
                    final ClassLoader instance = ClassLoader.class.cast(Reflections.invokeByReflection(loader, "createClassLoader", new Class<?>[0], null));
                    if (instance != null) {
                        urls = ClassLoaders.findUrls(instance);
                    }
                } catch (final Exception e) {
                    // no-op
                }
            }
        }
    }

    public Collection<URL> getAdditionalURLs() {
        final Set<URL> set = new LinkedHashSet<URL>();

        if (virtualClasspath != null) {
            final StringTokenizer tkn = new StringTokenizer(virtualClasspath, ";");
            while (tkn.hasMoreTokens()) {
                final String token = tkn.nextToken().trim();
                if (token.isEmpty()) {
                    continue;
                }

                set.addAll(Files.listJars(token));
            }
        }

        if (urls != null) {
            set.addAll(urls);
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
