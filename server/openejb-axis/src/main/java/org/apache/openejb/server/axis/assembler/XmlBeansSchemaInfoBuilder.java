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
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class XmlBeansSchemaInfoBuilder {
    private static final Log log = LogFactory.getLog(XmlBeansSchemaInfoBuilder.class);
    private static final String SOAP_ENCODING_NS = "http://schemas.xmlsoap.org/soap/encoding/";

    private final JarFile moduleFile;
    private final URI wsdlUri;

    public XmlBeansSchemaInfoBuilder(JarFile moduleFile, URI wsdlUri) throws OpenEJBException {
        if (moduleFile == null) throw new NullPointerException("moduleFile is null");
        if (wsdlUri == null) throw new NullPointerException("wsdlUri is null");

        this.moduleFile = moduleFile;
        this.wsdlUri = wsdlUri;
    }

    public XmlSchemaInfo createSchemaInfo() throws OpenEJBException {
        XmlBeansSchemaLoader schemaLoader = new XmlBeansSchemaLoader(wsdlUri, moduleFile);
        SchemaTypeSystem schemaTypeSystem = schemaLoader.loadSchema();

        List<XmlTypeInfo> list = buildSchemaTypeKeyToSchemaTypeMap(schemaTypeSystem);

        XmlSchemaInfo schemaInfo = new XmlSchemaInfo();
        for (XmlTypeInfo type : list) {
            schemaInfo.types.put(type.qname, type);
        }
        return schemaInfo;
    }


    /**
     * builds a map of SchemaTypeKey containing jaxrpc-style fake QName and context info to xmlbeans SchemaType object.
     *
     * @param schemaTypeSystem
     * @return Map of SchemaTypeKey to xmlbeans SchemaType object.
     */
    private List<XmlTypeInfo> buildSchemaTypeKeyToSchemaTypeMap(SchemaTypeSystem schemaTypeSystem) {
        List<XmlTypeInfo> types = new ArrayList<XmlTypeInfo>();

        SchemaType[] globalTypes = schemaTypeSystem.globalTypes();
        for (SchemaType globalType : globalTypes) {
            QName typeQName = globalType.getName();
            addSchemaType(typeQName, globalType, false, types);
        }

        SchemaGlobalElement[] globalElements = schemaTypeSystem.globalElements();
        for (SchemaGlobalElement globalElement : globalElements) {
            addElement(globalElement, null, types);
        }

        return types;
    }

    private void addElement(SchemaField element, XmlTypeInfo type, List<XmlTypeInfo> types) {
        //TODO is this null if element is a ref?
        QName elementName = element.getName();
        String elementNamespace = elementName.getNamespaceURI();
        //"" namespace means local element with elementFormDefault="unqualified"
        if (elementNamespace == null || elementNamespace.equals("")) {
            elementNamespace = type.qname.getNamespaceURI();
        }
        SchemaType schemaType = element.getType();

        String elementQNameLocalName;
        XmlTypeInfo elementType;
        if (type == null) {
            //top level. rule 2.a,
            elementQNameLocalName = elementName.getLocalPart();
            elementType = createXmlTypeInfo(elementName, false, elementName, schemaType);
        } else {
            //not top level. rule 2.b, key will be for enclosing Type.
            QName enclosingTypeQName = type.qname;
            String enclosingTypeLocalName = enclosingTypeQName.getLocalPart();
            elementQNameLocalName = enclosingTypeLocalName + ">" + elementName.getLocalPart();
            QName subElementName = new QName(elementNamespace, elementQNameLocalName);
            elementType = createXmlTypeInfo(subElementName, true, elementName, schemaType);
        }
        types.add(elementType);

//        new Exception("Adding: " + elementKey.getqName().getLocalPart()).printStackTrace();
        //check if it's an array. maxOccurs is null if unbounded
        //element should always be a SchemaParticle... this is a workaround for XMLBEANS-137
        if (element instanceof SchemaParticle) {
            addArrayForms((SchemaParticle) element, elementType.qname, types, schemaType);
        } else {
            log.warn("element is not a schemaParticle! " + element);
        }
        //now, name for type.  Rule 1.b, type inside an element
        String typeQNameLocalPart = ">" + elementQNameLocalName;
        QName typeQName = new QName(elementNamespace, typeQNameLocalPart);
        boolean isAnonymous = true;
        addSchemaType(typeQName, schemaType, isAnonymous, types);
    }

    private void addSchemaType(QName typeQName, SchemaType schemaType, boolean anonymous, List<XmlTypeInfo> types) {
        XmlTypeInfo type = createXmlTypeInfo(typeQName, anonymous, null, schemaType);
        types.add(type);
//        new Exception("Adding: " + typeKey.getqName().getLocalPart()).printStackTrace();
        //TODO xmlbeans recommends using summary info from getElementProperties and getAttributeProperties instead of traversing the content model by hand.
        SchemaParticle schemaParticle = schemaType.getContentModel();
        if (schemaParticle != null) {
            addSchemaParticle(schemaParticle, type, types);
        }
    }


    private void addSchemaParticle(SchemaParticle schemaParticle, XmlTypeInfo type, List<XmlTypeInfo> types) {
        if (schemaParticle.getParticleType() == SchemaParticle.ELEMENT) {
            SchemaType elementType = schemaParticle.getType();
            SchemaField element = elementType.getContainerField();
            //element will be null if the type is defined elsewhere, such as a built in type.
            if (element != null) {
                addElement(element, type, types);
            } else {
                QName keyQName = type.qname;
                //TODO I can't distinguish between 3.a and 3.b, so generate names both ways.
                //3.b
                String localPart = schemaParticle.getName().getLocalPart();
                QName elementName = new QName(keyQName.getNamespaceURI(), localPart);
                addArrayForms(schemaParticle, elementName, types, elementType);
                //3.a
                localPart = keyQName.getLocalPart() + ">" + schemaParticle.getName().getLocalPart();
                elementName = new QName(keyQName.getNamespaceURI(), localPart);
                addArrayForms(schemaParticle, elementName, types, elementType);
            }
        } else {
            try {
                for (SchemaParticle child : schemaParticle.getParticleChildren()) {
                    addSchemaParticle(child, type, types);
                }
            } catch (NullPointerException e) {
                //ignore xmlbeans bug
            }
        }
    }

    private void addArrayForms(SchemaParticle schemaParticle, QName keyName, List<XmlTypeInfo> types, SchemaType elementType) {
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
            XmlTypeInfo arrayType = createXmlTypeInfo(arrayName, true, elementName, elementType);
            //TODO not clear we want the schemaType as the value
            types.add(arrayType);
//            new Exception("Adding: " + arrayKey.getqName().getLocalPart()).printStackTrace();
            if (minOccurs == 1) {
                arrayQNameLocalName = keyName.getLocalPart() + "[," + maxOccurs + "]";
                arrayName = new QName(elementNamespace, arrayQNameLocalName);
                arrayType = createXmlTypeInfo(arrayName, true, elementName, elementType);
                //TODO not clear we want the schemaType as the value
                types.add(arrayType);
            }
        }
    }

    public static XmlTypeInfo createXmlTypeInfo(QName name,           // schema type name, element name or generated anonymous name
            boolean anonymous,     //
            QName elementQName,    //
            SchemaType schemaType) {

        if (name == null) throw new NullPointerException("qname is null");
        if (schemaType == null) throw new NullPointerException("schemaType is null");

        XmlTypeInfo type = new XmlTypeInfo();
        type.qname = name;
        type.anonymous = anonymous;
        type.elementQName = elementQName;

        type.simpleType = schemaType.isSimpleType();
        type.xmlType = schemaType.getName();
        type.restriction = schemaType.getDerivationType() == SchemaType.DT_RESTRICTION;

        type.arrayComponentType = extractArrayComponentType(schemaType);

        // Map type QName to declaration (including nested types)
        if (schemaType.getContentModel() != null) {
            int particleType = schemaType.getContentModel().getParticleType();
            if (SchemaParticle.ELEMENT == particleType) {
                XmlNestedElementInfo nestedElement = createXmlNestedElementInfo(schemaType.getContentModel());
                type.nestedElements.put(nestedElement.qname, nestedElement);
            } else if (particleType == SchemaParticle.SEQUENCE || particleType == SchemaParticle.ALL) {
                for (SchemaParticle parameter : schemaType.getContentModel().getParticleChildren()) {
                    // ignore non-element types
                    if (parameter.getParticleType() == SchemaParticle.ELEMENT) {
                        XmlNestedElementInfo nestedElement = createXmlNestedElementInfo(parameter);
                        type.nestedElements.put(nestedElement.qname, nestedElement);
                    }
                }
            } else  {
                throw new IllegalArgumentException("Only all, choice and sequence particle types are supported." + " SchemaType name =" + schemaType.getName());
            }
        }

        // Index attributes by name
        if (schemaType.getAttributeModel() != null) {
            for (SchemaLocalAttribute attribute : schemaType.getAttributeModel().getAttributes()) {
                Object old = type.attributeTypes.put(attribute.getName().getLocalPart(), attribute.getType().getName());
                if (old != null) {
                    throw new IllegalArgumentException("Complain to your expert group member, spec does not support attributes with the same local name and differing namespaces: original: " + old + ", duplicate local name: " + attribute);
                }
            }
        }

        //
        // Blah
        //
        type.enumType = schemaType.getEnumerationValues() != null;
        type.listType = schemaType.getSimpleVariety() == SchemaType.LIST;

        QName baseType = null;
        if (schemaType.isSimpleType()) {
            if (schemaType.getSimpleVariety() == SchemaType.ATOMIC) {
                baseType = schemaType.getPrimitiveType().getName();
            } else if (schemaType.getSimpleVariety() == SchemaType.LIST) {
                // not needed but we could use it
                // baseType = schemaType.getListItemType().getName();
            }
        } else {
            if (SchemaType.SIMPLE_CONTENT == schemaType.getContentType()) {
                baseType = schemaType.getBaseType().getName();
            } else if (SchemaType.EMPTY_CONTENT == schemaType.getContentType() ||
                    SchemaType.ELEMENT_CONTENT == schemaType.getContentType() ||
                    SchemaType.MIXED_CONTENT == schemaType.getContentType()) {
                baseType = schemaType.getName();
            }
        }
        type.baseType = baseType;

        return type;
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
        SchemaLocalAttribute arrayTypeAttribute =  schemaType.getAttributeModel().getAttribute(new QName(SOAP_ENCODING_NS, "arrayType"));
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
        if (schemaType.getBaseType().getName().equals(new QName(SOAP_ENCODING_NS, "Array"))) {
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

    private static XmlNestedElementInfo createXmlNestedElementInfo(SchemaParticle particle) {
        XmlNestedElementInfo nestedElementInfo = new XmlNestedElementInfo();

        nestedElementInfo.qname = particle.getName();
        nestedElementInfo.xmlType = particle.getType().getName();
        nestedElementInfo.simpleType = particle.getType().isSimpleType();

        SchemaType baseType = particle.getType().getBaseType();
        if (baseType != null) {
            nestedElementInfo.baseType = particle.getType().getBaseType().getName();
        } else {
            nestedElementInfo.baseType = null;
        }

        nestedElementInfo.minOccurs = particle.getIntMinOccurs();
        nestedElementInfo.maxOccurs = particle.getIntMaxOccurs();
        nestedElementInfo.nillable = particle.isNillable();

        return nestedElementInfo;
    }
}
