package org.apache.tomee.catalina.cluster;

import org.apache.catalina.ha.ClusterMessageBase;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.catalina.TomEERuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class DeployMessage extends ClusterMessageBase {
    public static final String TOMEE_CLUSTER_DEPLOY_SEND_ARCHIVE = "tomee.cluster.deploy.send-archive";

    private String file;
    private byte[] archive = null;

    public DeployMessage(final String path) {
        file = path;
        if (SystemInstance.get().getOptions().get(TOMEE_CLUSTER_DEPLOY_SEND_ARCHIVE, false)) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(path);
                IO.copy(fis, baos);
                archive = baos.toByteArray();
            } catch (Exception e) {
                throw new TomEERuntimeException(e);
            } finally {
                IO.close(fis);
                IO.close(baos);
            }
        }
    }

    public String getFile() {
        return file;
    }

    public byte[] getArchive() {
        return archive;
    }
}
