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
package org.openejb.jee.javaee;

import org.openejb.jee.javaee.JndiEnvironmentRef;

/**
 * @version $Revision$ $Date$
 */
public class PersistenceUnitRef extends JndiEnvironmentRef {
    private String persistenceUnitRefName;
    private String persistenceUnitName;

    public PersistenceUnitRef() {
    }

    public PersistenceUnitRef(String persistenceUnitRefName, String persistenceUnitName) {
        this.persistenceUnitRefName = persistenceUnitRefName;
        this.persistenceUnitName = persistenceUnitName;
    }

    public String getPersistenceUnitRefName() {
        return persistenceUnitRefName;
    }

    public void setPersistenceUnitRefName(String persistenceUnitRefName) {
        this.persistenceUnitRefName = persistenceUnitRefName;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }
}
