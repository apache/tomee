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
package org.apache.openejb.jee.wls;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for weblogic-rdbms-jar complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="weblogic-rdbms-jar"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="weblogic-rdbms-bean" type="{http://www.bea.com/ns/weblogic/90}weblogic-rdbms-bean" maxOccurs="unbounded"/&gt;
 *         &lt;element name="weblogic-rdbms-relation" type="{http://www.bea.com/ns/weblogic/90}weblogic-rdbms-relation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="order-database-operations" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="enable-batch-operations" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="create-default-dbms-tables" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="validate-db-schema-with" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="database-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="default-dbms-tables-ddl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="compatibility" type="{http://www.bea.com/ns/weblogic/90}compatibility" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "weblogic-rdbms-jar", propOrder = {
    "weblogicRdbmsBean",
    "weblogicRdbmsRelation",
    "orderDatabaseOperations",
    "enableBatchOperations",
    "createDefaultDbmsTables",
    "validateDbSchemaWith",
    "databaseType",
    "defaultDbmsTablesDdl",
    "compatibility"
})
public class WeblogicRdbmsJar {

    @XmlElement(name = "weblogic-rdbms-bean", required = true)
    protected List<WeblogicRdbmsBean> weblogicRdbmsBean;
    @XmlElement(name = "weblogic-rdbms-relation")
    protected List<WeblogicRdbmsRelation> weblogicRdbmsRelation;
    @XmlElement(name = "order-database-operations")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean orderDatabaseOperations;
    @XmlElement(name = "enable-batch-operations")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean enableBatchOperations;
    @XmlElement(name = "create-default-dbms-tables")
    protected String createDefaultDbmsTables;
    @XmlElement(name = "validate-db-schema-with")
    protected String validateDbSchemaWith;
    @XmlElement(name = "database-type")
    protected String databaseType;
    @XmlElement(name = "default-dbms-tables-ddl")
    protected String defaultDbmsTablesDdl;
    protected Compatibility compatibility;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the weblogicRdbmsBean property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the weblogicRdbmsBean property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWeblogicRdbmsBean().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link WeblogicRdbmsBean }
     */
    public List<WeblogicRdbmsBean> getWeblogicRdbmsBean() {
        if (weblogicRdbmsBean == null) {
            weblogicRdbmsBean = new ArrayList<WeblogicRdbmsBean>();
        }
        return this.weblogicRdbmsBean;
    }

    /**
     * Gets the value of the weblogicRdbmsRelation property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the weblogicRdbmsRelation property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWeblogicRdbmsRelation().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link WeblogicRdbmsRelation }
     */
    public List<WeblogicRdbmsRelation> getWeblogicRdbmsRelation() {
        if (weblogicRdbmsRelation == null) {
            weblogicRdbmsRelation = new ArrayList<WeblogicRdbmsRelation>();
        }
        return this.weblogicRdbmsRelation;
    }

    /**
     * Gets the value of the orderDatabaseOperations property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getOrderDatabaseOperations() {
        return orderDatabaseOperations;
    }

    /**
     * Sets the value of the orderDatabaseOperations property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setOrderDatabaseOperations(final Boolean value) {
        this.orderDatabaseOperations = value;
    }

    /**
     * Gets the value of the enableBatchOperations property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getEnableBatchOperations() {
        return enableBatchOperations;
    }

    /**
     * Sets the value of the enableBatchOperations property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setEnableBatchOperations(final Boolean value) {
        this.enableBatchOperations = value;
    }

    /**
     * Gets the value of the createDefaultDbmsTables property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCreateDefaultDbmsTables() {
        return createDefaultDbmsTables;
    }

    /**
     * Sets the value of the createDefaultDbmsTables property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCreateDefaultDbmsTables(final String value) {
        this.createDefaultDbmsTables = value;
    }

    /**
     * Gets the value of the validateDbSchemaWith property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValidateDbSchemaWith() {
        return validateDbSchemaWith;
    }

    /**
     * Sets the value of the validateDbSchemaWith property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValidateDbSchemaWith(final String value) {
        this.validateDbSchemaWith = value;
    }

    /**
     * Gets the value of the databaseType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDatabaseType() {
        return databaseType;
    }

    /**
     * Sets the value of the databaseType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDatabaseType(final String value) {
        this.databaseType = value;
    }

    /**
     * Gets the value of the defaultDbmsTablesDdl property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDefaultDbmsTablesDdl() {
        return defaultDbmsTablesDdl;
    }

    /**
     * Sets the value of the defaultDbmsTablesDdl property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultDbmsTablesDdl(final String value) {
        this.defaultDbmsTablesDdl = value;
    }

    /**
     * Gets the value of the compatibility property.
     *
     * @return possible object is
     * {@link Compatibility }
     */
    public Compatibility getCompatibility() {
        return compatibility;
    }

    /**
     * Sets the value of the compatibility property.
     *
     * @param value allowed object is
     *              {@link Compatibility }
     */
    public void setCompatibility(final Compatibility value) {
        this.compatibility = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
