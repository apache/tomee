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
package org.apache.openjpa.kernel;

import java.util.Iterator;
import java.util.List;

import org.apache.openjpa.util.RuntimeExceptionTranslator;

///////////////////////////////////////////////////////////////
// NOTE: when adding a public API method, be sure to add it to 
// JDO and JPA facades!
///////////////////////////////////////////////////////////////

/**
 * Delegating extent that also can perform exception translation for use
 * in facades.
 *
 * @since 0.4.0
 * @author Abe White
 * @nojavadoc
 */
public class DelegatingExtent<T>
    implements Extent<T> {

    private final Extent<T> _extent;
    private final DelegatingExtent<T> _del;
    private final RuntimeExceptionTranslator _trans;

    /**
     * Constructor; supply delegate.
     */
    public DelegatingExtent(Extent<T> extent) {
        this(extent, null);
    }

    /**
     * Constructor; supply delegate and exception translator.
     */
    public DelegatingExtent(Extent<T> extent, RuntimeExceptionTranslator trans) {
        _extent = extent;
        if (extent instanceof DelegatingExtent)
            _del = (DelegatingExtent<T>) extent;
        else
            _del = null;
        _trans = trans;
    }

    /**
     * Return the direct delegate.
     */
    public Extent<T> getDelegate() {
        return _extent;
    }

    /**
     * Return the native delegate.
     */
    public Extent<T> getInnermostDelegate() {
        return (_del == null) ? _extent : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingExtent)
            other = ((DelegatingExtent<T>) other).getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    /**
     * Translate the OpenJPA exception.
     */
    protected RuntimeException translate(RuntimeException re) {
        return (_trans == null) ? re : _trans.translate(re);
    }

    public Class<T> getElementType() {
        try {
            return _extent.getElementType();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean hasSubclasses() {
        try {
            return _extent.hasSubclasses();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Broker getBroker() {
        try {
            return _extent.getBroker();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public FetchConfiguration getFetchConfiguration() {
        try {
            return _extent.getFetchConfiguration();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean getIgnoreChanges() {
        try {
            return _extent.getIgnoreChanges();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void setIgnoreChanges(boolean ignoreCache) {
        try {
            _extent.setIgnoreChanges(ignoreCache);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public List<T> list() {
        try {
            return _extent.list();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Iterator<T> iterator() {
        try {
            return _extent.iterator();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void closeAll() {
        try {
            _extent.closeAll();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void lock() {
        try {
            _extent.lock();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void unlock() {
        try {
            _extent.unlock();
        } catch (RuntimeException re) {
            throw translate(re);
		}
	}
}
