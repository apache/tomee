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
    "securityRoleMapping",
    "enterpriseBeans"
})
@XmlRootElement(name = "sun-ejb-jar")
public class SunEjbJar {
    @XmlElement(name = "security-role-mapping")
    protected List<SecurityRoleMapping> securityRoleMapping;
    @XmlElement(name = "enterprise-beans", required = true)
    protected EnterpriseBeans enterpriseBeans;

    public List<SecurityRoleMapping> getSecurityRoleMapping() {
        if (securityRoleMapping == null) {
            securityRoleMapping = new ArrayList<SecurityRoleMapping>();
        }
        return this.securityRoleMapping;
    }

    public EnterpriseBeans getEnterpriseBeans() {
        return enterpriseBeans;
    }

    public void setEnterpriseBeans(EnterpriseBeans value) {
        this.enterpriseBeans = value;
    }
}
