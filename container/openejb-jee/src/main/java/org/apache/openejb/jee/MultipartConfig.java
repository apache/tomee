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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * web-common_3_0.xsd
 *
 * <p>Java class for multipart-configType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="multipart-configType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="location" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="max-file-size" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/&gt;
 *         &lt;element name="max-request-size" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/&gt;
 *         &lt;element name="file-size-threshold" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

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
    protected long maxFileSize = -1L;
    @XmlElement(name = "max-request-size")
    protected long maxRequestSize = -1L;
    @XmlElement(name = "file-size-threshold")
    protected int fileSizeThreshold;

    public String getLocation() {
        return location;
    }

    public void setLocation(final String value) {
        this.location = value;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(final long value) {
        this.maxFileSize = value;
    }

    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(final long value) {
        this.maxRequestSize = value;
    }

    public int getFileSizeThreshold() {
        return fileSizeThreshold;
    }

    public void setFileSizeThreshold(final int value) {
        this.fileSizeThreshold = value;
    }

}
