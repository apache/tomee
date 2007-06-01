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

import org.apache.openejb.client.EJBHomeProxyHandle;
import org.apache.openejb.client.EJBHomeHandler;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBObjectProxyHandle;
import org.apache.openejb.client.EJBObjectHandler;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.util.Logger;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
public class ServerSideResolver implements EJBHomeProxyHandle.Resolver, EJBObjectProxyHandle.Resolver {

    private static Logger logger = Logger.getInstance("OpenEJB.server.remote", "org.apache.openejb.server.util.resources");

    public Object resolve(EJBHomeHandler handler) {
        try {
            EJBMetaDataImpl ejb = handler.getEjb();

            InterfaceType interfaceType = (ejb.getRemoteInterfaceClass() == null)? InterfaceType.BUSINESS_REMOTE_HOME : InterfaceType.EJB_HOME;

            ArrayList<Class> interfaces = new ArrayList<Class>();
            if (interfaceType.isBusiness()){
                interfaces.addAll(ejb.getBusinessClasses());
            } else {
                interfaces.add(ejb.getRemoteInterfaceClass());
            }

            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            DeploymentInfo deploymentInfo = containerSystem.getDeploymentInfo(ejb.getDeploymentID());

            return EjbHomeProxyHandler.createHomeProxy(deploymentInfo, interfaceType, interfaces);
        } catch (Exception e) {
            logger.error("ServerSideResolver.resolve() failed, falling back to ClientSideResolver: "+e.getClass().getName()+": "+e.getMessage(), e );
            return new EJBHomeProxyHandle.ClientSideResovler().resolve(handler);
        }
    }

    public Object resolve(EJBObjectHandler handler) {
        try {
            EJBMetaDataImpl ejb = handler.getEjb();

            InterfaceType interfaceType = (ejb.getRemoteInterfaceClass() == null)? InterfaceType.BUSINESS_REMOTE_HOME : InterfaceType.EJB_HOME;

            ArrayList<Class> interfaces = new ArrayList<Class>();
            if (interfaceType.isBusiness()){
                interfaces.addAll(ejb.getBusinessClasses());
            } else {
                interfaces.add(ejb.getRemoteInterfaceClass());
            }

            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            DeploymentInfo deploymentInfo = containerSystem.getDeploymentInfo(ejb.getDeploymentID());

            return EjbObjectProxyHandler.createProxy(deploymentInfo, handler.getPrimaryKey(), interfaceType, interfaces);
        } catch (Exception e) {
            logger.error("ServerSideResolver.resolve() failed, falling back to ClientSideResolver: "+e.getClass().getName()+": "+e.getMessage(), e );
            return new EJBObjectProxyHandle.ClientSideResovler().resolve(handler);
        }
    }
}
