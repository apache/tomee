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
import javax.sql.DataSource;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.springframework.beans.factory.FactoryBean;

@Exported
public class OpenEJBDataSource implements FactoryBean {
    private String resourceId;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Object getObject() throws Exception {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (containerSystem == null) return null;
        Context initialContext = containerSystem.getJNDIContext();
        if (initialContext == null) return null;

        DataSource dataSource = (DataSource) initialContext.lookup("openejb/Resource/" + resourceId);
        return dataSource;
    }

    public Class getObjectType() {
        return DataSource.class;
    }

    public boolean isSingleton() {
        return false;
    }
}
