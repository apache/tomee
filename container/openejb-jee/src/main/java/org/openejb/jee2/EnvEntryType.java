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
public class EnvEntryType {

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
    protected List<InjectionTargetType> injectionTarget;
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

    /**
     * Gets the value of the injectionTarget property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the injectionTarget property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getInjectionTarget().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link InjectionTargetType }
     */
    public List<InjectionTargetType> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTargetType>();
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
