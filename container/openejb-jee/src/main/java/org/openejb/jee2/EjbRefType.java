//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.07.08 at 06:01:06 PM PDT 
//


package org.openejb.jee2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 
 * 	The ejb-refType is used by ejb-ref elements for the
 * 	declaration of a reference to an enterprise bean's home or
 *         to the remote business interface of a 3.0 bean.
 * 	The declaration consists of:
 * 
 * 	    - an optional description
 * 	    - the EJB reference name used in the code of
 * 	      the Deployment Component that's referencing the enterprise
 * 	      bean.
 * 	    - the optional expected type of the referenced enterprise bean
 *             - the optional remote interface of the referenced enterprise bean
 *               or the remote business interface of the referenced enterprise
 *               bean
 * 	    - the optional expected home interface of the referenced
 *               enterprise bean.  Not applicable if this ejb-ref
 *               refers to the remote business interface of a 3.0 bean.
 * 	    - optional ejb-link information, used to specify the
 * 	      referenced enterprise bean
 *             - optional elements to define injection of the named enterprise
 *               bean into a component field or property
 * 
 *       
 * 
 * <p>Java class for ejb-refType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ejb-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-ref-name" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-nameType"/>
 *         &lt;element name="ejb-ref-type" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-typeType" minOccurs="0"/>
 *         &lt;element name="home" type="{http://java.sun.com/xml/ns/javaee}homeType" minOccurs="0"/>
 *         &lt;element name="remote" type="{http://java.sun.com/xml/ns/javaee}remoteType" minOccurs="0"/>
 *         &lt;element name="ejb-link" type="{http://java.sun.com/xml/ns/javaee}ejb-linkType" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/>
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
@XmlType(name = "ejb-refType", propOrder = {
    "description",
    "ejbRefName",
    "ejbRefType",
    "home",
    "remote",
    "ejbLink",
    "mappedName",
    "injectionTarget"
})
public class EjbRefType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "ejb-ref-name", required = true)
    protected String ejbRefName;
    @XmlElement(name = "ejb-ref-type")
    protected EjbRefTypeType ejbRefType;
    protected String home;
    protected String remote;
    @XmlElement(name = "ejb-link")
    protected String ejbLink;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTargetType> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     * 
     * 
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    /**
     * Gets the value of the ejbRefName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjbRefName() {
        return ejbRefName;
    }

    /**
     * 	  The ejb-ref-name element contains the name of an EJB
     * 	  reference. The EJB reference is an entry in the
     * 	  Deployment Component's environment and is relative to the
     * 	  java:comp/env context.  The name must be unique within the
     * 	  Deployment Component.
     *
     * 	  It is recommended that name is prefixed with "ejb/".
     *
     * 	  Example:
     *
     * 	  <ejb-ref-name>ejb/Payroll</ejb-ref-name>
     */
    public void setEjbRefName(String value) {
        this.ejbRefName = value;
    }

    /**
     * Gets the value of the ejbRefType property.
     * 
     * @return
     *     possible object is
     *     {@link EjbRefTypeType }
     *     
     */
    public EjbRefTypeType getEjbRefType() {
        return ejbRefType;
    }

    /**
     * Sets the value of the ejbRefType property.
     * 
     * @param value
     *     allowed object is
     *     {@link EjbRefTypeType }
     *     
     */
    public void setEjbRefType(EjbRefTypeType value) {
        this.ejbRefType = value;
    }

    /**
     * Gets the value of the home property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHome() {
        return home;
    }

    /**
     * Sets the value of the home property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHome(String value) {
        this.home = value;
    }

    /**
     * Gets the value of the remote property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemote() {
        return remote;
    }

    /**
     * Sets the value of the remote property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemote(String value) {
        this.remote = value;
    }

    /**
     * 	  The value of the ejb-link element must be the ejb-name of an
     * 	  enterprise bean in the same ejb-jar file or in another ejb-jar
     * 	  file in the same Java EE application unit.
     *
     * 	  Alternatively, the name in the ejb-link element may be
     * 	  composed of a path name specifying the ejb-jar containing the
     * 	  referenced enterprise bean with the ejb-name of the target
     * 	  bean appended and separated from the path name by "#".  The
     * 	  path name is relative to the Deployment File containing
     * 	  Deployment Component that is referencing the enterprise
     * 	  bean.  This allows multiple enterprise beans with the same
     * 	  ejb-name to be uniquely identified.
     *
     * 	  Examples:
     *
     * 	      <ejb-link>EmployeeRecord</ejb-link>
     *
     * 	      <ejb-link>../products/product.jar#ProductEJB</ejb-link>
     *
     */
    public String getEjbLink() {
        return ejbLink;
    }

    /**
     * Sets the value of the ejbLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEjbLink(String value) {
        this.ejbLink = value;
    }

    /**
     * Gets the value of the mappedName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMappedName() {
        return mappedName;
    }

    /**
     * Sets the value of the mappedName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMappedName(String value) {
        this.mappedName = value;
    }

    /**
     * Gets the value of the injectionTarget property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the injectionTarget property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInjectionTarget().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InjectionTargetType }
     * 
     * 
     */
    public List<InjectionTargetType> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTargetType>();
        }
        return this.injectionTarget;
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
