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
 * The resource-env-refType is used to define
 * resource-env-type elements.  It contains a declaration of a
 * Deployment Component's reference to an administered object
 * associated with a resource in the Deployment Component's
 * environment.  It consists of an optional description, the
 * resource environment reference name, and an optional
 * indication of the resource environment reference type
 * expected by the Deployment Component code.
 * <p/>
 * It also includes optional elements to define injection of
 * the named resource into fields or JavaBeans properties.
 * <p/>
 * The resource environment type must be supplied unless an
 * injection target is specified, in which case the type
 * of the target is used.  If both are specified, the type
 * must be assignment compatible with the type of the injection
 * target.
 * <p/>
 * Example:
 * <p/>
 * <resource-env-ref>
 * <resource-env-ref-name>jms/StockQueue
 * </resource-env-ref-name>
 * <resource-env-ref-type>javax.jms.Queue
 * </resource-env-ref-type>
 * </resource-env-ref>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-env-refType", propOrder = {
        "description",
        "resourceEnvRefName",
        "resourceEnvRefType",
        "mappedName",
        "injectionTarget"
        })
public class ResourceEnvRef implements JndiReference {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "resource-env-ref-name", required = true)
    protected String resourceEnvRefName;
    @XmlElement(name = "resource-env-ref-type")
    protected String resourceEnvRefType;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    public String getName() {
        return getResourceEnvRefName();
    }

    @XmlTransient
    public String getType() {
        return getResourceEnvRefType();
    }

    public void setName(String name) {
        setResourceEnvRefName(name);
    }

    public void setType(String type) {
        setResourceEnvRefType(type);
    }

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getResourceEnvRefName() {
        return resourceEnvRefName;
    }

    public void setResourceEnvRefName(String value) {
        this.resourceEnvRefName = value;
    }

    public String getResourceEnvRefType() {
        return resourceEnvRefType;
    }

    public void setResourceEnvRefType(String value) {
        this.resourceEnvRefType = value;
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
