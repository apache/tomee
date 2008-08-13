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
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.BeanTypes;
import org.apache.openejb.OpenEJBException;

public class StatelessContainer implements ContainerProvider, BeanNameAware {
    private String id;
    private String beanName;
    private String provider;
    private String passivator;
    private Integer timeOut;
    private Integer poolSize;
    private Boolean strictPooling;

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

    public String getPassivator() {
        return passivator;
    }

    public void setPassivator(String passivator) {
        this.passivator = passivator;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public Boolean getStrictPooling() {
        return strictPooling;
    }

    public void setStrictPooling(Boolean strictPooling) {
        this.strictPooling = strictPooling;
    }

    public Container getContainerDefinition() throws OpenEJBException {
        Container container = new Container();
        container.setCtype(BeanTypes.STATELESS);

        if (id != null) {
            container.setId(id);
        } else if (beanName != null) {
            container.setId(beanName);
        } else {
            throw new OpenEJBException("No id defined for StatelessContainer");
        }

        if (provider != null) {
            container.setProvider(provider);
        }

        Properties properties = container.getProperties();
        if (passivator != null) {
            properties.put("Passivator", passivator);
        }
        if (timeOut != null) {
            properties.put("TimeOut", timeOut);
        }
        if (poolSize != null) {
            properties.put("PoolSize", poolSize);
        }
        if (strictPooling != null) {
            properties.put("StrictPooling", strictPooling);
        }
        return container;
    }
}