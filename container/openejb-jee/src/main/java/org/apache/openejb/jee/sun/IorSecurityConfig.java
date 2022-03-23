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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "transportConfig",
    "asContext",
    "sasContext"
})
public class IorSecurityConfig {
    @XmlElement(name = "transport-config")
    protected TransportConfig transportConfig;
    @XmlElement(name = "as-context")
    protected AsContext asContext;
    @XmlElement(name = "sas-context")
    protected SasContext sasContext;

    public TransportConfig getTransportConfig() {
        return transportConfig;
    }

    public void setTransportConfig(final TransportConfig value) {
        this.transportConfig = value;
    }

    public AsContext getAsContext() {
        return asContext;
    }

    public void setAsContext(final AsContext value) {
        this.asContext = value;
    }

    public SasContext getSasContext() {
        return sasContext;
    }

    public void setSasContext(final SasContext value) {
        this.sasContext = value;
    }
}
