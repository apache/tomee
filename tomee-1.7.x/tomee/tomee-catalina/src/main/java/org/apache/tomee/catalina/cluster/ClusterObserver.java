/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina.cluster;

import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.AfterEvent;

import java.io.File;
import java.util.Set;

public class ClusterObserver {
    private static final boolean ClUSTER_DEPLOYMENT = "true".equals(SystemInstance.get().getProperty("tomee.cluster.deployment", "false"));

    private final Set<CatalinaCluster> clusters;

    public ClusterObserver(final Set<CatalinaCluster> clusters) {
        this.clusters = clusters;
    }

    public void deploy(@Observes final AfterEvent<AssemblerAfterApplicationCreated> app) {
        if (!ClUSTER_DEPLOYMENT) {
            return;
        }

        final AppInfo appInfo = app.getEvent().getApp();
        send(new DeployMessage(appInfo.path), appInfo);
    }

    public void undeploy(@Observes final AssemblerBeforeApplicationDestroyed app) {
        if (!ClUSTER_DEPLOYMENT) {
            return;
        }

        final AppInfo appInfo = app.getApp();
        send(new UndeployMessage(appInfo.path), appInfo);
    }

    private void send(final ClusterMessage message, final AppInfo app) {
        for (final CatalinaCluster cluster : clusters) {
            final String path = app.path;
            if (new File(path).exists() && !app.autoDeploy) {
                cluster.send(message);
            }
        }
    }
}
