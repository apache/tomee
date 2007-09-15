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
 * The adminobjectType specifies information about an
 * administered object.  Administered objects are specific to a
 * messaging style or message provider.  This contains
 * information on the Java type of the interface implemented by
 * an administered object, its Java class name and its
 * configuration properties.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminobjectType", propOrder = {
        "adminObjectInterface",
        "adminObjectClass",
        "configProperty"
})
public class AdminObject {

    @XmlElement(name = "adminobject-interface", required = true)
    protected String adminObjectInterface;
    @XmlElement(name = "adminobject-class", required = true)
    protected String adminObjectClass;
    @XmlElement(name = "config-property")
    protected List<ConfigProperty> configProperty;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getAdminObjectInterface() {
        return adminObjectInterface;
    }

    public void setAdminObjectInterface(String value) {
        this.adminObjectInterface = value;
    }

    public String getAdminObjectClass() {
        return adminObjectClass;
    }

    public void setAdminObjectClass(String value) {
        this.adminObjectClass = value;
    }

    public List<ConfigProperty> getConfigProperty() {
        if (configProperty == null) {
            configProperty = new ArrayList<ConfigProperty>();
        }
        return this.configProperty;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
