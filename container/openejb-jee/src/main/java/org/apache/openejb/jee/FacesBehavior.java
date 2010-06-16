
package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 *         The "behavior" element represents a concrete Behavior 
 *         implementation class that should be registered under the
 *         specified behavior identifier.  Behavior identifiers must
 *         be unique within the entire web application.
 *         
 *         Nested "attribute" elements identify generic attributes that
 *         may be configured on the corresponding UIComponent in order
 *         to affect the operation of the Behavior.  Nested "property"
 *         elements identify JavaBeans properties of the Behavior 
 *         implementation class that may be configured to affect the
 *         operation of the Behavior.  "attribute" and "property"
 *         elements are intended to allow component developers to
 *         more completely describe their components to tools and users.
 *         These elements have no required runtime semantics.
 *         
 *       
 * 
 * <p>Java class for faces-config-behaviorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="faces-config-behaviorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="behavior-id" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;element name="behavior-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="attribute" type="{http://java.sun.com/xml/ns/javaee}faces-config-attributeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="property" type="{http://java.sun.com/xml/ns/javaee}faces-config-propertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="behavior-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-behavior-extensionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-behaviorType", propOrder = {
    "description",
    "displayName",
    "icon",
    "behaviorId",
    "behaviorClass",
    "attribute",
    "property",
    "behaviorExtension"
})
public class FacesBehavior {

    protected List<String> description;
    @XmlElement(name = "display-name")
    protected List<String> displayName;
    protected List<Icon> icon;
    @XmlElement(name = "behavior-id", required = true)
    protected String behaviorId;
    @XmlElement(name = "behavior-class", required = true)
    protected String behaviorClass;
    protected List<FacesAttribute> attribute;
    protected List<FacesProperty> property;
    @XmlElement(name = "behavior-extension")
    protected List<FacesBehaviorExtension> behaviorExtension;

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
     * {@link String }
     * 
     * 
     */
    public List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<String>();
        }
        return this.description;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDisplayName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<String>();
        }
        return this.displayName;
    }

    /**
     * Gets the value of the icon property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icon property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIcon().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Icon }
     * 
     * 
     */
    public List<Icon> getIcon() {
        if (icon == null) {
            icon = new ArrayList<Icon>();
        }
        return this.icon;
    }

    /**
     * Gets the value of the behaviorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBehaviorId() {
        return behaviorId;
    }

    /**
     * Sets the value of the behaviorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBehaviorId(String value) {
        this.behaviorId = value;
    }

    /**
     * Gets the value of the behaviorClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBehaviorClass() {
        return behaviorClass;
    }

    /**
     * Sets the value of the behaviorClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBehaviorClass(String value) {
        this.behaviorClass = value;
    }

    /**
     * Gets the value of the attribute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attribute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttribute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FacesAttribute }
     * 
     * 
     */
    public List<FacesAttribute> getAttribute() {
        if (attribute == null) {
            attribute = new ArrayList<FacesAttribute>();
        }
        return this.attribute;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
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
     * {@link FacesProperty }
     * 
     * 
     */
    public List<FacesProperty> getProperty() {
        if (property == null) {
            property = new ArrayList<FacesProperty>();
        }
        return this.property;
    }

    /**
     * Gets the value of the behaviorExtension property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the behaviorExtension property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBehaviorExtension().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FacesBehaviorExtension }
     * 
     * 
     */
    public List<FacesBehaviorExtension> getBehaviorExtension() {
        if (behaviorExtension == null) {
            behaviorExtension = new ArrayList<FacesBehaviorExtension>();
        }
        return this.behaviorExtension;
    }

}
