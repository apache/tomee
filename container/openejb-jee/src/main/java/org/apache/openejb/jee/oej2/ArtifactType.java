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

package org.apache.openejb.jee.oej2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Refers to either another module running in the server, or
 *                 an entry in the server's Repository.  In either case this effectively uses a
 *                 URI.
 * 
 *                 When this is pointing to a repository entry, the URI must have a form
 *                 acceptable to the repository, which is currently a URI consisting of
 *                 Maven-style identifiers separated by slashes (groupId/artifactId/version/type,
 *                 for example, the URI "postgresql/postgresql-8.0-jdbc/313/jar" for a file like
 *                 "repository/postgresql/postgresql-8.0-jdbc-313.jar").
 * 
 *                 When this is pointing to a module, the URI should match the
 *                 module's moduleId.  This also looks
 *                 like a Maven-style URI discussed above.
 * 
 *                 The artifactType element can take either a straight URI (as in the examples
 *                 above), or maven-style identifier fragments (groupId, type, artifactId, and
 *                 version), which it will compose into a URI by adding up the fragments with
 *                 slashes in between.
 * 
 *                 There is a correspondence between the xml format and a URI.  For example, the URI
 * 
 *                 postgresql/postgresql-8.0-jdbc/313/jar
 * 
 *                 corresponds to the xml:
 * 
 *                 <groupId>postgresql</groupId>
 *                 <artifactId>postgresql-8.0-jdbc</artifactId>
 *                 <version>313</version>
 *                 <type>jar</type>
 * 
 *           
 * 
 * <p>Java class for artifactType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="artifactType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="groupId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="artifactId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "artifactType", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", propOrder = {
    "groupId",
    "artifactId",
    "version",
    "type"
})
public class ArtifactType {

    @XmlElement(name="groupId", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected String groupId;
    @XmlElement(name="artifactId", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", required = true)
    protected String artifactId;
    @XmlElement(name="version", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected String version;
    @XmlElement(name="type", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected String type;

    /**
     * Gets the value of the groupId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupId(String value) {
        this.groupId = value;
    }

    /**
     * Gets the value of the artifactId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the value of the artifactId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArtifactId(String value) {
        this.artifactId = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
