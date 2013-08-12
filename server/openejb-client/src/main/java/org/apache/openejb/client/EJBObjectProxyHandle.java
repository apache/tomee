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

public class EJBObjectProxyHandle implements Externalizable {

    public static final ThreadLocal<Resolver> resolver = new DefaultedThreadLocal<Resolver>(new ClientSideResovler());
    private static final long serialVersionUID = -5290534267672475715L;

    private transient EJBObjectHandler handler;
    private transient ProtocolMetaData metaData;

    public EJBObjectProxyHandle() {
    }

    public EJBObjectProxyHandle(final EJBObjectHandler handler) {
        this.handler = handler;
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        handler.client.setMetaData(metaData);
        handler.client.writeExternal(out);

        handler.ejb.setMetaData(metaData);
        handler.ejb.writeExternal(out);

        handler.server.setMetaData(metaData);
        handler.server.writeExternal(out);

        out.writeObject(handler.primaryKey);
        out.writeObject(handler.authenticationInfo);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        final ClientMetaData client = new ClientMetaData();
        final EJBMetaDataImpl ejb = new EJBMetaDataImpl();
        final ServerMetaData server = new ServerMetaData();

        client.setMetaData(metaData);
        ejb.setMetaData(metaData);
        server.setMetaData(metaData);

        client.readExternal(in);
        ejb.readExternal(in);
        server.readExternal(in);

        final Object primaryKey = in.readObject();

        final JNDIContext.AuthenticationInfo authenticationInfo = JNDIContext.AuthenticationInfo.class.cast(in.readObject());

        handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey, authenticationInfo);
    }

    private Object readResolve() throws ObjectStreamException {
        return resolver.get().resolve(handler);
    }

    public static interface Resolver {

        Object resolve(EJBObjectHandler handler);
    }

    public static class ClientSideResovler implements Resolver {

        @Override
        public Object resolve(final EJBObjectHandler handler) {
            return handler.createEJBObjectProxy();
        }
    }
}
