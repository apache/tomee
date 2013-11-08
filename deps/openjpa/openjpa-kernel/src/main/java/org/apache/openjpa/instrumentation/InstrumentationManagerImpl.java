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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.conf.PluginListValue;
import org.apache.openjpa.lib.instrumentation.InstrumentationLevel;
import org.apache.openjpa.lib.instrumentation.InstrumentationProvider;

/**
 * An implementation of an instrumentation manager.
 */
public class InstrumentationManagerImpl implements InstrumentationManager {

    public Set<InstrumentationProvider> _providers = 
        Collections.synchronizedSet(new HashSet<InstrumentationProvider>());
    
    private boolean _closed = false;
    
    /**
     * Initializes all providers defined for the specified configuration.
     * @param conf
     * @param providers
     */
    public void initialize(OpenJPAConfiguration conf, PluginListValue pluginVal) {
        InstrumentationProvider[] providers = 
            (InstrumentationProvider[])pluginVal.instantiate(InstrumentationProvider.class, conf);
        _providers.addAll(Arrays.asList(providers));
    }
    
    /**
     * Make a provider managed.  This will bind its instrumentation to 
     * InstrumentationLevel type events (factory create/close, broker create/close).
     * @param provider
     * @param config
     */
    public void manageProvider(InstrumentationProvider provider) {
        _providers.add(provider);
    }

    /**
     * Returns all providers as an unmodifiable set
     */    
    public Set<InstrumentationProvider> getProviders() {
        return Collections.unmodifiableSet(_providers);
    }

    /**
     *  Starts all providers at a specific level and context
     */
    public void start(InstrumentationLevel level, Object context) {
        if (_providers == null || _providers.size() == 0) {
            return;
        }
        for (InstrumentationProvider provider : _providers) {
            if (!provider.isStarted()) {
                provider.start();
            }
            provider.startInstruments(level, context);
        }
    }

    /**
     *  Stops all providers at a specific level and context
     */
    public void stop(InstrumentationLevel level, Object context) {
        if (_providers == null || _providers.size() == 0) {
            return;
        }
        for (InstrumentationProvider provider : _providers) {
            provider.stopInstruments(level, context);
        }
    }

    /**
     *  Stops all providers
     */
    public void close() throws Exception {
        if (_closed) {
            return;
        }
        for (InstrumentationProvider provider : _providers) {
            provider.stop();
        }
        _closed = true;
    }
}
