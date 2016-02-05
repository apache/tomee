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
package org.apache.tomee.catalina;

import org.apache.catalina.Valve;
import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.catalina.cluster.TomEEClusterListener;

import java.util.Arrays;
import java.util.List;

public class SimpleTomEETcpCluster extends SimpleTcpCluster {
    public SimpleTomEETcpCluster(final SimpleTcpCluster from) {
        clusterListeners.addAll(Arrays.asList(from.findClusterListeners()));

        setClusterName(from.getClusterName());
        setContainer(from.getContainer());
        setNotifyLifecycleListenerOnFailure(from.isNotifyLifecycleListenerOnFailure());

        setChannelSendOptions(from.getChannelSendOptions());
        setChannelStartOptions(from.getChannelStartOptions());
        setHeartbeatBackgroundEnabled(from.isHeartbeatBackgroundEnabled());
        setChannel(from.getChannel());
        getManagers().putAll(from.getManagers());
        setManagerTemplate(from.getManagerTemplate());
        setClusterDeployer(from.getClusterDeployer());

        for (final Valve valve : from.getValves()) {
            addValve(valve);
        }
    }

    @Override
    protected void checkDefaults() {
        final List<ClusterListener> currentListeners = clusterListeners;
        final TomEEClusterListener tomEEClusterListener = SystemInstance.get().getComponent(TomEEClusterListener.class);
        if (currentListeners.size() == 1 && currentListeners.iterator().next() == tomEEClusterListener) {
            currentListeners.clear();
        }

        // else force the new cluster listener
        for (final ClusterListener clusterListener : currentListeners) {
            clusterListener.setCluster(this); // we don't care about TomEEClusterListener since it is stateless
        }
        if (getClusterDeployer() != null) {
            getClusterDeployer().setCluster(this);
        }

        super.checkDefaults();
        addClusterListener(tomEEClusterListener); // since that's a singleton and all listeners have to be unique (contains()) we can always add it
    }
}
