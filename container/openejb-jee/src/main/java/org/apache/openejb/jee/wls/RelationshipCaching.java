
package org.apache.openejb.jee.wls;

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
 * <p>Java class for relationship-caching complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="relationship-caching">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="caching-name" type="{http://www.bea.com/ns/weblogic/90}caching-name"/>
 *         &lt;element name="caching-element" type="{http://www.bea.com/ns/weblogic/90}caching-element" maxOccurs="unbounded"/>
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
@XmlType(name = "relationship-caching", propOrder = {
    "cachingName",
    "cachingElement"
})
public class RelationshipCaching {

    @XmlElement(name = "caching-name", required = true)
    protected CachingName cachingName;
    @XmlElement(name = "caching-element", required = true)
    protected List<CachingElement> cachingElement;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the cachingName property.
     * 
     * @return
     *     possible object is
     *     {@link CachingName }
     *     
     */
    public CachingName getCachingName() {
        return cachingName;
    }

    /**
     * Sets the value of the cachingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link CachingName }
     *     
     */
    public void setCachingName(CachingName value) {
        this.cachingName = value;
    }

    /**
     * Gets the value of the cachingElement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cachingElement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCachingElement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CachingElement }
     * 
     * 
     */
    public List<CachingElement> getCachingElement() {
        if (cachingElement == null) {
            cachingElement = new ArrayList<CachingElement>();
        }
        return this.cachingElement;
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
