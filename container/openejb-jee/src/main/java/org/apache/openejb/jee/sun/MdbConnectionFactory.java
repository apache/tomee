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
@XmlType(name = "", propOrder = {"jndiName", "defaultResourcePrincipal"})
public class MdbConnectionFactory {
    @XmlElement(name = "jndi-name", required = true)
    protected String jndiName;
    @XmlElement(name = "default-resource-principal")
    protected DefaultResourcePrincipal defaultResourcePrincipal;

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(final String value) {
        this.jndiName = value;
    }

    public DefaultResourcePrincipal getDefaultResourcePrincipal() {
        return defaultResourcePrincipal;
    }

    public void setDefaultResourcePrincipal(final DefaultResourcePrincipal value) {
        this.defaultResourcePrincipal = value;
    }
}
