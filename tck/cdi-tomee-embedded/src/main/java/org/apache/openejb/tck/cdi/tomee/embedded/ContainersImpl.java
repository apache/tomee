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
package org.apache.openejb.tck.cdi.tomee.embedded;

import org.apache.commons.io.FileUtils;
import org.apache.openejb.config.ValidationException;
import org.apache.tomee.embedded.Container;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.Containers;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Rev$ $Date$
 */
public class ContainersImpl implements Containers {

    private static int count = 0;
    private static final String tmpDir = System.getProperty("java.io.tmpdir");
    private Exception exception;
    private final Container container;

    private static final Map<String, File> FILES = new ConcurrentHashMap<String, File>();

    public ContainersImpl() {
        System.out.println("Initialized ContainersImpl " + (++count));
        container = new Container();
        System.setProperty("tomee.valves", ResetStaticValve.class.getName());
    }

    @Override
    public boolean deploy(InputStream archive, String name) throws IOException {
        exception = null;

        System.out.println("Deploying " + archive + " with name " + name);

        File application = getFile(name);
        System.out.println(application);
        FILES.put(name, application.getParentFile());
        writeToFile(application, archive);

        try {
            container.deploy(name, application);
        } catch (Exception ex) {
            Exception e = ex;
            if (e.getCause() instanceof ValidationException) {
                e = (Exception) e.getCause();
            }

            if (name.contains(".broken.")) {
                // Tests that contain the name '.broken.' are expected to fail deployment
                // This is how the TCK verifies the container is doing the required error checking
                exception = (DeploymentException) new DeploymentException("deploy failed").initCause(e);
            } else {
                // This on the other hand is not good ....
                System.out.println("FIX Deployment of " + name);
                e.printStackTrace();
                exception = e;
            }

            return false;
        }
        return true;
    }

    private void writeToFile(File file, InputStream archive) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = archive.read(buffer)) > -1) {
                fos.write(buffer, 0, bytesRead);
            }
            Util.close(fos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(String name) {
        final File dir = new File(tmpDir, Math.random() + "");
        dir.mkdirs();
        dir.deleteOnExit();

        return new File(dir, name);
    }

    @Override
    public DeploymentException getDeploymentException() {
        try {
            return (DeploymentException) exception;
        } catch (Exception e) {
            System.out.println("BADCAST");
            return new DeploymentException("", exception);
        }
    }

    @Override
    public void undeploy(String name) throws IOException {
        System.out.println("Undeploying " + name);
        try {
            container.undeploy(name);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        File file = FILES.remove(name);
        System.out.println("deleting " + file.getAbsolutePath());
        FileUtils.deleteDirectory(file);
    }

    @Override
    public void setup() throws IOException {
        try {
            container.start();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void cleanup() throws IOException {
        try {
            container.stop();
        } catch (Exception e) {
            throw new IOException(e);
        }

        for (File f : FILES.values()) {
            FileUtils.deleteDirectory(f);
        }
        FILES.clear();
    }

    private static final class Util {
        static void close(Closeable closeable) throws IOException {
            if (closeable == null)
                return;
            try {
                if (closeable instanceof Flushable) {
                    ((Flushable) closeable).flush();
                }
            } catch (IOException e) {
                // no-op
            }
            try {
                closeable.close();
            } catch (IOException e) {
                // no-op
            }
        }
    }
}
