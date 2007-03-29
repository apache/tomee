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

import java.io.ObjectStreamException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.loader.SystemInstance;

public class IntraVmHandle implements java.io.Serializable, javax.ejb.HomeHandle, javax.ejb.Handle {
    protected Object theProxy;

    public IntraVmHandle(Object proxy) {
        this.theProxy = proxy;
    }

    public EJBHome getEJBHome() {
        return (EJBHome) theProxy;
    }

    public EJBObject getEJBObject() {
        return (EJBObject) theProxy;
    }

    public Object getPrimaryKey() {
        return ((BaseEjbProxyHandler) org.apache.openejb.util.proxy.ProxyManager.getInvocationHandler(theProxy)).primaryKey;
    }

    protected Object writeReplace() throws ObjectStreamException {
        /*
         * If the handle is being  copied between bean instances in a RPC
         * call we use the IntraVmArtifact
         */
        if (IntraVmCopyMonitor.isIntraVmCopyOperation()) {
            return new IntraVmArtifact(this);
            /*
            * If the handle is referenced by a stateful bean that is being
            * passivated by the container, we allow this object to be serialized.
            */
        } else if (IntraVmCopyMonitor.isStatefulPassivationOperation()) {
            return this;
            /*
            * If the proxy is being copied between class loaders
            * we allow this object to be serialized.
            */
        } else if (IntraVmCopyMonitor.isCrossClassLoaderOperation()) {
            return this;
            /*
            * If the handle is serialized outside the core container system, we
            * allow the application server to handle it.
            */
        } else {
            BaseEjbProxyHandler handler = (BaseEjbProxyHandler) ProxyManager.getInvocationHandler(theProxy);
            if (theProxy instanceof javax.ejb.EJBObject) {
                ApplicationServer applicationServer = SystemInstance.get().getComponent(ApplicationServer.class);
                return applicationServer.getHandle(handler.getProxyInfo());
            } else if (theProxy instanceof javax.ejb.EJBHome) {
                ApplicationServer applicationServer = SystemInstance.get().getComponent(ApplicationServer.class);
                return applicationServer.getHomeHandle(handler.getProxyInfo());
            } else {
                throw new RuntimeException("Invalid proxy type. Handles are only supported by EJBObject types in EJB 1.1");
            }
        }
    }

}