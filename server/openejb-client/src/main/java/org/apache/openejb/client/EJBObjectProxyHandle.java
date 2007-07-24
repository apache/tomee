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

    public static ThreadLocal<Resolver> resolver = new DefaultedThreadLocal<Resolver>(new ClientSideResovler());

    EJBObjectHandler handler;

    public EJBObjectProxyHandle() {
    }

    public EJBObjectProxyHandle(EJBObjectHandler handler) {
        this.handler = handler;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        handler.client.writeExternal(out);

        handler.ejb.writeExternal(out);

        handler.server.writeExternal(out);
        out.writeObject(handler.primaryKey);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte version = in.readByte(); // future use

        ClientMetaData client = new ClientMetaData();
        EJBMetaDataImpl ejb = new EJBMetaDataImpl();
        ServerMetaData server = new ServerMetaData();

        client.readExternal(in);

        ejb.readExternal(in);

        server.readExternal(in);

        Object primaryKey = in.readObject();

        handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey);

    }

    private Object readResolve() throws ObjectStreamException {
        return resolver.get().resolve(handler);
    }

    public static interface Resolver {
        Object resolve(EJBObjectHandler handler);
    }

    public static class ClientSideResovler implements Resolver {
        public Object resolve(EJBObjectHandler handler) {
            return handler.createEJBObjectProxy();
        }
    }
}
