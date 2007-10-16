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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "web",
    "passByReference",
    "uniqueId",
    "securityRoleMapping",
    "realm"
})
@XmlRootElement(name = "sun-application")
public class SunApplication {
    protected List<Web> web;
    @XmlElement(name = "pass-by-reference")
    protected String passByReference;
    @XmlElement(name = "unique-id")
    protected String uniqueId;
    @XmlElement(name = "security-role-mapping")
    protected List<SecurityRoleMapping> securityRoleMapping;
    protected String realm;

    public List<Web> getWeb() {
        if (web == null) {
            web = new ArrayList<Web>();
        }
        return this.web;
    }

    public String getPassByReference() {
        return passByReference;
    }

    public void setPassByReference(String value) {
        this.passByReference = value;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String value) {
        this.uniqueId = value;
    }

    public List<SecurityRoleMapping> getSecurityRoleMapping() {
        if (securityRoleMapping == null) {
            securityRoleMapping = new ArrayList<SecurityRoleMapping>();
        }
        return this.securityRoleMapping;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String value) {
        this.realm = value;
    }
}
