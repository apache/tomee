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

import org.apache.openejb.jee.was.v6.common.CompatibilityDescriptionGroup;

/**
 * The root element of the EJB deployment descriptor. It contains an optional
 * description of the ejb-jar file; optional display name; optional small icon
 * file name; optional large icon file name; mandatory structural information
 * about all included enterprise beans; a descriptor for container managed
 * relationships, if any; an optional application-assembly descriptor; and an
 * optional name of an ejb-client-jar file for the ejb-jar.
 *
 *
 *
 * Java class for EJBJar complex type.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EJBJar"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{common.xmi}CompatibilityDescriptionGroup"&gt;
 *       &lt;choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="assemblyDescriptor" type="{ejb.xmi}AssemblyDescriptor"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="enterpriseBeans" type="{ejb.xmi}EnterpriseBean"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="relationshipList" type="{ejb.xmi}Relationships"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="ejbClientJar" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EJBJar", propOrder = {"assemblyDescriptors",
    "enterpriseBeans", "relationshipList"})
public class EJBJar extends CompatibilityDescriptionGroup {

    @XmlElement(name = "assemblyDescriptor")
    protected List<AssemblyDescriptor> assemblyDescriptors;
    protected List<EnterpriseBean> enterpriseBeans;
    protected List<Relationships> relationshipList;
    @XmlAttribute
    protected String ejbClientJar;
    @XmlAttribute(name = "version")
    protected String ejbSpecsVersion;

    /**
     * Gets the value of the assemblyDescriptors property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the assemblyDescriptors property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getAssemblyDescriptors().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link AssemblyDescriptor }
     */
    public List<AssemblyDescriptor> getAssemblyDescriptors() {
        if (assemblyDescriptors == null) {
            assemblyDescriptors = new ArrayList<AssemblyDescriptor>();
        }
        return this.assemblyDescriptors;
    }

    /**
     * Gets the value of the enterpriseBeans property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the enterpriseBeans property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEnterpriseBeans().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EnterpriseBean }
     */
    public List<EnterpriseBean> getEnterpriseBeans() {
        if (enterpriseBeans == null) {
            enterpriseBeans = new ArrayList<EnterpriseBean>();
        }
        return this.enterpriseBeans;
    }

    /**
     * Gets the value of the relationshipList property.
     *
     *
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the relationshipList property.
     *
     *
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getRelationshipList().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Relationships }
     */
    public List<Relationships> getRelationshipList() {
        if (relationshipList == null) {
            relationshipList = new ArrayList<Relationships>();
        }
        return this.relationshipList;
    }

    /**
     * Gets the value of the ejbClientJar property.
     *
     * @return possible object is {@link String }
     */
    public String getEjbClientJar() {
        return ejbClientJar;
    }

    /**
     * Sets the value of the ejbClientJar property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEjbClientJar(final String value) {
        this.ejbClientJar = value;
    }

    /**
     * Gets the value of the ejbSpecsVersion property.
     *
     * @return possible object is {@link String }
     */
    public String getEjbSpecsVersion() {
        return ejbSpecsVersion;
    }

    /**
     * Sets the value of the ejbSpecsVersion property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEjbSpecsVersion(final String value) {
        this.ejbSpecsVersion = value;
    }

}
