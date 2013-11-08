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
package org.apache.openjpa.conf;


import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;

/**
 * Responsible for marshalling and unmarshalling objects between memory and
 * durable cache.
 *
 * @since 1.1.0
 */
public interface CacheMarshaller {

    /**
     * Load and return an instance of the type handled by this marshaller.
     * If the type implements {@link Configurable}, then this method will invoke
     * {@link Configurable#setConfiguration}, 
     * {@link Configurable#startConfiguration()}, and
     * {@link Configurable#endConfiguration()} on the instance before returning.
     */
    public Object load();

    /**
     * Store <code>o</code> into the cache.
     */
    public void store(Object o);

    /**
     * The id that this marshaller is responsible for.
     * A value for this parameter is required.
     */
    public void setId(String id);

    /**
     * The id that this marshaller is responsible for.
     */
    public String getId();

    /**
     * The {@link ValidationPolicy} that this marshaller should use.
     * A value for this parameter is required. The class will be instantiated
     * via the {@link org.apache.openjpa.lib.conf.Configurations} mechanism,
     * ensuring that if the class implements {@link Configurable} or
     * {@link org.apache.openjpa.lib.conf.GenericConfigurable}, it will be taken
     * through the appropriate lifecycle.
     */
    public void setValidationPolicy(String policy)
        throws InstantiationException, IllegalAccessException;

    /**
     * Validation policies are responsible for computing whether or not a
     * cached data structure is valid for the current context.
     * <p/>
     * <code>getValidCachedData(getCacheableData(o), conf)</code> should
     * return an object equivalent to <code>o</code> in the expected case.
     * <p/>
     * Implementations of this class will often also implement
     * {@link Configurable} in order to receive the current
     * {@link Configuration}.
     */
    public interface ValidationPolicy {
        /**
         * Returns an object that this policy considers to be valid, based
         * on <code>o</code>. If <code>o</code> is not valid, this method
         * will return <code>null</code>.
         */
        public Object getValidData(Object o);

        /**
         * Return an object that the {@link CacheMarshaller} should store.
         */
        public Object getCacheableData(Object o);
    }
}
