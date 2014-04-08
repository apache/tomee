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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "integrity",
    "confidentiality",
    "establishTrustInTarget",
    "establishTrustInClient"
})
public class TransportConfig {
    @XmlElement(required = true)
    protected String integrity;
    @XmlElement(required = true)
    protected String confidentiality;
    @XmlElement(name = "establish-trust-in-target", required = true)
    protected String establishTrustInTarget;
    @XmlElement(name = "establish-trust-in-client", required = true)
    protected String establishTrustInClient;

    public String getIntegrity() {
        return integrity;
    }

    public void setIntegrity(String value) {
        this.integrity = value;
    }

    public String getConfidentiality() {
        return confidentiality;
    }

    public void setConfidentiality(String value) {
        this.confidentiality = value;
    }

    public String getEstablishTrustInTarget() {
        return establishTrustInTarget;
    }

    public void setEstablishTrustInTarget(String value) {
        this.establishTrustInTarget = value;
    }

    public String getEstablishTrustInClient() {
        return establishTrustInClient;
    }

    public void setEstablishTrustInClient(String value) {
        this.establishTrustInClient = value;
    }
}
