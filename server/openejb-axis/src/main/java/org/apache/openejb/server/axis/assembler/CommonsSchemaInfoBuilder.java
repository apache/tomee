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
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaGroupBase;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

public class CommonsSchemaInfoBuilder {
    private static final Log log = LogFactory.getLog(CommonsSchemaInfoBuilder.class);
    private static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    private static final String XML_NS_NS = "http://www.w3.org/2000/xmlns/";
    private static final String SOAP_ENCODING_NS = "http://schemas.xmlsoap.org/soap/encoding/";
    private static final QName SOAP_ARRAY = new QName(SOAP_ENCODING_NS, "Array");
    private static final QName SOAP_ARRAY_TYPE = new QName(SOAP_ENCODING_NS, "arrayType");
    private static final QName WSDL_ARRAY_TYPE = new QName("http://schemas.xmlsoap.org/wsdl/", "arrayType");

    private final XmlSchemaCollection xmlSchemaCollection;

    private final Map<QName, XmlTypeInfo> xmlTypes = new HashMap<QName, XmlTypeInfo>();
    private final Map<QName, XmlElementInfo> xmlElements = new HashMap<QName, XmlElementInfo>();

    public CommonsSchemaInfoBuilder(JarFile moduleFile, URI wsdlUri) throws OpenEJBException {
        if (moduleFile == null) throw new NullPointerException("moduleFile is null");
        if (wsdlUri == null) throw new NullPointerException("wsdlUri is null");

        CommonsSchemaLoader schemaLoader = new CommonsSchemaLoader(wsdlUri, moduleFile);
        xmlSchemaCollection = schemaLoader.loadSchema();
    }

    public CommonsSchemaInfoBuilder(XmlSchemaCollection xmlSchemaCollection) {
        if (xmlSchemaCollection == null) throw new NullPointerException("schemaTypeSystem is null");
        this.xmlSchemaCollection = xmlSchemaCollection;
    }

    public XmlSchemaInfo createSchemaInfo() throws OpenEJBException {

        buildXmlTypeInfos();

        XmlSchemaInfo schemaInfo = new XmlSchemaInfo();
        schemaInfo.types.putAll(xmlTypes);
        schemaInfo.elements.putAll(xmlElements);
        return schemaInfo;
    }


    private void buildXmlTypeInfos() {
        for (XmlSchema schema : xmlSchemaCollection.getXmlSchemas()) {
            // Global Elements
            for (Iterator iterator = schema.getElements().getValues(); iterator.hasNext();) {
                XmlSchemaElement globalElement = (XmlSchemaElement) iterator.next();
                addGlobalElement(globalElement);
            }

            // Global Types
            for (Iterator iterator = schema.getSchemaTypes().getValues(); iterator.hasNext();) {
                XmlSchemaType globalType = (XmlSchemaType) iterator.next();
                addType(globalType.getQName(), globalType);
            }
        }
    }

    private void addGlobalElement(XmlSchemaElement element) {
        // Nested anonymous type
        QName xmlType = element.getSchemaTypeName();
        if (xmlType == null) {
            // Rule 1.b: Anonymous type inside an element ">E"
            xmlType = new QName(element.getQName().getNamespaceURI(), ">" + element.getQName().getLocalPart());
            addType(xmlType, element.getSchemaType());
        }

        // create the XmlElementInfo
        XmlElementInfo elementInfo = createXmlElementInfo(element.getQName(), xmlType, element);
        xmlElements.put(element.getQName(), elementInfo);

    }

    private static XmlElementInfo createXmlElementInfo(QName qname, QName xmlType, XmlSchemaElement element) {
        XmlElementInfo elementInfo = new XmlElementInfo();

        elementInfo.qname = qname;
        elementInfo.xmlType = xmlType;
        elementInfo.minOccurs = element.getMinOccurs();
        elementInfo.maxOccurs = element.getMaxOccurs();
        elementInfo.nillable = element.isNillable();

        return elementInfo;
    }

    private void addType(QName typeQName, XmlSchemaType type) {
        // skip built in xml schema types
        if (XML_SCHEMA_NS.equals(typeQName.getNamespaceURI())) {
            return;
        }

        XmlTypeInfo typeInfo = createXmlTypeInfo(typeQName, type);
        xmlTypes.put(typeQName, typeInfo);

        if (type instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;

            // process elements nested inside of this element
            List<XmlSchemaElement> elements = getNestedElements(complexType);
            for (XmlSchemaElement element : elements) {
                addNestedElement(element, typeInfo);
            }
        }
    }

