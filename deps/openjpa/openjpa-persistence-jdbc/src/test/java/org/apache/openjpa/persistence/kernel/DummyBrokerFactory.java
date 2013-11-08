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
package org.apache.openjpa.persistence.kernel;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.kernel.JDBCBrokerFactory;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.lib.conf.ConfigurationProvider;

@SuppressWarnings("serial")
public class DummyBrokerFactory extends JDBCBrokerFactory {
    JDBCConfiguration _conf;
    public DummyBrokerFactory(JDBCConfiguration conf) {
        super(conf);
        _conf = conf;
    }
    
    public static DummyBrokerFactory newInstance(ConfigurationProvider cp) {
        JDBCConfiguration conf = new JDBCConfigurationImpl();
        cp.setInto(conf);
        return new DummyBrokerFactory(conf);
    }   
    
    @Override
    protected StoreManager newStoreManager() {
        // Do something with the config.
        _conf.getLog();
        
        return super.newStoreManager();
    }
}
