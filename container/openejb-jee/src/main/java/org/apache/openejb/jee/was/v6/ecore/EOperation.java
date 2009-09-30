
package org.apache.openejb.jee.was.v6.ecore;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EOperation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EOperation">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}ETypedElement">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="eParameters" type="{http://www.eclipse.org/emf/2002/Ecore}EParameter"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="eExceptions" type="{http://www.eclipse.org/emf/2002/Ecore}EClassifier"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attribute name="eExceptions" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EOperation", propOrder = {
    "eParameters",
    "eClassifierEExceptions"
})
public class EOperation
    extends ETypedElement
{

    protected List<EParameter> eParameters;
    @XmlElement(name = "eExceptions")
    protected List<EClassifier> eClassifierEExceptions;
    @XmlAttribute
    protected String eExceptions;

    /**
     * Gets the value of the eParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EParameter }
     * 
     * 
     */
    public List<EParameter> getEParameters() {
        if (eParameters == null) {
            eParameters = new ArrayList<EParameter>();
        }
        return this.eParameters;
    }

    /**
     * Gets the value of the eClassifierEExceptions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eClassifierEExceptions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEClassifierEExceptions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EClassifier }
     * 
     * 
     */
    public List<EClassifier> getEClassifierEExceptions() {
        if (eClassifierEExceptions == null) {
            eClassifierEExceptions = new ArrayList<EClassifier>();
        }
        return this.eClassifierEExceptions;
    }

    /**
     * Gets the value of the eExceptions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEExceptions() {
        return eExceptions;
    }

    /**
     * Sets the value of the eExceptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEExceptions(String value) {
        this.eExceptions = value;
    }

}
