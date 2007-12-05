/**
 *
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
package org.apache.openejb.server.axis.assembler;

import com.ibm.wsdl.extensions.PopulatedExtensionRegistry;
import com.ibm.wsdl.extensions.schema.SchemaConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class XmlBeansSchemaLoader {
    private static final Log log = LogFactory.getLog(XmlBeansSchemaInfoBuilder.class);
    private static final SchemaTypeSystem basicTypeSystem;

    private static XmlOptions createXmlOptions(Collection errors) {
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        return options;
    }

    static {
        InputStream is = XmlBeansSchemaInfoBuilder.class.getClassLoader().getResourceAsStream("META-INF/schema/soap_encoding_1_1.xsd");
        if (is == null) {
            throw new RuntimeException("Could not locate soap encoding schema");
        }
        ArrayList errors = new ArrayList();
        XmlOptions xmlOptions = createXmlOptions(errors);
        try {
            SchemaDocument parsed = SchemaDocument.Factory.parse(is, xmlOptions);
            if (errors.size() != 0) {
                throw new XmlException(errors.toArray().toString());
            }

            basicTypeSystem = XmlBeans.compileXsd(new XmlObject[]{parsed}, XmlBeans.getBuiltinTypeSystem(), xmlOptions);
            if (errors.size() > 0) {
                throw new RuntimeException("Could not compile schema type system: errors: " + errors);
            }
        } catch (XmlException e) {
            throw new RuntimeException("Could not compile schema type system", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not compile schema type system", e);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    private final URI wsdlUri;
    private final JarFile moduleFile;
    private final LinkedList<URI> uris = new LinkedList<URI>();


    public XmlBeansSchemaLoader(URI wsdlUri, JarFile moduleFile) {
        this.wsdlUri = wsdlUri;
        this.moduleFile = moduleFile;
        uris.addFirst(wsdlUri);
    }

    public SchemaTypeSystem loadSchema() throws OpenEJBException {
        Definition definition = readWsdl(wsdlUri);

        List<XmlObject> schemaList = new ArrayList<XmlObject>();
        addImportsFromDefinition(definition, schemaList);
//        System.out.println("Schemas: " + schemaList);
        Collection<XmlError> errors = new ArrayList<XmlError>();
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setErrorListener(errors);
        xmlOptions.setEntityResolver(new JarEntityResolver());
        XmlObject[] schemas = schemaList.toArray(new XmlObject[schemaList.size()]);
        try {
            SchemaTypeSystem schemaTypeSystem = XmlBeans.compileXsd(schemas, basicTypeSystem, xmlOptions);
            if (errors.size() > 0) {
                boolean wasError = false;
                for (XmlError xmlError : errors) {
                    if (xmlError.getSeverity() == XmlError.SEVERITY_ERROR) {
                        log.error(xmlError);
                        wasError = true;
                    } else if (xmlError.getSeverity() == XmlError.SEVERITY_WARNING) {
                        log.warn(xmlError);
                    } else if (xmlError.getSeverity() == XmlError.SEVERITY_INFO) {
                        log.debug(xmlError);
                    }
                }
                if (wasError) {
                    throw new OpenEJBException("Could not compile schema type system, see log for errors");
                }
            }
            return schemaTypeSystem;
        } catch (XmlException e) {
            throw new OpenEJBException("Could not compile schema type system: " + schemaList, e);
        }
    }

    private void addImportsFromDefinition(Definition definition, List<XmlObject> schemaList) throws OpenEJBException {
        //noinspection unchecked
        Map<String,String> namespaceMap = definition.getNamespaces();

        Types types = definition.getTypes();
        if (types != null) {
            for (Object extensibilityElement : types.getExtensibilityElements()) {
                if (extensibilityElement instanceof Schema) {
                    Schema unknownExtensibilityElement = (Schema) extensibilityElement;
                    QName elementType = unknownExtensibilityElement.getElementType();
                    if (new QName("http://www.w3.org/2001/XMLSchema", "schema").equals(elementType)) {
                        Element element = unknownExtensibilityElement.getElement();
                        addSchemaElement(element, namespaceMap, schemaList);
                    }
                } else if (extensibilityElement instanceof UnknownExtensibilityElement) {
                    //This is allegedly obsolete as of axis-wsdl4j-1.2-RC3.jar which includes the Schema extension above.
                    //The change notes imply that imported schemas should end up in Schema elements.  They don't, so this is still needed.
                    UnknownExtensibilityElement unknownExtensibilityElement = (UnknownExtensibilityElement) extensibilityElement;
                    Element element = unknownExtensibilityElement.getElement();
                    String elementNamespace = element.getNamespaceURI();
                    String elementLocalName = element.getNodeName();
                    if ("http://www.w3.org/2001/XMLSchema".equals(elementNamespace) && "schema".equals(elementLocalName)) {
                        addSchemaElement(element, namespaceMap, schemaList);
                    }
                }
            }
        }

        //noinspection unchecked
        Map<String,List<Import>> imports = definition.getImports();
        if (imports != null) {
            for (Map.Entry<String, List<Import>> entry : imports.entrySet()) {
                String namespaceURI = entry.getKey();
                List<Import> importList = entry.getValue();
                for (Import anImport : importList) {
                    //according to the 1.1 jwsdl mr shcema imports are supposed to show up here,
                    //but according to the 1.0 spec there is supposed to be no Definition.
                    Definition definition1 = anImport.getDefinition();
                    if (definition1 != null) {
                        try {
                            URI uri = new URI(definition1.getDocumentBaseURI());
                            uris.addFirst(uri);
                        } catch (URISyntaxException e) {
                            throw new OpenEJBException("Could not locate definition", e);
                        }
                        try {
                            addImportsFromDefinition(definition1, schemaList);
                        } finally {
                            uris.removeFirst();
                        }
                    } else {
                        log.warn("Missing definition in import for namespace " + namespaceURI);
                    }
                }
            }
        }
    }

    private void addSchemaElement(Element element, Map<String,String>  namespaceMap, List<XmlObject> schemaList) throws OpenEJBException {
        try {
            XmlObject xmlObject = parseWithNamespaces(element, namespaceMap);
            schemaList.add(xmlObject);
        } catch (XmlException e) {
            throw new OpenEJBException("Could not parse schema element", e);
        }
    }

    static XmlObject parseWithNamespaces(Element element, Map<String,String> namespaceMap) throws XmlException {
        ArrayList errors = new ArrayList();
        XmlOptions xmlOptions = createXmlOptions(errors);
        SchemaDocument parsed = SchemaDocument.Factory.parse(element, xmlOptions);
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        XmlCursor cursor = parsed.newCursor();
        try {
            cursor.toFirstContentToken();
            for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
                cursor.insertNamespace(entry.getKey(), entry.getValue());
            }
        } finally {
            cursor.dispose();
        }
        return parsed;
    }

    private Definition readWsdl(URI wsdlURI) throws OpenEJBException {
        Definition definition;
        WSDLFactory wsdlFactory;
        try {
            wsdlFactory = WSDLFactory.newInstance();
        } catch (WSDLException e) {
            throw new OpenEJBException("Could not create WSDLFactory", e);
        }
        WSDLReader wsdlReaderNoImport = wsdlFactory.newWSDLReader();
        wsdlReaderNoImport.setFeature("javax.wsdl.importDocuments", false);
        ExtensionRegistry extensionRegistry = new PopulatedExtensionRegistry();
        extensionRegistry.mapExtensionTypes(Types.class, SchemaConstants.Q_ELEM_XSD_1999,
                UnknownExtensibilityElement.class);
        extensionRegistry.registerDeserializer(Types.class, SchemaConstants.Q_ELEM_XSD_1999,
                extensionRegistry.getDefaultDeserializer());
        extensionRegistry.registerSerializer(Types.class, SchemaConstants.Q_ELEM_XSD_1999,
                extensionRegistry.getDefaultSerializer());

        extensionRegistry.mapExtensionTypes(Types.class, SchemaConstants.Q_ELEM_XSD_2000,
                UnknownExtensibilityElement.class);
        extensionRegistry.registerDeserializer(Types.class, SchemaConstants.Q_ELEM_XSD_2000,
                extensionRegistry.getDefaultDeserializer());
        extensionRegistry.registerSerializer(Types.class, SchemaConstants.Q_ELEM_XSD_2000,
                extensionRegistry.getDefaultSerializer());

        extensionRegistry.mapExtensionTypes(Types.class, SchemaConstants.Q_ELEM_XSD_2001,
                UnknownExtensibilityElement.class);
        extensionRegistry.registerDeserializer(Types.class, SchemaConstants.Q_ELEM_XSD_2001,
                extensionRegistry.getDefaultDeserializer());
        extensionRegistry.registerSerializer(Types.class, SchemaConstants.Q_ELEM_XSD_2001,
                extensionRegistry.getDefaultSerializer());
        wsdlReaderNoImport.setExtensionRegistry(extensionRegistry);

        JarWSDLLocator wsdlLocator = new JarWSDLLocator(wsdlURI);
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

        Thread thread = Thread.currentThread();
        ClassLoader oldCl = thread.getContextClassLoader();
        thread.setContextClassLoader(this.getClass().getClassLoader());
        try {
            try {
                definition = wsdlReader.readWSDL(wsdlLocator);
            } catch (WSDLException e) {
                throw new OpenEJBException("Failed to read wsdl document", e);
            } catch (RuntimeException e) {
                throw new OpenEJBException(e.getMessage(), e);
            }
        } finally {
            thread.setContextClassLoader(oldCl);
        }

        return definition;
    }

    private class JarEntityResolver implements EntityResolver {

        private final static String PROJECT_URL_PREFIX = "project://local/";

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            //seems like this must be a bug in xmlbeans...
            if (systemId.indexOf(PROJECT_URL_PREFIX) > -1) {
                systemId = systemId.substring(PROJECT_URL_PREFIX.length());
            }
            URI location = uris.peek().resolve(systemId);
            ZipEntry entry = moduleFile.getEntry(location.toString());
            InputStream wsdlInputStream = moduleFile.getInputStream(entry);
            return new InputSource(wsdlInputStream);
        }
    }

    class JarWSDLLocator implements WSDLLocator {

        private final List<InputStream> streams = new ArrayList<InputStream>();
        private final URI wsdlURI;
        private URI latestImportURI;

        public JarWSDLLocator(URI wsdlURI) {
            this.wsdlURI = wsdlURI;
        }

        public InputSource getBaseInputSource() {
            ZipEntry entry = moduleFile.getEntry(wsdlURI.toString());
            if(entry == null){
                throw new RuntimeException("The webservices.xml file points to a non-existant WSDL file "+wsdlURI.toString());
            }

            InputStream wsdlInputStream;
            try {
                wsdlInputStream = moduleFile.getInputStream(entry);
                streams.add(wsdlInputStream);
            } catch (Exception e) {
                throw new RuntimeException("Could not open stream to wsdl file", e);
            }
            return new InputSource(wsdlInputStream);
        }

        public String getBaseURI() {
            return wsdlURI.toString();
        }

        public InputSource getImportInputSource(String parentLocation, String relativeLocation) {
            URI parentURI = URI.create(parentLocation);
            latestImportURI = parentURI.resolve(relativeLocation);

            InputStream importInputStream;
            try {
                ZipEntry entry = moduleFile.getEntry(latestImportURI.toString());
                importInputStream = moduleFile.getInputStream(entry);
                streams.add(importInputStream);
            } catch (Exception e) {
                throw new RuntimeException("Could not open stream to import file", e);
            }

            InputSource inputSource = new InputSource(importInputStream);
            inputSource.setSystemId(getLatestImportURI());
            return inputSource;
        }

        public String getLatestImportURI() {
            return latestImportURI.toString();
        }

        public void close() {
            for (InputStream inputStream : streams) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            streams.clear();
        }
    }
}
