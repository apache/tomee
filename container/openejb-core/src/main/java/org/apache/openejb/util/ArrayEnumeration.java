/*
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

package org.apache.openejb.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

@SuppressWarnings("UseOfObsoleteCollectionType")
public final class ArrayEnumeration implements Enumeration, Externalizable {

    static final long serialVersionUID = -1194966576855523042L;

    private Object[] elements;
    private int elementsIndex;

    public ArrayEnumeration(final Vector elements) {
        this.elements = new Object[elements.size()];
        elements.copyInto(this.elements);
    }

    public ArrayEnumeration(final List list) {
        this.elements = new Object[list.size()];
        list.toArray(this.elements);
    }

    public ArrayEnumeration(final Set<?> set) {
        this.elements = new Object[set.size()];
        set.toArray(this.elements);
    }

    public ArrayEnumeration() {
    }

    public Object get(final int index) {
        return elements[index];
    }

    public void set(final int index, final Object o) {
        elements[index] = o;
    }

    public int size() {
        return elements.length;
    }

    @Override
    public boolean hasMoreElements() {
        return elementsIndex < elements.length;
    }

    @Override
    public Object nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException("No more elements exist");
        }
        return elements[elementsIndex++];
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(elements.length);
        out.writeInt(elementsIndex);
        for (final Object element : elements) {
            out.writeObject(element);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        elements = new Object[in.readInt()];
        elementsIndex = in.readInt();
        for (int i = 0; i < elements.length; i++) {
            elements[i] = in.readObject();
        }
    }

}

