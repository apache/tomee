
package org.apache.openejb.jee;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "multipart-configType", propOrder = {
    "location",
    "maxFileSize",
    "maxRequestSize",
    "fileSizeThreshold"
})
public class MultipartConfig {

    protected String location;
    @XmlElement(name = "max-file-size")
    protected Long maxFileSize;
    @XmlElement(name = "max-request-size")
    protected Long maxRequestSize;
    @XmlElement(name = "file-size-threshold")
    protected BigInteger fileSizeThreshold;

    public String getLocation() {
        return location;
    }

    public void setLocation(String value) {
        this.location = value;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long value) {
        this.maxFileSize = value;
    }

    public Long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(Long value) {
        this.maxRequestSize = value;
    }

    public BigInteger getFileSizeThreshold() {
        return fileSizeThreshold;
    }

    public void setFileSizeThreshold(BigInteger value) {
        this.fileSizeThreshold = value;
    }

}
