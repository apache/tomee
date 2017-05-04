/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.maven.util;

import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public final class XmlFormatter {
    public static String format(final String in) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final InputSource is = new InputSource(new StringReader(in));
            final Document document = db.parse(is);

            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = DOMImplementationLS.class.cast(registry.getDOMImplementation("XML 3.0 LS 3.0"));
            if (impl == null) {
                return in;
            }

            final LSSerializer serializer = impl.createLSSerializer();
            if (serializer.getDomConfig().canSetParameter("format-pretty-print", Boolean.TRUE)) {
                serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
                final LSOutput lsOutput = impl.createLSOutput();
                lsOutput.setEncoding("UTF-8");
                final StringWriter stringWriter = new StringWriter();
                lsOutput.setCharacterStream(stringWriter);
                serializer.write(document, lsOutput);
                return stringWriter.toString().replace("\"UTF-8\"?><", "\"UTF-8\"?>\n<");
            }

            return in;
        } catch (final Throwable t) {
            return in; // just to be more sexy so ignore it and use ugly xml
        }
    }

    private XmlFormatter() {
        // no-op
    }
}
