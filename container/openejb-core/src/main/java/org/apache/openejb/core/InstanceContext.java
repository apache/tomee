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
package org.apache.openejb.core;

import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class InstanceContext {

    private final CoreDeploymentInfo deploymentInfo;
    private final Object bean;
    private final Map<String, Object> interceptors;

    /**
     * A slot where the container can put instance-specific
     * data without having to wrap this object
     */
    private Object instanceData;

    public InstanceContext(CoreDeploymentInfo deploymentInfo, Object bean, Map<String, Object> interceptors) {
        this.deploymentInfo = deploymentInfo;
        this.bean = bean;
        this.interceptors = interceptors;
    }

    public CoreDeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
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

    public void setInstanceData(Object instanceData) {
        this.instanceData = instanceData;
    }
}
