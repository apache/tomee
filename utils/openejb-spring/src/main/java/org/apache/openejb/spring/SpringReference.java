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

import org.apache.openejb.core.ivm.naming.Reference;
import org.springframework.context.ApplicationContext;

public class SpringReference extends Reference {
    private final ApplicationContext applicationContext;
    private final String beanName;
    private final Class beanType;

    public SpringReference(ApplicationContext applicationContext, String beanName, Class beanType) {
        this.applicationContext = applicationContext;
        this.beanName = beanName;
        this.beanType = beanType;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class getBeanType() {
        return beanType;
    }

    public Object getObject() {
        Object bean = applicationContext.getBean(beanName);
        return bean;
    }
}
