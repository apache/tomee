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
