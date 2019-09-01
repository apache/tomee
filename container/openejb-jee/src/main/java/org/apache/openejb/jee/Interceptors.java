/**
 *
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

package org.apache.openejb.jee;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for interceptorsType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="interceptorsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="interceptor" type="{http://java.sun.com/xml/ns/javaee}interceptorType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interceptorsType", propOrder = {
    "descriptions",
    "interceptor"
})
public class Interceptors {

    @XmlTransient
    protected TextMap description = new TextMap();

    @XmlTransient
    protected Map<String, Interceptor> interceptors = new LinkedHashMap<String, Interceptor>();

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

    @XmlElement(name = "interceptor", required = true)
    public Interceptor[] getInterceptor() {
        return interceptors.values().toArray(new Interceptor[]{});
    }

    public void setInterceptor(final Interceptor[] v) {
        interceptors.clear();
        for (final Interceptor e : v) addInterceptor(e);
    }

    public Interceptor addInterceptor(final Interceptor interceptor) {
        interceptors.put(interceptor.getInterceptorClass(), interceptor);
        return interceptor;
    }

    public Interceptor getInterceptor(final String className) {
        return interceptors.get(className);
    }
}
