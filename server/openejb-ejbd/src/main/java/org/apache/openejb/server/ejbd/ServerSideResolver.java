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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.client.EJBHomeHandler;
import org.apache.openejb.client.EJBHomeProxyHandle;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBObjectHandler;
import org.apache.openejb.client.EJBObjectProxyHandle;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
public class ServerSideResolver implements EJBHomeProxyHandle.Resolver, EJBObjectProxyHandle.Resolver {

    @Override
    public Object resolve(final EJBHomeHandler handler) {
        try {
            final EJBMetaDataImpl ejb = handler.getEjb();

            final InterfaceType interfaceType = (ejb.getRemoteInterfaceClass() == null) ? InterfaceType.BUSINESS_REMOTE_HOME : InterfaceType.EJB_HOME;

            final ArrayList<Class> interfaces = new ArrayList<>();
            if (interfaceType.isBusiness()) {
                interfaces.addAll(ejb.getBusinessClasses());
            } else {
                interfaces.add(ejb.getRemoteInterfaceClass());
            }

            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            final BeanContext beanContext = containerSystem.getBeanContext(ejb.getDeploymentID());

            return EjbHomeProxyHandler.createHomeProxy(beanContext, interfaceType, interfaces, ejb.getMainInterface());
        } catch (Exception e) {
            Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources")
                .error("ServerSideResolver.resolve() failed, falling back to ClientSideResolver: " +
                    e.getClass().getName() +
                    ": " +
                    e.getMessage(), e);
            return new EJBHomeProxyHandle.ClientSideResovler().resolve(handler);
        }
    }

    @Override
    public Object resolve(final EJBObjectHandler handler) {
        try {
            final EJBMetaDataImpl ejb = handler.getEjb();

            final InterfaceType interfaceType = (ejb.getRemoteInterfaceClass() == null) ? InterfaceType.BUSINESS_REMOTE_HOME : InterfaceType.EJB_HOME;

            final ArrayList<Class> interfaces = new ArrayList<>();
            if (interfaceType.isBusiness()) {
                interfaces.addAll(ejb.getBusinessClasses());
            } else {
                interfaces.add(ejb.getRemoteInterfaceClass());
            }

            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            final BeanContext beanContext = containerSystem.getBeanContext(ejb.getDeploymentID());

            return EjbObjectProxyHandler.createProxy(beanContext, handler.getPrimaryKey(), interfaceType, interfaces, ejb.getMainInterface());
        } catch (Exception e) {
            Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources")
                .error("ServerSideResolver.resolve() failed, falling back to ClientSideResolver: " +
                    e.getClass().getName() +
                    ": " +
                    e.getMessage(), e);
            return new EJBObjectProxyHandle.ClientSideResovler().resolve(handler);
        }
    }
}
