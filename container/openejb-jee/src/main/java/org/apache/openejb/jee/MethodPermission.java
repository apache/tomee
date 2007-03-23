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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The method-permissionType specifies that one or more
 * security roles are allowed to invoke one or more enterprise
 * bean methods. The method-permissionType consists of an
 * optional description, a list of security role names or an
 * indicator to state that the method is unchecked for
 * authorization, and a list of method elements.
 * <p/>
 * The security roles used in the method-permissionType
 * must be defined in the security-role elements of the
 * deployment descriptor, and the methods must be methods
 * defined in the enterprise bean's business, home, component
 * and/or web service endpoint interfaces.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "method-permissionType", propOrder = {
        "descriptions",
        "roleName",
        "unchecked",
        "method"
        })
public class MethodPermission {

    @XmlElement(name = "role-name", required = true)
    protected List<String> roleName;
    protected EmptyType unchecked;
    @XmlElement(required = true)
    protected List<Method> method;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    protected TextMap description = new TextMap();

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public List<String> getRoleName() {
        if (roleName == null) {
            roleName = new ArrayList<String>();
        }
        return this.roleName;
    }

    public boolean getUnchecked() {
        return unchecked != null;
    }

    public void setUnchecked(boolean b) {
        this.unchecked = (b) ? new EmptyType() : null;
    }

    public List<Method> getMethod() {
        if (method == null) {
            method = new ArrayList<Method>();
        }
        return this.method;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
