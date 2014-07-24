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
import org.apache.openejb.server.ServerRuntimeException;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class CommonsSchemaLoader {
    private static final Log log = LogFactory.getLog(CommonsSchemaLoader.class);

    private final URI wsdlUri;
    private final JarFile moduleFile;
    private final XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();

    public CommonsSchemaLoader(final URI wsdlUri, final JarFile moduleFile) {
        this.wsdlUri = wsdlUri;
        this.moduleFile = moduleFile;
    }

    public XmlSchemaCollection loadSchema() throws OpenEJBException {
        final Definition definition = readWsdl(wsdlUri);
        addImportsFromDefinition(definition);
        return xmlSchemaCollection;
    }

    private void addImportsFromDefinition(final Definition definition) throws OpenEJBException {
        final Types types = definition.getTypes();
        if (types != null) {
            for (final Object extensibilityElement : types.getExtensibilityElements()) {
                if (extensibilityElement instanceof Schema) {
                    final Schema unknownExtensibilityElement = (Schema) extensibilityElement;
                    final QName elementType = unknownExtensibilityElement.getElementType();
                    if (new QName("http://www.w3.org/2001/XMLSchema", "schema").equals(elementType)) {
                        final Element element = unknownExtensibilityElement.getElement();
                        xmlSchemaCollection.read(element);
                    }
                } else if (extensibilityElement instanceof UnknownExtensibilityElement) {
                    //This is allegedly obsolete as of axis-wsdl4j-1.2-RC3.jar which includes the Schema extension above.
                    //The change notes imply that imported schemas should end up in Schema elements.  They don't, so this is still needed.
                    final UnknownExtensibilityElement unknownExtensibilityElement = (UnknownExtensibilityElement) extensibilityElement;
                    final Element element = unknownExtensibilityElement.getElement();
                    final String elementNamespace = element.getNamespaceURI();
                    final String elementLocalName = element.getNodeName();
                    if ("http://www.w3.org/2001/XMLSchema".equals(elementNamespace) && "schema".equals(elementLocalName)) {
                        xmlSchemaCollection.read(element);
                    }
                }
            }
        }

        //noinspection unchecked
        final Map<String, List<Import>> imports = definition.getImports();
        if (imports != null) {
            for (final Map.Entry<String, List<Import>> entry : imports.entrySet()) {
                final String namespaceURI = entry.getKey();
                final List<Import> importList = entry.getValue();
                for (final Import anImport : importList) {
                    //according to the 1.1 jwsdl mr shcema imports are supposed to show up here,
                    //but according to the 1.0 spec there is supposed to be no Definition.
                    final Definition importedDef = anImport.getDefinition();
                    if (importedDef != null) {
                        addImportsFromDefinition(importedDef);
                    } else {
                        log.warn("Missing definition in import for namespace " + namespaceURI);
                    }
                }
            }
        }
    }

    private Definition readWsdl(final URI wsdlURI) throws OpenEJBException {
        Definition definition;
        final WSDLFactory wsdlFactory;
        try {
            wsdlFactory = WSDLFactory.newInstance();
        } catch (final WSDLException e) {
            throw new OpenEJBException("Could not create WSDLFactory", e);
        }
        final WSDLReader wsdlReaderNoImport = wsdlFactory.newWSDLReader();
        wsdlReaderNoImport.setFeature("javax.wsdl.importDocuments", false);
        final ExtensionRegistry extensionRegistry = new PopulatedExtensionRegistry();
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

        final JarWSDLLocator wsdlLocator = new JarWSDLLocator(wsdlURI);
        final WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

        final Thread thread = Thread.currentThread();
        final ClassLoader oldCl = thread.getContextClassLoader();
        thread.setContextClassLoader(this.getClass().getClassLoader());
        try {
            try {
                definition = wsdlReader.readWSDL(wsdlLocator);
            } catch (final WSDLException e) {
                throw new OpenEJBException("Failed to read wsdl document", e);
            } catch (final RuntimeException e) {
                throw new OpenEJBException(e.getMessage(), e);
            }
        } finally {
            thread.setContextClassLoader(oldCl);
        }

        return definition;
    }

    class JarWSDLLocator implements WSDLLocator {

        private final List<InputStream> streams = new ArrayList<InputStream>();
        private final URI wsdlURI;
        private URI latestImportURI;

        public JarWSDLLocator(final URI wsdlURI) {
            this.wsdlURI = wsdlURI;
        }

        public InputSource getBaseInputSource() {
            final ZipEntry entry = moduleFile.getEntry(wsdlURI.toString());
            if (entry == null) {
                throw new ServerRuntimeException("The webservices.xml file points to a non-existant WSDL file " + wsdlURI.toString());
            }

            final InputStream wsdlInputStream;
            try {
                wsdlInputStream = moduleFile.getInputStream(entry);
                streams.add(wsdlInputStream);
            } catch (final Exception e) {
                throw new ServerRuntimeException("Could not open stream to wsdl file", e);
            }
            return new InputSource(wsdlInputStream);
        }

        public String getBaseURI() {
            return wsdlURI.toString();
        }

        public InputSource getImportInputSource(final String parentLocation, final String relativeLocation) {
            final URI parentURI = URI.create(parentLocation);
            latestImportURI = parentURI.resolve(relativeLocation);

            final InputStream importInputStream;
            try {
                final ZipEntry entry = moduleFile.getEntry(latestImportURI.toString());
                importInputStream = moduleFile.getInputStream(entry);
                streams.add(importInputStream);
            } catch (final Exception e) {
                throw new ServerRuntimeException("Could not open stream to import file", e);
            }

            final InputSource inputSource = new InputSource(importInputStream);
            inputSource.setSystemId(getLatestImportURI());
            return inputSource;
        }

        public String getLatestImportURI() {
            return latestImportURI.toString();
        }

        public void close() {
            for (final InputStream inputStream : streams) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    //ignore
                }
            }
            streams.clear();
        }
    }
}