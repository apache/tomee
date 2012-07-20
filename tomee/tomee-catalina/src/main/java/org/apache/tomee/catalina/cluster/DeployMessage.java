package org.apache.tomee.catalina.cluster;

import org.apache.catalina.ha.ClusterMessageBase;

// TODO: serialize file in byte[] to be able to send it over the network?
public class DeployMessage extends ClusterMessageBase {
    private String file;

    public DeployMessage(final String path) {
        file = path;
    }

    public String getFile() {
        return file;
    }
}
