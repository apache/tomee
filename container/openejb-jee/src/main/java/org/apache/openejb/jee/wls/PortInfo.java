
package org.apache.openejb.jee.wls;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for port-info complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="port-info">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="port-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="stub-property" type="{http://www.bea.com/ns/weblogic/90}property-namevalue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="call-property" type="{http://www.bea.com/ns/weblogic/90}property-namevalue" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "port-info", propOrder = {
    "portName",
    "stubProperty",
    "callProperty"
})
public class PortInfo {

    @XmlElement(name = "port-name", required = true)
    protected String portName;
    @XmlElement(name = "stub-property")
    protected List<PropertyNamevalue> stubProperty;
    @XmlElement(name = "call-property")
    protected List<PropertyNamevalue> callProperty;

    /**
     * Gets the value of the portName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPortName() {
        return portName;
    }

    /**
     * Sets the value of the portName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPortName(String value) {
        this.portName = value;
    }

    /**
     * Gets the value of the stubProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stubProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStubProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyNamevalue }
     * 
     * 
     */
    public List<PropertyNamevalue> getStubProperty() {
        if (stubProperty == null) {
            stubProperty = new ArrayList<PropertyNamevalue>();
        }
        return this.stubProperty;
    }

    /**
     * Gets the value of the callProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the callProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCallProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyNamevalue }
     * 
     * 
     */
    public List<PropertyNamevalue> getCallProperty() {
        if (callProperty == null) {
            callProperty = new ArrayList<PropertyNamevalue>();
        }
        return this.callProperty;
    }

}
