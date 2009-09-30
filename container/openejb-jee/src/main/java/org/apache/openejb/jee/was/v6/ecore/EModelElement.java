
package org.apache.openejb.jee.was.v6.ecore;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EModelElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EModelElement">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EObject">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="eAnnotations" type="{http://www.eclipse.org/emf/2002/Ecore}EAnnotation"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EModelElement", propOrder = {
    "eAnnotations"
})
public class EModelElement
    extends EObject
{

    protected List<EAnnotation> eAnnotations;

    /**
     * Gets the value of the eAnnotations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eAnnotations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEAnnotations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EAnnotation }
     * 
     * 
     */
    public List<EAnnotation> getEAnnotations() {
        if (eAnnotations == null) {
            eAnnotations = new ArrayList<EAnnotation>();
        }
        return this.eAnnotations;
    }

}
