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

import org.apache.openejb.config.BeanTypes;

@Exported
public class SingletonContainer extends AbstractContainerProvider {
    private String accessTimeout;

    public String getAccessTimeout() {
        return accessTimeout;
    }

    public void setAccessTimeout(String accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    protected String getContainerType() {
        return BeanTypes.SINGLETON;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();
        if (accessTimeout != null) {
            properties.setProperty("AccessTimeout", accessTimeout);
        }
        return properties;
    }
}