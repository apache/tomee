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

import org.apache.openejb.client.serializer.EJBDSerializer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ClientMetaData implements Externalizable {

    private static final long serialVersionUID = -8769170505291957783L;
    transient Object clientIdentity;
    private transient EJBDSerializer serializer;
    private transient ProtocolMetaData metaData;

    public ClientMetaData() {
    }

    public ClientMetaData(final Object identity) {
        this.clientIdentity = identity;
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    public Object getClientIdentity() {
        return clientIdentity;
    }

    public void setClientIdentity(final Object clientIdentity) {
        this.clientIdentity = clientIdentity;
    }

    public EJBDSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(final EJBDSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

        final byte version = in.readByte(); // future use
        this.clientIdentity = in.readObject();

        if (null == metaData || metaData.isAtLeast(4, 6)) {
            if (in.readBoolean()) {
                try {
                    serializer = EJBDSerializer.class.cast(Thread.currentThread().getContextClassLoader().loadClass(in.readUTF()).newInstance());
                } catch (final Exception e) {
                    // no-op
                }
            }
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);
        out.writeObject(clientIdentity);

        if (null == metaData || metaData.isAtLeast(4, 6)) {

            out.writeBoolean(serializer != null);
            if (serializer != null) {
                out.writeUTF(serializer.getClass().getName());
            }
        }
    }
}
