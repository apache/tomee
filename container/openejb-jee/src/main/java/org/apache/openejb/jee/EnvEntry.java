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
 * The env-entryType is used to declare an application's
 * environment entry. The declaration consists of an optional
 * description, the name of the environment entry, a type
 * (optional if the value is injected, otherwise required), and
 * an optional value.
 * <p/>
 * It also includes optional elements to define injection of
 * the named resource into fields or JavaBeans properties.
 * <p/>
 * If a value is not specified and injection is requested,
 * no injection will occur and no entry of the specified name
 * will be created.  This allows an initial value to be
 * specified in the source code without being incorrectly
 * changed when no override has been specified.
 * <p/>
 * If a value is not specified and no injection is requested,
 * a value must be supplied during deployment.
 * <p/>
 * This type is used by env-entry elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "env-entryType", propOrder = {
        "description",
        "envEntryName",
        "envEntryType",
        "envEntryValue",
        "mappedName",
        "injectionTarget"
        })
public class EnvEntry implements JndiReference {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "env-entry-name", required = true)
    protected String envEntryName;
    @XmlElement(name = "env-entry-type")
    protected String envEntryType;
    @XmlElement(name = "env-entry-value")
    protected String envEntryValue;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public EnvEntry() {
    }

    public EnvEntry(String envEntryName, String envEntryType, String envEntryValue) {
        this.envEntryName = envEntryName;
        this.envEntryType = envEntryType;
        this.envEntryValue = envEntryValue;
    }

    @XmlTransient
    public String getName() {
        return getEnvEntryName();
    }

    @XmlTransient
    public String getType() {
        return getEnvEntryType();
    }

    public void setName(String name) {
        setEnvEntryName(name);
    }

    public void setType(String type) {
        setEnvEntryType(type);
    }

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getEnvEntryName() {
        return envEntryName;
    }

    public void setEnvEntryName(String value) {
        this.envEntryName = value;
    }

    /**
     * Gets the value of the envEntryType property.
     */
    public String getEnvEntryType() {
        return envEntryType;
    }

    public void setEnvEntryType(String value) {
        this.envEntryType = value;
    }

    public String getEnvEntryValue() {
        return envEntryValue;
    }

    public void setEnvEntryValue(String value) {
        this.envEntryValue = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    public List<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTarget>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
