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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"property"})
public class ClassLoader {
    @XmlAttribute(name = "extra-class-path")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String extraClassPath;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String delegate;
    @XmlAttribute(name = "dynamic-reload-interval")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String dynamicReloadInterval;
    protected List<Property> property;

    public String getExtraClassPath() {
        return extraClassPath;
    }

    public void setExtraClassPath(String value) {
        this.extraClassPath = value;
    }

    public String getDelegate() {
        if (delegate == null) {
            return "true";
        } else {
            return delegate;
        }
    }

    public void setDelegate(String value) {
        this.delegate = value;
    }

    public String getDynamicReloadInterval() {
        return dynamicReloadInterval;
    }

    public void setDynamicReloadInterval(String value) {
        this.dynamicReloadInterval = value;
    }

    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }
}
