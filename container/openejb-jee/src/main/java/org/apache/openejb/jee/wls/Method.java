
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
 * <p>Java class for method complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="method">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.bea.com/ns/weblogic/90}description" minOccurs="0"/>
 *         &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="method-intf" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="method-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="method-params" type="{http://www.bea.com/ns/weblogic/90}method-params" minOccurs="0"/>
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
@XmlType(name = "method", propOrder = {
    "description",
    "ejbName",
    "methodIntf",
    "methodName",
    "methodParams"
})
public class Method {

    protected Description description;
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "method-intf")
    protected String methodIntf;
    @XmlElement(name = "method-name", required = true)
    protected String methodName;
    @XmlElement(name = "method-params")
    protected MethodParams methodParams;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

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
     * Gets the value of the ejbName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEjbName(String value) {
        this.ejbName = value;
    }

    /**
     * Gets the value of the methodIntf property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethodIntf() {
        return methodIntf;
    }

    /**
     * Sets the value of the methodIntf property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethodIntf(String value) {
        this.methodIntf = value;
    }

    /**
     * Gets the value of the methodName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the value of the methodName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethodName(String value) {
        this.methodName = value;
    }

    /**
     * Gets the value of the methodParams property.
     * 
     * @return
     *     possible object is
     *     {@link MethodParams }
     *     
     */
    public MethodParams getMethodParams() {
        return methodParams;
    }

    /**
     * Sets the value of the methodParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodParams }
     *     
     */
    public void setMethodParams(MethodParams value) {
        this.methodParams = value;
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
