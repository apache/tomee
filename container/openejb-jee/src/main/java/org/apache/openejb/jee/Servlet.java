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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Map;

/**
 * web-common_3_0.xsd
 *
 * <p>Java class for servletType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="servletType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="servlet-name" type="{http://java.sun.com/xml/ns/javaee}servlet-nameType"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="servlet-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *           &lt;element name="jsp-file" type="{http://java.sun.com/xml/ns/javaee}jsp-fileType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="init-param" type="{http://java.sun.com/xml/ns/javaee}param-valueType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="load-on-startup" type="{http://java.sun.com/xml/ns/javaee}load-on-startupType" minOccurs="0"/&gt;
 *         &lt;element name="enabled" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="async-supported" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="run-as" type="{http://java.sun.com/xml/ns/javaee}run-asType" minOccurs="0"/&gt;
 *         &lt;element name="security-role-ref" type="{http://java.sun.com/xml/ns/javaee}security-role-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="multipart-config" type="{http://java.sun.com/xml/ns/javaee}multipart-configType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "servletType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "servletName",
    "servletClass",
    "jspFile",
    "initParam",
    "loadOnStartup",
    "enabled",
    "asyncSupported",
    "runAs",
    "securityRoleRef",
    "multipartConfig"
})
public class Servlet {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(name = "servlet-name", required = true)
    protected String servletName;
    @XmlElement(name = "servlet-class")
    protected String servletClass;
    @XmlElement(name = "jsp-file")
    protected String jspFile;
    @XmlElement(name = "init-param")
    protected List<ParamValue> initParam;
    @XmlJavaTypeAdapter(type = Integer.class, value = LoadOnStartupAdapter.class)
    @XmlElement(name = "load-on-startup")
    protected Integer loadOnStartup;
    protected Boolean enabled;
    @XmlElement(name = "async-supported")
    protected Boolean asyncSupported;
    @XmlElement(name = "run-as")
    protected RunAs runAs;
    @XmlElement(name = "security-role-ref")
    protected List<SecurityRoleRef> securityRoleRef;
    @XmlElement(name = "multipart-config")
    protected MultipartConfig multipartConfig;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public void addDescription(final Text text) {
        description.add(text);
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(final Text[] text) {
        displayName.set(text);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public void addDisplayName(final Text text) {
        displayName.add(text);
    }

    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    public Map<String, Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(final String value) {
        this.servletName = value;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(final String value) {
        this.servletClass = value;
    }

    public String getJspFile() {
        return jspFile;
    }

    public void setJspFile(final String value) {
        this.jspFile = value;
    }

    public List<ParamValue> getInitParam() {
        if (initParam == null) {
            initParam = new ArrayList<ParamValue>();
        }
        return this.initParam;
    }

    public Integer getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(final Integer value) {
        this.loadOnStartup = value;
    }

    public Boolean getAsyncSupported() {
        return asyncSupported;
    }

    public void setAsyncSupported(final Boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public RunAs getRunAs() {
        return runAs;
    }

    public void setRunAs(final RunAs value) {
        this.runAs = value;
    }

    public List<SecurityRoleRef> getSecurityRoleRef() {
        if (securityRoleRef == null) {
            securityRoleRef = new ArrayList<SecurityRoleRef>();
        }
        return this.securityRoleRef;
    }

    public MultipartConfig getMultipartConfig() {
        return multipartConfig;
    }

    public void setMultipartConfig(final MultipartConfig multipartConfig) {
        this.multipartConfig = multipartConfig;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
