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
package org.apache.openjpa.conf;

import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.kernel.BrokerImpl;
import org.apache.openjpa.kernel.FinalizingBrokerImpl;
import org.apache.openjpa.util.InternalException;

/**
 * Custom {@link PluginValue} that can efficiently create {@link BrokerImpl}
 * instances.
 *
 * @since 0.9.7
 */
public class BrokerValue 
    extends PluginValue {

    public static final String KEY = "BrokerImpl";
    public static final String NON_FINALIZING_ALIAS = "non-finalizing";
    public static final String DEFAULT_ALIAS = "default";

    private BrokerImpl _templateBroker;

    public BrokerValue() {
        super(KEY, false);
        String[] aliases = new String[] {
            DEFAULT_ALIAS, FinalizingBrokerImpl.class.getName(),
            NON_FINALIZING_ALIAS, BrokerImpl.class.getName(), 
        };
        setAliases(aliases);
        setDefault(aliases[0]);
        setString(aliases[0]);        
    }

    public Object newInstance(String clsName, Class type, Configuration conf,
        boolean fatal) {
        getTemplateBroker(clsName, type, conf, fatal);

        try {
            return _templateBroker.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalException(e);
        }
    }

    public Class<? extends BrokerImpl> getTemplateBrokerType(Configuration c) {
        return getTemplateBroker(getClassName(), BrokerImpl.class, c, true)
            .getClass();
    }

    private BrokerImpl getTemplateBroker(String clsName, Class type,
        Configuration conf, boolean fatal) {
        if (clsName == null || !clsName.equals(getClassName()))
            throw new IllegalArgumentException("clsName != configured value '"
                + getClassName() + "'");

        // This is not synchronized. If there are concurrent invocations
        // while _templateBroker is null, we'll just end up with extra
        // template brokers, which will get safely garbage collected.
        if (_templateBroker == null)
            _templateBroker = (BrokerImpl) super.newInstance(clsName, type,
                conf, fatal);
        return _templateBroker;
    }
}
