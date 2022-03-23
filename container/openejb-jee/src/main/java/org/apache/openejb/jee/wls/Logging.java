/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee.wls;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for logging complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="logging"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="log-filename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="logging-enabled" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="rotation-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="number-of-files-limited" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="file-count" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="file-size-limit" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="rotate-log-on-startup" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="log-file-rotation-dir" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="rotation-time" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="file-time-span" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
     * @return possible object is
     * {@link String }
     */
    public String getLogFilename() {
        return logFilename;
    }

    /**
     * Sets the value of the logFilename property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogFilename(final String value) {
        this.logFilename = value;
    }

    /**
     * Gets the value of the loggingEnabled property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Sets the value of the loggingEnabled property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setLoggingEnabled(final Boolean value) {
        this.loggingEnabled = value;
    }

    /**
     * Gets the value of the rotationType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRotationType() {
        return rotationType;
    }

    /**
     * Sets the value of the rotationType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRotationType(final String value) {
        this.rotationType = value;
    }

    /**
     * Gets the value of the numberOfFilesLimited property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getNumberOfFilesLimited() {
        return numberOfFilesLimited;
    }

    /**
     * Sets the value of the numberOfFilesLimited property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setNumberOfFilesLimited(final Boolean value) {
        this.numberOfFilesLimited = value;
    }

    /**
     * Gets the value of the fileCount property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getFileCount() {
        return fileCount;
    }

    /**
     * Sets the value of the fileCount property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setFileCount(final BigInteger value) {
        this.fileCount = value;
    }

    /**
     * Gets the value of the fileSizeLimit property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getFileSizeLimit() {
        return fileSizeLimit;
    }

    /**
     * Sets the value of the fileSizeLimit property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setFileSizeLimit(final BigInteger value) {
        this.fileSizeLimit = value;
    }

    /**
     * Gets the value of the rotateLogOnStartup property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getRotateLogOnStartup() {
        return rotateLogOnStartup;
    }

    /**
     * Sets the value of the rotateLogOnStartup property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setRotateLogOnStartup(final Boolean value) {
        this.rotateLogOnStartup = value;
    }

    /**
     * Gets the value of the logFileRotationDir property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLogFileRotationDir() {
        return logFileRotationDir;
    }

    /**
     * Sets the value of the logFileRotationDir property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogFileRotationDir(final String value) {
        this.logFileRotationDir = value;
    }

    /**
     * Gets the value of the rotationTime property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRotationTime() {
        return rotationTime;
    }

    /**
     * Sets the value of the rotationTime property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRotationTime(final String value) {
        this.rotationTime = value;
    }

    /**
     * Gets the value of the fileTimeSpan property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getFileTimeSpan() {
        return fileTimeSpan;
    }

    /**
     * Sets the value of the fileTimeSpan property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setFileTimeSpan(final BigInteger value) {
        this.fileTimeSpan = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
