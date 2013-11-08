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
package org.apache.openjpa.datacache;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.conf.Configurable;

/**
 * A policy determines whether a given entity should be cached and if true, in which named partition of the
 * cache.
 * <br>
 * This policy is activated for each instance if and only if the cache settings specified in metadata 
 * such as JPA specification defined {@link Cacheable @Cacheable} annotation or OpenJPA specific
 * {@link org.apache.openjpa.persistence.DataCache @DataCache} annotation or configuration property 
 * such as <code>javax.persistence.sharedCache.mode</code> determined the type of the instance being cachable.
 * <br> 
 * For example, a specific policy will never be active for when <code>javax.persistence.sharedCache.mode</code> 
 * is set to <code>NONE</code>. 
 * <br>
 * Distribution Policies are configurable. So a specific policy can be configured as
 * <pre>
 *  &lt;property name="openjpa.CacheDistributionPolicy" value="com.acme.FooPolicy(param1='xyz',param2=true)"/&gt;
 * </pre>
 * where <code>com.acme.FooPolicy</code> is an implementation of this interface and defines bean style setter and
 * getter methods for String property <code>param1</code> and boolean property <code>param2</code>. 
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 *
 */
public interface CacheDistributionPolicy extends Configurable {
    /**
     * Selects the name of the cache where the given managed proxy object state be cached.
     * 
     * @param sm the managed proxy object to be cached. The actual managed instance can be accessed from the proxy
     * instance simply as <code>sm.getManagedInstance()</code>.
     * 
     * @param context the context of invocation. No specific semantics is attributed currently. Can be null.
     *  
     * @return name of the cache or null, implying that that the instance should not be cached.
     */
    String selectCache(OpenJPAStateManager sm, Object context);
}
