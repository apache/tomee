
package org.apache.openejb.jee.wls;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for column-map complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="column-map">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="foreign-key-column" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="key-column" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "column-map", propOrder = {
    "foreignKeyColumn",
    "keyColumn"
})
public class ColumnMap {

    @XmlElement(name = "foreign-key-column", required = true)
    protected String foreignKeyColumn;
    @XmlElement(name = "key-column", required = true)
    protected String keyColumn;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the foreignKeyColumn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }

    /**
     * Sets the value of the foreignKeyColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForeignKeyColumn(String value) {
        this.foreignKeyColumn = value;
    }

    /**
     * Gets the value of the keyColumn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyColumn() {
        return keyColumn;
    }

    /**
     * Sets the value of the keyColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyColumn(String value) {
        this.keyColumn = value;
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
