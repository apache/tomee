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
package org.apache.openejb.monitoring;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class ObjectNameBuilder {

    private Map<String, String> map;
    private String domain;

    public ObjectNameBuilder() {
        this("default");
    }

    public ObjectNameBuilder(String domain) {
        map = new LinkedHashMap<String, String>();
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public ObjectName build() {
        try {
            StringBuilder sb = new StringBuilder(domain + ":");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));

            return new ObjectName(sb.toString());
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e);
        }
    }

    public ObjectNameBuilder set(String key, String value) {
        map.put(key, value);
        return this;
    }

    public ObjectNameBuilder copy() {
        ObjectNameBuilder builder = new ObjectNameBuilder();
        builder.domain = this.domain;
        builder.map.putAll(this.map);
        return builder;
    }
}
