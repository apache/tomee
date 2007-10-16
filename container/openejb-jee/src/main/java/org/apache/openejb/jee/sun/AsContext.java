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
    "authMethod",
    "realm",
    "required"
})
public class AsContext {
    @XmlElement(name = "auth-method", required = true)
    protected String authMethod;
    @XmlElement(required = true)
    protected String realm;
    @XmlElement(required = true)
    protected String required;

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String value) {
        this.authMethod = value;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String value) {
        this.realm = value;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String value) {
        this.required = value;
    }
}
