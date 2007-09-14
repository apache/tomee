/**
 *
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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "security-constraintType", propOrder = {
        "displayName",
        "webResourceCollection",
        "authConstraint",
        "userDataConstraint"
})
public class SecurityConstraint {

    @XmlElement(name = "display-name")
    protected List<String> displayName;
    @XmlElement(name = "web-resource-collection", required = true)
    protected List<WebResourceCollection> webResourceCollection;
    @XmlElement(name = "auth-constraint")
    protected AuthConstraint authConstraint;
    @XmlElement(name = "user-data-constraint")
    protected UserDataConstraint userDataConstraint;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<String> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<String>();
        }
        return this.displayName;
    }

    public List<WebResourceCollection> getWebResourceCollection() {
        if (webResourceCollection == null) {
            webResourceCollection = new ArrayList<WebResourceCollection>();
        }
        return this.webResourceCollection;
    }

    public AuthConstraint getAuthConstraint() {
        return authConstraint;
    }

    public void setAuthConstraint(AuthConstraint value) {
        this.authConstraint = value;
    }

    public UserDataConstraint getUserDataConstraint() {
        return userDataConstraint;
    }

    public void setUserDataConstraint(UserDataConstraint value) {
        this.userDataConstraint = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
