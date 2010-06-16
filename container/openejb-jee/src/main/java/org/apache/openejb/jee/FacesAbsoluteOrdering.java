
package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 *         Only relevant if this is placed within the /WEB-INF/faces-config.xml.
 *         Please see section JSF.11.4.6 for the specification for details.
 *         
 *       
 * 
 * <p>Java class for faces-config-absoluteOrderingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="faces-config-absoluteOrderingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="others" type="{http://java.sun.com/xml/ns/javaee}faces-config-ordering-othersType" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-absoluteOrderingType", propOrder = {
    "nameOrOthers"
})
public class FacesAbsoluteOrdering {

    @XmlElements({
        @XmlElement(name = "others", type = FacesOrderingOthers.class),
        @XmlElement(name = "name", type = String.class)
    })
    protected List<Object> nameOrOthers;

    /**
     * Gets the value of the nameOrOthers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameOrOthers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNameOrOthers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FacesOrderingOthers }
     * {@link String }
     * 
     * 
     */
    public List<Object> getNameOrOthers() {
        if (nameOrOthers == null) {
            nameOrOthers = new ArrayList<Object>();
        }
        return this.nameOrOthers;
    }

}
