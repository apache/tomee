
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * class that combines the access-timeoutType and session-timeoutType xml types which have the same structure.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "access-timeoutType", propOrder = {
    "timeout",
    "unit"
})
public class Timeout {

    @XmlElement(required = true)
    protected long timeout;
    @XmlElement(required = true)
    protected TimeUnitType unit;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long value) {
        this.timeout = value;
    }

    public TimeUnitType getUnit() {
        return unit;
    }

    public void setUnit(TimeUnitType value) {
        this.unit = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

}
