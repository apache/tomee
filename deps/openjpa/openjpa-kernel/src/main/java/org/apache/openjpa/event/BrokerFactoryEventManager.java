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
package org.apache.openjpa.event;

import org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.conf.OpenJPAConfiguration;

/**
 * {@link EventManager} responsible for notifying listeners of
 * {@link BrokerFactoryEvent}s.
 *
 * @since 1.0.0
 */
public class BrokerFactoryEventManager
    extends AbstractConcurrentEventManager {

    private static final Localizer _loc = Localizer.forPackage(
        BrokerFactoryEventManager.class);

    private final Configuration _conf;

    public BrokerFactoryEventManager(Configuration conf) {
        _conf = conf;
    }

    protected void fireEvent(Object event, Object listener) {
        try {
            BrokerFactoryEvent e = (BrokerFactoryEvent) event;
            ((BrokerFactoryListener) listener).eventFired(e);
        } catch (Exception e) {
            _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME).warn(
                _loc.get("broker-factory-listener-exception"), e);
        }
    }
}
