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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * TODO what schema?
 * The variable-mapping element defines the correlation between a
 * Java class data member or JavaBeans property to an XML element
 * or attribute name of an XML root type. If the data-member
 * element is present, the Java variable name is a public data
 * member.  If data-member	is not present, the Java variable name
 * is a JavaBeans property.
 *
 * Used in: java-xml-type-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "variable-mappingType", propOrder = {
    "javaVariableName",
    "dataMember",
    "xmlAttributeName",
    "xmlElementName",
    "xmlWildcard"
})
public class VariableMapping {
    @XmlElement(name = "java-variable-name", required = true)
    protected String javaVariableName;
    @XmlElement(name = "data-member")
    protected Object dataMember;
    @XmlElement(name = "xml-attribute-name")
    protected String xmlAttributeName;
    @XmlElement(name = "xml-element-name")
    protected String xmlElementName;
    @XmlElement(name = "xml-wildcard")
    protected Object xmlWildcard;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getJavaVariableName() {
        return javaVariableName;
    }

    public void setJavaVariableName(final String value) {
        this.javaVariableName = value;
    }

    public Object getDataMember() {
        return dataMember;
    }

    public void setDataMember(final Object value) {
        this.dataMember = value;
    }

    public String getXmlAttributeName() {
        return xmlAttributeName;
    }

    public void setXmlAttributeName(final String value) {
        this.xmlAttributeName = value;
    }

    public String getXmlElementName() {
        return xmlElementName;
    }

    public void setXmlElementName(final String value) {
        this.xmlElementName = value;
    }

    public Object getXmlWildcard() {
        return xmlWildcard;
    }

    public void setXmlWildcard(final Object value) {
        this.xmlWildcard = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }
}
