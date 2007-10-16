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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class IdempotentUrlPattern {
    @XmlAttribute(name = "url-pattern", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String urlPattern;
    @XmlAttribute(name = "num-of-retries")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String numOfRetries;

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String value) {
        this.urlPattern = value;
    }

    public String getNumOfRetries() {
        if (numOfRetries == null) {
            return "-1";
        } else {
            return numOfRetries;
        }
    }

    public void setNumOfRetries(String value) {
        this.numOfRetries = value;
    }
}
