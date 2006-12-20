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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.jba.cmp;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://jboss.org}ejb-relation-name"/>
 *         &lt;element ref="{http://jboss.org}read-only" minOccurs="0"/>
 *         &lt;element ref="{http://jboss.org}read-time-out" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://jboss.org}foreign-key-mapping"/>
 *           &lt;element ref="{http://jboss.org}relation-table-mapping"/>
 *         &lt;/choice>
 *         &lt;sequence minOccurs="0">
 *           &lt;element ref="{http://jboss.org}ejb-relationship-role"/>
 *           &lt;element ref="{http://jboss.org}ejb-relationship-role"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "ejb-relation")
public class EjbRelation {

    @XmlElementRefs({
        @XmlElementRef(name = "relation-table-mapping", namespace = "http://jboss.org", type = RelationTableMapping.class),
        @XmlElementRef(name = "ejb-relationship-role", namespace = "http://jboss.org", type = EjbRelationshipRole.class),
        @XmlElementRef(name = "ejb-relation-name", namespace = "http://jboss.org", type = EjbRelationName.class),
        @XmlElementRef(name = "foreign-key-mapping", namespace = "http://jboss.org", type = ForeignKeyMapping.class),
        @XmlElementRef(name = "read-only", namespace = "http://jboss.org", type = ReadOnly.class),
        @XmlElementRef(name = "read-time-out", namespace = "http://jboss.org", type = ReadTimeOut.class)
    })
    protected List<Object> content;

    /**
     * Gets the rest of the content model. 
     * 
     * <p>
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "EjbRelationshipRole" is used by two different parts of a schema. See: 
     * line 476 of file:/Users/dblevins/work/openejb3/jbosscmp-jdbc_4_0.xsd
     * line 475 of file:/Users/dblevins/work/openejb3/jbosscmp-jdbc_4_0.xsd
     * <p>
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRelationshipRole }
     * {@link EjbRelationName }
     * {@link RelationTableMapping }
     * {@link ForeignKeyMapping }
     * {@link ReadOnly }
     * {@link ReadTimeOut }
     * 
     * 
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        return this.content;
    }

}
