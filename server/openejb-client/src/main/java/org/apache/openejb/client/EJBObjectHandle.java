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

import jakarta.ejb.EJBObject;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class EJBObjectHandle implements java.io.Externalizable, jakarta.ejb.Handle {

    private static final long serialVersionUID = -4428541526493118024L;
    protected transient EJBObjectProxy ejbObjectProxy;
    protected transient EJBObjectHandler handler;
    private transient ProtocolMetaData metaData;

    public EJBObjectHandle() {
    }

    public EJBObjectHandle(final EJBObjectProxy proxy) {
        this.ejbObjectProxy = proxy;
        this.handler = ejbObjectProxy.getEJBObjectHandler();
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    protected void setEJBObjectProxy(final EJBObjectProxy ejbObjectProxy) {
        this.ejbObjectProxy = ejbObjectProxy;
        this.handler = ejbObjectProxy.getEJBObjectHandler();
    }

    @Override
    public EJBObject getEJBObject() throws RemoteException {
        return (EJBObject) ejbObjectProxy;
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

        out.writeObject(handler.primaryKey);
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

        final Object primaryKey = in.readObject();

        handler = EJBObjectHandler.createEJBObjectHandler(executorService, ejb, server, client, primaryKey, null);
        ejbObjectProxy = handler.createEJBObjectProxy();
    }

    private static String getClassName(final Class clazz) {
        return (clazz == null) ? null : clazz.getName();
    }

    private static Class loadClass(final ClassLoader classLoader, final String homeClassName) throws ClassNotFoundException {
        return (homeClassName == null) ? null : Class.forName(homeClassName, true, classLoader);
    }
}