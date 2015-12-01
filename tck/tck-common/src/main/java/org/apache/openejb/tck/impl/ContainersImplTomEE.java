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
package org.apache.openejb.tck.impl;

import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.config.ValidationException;
import org.apache.openejb.loader.Options;
import org.apache.openejb.tck.OpenEJBTCKRuntimeException;
import org.apache.openejb.tck.util.ServerLocal;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.Containers;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ContainersImplTomEE extends AbstractContainers implements Containers {
    private static int count = 0;
    private final RemoteServer server;
    private Deployer deployer = null;
    private Exception exception;
    private AppInfo appInfo;
    private File currentFile = null;
    private final int port = ServerLocal.getPort(8080);

    private Deployer lookup() {
        final Options options = new Options(System.getProperties());
        final Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        props.put(Context.PROVIDER_URL, options.get(Context.PROVIDER_URL, "http://localhost:" + port + "/tomee/ejb"));

        final String deployerJndi = System.getProperty("openejb.deployer.jndiname", "openejb/DeployerBusinessRemote");

        try {
            final InitialContext context = new InitialContext(props);
            return (Deployer) context.lookup(deployerJndi);
        } catch (final Exception e) {
            throw new OpenEJBTCKRuntimeException(e);
        }
    }

    public ContainersImplTomEE() {
        System.out.println("ContainersImpl=" + ContainersImplTomEE.class.getName());
        System.out.println("Initialized ContainersImplTomEE " + (++count) + ", wait port = " + port);
        server = new RemoteServer();
        server.setPortStartup(this.port);
    }

    @Override
    public boolean deploy(final InputStream archive, final String name) throws IOException {
        exception = null;
        appInfo = null;

        System.out.println("Deploying " + archive + " with name " + name);

        currentFile = getFile(name);
        System.out.println(currentFile);
        writeToFile(currentFile, archive);
        try {
            if (deployer == null) {
                deployer = lookup();
            }
            appInfo = deployer.deploy(currentFile.getAbsolutePath());
        } catch (final Exception ex) {
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

    @Override
    public DeploymentException getDeploymentException() {
        try {
            return (DeploymentException) exception;
        } catch (final Exception e) {
            System.out.println("BADCAST");
            return new DeploymentException("", exception);
        }
    }

    @Override
    public void undeploy(final String name) throws IOException {
        if (appInfo == null) {
            if (!(exception instanceof DeploymentException)) {
                System.out.println("Nothing to undeploy" + name);
            }
            return;
        }

        System.out.println("Undeploying " + name);
        try {
            deployer.undeploy(appInfo.path);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new OpenEJBTCKRuntimeException(e);
        }

        final File toDelete;
        if (currentFile != null && (toDelete = currentFile.getParentFile()).exists()) {
            System.out.println("deleting " + toDelete.getAbsolutePath());
            delete(toDelete);
        }
    }

    protected File getFile(final String name) {
        final File dir = new File(tmpDir, Math.random() + "");
        if (!dir.exists() && !dir.mkdir()) {
            throw new RuntimeException("Failed to create directory: " + dir);
        }
        dir.deleteOnExit();
        return new File(dir, name);
    }

    @Override
    public void setup() throws IOException {
        System.out.println("Setup called");
        try {
            server.start(Arrays.asList("-Dopenejb.classloader.forced-load=org.apache.openejb.tck"), "start", true);
        } catch (final Exception e) {
            cleanup();
            throw e;
        }
        System.out.println("Started");
    }

    @Override
    public void cleanup() throws IOException {
        System.out.println("Cleanup called");
        server.destroy();
    }
}
