/**
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
package org.apache.openejb.jee.wls;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>Java class for resource-description complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resource-description"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="res-ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;group ref="{http://www.bea.com/ns/weblogic/90}resource-lookup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reference-descriptor", propOrder = {
    "resourceDescription",
    "resourceEnvDescription",
    "ejbReferenceDescription",
    "serviceReferenceDescription"
})
public class ReferenceDescriptor {

    @XmlElement(name = "resource-description")
    protected List<ResourceDescription> resourceDescription;

    @XmlElement(name = "resource-env-description")
    protected List<ResourceEnvDescription> resourceEnvDescription;

    @XmlElement(name = "ejb-reference-description")
    protected List<EjbReferenceDescription> ejbReferenceDescription;

    @XmlElement(name = "service-reference-description")
    protected List<ServiceReferenceDescription> serviceReferenceDescription;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the resourceDescription property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceDescription property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceDescription().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceDescription }
     */
    public List<ResourceDescription> getResourceDescription() {
        if (resourceDescription == null) {
            resourceDescription = new ArrayList<ResourceDescription>();
        }
        return this.resourceDescription;
    }

    /**
     * Gets the value of the resourceEnvDescription property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceEnvDescription property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceEnvDescription().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceEnvDescription }
     */
    public List<ResourceEnvDescription> getResourceEnvDescription() {
        if (resourceEnvDescription == null) {
            resourceEnvDescription = new ArrayList<ResourceEnvDescription>();
        }
        return this.resourceEnvDescription;
    }

    /**
     * Gets the value of the ejbReferenceDescription property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbReferenceDescription property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbReferenceDescription().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link EjbReferenceDescription }
     */
    public List<EjbReferenceDescription> getEjbReferenceDescription() {
        if (ejbReferenceDescription == null) {
            ejbReferenceDescription = new ArrayList<EjbReferenceDescription>();
        }
        return this.ejbReferenceDescription;
    }

    /**
     * Gets the value of the serviceReferenceDescription property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceReferenceDescription property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceReferenceDescription().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceReferenceDescription }
     */
    public List<ServiceReferenceDescription> getServiceReferenceDescription() {
        if (serviceReferenceDescription == null) {
            serviceReferenceDescription = new ArrayList<ServiceReferenceDescription>();
        }
        return this.serviceReferenceDescription;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
