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

import java.util.Collection;
import java.util.NoSuchElementException;

import org.apache.openjpa.lib.util.Localizer;

/**
 * Abstract base class for read-only result lists.
 *
 * @author Abe White
 * @nojavadoc
 */
@SuppressWarnings("serial")
public abstract class AbstractResultList<E> implements ResultList<E> {
    private transient Object _userObject;
    
    private static final Localizer _loc = Localizer.forPackage
        (AbstractResultList.class);

    public void add(int index, Object element) {
        throw readOnly();
    }

    private UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException(_loc.get("read-only")
            .getMessage());
    }

    public boolean add(Object o) {
        throw readOnly();
    }

    public boolean addAll(Collection<? extends E> c) {
        throw readOnly();
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        throw readOnly();
    }

    public E remove(int index) {
        throw readOnly();
    }

    public boolean remove(Object o) {
        throw readOnly();
    }

    public boolean removeAll(Collection<?> c) {
        throw readOnly();
    }

    public boolean retainAll(Collection<?> c) {
        throw readOnly();
    }

    public E set(int index, Object element) {
        throw readOnly();
    }

    public void clear() {
        throw readOnly();
    }

    protected void assertOpen() {
        if (isClosed())
            throw new NoSuchElementException(_loc.get("closed").getMessage());
    }
    
    public final Object getUserObject() {
        return _userObject;
    }
    
    public final void setUserObject(Object opaque) {
        _userObject = opaque;
    }
    
}
