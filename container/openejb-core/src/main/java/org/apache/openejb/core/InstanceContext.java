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

package org.apache.openejb.core;

import org.apache.openejb.BeanContext;

import jakarta.enterprise.context.spi.CreationalContext;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class InstanceContext {

    private final BeanContext beanContext;
    private final Object bean;
    private final Map<String, Object> interceptors;
    private final CreationalContext creationalContext;

    /**
     * A slot where the container can put instance-specific
     * data without having to wrap this object
     */
    private Object instanceData;

    public InstanceContext(final BeanContext beanContext, final Object bean, final Map<String, Object> interceptors, final CreationalContext creationalContext) {
        this.beanContext = beanContext;
        this.bean = bean;
        this.interceptors = interceptors;
        this.creationalContext = creationalContext;
    }

    public CreationalContext getCreationalContext() {
        return creationalContext;
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    public Object getBean() {
        return bean;
    }

    public Map<String, Object> getInterceptors() {
        return interceptors;
    }

    public Object getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(final Object instanceData) {
        this.instanceData = instanceData;
    }
}
