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
 * The resource-refType contains a declaration of a
 * Deployment Component's reference to an external resource. It
 * consists of an optional description, the resource manager
 * connection factory reference name, an optional indication of
 * the resource manager connection factory type expected by the
 * Deployment Component code, an optional type of authentication
 * (Application or Container), and an optional specification of
 * the shareability of connections obtained from the resource
 * (Shareable or Unshareable).
 * <p/>
 * It also includes optional elements to define injection of
 * the named resource into fields or JavaBeans properties.
 * <p/>
 * The connection factory type must be supplied unless an
 * injection target is specified, in which case the type
 * of the target is used.  If both are specified, the type
 * must be assignment compatible with the type of the injection
 * target.
 * <p/>
 * Example:
 * <p/>
 * <resource-ref>
 * <res-ref-name>jdbc/EmployeeAppDB</res-ref-name>
 * <res-type>javax.sql.DataSource</res-type>
 * <res-auth>Container</res-auth>
 * <res-sharing-scope>Shareable</res-sharing-scope>
 * </resource-ref>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-refType", propOrder = {
        "description",
        "resRefName",
        "resType",
        "resAuth",
        "resSharingScope",
        "mappedName",
        "injectionTarget"
        })
public class ResourceRef {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "res-ref-name", required = true)
    protected String resRefName;
    @XmlElement(name = "res-type")
    protected String resType;
    @XmlElement(name = "res-auth")
    protected ResAuth resAuth;
    @XmlElement(name = "res-sharing-scope")
    protected ResSharingScope resSharingScope = ResSharingScope.SHAREABLE;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    protected String resLink;

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getResRefName() {
        return resRefName;
    }

    public void setResRefName(String value) {
        this.resRefName = value;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String value) {
        this.resType = value;
    }

    public ResAuth getResAuth() {
        return resAuth;
    }

    public void setResAuth(ResAuth value) {
        this.resAuth = value;
    }

    public ResSharingScope getResSharingScope() {
        return resSharingScope;
    }

    public void setResSharingScope(ResSharingScope value) {
        this.resSharingScope = value;
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

    public String getResLink() {
        return resLink;
    }

    public void setResLink(String resLink) {
        this.resLink = resLink;
    }

}
