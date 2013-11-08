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
package org.apache.openjpa.lib.instrumentation;

import java.util.Set;

import org.apache.openjpa.lib.conf.Configuration;

/**
 * Pluggable instrumentation providers (ex. JMX) must implement this interface. It
 * provides methods for controlling the provider and the instruments instrumented
 * by the provider.
 */
public interface InstrumentationProvider {

    /**
     * Whether the instrumentation provider started
     * @return true if the provider is started
     */
    public boolean isStarted();    
    /**
     * Starts the instrumentation provider
     */
    public void stop();

    /**
     * Stops the instrumentation provider
     */
    public void start();

    /**
     * Gets the configuration associated with the instrumentation provider
     * @return the configuration associated with the provider
     */
    public Configuration getConfiguration();
    
    /**
     * Used to associate one or more instruments to a provider.  Instruments 
     * are specified by class name or alias.  Multiple instruments must be
     * specified as a comma separated list.
     * 
     * example:  DataCache,QueryCache,com.my.MyInstrument
     *   where DataCache and QueryCache have aliases and com.my.MyInstrument is 
     *   a class implementing an Instrument.
     * 
     * @param instruments  one or more instrument class names or aliases
     */
    public void setInstrument(String instruments);

    /**
     * Returns the string-based list of instruments directly configured by
     * this provider via setInstrument.
     * @return
     */
    public String getInstrument();
 
    /**
     * Sets configuration options for this provider
     * @param options
     */
    public void setOptions(String options);
    
    /**
     * Gets configuration options for this provider
     * @param options
     */
    public String getOptions();
    
    /**
     * Returns an string array of identifier to class name aliases for
     * instruments known to the instrumentation provider.  Example:
     * 
     *  {"DataCache", "org.apache.openjpa.instrumentation.DataCacheInstrument",
     *   "QueryCache", "org.apache.openjpa.instrumentation.QueryCacheInstrument"}
     * @return a string array of identifier, class name pairs.
     */
    public String[] getInstrumentAliases();
    
    /**
     * Adds an instrument to this providers list of managed instruments.  The
     * instrument will participate in context-based lifecycle routines, 
     * depending on the instrumentation level.
     * @param instrument
     */
    public void addInstrument(Instrument instrument);
    
    /**
     * Stops all instruments of the specified instrumentation level and context.
     * @param level instrumentation level
     * @param context  instrumentation context (factory, broker, config)
     */
    public void stopInstruments(InstrumentationLevel level, Object context);
    
    /**
     * Starts all instruments of the specified instrumentation level and context.
     * @param level instrumentation level
     * @param context  instrumentation context (factory, broker, config)
     */
    public void startInstruments(InstrumentationLevel level, Object context);

    /**
     * Initializes an instrument within the provided context.
     * @param instrument an instrument 
     * @param context  instrumentation context (factory, broker, config)
     */
    public void initializeInstrument(Instrument instrument, Object context);

    /**
     * Initializes an instrument within the provided options and context.
     * @param instrument an instrument 
     * @param options configuration options to provide the instrument during initialization
     * @param context  instrumentation context (factory, broker, config)
     */
    public void initializeInstrument(Instrument instrument, String options, Object context);

    /**
     * Returns an instrument instrumented by this provider by name
     * @param name the name of the instrument to return
     * @return the instrument or null if not instrumented by this provider
     */
    public Instrument getInstrumentByName(String name);

    /**
     * Removes an instrument instrumented by this provider by name
     * @param name the name of the instrument to remove
     */
    public void removeInstrumentByName(String name);

    /**
     * Gets all instruments instrumented by this provider
     * @return instruments instrumented by this provider
     */
    public Set<Instrument> getInstruments();

    /**
     * Starts an instrument
     * @param instrument this instrument to start
     */
    public void startInstrument(Instrument instrument);
    
    /**
     * Stops an instrument
     * @param instrument the instrument to stop
     */
    public void stopInstrument(Instrument instrument);

    /**
     * Stops an instrument, forcing the stop, if necessary.
     * @param instrument the instrument to stop
     * @param force forces the stop if the instrument does not stop gracefully.
     */
    public void stopInstrument(Instrument instrument, boolean force);
}