    private void addNestedElement(XmlSchemaElement element, XmlTypeInfo enclosingType) {
        QName elementQName;
        QName typeQName;
        if (element.getRefName() == null) {
            //
            // Normal element in a type
            //

            // Element Name with namespace
            String elementNamespace = element.getQName().getNamespaceURI();
            if (elementNamespace == null || elementNamespace.equals("")) {
                elementNamespace = enclosingType.qname.getNamespaceURI();
            }
            elementQName = new QName(elementNamespace, element.getQName().getLocalPart());

            // Type name
            if (element.getSchemaTypeName() != null) {
                // Global type
                typeQName = element.getSchemaTypeName();
            } else {
                // Anonymous type, so we need to declare it

                // Rule 2.b: Anonymous element absolute name ÒT>NÓ
                String anonymoustName = enclosingType.qname.getLocalPart() + ">" + elementQName.getLocalPart();
                QName anonymousQName = new QName(elementNamespace, anonymoustName);

                // Rule 1.b: Anonymous type name ">E"
                typeQName = new QName(elementNamespace, ">" + anonymousQName.getLocalPart());
                addType(typeQName, element.getSchemaType());
            }
        } else {
            //
            // Referenced global element
            //

            // Local the referenced global element
            XmlSchemaElement refElement = xmlSchemaCollection.getElementByQName(element.getRefName());

            // The name and type of the nested element are determined by the referenced element
            elementQName = refElement.getQName();
            typeQName = refElement.getSchemaTypeName();
        }

        // Add element to enclosing type
        XmlElementInfo nestedElement = createXmlElementInfo(elementQName, typeQName, element);
        enclosingType.elements.put(nestedElement.qname, nestedElement);
    }

