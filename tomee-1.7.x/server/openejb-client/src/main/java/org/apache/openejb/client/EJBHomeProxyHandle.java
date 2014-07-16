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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class EJBHomeProxyHandle implements Externalizable {

    private static final long serialVersionUID = 1523695567435111622L;
    public static final ThreadLocal<Resolver> resolver = new DefaultedThreadLocal<Resolver>(new ClientSideResovler());

    private transient EJBHomeHandler handler;
    private transient ProtocolMetaData metaData;

    public EJBHomeProxyHandle() {
    }

    public EJBHomeProxyHandle(final EJBHomeHandler handler) {
        this.handler = handler;
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
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
        out.writeObject(ejb.homeClass);
        out.writeObject(ejb.remoteClass);
        out.writeObject(ejb.keyClass);
        out.writeByte(ejb.type);
        out.writeUTF(ejb.deploymentID);
        out.writeShort(ejb.deploymentCode);

        handler.server.setMetaData(metaData);
        handler.server.writeExternal(out);
        ///        out.writeObject( handler.primaryKey );
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

        ejb.homeClass = (Class) in.readObject();
        ejb.remoteClass = (Class) in.readObject();
        ejb.keyClass = (Class) in.readObject();
        ejb.type = in.readByte();
        ejb.deploymentID = in.readUTF();
        ejb.deploymentCode = in.readShort();

        server.setMetaData(metaData);
        server.readExternal(in);

        handler = EJBHomeHandler.createEJBHomeHandler(executorService, ejb, server, client, /* TODO */ null);
    }

    private Object readResolve() throws ObjectStreamException {
        return resolver.get().resolve(handler);
    }

    public static interface Resolver {

        Object resolve(EJBHomeHandler handler);
    }

    public static class ClientSideResovler implements Resolver {

        @Override
        public Object resolve(final EJBHomeHandler handler) {
            handler.ejb.ejbHomeProxy = handler.createEJBHomeProxy();
            return handler.ejb.ejbHomeProxy;
        }
    }
}
