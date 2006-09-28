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
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.util.proxy.ProxyManager;

public class IntraVmMetaData implements javax.ejb.EJBMetaData, java.io.Serializable {

    final public static byte ENTITY = DeploymentInfo.BMP_ENTITY;

    final public static byte STATEFUL = DeploymentInfo.STATEFUL;

    final public static byte STATELESS = DeploymentInfo.STATELESS;

    protected Class homeClass;

    protected Class remoteClass;

    protected Class keyClass;

    protected EJBHome homeStub;

    protected byte type;

    public IntraVmMetaData(Class homeInterface, Class remoteInterface, byte typeOfBean) {
        this(homeInterface, remoteInterface, null, typeOfBean);
    }

    public IntraVmMetaData(Class homeInterface, Class remoteInterface, Class primaryKeyClass, byte typeOfBean) {
        if (typeOfBean != ENTITY && typeOfBean != STATEFUL && typeOfBean != STATELESS) {
            if (typeOfBean == DeploymentInfo.CMP_ENTITY) {
                typeOfBean = ENTITY;
            } else {
                throw new IllegalArgumentException("typeOfBean parameter not in range: " + typeOfBean);
            }
        }
        if (homeInterface == null || remoteInterface == null) {
            throw new IllegalArgumentException();
        }
        if (typeOfBean == ENTITY && primaryKeyClass == null) {
            throw new IllegalArgumentException();
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
        if (type == ENTITY)
            return keyClass;
        else
            throw new UnsupportedOperationException("Session objects are private resources and do not have primary keys");
    }

    public boolean isSession() {
        return (type == STATEFUL || type == STATELESS);
    }

    public boolean isStatelessSession() {
        return type == STATELESS;
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