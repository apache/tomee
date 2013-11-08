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

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.openjpa.lib.instrumentation.AbstractInstrumentationProvider;
import org.apache.openjpa.lib.instrumentation.Instrument;
import org.apache.openjpa.util.UserException;

/**
 * A simple MBean JMX instrumentation provider
 */
public class JMXProvider
    extends AbstractInstrumentationProvider {
    
    // Aliases for built-in JMX Instrumentation
    public static final String[] JMX_INSTRUMENT_ALIASES = {
        "DataCache", "org.apache.openjpa.instrumentation.jmx.DataCacheJMXInstrument",
        "QueryCache", "org.apache.openjpa.instrumentation.jmx.QueryCacheJMXInstrument",
        "QuerySQLCache", "org.apache.openjpa.instrumentation.jmx.PreparedQueryCacheJMXInstrument"
    };
    
    /**
     * The MBean domain for OpenJPA
     */
    public static final String MBEAN_DOMAIN = "org.apache.openjpa";
    
    private Set<MBeanServer> _mbs = null;

    /**
     * Register an MBean with the mbean server.
     * @param mBean
     */
    protected void registerMBean(JMXInstrument mBean) {
        Set<MBeanServer> mbs = getMBeanServer(); 
        try {
            for (MBeanServer s : mbs) {
                s.registerMBean(mBean, mBean.getObjectName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the mbean server
     * @return
     */
    public Set<MBeanServer> getMBeanServer() {
        if (_mbs == null) {
            _mbs =  new HashSet<MBeanServer>();
            // Look in both of these static methods to find all MBServers. In some environments the server returned by
            // the getPlatformMBeanServer() call isn't the one used by the runtime. Might be over kill by calling both,
            // but it shouldn't hurt anything.
            _mbs.addAll(MBeanServerFactory.findMBeanServer(null));
            _mbs.add(ManagementFactory.getPlatformMBeanServer());
        }
        return _mbs;
    }

    @Override
    public void start() {
        Set<MBeanServer> mbs = getMBeanServer();
        try {
            for (MBeanServer s : mbs) {
                if (mbs == null || mbs.size() == 0) {
                    throw new UserException("jmx-server-failed-creation");
                }
            }
            setStarted(true);
        } catch (Throwable t) {
            throw new UserException("jmx-server-unavailable", t);
        }
    }

    /**
     * Stops all instruments registered with this provider and releases the 
     * reference to the Platform MBean server instance. 
     */
    @Override
    public void stop() {
        if (isStarted()) {
            Set<Instrument> instruments = getInstruments();
            if (instruments != null && instruments.size() > 0) {
                for (Instrument inst : instruments) {
                    stopInstrument(inst);
                }
            }
            // The MBean server factory does appear to ref count properly so the 
            // platform server cannot released from the factory once it is acquired.  
            // Multiple attempts to capture and release the server will result in a 
            // runtime exception.
            // MBeanServerFactory.releaseMBeanServer(getMBeanServer());
            // _mbs = null;
            setStarted(false);
        }
    }

    /**
     * Creates an object name for the supplied instrument and key properties
     * @param instrument the instrument
     * @param props additional key properties
     * @return the JMX object name
     * @throws Exception a generic JMX-type exception
     */
    public static ObjectName createObjectName(JMXInstrument instrument, Map<String,String> props) 
        throws Exception {
        // Construct the base name
        StringBuilder sbName = new StringBuilder(MBEAN_DOMAIN);
        sbName.append(":type=");
        sbName.append(instrument.getName());
        sbName.append(",cfgid=");
        sbName.append(instrument.getConfigId());
        sbName.append(",cfgref=");
        sbName.append(instrument.getContextRef());
        // Add any additional key properties that were provided
        if (props != null && !props.isEmpty()) {
            for (Entry<String,String> prop : props.entrySet()) {
               sbName.append(",");
               sbName.append(prop.getKey());
               sbName.append("=");
               sbName.append(prop.getValue());
            }
        }
        return new ObjectName(sbName.toString());
    }

    /**
     * Start an instrument.  Registers an mbean with the server.
     */
    public void startInstrument(Instrument instrument) {
        if (!instrument.isStarted()) {
            registerMBean((JMXInstrument)instrument);
            instrument.setStarted(true);
        }
    }

    /**
     * Stop an instrument.  Unregisters an mbean with the server.
     */
    public void stopInstrument(Instrument instrument, boolean force) {
        if (instrument.isStarted() || force) {
            Set<MBeanServer> mbs = getMBeanServer();
            try {
                for (MBeanServer s : mbs) {
                    s.unregisterMBean(((JMXInstrument) instrument).getObjectName());
                }
                instrument.setStarted(false);
            } catch (Exception e) {
                // If force, swallow the exception since the bean may not even
                // be registered.
                if (!force) {
                    throw new UserException("cannot-stop-instrument",e);
                }
            }
        }
    }
    
    /**
     * Returns aliases for built-in instruments.
     */
    @Override
    public String[] getInstrumentAliases() {
        return JMX_INSTRUMENT_ALIASES;
    }
}
