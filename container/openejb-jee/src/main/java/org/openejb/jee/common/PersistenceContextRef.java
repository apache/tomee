/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.openejb.jee.common;

import org.openejb.jee.common.JndiEnvironmentRef;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class PersistenceContextRef extends JndiEnvironmentRef {
    private String persistenceContextRefName;
    private String persistenceUnitName;
    private PersistenceContextType persistenceContextType;
    private List<PersistenceProperty> persistenceProperties;

    public PersistenceContextRef() {
    }

    public PersistenceContextRef(String persistenceContextRefName, String persistenceUnitName, PersistenceContextType persistenceContextType) {
        this.persistenceContextRefName = persistenceContextRefName;
        this.persistenceUnitName = persistenceUnitName;
        this.persistenceContextType = persistenceContextType;
    }

    public String getPersistenceContextRefName() {
        return persistenceContextRefName;
    }

    public void setPersistenceContextRefName(String persistenceContextRefName) {
        this.persistenceContextRefName = persistenceContextRefName;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public PersistenceContextType getPersistenceContextType() {
        return persistenceContextType;
    }

    public void setPersistenceContextType(PersistenceContextType persistenceContextType) {
        this.persistenceContextType = persistenceContextType;
    }

    public List<PersistenceProperty> getPersistenceProperties() {
        return persistenceProperties;
    }

    public void setPersistenceProperties(List<PersistenceProperty> persistenceProperties) {
        this.persistenceProperties = persistenceProperties;
    }
}
