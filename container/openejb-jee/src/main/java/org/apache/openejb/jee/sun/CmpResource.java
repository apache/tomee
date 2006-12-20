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

package org.apache.openejb.jee.sun;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "jndiName",
    "defaultResourcePrincipal",
    "property",
    "createTablesAtDeploy",
    "dropTablesAtUndeploy",
    "databaseVendorName",
    "schemaGeneratorProperties"
})
@XmlRootElement(name = "cmp-resource")
public class CmpResource {

    @XmlElement(name = "jndi-name", required = true)
    protected String jndiName;
    @XmlElement(name = "default-resource-principal")
    protected DefaultResourcePrincipal defaultResourcePrincipal;
    protected List<Property> property;
    @XmlElement(name = "create-tables-at-deploy")
    protected String createTablesAtDeploy;
    @XmlElement(name = "drop-tables-at-undeploy")
    protected String dropTablesAtUndeploy;
    @XmlElement(name = "database-vendor-name")
    protected String databaseVendorName;
    @XmlElement(name = "schema-generator-properties")
    protected SchemaGeneratorProperties schemaGeneratorProperties;

    /**
     * Gets the value of the jndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * Sets the value of the jndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJndiName(String value) {
        this.jndiName = value;
    }

    /**
     * Gets the value of the defaultResourcePrincipal property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultResourcePrincipal }
     *     
     */
    public DefaultResourcePrincipal getDefaultResourcePrincipal() {
        return defaultResourcePrincipal;
    }

    /**
     * Sets the value of the defaultResourcePrincipal property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultResourcePrincipal }
     *     
     */
    public void setDefaultResourcePrincipal(DefaultResourcePrincipal value) {
        this.defaultResourcePrincipal = value;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * 
     * 
     */
    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

    /**
     * Gets the value of the createTablesAtDeploy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreateTablesAtDeploy() {
        return createTablesAtDeploy;
    }

    /**
     * Sets the value of the createTablesAtDeploy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreateTablesAtDeploy(String value) {
        this.createTablesAtDeploy = value;
    }

    /**
     * Gets the value of the dropTablesAtUndeploy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDropTablesAtUndeploy() {
        return dropTablesAtUndeploy;
    }

    /**
     * Sets the value of the dropTablesAtUndeploy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDropTablesAtUndeploy(String value) {
        this.dropTablesAtUndeploy = value;
    }

    /**
     * Gets the value of the databaseVendorName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatabaseVendorName() {
        return databaseVendorName;
    }

    /**
     * Sets the value of the databaseVendorName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatabaseVendorName(String value) {
        this.databaseVendorName = value;
    }

    /**
     * Gets the value of the schemaGeneratorProperties property.
     * 
     * @return
     *     possible object is
     *     {@link SchemaGeneratorProperties }
     *     
     */
    public SchemaGeneratorProperties getSchemaGeneratorProperties() {
        return schemaGeneratorProperties;
    }

    /**
     * Sets the value of the schemaGeneratorProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemaGeneratorProperties }
     *     
     */
    public void setSchemaGeneratorProperties(SchemaGeneratorProperties value) {
        this.schemaGeneratorProperties = value;
    }

}
