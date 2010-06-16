
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 *         Please see section JSF.11.4.6 for the specification of this element.
 *         
 *       
 * 
 * <p>Java class for faces-config-orderingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="faces-config-orderingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="after" type="{http://java.sun.com/xml/ns/javaee}faces-config-ordering-orderingType" minOccurs="0"/>
 *         &lt;element name="before" type="{http://java.sun.com/xml/ns/javaee}faces-config-ordering-orderingType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-orderingType", propOrder = {
    "after",
    "before"
})
public class FacesOrdering {

    protected FacesOrderingOrdering after;
    protected FacesOrderingOrdering before;

    /**
     * Gets the value of the after property.
     * 
     * @return
     *     possible object is
     *     {@link FacesOrderingOrdering }
     *     
     */
    public FacesOrderingOrdering getAfter() {
        return after;
    }

    /**
     * Sets the value of the after property.
     * 
     * @param value
     *     allowed object is
     *     {@link FacesOrderingOrdering }
     *     
     */
    public void setAfter(FacesOrderingOrdering value) {
        this.after = value;
    }

    /**
     * Gets the value of the before property.
     * 
     * @return
     *     possible object is
     *     {@link FacesOrderingOrdering }
     *     
     */
    public FacesOrderingOrdering getBefore() {
        return before;
    }

    /**
     * Sets the value of the before property.
     * 
     * @param value
     *     allowed object is
     *     {@link FacesOrderingOrdering }
     *     
     */
    public void setBefore(FacesOrderingOrdering value) {
        this.before = value;
    }

}