    public static XmlTypeInfo createXmlTypeInfo(QName qname, XmlSchemaType type) {
        if (qname == null) throw new NullPointerException("qname is null");
        if (type == null) throw new NullPointerException("type is null");

        XmlTypeInfo typeInfo = new XmlTypeInfo();
        typeInfo.qname = qname;
        typeInfo.anonymous = qname.getLocalPart().indexOf('>') >= 0;

        if (type instanceof XmlSchemaSimpleType) {
            XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType) type;
            XmlSchemaSimpleTypeContent content = simpleType.getContent();
            if (content instanceof XmlSchemaSimpleTypeList) {
                XmlSchemaSimpleTypeList list = (XmlSchemaSimpleTypeList) content;
                typeInfo.simpleBaseType = list.getItemType().getQName();

                // this is a list
                typeInfo.listType = true;
            } else if(content instanceof XmlSchemaSimpleTypeRestriction) {
                XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) content;
                typeInfo.simpleBaseType = restriction.getBaseTypeName();

                // is this an enumeration?
                for (Iterator iterator = restriction.getFacets().getIterator(); iterator.hasNext();) {
                    if (iterator.next() instanceof XmlSchemaEnumerationFacet) {
                        typeInfo.enumType = true;
                        break;
                    }

                }
            }
        } else if (type instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;

            // SOAP array component type
            typeInfo.arrayComponentType = extractSoapArrayComponentType(complexType);

            // process attributes (skip soap arrays which have non-mappable attributes)
            if (!isSoapArray(complexType)) {
                XmlSchemaObjectCollection attributes = complexType.getAttributes();
                for (Iterator iterator = attributes.getIterator(); iterator.hasNext();) {
                    Object item = iterator.next();
                    if (item instanceof XmlSchemaAttribute) {
                        XmlSchemaAttribute attribute = (XmlSchemaAttribute) item;
                        Object old = typeInfo.attributes.put(attribute.getQName().getLocalPart(), attribute.getSchemaTypeName());
                        if (old != null) {
                            throw new IllegalArgumentException("Complain to your expert group member, spec does not support attributes with the same local name and differing namespaces: original: " + old + ", duplicate local name: " + attribute);
                        }
                    }
                }
            }
        } else {
            log.warn("Unknown schema type class " + typeInfo.getClass().getName());
        }

        return typeInfo;
    }

    private static boolean isSoapArray(XmlSchemaComplexType complexType) {
        // Soap arrays are based on complex content restriction
        XmlSchemaContentModel contentModel = complexType.getContentModel();
        if (contentModel == null) {
            return false;
        }
        XmlSchemaContent content = contentModel.getContent();
        if (!(content instanceof XmlSchemaComplexContentRestriction)) {
            return false;
        }

        XmlSchemaComplexContentRestriction restriction = (XmlSchemaComplexContentRestriction) content;
        return SOAP_ARRAY.equals(restriction.getBaseTypeName());
    }

    /**
     * Extract the nested component type of an Array from the XML Schema Type.
     * @return the QName of the nested component type or null if the schema type can not be determined
     * @throws org.apache.openejb.OpenEJBException if the XML Schema Type can not represent an Array @param complexType
     */
    private static QName extractSoapArrayComponentType(XmlSchemaComplexType complexType) {
        // Soap arrays are based on complex content restriction
        if (!isSoapArray(complexType)) {
            return null;
        }

        XmlSchemaComplexContentRestriction restriction = (XmlSchemaComplexContentRestriction) complexType.getContentModel().getContent();

        //First, handle case that looks like this:
        // <complexType name="ArrayOfstring">
        //     <complexContent>
        //         <restriction base="soapenc:Array">
        //             <attribute ref="soapenc:arrayType" wsdl:arrayType="xsd:string[]"/>
        //         </restriction>
        //     </complexContent>
        // </complexType>
        XmlSchemaObjectCollection attributes = restriction.getAttributes();
        for (Iterator iterator = attributes.getIterator(); iterator.hasNext();) {
            Object item = iterator.next();
            if (item instanceof XmlSchemaAttribute) {
                XmlSchemaAttribute attribute = (XmlSchemaAttribute) item;
                if (attribute.getRefName().equals(SOAP_ARRAY_TYPE)) {
                    for (Attr attr : attribute.getUnhandledAttributes()) {
                        QName attQName = new QName(attr.getNamespaceURI(), attr.getLocalName());
                        if (WSDL_ARRAY_TYPE.equals(attQName)) {
                            // value is a namespace prefixed xsd type
                            String value = attr.getValue();

                            // extract local part
                            int pos = value.lastIndexOf(":");
                            QName componentType;
                            if (pos < 0) {
                                componentType = new QName("", value);
                            } else {
                                String localPart = value.substring(pos + 1);

                                // resolve the namespace prefix
                                String prefix = value.substring(0, pos);
                                String namespace = getNamespaceForPrefix(prefix, attr.getOwnerElement());

                                componentType = new QName(namespace, localPart);
                            }
                            log.debug("determined component type from element type");
                            return componentType;
                        }
                    }
                }
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
        XmlSchemaParticle particle = restriction.getParticle();
        if (particle instanceof XmlSchemaSequence) {
            XmlSchemaSequence sequence = (XmlSchemaSequence) particle;
            if (sequence.getItems().getCount() != 1) {
                throw new IllegalArgumentException("more than one element inside array definition: " + complexType);
            }
            XmlSchemaObject item = sequence.getItems().getItem(0);
            if (item instanceof XmlSchemaElement) {
                XmlSchemaElement element = (XmlSchemaElement) item;
                QName componentType = element.getSchemaTypeName();
                log.debug("determined component type from element type");
                return componentType;
            }
        }

        return null;
    }

    private static String getNamespaceForPrefix(String prefix, Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            if (node instanceof Attr) {
                Attr attr = (Attr) node;
                if (XML_NS_NS.equals(attr.getNamespaceURI())) {
                    // this is a namespace declaration, is it the one we are looking for?
                    if (attr.getLocalName().equals(prefix)) {
                        return attr.getValue();
                    }
                }
            }
        }

        // try parent
        if (element.getParentNode() instanceof Element) {
            return getNamespaceForPrefix(prefix, (Element) element.getParentNode());
        }

        // didn't find it - just use prefix as the namespace
        return prefix;
    }


    private static List<XmlSchemaElement> getNestedElements(XmlSchemaComplexType complexType) {
        List<XmlSchemaElement> elements = new ArrayList<XmlSchemaElement>();
        XmlSchemaParticle particle = complexType.getParticle();
        if (particle instanceof XmlSchemaElement) {
            XmlSchemaElement element = (XmlSchemaElement) particle;
            elements.add(element);
        } else if (particle instanceof XmlSchemaGroupBase && !(particle instanceof XmlSchemaChoice)) {
            XmlSchemaGroupBase groupBase = (XmlSchemaGroupBase) particle;
            for (Iterator iterator = groupBase.getItems().getIterator(); iterator.hasNext();) {
                XmlSchemaParticle child = (XmlSchemaParticle) iterator.next();
                if (child instanceof XmlSchemaElement) {
                    XmlSchemaElement element = (XmlSchemaElement) child;
                    elements.add(element);
                }
            }
        } else {
            // ignore all other types... you can have these other types, but JAX-RPC doesn't support them
        }
        return elements;
    }
}