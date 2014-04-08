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
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"value"})
public class ConstraintFieldValue {
    @XmlAttribute(name = "match-expr")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String matchExpr;
    @XmlAttribute(name = "cache-on-match")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String cacheOnMatch;
    @XmlAttribute(name = "cache-on-match-failure")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String cacheOnMatchFailure;
    @XmlValue
    protected String value;

    public String getMatchExpr() {
        if (matchExpr == null) {
            return "equals";
        } else {
            return matchExpr;
        }
    }

    public void setMatchExpr(String value) {
        this.matchExpr = value;
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

    public String getvalue() {
        return value;
    }

    public void setvalue(String value) {
        this.value = value;
    }
}
