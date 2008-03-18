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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.proxy.ProxyFactory;

import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import java.util.List;
import java.util.Map;

public class AssemblerTool {

    public static final Map<String, Class> serviceInterfaces = new HashMap<String, Class>();
    static {
        serviceInterfaces.put("ProxyFactory", ProxyFactory.class);
        serviceInterfaces.put("SecurityService", org.apache.openejb.spi.SecurityService.class);
        serviceInterfaces.put("TransactionManager", TransactionManager.class);
        serviceInterfaces.put("ConnectionManager", javax.resource.spi.ConnectionManager.class);
        serviceInterfaces.put("Connector", javax.resource.spi.ManagedConnectionFactory.class);
        serviceInterfaces.put("Resource", javax.resource.spi.ResourceAdapter.class);
        serviceInterfaces.put("Container", org.apache.openejb.Container.class);
    }

    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");
    protected static final SafeToolkit toolkit = SafeToolkit.getToolkit("AssemblerTool");

    protected Properties props = new Properties();

    static {
        System.setProperty("noBanner", "true");
    }

    protected static void checkImplementation(Class intrfce, Class factory, String serviceType, String serviceName) throws OpenEJBException {
        if (!intrfce.isAssignableFrom(factory)) {
            throw new OpenEJBException(messages.format("init.0100", serviceType, serviceName, factory.getName(), intrfce.getName()));
        }
    }

}