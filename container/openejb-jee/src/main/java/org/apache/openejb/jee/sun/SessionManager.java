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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"managerProperties", "storeProperties"})
public class SessionManager {
    @XmlAttribute(name = "persistence-type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String persistenceType;
    @XmlElement(name = "manager-properties")
    protected ManagerProperties managerProperties;
    @XmlElement(name = "store-properties")
    protected StoreProperties storeProperties;

    public String getPersistenceType() {
        if (persistenceType == null) {
            return "memory";
        } else {
            return persistenceType;
        }
    }

    public void setPersistenceType(String value) {
        this.persistenceType = value;
    }

    public ManagerProperties getManagerProperties() {
        return managerProperties;
    }

    public void setManagerProperties(ManagerProperties value) {
        this.managerProperties = value;
    }

    public StoreProperties getStoreProperties() {
        return storeProperties;
    }

    public void setStoreProperties(StoreProperties value) {
        this.storeProperties = value;
    }
}
