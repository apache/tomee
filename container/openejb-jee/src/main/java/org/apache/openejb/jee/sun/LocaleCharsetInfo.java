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
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"localeCharsetMap", "parameterEncoding"})
public class LocaleCharsetInfo {
    @XmlAttribute(name = "default-locale")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String defaultLocale;
    @XmlElement(name = "locale-charset-map", required = true)
    protected List<LocaleCharsetMap> localeCharsetMap;
    @XmlElement(name = "parameter-encoding")
    protected ParameterEncoding parameterEncoding;

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String value) {
        this.defaultLocale = value;
    }

    public List<LocaleCharsetMap> getLocaleCharsetMap() {
        if (localeCharsetMap == null) {
            localeCharsetMap = new ArrayList<LocaleCharsetMap>();
        }
        return this.localeCharsetMap;
    }

    public ParameterEncoding getParameterEncoding() {
        return parameterEncoding;
    }

    public void setParameterEncoding(ParameterEncoding value) {
        this.parameterEncoding = value;
    }
}
