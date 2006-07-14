/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

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
        "description",
        "roleName",
        "unchecked",
        "method"
        })
public class MethodPermission {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "role-name", required = true)
    protected List<String> roleName;
    protected EmptyType unchecked;
    @XmlElement(required = true)
    protected List<Method> method;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDescription().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    /**
     * Gets the value of the roleName property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the roleName property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getRoleName().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getRoleName() {
        if (roleName == null) {
            roleName = new ArrayList<String>();
        }
        return this.roleName;
    }

    public EmptyType getUnchecked() {
        return unchecked;
    }

    public void setUnchecked(EmptyType value) {
        this.unchecked = value;
    }

    /**
     * Gets the value of the method property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the method property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getMethod().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Method }
     */
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
