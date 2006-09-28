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
package org.apache.openejb.core.ivm;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.apache.openejb.ProxyInfo;
import org.apache.openejb.core.entity.EntityEjbHomeHandler;
import org.apache.openejb.core.stateful.StatefulEjbHomeHandler;
import org.apache.openejb.core.stateless.StatelessEjbHomeHandler;
import org.apache.openejb.util.proxy.ProxyManager;

public class IntraVmServer implements org.apache.openejb.spi.ApplicationServer {

    public EJBMetaData getEJBMetaData(ProxyInfo pi) {
        org.apache.openejb.DeploymentInfo di = pi.getDeploymentInfo();
        IntraVmMetaData metaData = new IntraVmMetaData(di.getHomeInterface(), di.getRemoteInterface(), di.getComponentType());

        metaData.setEJBHome(getEJBHome(pi));
        return metaData;
    }

    public Handle getHandle(ProxyInfo pi) {
        return new IntraVmHandle(getEJBObject(pi));
    }

    public HomeHandle getHomeHandle(ProxyInfo pi) {
        return new IntraVmHandle(getEJBHome(pi));
    }

    public EJBObject getEJBObject(ProxyInfo pi) {
        EjbHomeProxyHandler handler = null;
        return (EJBObject) getEjbHomeHandler(pi).createProxy(pi);
    }

    public EJBHome getEJBHome(ProxyInfo pi) {

        if (pi.getDeploymentInfo() instanceof org.apache.openejb.core.CoreDeploymentInfo) {
            org.apache.openejb.core.CoreDeploymentInfo coreDeployment = (org.apache.openejb.core.CoreDeploymentInfo) pi.getDeploymentInfo();
            return coreDeployment.getEJBHome();

        } else {
            try {
                Class[] interfaces = new Class[]{pi.getDeploymentInfo().getHomeInterface(), org.apache.openejb.core.ivm.IntraVmProxy.class};
                return (javax.ejb.EJBHome) ProxyManager.newProxyInstance(interfaces, getEjbHomeHandler(pi));
            } catch (Exception e) {
                throw new RuntimeException("Can't create EJBHome stub" + e.getMessage());
            }
        }
    }

    private EjbHomeProxyHandler getEjbHomeHandler(ProxyInfo pi) {

        switch (pi.getDeploymentInfo().getComponentType()) {

            case org.apache.openejb.DeploymentInfo.BMP_ENTITY:
            case org.apache.openejb.DeploymentInfo.CMP_ENTITY:
                return new EntityEjbHomeHandler(pi.getBeanContainer(), pi.getPrimaryKey(), pi.getDeploymentInfo().getDeploymentID());

            case org.apache.openejb.DeploymentInfo.STATEFUL:
                return new StatefulEjbHomeHandler(pi.getBeanContainer(), pi.getPrimaryKey(), pi.getDeploymentInfo().getDeploymentID());

            case org.apache.openejb.DeploymentInfo.STATELESS:
                return new StatelessEjbHomeHandler(pi.getBeanContainer(), pi.getPrimaryKey(), pi.getDeploymentInfo().getDeploymentID());
            default:
                throw new RuntimeException("Unknown EJB type: " + pi.getDeploymentInfo());
        }
    }
}
