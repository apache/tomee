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
package org.apache.openjpa.instrumentation.jmx;

import javax.management.ObjectName;

import org.apache.openjpa.lib.instrumentation.Instrument;

/**
 * Interface for JMX-specific instruments
 */
public interface JMXInstrument extends Instrument {
    
    /**
     * Returns the JMX object name for the instrument
     * @return
     */
    public ObjectName getObjectName();

    /**
     * Sets the context reference for the instrument.  Required to register
     * the instrument under a unique id.
     * @param cref the context reference for the instrument 
     */
    public void setContextRef(String cref);
    
    /**
     * Gets the context reference for the instrument.  Required to register
     * the instrument under a unique id.
     * @param cref the context reference for the instrument
     */
    public String getContextRef();
    
    /**
     * Sets the config id for the instrument.  Required to register
     * the instrument under a unique id.
     * @return the config id of the instrument
     */
    public String getConfigId();
    
    /**
     * Gets the config id for the instrument.  Required to register
     * the instrument under a unique id.
     * @param cid the config id of the instrument
     */
    public void setConfigId(String cid);
}
