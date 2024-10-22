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
package org.apache.openejb.jee;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.jba.JndiName;

import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "managed-scheduled-executorType", propOrder = {
        "description",
        "name",
        "contextService",
        "hungTaskThreshold",
        "maxAsync",
        "properties"
})
public class ManagedScheduledExecutor implements Keyable<String> {
    @XmlElement
    protected Description description;
    @XmlElement
    protected JndiName name;
    @XmlElement(name = "context-service-ref")
    protected JndiName contextService;
    @XmlElement(name = "hung-task-threshold")
    protected Long hungTaskThreshold;
    @XmlElement(name = "max-async")
    protected Integer maxAsync;
    @XmlElement(name = "property")
    protected List<Property> properties;

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public JndiName getName() {
        return name;
    }

    public void setName(JndiName name) {
        this.name = name;
    }

    public JndiName getContextService() {
        return contextService;
    }

    public void setContextService(JndiName contextService) {
        this.contextService = contextService;
    }

    public Long getHungTaskThreshold() {
        return hungTaskThreshold;
    }

    public void setHungTaskThreshold(Long hungTaskThreshold) {
        this.hungTaskThreshold = hungTaskThreshold;
    }

    public Integer getMaxAsync() {
        return maxAsync;
    }

    public void setMaxAsync(Integer maxAsync) {
        this.maxAsync = maxAsync;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    @Override
    public String getKey() {
        return name.getvalue();
    }
}
