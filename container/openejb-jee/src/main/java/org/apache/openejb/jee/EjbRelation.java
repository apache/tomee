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

package org.apache.openejb.jee;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The ejb-relationType describes a relationship between two
 * entity beans with container-managed persistence.  It is used
 * by ejb-relation elements. It contains a description; an
 * optional ejb-relation-name element; and exactly two
 * relationship role declarations, defined by the
 * ejb-relationship-role elements. The name of the
 * relationship, if specified, is unique within the ejb-jar
 * file.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejb-relationType", propOrder = {
        "content"
        })
public class EjbRelation {

    @XmlElementRefs({
    @XmlElementRef(name = "ejb-relation-name", namespace = "http://java.sun.com/xml/ns/javaee", type = JAXBElement.class),
    @XmlElementRef(name = "ejb-relationship-role", namespace = "http://java.sun.com/xml/ns/javaee", type = JAXBElement.class),
    @XmlElementRef(name = "description", namespace = "http://java.sun.com/xml/ns/javaee", type = JAXBElement.class)
            })
    protected List<JAXBElement<?>> content;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the rest of the content model.
     * <p/>
     * <p/>
     * You are getting this "catch-all" property because of the following reason:
     * The field name "EjbRelationshipRole" is used by two different parts of a schema. See:
     * line 766 of openejb3/container/openejb-jee/src/main/xsd/ejb-jar_3_0.xsd
     * line 764 of openejb3/container/openejb-jee/src/main/xsd/ejb-jar_3_0.xsd
     * <p/>
     * To get rid of this property, apply a property customization to one
     * of both of the following declarations to change their names:
     * Gets the value of the content property.
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link EjbRelationshipRole }{@code >}
     * {@link JAXBElement }{@code <}{@link Text }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    public List<JAXBElement<?>> getContent() {
        if (content == null) {
            content = new ArrayList<JAXBElement<?>>();
        }
        return this.content;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
