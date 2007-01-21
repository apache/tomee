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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.oej2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for environmentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="environmentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="moduleId" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}artifactType" minOccurs="0"/>
 *         &lt;element name="dependencies" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}dependenciesType" minOccurs="0"/>
 *         &lt;element name="hidden-classes" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}classFilterType" minOccurs="0"/>
 *         &lt;element name="non-overridable-classes" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}classFilterType" minOccurs="0"/>
 *         &lt;element name="inverse-classloading" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}emptyType" minOccurs="0"/>
 *         &lt;element name="suppress-default-environment" type="{http://geronimo.apache.org/xml/ns/deployment-1.2}emptyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "environmentType", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", propOrder = {
    "moduleId",
    "dependencies",
    "hiddenClasses",
    "nonOverridableClasses",
    "inverseClassloading",
    "suppressDefaultEnvironment"
})
public class EnvironmentType {

    @XmlElement(name="moduleId", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected ArtifactType moduleId;
    @XmlElement(name="dependencies", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected DependenciesType dependencies;
    @XmlElement(name = "hidden-classes", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected ClassFilterType hiddenClasses;
    @XmlElement(name = "non-overridable-classes", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected ClassFilterType nonOverridableClasses;
    @XmlElement(name = "inverse-classloading", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected EmptyType inverseClassloading;
    @XmlElement(name = "suppress-default-environment", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected EmptyType suppressDefaultEnvironment;

    /**
     * Gets the value of the moduleId property.
     * 
     * @return
     *     possible object is
     *     {@link ArtifactType }
     *     
     */
    public ArtifactType getModuleId() {
        return moduleId;
    }

    /**
     * Sets the value of the moduleId property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArtifactType }
     *     
     */
    public void setModuleId(ArtifactType value) {
        this.moduleId = value;
    }

    /**
     * Gets the value of the dependencies property.
     * 
     * @return
     *     possible object is
     *     {@link DependenciesType }
     *     
     */
    public DependenciesType getDependencies() {
        return dependencies;
    }

    /**
     * Sets the value of the dependencies property.
     * 
     * @param value
     *     allowed object is
     *     {@link DependenciesType }
     *     
     */
    public void setDependencies(DependenciesType value) {
        this.dependencies = value;
    }

    /**
     * Gets the value of the hiddenClasses property.
     * 
     * @return
     *     possible object is
     *     {@link ClassFilterType }
     *     
     */
    public ClassFilterType getHiddenClasses() {
        return hiddenClasses;
    }

    /**
     * Sets the value of the hiddenClasses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassFilterType }
     *     
     */
    public void setHiddenClasses(ClassFilterType value) {
        this.hiddenClasses = value;
    }

    /**
     * Gets the value of the nonOverridableClasses property.
     * 
     * @return
     *     possible object is
     *     {@link ClassFilterType }
     *     
     */
    public ClassFilterType getNonOverridableClasses() {
        return nonOverridableClasses;
    }

    /**
     * Sets the value of the nonOverridableClasses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassFilterType }
     *     
     */
    public void setNonOverridableClasses(ClassFilterType value) {
        this.nonOverridableClasses = value;
    }

    /**
     * Gets the value of the inverseClassloading property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getInverseClassloading() {
        return inverseClassloading;
    }

    /**
     * Sets the value of the inverseClassloading property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setInverseClassloading(EmptyType value) {
        this.inverseClassloading = value;
    }

    /**
     * Gets the value of the suppressDefaultEnvironment property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getSuppressDefaultEnvironment() {
        return suppressDefaultEnvironment;
    }

    /**
     * Sets the value of the suppressDefaultEnvironment property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setSuppressDefaultEnvironment(EmptyType value) {
        this.suppressDefaultEnvironment = value;
    }

}
