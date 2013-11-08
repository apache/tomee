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
 * Provides a base for creating instruments.  Specialized instruments can
 * extend this class to get base instrument capabilities and then add their
 * own specialized functionality.
 */
public abstract class AbstractInstrument implements Instrument {
    
    private boolean _started = false;
    private InstrumentationProvider _provider;
    private Object _context;
    private String _options;

    public Object getContext() {
        return _context;
    }
    
    public void setContext(Object context) {
        _context = context;
    }
    
    public String getOptions() {
        return _options;
    }
    
    public void setOptions(String options) {
        _options = options;
    }
    
    public boolean isStarted() {
        return _started;
    }
    
    public void setStarted(boolean started) {
        _started = started;
    }

    public void restart() {
        stop();
        start();
    }
        
    public void setProvider(InstrumentationProvider provider) {
        _provider = provider;
    }
    
    public InstrumentationProvider getProvider() {
        return _provider;
    }
    
    public InstrumentationLevel getLevel() {
        return InstrumentationLevel.MANUAL;
    }

    public abstract String getName();

    public abstract void initialize();
}
