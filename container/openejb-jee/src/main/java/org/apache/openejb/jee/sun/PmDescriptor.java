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
    "pmIdentifier",
    "pmVersion",
    "pmConfig",
    "pmClassGenerator",
    "pmMappingFactory"
})
public class PmDescriptor {
    @XmlElement(name = "pm-identifier", required = true)
    protected String pmIdentifier;
    @XmlElement(name = "pm-version", required = true)
    protected String pmVersion;
    @XmlElement(name = "pm-config")
    protected String pmConfig;
    @XmlElement(name = "pm-class-generator")
    protected String pmClassGenerator;
    @XmlElement(name = "pm-mapping-factory")
    protected String pmMappingFactory;

    public String getPmIdentifier() {
        return pmIdentifier;
    }

    public void setPmIdentifier(final String value) {
        this.pmIdentifier = value;
    }

    public String getPmVersion() {
        return pmVersion;
    }

    public void setPmVersion(final String value) {
        this.pmVersion = value;
    }

    public String getPmConfig() {
        return pmConfig;
    }

    public void setPmConfig(final String value) {
        this.pmConfig = value;
    }

    public String getPmClassGenerator() {
        return pmClassGenerator;
    }

    public void setPmClassGenerator(final String value) {
        this.pmClassGenerator = value;
    }

    public String getPmMappingFactory() {
        return pmMappingFactory;
    }

    public void setPmMappingFactory(final String value) {
        this.pmMappingFactory = value;
    }
}
