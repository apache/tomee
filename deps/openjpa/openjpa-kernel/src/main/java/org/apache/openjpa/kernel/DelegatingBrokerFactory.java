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

import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

///////////////////////////////////////////////////////////////
// NOTE: when adding a public API method, be sure to add it to 
// JDO and JPA facades!
///////////////////////////////////////////////////////////////

/**
 * Delegating broker factory that can also perform exception translation
 * for use in facades.
 *
 * @since 0.4.0
 * @author Abe White
 * @nojavadoc
 */
public class DelegatingBrokerFactory
    implements BrokerFactory {

    private final BrokerFactory _factory;
    private final DelegatingBrokerFactory _del;
    private final RuntimeExceptionTranslator _trans;

    /**
     * Constructor; supply delegate.
     */
    public DelegatingBrokerFactory(BrokerFactory factory) {
        this(factory, null);
    }

    /**
     * Constructor; supply delegate and exception translator.
     */
    public DelegatingBrokerFactory(BrokerFactory factory,
        RuntimeExceptionTranslator trans) {
        _factory = factory;
        if (factory instanceof DelegatingBrokerFactory)
            _del = (DelegatingBrokerFactory) factory;
        else
            _del = null;
        _trans = trans;
    }

    /**
     * Return the direct delegate.
     */
    public BrokerFactory getDelegate() {
        return _factory;
    }

    /**
     * Return the native delegate.
     */
    public BrokerFactory getInnermostDelegate() {
        return (_del == null) ? _factory : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingBrokerFactory)
            other = ((DelegatingBrokerFactory) other).getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    /**
     * Translate the OpenJPA exception.
     */
    protected RuntimeException translate(RuntimeException re) {
        return (_trans == null) ? re : _trans.translate(re);
    }

    public OpenJPAConfiguration getConfiguration() {
        try {
            return _factory.getConfiguration();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Map<String,Object> getProperties() {
        try {
            return _factory.getProperties();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public Set<String> getSupportedProperties() {
        try {
            return _factory.getSupportedProperties();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object putUserObject(Object key, Object val) {
        try {
            return _factory.putUserObject(key, val);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object getUserObject(Object key) {
        try {
            return _factory.getUserObject(key);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Broker newBroker() {
        try {
            return _factory.newBroker();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Broker newBroker(String user, String pass, boolean managed,
        int connRetainMode, boolean findExisting) {
        return newBroker(user, pass, managed, connRetainMode, findExisting, "", "");
    }
    public Broker newBroker(String user, String pass, boolean managed,
        int connRetainMode, boolean findExisting, String cfName, String cf2Name) {
        try {
            return _factory.newBroker(user, pass, managed, connRetainMode,
                findExisting, cfName, cf2Name);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void addLifecycleListener(Object listener, Class[] classes) {
        try {
            _factory.addLifecycleListener(listener, classes);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void removeLifecycleListener(Object listener) {
        try {
            _factory.removeLifecycleListener(listener);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void addTransactionListener(Object listener) {
        try {
            _factory.addTransactionListener(listener);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void removeTransactionListener(Object listener) {
        try {
            _factory.removeTransactionListener(listener);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void close() {
        try {
            _factory.close();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean isClosed() {
        try {
            return _factory.isClosed();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void lock() {
        try {
            _factory.lock();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void unlock() {
        try {
            _factory.unlock();
        } catch (RuntimeException re) {
            throw translate(re);
		}
	}
    
    public void assertOpen() {
        try {
            _factory.assertOpen();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public void postCreationCallback() {
        try {
            _factory.postCreationCallback();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
}
