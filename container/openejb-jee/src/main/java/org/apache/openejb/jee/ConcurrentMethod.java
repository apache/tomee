
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


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "concurrent-methodType", propOrder = {
    "method",
    "lock",
    "accessTimeout"
})
public class ConcurrentMethod  {

    @XmlElement(required = true)
    protected NamedMethod method;
    protected ConcurrentLockType lock;
    @XmlElement(name = "access-timeout")
    protected Timeout accessTimeout;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public NamedMethod getMethod() {
        return method;
    }

    public void setMethod(NamedMethod value) {
        this.method = value;
    }

    public ConcurrentLockType getLock() {
        return lock;
    }

    public void setLock(ConcurrentLockType value) {
        this.lock = value;
    }

    public Timeout getAccessTimeout() {
        return accessTimeout;
    }

    public void setAccessTimeout(Timeout value) {
        this.accessTimeout = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

}
