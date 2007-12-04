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
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
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
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class SchemaInfoBuilder {
    private static final Log log = LogFactory.getLog(SchemaInfoBuilder.class);
    private static final SchemaTypeSystem basicTypeSystem;

    public static XmlOptions createXmlOptions(Collection errors) {
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        return options;
    }

    static {
        InputStream is = SchemaInfoBuilder.class.getClassLoader().getResourceAsStream("META-INF/schema/soap_encoding_1_1.xsd");
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

    private final JarFile moduleFile;
    private final Definition definition;
    private final LinkedList<URI> uris = new LinkedList<URI>();

    private final Map<SchemaTypeKey, SchemaType> schemaTypeKeyToSchemaTypeMap;

    // Simple types by QName
    private final Map<QName, SchemaType> simpleTypeMap;

    // Complex types by QName
    private final Map<QName, SchemaType> complexTypeMap;

    // Map from element QName to type QName
    private final Map<QName, QName> elementToTypeMap;

    // Ports by name
    private final Map<String, Port> portMap;


    public SchemaInfoBuilder(JarFile moduleFile, URI wsdlUri) throws OpenEJBException {
        if (moduleFile == null) throw new NullPointerException("moduleFile is null");
        if (wsdlUri == null) throw new NullPointerException("wsdlUri is null");

        this.moduleFile = moduleFile;
        uris.addFirst(wsdlUri);
        definition = readWsdl(wsdlUri);

        SchemaTypeSystem schemaTypeSystem = compileSchemaTypeSystem(definition);
        schemaTypeKeyToSchemaTypeMap = buildSchemaTypeKeyToSchemaTypeMap(schemaTypeSystem);

        complexTypeMap = buildComplexTypeMap();
        simpleTypeMap = buildSimpleTypeMap();
        elementToTypeMap = buildElementMap();
        portMap = buildPortMap();
    }

    public Map<SchemaTypeKey, SchemaType> getSchemaTypeKeyToSchemaTypeMap() {
        return schemaTypeKeyToSchemaTypeMap;
    }

    public Definition getDefinition() {
        return definition;
    }

    /**
     * Find all the complex types in the previously constructed schema analysis.
     * Put them in a map from complex type QName to schema fragment.
     *
     * @return map of complexType QName to schema fragment
     */
    public Map<QName, SchemaType> getComplexTypesInWsdl() {
        return complexTypeMap;
    }

    private Map<QName, SchemaType> buildComplexTypeMap() {
        Map<QName, SchemaType> complexTypeMap = new HashMap<QName, SchemaType>();
        for (Map.Entry<SchemaTypeKey, SchemaType> entry : schemaTypeKeyToSchemaTypeMap.entrySet()) {
            SchemaTypeKey key = entry.getKey();
            SchemaType schemaType = entry.getValue();

            if (!key.isSimpleType() && !key.isAnonymous()) {
                QName qName = key.getQName();
                complexTypeMap.put(qName, schemaType);
            }
        }
        return complexTypeMap;
    }

    public Map<QName, QName> getElementToTypeMap() {
        return elementToTypeMap;
    }

    private Map<QName, QName> buildElementMap() {
        Map<QName, QName> elementToTypeMap = new HashMap<QName, QName>();
        for (Map.Entry<SchemaTypeKey, SchemaType> entry : schemaTypeKeyToSchemaTypeMap.entrySet()) {
            SchemaTypeKey key = entry.getKey();
            SchemaType schemaType = entry.getValue();

            if (key.isElement()) {
                QName elementQName = key.getQName();
                QName typeQName = schemaType.getName();
                elementToTypeMap.put(elementQName, typeQName);
            }
        }
        return elementToTypeMap;
    }

    /**
     * Gets a map of all the javax.wsdl.Port instance in the WSDL definition keyed by the port's QName
     * <p/>
     * WSDL 1.1 spec: 2.6 "The name attribute provides a unique name among all ports defined within in the enclosing WSDL document."
     *
     * @return Map of port QName to javax.wsdl.Port for that QName.
     */

    public Map<String, Port> getPortMap() {
        return portMap;
    }

    @SuppressWarnings({"unchecked"})
    private Map<String, Port> buildPortMap() {
        HashMap<String, Port> ports = new HashMap<String, Port>();
        if (definition != null) {
            for (Object object : definition.getServices().values()) {
                Service service = (Service) object;
                ports.putAll(service.getPorts());
            }
        }
        return ports;
    }

    public Map<QName, SchemaType> getSimpleTypeMap() {
        return simpleTypeMap;
    }

    private Map<QName, SchemaType> buildSimpleTypeMap() {
        Map<QName, SchemaType> simpleTypeMap = new HashMap<QName, SchemaType>();
        for (Map.Entry<SchemaTypeKey, SchemaType> entry : schemaTypeKeyToSchemaTypeMap.entrySet()) {
            SchemaTypeKey key = entry.getKey();
            SchemaType schemaType = entry.getValue();

            if (key.isSimpleType() && !key.isAnonymous()) {
                QName qName = key.getQName();
                simpleTypeMap.put(qName, schemaType);
            }
        }
        return simpleTypeMap;
    }

    public SchemaTypeSystem compileSchemaTypeSystem(Definition definition) throws OpenEJBException {
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

    /**
     * builds a map of SchemaTypeKey containing jaxrpc-style fake QName and context info to xmlbeans SchemaType object.
     *
     * @param schemaTypeSystem
     * @return Map of SchemaTypeKey to xmlbeans SchemaType object.
     */
    private Map<SchemaTypeKey, SchemaType> buildSchemaTypeKeyToSchemaTypeMap(SchemaTypeSystem schemaTypeSystem) {
        Map<SchemaTypeKey, SchemaType> qnameMap = new HashMap<SchemaTypeKey, SchemaType>();

        SchemaType[] globalTypes = schemaTypeSystem.globalTypes();
        for (SchemaType globalType : globalTypes) {
            QName typeQName = globalType.getName();
            addSchemaType(typeQName, globalType, false, qnameMap);
        }

        SchemaGlobalElement[] globalElements = schemaTypeSystem.globalElements();
        for (SchemaGlobalElement globalElement : globalElements) {
            addElement(globalElement, null, qnameMap);
        }

        return qnameMap;
    }

    private void addElement(SchemaField element, SchemaTypeKey key, Map<SchemaTypeKey, SchemaType> qnameMap) {
        //TODO is this null if element is a ref?
        QName elementName = element.getName();
        String elementNamespace = elementName.getNamespaceURI();
        //"" namespace means local element with elementFormDefault="unqualified"
        if (elementNamespace == null || elementNamespace.equals("")) {
            elementNamespace = key.getQName().getNamespaceURI();
        }
        String elementQNameLocalName;
        SchemaTypeKey elementKey;
        if (key == null) {
            //top level. rule 2.a,
            elementQNameLocalName = elementName.getLocalPart();
            elementKey = new SchemaTypeKey(elementName, true, false, false, elementName);
        } else {
            //not top level. rule 2.b, key will be for enclosing Type.
            QName enclosingTypeQName = key.getQName();
            String enclosingTypeLocalName = enclosingTypeQName.getLocalPart();
            elementQNameLocalName = enclosingTypeLocalName + ">" + elementName.getLocalPart();
            QName subElementName = new QName(elementNamespace, elementQNameLocalName);
            elementKey = new SchemaTypeKey(subElementName, true, false, true, elementName);
        }
        SchemaType schemaType = element.getType();
        qnameMap.put(elementKey, schemaType);
//        new Exception("Adding: " + elementKey.getqName().getLocalPart()).printStackTrace();
        //check if it's an array. maxOccurs is null if unbounded
        //element should always be a SchemaParticle... this is a workaround for XMLBEANS-137
        if (element instanceof SchemaParticle) {
            addArrayForms((SchemaParticle) element, elementKey.getQName(), qnameMap, schemaType);
        } else {
            log.warn("element is not a schemaParticle! " + element);
        }
        //now, name for type.  Rule 1.b, type inside an element
        String typeQNameLocalPart = ">" + elementQNameLocalName;
        QName typeQName = new QName(elementNamespace, typeQNameLocalPart);
        boolean isAnonymous = true;
        addSchemaType(typeQName, schemaType, isAnonymous, qnameMap);
    }

    private void addSchemaType(QName typeQName, SchemaType schemaType, boolean anonymous, Map<SchemaTypeKey, SchemaType> qnameMap) {
        SchemaTypeKey typeKey = new SchemaTypeKey(typeQName, false, schemaType.isSimpleType(), anonymous, null);
        qnameMap.put(typeKey, schemaType);
//        new Exception("Adding: " + typeKey.getqName().getLocalPart()).printStackTrace();
        //TODO xmlbeans recommends using summary info from getElementProperties and getAttributeProperties instead of traversing the content model by hand.
        SchemaParticle schemaParticle = schemaType.getContentModel();
        if (schemaParticle != null) {
            addSchemaParticle(schemaParticle, typeKey, qnameMap);
        }
    }


    private void addSchemaParticle(SchemaParticle schemaParticle, SchemaTypeKey key, Map<SchemaTypeKey, SchemaType> qnameMap) {
        if (schemaParticle.getParticleType() == SchemaParticle.ELEMENT) {
            SchemaType elementType = schemaParticle.getType();
            SchemaField element = elementType.getContainerField();
            //element will be null if the type is defined elsewhere, such as a built in type.
            if (element != null) {
                addElement(element, key, qnameMap);
            } else {
                QName keyQName = key.getQName();
                //TODO I can't distinguish between 3.a and 3.b, so generate names both ways.
                //3.b
                String localPart = schemaParticle.getName().getLocalPart();
                QName elementName = new QName(keyQName.getNamespaceURI(), localPart);
                addArrayForms(schemaParticle, elementName, qnameMap, elementType);
                //3.a
                localPart = keyQName.getLocalPart() + ">" + schemaParticle.getName().getLocalPart();
                elementName = new QName(keyQName.getNamespaceURI(), localPart);
                addArrayForms(schemaParticle, elementName, qnameMap, elementType);
            }
        } else {
            try {
                for (SchemaParticle child : schemaParticle.getParticleChildren()) {
                    addSchemaParticle(child, key, qnameMap);
                }
            } catch (NullPointerException e) {
                //ignore xmlbeans bug
            }
        }
    }

    private void addArrayForms(SchemaParticle schemaParticle, QName keyName, Map<SchemaTypeKey, SchemaType> qnameMap, SchemaType elementType) {
        //it may be a ref or a built in type.  If it's an array (maxOccurs >1) form a type for it.
        if (schemaParticle.getIntMaxOccurs() > 1) {
            String maxOccurs = schemaParticle.getMaxOccurs() == null ? "unbounded" : "" + schemaParticle.getIntMaxOccurs();
            int minOccurs = schemaParticle.getIntMinOccurs();
            QName elementName = schemaParticle.getName();
            String arrayQNameLocalName = keyName.getLocalPart() + "[" + minOccurs + "," + maxOccurs + "]";
            String elementNamespace = elementName.getNamespaceURI();
            if (elementNamespace == null || elementNamespace.equals("")) {
                elementNamespace = keyName.getNamespaceURI();
            }
            QName arrayName = new QName(elementNamespace, arrayQNameLocalName);
            SchemaTypeKey arrayKey = new SchemaTypeKey(arrayName, false, false, true, elementName);
            //TODO not clear we want the schemaType as the value
            qnameMap.put(arrayKey, elementType);
//            new Exception("Adding: " + arrayKey.getqName().getLocalPart()).printStackTrace();
            if (minOccurs == 1) {
                arrayQNameLocalName = keyName.getLocalPart() + "[," + maxOccurs + "]";
                arrayName = new QName(elementNamespace, arrayQNameLocalName);
                arrayKey = new SchemaTypeKey(arrayName, false, false, true, elementName);
                //TODO not clear we want the schemaType as the value
                qnameMap.put(arrayKey, elementType);
            }
        }
    }


    public Definition readWsdl(URI wsdlURI) throws OpenEJBException {
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

    public static <T extends ExtensibilityElement> T getExtensibilityElement(Class<T> clazz, List extensibilityElements) throws OpenEJBException {
        for (Object o : extensibilityElements) {
            ExtensibilityElement extensibilityElement = (ExtensibilityElement) o;
            if (clazz.isAssignableFrom(extensibilityElement.getClass())) {
                return clazz.cast(extensibilityElement);
            }
        }
        throw new OpenEJBException("No element of class " + clazz.getName() + " found");
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
