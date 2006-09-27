/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.util.Enumeration;
import java.util.Vector;
import java.util.NoSuchElementException;
import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

/**
 * @version $Revision$ $Date$
 */
public final class ArrayEnumeration implements Enumeration, Externalizable {
    static final long serialVersionUID = -1194966576855523042L;

    private Object[] elements;
    private int elementsIndex;

    public ArrayEnumeration(Vector elements) {
        this.elements = new Object[elements.size()];
        elements.copyInto(this.elements);
    }

    public ArrayEnumeration(java.util.List list) {
        this.elements = new Object[list.size()];
        list.toArray(this.elements);
    }

    public ArrayEnumeration() {
    }

    public Object get(int index) {
        return elements[index];
    }

    public void set(int index, Object o) {
        elements[index] = o;
    }

    public int size() {
        return elements.length;
    }

    public boolean hasMoreElements() {
        return (elementsIndex < elements.length);
    }

    public Object nextElement() {
        if (!hasMoreElements()) throw new NoSuchElementException("No more elements exist");
        return elements[elementsIndex++];
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(elements.length);
        out.writeInt(elementsIndex);
        for (int i = 0; i < elements.length; i++) {
            out.writeObject(elements[i]);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        elements = new Object[in.readInt()];
        elementsIndex = in.readInt();
        for (int i = 0; i < elements.length; i++) {
            elements[i] = in.readObject();
        }
    }

}
