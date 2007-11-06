
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
 * <p>Java class for relationship-role-map complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="relationship-role-map">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="foreign-key-table" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="primary-key-table" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="column-map" type="{http://www.bea.com/ns/weblogic/90}column-map" maxOccurs="unbounded"/>
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
@XmlType(name = "relationship-role-map", propOrder = {
    "foreignKeyTable",
    "primaryKeyTable",
    "columnMap"
})
public class RelationshipRoleMap {

    @XmlElement(name = "foreign-key-table")
    protected String foreignKeyTable;
    @XmlElement(name = "primary-key-table")
    protected String primaryKeyTable;
    @XmlElement(name = "column-map", required = true)
    protected List<ColumnMap> columnMap;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the foreignKeyTable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForeignKeyTable() {
        return foreignKeyTable;
    }

    /**
     * Sets the value of the foreignKeyTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForeignKeyTable(String value) {
        this.foreignKeyTable = value;
    }

    /**
     * Gets the value of the primaryKeyTable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrimaryKeyTable() {
        return primaryKeyTable;
    }

    /**
     * Sets the value of the primaryKeyTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrimaryKeyTable(String value) {
        this.primaryKeyTable = value;
    }

    /**
     * Gets the value of the columnMap property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the columnMap property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getColumnMap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ColumnMap }
     * 
     * 
     */
    public List<ColumnMap> getColumnMap() {
        if (columnMap == null) {
            columnMap = new ArrayList<ColumnMap>();
        }
        return this.columnMap;
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
