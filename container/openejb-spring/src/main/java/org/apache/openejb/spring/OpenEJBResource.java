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

import javax.naming.Context;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.springframework.beans.factory.FactoryBean;

@Exported
public class OpenEJBResource<T> implements FactoryBean {
    private final Class<T> type;
    private String resourceId;

    public OpenEJBResource(Class<T> type) {
        if (type == null) throw new NullPointerException("type is null");
        this.type = type;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public T getObject() throws Exception {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (containerSystem == null) return null;
        Context initialContext = containerSystem.getJNDIContext();
        if (initialContext == null) return null;

        Object resource = initialContext.lookup("openejb/Resource/" + resourceId);

        if (!type.isInstance(resource)) {
            throw new IllegalArgumentException(
                    "Resouce " + getResourceId() + " is not an instance of " + type.getSimpleName() + ", but is " + resource.getClass().getName());
        }

        return type.cast(resource);
    }

    public Class<T> getObjectType() {
        return type;
    }

    public boolean isSingleton() {
        return false;
    }
}
