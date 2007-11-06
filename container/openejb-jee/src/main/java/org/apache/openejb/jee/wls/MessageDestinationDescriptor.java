
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
 * <p>Java class for message-destination-descriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="message-destination-descriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message-destination-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;element name="destination-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *             &lt;element name="initial-context-factory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *             &lt;element name="provider-url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;/sequence>
 *           &lt;element name="destination-resource-link" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
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
@XmlType(name = "message-destination-descriptor", propOrder = {
    "messageDestinationName",
    "destinationJndiName",
    "initialContextFactory",
    "providerUrl",
    "destinationResourceLink"
})
public class MessageDestinationDescriptor {

    @XmlElement(name = "message-destination-name", required = true)
    protected String messageDestinationName;
    @XmlElement(name = "destination-jndi-name")
    protected String destinationJndiName;
    @XmlElement(name = "initial-context-factory")
    protected String initialContextFactory;
    @XmlElement(name = "provider-url")
    protected String providerUrl;
    @XmlElement(name = "destination-resource-link")
    protected String destinationResourceLink;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the messageDestinationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageDestinationName() {
        return messageDestinationName;
    }

    /**
     * Sets the value of the messageDestinationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageDestinationName(String value) {
        this.messageDestinationName = value;
    }

    /**
     * Gets the value of the destinationJndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationJndiName() {
        return destinationJndiName;
    }

    /**
     * Sets the value of the destinationJndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationJndiName(String value) {
        this.destinationJndiName = value;
    }

    /**
     * Gets the value of the initialContextFactory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    /**
     * Sets the value of the initialContextFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInitialContextFactory(String value) {
        this.initialContextFactory = value;
    }

    /**
     * Gets the value of the providerUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderUrl() {
        return providerUrl;
    }

    /**
     * Sets the value of the providerUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderUrl(String value) {
        this.providerUrl = value;
    }

    /**
     * Gets the value of the destinationResourceLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationResourceLink() {
        return destinationResourceLink;
    }

    /**
     * Sets the value of the destinationResourceLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationResourceLink(String value) {
        this.destinationResourceLink = value;
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
