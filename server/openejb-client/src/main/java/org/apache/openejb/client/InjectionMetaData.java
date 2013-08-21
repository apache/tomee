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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class InjectionMetaData implements Externalizable {

    private static final long serialVersionUID = -4034793010942105791L;
    private final List<Injection> injections = new ArrayList<Injection>();
    private transient ProtocolMetaData metaData;

    public InjectionMetaData() {
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    public List<Injection> getInjections() {
        return injections;
    }

    public void addInjection(final String target, final String name, final String jndiName) {
        injections.add(new Injection(target, name, jndiName));
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final String jndiName = (String) in.readObject();
            final String name = (String) in.readObject();
            final String target = (String) in.readObject();
            addInjection(target, name, jndiName);
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeInt(injections.size());
        for (final Injection injection : injections) {
            out.writeObject(injection.getJndiName());
            out.writeObject(injection.getName());
            out.writeObject(injection.getTargetClass());
        }
    }

}
