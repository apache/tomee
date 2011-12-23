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
package org.apache.openejb.spring;

import java.util.Properties;

import org.springframework.beans.factory.BeanNameAware;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.Resource;

public abstract class AbstractResourceProvider implements ResourceProvider, BeanNameAware {
    private String id;
    private String beanName;
    private String provider;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public abstract String getType();

    public abstract Properties getProperties();

    public Resource getResourceDefinition() throws OpenEJBException {
        Resource resource = new Resource();

        if (id != null) {
            resource.setId(id);
        } else if (beanName != null) {
            resource.setId(beanName);
        } else {
            throw new OpenEJBException("No id defined for Resource");
        }

        if (provider != null) {
            resource.setProvider(provider);
        }

        if (getType() != null) {
            resource.setType(getType());
        }

        resource.getProperties().putAll(getProperties());

        return resource;
    }
}
