package org.apache.tomee.catalina.cluster;

import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.observer.Observes;

import java.io.File;
import java.util.Set;

public class ClusterObserver {
    private final Set<CatalinaCluster> clusters;

    public ClusterObserver(final Set<CatalinaCluster> clusters) {
        this.clusters = clusters;
    }

    public void deploy(@Observes final AssemblerAfterApplicationCreated app) {
        final AppInfo appInfo = app.getApp();
        send(new UndeployMessage(appInfo.path), appInfo);
    }

    public void undeploy(@Observes final AssemblerBeforeApplicationDestroyed app) {
        final AppInfo appInfo = app.getApp();
        send(new DeployMessage(appInfo.path), appInfo);
    }

    private void send(final ClusterMessage message, final AppInfo app) {
        for (CatalinaCluster cluster : clusters) {
            final String path = app.path;
            if (new File(path).exists() && !app.autoDeploy) {
                cluster.send(message);
            }
        }
    }
}
