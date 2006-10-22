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

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.BeanType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.util.proxy.ProxyManager;

public class IntraVmMetaData implements javax.ejb.EJBMetaData, java.io.Serializable {

    protected Class homeClass;

    protected Class remoteClass;

    protected Class keyClass;

    protected EJBHome homeStub;

    protected BeanType type;

    public IntraVmMetaData(Class homeInterface, Class remoteInterface, BeanType typeOfBean) {
        this(homeInterface, remoteInterface, null, typeOfBean);
    }

    public IntraVmMetaData(Class homeInterface, Class remoteInterface, Class primaryKeyClass, BeanType typeOfBean) {
        this.type = typeOfBean;
        if (homeInterface == null || remoteInterface == null) {
            throw new IllegalArgumentException();
        }
        if (typeOfBean.isEntity() && primaryKeyClass == null) {
            throw new IllegalArgumentException("Entity beans must have a primary key class");
        }
        type = typeOfBean;
        homeClass = homeInterface;
        remoteClass = remoteInterface;
        keyClass = primaryKeyClass;
    }

    public Class getHomeInterfaceClass() {
        return homeClass;
    }

    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }

    public Class getPrimaryKeyClass() {
        if (type.isEntity())
            return keyClass;
        else
            throw new UnsupportedOperationException("Session objects are private resources and do not have primary keys");
    }

    public boolean isSession() {
        return type.isSession();
    }

    public boolean isStatelessSession() {
        return type == BeanType.STATELESS;
    }

    public void setEJBHome(EJBHome home) {
        homeStub = home;
    }

    public javax.ejb.EJBHome getEJBHome() {
        return homeStub;
    }

    protected Object writeReplace() throws ObjectStreamException {

        /*
         * If the meta data is being  copied between bean instances in a RPC
         * call we use the IntraVmArtifact
         */
        if (IntraVmCopyMonitor.isIntraVmCopyOperation()) {
            return new IntraVmArtifact(this);
            /*
            * If the meta data is referenced by a stateful bean that is being
            * passivated by the container, we allow this object to be serialized.
            */
        } else if (IntraVmCopyMonitor.isStatefulPassivationOperation()) {
            return this;
            /*
            * If the meta data is serialized outside the core container system,
            * we allow the application server to handle it.
            */
        } else {
            BaseEjbProxyHandler handler = (BaseEjbProxyHandler) ProxyManager.getInvocationHandler(homeStub);
            return ((ApplicationServer) SystemInstance.get().getComponent(ApplicationServer.class)).getEJBMetaData(handler.getProxyInfo());
        }
    }
}