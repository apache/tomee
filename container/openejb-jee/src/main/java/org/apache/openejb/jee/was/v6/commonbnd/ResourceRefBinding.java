/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.openejb.jee.was.v6.commonbnd;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.apache.openejb.jee.was.v6.common.ResourceRef;
import org.apache.openejb.jee.was.v6.xmi.Extension;

/**
 *
 * Java class for ResourceRefBinding complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="ResourceRefBinding"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="defaultAuth" type="{commonbnd.xmi}AbstractAuthData"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="bindingResourceRef" type="{common.xmi}ResourceRef"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="properties" type="{commonbnd.xmi}Property"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.omg.org/XMI}Extension"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attGroup ref="{http://www.omg.org/XMI}ObjectAttribs"/&gt;
 *       &lt;attribute name="bindingResourceRef" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="jndiName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="loginConfigurationName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute ref="{http://www.omg.org/XMI}id"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourceRefBinding", propOrder = {"defaultAuths",
    "bindingResourceRefs", "properties", "extensions"})
public class ResourceRefBinding {

    @XmlElement(name = "defaultAuth")
    protected List<AbstractAuthData> defaultAuths;
    @XmlElement(name = "bindingResourceRef")
    protected List<ResourceRef> bindingResourceRefs;
    protected List<Property> properties;
    @XmlElement(name = "Extension", namespace = "http://www.omg.org/XMI")
    protected List<Extension> extensions;
    @XmlAttribute
    protected String bindingResourceRef;
    @XmlAttribute
    protected String jndiName;
    @XmlAttribute
    protected String loginConfigurationName;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected QName type;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String version;
    @XmlAttribute
    protected String href;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    @XmlIDREF
    protected Object idref;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String label;
    @XmlAttribute(namespace = "http://www.omg.org/XMI")
    protected String uuid;

    /**
     * Gets the value of the defaultAuths property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the defaultAuths property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getDefaultAuths().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractAuthData }
     */
    public List<AbstractAuthData> getDefaultAuths() {
        if (defaultAuths == null) {
            defaultAuths = new ArrayList<AbstractAuthData>();
        }
        return this.defaultAuths;
    }

    /**
     * Gets the value of the bindingResourceRefs property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the bindingResourceRefs property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getBindingResourceRefs().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceRef }
     */
    public List<ResourceRef> getBindingResourceRefs() {
        if (bindingResourceRefs == null) {
            bindingResourceRefs = new ArrayList<ResourceRef>();
        }
        return this.bindingResourceRefs;
    }

    /**
     * Gets the value of the properties property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the properties property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getProperties().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list {@link Property }
     */
    public List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<Property>();
        }
        return this.properties;
    }

    /**
     * Gets the value of the extensions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the extensions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getExtensions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Extension }
     */
    public List<Extension> getExtensions() {
        if (extensions == null) {
            extensions = new ArrayList<Extension>();
        }
        return this.extensions;
    }

    /**
     * Gets the value of the bindingResourceRef property.
     *
     * @return possible object is {@link String }
     */
    public String getBindingResourceRef() {
        return bindingResourceRef;
    }

    /**
     * Sets the value of the bindingResourceRef property.
     *
     * @param value allowed object is {@link String }
     */
    public void setBindingResourceRef(final String value) {
        this.bindingResourceRef = value;
    }

    /**
     * Gets the value of the jndiName property.
     *
     * @return possible object is {@link String }
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * Sets the value of the jndiName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJndiName(final String value) {
        this.jndiName = value;
    }

    /**
     * Gets the value of the loginConfigurationName property.
     *
     * @return possible object is {@link String }
     */
    public String getLoginConfigurationName() {
        return loginConfigurationName;
    }

    /**
     * Sets the value of the loginConfigurationName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLoginConfigurationName(final String value) {
        this.loginConfigurationName = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link QName }
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link QName }
     */
    public void setType(final QName value) {
        this.type = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is {@link String }
     */
    public String getVersion() {
        if (version == null) {
            return "2.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is {@link String }
     */
    public void setVersion(final String value) {
        this.version = value;
    }

    /**
     * Gets the value of the href property.
     *
     * @return possible object is {@link String }
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     *
     * @param value allowed object is {@link String }
     */
    public void setHref(final String value) {
        this.href = value;
    }

    /**
     * Gets the value of the idref property.
     *
     * @return possible object is {@link Object }
     */
    public Object getIdref() {
        return idref;
    }

    /**
     * Sets the value of the idref property.
     *
     * @param value allowed object is {@link Object }
     */
    public void setIdref(final Object value) {
        this.idref = value;
    }

    /**
     * Gets the value of the label property.
     *
     * @return possible object is {@link String }
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLabel(final String value) {
        this.label = value;
    }

    /**
     * Gets the value of the uuid property.
     *
     * @return possible object is {@link String }
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUuid(final String value) {
        this.uuid = value;
    }

}
