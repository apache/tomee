
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
 * &lt;complexType name="weblogic-rdbms-jar">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="weblogic-rdbms-bean" type="{http://www.bea.com/ns/weblogic/90}weblogic-rdbms-bean" maxOccurs="unbounded"/>
 *         &lt;element name="weblogic-rdbms-relation" type="{http://www.bea.com/ns/weblogic/90}weblogic-rdbms-relation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="order-database-operations" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="enable-batch-operations" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="create-default-dbms-tables" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validate-db-schema-with" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="database-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="default-dbms-tables-ddl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="compatibility" type="{http://www.bea.com/ns/weblogic/90}compatibility" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
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
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the weblogicRdbmsBean property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWeblogicRdbmsBean().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WeblogicRdbmsBean }
     * 
     * 
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
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the weblogicRdbmsRelation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWeblogicRdbmsRelation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WeblogicRdbmsRelation }
     * 
     * 
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
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getOrderDatabaseOperations() {
        return orderDatabaseOperations;
    }

    /**
     * Sets the value of the orderDatabaseOperations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOrderDatabaseOperations(Boolean value) {
        this.orderDatabaseOperations = value;
    }

    /**
     * Gets the value of the enableBatchOperations property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getEnableBatchOperations() {
        return enableBatchOperations;
    }

    /**
     * Sets the value of the enableBatchOperations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableBatchOperations(Boolean value) {
        this.enableBatchOperations = value;
    }

    /**
     * Gets the value of the createDefaultDbmsTables property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreateDefaultDbmsTables() {
        return createDefaultDbmsTables;
    }

    /**
     * Sets the value of the createDefaultDbmsTables property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreateDefaultDbmsTables(String value) {
        this.createDefaultDbmsTables = value;
    }

    /**
     * Gets the value of the validateDbSchemaWith property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidateDbSchemaWith() {
        return validateDbSchemaWith;
    }

    /**
     * Sets the value of the validateDbSchemaWith property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidateDbSchemaWith(String value) {
        this.validateDbSchemaWith = value;
    }

    /**
     * Gets the value of the databaseType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatabaseType() {
        return databaseType;
    }

    /**
     * Sets the value of the databaseType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatabaseType(String value) {
        this.databaseType = value;
    }

    /**
     * Gets the value of the defaultDbmsTablesDdl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultDbmsTablesDdl() {
        return defaultDbmsTablesDdl;
    }

    /**
     * Sets the value of the defaultDbmsTablesDdl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultDbmsTablesDdl(String value) {
        this.defaultDbmsTablesDdl = value;
    }

    /**
     * Gets the value of the compatibility property.
     * 
     * @return
     *     possible object is
     *     {@link Compatibility }
     *     
     */
    public Compatibility getCompatibility() {
        return compatibility;
    }

    /**
     * Sets the value of the compatibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link Compatibility }
     *     
     */
    public void setCompatibility(Compatibility value) {
        this.compatibility = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
