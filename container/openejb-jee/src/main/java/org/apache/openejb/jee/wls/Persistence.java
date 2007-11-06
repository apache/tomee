
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
 * <p>Java class for persistence complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="persistence">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="is-modified-method-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="delay-updates-until-end-of-tx" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="finders-load-bean" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="persistence-use" type="{http://www.bea.com/ns/weblogic/90}persistence-use" minOccurs="0"/>
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
@XmlType(name = "persistence", propOrder = {
    "isModifiedMethodName",
    "delayUpdatesUntilEndOfTx",
    "findersLoadBean",
    "persistenceUse"
})
public class Persistence {

    @XmlElement(name = "is-modified-method-name")
    protected String isModifiedMethodName;
    @XmlElement(name = "delay-updates-until-end-of-tx")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean delayUpdatesUntilEndOfTx;
    @XmlElement(name = "finders-load-bean")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean findersLoadBean;
    @XmlElement(name = "persistence-use")
    protected PersistenceUse persistenceUse;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the isModifiedMethodName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsModifiedMethodName() {
        return isModifiedMethodName;
    }

    /**
     * Sets the value of the isModifiedMethodName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsModifiedMethodName(String value) {
        this.isModifiedMethodName = value;
    }

    /**
     * Gets the value of the delayUpdatesUntilEndOfTx property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getDelayUpdatesUntilEndOfTx() {
        return delayUpdatesUntilEndOfTx;
    }

    /**
     * Sets the value of the delayUpdatesUntilEndOfTx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDelayUpdatesUntilEndOfTx(Boolean value) {
        this.delayUpdatesUntilEndOfTx = value;
    }

    /**
     * Gets the value of the findersLoadBean property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getFindersLoadBean() {
        return findersLoadBean;
    }

    /**
     * Sets the value of the findersLoadBean property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFindersLoadBean(Boolean value) {
        this.findersLoadBean = value;
    }

    /**
     * Gets the value of the persistenceUse property.
     * 
     * @return
     *     possible object is
     *     {@link PersistenceUse }
     *     
     */
    public PersistenceUse getPersistenceUse() {
        return persistenceUse;
    }

    /**
     * Sets the value of the persistenceUse property.
     * 
     * @param value
     *     allowed object is
     *     {@link PersistenceUse }
     *     
     */
    public void setPersistenceUse(PersistenceUse value) {
        this.persistenceUse = value;
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
