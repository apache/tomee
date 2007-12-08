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

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.openejb.OpenEJBException;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.ArrayList;

public class SchemaInfoBuilderTest extends TestCase {
    public void testSimpleType() throws Exception {
        XmlSchemaInfo expected = new XmlSchemaInfo();
        {
            QName xmlTypeKey = new QName("X", "type");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", "type");
            xmlType.anonymous = false;
            xmlType.simpleBaseType = new QName("http://www.w3.org/2001/XMLSchema", "integer");
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = null;

            expected.types.put(xmlTypeKey, xmlType);
        }

        XmlSchemaInfo schemaInfo = loadSchemaInfo("schema/SimpleType.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);
    }

    public void testNestedElements() throws Exception {
        XmlSchemaInfo expected = new XmlSchemaInfo();
        {
            QName xmlTypeKey = new QName("X", "type");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", "type");
            xmlType.anonymous = false;
            xmlType.simpleBaseType = null;
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = null;
            xmlType.attributes.put("stringAttribute", new QName("http://www.w3.org/2001/XMLSchema", "string"));
            xmlType.attributes.put("intAttribute", new QName("http://www.w3.org/2001/XMLSchema", "integer"));

            {
                QName key = new QName("X", "string");

                XmlElementInfo xmlElement = new XmlElementInfo();
                xmlElement.qname = new QName("X", "string");
                xmlElement.xmlType = new QName("http://www.w3.org/2001/XMLSchema", "string");
                xmlElement.minOccurs = 1;
                xmlElement.maxOccurs = 1;
                xmlElement.nillable = false;

                xmlType.elements.put(key, xmlElement);
            }
            {
                QName key = new QName("X", "int");

                XmlElementInfo xmlElement = new XmlElementInfo();
                xmlElement.qname = new QName("X", "int");
                xmlElement.xmlType = new QName("http://www.w3.org/2001/XMLSchema", "integer");
                xmlElement.minOccurs = 1;
                xmlElement.maxOccurs = 1;
                xmlElement.nillable = false;

                xmlType.elements.put(key, xmlElement);
            }
            expected.types.put(xmlTypeKey, xmlType);
        }

        XmlSchemaInfo schemaInfo = loadSchemaInfo("schema/ComplexSequenceType.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);

        schemaInfo = loadSchemaInfo("schema/ComplexAllType.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);
    }

    public void testSoapArrayByAttribute() throws Exception {
        XmlSchemaInfo expected = new XmlSchemaInfo();
        {
            QName xmlTypeKey = new QName("X", "array");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", "array");
            xmlType.anonymous = false;
            xmlType.simpleBaseType = null;
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = new QName("http://www.w3.org/2001/XMLSchema", "integer[]");

            expected.types.put(xmlTypeKey, xmlType);
        }

        XmlSchemaInfo schemaInfo = loadSchemaInfo("schema/SoapArrayByAttribute.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);
    }

    public void testSoapArrayByRestriction() throws Exception {
        XmlSchemaInfo expected = new XmlSchemaInfo();
        {
            QName xmlTypeKey = new QName("X", "array");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", "array");
            xmlType.anonymous = false;
            xmlType.simpleBaseType = null;
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = new QName("http://www.w3.org/2001/XMLSchema", "integer");

            expected.types.put(xmlTypeKey, xmlType);
        }

        XmlSchemaInfo schemaInfo = loadSchemaInfo("schema/SoapArrayByRestriction.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);
    }

    public void testSimpleElement() throws Exception {
        XmlSchemaInfo expected = new XmlSchemaInfo();
        {
            QName xmlElementKey = new QName("X", "element");

            XmlElementInfo xmlElement = new XmlElementInfo();
            xmlElement.qname = new QName("X", "element");
            xmlElement.xmlType = new QName("X", ">element");
            xmlElement.minOccurs = 1;
            xmlElement.maxOccurs = 1;
            xmlElement.nillable = false;

            expected.elements.put(xmlElementKey, xmlElement);
        }
        {
            QName xmlTypeKey = new QName("X", ">element");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", ">element");
            xmlType.anonymous = true;
            xmlType.simpleBaseType = new QName("http://www.w3.org/2001/XMLSchema", "integer");
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = null;

            expected.types.put(xmlTypeKey, xmlType);
        }

        XmlSchemaInfo schemaInfo = loadSchemaInfo("schema/SimpleElement.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);
    }

    public void testComplexElement() throws Exception {
        XmlSchemaInfo expected = new XmlSchemaInfo();
        {
            QName xmlElementKey = new QName("X", "element");

            XmlElementInfo xmlElement = new XmlElementInfo();
            xmlElement.qname = new QName("X", "element");
            xmlElement.xmlType = new QName("X", ">element");
            xmlElement.minOccurs = 1;
            xmlElement.maxOccurs = 1;
            xmlElement.nillable = false;

            expected.elements.put(xmlElementKey, xmlElement);
        }
        {
            QName xmlTypeKey = new QName("X", ">element");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", ">element");
            xmlType.anonymous = true;
            xmlType.simpleBaseType = null;
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = null;
            xmlType.attributes.put("stringAttribute", new QName("http://www.w3.org/2001/XMLSchema", "string"));
            xmlType.attributes.put("intAttribute", new QName("http://www.w3.org/2001/XMLSchema", "integer"));

            {
                QName key = new QName("X", "string");

                XmlElementInfo xmlElement = new XmlElementInfo();
                xmlElement.qname = new QName("X", "string");
                xmlElement.xmlType = new QName("http://www.w3.org/2001/XMLSchema", "string");
                xmlElement.minOccurs = 1;
                xmlElement.maxOccurs = 1;
                xmlElement.nillable = false;

                xmlType.elements.put(key, xmlElement);
            }
            {
                QName key = new QName("X", "int");

                XmlElementInfo xmlElement = new XmlElementInfo();
                xmlElement.qname = new QName("X", "int");
                xmlElement.xmlType = new QName("http://www.w3.org/2001/XMLSchema", "integer");
                xmlElement.minOccurs = 1;
                xmlElement.maxOccurs = 1;
                xmlElement.nillable = false;

                xmlType.elements.put(key, xmlElement);
            }
            expected.types.put(xmlTypeKey, xmlType);
        }

        XmlSchemaInfo schemaInfo = loadSchemaInfo("schema/ComplexSequenceElement.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);

        schemaInfo = loadSchemaInfo("schema/ComplexAllElement.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);
    }

    public void testJaxRpcSpecExample() throws Exception {
        XmlSchemaInfo expected = new XmlSchemaInfo();

        // <complexType name="root">
        //   <sequence>
        //     <element name="data" type="string"/>
        //   </sequence>
        // </complexType>
        {
            QName xmlTypeKey = new QName("X", "root");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", "root");
            xmlType.anonymous = false;
            xmlType.simpleBaseType = null;
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = null;
            {
                QName xmlElementKey = new QName("X", "data");

                XmlElementInfo xmlElement = new XmlElementInfo();
                xmlElement.qname = new QName("X", "data");
                xmlElement.xmlType = new QName("http://www.w3.org/2001/XMLSchema", "string");
                xmlElement.minOccurs = 1;
                xmlElement.maxOccurs = 1;
                xmlElement.nillable = false;

                xmlType.elements.put(xmlElementKey, xmlElement);
            }

            expected.types.put(xmlTypeKey, xmlType);
        }

        // <element name="root">
        //   <complexType>
        //     <sequence>
        //       <element name="inside" maxOccurs="10">
        //         <complexType>
        //           <sequence>
        //             <element name="data2" type="string"/>
        //           </sequence>
        //         </complexType>
        //       </element>
        //       <element ref="tns:someOtherElement" maxOccurs="20"/>
        //     </sequence>
        //   </complexType>
        // </element>
        {
            QName xmlElementKey = new QName("X", "root");

            XmlElementInfo xmlElement = new XmlElementInfo();
            xmlElement.qname = new QName("X", "root");
            xmlElement.xmlType = new QName("X", ">root");
            xmlElement.minOccurs = 1;
            xmlElement.maxOccurs = 1;
            xmlElement.nillable = false;

            expected.elements.put(xmlElementKey, xmlElement);
        }
        {
            QName xmlTypeKey = new QName("X", ">root");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", ">root");
            xmlType.anonymous = true;
            xmlType.simpleBaseType = null;
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = null;
            {
                QName xmlElementKey = new QName("X", "inside");

                XmlElementInfo xmlElement = new XmlElementInfo();
                xmlElement.qname = new QName("X", "inside");
                xmlElement.xmlType = new QName("X", ">>root>inside");
                xmlElement.minOccurs = 1;
                xmlElement.maxOccurs = 10;
                xmlElement.nillable = false;

                xmlType.elements.put(xmlElementKey, xmlElement);
            }
            {
                QName xmlElementKey = new QName("X", "someOtherElement");

                XmlElementInfo xmlElement = new XmlElementInfo();
                xmlElement.qname = new QName("X", "someOtherElement");
                xmlElement.xmlType = new QName("http://www.w3.org/2001/XMLSchema", "int");
                xmlElement.minOccurs = 1;
                xmlElement.maxOccurs = 20;
                xmlElement.nillable = false;

                xmlType.elements.put(xmlElementKey, xmlElement);
            }

            expected.types.put(xmlTypeKey, xmlType);
        }
        {
            QName xmlTypeKey = new QName("X", ">>root>inside");

            XmlTypeInfo xmlType = new XmlTypeInfo();
            xmlType.qname = new QName("X", ">>root>inside");
            xmlType.anonymous = true;
            xmlType.simpleBaseType = null;
            xmlType.enumType = false;
            xmlType.listType = false;
            xmlType.arrayComponentType = null;
            {
                QName xmlElementKey = new QName("X", "data2");

                XmlElementInfo xmlElement = new XmlElementInfo();
                xmlElement.qname = new QName("X", "data2");
                xmlElement.xmlType = new QName("http://www.w3.org/2001/XMLSchema", "string");
                xmlElement.minOccurs = 1;
                xmlElement.maxOccurs = 1;
                xmlElement.nillable = false;

                xmlType.elements.put(xmlElementKey, xmlElement);
            }

            expected.types.put(xmlTypeKey, xmlType);
        }

        // <element name="someOtherElement" type="xsd:int"/>
        {
            QName xmlElementKey = new QName("X", "someOtherElement");

            XmlElementInfo xmlElement = new XmlElementInfo();
            xmlElement.qname = new QName("X", "someOtherElement");
            xmlElement.xmlType = new QName("http://www.w3.org/2001/XMLSchema", "int");
            xmlElement.minOccurs = 1;
            xmlElement.maxOccurs = 1;
            xmlElement.nillable = false;

            expected.elements.put(xmlElementKey, xmlElement);
        }

        XmlSchemaInfo schemaInfo = loadSchemaInfo("schema/JaxRpcSpecExample.xsd");
        TypeInfoTestUtil.assertEqual(expected, schemaInfo, true);
    }

    private XmlSchemaInfo loadSchemaInfo(String fileName) throws Exception {
        if (true) {
            InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
            XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
            xmlSchemaCollection.read(new InputStreamReader(in), null);
            CommonsSchemaInfoBuilder schemaInfoBuilder = new CommonsSchemaInfoBuilder(xmlSchemaCollection);
            XmlSchemaInfo schemaInfo = schemaInfoBuilder.createSchemaInfo();
            return schemaInfo;
        } else {
            Collection<XmlError> errors = new ArrayList<XmlError>();
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setErrorListener(errors);
            InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
            SchemaDocument schema = SchemaDocument.Factory.parse(in, xmlOptions);

            SchemaTypeSystem schemaTypeSystem = XmlBeans.compileXsd(new XmlObject[]{schema}, XmlBeansSchemaLoader.basicTypeSystem, xmlOptions);
            if (errors.size() > 0) {
                boolean wasError = false;
                for (XmlError xmlError : errors) {
                    if (xmlError.getSeverity() == XmlError.SEVERITY_ERROR) {
                        System.out.println(xmlError);
                        wasError = true;
                    } else if (xmlError.getSeverity() == XmlError.SEVERITY_WARNING) {
                        System.out.println(xmlError);
                    } else if (xmlError.getSeverity() == XmlError.SEVERITY_INFO) {
                        System.out.println(xmlError);
                    }
                }
                if (wasError) {
                    throw new OpenEJBException("Could not compile schema type system, see log for errors");
                }
            }

            XmlBeansSchemaInfoBuilder schemaInfoBuilder = new XmlBeansSchemaInfoBuilder(schemaTypeSystem);
            XmlSchemaInfo schemaInfo = schemaInfoBuilder.createSchemaInfo();
            return schemaInfo;
        }
    }
}
