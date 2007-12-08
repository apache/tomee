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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public class XmlBeansSchemaInfoBuilder {
    private static final Log log = LogFactory.getLog(XmlBeansSchemaInfoBuilder.class);
    private static final String SOAP_ENCODING_NS = "http://schemas.xmlsoap.org/soap/encoding/";
    private static final QName SOAP_ARRAY = new QName(SOAP_ENCODING_NS, "Array");
    private static final QName SOAP_ARRAY_TYPE = new QName(SOAP_ENCODING_NS, "arrayType");

    private final SchemaTypeSystem schemaTypeSystem;

    private final Map<QName, XmlTypeInfo> xmlTypes = new HashMap<QName, XmlTypeInfo>();
    private final Map<QName, XmlElementInfo> xmlElements = new HashMap<QName, XmlElementInfo>();

    public XmlBeansSchemaInfoBuilder(JarFile moduleFile, URI wsdlUri) throws OpenEJBException {
        if (moduleFile == null) throw new NullPointerException("moduleFile is null");
        if (wsdlUri == null) throw new NullPointerException("wsdlUri is null");

        XmlBeansSchemaLoader schemaLoader = new XmlBeansSchemaLoader(wsdlUri, moduleFile);
        schemaTypeSystem = schemaLoader.loadSchema();
    }

    public XmlBeansSchemaInfoBuilder(SchemaTypeSystem schemaTypeSystem) {
        if (schemaTypeSystem == null) throw new NullPointerException("schemaTypeSystem is null");
        this.schemaTypeSystem = schemaTypeSystem;
    }

    public XmlSchemaInfo createSchemaInfo() throws OpenEJBException {

        buildXmlTypeInfos();

        XmlSchemaInfo schemaInfo = new XmlSchemaInfo();
        schemaInfo.types.putAll(xmlTypes);
        schemaInfo.elements.putAll(xmlElements);
        return schemaInfo;
    }


    /**
     * builds a map of SchemaTypeKey containing jaxrpc-style fake QName and context info to xmlbeans SchemaType object.
     */
    private void buildXmlTypeInfos() {
        // Global Types
        SchemaType[] globalTypes = schemaTypeSystem.globalTypes();
        for (SchemaType globalType : globalTypes) {
            QName typeQName = globalType.getName();
            addSchemaType(typeQName, globalType);
        }

        // Global Elements
        SchemaGlobalElement[] globalElements = schemaTypeSystem.globalElements();
        for (SchemaGlobalElement globalElement : globalElements) {
            addElement(globalElement, null);
        }
    }

    private void addSchemaType(QName typeQName, SchemaType schemaType) {
        XmlTypeInfo type = createXmlTypeInfo(typeQName, schemaType);
        xmlTypes.put(typeQName, type);

        // process elements nested inside of this element
        SchemaParticle schemaParticle = schemaType.getContentModel();
        if (schemaParticle != null) {
            addSchemaParticle(schemaParticle, type);
        }
    }

    private void addSchemaParticle(SchemaParticle schemaParticle, XmlTypeInfo type) {
        if (schemaParticle.getParticleType() == SchemaParticle.ELEMENT) {
            SchemaType elementType = schemaParticle.getType();
            SchemaField element = elementType.getContainerField();

            // element will be null if the type is defined elsewhere, such as a built in type.
            if (element != null) {
                addElement(element, type);
            }
        } else if (schemaParticle.getParticleType() == SchemaParticle.SEQUENCE ||
                schemaParticle.getParticleType() == SchemaParticle.ALL) {
            try {
                for (SchemaParticle child : schemaParticle.getParticleChildren()) {
                    addSchemaParticle(child, type);
                }
            } catch (NullPointerException e) {
                //ignore xmlbeans bug
            }
        } else {
            // ignore all other types... you can have these other types, but JAX-RPC doesn't support them
        }
    }

    private void addElement(SchemaField element, XmlTypeInfo enclosingType) {
        String elementNamespace = element.getName().getNamespaceURI();
        if (elementNamespace == null || elementNamespace.equals("")) {
            elementNamespace = enclosingType.qname.getNamespaceURI();
        }

        //
        QName elementName;
        if (enclosingType == null) {
            // Rule 2.a: Global element
            elementName = new QName(elementNamespace, element.getName().getLocalPart());
        } else {
            // Rule 2.b: Anonymous element with in a type ÒT>NÓ
            String anonymoustName = enclosingType.qname.getLocalPart() + ">" + element.getName().getLocalPart();
            elementName = new QName(elementNamespace, anonymoustName);
        }

        // create the XmlElementInfo
        XmlElementInfo elementInfo = createXmlElementInfo(elementName, (SchemaParticle) element);
        xmlElements.put(elementName, elementInfo);

        // Nested anonymous type
        // the type name is null when the type is anonymous
        if (element.getType().getName() == null) {
            // Rule 1.b: Anonymous type inside an element ">E"
            QName typeQName = new QName(elementNamespace, ">" + elementInfo.qname.getLocalPart());
            addSchemaType(typeQName, element.getType());
        }
    }

    public static XmlTypeInfo createXmlTypeInfo(QName name, SchemaType schemaType) {
        if (name == null) throw new NullPointerException("qname is null");
        if (schemaType == null) throw new NullPointerException("schemaType is null");

        XmlTypeInfo type = new XmlTypeInfo();
        type.qname = name;
        type.anonymous = name.getLocalPart().indexOf('>') >= 0;

        // array component type
        type.arrayComponentType = extractArrayComponentType(schemaType);

        // Map type QName to declaration (including nested types)
        if (schemaType.getContentModel() != null) {
            int particleType = schemaType.getContentModel().getParticleType();
            if (SchemaParticle.ELEMENT == particleType) {
                XmlElementInfo nestedElement = createXmlElementInfo(schemaType.getContentModel().getName(), schemaType.getContentModel());
                type.elements.put(nestedElement.qname, nestedElement);
            } else if (particleType == SchemaParticle.SEQUENCE || particleType == SchemaParticle.ALL) {
                for (SchemaParticle parameter : schemaType.getContentModel().getParticleChildren()) {
                    // ignore non-element types
                    if (parameter.getParticleType() == SchemaParticle.ELEMENT) {
                        XmlElementInfo nestedElement = createXmlElementInfo(parameter.getName(), parameter);
                        type.elements.put(nestedElement.qname, nestedElement);
                    }
                }
            } else  {
                throw new IllegalArgumentException("Only all, choice and sequence particle types are supported." + " SchemaType name =" + schemaType.getName());
            }
        }

        // Index attributes by name
        if (schemaType.getAttributeModel() != null) {
            // don't index the attributes on a soap array
            if (!schemaType.getBaseType().getName().equals(SOAP_ARRAY)) {
                for (SchemaLocalAttribute attribute : schemaType.getAttributeModel().getAttributes()) {
                    Object old = type.attributes.put(attribute.getName().getLocalPart(), attribute.getType().getName());
                    if (old != null) {
                        throw new IllegalArgumentException("Complain to your expert group member, spec does not support attributes with the same local name and differing namespaces: original: " + old + ", duplicate local name: " + attribute);
                    }
                }
            }
        }

        //
        // Blah
        //
        type.enumType = schemaType.getEnumerationValues() != null;
        type.listType = schemaType.getSimpleVariety() == SchemaType.LIST;

        if (schemaType.getDerivationType() == SchemaType.DT_RESTRICTION) {
            QName baseType = null;
            if (schemaType.isSimpleType()) {
                if (schemaType.getSimpleVariety() == SchemaType.ATOMIC) {
                    baseType = schemaType.getBaseType().getName();
                } else if (schemaType.getSimpleVariety() == SchemaType.LIST) {
                    // not needed but we could use it
                     baseType = schemaType.getListItemType().getName();
                }
            } else {
                if (SchemaType.SIMPLE_CONTENT == schemaType.getContentType()) {
                    baseType = schemaType.getBaseType().getName();
                }
            }
            type.simpleBaseType = baseType;
        }

        return type;
    }

    private static XmlElementInfo createXmlElementInfo(QName name, SchemaParticle particle) {
        XmlElementInfo elementInfo = new XmlElementInfo();

        elementInfo.qname = name;
        elementInfo.xmlType = particle.getType().getName();
        if (elementInfo.xmlType == null) {
            // anonymous type
            elementInfo.xmlType = new QName(name.getNamespaceURI(), ">" + elementInfo.qname.getLocalPart());
        }
        elementInfo.minOccurs = particle.getIntMinOccurs();
        elementInfo.maxOccurs = particle.getIntMaxOccurs();
        elementInfo.nillable = particle.isNillable();

        return elementInfo;
    }

    /**
     * Extract the nested component type of an Array from the XML Schema Type.
     * @return the QName of the nested component type or null if the schema type can not be determined
     * @throws org.apache.openejb.OpenEJBException if the XML Schema Type can not represent an Array
     * @param schemaType
     */
    private static QName extractArrayComponentType(SchemaType schemaType) {
        //First, handle case that looks like this:
        // <complexType name="ArrayOfstring">
        //     <complexContent>
        //         <restriction base="soapenc:Array">
        //             <attribute ref="soapenc:arrayType" wsdl:arrayType="xsd:string[]"/>
        //         </restriction>
        //     </complexContent>
        // </complexType>
        SchemaAttributeModel attributeModel = schemaType.getAttributeModel();
        if (attributeModel != null) {
            SchemaLocalAttribute arrayTypeAttribute = attributeModel.getAttribute(SOAP_ARRAY_TYPE);
            if (arrayTypeAttribute != null) {
                SchemaWSDLArrayType wsdlArrayType = (SchemaWSDLArrayType) arrayTypeAttribute;
                SOAPArrayType soapArrayType = wsdlArrayType.getWSDLArrayType();
                if (soapArrayType != null) {
                    QName componentType = soapArrayType.getQName();
                    log.debug("Extracted componentType " + componentType + " from schemaType " + schemaType);
                    return componentType;
                } else {
                    log.info("No SOAPArrayType for component from schemaType " + schemaType);
                }
            } else {
                log.warn("No soap array info for schematype: " + schemaType);
            }
        }

        // If that didn't work, try to handle case like this:
        // <complexType name="ArrayOfstring1">
        //     <complexContent>
        //         <restriction base="soapenc:Array">
        //             <sequence>
        //                 <element name="string1" type="xsd:string" minOccurs="0" maxOccurs="unbounded"/>
        //             </sequence>
        //         </restriction>
        //     </complexContent>
        // </complexType>
        if (schemaType.getBaseType().getName().equals(SOAP_ARRAY)) {
            SchemaProperty[] properties = schemaType.getDerivedProperties();
            if (properties.length != 1) {
                throw new IllegalArgumentException("more than one element inside array definition: " + schemaType);
            }
            QName componentType = properties[0].getType().getName();
            log.debug("determined component type from element type");
            return componentType;
        }

        return null;
    }
}
