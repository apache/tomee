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

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.abstractstore.AbstractStoreBrokerFactory;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.conf.ProductDerivations;
import org.apache.openjpa.lib.conf.ProductDerivation;

/**
 * Value type used to represent the {@link BrokerFactory}. This type is
 * defined separately so that it can be used both in the global configuration
 * and in {@link org.apache.openjpa.kernel.Bootstrap} with the same
 * encapsulated configuration.
 *
 * @nojavadoc
 */
public class BrokerFactoryValue
    extends PluginValue {

    public static final String KEY = "BrokerFactory";

    private static final String[] _aliases;
    static {
        Map<String, String> aliases = new HashMap<String, String>();
        aliases.put("abstractstore", AbstractStoreBrokerFactory.class.getName());
        ProductDerivation[] ds = ProductDerivations.getProductDerivations();
        for (int i = 0; i < ds.length; i++) {
            if (ds[i] instanceof OpenJPAProductDerivation)
                ((OpenJPAProductDerivation) ds[i]).putBrokerFactoryAliases(aliases);
        }

        _aliases = new String[aliases.size() * 2];
        int i = 0;
        for(Map.Entry<String, String>e : aliases.entrySet()) {
            _aliases[i++] = e.getKey();
            _aliases[i++] = e.getValue();
        }
    }

    /**
     * Extract the value of this property if set in the given provider.
     */
    public static Object get(ConfigurationProvider cp) {
        Map props = cp.getProperties();
        return props.get(ProductDerivations.getConfigurationKey(KEY, props));
    }

    /**
     * Set the value of this property in the given provider.
     */
    public static void set(ConfigurationProvider cp, String value) {
        String key = ProductDerivations.getConfigurationKey(KEY, 
            cp.getProperties());
        cp.addProperty(key, value);
    }

    public BrokerFactoryValue() {
        super(KEY, false);
        setAliases(_aliases);
    }
}
