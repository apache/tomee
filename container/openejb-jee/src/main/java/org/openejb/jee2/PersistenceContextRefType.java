/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistence-context-ref element contains a declaration
 * of Deployment Component's reference to a persistence context
 * associated within a Deployment Component's
 * environment. It consists of:
 * <p/>
 * - an optional description
 * - the persistence context reference name
 * - an optional persistence unit name.  If not specified,
 * the default persistence unit is assumed.
 * - an optional specification as to whether
 * the persistence context type is Transaction or
 * Extended.  If not specified, Transaction is assumed.
 * - an optional list of persistence properties
 * - optional injection targets
 * <p/>
 * Examples:
 * <p/>
 * <persistence-context-ref>
 * <persistence-context-ref-name>myPersistenceContext
 * </persistence-context-ref-name>
 * </persistence-context-ref>
 * <p/>
 * <persistence-context-ref>
 * <persistence-context-ref-name>myPersistenceContext
 * </persistence-context-ref-name>
 * <persistence-unit-name>PersistenceUnit1
 * </persistence-unit-name>
 * <persistence-context-type>Extended</persistence-context-type>
 * </persistence-context-ref>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistence-context-refType", propOrder = {
        "description",
        "persistenceContextRefName",
        "persistenceUnitName",
        "persistenceContextType",
        "persistenceProperty",
        "mappedName",
        "injectionTarget"
        })
public class PersistenceContextRefType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "persistence-context-ref-name", required = true)
    protected String persistenceContextRefName;
    @XmlElement(name = "persistence-unit-name")
    protected String persistenceUnitName;
    @XmlElement(name = "persistence-context-type")
    protected PersistenceContextTypeType persistenceContextType;
    @XmlElement(name = "persistence-property", required = true)
    protected List<PropertyType> persistenceProperty;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTargetType> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDescription().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getPersistenceContextRefName() {
        return persistenceContextRefName;
    }

    public void setPersistenceContextRefName(String value) {
        this.persistenceContextRefName = value;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String value) {
        this.persistenceUnitName = value;
    }

    public PersistenceContextTypeType getPersistenceContextType() {
        return persistenceContextType;
    }

    public void setPersistenceContextType(PersistenceContextTypeType value) {
        this.persistenceContextType = value;
    }

    /**
     * Gets the value of the persistenceProperty property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the persistenceProperty property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPersistenceProperty().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyType }
     */
    public List<PropertyType> getPersistenceProperty() {
        if (persistenceProperty == null) {
            persistenceProperty = new ArrayList<PropertyType>();
        }
        return this.persistenceProperty;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    /**
     * Gets the value of the injectionTarget property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the injectionTarget property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getInjectionTarget().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link InjectionTargetType }
     */
    public List<InjectionTargetType> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTargetType>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
