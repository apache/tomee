
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
@XmlType(name = "async-methodType", propOrder = {
    "methodName",
    "methodParams"
})
public class AsyncMethod {

    @XmlElement(name = "method-name", required = true)
    protected String methodName;
    @XmlElement(name = "method-params")
    protected MethodParams methodParams;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String value) {
        this.methodName = value;
    }

    public MethodParams getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(MethodParams value) {
        this.methodParams = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

}
