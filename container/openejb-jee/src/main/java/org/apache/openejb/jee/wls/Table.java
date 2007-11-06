
package org.apache.openejb.jee.wls;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for table complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="table">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="table-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dbms-column" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="ejb-relationship-role-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "table", propOrder = {
    "tableName",
    "dbmsColumn",
    "ejbRelationshipRoleName"
})
public class Table {

    @XmlElement(name = "table-name", required = true)
    protected String tableName;
    @XmlElement(name = "dbms-column", required = true)
    protected List<String> dbmsColumn;
    @XmlElement(name = "ejb-relationship-role-name")
    protected String ejbRelationshipRoleName;

    /**
     * Gets the value of the tableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableName(String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the dbmsColumn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dbmsColumn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDbmsColumn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDbmsColumn() {
        if (dbmsColumn == null) {
            dbmsColumn = new ArrayList<String>();
        }
        return this.dbmsColumn;
    }

    /**
     * Gets the value of the ejbRelationshipRoleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjbRelationshipRoleName() {
        return ejbRelationshipRoleName;
    }

    /**
     * Sets the value of the ejbRelationshipRoleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEjbRelationshipRoleName(String value) {
        this.ejbRelationshipRoleName = value;
    }

}
