
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
 * <p>Java class for persistence-use complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="persistence-use">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="type-identifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type-version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type-storage" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "persistence-use", propOrder = {
    "typeIdentifier",
    "typeVersion",
    "typeStorage"
})
public class PersistenceUse {

    @XmlElement(name = "type-identifier", required = true)
    protected String typeIdentifier;
    @XmlElement(name = "type-version", required = true)
    protected String typeVersion;
    @XmlElement(name = "type-storage", required = true)
    protected String typeStorage;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the typeIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeIdentifier() {
        return typeIdentifier;
    }

    /**
     * Sets the value of the typeIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeIdentifier(String value) {
        this.typeIdentifier = value;
    }

    /**
     * Gets the value of the typeVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeVersion() {
        return typeVersion;
    }

    /**
     * Sets the value of the typeVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeVersion(String value) {
        this.typeVersion = value;
    }

    /**
     * Gets the value of the typeStorage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeStorage() {
        return typeStorage;
    }

    /**
     * Sets the value of the typeStorage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeStorage(String value) {
        this.typeStorage = value;
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
