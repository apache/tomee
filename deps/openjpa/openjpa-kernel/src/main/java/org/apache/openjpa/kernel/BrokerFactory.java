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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.util.Closeable;

/**
 * Factory for {@link Broker} instances.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface BrokerFactory
    extends Serializable, Closeable {

    /**
     * Return the configuration for this factory.
     */
    public OpenJPAConfiguration getConfiguration();

    /**
     * Return properties describing this runtime.
     */
    public Map<String,Object> getProperties();
    
    /**
     * Return all of the supported properties as a set of keys. If a property
     * has multiple keys, all keys will be returned.
     * 
     * @since 2.0.0
     */
    public Set<String>  getSupportedProperties();

    /**
     * Put the specified key-value pair into the map of user objects.
     */
    public Object putUserObject(Object key, Object val);

    /**
     * Get the value for the specified key from the map of user objects.
     */
    public Object getUserObject(Object key);

    /**
     * Return a broker with default settings.
     */
    public Broker newBroker();

    /**
     * Return a broker using the given credentials and in the given
     * transaction and connection retain mode, optionally finding
     * existing broker in the global transaction.
     */
    public Broker newBroker(String user, String pass, boolean managed,
        int connRetainMode, boolean findExisting);
    
    /**
     * Return a new broker using the supplied
     * <ul>
     * <li>credentials</li>
     * <li>transaction management mode</li>
     * <li>connectionRetainMode</li>
     * <li>connectionFactories</li>
     * </ul>
     * 
     * @param user  Username to use when obtaining a connection. Will be ignored if a connection factory is 
     *     obtained from JNDI.
     * @param pass  Password to use when obtaining a connection. Will be ignored if a connection factory is 
     *     obtained from JNDI.
     * @param managed Whether managed transactions will be used by this Broker
     * @param connRetainMode {@link ConnectionRetainMode}
     * @param findExisting Whether the internal pool of brokers should be used. 
     * @param cfName  JTA ConnectionFactory to use
     * @param cf2Name  Non-JTA ConnectionFactory to use. 
     * @return A Broker which matches the provided criteria.
     */
    public Broker newBroker(String user, String pass, boolean managed,
        int connRetainMode, boolean findExisting, String cfName, String cf2Name);

    /**
     * Register a listener for lifecycle-related events on the specified
     * classes. If the classes are null, all events will be propagated to
     * the listener. The listener will be passed on to all new brokers.
     *
     * @since 0.3.3
     */
    public void addLifecycleListener(Object listener, Class<?>[] classes);

    /**
     * Remove a listener for lifecycle-related events.
     *
     * @since 0.3.3
     */
    public void removeLifecycleListener(Object listener);

    /**
     * Register a listener for transaction-related events on the specified
     * classes. It will be registered with all {@link Broker}s created
     * from this instance moving forward.
     *
     * @since 1.0.0
     */
    public void addTransactionListener(Object listener);

    /**
     * Remove a listener for transaction-related events. It will no longer
     * be registered with new {@link Broker}s created from this instance.
     *
     * @since 1.0.0
     */
    public void removeTransactionListener(Object listener);

    /**
     * Close the factory.
     */
    public void close();

    /**
     * Returns true if this broker factory is closed.
     */
    public boolean isClosed();

    /**
     * Synchronizes on an internal lock.
     */
    public void lock();

    /**
     * Release the internal lock.
     */
    public void unlock ();
    
    /**
     * assert that this broker is open. If the broker has been closed an IllegalStateException will be thrown
     * with information on when the broker was closed. 
     */
    public void assertOpen(); 
    
    /**
     * This method is invoked AFTER a BrokerFactory has been instantiated. 
     */
    public void postCreationCallback();
}
