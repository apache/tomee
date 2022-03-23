/*
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
package org.apache.openejb.jee.jpa.fragment;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "mappingFile",
    "jarFile",
    "clazz",
    "excludeUnlistedClasses"
})
public class PersistenceUnitFragment {

    protected String description;
    @XmlElement(name = "mapping-file", required = true)
    protected List<String> mappingFile;
    @XmlElement(name = "jar-file", required = true)
    protected List<String> jarFile;
    @XmlElement(name = "class", required = true)
    protected List<String> clazz;
    @XmlElement(name = "exclude-unlisted-classes", defaultValue = "false")
    protected boolean excludeUnlistedClasses;
    @XmlAttribute(required = true)
    protected String name;

    public PersistenceUnitFragment() {
        // no-op
    }

    public PersistenceUnitFragment(final String unitName) {
        this.name = unitName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String value) {
        this.description = value;
    }

    public List<String> getMappingFile() {
        if (mappingFile == null) {
            mappingFile = new ArrayList<String>();
        }
        return this.mappingFile;
    }

    public List<String> getClazz() {
        if (clazz == null) {
            clazz = new ArrayList<String>();
        }
        return this.clazz;
    }

    public boolean addClass(final String s) {
        return getClazz().add(s);
    }

    public boolean addClass(final Class<?> clazz) {
        return addClass(clazz.getName());
    }

    public boolean isExcludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(final boolean value) {
        this.excludeUnlistedClasses = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        this.name = value;
    }

    public List<String> getJarFile() {
        if (jarFile == null) {
            jarFile = new ArrayList<String>();
        }
        return this.jarFile;
    }
}
