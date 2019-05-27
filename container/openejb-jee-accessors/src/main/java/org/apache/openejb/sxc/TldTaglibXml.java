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
package org.apache.openejb.sxc;

import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.TldTaglib$JAXB;
import org.apache.openejb.loader.IO;
import org.metatype.sxc.util.XoXMLStreamReaderImpl;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class TldTaglibXml {

    public static TldTaglib unmarshal(final InputStream inputStream) throws Exception {
        return Sxc.unmarshalJavaee(new TldTaglib$JAXB(), inputStream);
    }

    public static TldTaglib unmarshal(final URL url) throws Exception {
        final InputStream inputStream = IO.read(url);
        try {
            final XMLStreamReader filter = new TaglibNamespaceFilter(Sxc.prepareReader(inputStream));
            return Sxc.unmarhsal(new TldTaglib$JAXB(), new XoXMLStreamReaderImpl(filter));
        } finally {
            IO.close(inputStream);
        }
    }

    public static void marshal(final TldTaglib taglib, final OutputStream outputStream) throws Exception {
        Sxc.marshal(new TldTaglib$JAXB(), taglib, new StreamResult(outputStream));
    }

    public static class TaglibNamespaceFilter extends StreamReaderDelegate {
        public TaglibNamespaceFilter(final XMLStreamReader xmlStreamReader) {
            super(xmlStreamReader);
        }

        @Override
        public String getLocalName() {
            return fixLocalName(super.getLocalName());
        }

        protected String fixLocalName(final String localName) {
            switch (localName) {
                case "tlibversion":
                    return "tlib-version";
                case "jspversion":
                    return "jsp-version";
                case "shortname":
                    return "short-name";
                case "tagclass":
                    return "tag-class";
                case "teiclass":
                    return "tei-class";
                case "bodycontent":
                    return "body-content";
                case "info":
                    return "description";
            }
            return localName;
        }
    }
}
