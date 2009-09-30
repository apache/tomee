
package org.apache.openejb.jee.was.v6.java;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.was.v6.ecore.EParameter;


/**
 * <p>Java class for JavaParameter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="JavaParameter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EParameter">
 *       &lt;attribute name="final" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="parameterKind" type="{java.xmi}JavaParameterKind" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JavaParameter")
public class JavaParameter
    extends EParameter
{

    @XmlAttribute(name = "final")
    protected Boolean isFinal;
    @XmlAttribute
    protected JavaParameterKind parameterKind;

    /**
     * Gets the value of the isFinal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsFinal() {
        return isFinal;
    }

    /**
     * Sets the value of the isFinal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsFinal(Boolean value) {
        this.isFinal = value;
    }

    /**
     * Gets the value of the parameterKind property.
     * 
     * @return
     *     possible object is
     *     {@link JavaParameterKind }
     *     
     */
    public JavaParameterKind getParameterKind() {
        return parameterKind;
    }

    /**
     * Sets the value of the parameterKind property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaParameterKind }
     *     
     */
    public void setParameterKind(JavaParameterKind value) {
        this.parameterKind = value;
    }

}
