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
import jakarta.xml.bind.annotation.XmlType;

import org.apache.openejb.jee.was.v6.common.JNDIEnvRefsGroup;
import org.apache.openejb.jee.was.v6.common.SecurityIdentity;
import org.apache.openejb.jee.was.v6.common.SecurityRoleRef;
import org.apache.openejb.jee.was.v6.java.JavaClass;

/**
 * EnterpriseJavaBean is a class. It can have instances, someone could write an
 * instance document containing Departments and Employees. It also has
 * attributes, operations, and associations. These are actually derived/filtered
 * from its implementation classes and interfaces. For mapping and browsing
 * purposes, though, you would like the EJB to appear as a class.
 *
 * In this light, even Session Beans can have associations and properties
 * implemented by their bean. For example, it would be meaningful to describe
 * associations from a Session to the Entities which it uses to perform its
 * work.
 *
 *
 *
 * Java class for EnterpriseBean complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EnterpriseBean"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{common.xmi}JNDIEnvRefsGroup"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="securityRoleRefs" type="{common.xmi}SecurityRoleRef"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="ejbClass" type="{java.xmi}JavaClass"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="homeInterface" type="{java.xmi}JavaClass"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="remoteInterface" type="{java.xmi}JavaClass"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="securityIdentity" type="{common.xmi}SecurityIdentity"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="localHomeInterface" type="{java.xmi}JavaClass"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="localInterface" type="{java.xmi}JavaClass"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="ejbClass" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="homeInterface" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="localHomeInterface" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="localInterface" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="remoteInterface" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnterpriseBean", propOrder = {"securityRoleRefs",
    "ejbClasses", "homeInterfaces", "remoteInterfaces",
    "securityIdentities", "localHomeInterfaces", "localInterfaces"})
public class EnterpriseBean extends JNDIEnvRefsGroup {

    protected List<SecurityRoleRef> securityRoleRefs;
    @XmlElement(name = "ejbClass")
    protected List<JavaClass> ejbClasses;
    @XmlElement(name = "homeInterface")
    protected List<JavaClass> homeInterfaces;
    @XmlElement(name = "remoteInterface")
    protected List<JavaClass> remoteInterfaces;
    @XmlElement(name = "securityIdentity")
    protected List<SecurityIdentity> securityIdentities;
    @XmlElement(name = "localHomeInterface")
    protected List<JavaClass> localHomeInterfaces;
    @XmlElement(name = "localInterface")
    protected List<JavaClass> localInterfaces;
    @XmlAttribute
    protected String ejbClass;
    @XmlAttribute
    protected String homeInterface;
    @XmlAttribute
    protected String localHomeInterface;
    @XmlAttribute
    protected String localInterface;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected String remoteInterface;

    /**
     * Gets the value of the securityRoleRefs property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the securityRoleRefs property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSecurityRoleRefs().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link SecurityRoleRef }
     */
    public List<SecurityRoleRef> getSecurityRoleRefs() {
        if (securityRoleRefs == null) {
            securityRoleRefs = new ArrayList<SecurityRoleRef>();
        }
        return this.securityRoleRefs;
    }

    /**
     * Gets the value of the ejbClasses property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the ejbClasses property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEjbClasses().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getEjbClasses() {
        if (ejbClasses == null) {
            ejbClasses = new ArrayList<JavaClass>();
        }
        return this.ejbClasses;
    }

    /**
     * Gets the value of the homeInterfaces property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the homeInterfaces property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getHomeInterfaces().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getHomeInterfaces() {
        if (homeInterfaces == null) {
            homeInterfaces = new ArrayList<JavaClass>();
        }
        return this.homeInterfaces;
    }

    /**
     * Gets the value of the remoteInterfaces property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the remoteInterfaces property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getRemoteInterfaces().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getRemoteInterfaces() {
        if (remoteInterfaces == null) {
            remoteInterfaces = new ArrayList<JavaClass>();
        }
        return this.remoteInterfaces;
    }

    /**
     * Gets the value of the securityIdentities property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the securityIdentities property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSecurityIdentities().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link SecurityIdentity }
     */
    public List<SecurityIdentity> getSecurityIdentities() {
        if (securityIdentities == null) {
            securityIdentities = new ArrayList<SecurityIdentity>();
        }
        return this.securityIdentities;
    }

    /**
     * Gets the value of the localHomeInterfaces property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the localHomeInterfaces property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getLocalHomeInterfaces().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getLocalHomeInterfaces() {
        if (localHomeInterfaces == null) {
            localHomeInterfaces = new ArrayList<JavaClass>();
        }
        return this.localHomeInterfaces;
    }

    /**
     * Gets the value of the localInterfaces property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the localInterfaces property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getLocalInterfaces().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JavaClass }
     */
    public List<JavaClass> getLocalInterfaces() {
        if (localInterfaces == null) {
            localInterfaces = new ArrayList<JavaClass>();
        }
        return this.localInterfaces;
    }

    /**
     * Gets the value of the ejbClass property.
     *
     * @return possible object is {@link String }
     */
    public String getEjbClass() {
        return ejbClass;
    }

    /**
     * Sets the value of the ejbClass property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEjbClass(final String value) {
        this.ejbClass = value;
    }

    /**
     * Gets the value of the homeInterface property.
     *
     * @return possible object is {@link String }
     */
    public String getHomeInterface() {
        return homeInterface;
    }

    /**
     * Sets the value of the homeInterface property.
     *
     * @param value allowed object is {@link String }
     */
    public void setHomeInterface(final String value) {
        this.homeInterface = value;
    }

    /**
     * Gets the value of the localHomeInterface property.
     *
     * @return possible object is {@link String }
     */
    public String getLocalHomeInterface() {
        return localHomeInterface;
    }

    /**
     * Sets the value of the localHomeInterface property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLocalHomeInterface(final String value) {
        this.localHomeInterface = value;
    }

    /**
     * Gets the value of the localInterface property.
     *
     * @return possible object is {@link String }
     */
    public String getLocalInterface() {
        return localInterface;
    }

    /**
     * Sets the value of the localInterface property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLocalInterface(final String value) {
        this.localInterface = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the remoteInterface property.
     *
     * @return possible object is {@link String }
     */
    public String getRemoteInterface() {
        return remoteInterface;
    }

    /**
     * Sets the value of the remoteInterface property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRemoteInterface(final String value) {
        this.remoteInterface = value;
    }

}
