package org.apache.openejb.jee.bval;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for constraint-mappingsType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="constraint-mappingsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="default-package" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bean" type="{http://jboss.org/xml/ns/javax/validation/mapping}beanType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="constraint-definition" type="{http://jboss.org/xml/ns/javax/validation/mapping}constraint-definitionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "constraint-mappingsType", propOrder = {
    "defaultPackage",
    "bean",
    "constraintDefinition"
})
public class ConstraintMappingsType {
    @XmlElement(name = "default-package")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String defaultPackage;
    protected List<BeanType> bean;
    @XmlElement(name = "constraint-definition")
    protected List<ConstraintDefinitionType> constraintDefinition;

    /**
     * Gets the value of the defaultPackage property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDefaultPackage() {
        return defaultPackage;
    }

    /**
     * Sets the value of the defaultPackage property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDefaultPackage(String value) {
        this.defaultPackage = value;
    }

    /**
     * Gets the value of the bean property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bean property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBean().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BeanType }
     *
     *
     */
    public List<BeanType> getBean() {
        if (bean == null) {
            bean = new ArrayList<BeanType>();
        }
        return this.bean;
    }

    /**
     * Gets the value of the constraintDefinition property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the constraintDefinition property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConstraintDefinition().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConstraintDefinitionType }
     *
     *
     */
    public List<ConstraintDefinitionType> getConstraintDefinition() {
        if (constraintDefinition == null) {
            constraintDefinition = new ArrayList<ConstraintDefinitionType>();
        }
        return this.constraintDefinition;
    }

}

