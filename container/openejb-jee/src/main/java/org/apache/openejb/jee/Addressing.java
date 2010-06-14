
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addressingType", propOrder = {
    "enabled",
    "required",
    "responses"
})
public class Addressing {

    protected Boolean enabled;
    protected Boolean required;
    protected AddressingResponses responses;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean value) {
        this.required = value;
    }

    public AddressingResponses getResponses() {
        return responses;
    }

    public void setResponses(AddressingResponses value) {
        this.responses = value;
    }

}
