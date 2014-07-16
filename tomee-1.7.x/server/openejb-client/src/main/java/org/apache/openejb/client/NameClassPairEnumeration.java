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

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The product of a javax.naming.Context.list() method
 */
public class NameClassPairEnumeration<T extends NameClassPair> implements NamingEnumeration<T>, Externalizable {

    private static final long serialVersionUID = 5678570940990836625L;
    private transient List<NameClassPair> list;
    private transient Iterator<NameClassPair> iterator;
    private transient ProtocolMetaData metaData;

    public NameClassPairEnumeration(final List<NameClassPair> list) {
        this.list = list;
        this.iterator = list.iterator();
    }

    @SuppressWarnings("unchecked")
    public NameClassPairEnumeration() {
        list = Collections.EMPTY_LIST;
        iterator = list.iterator();
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public void close() {
        iterator = null;
    }

    @Override
    public boolean hasMore() {
        return iterator.hasNext();
    }

    @Override
    public boolean hasMoreElements() {
        return hasMore();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        return (T) iterator.next();
    }

    @Override
    public T nextElement() {
        return next();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeInt(list.size());
        for (final NameClassPair pair : list) {
            out.writeObject(pair.getName());
            out.writeObject(pair.getClassName());
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        int size = in.readInt();

        list = new ArrayList<NameClassPair>(size);

        for (; size > 0; size--) {
            final String name = (String) in.readObject();
            final String className = (String) in.readObject();

            list.add(new NameClassPair(name, className));
        }

        iterator = list.iterator();
    }
}
