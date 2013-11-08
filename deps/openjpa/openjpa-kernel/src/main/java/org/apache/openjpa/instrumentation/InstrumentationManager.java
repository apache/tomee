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
package org.apache.openjpa.instrumentation;

import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.conf.PluginListValue;
import org.apache.openjpa.lib.instrumentation.InstrumentationLevel;
import org.apache.openjpa.lib.instrumentation.InstrumentationProvider;
import org.apache.openjpa.lib.util.Closeable;

/**
 * Managers of instrumentation providers must implement this interface.  It
 * provides methods for initializing multiple providers via configuration in
 * addition to managing instrumentation providers and the state of the providers.
 */
public interface InstrumentationManager extends Closeable {

    /**
     * Used to initialize one or more providers using the supplied configuration.
     * @param conf the configuration to use for initialization
     * @param providers one or more providers as supplied via plugin list value
     */
    public void initialize(OpenJPAConfiguration conf, PluginListValue providers);

    /**
     * Manage a given provider.  This will plug the instruments managed by the 
     * the provider into the life cycle of the manager
     * @param provider the instrumentation provider
     */
    public void manageProvider(InstrumentationProvider provider);

    /**
     * Starts all instruments for all managed providers for a given level
     * and context.
     * @param level  instrumentation level
     * @param context  instrumentation context (broker, factory, config,...)
     */
    public void start(InstrumentationLevel level, Object context);

    /**
     * Stops all instruments for all managed providers for a given level
     * and context.
     * @param level  instrumentation level
     * @param context  instrumentation context (broker, factory, config,...)
     */
    public void stop(InstrumentationLevel broker, Object context);

    /**
     * Returns all providers managed by this manager.
     * @return  all providers managed by this manager
     */
    public Set<InstrumentationProvider> getProviders();
}
