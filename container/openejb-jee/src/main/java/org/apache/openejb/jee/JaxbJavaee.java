/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
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

    private static Map<Class<?>,JAXBContext> jaxbContexts = new HashMap<Class<?>,JAXBContext>();

    public static <T>String marshal(Class<T> type, Object object) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        marshal(type, object, baos);

        return new String(baos.toByteArray());
    }

    public static <T>void marshal(Class<T> type, Object object, OutputStream out) throws JAXBException {
        JAXBContext ctx2 = JaxbJavaee.getContext(type);
        Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }

    private static <T>JAXBContext getContext(Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = jaxbContexts.get(type);
        if (jaxbContext == null) {
            jaxbContext = JAXBContextFactory.newInstance(type);
            jaxbContexts.put(type, jaxbContext);
        }
        return jaxbContext;
    }

    /**
     * Convert the namespaceURI in the input to the javaee URI, do not validate the xml, and read in a T.
     *
     * @param type Class of object to be read in
     * @param in input stream to read
     * @param <T> class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException if there is an xml problem
     * @throws JAXBException if the xml cannot be marshalled into a T.
     */
    public static <T>Object unmarshalJavaee(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = JaxbJavaee.getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                String verbose = System.getProperty("openejb.validation.output.level");
                if (verbose != null && "VERBOSE".equals(verbose.toUpperCase())) {
                    System.err.println(validationEvent);
                }
                return false;
            }
        });

        JavaeeNamespaceFilter xmlFilter = new JavaeeNamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());
        
        // unmarshall
        SAXSource source = new SAXSource(xmlFilter, new InputSource(in));
        
        currentPublicId.set(new TreeSet<String>());
        try {
            JAXBElement<T> element = unmarshaller.unmarshal(source, type);
            return element.getValue();
        } finally {
            currentPublicId.set(null);
        }
    }

    /**
     * Read in a T from the input stream.
     *
     * @param type Class of object to be read in
     * @param in input stream to read
     * @param validate whether to validate the input.
     * @param <T> class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException if there is an xml problem
     * @throws JAXBException if the xml cannot be marshalled into a T.
     */
    public static <T>Object unmarshal(Class<T> type, InputStream in, boolean validate) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(validate);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = JaxbJavaee.getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });

        JaxbJavaee.NoSourceFilter xmlFilter = new JaxbJavaee.NoSourceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);

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
     * @param in input stream to read
     * @param <T> class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException if there is an xml problem
     * @throws JAXBException if the xml cannot be marshalled into a T.
     */
    public static <T>Object unmarshalTaglib(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = JaxbJavaee.getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });


        JaxbJavaee.TaglibNamespaceFilter xmlFilter = new JaxbJavaee.TaglibNamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source);
        } finally {
            currentPublicId.set(null);
        }
    }

    /**   
     *
     * @param type Class of object to be read in
     * @param in input stream to read
     * @param <T> class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException if there is an xml problem
     * @throws JAXBException if the xml cannot be marshalled into a T.
     */
    public static <T>Object unmarshalHandlerChains(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = JaxbJavaee.getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });

        JaxbJavaee.HandlerChainsNamespaceFilter xmlFilter = new JaxbJavaee.HandlerChainsNamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());
        HandlerChainsStringQNameAdapter adapter = new HandlerChainsStringQNameAdapter();
        adapter.setHandlerChainsNamespaceFilter(xmlFilter);
        unmarshaller.setAdapter(HandlerChainsStringQNameAdapter.class, adapter);

        SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source);
        } finally {
            currentPublicId.set(null);
        }
    }

    public static class JavaeeNamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public JavaeeNamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }
    }
    
    public static class NoSourceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NoSourceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }
    }

    public static class HandlerChainsNamespaceFilter extends XMLFilterImpl {

        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        private Stack<Map.Entry<String, String>> effectiveNamespaces = new Stack<Map.Entry<String, String>>();

        public HandlerChainsNamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            effectiveNamespaces.pop();
            super.endPrefixMapping(prefix);
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            effectiveNamespaces.push(new AbstractMap.SimpleEntry<String, String>(prefix, uri));
            super.startPrefixMapping(prefix, uri);
        }

        public String lookupNamespaceURI(String prefix) {
            for (int index = effectiveNamespaces.size() - 1; index >= 0; index--) {
                Map.Entry<String, String> entry = effectiveNamespaces.get(index);
                if (entry.getKey().equals(prefix)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

    public static class TaglibNamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public TaglibNamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            localName = fixLocalName(localName);
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }

        private String fixLocalName(String localName) {
            if (localName.equals("tlibversion")) {
                localName = "tlib-version";
            } else if (localName.equals("jspversion")) {
                localName = "jsp-version";
            } else if (localName.equals("shortname")) {
                localName = "short-name";
            } else if (localName.equals("tagclass")) {
                localName = "tag-class";
            } else if (localName.equals("teiclass")) {
                localName = "tei-class";
            } else if (localName.equals("bodycontent")) {
                localName = "body-content";
            } else if (localName.equals("jspversion")) {
                localName = "jsp-version";
            } else if (localName.equals("info")) {
                localName = "description";
            }
            return localName;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            localName = fixLocalName(localName);
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }
    }

    public static class Javaee6SchemaFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public Javaee6SchemaFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, fixVersion(localName, atts));
            
        }

        private Attributes fixVersion(String localName, Attributes atts){
            if (localName.equals("web-app") && atts.getIndex("version")!=-1 && !atts.getValue(atts.getIndex("version")).equals("3.0")) {
                AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "3.0");
                return newAtts;
            } 
            
            if (localName.equals("ejb-jar") && atts.getIndex("version")!=-1 && !atts.getValue(atts.getIndex("version")).equals("3.1")){
                AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "3.1");
                return newAtts;
            } 
            
            if (localName.equals("application") && atts.getIndex("version")!=-1 && !atts.getValue(atts.getIndex("version")).equals("6")){
                AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "6");
                return newAtts;
            } 
            
            if (localName.equals("application-client") && atts.getIndex("version")!=-1 && !atts.getValue(atts.getIndex("version")).equals("6")){
                AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "6");
                return newAtts;
            } 
            
            if (localName.equals("connector") && atts.getIndex("version")!=-1 && !atts.getValue(atts.getIndex("version")).equals("1.6")){
                AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "1.6");
                return newAtts;
            } 
            
            return atts;
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }
    }
    

    /**
     * validate the inputStream, which should be a Java EE standard deployment descriptor against its schema type
     * Note, this method will use the new Java EE 6 schema to validate the old descriptors after changing their namespace and version.
     * @param type
     * @param in
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void validateJavaee(JavaeeSchema type, InputStream in) throws ParserConfigurationException, SAXException, IOException {
        URL javaeeSchemaURL = resolveJavaeeSchemaURL(type);
        if (javaeeSchemaURL == null) {
            throw new IllegalArgumentException("Can not find the xsd file against type:" + type);
        }
        
        URL xmlSchemaURL = JaxbJavaee.getSchemaURL("xml.xsd");
        if (xmlSchemaURL == null) {
            throw new IllegalArgumentException("Can not find the xml.xsd file");
        }
        
        // get the parser
        SAXParserFactory parserfactory = SAXParserFactory.newInstance();
        parserfactory.setNamespaceAware(true);
        parserfactory.setValidating(false);
        SAXParser parser = parserfactory.newSAXParser();
        
        // get the xml filter
        Javaee6SchemaFilter xmlFilter = new Javaee6SchemaFilter(parser.getXMLReader());
        
        // get the source
        SAXSource sourceForValidate = new SAXSource(xmlFilter, new InputSource(in));
        
        // get the schema
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        JaxbJavaeeSchemaResourceResolver resourceResolver = new JaxbJavaeeSchemaResourceResolver();  
        schemaFactory.setResourceResolver(resourceResolver);  
        
        Schema schema = schemaFactory.newSchema(
                new Source[] {
                        new StreamSource(xmlSchemaURL.openStream()),
                        new StreamSource(javaeeSchemaURL.openStream())
                });

        // validate
        schema.newValidator().validate(sourceForValidate);
    }
    
    private static URL getSchemaURL(String xsdFileName){
        return JaxbJavaee.class.getClassLoader().getResource("/META-INF/schema/" + xsdFileName);
    }
    
    private static URL resolveJavaeeSchemaURL(JavaeeSchema type){
        URL schemaURL = null;
        if (type.equals(JavaeeSchema.WEB_APP_3_0)){
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
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {  
//            System.out.println("\n>> Resolving "  +  "\n"   
//                              + "TYPE: "  + type +  "\n"   
//                              + "NAMESPACE_URI: "  + namespaceURI +  "\n"    
//                              + "PUBLIC_ID: "  + publicId +  "\n"   
//                              + "SYSTEM_ID: "  + systemId +  "\n"   
//                              + "BASE_URI: "  + baseURI +  "\n" );  
              
            LSInput lsInput = new LSInputImpl();  
              
            // In all Java EE schema xsd files, the <xsd:include schemaLocation=../> always reference to a relative path. 
            // so the systemId here will be the xsd file name.
            URL schemaURL = JaxbJavaee.getSchemaURL(systemId);

            InputStream is = null;
            if (schemaURL!=null) {
                try {
                    is = schemaURL.openStream();
                } catch (IOException e) {
                    //should not happen
                    throw new RuntimeException(e);
                }
            }
                        
            lsInput.setSystemId(systemId);  
            lsInput.setByteStream(is);  
          
            return  lsInput;  
        }  
        
        
        
        /**  
         *   
         * Represents an input source for data  
         *  
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

            public LSInputImpl(String publicId, String systemId, InputStream byteStream) {
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

            public void setBaseURI(String baseURI) {
                this.baseURI = baseURI;
            }

            public void setByteStream(InputStream byteStream) {
                this.byteStream = byteStream;
            }

            public void setCertifiedText(boolean certifiedText) {
                this.certifiedText = certifiedText;
            }

            public void setCharacterStream(Reader characterStream) {
                this.charStream = characterStream;
            }

            public void setEncoding(String encoding) {
                this.encoding = encoding;
            }

            public void setPublicId(String publicId) {
                this.publicId = publicId;
            }

            public void setStringData(String stringData) {
                this.stringData = stringData;
            }

            public void setSystemId(String systemId) {
                this.systemId = systemId;
            }  
              
        }  
      
    }  

}

