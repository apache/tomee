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

import org.apache.openjpa.lib.instrumentation.AbstractInstrument;
import org.apache.openjpa.lib.instrumentation.InstrumentationLevel;

public class BrokerLevelInstrument extends AbstractInstrument {

    public static String NAME = "NoneInstrument";
    
    private boolean _initialized; 
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize() {
        setInitialized(true);
    }

    public void start() {
        setStarted(true);
    }

    public void stop() {
        setStarted(false);
    }

    public InstrumentationLevel getLevel() {
        return InstrumentationLevel.BROKER;
    }

    public void setInitialized(boolean _initialized) {
        this._initialized = _initialized;
    }

    public boolean isInitialized() {
        return _initialized;
    }
    
}
