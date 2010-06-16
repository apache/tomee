
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 *         The "client-behavior-renderer" element represents a concrete 
 *         ClientBehaviorRenderer implementation class that should be 
 *         registered under the specified behavior renderer type identifier,
 *         in the RenderKit associated with the parent "render-kit"
 *         element.  Client Behavior renderer type must be unique within the RenderKit
 *         associated with the parent "render-kit" element.
 *         
 *         Nested "attribute" elements identify generic component
 *         attributes that are recognized by this renderer.
 *         
 *       
 * 
 * <p>Java class for faces-config-client-behavior-rendererType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="faces-config-client-behavior-rendererType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="client-behavior-renderer-type" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;element name="client-behavior-renderer-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-client-behavior-rendererType", propOrder = {
    "clientBehaviorRendererType",
    "clientBehaviorRendererClass"
})
public class FacesClientBehaviorRenderer {

    @XmlElement(name = "client-behavior-renderer-type", required = true)
    protected String clientBehaviorRendererType;
    @XmlElement(name = "client-behavior-renderer-class", required = true)
    protected String clientBehaviorRendererClass;

    /**
     * Gets the value of the clientBehaviorRendererType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientBehaviorRendererType() {
        return clientBehaviorRendererType;
    }

    /**
     * Sets the value of the clientBehaviorRendererType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientBehaviorRendererType(String value) {
        this.clientBehaviorRendererType = value;
    }

    /**
     * Gets the value of the clientBehaviorRendererClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientBehaviorRendererClass() {
        return clientBehaviorRendererClass;
    }

    /**
     * Sets the value of the clientBehaviorRendererClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientBehaviorRendererClass(String value) {
        this.clientBehaviorRendererClass = value;
    }

}
