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
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cacheHelper",
    "defaultHelper",
    "property",
    "cacheMapping"
})
public class Cache {
    @XmlAttribute(name = "max-entries")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String maxEntries;
    @XmlAttribute(name = "timeout-in-seconds")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String timeoutInSeconds;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String enabled;
    @XmlElement(name = "cache-helper")
    protected List<CacheHelper> cacheHelper;
    @XmlElement(name = "default-helper")
    protected DefaultHelper defaultHelper;
    protected List<Property> property;
    @XmlElement(name = "cache-mapping")
    protected List<CacheMapping> cacheMapping;

    public String getMaxEntries() {
        if (maxEntries == null) {
            return "4096";
        } else {
            return maxEntries;
        }
    }

    public void setMaxEntries(String value) {
        this.maxEntries = value;
    }

    public String getTimeoutInSeconds() {
        if (timeoutInSeconds == null) {
            return "30";
        } else {
            return timeoutInSeconds;
        }
    }

    public void setTimeoutInSeconds(String value) {
        this.timeoutInSeconds = value;
    }

    public String getEnabled() {
        if (enabled == null) {
            return "true";
        } else {
            return enabled;
        }
    }

    public void setEnabled(String value) {
        this.enabled = value;
    }

    public List<CacheHelper> getCacheHelper() {
        if (cacheHelper == null) {
            cacheHelper = new ArrayList<CacheHelper>();
        }
        return this.cacheHelper;
    }

    public DefaultHelper getDefaultHelper() {
        return defaultHelper;
    }

    public void setDefaultHelper(DefaultHelper value) {
        this.defaultHelper = value;
    }

    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

    public List<CacheMapping> getCacheMapping() {
        if (cacheMapping == null) {
            cacheMapping = new ArrayList<CacheMapping>();
        }
        return this.cacheMapping;
    }
}
