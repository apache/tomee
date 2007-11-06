
package org.apache.openejb.jee.wls;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sql-shape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sql-shape">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.bea.com/ns/weblogic/90}description" minOccurs="0"/>
 *         &lt;element name="sql-shape-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="table" type="{http://www.bea.com/ns/weblogic/90}table" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="pass-through-columns" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ejb-relation-name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sql-shape", propOrder = {
    "description",
    "sqlShapeName",
    "table",
    "passThroughColumns",
    "ejbRelationName"
})
public class SqlShape {

    protected Description description;
    @XmlElement(name = "sql-shape-name", required = true)
    protected String sqlShapeName;
    protected List<Table> table;
    @XmlElement(name = "pass-through-columns")
    protected BigInteger passThroughColumns;
    @XmlElement(name = "ejb-relation-name")
    protected List<String> ejbRelationName;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the sqlShapeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSqlShapeName() {
        return sqlShapeName;
    }

    /**
     * Sets the value of the sqlShapeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSqlShapeName(String value) {
        this.sqlShapeName = value;
    }

    /**
     * Gets the value of the table property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the table property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTable().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Table }
     * 
     * 
     */
    public List<Table> getTable() {
        if (table == null) {
            table = new ArrayList<Table>();
        }
        return this.table;
    }

    /**
     * Gets the value of the passThroughColumns property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPassThroughColumns() {
        return passThroughColumns;
    }

    /**
     * Sets the value of the passThroughColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPassThroughColumns(BigInteger value) {
        this.passThroughColumns = value;
    }

    /**
     * Gets the value of the ejbRelationName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRelationName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbRelationName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getEjbRelationName() {
        if (ejbRelationName == null) {
            ejbRelationName = new ArrayList<String>();
        }
        return this.ejbRelationName;
    }

}
