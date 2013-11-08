/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.rop;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.commons.lang.ObjectUtils;

/**
 * Abstract base class for sequential result lists. Unlike the
 * {@link AbstractSequentialList}, this class doesn't rely on the
 * {@link Collection#size} method.
 *
 * @author Abe White
 * @nojavadoc
 */
public abstract class AbstractSequentialResultList extends AbstractResultList {

    /**
     * Implement this method and {@link #size}.
     */
    protected abstract ListIterator itr(int index);

    public boolean contains(Object o) {
        assertOpen();
        for (Iterator itr = itr(0); itr.hasNext();)
            if (ObjectUtils.equals(o, itr.next()))
                return true;
        return false;
    }

    public boolean containsAll(Collection c) {
        assertOpen();
        for (Iterator itr = c.iterator(); itr.hasNext();)
            if (!contains(itr.next()))
                return false;
        return true;
    }

    public Object get(int index) {
        assertOpen();
        return itr(index).next();
    }

    public int indexOf(Object o) {
        assertOpen();
        int index = 0;
        for (Iterator itr = itr(0); itr.hasNext(); index++)
            if (ObjectUtils.equals(o, itr.next()))
                return index;
        return -1;
    }

    public int lastIndexOf(Object o) {
        assertOpen();
        int index = -1;
        int i = 0;
        for (Iterator itr = itr(0); itr.hasNext(); i++)
            if (ObjectUtils.equals(o, itr.next()))
                index = i;
        return index;
    }

    public boolean isEmpty() {
        assertOpen();
        return !itr(0).hasNext();
    }

    public Iterator iterator() {
        return listIterator();
    }

    public ListIterator listIterator() {
        return listIterator(0);
    }

    public ListIterator listIterator(int index) {
        return new ResultListIterator(itr(index), this);
    }

    public Object[] toArray() {
        assertOpen();
        ArrayList list = new ArrayList();
        for (Iterator itr = itr(0); itr.hasNext();)
            list.add(itr.next());
        return list.toArray();
    }

    public Object[] toArray(Object[] a) {
        assertOpen();
        ArrayList list = new ArrayList();
        for (Iterator itr = itr(0); itr.hasNext();)
            list.add(itr.next());
        return list.toArray(a);
    }
}
