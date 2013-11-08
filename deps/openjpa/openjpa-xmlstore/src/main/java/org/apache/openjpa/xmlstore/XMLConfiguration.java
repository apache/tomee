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
package org.apache.openjpa.xmlstore;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.lib.conf.ProductDerivations;

/**
 * Configuration implementation for the XML file store. Each unique
 * {@link BrokerFactory} has a distinct configuration instance.
 * Thus this configuration is shared by all {@link Broker}s
 * associated with the owning factory, and is a good place to provide access
 * to shared resources. Note that each broker has its own
 * {@link org.apache.openjpa.abstractstore.AbstractStoreManager}.
 */
public class XMLConfiguration
    extends OpenJPAConfigurationImpl {

    // shared resources
    private XMLStore _store;
    private XMLFileHandler _handler;

    /**
     * Default constructor.
     */
    public XMLConfiguration() {
        super(false, false);

        // override the default and the current value of lock manager plugin
        // from our superclass to use the single-jvm lock manager
        lockManagerPlugin.setDefault("version");
        lockManagerPlugin.setString("version");

        ProductDerivations.beforeConfigurationLoad(this);
        loadGlobals();
    }

    /**
     * Return the {@link XMLFileHandler} associated with this configuration.
     */
    public synchronized XMLFileHandler getFileHandler() {
        if (_handler == null)
            _handler = new XMLFileHandler(this);
        return _handler;
    }

    /**
     * Return the {@link XMLStore} associated with this configuration.
     */
    public synchronized XMLStore getStore() {
        if (_store == null)
            _store = new XMLStore(this);
        return _store;
	}
}
