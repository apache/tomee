package org.apache.tomee.catalina.cluster;

import org.apache.catalina.ha.ClusterMessageBase;

public class UndeployMessage extends ClusterMessageBase {
    private String file;

    public UndeployMessage(final String path) {
        file = path;
    }

    public String getFile() {
        return file;
    }
}
