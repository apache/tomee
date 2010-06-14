
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
@XmlType(name = "cookie-configType", propOrder = {
    "name",
    "domain",
    "path",
    "comment",
    "httpOnly",
    "secure",
    "maxAge"
})
public class CookieConfig {

    protected String name;
    protected String domain;
    protected String path;
    protected String comment;
    @XmlElement(name = "http-only")
    protected boolean httpOnly;
    protected boolean secure;
    @XmlElement(name = "max-age")
    protected int maxAge;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        this.domain = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        this.path = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        this.comment = value;
    }

    public boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean value) {
        this.httpOnly = value;
    }

    public boolean getSecure() {
        return secure;
    }

    public void setSecure(boolean value) {
        this.secure = value;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int value) {
        this.maxAge = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

}
