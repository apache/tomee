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
 * The interceptorsType element declares one or more interceptor
 * classes used by components within this ejb-jar.  The declaration
 * consists of :
 * <p/>
 * - An optional description.
 * - One or more interceptor elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interceptorsType", propOrder = {
        "description",
        "interceptor"
        })
public class Interceptors {

    @XmlElement(required = true)
    protected List<Text> description;

    @XmlTransient
    protected Map<String,Interceptor> interceptors = new LinkedHashMap<String,Interceptor>();

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    @XmlElement(name = "interceptor", required = true)
    public Interceptor[] getInterceptor() {
        return interceptors.values().toArray(new Interceptor[]{});
    }

    public void setInterceptor(Interceptor[] v) {
        interceptors.clear();
        for (Interceptor e : v) addInterceptor(e);
    }

    public Interceptor addInterceptor(Interceptor interceptor){
        interceptors.put(interceptor.getInterceptorClass(), interceptor);
        return interceptor;
    }

    public Interceptor getInterceptor(String className){
        return interceptors.get(className);
    }
}
