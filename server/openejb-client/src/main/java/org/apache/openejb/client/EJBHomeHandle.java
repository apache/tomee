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
package org.apache.openejb.client;

import jakarta.ejb.EJBHome;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class EJBHomeHandle implements java.io.Externalizable, jakarta.ejb.HomeHandle {

    private static final long serialVersionUID = -5221661986423761808L;
    protected transient EJBHomeProxy ejbHomeProxy;
    protected transient EJBHomeHandler handler;
    private transient ProtocolMetaData metaData;

    public EJBHomeHandle() {
    }

    public EJBHomeHandle(final EJBHomeProxy proxy) {
        this.ejbHomeProxy = proxy;
        this.handler = ejbHomeProxy.getEJBHomeHandler();
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    protected void setEJBHomeProxy(final EJBHomeProxy ejbHomeProxy) {
        this.ejbHomeProxy = ejbHomeProxy;
        this.handler = ejbHomeProxy.getEJBHomeHandler();
    }

    @Override
    public EJBHome getEJBHome() throws RemoteException {
        return ejbHomeProxy;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(2);

        final boolean hasExec = handler.executor != null && handler.executor != JNDIContext.globalExecutor();
        out.writeBoolean(hasExec);
        if (hasExec) {
            out.writeInt(handler.executor.getMaximumPoolSize());
            final BlockingQueue<Runnable> queue = handler.executor.getQueue();
            out.writeInt(queue.size() + queue.remainingCapacity());
        }

        handler.client.setMetaData(metaData);
        handler.client.writeExternal(out);

        final EJBMetaDataImpl ejb = handler.ejb;
        out.writeObject(getClassName(ejb.homeClass));
        out.writeObject(getClassName(ejb.remoteClass));
        out.writeObject(getClassName(ejb.keyClass));
        out.writeByte(ejb.type);
        out.writeUTF(ejb.deploymentID);
        out.writeShort(ejb.deploymentCode);

        handler.server.setMetaData(metaData);
        handler.server.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        ThreadPoolExecutor executorService;
        if (version > 1) {
            if (in.readBoolean()) {
                final int queue = in.readInt();
                final BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>((queue < 2 ? 2 : queue));
                final int threads = in.readInt();
                executorService = JNDIContext.newExecutor(threads, blockingQueue);
            } else {
                executorService = null;
            }
        } else {
            executorService = JNDIContext.globalExecutor();
        }

        final ClientMetaData client = new ClientMetaData();
        final EJBMetaDataImpl ejb = new EJBMetaDataImpl();
        final ServerMetaData server = new ServerMetaData();

        client.setMetaData(metaData);
        client.readExternal(in);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        ejb.homeClass = loadClass(classLoader, (String) in.readObject());
        ejb.remoteClass = loadClass(classLoader, (String) in.readObject());
        ejb.keyClass = loadClass(classLoader, (String) in.readObject());
        ejb.type = in.readByte();
        ejb.deploymentID = in.readUTF();
        ejb.deploymentCode = in.readShort();

        server.setMetaData(metaData);
        server.readExternal(in);

        handler = EJBHomeHandler.createEJBHomeHandler(executorService, ejb, server, client, null);
        ejbHomeProxy = handler.createEJBHomeProxy();
    }

    private static String getClassName(final Class clazz) {
        return (clazz == null) ? null : clazz.getName();
    }

    private static Class loadClass(final ClassLoader classLoader, final String homeClassName) throws ClassNotFoundException {
        return (homeClassName == null) ? null : Class.forName(homeClassName, true, classLoader);
    }
}
