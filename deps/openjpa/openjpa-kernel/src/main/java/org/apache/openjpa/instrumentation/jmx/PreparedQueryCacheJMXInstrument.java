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

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.instrumentation.AbstractPreparedQueryCacheInstrument;
import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.lib.instrumentation.InstrumentationLevel;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * A JMX-specific instrument for the query cache
 */
public class PreparedQueryCacheJMXInstrument extends AbstractPreparedQueryCacheInstrument 
    implements JMXInstrument, PreparedQueryCacheJMXInstrumentMBean {
    
    private static Localizer _loc = Localizer.forPackage(PreparedQueryCacheJMXInstrument.class);

    private static final String MBEAN_TYPE = "QuerySQLCache";
    
    private ObjectName _objName = null;
    
    @Override
    public String getName() {
        return MBEAN_TYPE;
    }

    @Override
    public InstrumentationLevel getLevel() {
        return InstrumentationLevel.FACTORY;
    }

    
    @Override
    public void initialize() {
        
        OpenJPAConfiguration conf = (OpenJPAConfiguration)getProvider().getConfiguration();
        PreparedQueryCache pqc = conf.getQuerySQLCacheInstance();
        
        if (pqc == null) {
            throw new UserException(_loc.get("prep-query-cache-not-found"));
        }
        
        setPreparedQueryCache(pqc);
        setConfigId(conf.getId());
        setContextRef(Integer.toString(System.identityHashCode(getContext())));
    }

    public ObjectName getObjectName() {
        if (_objName != null) {
            return _objName;
        }
        
        try {
            _objName = JMXProvider.createObjectName(this, null);
            return _objName;
        } catch (Throwable t) {
            throw new UserException(_loc.get("unable-to-create-object-name", getName()), t);
        }
    }
    
    public void start() {
        getProvider().startInstrument(this);
    }

    public void stop() {
        getProvider().stopInstrument(this);
    }
}
