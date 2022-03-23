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
package org.apache.openejb.jee.oejb2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "securityType", namespace = "http://geronimo.apache.org/xml/ns/security-2.0", propOrder = {
    "description", "credentialStoreRef", "defaultSubject", "roleMappings"
})
public class SecurityType extends AbstractSecurityType {
    @XmlElement(name = "description", namespace = "http://geronimo.apache.org/xml/ns/security-2.0")
    protected List<Object> description;
    @XmlElement(name = "credential-store-ref", namespace = "http://geronimo.apache.org/xml/ns/security-2.0")
    protected Object credentialStoreRef;
    @XmlElement(name = "default-subject", namespace = "http://geronimo.apache.org/xml/ns/security-2.0")
    protected Object defaultSubject;
    @XmlElement(name = "role-mappings", namespace = "http://geronimo.apache.org/xml/ns/security-2.0")
    protected Object roleMappings;
}
