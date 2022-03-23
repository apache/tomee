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
package org.apache.openejb.jee.was.v6.ejb;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.apache.openejb.jee.was.v6.common.MessageDestination;
import org.apache.openejb.jee.was.v6.common.SecurityRole;
import org.apache.openejb.jee.was.v6.xmi.Extension;

/**
 * The assembly-descriptor element contains application-assembly information.
 * The application-assembly information consists of the following parts: the
 * definition of security roles, the definition of method permissions, and the
 * definition of transaction attributes for enterprise beans with
 * container-managed transaction demarcation. All the parts are optional in the
 * sense that they are omitted if the lists represented by them are empty.
 * Providing an assembly-descriptor in the deployment descriptor is optional for
 * the ejb-jar file producer.
 *
 *
 *
 * Java class for AssemblyDescriptor complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="AssemblyDescriptor"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="methodPermissions" type="{ejb.xmi}MethodPermission"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="methodTransactions" type="{ejb.xmi}MethodTransaction"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="securityRoles" type="{common.xmi}SecurityRole"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="excludeList" type="{ejb.xmi}ExcludeList"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="messageDestinations" type="{common.xmi}MessageDestination"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://www.omg.org/XMI}Extension"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attGroup ref="{http://www.omg.org/XMI}ObjectAttribs"/&gt;
 *       &lt;attribute ref="{http://www.omg.org/XMI}id"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssemblyDescriptor", propOrder = {"methodPermissions",
    "methodTransactions", "securityRoles", "excludeList",
    "messageDestinations", "extensions"})
public class AssemblyDescriptor {

    protected List<MethodPermission> methodPermissions;
    protected List<MethodTransaction> methodTransactions;
    protected List<SecurityRole> securityRoles;
    protected List<ExcludeList> excludeList;
    protected List<MessageDestination> messageDestinations;
    @XmlElement(name = "Extension", namespace = "http://www.omg.org/XMI")
    protected List<Extension> extensions;
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
     * Gets the value of the methodPermissions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the methodPermissions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getMethodPermissions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link MethodPermission }
     */
    public List<MethodPermission> getMethodPermissions() {
        if (methodPermissions == null) {
            methodPermissions = new ArrayList<MethodPermission>();
        }
        return this.methodPermissions;
    }

    /**
     * Gets the value of the methodTransactions property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the methodTransactions property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getMethodTransactions().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link MethodTransaction }
     */
    public List<MethodTransaction> getMethodTransactions() {
        if (methodTransactions == null) {
            methodTransactions = new ArrayList<MethodTransaction>();
        }
        return this.methodTransactions;
    }

    /**
     * Gets the value of the securityRoles property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the securityRoles property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSecurityRoles().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link SecurityRole }
     */
    public List<SecurityRole> getSecurityRoles() {
        if (securityRoles == null) {
            securityRoles = new ArrayList<SecurityRole>();
        }
        return this.securityRoles;
    }

    /**
     * Gets the value of the excludeList property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the excludeList property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getExcludeList().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ExcludeList }
     */
    public List<ExcludeList> getExcludeList() {
        if (excludeList == null) {
            excludeList = new ArrayList<ExcludeList>();
        }
        return this.excludeList;
    }

    /**
     * Gets the value of the messageDestinations property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the messageDestinations property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getMessageDestinations().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDestination }
     */
    public List<MessageDestination> getMessageDestinations() {
        if (messageDestinations == null) {
            messageDestinations = new ArrayList<MessageDestination>();
        }
        return this.messageDestinations;
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
