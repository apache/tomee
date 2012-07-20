/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
