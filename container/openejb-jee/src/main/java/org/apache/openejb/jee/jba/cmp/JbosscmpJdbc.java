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

package org.apache.openejb.jee.jba.cmp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://jboss.org}defaults" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}enterprise-beans" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}relationships" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}dependent-value-classes" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}type-mappings" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}entity-commands" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}user-type-mappings" minOccurs="0"/&gt;
 *         &lt;element ref="{http://jboss.org}reserved-words" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "defaults",
    "enterpriseBeans",
    "relationships",
    "dependentValueClasses",
    "typeMappings",
    "entityCommands",
    "userTypeMappings",
    "reservedWords"
})
@XmlRootElement(name = "jbosscmp-jdbc")
public class JbosscmpJdbc {

    protected Defaults defaults;
    @XmlElement(name = "enterprise-beans")
    protected EnterpriseBeans enterpriseBeans;
    protected Relationships relationships;
    @XmlElement(name = "dependent-value-classes")
    protected DependentValueClasses dependentValueClasses;
    @XmlElement(name = "type-mappings")
    protected TypeMappings typeMappings;
    @XmlElement(name = "entity-commands")
    protected EntityCommands entityCommands;
    @XmlElement(name = "user-type-mappings")
    protected UserTypeMappings userTypeMappings;
    @XmlElement(name = "reserved-words")
    protected ReservedWords reservedWords;

    /**
     * Gets the value of the defaults property.
     *
     * @return possible object is
     * {@link Defaults }
     */
    public Defaults getDefaults() {
        return defaults;
    }

    /**
     * Sets the value of the defaults property.
     *
     * @param value allowed object is
     *              {@link Defaults }
     */
    public void setDefaults(final Defaults value) {
        this.defaults = value;
    }

    /**
     * Gets the value of the enterpriseBeans property.
     *
     * @return possible object is
     * {@link EnterpriseBeans }
     */
    public EnterpriseBeans getEnterpriseBeans() {
        return enterpriseBeans;
    }

    /**
     * Sets the value of the enterpriseBeans property.
     *
     * @param value allowed object is
     *              {@link EnterpriseBeans }
     */
    public void setEnterpriseBeans(final EnterpriseBeans value) {
        this.enterpriseBeans = value;
    }

    /**
     * Gets the value of the relationships property.
     *
     * @return possible object is
     * {@link Relationships }
     */
    public Relationships getRelationships() {
        return relationships;
    }

    /**
     * Sets the value of the relationships property.
     *
     * @param value allowed object is
     *              {@link Relationships }
     */
    public void setRelationships(final Relationships value) {
        this.relationships = value;
    }

    /**
     * Gets the value of the dependentValueClasses property.
     *
     * @return possible object is
     * {@link DependentValueClasses }
     */
    public DependentValueClasses getDependentValueClasses() {
        return dependentValueClasses;
    }

    /**
     * Sets the value of the dependentValueClasses property.
     *
     * @param value allowed object is
     *              {@link DependentValueClasses }
     */
    public void setDependentValueClasses(final DependentValueClasses value) {
        this.dependentValueClasses = value;
    }

    /**
     * Gets the value of the typeMappings property.
     *
     * @return possible object is
     * {@link TypeMappings }
     */
    public TypeMappings getTypeMappings() {
        return typeMappings;
    }

    /**
     * Sets the value of the typeMappings property.
     *
     * @param value allowed object is
     *              {@link TypeMappings }
     */
    public void setTypeMappings(final TypeMappings value) {
        this.typeMappings = value;
    }

    /**
     * Gets the value of the entityCommands property.
     *
     * @return possible object is
     * {@link EntityCommands }
     */
    public EntityCommands getEntityCommands() {
        return entityCommands;
    }

    /**
     * Sets the value of the entityCommands property.
     *
     * @param value allowed object is
     *              {@link EntityCommands }
     */
    public void setEntityCommands(final EntityCommands value) {
        this.entityCommands = value;
    }

    /**
     * Gets the value of the userTypeMappings property.
     *
     * @return possible object is
     * {@link UserTypeMappings }
     */
    public UserTypeMappings getUserTypeMappings() {
        return userTypeMappings;
    }

    /**
     * Sets the value of the userTypeMappings property.
     *
     * @param value allowed object is
     *              {@link UserTypeMappings }
     */
    public void setUserTypeMappings(final UserTypeMappings value) {
        this.userTypeMappings = value;
    }

    /**
     * Gets the value of the reservedWords property.
     *
     * @return possible object is
     * {@link ReservedWords }
     */
    public ReservedWords getReservedWords() {
        return reservedWords;
    }

    /**
     * Sets the value of the reservedWords property.
     *
     * @param value allowed object is
     *              {@link ReservedWords }
     */
    public void setReservedWords(final ReservedWords value) {
        this.reservedWords = value;
    }

}
