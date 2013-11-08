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

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.event.RemoteCommitEventManager;
import org.apache.openjpa.event.RemoteCommitProvider;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.util.Options;

/**
 * Value type used to represent a {@link RemoteCommitProvider}. This
 * plugin allows users to specify whether to transmit the ids of added objects
 * in the remote commit events distributed.
 *
 * @author Abe White
 * @nojavadoc
 */
public class RemoteCommitProviderValue
    extends PluginValue {

    private static final String[] ALIASES = new String[]{
        "sjvm", "org.apache.openjpa.event.SingleJVMRemoteCommitProvider",
        "jms", "org.apache.openjpa.event.JMSRemoteCommitProvider",
        "tcp", "org.apache.openjpa.event.TCPRemoteCommitProvider",
    };

    private Options _opts = null;
    private Boolean _transmitPersIds = null;

    public RemoteCommitProviderValue() {
        super("RemoteCommitProvider", true);
        setAliases(ALIASES);
    }

    public void setProperties(String props) {
        super.setProperties(props);
        _opts = null;
        _transmitPersIds = null;
    }

    public void setString(String str) {
        super.setString(str);
        _opts = null;
        _transmitPersIds = null;
    }

    /**
     * The cached provider.
     */
    public RemoteCommitProvider getProvider() {
        return (RemoteCommitProvider) get();
    }

    /**
     * The cached provider.
     */
    public void setProvider(RemoteCommitProvider provider) {
        set(provider);
    }

    /**
     * Whether to transmit persisted object ids in remote commit events.
     */
    public boolean getTransmitPersistedObjectIds() {
        return Boolean.TRUE.equals(_transmitPersIds);
    }

    /**
     * The cached decorators.
     */
    public void setTransmitPersistedObjectIds(boolean transmit) {
        _transmitPersIds = (transmit) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Instantiate the provider.
     */
    public RemoteCommitProvider instantiateProvider(Configuration conf) {
        return instantiateProvider(conf, true);
    }

    /**
     * Instantiate the provider.
     */
    public RemoteCommitProvider instantiateProvider(Configuration conf,
        boolean fatal) {
        return (RemoteCommitProvider) instantiate(RemoteCommitProvider.class,
            conf, fatal);
    }

    /**
     * Configure the remote event manager.
     */
    public void configureEventManager(RemoteCommitEventManager mgr) {
        parseOptions();
        if (_transmitPersIds != null)
            mgr.setTransmitPersistedObjectIds(_transmitPersIds.booleanValue());
    }

    /**
     * Override to keep decorators out of transport configuration.
     */
    public Object instantiate(Class type, Configuration conf, boolean fatal) {
        Object obj = newInstance(getClassName(), type, conf, fatal);
        parseOptions();
        Configurations.configureInstance(obj, conf, _opts, getProperty());
        set(obj, true);
        return obj;
    }

    private void parseOptions() {
        if (_opts != null)
            return;

        _opts = Configurations.parseProperties(getProperties());
        String transmit = StringUtils.trimToNull(_opts.removeProperty
            ("transmitPersistedObjectIds", "TransmitPersistedObjectIds", null));
        if (transmit != null)
            _transmitPersIds = Boolean.valueOf (transmit);
	}
}
