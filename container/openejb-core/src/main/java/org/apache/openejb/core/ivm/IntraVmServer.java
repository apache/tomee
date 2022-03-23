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

package org.apache.openejb.core.ivm;

import org.apache.openejb.BeanContext;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.spi.ApplicationServer;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import jakarta.ejb.HomeHandle;

public class IntraVmServer implements ApplicationServer {

    public EJBMetaData getEJBMetaData(final ProxyInfo pi) {
        final BeanContext beanContext = pi.getBeanContext();
        final IntraVmMetaData metaData = new IntraVmMetaData(beanContext.getHomeInterface(), beanContext.getRemoteInterface(), beanContext.getComponentType());

        metaData.setEJBHome(getEJBHome(pi));
        return metaData;
    }

    public Handle getHandle(final ProxyInfo pi) {
        return new IntraVmHandle(getEJBObject(pi));
    }

    public HomeHandle getHomeHandle(final ProxyInfo pi) {
        return new IntraVmHandle(getEJBHome(pi));
    }

    public EJBObject getEJBObject(final ProxyInfo pi) {
        return (EJBObject) EjbObjectProxyHandler.createProxy(pi.getBeanContext(), pi.getPrimaryKey(), pi.getInterfaceType(), pi.getInterfaces(), pi.getInterface());
    }

    public Object getBusinessObject(final ProxyInfo pi) {
        return EjbObjectProxyHandler.createProxy(pi.getBeanContext(), pi.getPrimaryKey(), pi.getInterfaceType(), pi.getInterfaces(), pi.getInterface());
    }

    public EJBHome getEJBHome(final ProxyInfo pi) {
        return (EJBHome) EjbHomeProxyHandler.createHomeProxy(pi.getBeanContext(), pi.getInterfaceType());
    }

}
