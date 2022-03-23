/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Locale;

import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @version $Rev$ $Date$
 */
public class JaxbJavaee {
    public static final ThreadLocal<Set<String>> currentPublicId = new ThreadLocal<Set<String>>();

    private static final Map<Class<?>, JAXBContext> jaxbContexts = new HashMap<Class<?>, JAXBContext>();

    public static <T> String marshal(final Class<T> type, final Object object) throws JAXBException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        marshal(type, object, baos);

        return new String(baos.toByteArray());
    }

    public static <T> void marshal(final Class<T> type, final Object object, final OutputStream out) throws JAXBException {
        final JAXBContext ctx2 = JaxbJavaee.getContext(type);
        final Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }

    public static <T> JAXBContext getContext(final Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = jaxbContexts.get(type);
        if (jaxbContext == null) {
            jaxbContext = JAXBContextFactory.newInstance(type);
            jaxbContexts.put(type, jaxbContext);
        }
        return jaxbContext;
    }

    private static <T> Object unmarshalJavaee(final Class<T> type, final InputStream in, boolean filter) throws ParserConfigurationException, SAXException, JAXBException {

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);

        final SAXParser parser = factory.newSAXParser();

        final JAXBContext ctx = JaxbJavaee.getContext(type);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(final ValidationEvent validationEvent) {
                final String verbose = System.getProperty("openejb.validation.output.level");
                if (verbose != null && "VERBOSE".equals(verbose.toUpperCase(Locale.ENGLISH))) {
                    System.err.println(validationEvent);
                }
                return false;
            }
        });

        SAXSource source = null;
        if (filter) {
            final JavaeeNamespaceFilter xmlFilter = new JavaeeNamespaceFilter(parser.getXMLReader());
            xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());
            // unmarshall
            source = new SAXSource(xmlFilter, new InputSource(in));
        } else {
            source = new SAXSource(new InputSource(in));
        }


        currentPublicId.set(new TreeSet<String>());
        try {
            final JAXBElement<T> element = unmarshaller.unmarshal(source, type);
            return element.getValue();
        } finally {
            currentPublicId.set(null);
        }
    }

    /**
     *
     * It unmarshals, but not using the {@link JavaeeNamespaceFilter}
     *
     * @param type Class of object to be read in
     * @param in   input stream to read
     * @param <T>  class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException                 if there is an xml problem
     * @throws JAXBException                if the xml cannot be marshalled into a T.
     */
    public static <T> Object unmarshal(final Class<T> type, final InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        return unmarshalJavaee(type, in, false);
    }

    /**
     * Convert the namespaceURI in the input to the javaee URI, do not validate the xml, and read in a T.
     *
     * @param type Class of object to be read in
     * @param in   input stream to read
     * @param <T>  class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException                 if there is an xml problem
     * @throws JAXBException                if the xml cannot be marshalled into a T.
     */
    public static <T> Object unmarshalJavaee(final Class<T> type, final InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        return unmarshalJavaee(type, in, true);
    }

    /**
     * Read in a T from the input stream.
     *
     * @param type     Class of object to be read in
     * @param in       input stream to read
     * @param validate whether to validate the input.
     * @param <T>      class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException                 if there is an xml problem
     * @throws JAXBException                if the xml cannot be marshalled into a T.
     */
    public static <T> Object unmarshal(final Class<T> type, final InputStream in, final boolean validate) throws ParserConfigurationException, SAXException, JAXBException {
        final InputSource inputSource = new InputSource(in);

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(validate);
        final SAXParser parser = factory.newSAXParser();

        final JAXBContext ctx = JaxbJavaee.getContext(type);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(final ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });

        final JaxbJavaee.NoSourceFilter xmlFilter = new JaxbJavaee.NoSourceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        final SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source);
        } finally {
            currentPublicId.set(null);
        }
    }

    /**
     * Convert the namespaceURI in the input to the taglib URI, do not validate the xml, and read in a T.
     *
     * @param type Class of object to be read in
     * @param in   input stream to read
     * @param <T>  class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException                 if there is an xml problem
     * @throws JAXBException                if the xml cannot be marshalled into a T.
     */
    public static <T> Object unmarshalTaglib(final Class<T> type, final InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        final InputSource inputSource = new InputSource(in);

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        final SAXParser parser = factory.newSAXParser();

        final JAXBContext ctx = JaxbJavaee.getContext(type);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(final ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });


        final JaxbJavaee.TaglibNamespaceFilter xmlFilter = new JaxbJavaee.TaglibNamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        final SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source);
        } finally {
            currentPublicId.set(null);
        }
    }

    /**
     * @param type Class of object to be read in
     * @param in   input stream to read
     * @param <T>  class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException                 if there is an xml problem
     * @throws JAXBException                if the xml cannot be marshalled into a T.
     */
    public static <T> Object unmarshalHandlerChains(final Class<T> type, final InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        final InputSource inputSource = new InputSource(in);

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        final SAXParser parser = factory.newSAXParser();

        final JAXBContext ctx = JaxbJavaee.getContext(type);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(final ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });

        final JaxbJavaee.HandlerChainsNamespaceFilter xmlFilter = new JaxbJavaee.HandlerChainsNamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());
        final HandlerChainsStringQNameAdapter adapter = new HandlerChainsStringQNameAdapter();
        adapter.setHandlerChainsNamespaceFilter(xmlFilter);
        unmarshaller.setAdapter(HandlerChainsStringQNameAdapter.class, adapter);

        final SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source);
        } finally {
            currentPublicId.set(null);
        }
    }

    public static class JavaeeNamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        private boolean ignore = false;

        public JavaeeNamespaceFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            final Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qname, final Attributes atts) throws SAXException {
            if (ignore) {
                return;
            }

            if (uri != null && (uri.startsWith("http://jboss.org") || uri.startsWith("urn:java:"))) { // ignore it to be able to read beans.xml with weld config for instances
                ignore = true;
            } else {
                super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
            }
        }

        @Override
        public void characters(final char ch[], final int start, final int length) throws SAXException {
            if (!ignore) {
                super.characters(ch, start, length);
            }
        }

        @Override
        public void ignorableWhitespace(final char ch[], final int start, final int length) throws SAXException {
            if (!ignore) {
                super.ignorableWhitespace(ch, start, length);
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (uri != null && (uri.startsWith("http://jboss.org") || uri.startsWith("urn:java:"))) { // ignore it
                ignore = false;
            } else if (!ignore) {
                super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
            }
        }
    }

    public static class NoSourceFilter extends XMLFilterImpl {
        protected static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NoSourceFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        protected String eeUri(final String uri) {
            // if ee 7 then switch back on ee 6 to not break compatibility - to rework surely when we'll be fully ee 7
            return "http://xmlns.jcp.org/xml/ns/javaee".equals(uri) ? "http://java.sun.com/xml/ns/javaee" : uri;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
            super.startElement(eeUri(uri), localName, qName, atts);
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            super.endElement(eeUri(uri), localName, qName);
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            final Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }
    }

    public static class HandlerChainsNamespaceFilter extends XMLFilterImpl {

        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        private final Stack<Map.Entry<String, String>> effectiveNamespaces = new Stack<Map.Entry<String, String>>();

        public HandlerChainsNamespaceFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            final Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qname, final Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }

        @Override
        public void endPrefixMapping(final String prefix) throws SAXException {
            effectiveNamespaces.pop();
            super.endPrefixMapping(prefix);
        }

        @Override
        public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
            effectiveNamespaces.push(new AbstractMap.SimpleEntry<String, String>(prefix, uri));
            super.startPrefixMapping(prefix, uri);
        }

        public String lookupNamespaceURI(final String prefix) {
            for (int index = effectiveNamespaces.size() - 1; index >= 0; index--) {
                final Map.Entry<String, String> entry = effectiveNamespaces.get(index);
                if (entry.getKey().equals(prefix)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

    public static class TaglibNamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public TaglibNamespaceFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            final Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(final String uri, String localName, final String qname, final Attributes atts) throws SAXException {
            localName = fixLocalName(localName);
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }

        private String fixLocalName(String localName) {
            switch (localName) {
                case "tlibversion":
                    localName = "tlib-version";
                    break;
                case "jspversion":
                    localName = "jsp-version";
                    break;
                case "shortname":
                    localName = "short-name";
                    break;
                case "tagclass":
                    localName = "tag-class";
                    break;
                case "teiclass":
                    localName = "tei-class";
                    break;
                case "bodycontent":
                    localName = "body-content";
                    break;
                case "info":
                    localName = "description";
                    break;
            }
            return localName;
        }

        @Override
        public void endElement(final String uri, String localName, final String qName) throws SAXException {
            localName = fixLocalName(localName);
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }
    }

    public static class Javaee6SchemaFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public Javaee6SchemaFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            final Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qname, final Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, fixVersion(localName, atts));

        }

        private Attributes fixVersion(final String localName, final Attributes atts) {
            if (localName.equals("web-app") && atts.getIndex("version") != -1 && !atts.getValue(atts.getIndex("version")).equals("3.0")) {
                final AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "3.0");
                return newAtts;
            }

            if (localName.equals("ejb-jar") && atts.getIndex("version") != -1 && !atts.getValue(atts.getIndex("version")).equals("3.1")) {
                final AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "3.1");
                return newAtts;
            }

            if (localName.equals("application") && atts.getIndex("version") != -1 && !atts.getValue(atts.getIndex("version")).equals("6")) {
                final AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "6");
                return newAtts;
            }

            if (localName.equals("application-client") && atts.getIndex("version") != -1 && !atts.getValue(atts.getIndex("version")).equals("6")) {
                final AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "6");
                return newAtts;
            }

            if (localName.equals("connector") && atts.getIndex("version") != -1 && !atts.getValue(atts.getIndex("version")).equals("1.6")) {
                final AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "1.6");
                return newAtts;
            }

            return atts;
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }
    }


    /**
     * validate the inputStream, which should be a Java EE standard deployment descriptor against its schema type
     * Note, this method will use the new Java EE 6 schema to validate the old descriptors after changing their namespace and version.
     *
     * @param type
     * @param in
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void validateJavaee(final JavaeeSchema type, final InputStream in) throws ParserConfigurationException, SAXException, IOException {
        final URL javaeeSchemaURL = resolveJavaeeSchemaURL(type);
        if (javaeeSchemaURL == null) {
            throw new IllegalArgumentException("Can not find the xsd file against type:" + type);
        }

        final URL xmlSchemaURL = JaxbJavaee.getSchemaURL("xml.xsd");
        if (xmlSchemaURL == null) {
            throw new IllegalArgumentException("Can not find the xml.xsd file");
        }

        // get the parser
        final SAXParserFactory parserfactory = SAXParserFactory.newInstance();
        parserfactory.setNamespaceAware(true);
        parserfactory.setValidating(false);
        final SAXParser parser = parserfactory.newSAXParser();

        // get the xml filter
        final Javaee6SchemaFilter xmlFilter = new Javaee6SchemaFilter(parser.getXMLReader());

        // get the source
        final SAXSource sourceForValidate = new SAXSource(xmlFilter, new InputSource(in));

        // get the schema
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        final JaxbJavaeeSchemaResourceResolver resourceResolver = new JaxbJavaeeSchemaResourceResolver();
        schemaFactory.setResourceResolver(resourceResolver);

        final Schema schema = schemaFactory.newSchema(
                new Source[]{
                        new StreamSource(xmlSchemaURL.openStream()),
                        new StreamSource(javaeeSchemaURL.openStream())
                });

        // validate
        schema.newValidator().validate(sourceForValidate);
    }

    private static URL getSchemaURL(final String xsdFileName) {
        return JaxbJavaee.class.getClassLoader().getResource("/META-INF/schema/" + xsdFileName);
    }

    private static URL resolveJavaeeSchemaURL(final JavaeeSchema type) {
        URL schemaURL = null;
        if (type.equals(JavaeeSchema.WEB_APP_3_0)) {
            //will include web-common.xsd, jsp_2_2.xsd, javaee_6.xsd and javaee_web_services_client_1_3.xsd
            schemaURL = JaxbJavaee.getSchemaURL(JavaeeSchema.WEB_APP_3_0.getSchemaFileName());
        } else if (type.equals(JavaeeSchema.EJB_JAR_3_1)) {
            schemaURL = JaxbJavaee.getSchemaURL(JavaeeSchema.EJB_JAR_3_1.getSchemaFileName());
        } else if (type.equals(JavaeeSchema.APPLICATION_6)) {
            schemaURL = JaxbJavaee.getSchemaURL(JavaeeSchema.APPLICATION_6.getSchemaFileName());
        } else if (type.equals(JavaeeSchema.APPLICATION_CLIENT_6)) {
            schemaURL = JaxbJavaee.getSchemaURL(JavaeeSchema.APPLICATION_CLIENT_6.getSchemaFileName());
        } else if (type.equals(JavaeeSchema.CONNECTOR_1_6)) {
            schemaURL = JaxbJavaee.getSchemaURL(JavaeeSchema.CONNECTOR_1_6.getSchemaFileName());
        }

        return schemaURL;
    }

    static class JaxbJavaeeSchemaResourceResolver implements LSResourceResolver {

        /**
         * Allow the application to resolve external resources.
         */
        public LSInput resolveResource(final String type, final String namespaceURI, final String publicId, final String systemId, final String baseURI) {
//            System.out.println("\n>> Resolving "  +  "\n"   
//                              + "TYPE: "  + type +  "\n"   
//                              + "NAMESPACE_URI: "  + namespaceURI +  "\n"    
//                              + "PUBLIC_ID: "  + publicId +  "\n"   
//                              + "SYSTEM_ID: "  + systemId +  "\n"   
//                              + "BASE_URI: "  + baseURI +  "\n" );  

            final LSInput lsInput = new LSInputImpl();

            // In all Java EE schema xsd files, the <xsd:include schemaLocation=../> always reference to a relative path. 
            // so the systemId here will be the xsd file name.
            final URL schemaURL = JaxbJavaee.getSchemaURL(systemId);

            InputStream is = null;
            if (schemaURL != null) {
                try {
                    is = schemaURL.openStream();
                } catch (final IOException e) {
                    //should not happen
                    throw new RuntimeException(e);
                }
            }

            lsInput.setSystemId(systemId);
            lsInput.setByteStream(is);

            return lsInput;
        }


        /**
         * Represents an input source for data
         */
        class LSInputImpl implements LSInput {

            private String publicId;
            private String systemId;
            private String baseURI;
            private InputStream byteStream;
            private Reader charStream;
            private String stringData;
            private String encoding;
            private boolean certifiedText;

            public LSInputImpl() {
            }

            public LSInputImpl(final String publicId, final String systemId, final InputStream byteStream) {
                this.publicId = publicId;
                this.systemId = systemId;
                this.byteStream = byteStream;
            }

            public String getBaseURI() {
                return baseURI;
            }

            public InputStream getByteStream() {
                return byteStream;
            }

            public boolean getCertifiedText() {
                return certifiedText;
            }

            public Reader getCharacterStream() {
                return charStream;
            }

            public String getEncoding() {
                return encoding;
            }

            public String getPublicId() {
                return publicId;
            }

            public String getStringData() {
                return stringData;
            }

            public String getSystemId() {
                return systemId;
            }

            public void setBaseURI(final String baseURI) {
                this.baseURI = baseURI;
            }

            public void setByteStream(final InputStream byteStream) {
                this.byteStream = byteStream;
            }

            public void setCertifiedText(final boolean certifiedText) {
                this.certifiedText = certifiedText;
            }

            public void setCharacterStream(final Reader characterStream) {
                this.charStream = characterStream;
            }

            public void setEncoding(final String encoding) {
                this.encoding = encoding;
            }

            public void setPublicId(final String publicId) {
                this.publicId = publicId;
            }

            public void setStringData(final String stringData) {
                this.stringData = stringData;
            }

            public void setSystemId(final String systemId) {
                this.systemId = systemId;
            }

        }

    }

}

