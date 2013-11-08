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
package org.apache.openjpa.persistence;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.lib.conf.PluginValue;

/**
 * Plugin type used to represent the {@link EntityManagerFactory}. 
 *
 * @since 0.4.1
 * @nojavadoc
 */
public class EntityManagerFactoryValue
    extends PluginValue {

    /**
     * Configuration property key.
     */
    public static final String KEY = "EntityManagerFactory";

    public static final String[] ALIASES = {
        "default", EntityManagerFactoryImpl.class.getName(),
    };

    /**
     * Create a new factory of the configured type.
     */
    public static OpenJPAEntityManagerFactory newFactory(BrokerFactory bf) {
        OpenJPAConfiguration conf = bf.getConfiguration();
        PluginValue val = (PluginValue) conf.getValue(KEY); 
        if (val == null)
            return null;
        EntityManagerFactoryImpl emf = (EntityManagerFactoryImpl) val.
            instantiate(EntityManagerFactoryImpl.class, conf, true);
        emf.setBrokerFactory(bf);
        return emf;
    }

    public EntityManagerFactoryValue() {
        super(KEY, false);
        setAliases(ALIASES);
        setDefault(ALIASES[0]);
        setClassName(ALIASES[1]);
        setScope(getClass());
    }
}
