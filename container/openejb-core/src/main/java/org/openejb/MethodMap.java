/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import org.openejb.dispatch.InterfaceMethodSignature;

/**
 * @version $Revision$ $Date$
 */
public class MethodMap extends AbstractMap {

    private final MethodMetadata[] methodIndex;
    private final LinkedHashMap signatureIndex;
    private final MethodMapEntrySet entrySet;
    private MethodMapList methodMapList;

    public MethodMap(InterfaceMethodSignature[] methods) {
        methodIndex = new MethodMetadata[methods.length];
        signatureIndex = new LinkedHashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            InterfaceMethodSignature method = methods[i];
            methodIndex[i] = new MethodMetadata(method, null);
            signatureIndex.put(method, new Integer(i));
        }

        entrySet = new MethodMapEntrySet();
    }

    public List valuesList() {
        if (methodMapList == null) {
            methodMapList = new MethodMapList();
        }
        return methodMapList;
    }

    public Collection values() {
        return valuesList();
    }

    public Set entrySet() {
        return entrySet;
    }

    public Object get(int index) {
        if (index < 0 || index >= methodIndex.length) throw new IndexOutOfBoundsException("" + index);
        return methodIndex[index].getValue();
    }

    public Object set(int index, Object value) {
        if (index < 0 || index >= methodIndex.length) throw new IndexOutOfBoundsException("" + index);
        MethodMetadata methodMetadata = methodIndex[index];
        Object oldValue = methodMetadata.getValue();
        methodMetadata.setValue(value);
        return oldValue;
    }

    public Object put(Object key, Object value) {
        if (!(key instanceof InterfaceMethodSignature)) {
            throw new IllegalArgumentException("Key is not an instance of InterfaceMethodSignature");
        }

        InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) key;
        int i = indexOf(methodSignature);
        if (i < 0) {
            throw new IllegalArgumentException("MethodMap does not contain this method and new entries can not be added: " + methodSignature);
        }

        MethodMetadata methodMetadata = methodIndex[i];
        Object oldValue = methodMetadata.getValue();
        methodMetadata.setValue(value);
        return oldValue;
    }

    public boolean containsKey(Object key) {
        return signatureIndex.containsKey(key);
    }

    public int indexOf(InterfaceMethodSignature methodSignature) {
        Integer index = (Integer) signatureIndex.get(methodSignature);
        if (index == null) {
            return -1;
        }
        int i = index.intValue();
        return i;
    }

    public Object get(Object key) {
        if (!(key instanceof InterfaceMethodSignature)) {
            return null;
        }

        InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) key;
        int i = indexOf(methodSignature);
        if (i < 0) {
            return null;
        }

        Object value = methodIndex[i].getValue();
        return value;
    }

    public Iterator iterator() {
        return new MethodMapIterator();
    }

    public ListIterator listIterator() {
        return new MethodMapListIterator(0);
    }

    public ListIterator listIterator(int index) {
        if (index < 0 || index >= methodIndex.length) throw new IndexOutOfBoundsException("" + index);
        return new MethodMapListIterator(index);
    }

    public Object[] toArray() {
        return toArray(new Object[methodIndex.length]);
    }

    public Object[] toArray(Object values[]) {
        if (values.length < methodIndex.length) {
            values = (Object[]) Array.newInstance(values.getClass().getComponentType(), methodIndex.length);
        }

        for (int i = 0; i < methodIndex.length; i++) {
            MethodMetadata methodMetadata = methodIndex[i];
            values[i] = methodMetadata.getValue();
        }
        return values;
    }

    private static class MethodMetadata implements Map.Entry {
        private final InterfaceMethodSignature method;
        private Object value;

        private MethodMetadata(InterfaceMethodSignature method, Object value) {
            this.method = method;
            this.value = value;
        }

        public InterfaceMethodSignature getMethod() {
            return method;
        }

        public Object getKey() {
            return method;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    private class MethodMapEntrySet extends AbstractSet {
        public Iterator iterator() {
            return new Iterator() {
                private int index = 0;
                public boolean hasNext() {
                    return index < methodIndex.length;
                }

                public Object next() {
                    return methodIndex[index++];
                }

                public void remove() {
                    throw new UnsupportedOperationException("MethodMap entries can not be removed");
                }
            };
        }

        public int size() {
            return methodIndex.length;
        }
    }

    private class MethodMapList extends AbstractList {
        public Object get(int index) {
            return MethodMap.this.get(index);
        }

        public Object set(int index, Object element) {
            return MethodMap.this.set(index, element);
        }

        public int size() {
            return MethodMap.this.size();
        }
    }

    private class MethodMapIterator implements Iterator {
        protected int index;

        public boolean hasNext() {
            return index < methodIndex.length;
        }

        public Object next() {
            return methodIndex[index++].getValue();
        }

        public void remove() {
            throw new UnsupportedOperationException("MethodMap entries can not be removed");
        }
    }

    private class MethodMapListIterator extends MethodMapIterator implements ListIterator {
        public MethodMapListIterator(int index) {
            this.index = index;
        }

        public int nextIndex() {
            return index;
        }

        public boolean hasPrevious() {
            return index > 0;
        }

        public Object previous() {
            return methodIndex[--index].getValue();
        }

        public int previousIndex() {
            return index - 1;
        }

        public void set(Object o) {
            methodIndex[index].setValue(o);
        }

        public void add(Object o) {
            throw new UnsupportedOperationException("Entries can not be added to a MethodMap");
        }

    }
}
