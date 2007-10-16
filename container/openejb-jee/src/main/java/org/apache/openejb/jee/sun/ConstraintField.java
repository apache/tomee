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
@XmlType(name = "", propOrder = {"constraintFieldValue"})
public class ConstraintField {
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String name;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String scope;
    @XmlAttribute(name = "cache-on-match")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String cacheOnMatch;
    @XmlAttribute(name = "cache-on-match-failure")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String cacheOnMatchFailure;
    @XmlElement(name = "constraint-field-value")
    protected List<ConstraintFieldValue> constraintFieldValue;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getScope() {
        if (scope == null) {
            return "request.parameter";
        } else {
            return scope;
        }
    }

    public void setScope(String value) {
        this.scope = value;
    }

    public String getCacheOnMatch() {
        if (cacheOnMatch == null) {
            return "true";
        } else {
            return cacheOnMatch;
        }
    }

    public void setCacheOnMatch(String value) {
        this.cacheOnMatch = value;
    }

    public String getCacheOnMatchFailure() {
        if (cacheOnMatchFailure == null) {
            return "false";
        } else {
            return cacheOnMatchFailure;
        }
    }

    public void setCacheOnMatchFailure(String value) {
        this.cacheOnMatchFailure = value;
    }

    public List<ConstraintFieldValue> getConstraintFieldValue() {
        if (constraintFieldValue == null) {
            constraintFieldValue = new ArrayList<ConstraintFieldValue>();
        }
        return this.constraintFieldValue;
    }
}
