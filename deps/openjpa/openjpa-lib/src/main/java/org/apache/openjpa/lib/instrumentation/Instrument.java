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

/**
 * Interface that must be implemented by instruments.
 */
public interface Instrument {

    /**
     * Returns the name of the instrument.  Must be unique per-provider.
     */
    public String getName();

    /**
     * Returns the options specified for the instrument in string form.
     * @return options configuration options for the instrument
     */
    public String getOptions();

    /**
     * Sets options to specify for the instrument in standard string form.
     * ex.  DataCache(Options='Start=true')
     * @param options options 
     */
    public void setOptions(String options);

    /**
     * Gets the context of the instrument.  Typically, a reference to a broker 
     * or broker factory.
     * @return the context associated with the instrument.
     */
    public Object getContext();
    
    /**
     * Sets the context of the instrument.  Typically, a reference to a broker 
     * or broker factory.
     * @return the context associated with the instrument.
     */
    public void setContext(Object context);
    
    /**
     * Sets the instrumentation provider for the instrument.
     * @param provider instrumentation provider of the instrument
     */
    public void setProvider(InstrumentationProvider provider);
    
    /**
     * Gets the instrumentation provider for the instrument.
     * @return instrumentation provider of the instrument
     */
    public InstrumentationProvider getProvider();

    /**
     * Initializes the instrument.  Depending on the instrument, the provider,
     * options, and various options may need to be set before calling this method.
     */
    public void initialize();

    /**
     * Gets the instrumentation level of this instrument.  The instrumentation level
     * determines if and when the instrument will automatically start and stop.
     * @return  the instrumentation level of the instrument
     */
    public InstrumentationLevel getLevel();

    /**
     * Returns true if the instrument is started.
     * @return
     */
    public boolean isStarted();

    /**
     * Sets whether the instrument is an available state.
     * @param started
     */
    public void setStarted(boolean started);
    
    /**
     * Starts the instrument.  Typically this will be performed through the provider,
     * but in some cases an instrument will have its own specialized startup.
     */
    public void start();
    
    /**
     * Starts the instrument.  Typically this will be performed through the provider,
     * but in some cases an instrument will have its own specialized shutdown.
     */
    public void stop();

    /**
     * Restarts the instrument.  Typically this will be performed through the provider,
     * but in some cases an instrument will have its own specialized restart.
     */
    public void restart();
}
