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
package org.apache.openejb.jee.jpa.fragment;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "persistenceUnitFragment"
})
@XmlRootElement(name = "persistence-fragment")
public class PersistenceFragment {

    @XmlElement(name = "persistence-unit-fragment", required = true)
    protected List<PersistenceUnitFragment> persistenceUnitFragment;

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String version = "1.0";

    public PersistenceFragment() {
    }

    public PersistenceFragment(final PersistenceUnitFragment... persistenceUnit) {
        for (final PersistenceUnitFragment unit : persistenceUnit) {
            getPersistenceUnitFragment().add(unit);
        }
    }

    /**
     * Gets the value of the persistenceUnit property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the persistenceUnit property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersistenceUnit().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link org.apache.openejb.jee.jpa.unit.PersistenceUnit }
     */
    public List<PersistenceUnitFragment> getPersistenceUnitFragment() {
        if (persistenceUnitFragment == null) {
            persistenceUnitFragment = new ArrayList<PersistenceUnitFragment>();
        }
        return persistenceUnitFragment;
    }

    public PersistenceUnitFragment addPersistenceUnitFragment(final PersistenceUnitFragment unit) {
        getPersistenceUnitFragment().add(unit);
        return unit;
    }

    public PersistenceUnitFragment addPersistenceUnitFragment(final String unitName) {
        return addPersistenceUnitFragment(new PersistenceUnitFragment(unitName));
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVersion() {
        if (version == null) {
            return "1.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersion(final String value) {
        this.version = value;
    }


}
