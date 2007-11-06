
package org.apache.openejb.jee.wls;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for logging complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="logging">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="log-filename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="logging-enabled" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="rotation-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="number-of-files-limited" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="file-count" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="file-size-limit" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="rotate-log-on-startup" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="log-file-rotation-dir" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rotation-time" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="file-time-span" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "logging", propOrder = {
    "logFilename",
    "loggingEnabled",
    "rotationType",
    "numberOfFilesLimited",
    "fileCount",
    "fileSizeLimit",
    "rotateLogOnStartup",
    "logFileRotationDir",
    "rotationTime",
    "fileTimeSpan"
})
public class Logging {

    @XmlElement(name = "log-filename")
    protected String logFilename;
    @XmlElement(name = "logging-enabled")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean loggingEnabled;
    @XmlElement(name = "rotation-type")
    protected String rotationType;
    @XmlElement(name = "number-of-files-limited")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean numberOfFilesLimited;
    @XmlElement(name = "file-count")
    protected BigInteger fileCount;
    @XmlElement(name = "file-size-limit")
    protected BigInteger fileSizeLimit;
    @XmlElement(name = "rotate-log-on-startup")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean rotateLogOnStartup;
    @XmlElement(name = "log-file-rotation-dir")
    protected String logFileRotationDir;
    @XmlElement(name = "rotation-time")
    protected String rotationTime;
    @XmlElement(name = "file-time-span")
    protected BigInteger fileTimeSpan;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the logFilename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogFilename() {
        return logFilename;
    }

    /**
     * Sets the value of the logFilename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogFilename(String value) {
        this.logFilename = value;
    }

    /**
     * Gets the value of the loggingEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Sets the value of the loggingEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLoggingEnabled(Boolean value) {
        this.loggingEnabled = value;
    }

    /**
     * Gets the value of the rotationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRotationType() {
        return rotationType;
    }

    /**
     * Sets the value of the rotationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRotationType(String value) {
        this.rotationType = value;
    }

    /**
     * Gets the value of the numberOfFilesLimited property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getNumberOfFilesLimited() {
        return numberOfFilesLimited;
    }

    /**
     * Sets the value of the numberOfFilesLimited property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNumberOfFilesLimited(Boolean value) {
        this.numberOfFilesLimited = value;
    }

    /**
     * Gets the value of the fileCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFileCount() {
        return fileCount;
    }

    /**
     * Sets the value of the fileCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFileCount(BigInteger value) {
        this.fileCount = value;
    }

    /**
     * Gets the value of the fileSizeLimit property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFileSizeLimit() {
        return fileSizeLimit;
    }

    /**
     * Sets the value of the fileSizeLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFileSizeLimit(BigInteger value) {
        this.fileSizeLimit = value;
    }

    /**
     * Gets the value of the rotateLogOnStartup property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getRotateLogOnStartup() {
        return rotateLogOnStartup;
    }

    /**
     * Sets the value of the rotateLogOnStartup property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRotateLogOnStartup(Boolean value) {
        this.rotateLogOnStartup = value;
    }

    /**
     * Gets the value of the logFileRotationDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogFileRotationDir() {
        return logFileRotationDir;
    }

    /**
     * Sets the value of the logFileRotationDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogFileRotationDir(String value) {
        this.logFileRotationDir = value;
    }

    /**
     * Gets the value of the rotationTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRotationTime() {
        return rotationTime;
    }

    /**
     * Sets the value of the rotationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRotationTime(String value) {
        this.rotationTime = value;
    }

    /**
     * Gets the value of the fileTimeSpan property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFileTimeSpan() {
        return fileTimeSpan;
    }

    /**
     * Sets the value of the fileTimeSpan property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFileTimeSpan(BigInteger value) {
        this.fileTimeSpan = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
