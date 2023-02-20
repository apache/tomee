
package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.openejb.jee.jba.JndiName;


/**
 * 
 * 
 *         Configuration of a ContextService.
 * 
 *       
 * 
 * <p>Java class for context-serviceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="context-serviceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" minOccurs="0"/&gt;
 *         &lt;element name="name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/&gt;
 *         &lt;element name="cleared" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="propagated" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="unchanged" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="property" type="{http://java.sun.com/xml/ns/javaee}propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "context-serviceType", propOrder = {
    "description",
    "name",
    "cleared",
    "propagated",
    "unchanged",
    "property"
})
public class ContextService implements Keyable<String>{

    @XmlElement
    protected Description description;
    @XmlElement(required = true)
    protected JndiName name;
    @XmlElement
    protected List<String> cleared;
    @XmlElement
    protected List<String> propagated;
    @XmlElement
    protected List<String> unchanged;
    @XmlElement
    protected List<Property> property;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link JndiName }
     *     
     */
    public JndiName getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link JndiName }
     *     
     */
    public void setName(JndiName value) {
        this.name = value;
    }

    /**
     * Gets the value of the cleared property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the cleared property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCleared().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCleared() {
        if (cleared == null) {
            cleared = new ArrayList<String>();
        }
        return this.cleared;
    }

    /**
     * Gets the value of the propagated property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the propagated property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropagated().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPropagated() {
        if (propagated == null) {
            propagated = new ArrayList<String>();
        }
        return this.propagated;
    }

    /**
     * Gets the value of the unchanged property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the unchanged property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUnchanged().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUnchanged() {
        if (unchanged == null) {
            unchanged = new ArrayList<String>();
        }
        return this.unchanged;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
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
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

    @Override
    public String getKey() {
        return this.getName().getvalue();
    }
}

