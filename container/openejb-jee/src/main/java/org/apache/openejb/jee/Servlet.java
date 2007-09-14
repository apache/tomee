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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "servletType", propOrder = {
        "descriptions",
        "displayNames",
        "icons",
        "servletName",
        "servletClass",
        "jspFile",
        "initParam",
        "loadOnStartup",
        "runAs",
        "securityRoleRef"
})
public class Servlet {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlTransient
    protected LocalList<String, Icon> icon = new LocalList<String, Icon>(Icon.class);

    @XmlElement(name = "servlet-name", required = true)
    protected String servletName;
    @XmlElement(name = "servlet-class")
    protected String servletClass;
    @XmlElement(name = "jsp-file")
    protected String jspFile;
    @XmlElement(name = "init-param")
    protected List<ParamValue> initParam;
    @XmlElement(name = "load-on-startup")
    protected Boolean loadOnStartup;
    @XmlElement(name = "run-as")
    protected RunAs runAs;
    @XmlElement(name = "security-role-ref")
    protected List<SecurityRoleRef> securityRoleRef;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

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

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(Text[] text) {
        displayName.set(text);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    @XmlElement(name = "icon", required = true)
    public Icon[] getIcons() {
        return icon.toArray();
    }

    public void setIcons(Icon[] text) {
        icon.set(text);
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String value) {
        this.servletName = value;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String value) {
        this.servletClass = value;
    }

    public String getJspFile() {
        return jspFile;
    }

    public void setJspFile(String value) {
        this.jspFile = value;
    }

    public List<ParamValue> getInitParam() {
        if (initParam == null) {
            initParam = new ArrayList<ParamValue>();
        }
        return this.initParam;
    }

    public Boolean getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(Boolean value) {
        this.loadOnStartup = value;
    }

    public RunAs getRunAs() {
        return runAs;
    }

    public void setRunAs(RunAs value) {
        this.runAs = value;
    }

    public List<SecurityRoleRef> getSecurityRoleRef() {
        if (securityRoleRef == null) {
            securityRoleRef = new ArrayList<SecurityRoleRef>();
        }
        return this.securityRoleRef;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
