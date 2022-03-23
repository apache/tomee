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
package org.apache.openejb.tck.cdi.embedded;

import org.apache.jasper.compiler.TldCache;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.openejb.web.LightweightWebAppBuilder;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomee.jasper.TomEETldScanner;
import org.xml.sax.SAXException;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.util.Map;

public class TckTlds extends TldCache {
    private static final TomEETldScanner SCANNER = new TomEETldScanner(null, false, false, false) {
        @Override
        protected void scanJspConfig() throws IOException, SAXException {
            // no-op
        }

        @Override
        protected void scanResourcePaths(final String startPath) throws IOException, SAXException {
            // no-op
        }

        @Override
        public void scanJars() {
            // no-op
        }
    };
    static {
        try {
            SCANNER.scan();
        } catch (final Exception e) {
            // no-op
        }
    }

    public TckTlds(final ServletContext context) {
        super(context,
                (Map<String, TldResourcePath>) Reflections.get(SCANNER, "uriTldResourcePathMap"),
                (Map<TldResourcePath, TaglibXml>) Reflections.get(SCANNER, "tldResourcePathTaglibXmlMap"));
    }

    public static class Observer {
        public void setup(@Observes final LightweightWebAppBuilder.EmbeddedServletContextCreated created) {
            created.getContext().setAttribute(TldCache.class.getName(), new TckTlds(created.getContext()));
        }
    }
}
