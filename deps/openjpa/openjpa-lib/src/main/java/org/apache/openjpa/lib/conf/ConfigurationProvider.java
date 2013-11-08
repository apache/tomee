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
package org.apache.openjpa.lib.conf;

import java.util.Map;

/**
 * Implementations of this interface can populate {@link Configuration}s in
 * some environment-specific way. Implementations must implement the
 * <code>equals</code> and <code>hashCode</code> methods so that equivalent
 * configurations compare equal.
 *
 * @nojavadoc
 * @since 0.4.0.0
 */
public interface ConfigurationProvider {

    /**
     * Return properties loaded thus far, or empty map if none.
     */
    public Map<String,Object> getProperties();

    /**
     * Add the given properties to those in this provider, overwriting
     * any existing properties under the same keys.
     */
    public void addProperties(Map<?,?> props);

    /**
     * Add a single property, overwriting any existing property under the
     * same key.
     */
    public Object addProperty(String key, Object value);

    /**
     * Set loaded information into the given configuration.
     */
    public void setInto(Configuration conf);
}
