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

import java.util.Arrays;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Abstract implementation of {@link RemoteCommitProvider}. Obtains handles
 * to the event manager and log.
 *
 * @author Patrick Linskey
 * @since 0.2.5.0
 */
public abstract class AbstractRemoteCommitProvider
    implements RemoteCommitProvider, Configurable {

    private static final Localizer _loc = Localizer.forPackage
        (AbstractRemoteCommitProvider.class);

    protected RemoteCommitEventManager eventManager;
    protected Log log;

    public void setConfiguration(Configuration config) {
        this.log = config.getLog(OpenJPAConfiguration.LOG_RUNTIME);
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
    }

    public void setRemoteCommitEventManager(RemoteCommitEventManager mgr) {
        eventManager = mgr;
    }

    /**
     * Fire a remote commit event via the cached event manager.
     */
    protected void fireEvent(RemoteCommitEvent event) {
        Exception[] es = eventManager.fireEvent(event);
        if (es.length > 0 && log.isWarnEnabled())
            log.warn(_loc.get("remote-listener-ex", Arrays.asList(es)));
        if (log.isTraceEnabled())
            for (int i = 0; i < es.length; i++)
                log.trace(es[i]);
    }
}
