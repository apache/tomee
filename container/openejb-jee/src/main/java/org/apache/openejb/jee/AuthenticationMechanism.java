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
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * The authentication-mechanismType specifies an authentication
 * mechanism supported by the resource adapter. Note that this
 * support is for the resource adapter and not for the
 * underlying EIS instance. The optional description specifies
 * any resource adapter specific requirement for the support of
 * security contract and authentication mechanism.
 * <p/>
 * Note that BasicPassword mechanism type should support the
 * javax.resource.spi.security.PasswordCredential interface.
 * The Kerbv5 mechanism type should support the
 * org.ietf.jgss.GSSCredential interface or the deprecated
 * javax.resource.spi.security.GenericCredential interface.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "authentication-mechanismType", propOrder = {
    "description",
    "authenticationMechanismType",
    "credentialInterface"
})
public class AuthenticationMechanism {

    protected List<Text> description;
    @XmlElement(name = "authentication-mechanism-type", required = true)
    protected String authenticationMechanismType;
    @XmlElement(name = "credential-interface", required = true)
    protected String credentialInterface;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getAuthenticationMechanismType() {
        return authenticationMechanismType;
    }

    public void setAuthenticationMechanismType(String value) {
        this.authenticationMechanismType = value;
    }

    public String getCredentialInterface() {
        return credentialInterface;
    }

    public void setCredentialInterface(String value) {
        this.credentialInterface = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
