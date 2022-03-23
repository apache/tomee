/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.oejb3;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "ejb-link")
public class EjbLink {

    @XmlAttribute(name = "deployment-id")
    protected String deployentId;

    @XmlAttribute(name = "ejb-ref-name")
    protected String ejbRefName;

    public EjbLink() {
    }

    public EjbLink(final String ejbRefName, final String deployentId) {
        this.ejbRefName = ejbRefName;
        this.deployentId = deployentId;
    }

    public String getDeployentId() {
        return deployentId;
    }

    public void setDeployentId(final String value) {
        this.deployentId = value;
    }

    public String getEjbRefName() {
        return ejbRefName;
    }

    public void setEjbRefName(final String value) {
        this.ejbRefName = value;
    }

}
