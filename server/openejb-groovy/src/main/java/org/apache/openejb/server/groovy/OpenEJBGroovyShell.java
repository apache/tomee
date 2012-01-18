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
package org.apache.openejb.server.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.openejb.BeanContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.proxy.ProxyEJB;

import java.util.concurrent.ConcurrentHashMap;

public class OpenEJBGroovyShell extends GroovyShell {
    public OpenEJBGroovyShell() {
        super(new Binding(new ConcurrentHashMap<String, Object>())); // to be thread safe
        feedBinding();
    }

    private void feedBinding() {
        final ContainerSystem cs = SystemInstance.get().getComponent(ContainerSystem.class);
        for (BeanContext beanContext : cs.deployments()) {
            if (BeanContext.Comp.class.equals(beanContext.getBeanClass())) {
                continue;
            }

            Object service = null;
            if (beanContext.getBusinessLocalInterface() != null) {
                service = ProxyEJB.proxy(beanContext, beanContext.getBusinessLocalInterfaces().toArray(new Class<?>[beanContext.getBusinessLocalInterfaces().size()]));
            } else if (beanContext.isLocalbean()) {
                service = ProxyEJB.proxy(beanContext, new Class<?>[] { beanContext.getBusinessLocalBeanInterface() });
            } else if (beanContext.getBusinessRemoteInterface() != null) {
                service = ProxyEJB.proxy(beanContext, beanContext.getBusinessRemoteInterfaces().toArray(new Class<?>[beanContext.getBusinessRemoteInterfaces().size()]));
            }

            if (service != null) {
                setVariable(beanContext.getEjbName(), service);
            }
        }
    }
}
