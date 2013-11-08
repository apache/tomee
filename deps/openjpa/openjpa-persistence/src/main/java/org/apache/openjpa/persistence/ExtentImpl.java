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
package org.apache.openjpa.persistence;

import java.util.Iterator;
import java.util.List;

import org.apache.openjpa.kernel.DelegatingExtent;

/**
 * An extent is a logical view of all instances of a class.
 *
 * @author Abe White
 * @since 0.4.1
 * @nojavadoc
 */
public class ExtentImpl<T>
    implements Extent<T> {

    private final EntityManagerImpl _em;
    private final DelegatingExtent _extent;
    private FetchPlan _fetch = null;

    /**
     * Constructor; supply delegate.
     */
    public ExtentImpl(EntityManagerImpl em, org.apache.openjpa.kernel.Extent<T> extent) {
        _em = em;
        _extent = new DelegatingExtent(extent,
            PersistenceExceptions.getRollbackTranslator(em));
    }

    /**
     * Delegate.
     */
    public org.apache.openjpa.kernel.Extent<T> getDelegate() {
        return _extent.getDelegate();
    }

    public Class<T> getElementClass() {
        return _extent.getElementType();
    }

    public boolean hasSubclasses() {
        return _extent.hasSubclasses();
    }

    public OpenJPAEntityManager getEntityManager() {
        return _em;
    }

    public FetchPlan getFetchPlan() {
        _em.assertNotCloseInvoked();
        _extent.lock();
        try {
            if (_fetch == null)
                _fetch = ((EntityManagerFactoryImpl) _em.
                    getEntityManagerFactory()).toFetchPlan(_extent.getBroker(),
                    _extent.getFetchConfiguration());
            return _fetch;
        } finally {
            _extent.unlock();
        }
    }

    public boolean getIgnoreChanges() {
        return _extent.getIgnoreChanges();
    }

    public void setIgnoreChanges(boolean ignoreChanges) {
        _em.assertNotCloseInvoked();
        _extent.setIgnoreChanges(ignoreChanges);
    }

    public List<T> list() {
        _em.assertNotCloseInvoked();
        return _extent.list();
    }

    public Iterator<T> iterator() {
        _em.assertNotCloseInvoked();
        return _extent.iterator();
    }

    public void closeAll() {
        _extent.closeAll();
    }

    public int hashCode() {
        return ((_extent == null) ? 0  : _extent.hashCode());
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if ((other == null) || (other.getClass() != this.getClass()))
            return false;
        if (_extent == null)
        	return false;
        
        return _extent.equals(((ExtentImpl) other)._extent);
	}
}
