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
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "uniqueId",
    "ejb",
    "pmDescriptors",
    "cmpResource",
    "messageDestination",
    "webserviceDescription"
})
public class EnterpriseBeans {
    protected String name;
    @XmlElement(name = "unique-id")
    protected String uniqueId;
    protected List<Ejb> ejb;
    @XmlElement(name = "pm-descriptors")
    protected PmDescriptors pmDescriptors;
    @XmlElement(name = "cmp-resource")
    protected CmpResource cmpResource;
    @XmlElement(name = "message-destination")
    protected List<MessageDestination> messageDestination;
    @XmlElement(name = "webservice-description")
    protected List<WebserviceDescription> webserviceDescription;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String value) {
        this.uniqueId = value;
    }

    public List<Ejb> getEjb() {
        if (ejb == null) {
            ejb = new ArrayList<Ejb>();
        }
        return this.ejb;
    }

    public PmDescriptors getPmDescriptors() {
        return pmDescriptors;
    }

    public void setPmDescriptors(PmDescriptors value) {
        this.pmDescriptors = value;
    }

    public CmpResource getCmpResource() {
        return cmpResource;
    }

    public void setCmpResource(CmpResource value) {
        this.cmpResource = value;
    }

    public List<MessageDestination> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestination>();
        }
        return this.messageDestination;
    }

    public List<WebserviceDescription> getWebserviceDescription() {
        if (webserviceDescription == null) {
            webserviceDescription = new ArrayList<WebserviceDescription>();
        }
        return this.webserviceDescription;
    }
}
